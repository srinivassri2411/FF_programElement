package Mac;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

/**
 * NLP: Click on Element using repository locator list (Standard Selenium).
 * Input "Element" attribute:
 * List<Map<String, Object>> like:
 * [
 * {LocatorValue=..., LocatorName=id},
 * {LocatorValue=..., LocatorName=xpath},
 * ...
 * ]
 */
public class Click_On_Element_Mac implements Nlp {

    @InputParams({
        @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false)
    })

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel response = new NlpResponseModel();

        try {
            WebDriver driver = nlpRequestModel.getWebDriver();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> locatorMaps =
                    (List<Map<String, Object>>) nlpRequestModel.getAttributes().get("Element");

            if (locatorMaps == null || locatorMaps.isEmpty()) {
                response.setStatus(CommonConstants.fail);
                response.setMessage("No locators provided for the 'Element'");
                return response;
            }

            WebElement element = null;
            String usedLocatorInfo = null;
            Exception lastException = null;

            // 1. Iterate through locators until one works
            for (Map<String, Object> m : locatorMaps) {
                if (m == null) continue;

                Object nameObj = m.get("LocatorName");
                Object valObj = m.get("LocatorValue");

                if (nameObj == null || valObj == null) continue;

                String locatorType = String.valueOf(nameObj);
                String locatorValue = String.valueOf(valObj);

                try {
                    // Convert String type to Selenium By object
                    By by = getBy(locatorType, locatorValue);

                    if (by != null) {
                        element = driver.findElement(by);
                        if (element != null) {
                            usedLocatorInfo = locatorType + "=" + locatorValue;
                            break; // Found it!
                        }
                    }
                } catch (Exception e) {
                    lastException = e; // Store exception and try next locator
                }
            }

            if (element == null) {
                StringWriter sw = new StringWriter();
                if (lastException != null) {
                    lastException.printStackTrace(new PrintWriter(sw));
                }
                response.setStatus(CommonConstants.fail);
                response.setMessage("Unable to find element using any of the provided locators. "
                        + (lastException != null ? (" Last error: " + sw.toString()) : ""));
                return response;
            }

            // 2. Perform Click Action
            clickOnCentreOfElement(element, driver);

            response.setStatus(CommonConstants.pass);
            response.setMessage("Clicked on element using locator: " + usedLocatorInfo);
            return response;

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            response.setStatus(CommonConstants.fail);
            response.setMessage("Exception during execution: " + sw.toString());
            return response;
        }
    }

    /** Click exactly at center of element using Actions */
    public void clickOnCentreOfElement(WebElement element, WebDriver driver) {
        new Actions(driver).moveToElement(element).click().perform();
    }

    // --------------------------------------------------------------------
    //  Standard Locator Builder: Maps Strings to Selenium 'By'
    // --------------------------------------------------------------------

    private By getBy(String locatorType, String locatorValue) {
        String normalized = normalizeLocatorType(locatorType);

        switch (normalized) {
            case "xpath":
                return By.xpath(locatorValue);
            case "cssSelector":
                return By.cssSelector(locatorValue);
            case "id":
                return By.id(locatorValue);
            case "name":
                return By.name(locatorValue);
            case "className":
                return By.className(locatorValue);
            case "tagName":
                return By.tagName(locatorValue);
            case "linkText":
                return By.linkText(locatorValue);
            case "partialLinkText":
                return By.partialLinkText(locatorValue);
            default:
                throw new IllegalArgumentException("Unsupported locator type: " + locatorType);
        }
    }

    // --------------------------------------------------------------------
    //  Locator Type Normalizer
    // --------------------------------------------------------------------

    private String normalizeLocatorType(String locatorType) {
        if (locatorType == null) return "";

        String s = locatorType.replace(" ", "")
                              .replace("-", "")
                              .trim()
                              .toLowerCase();

        if (s.equals("id")) return "id";
        if (s.equals("name")) return "name";
        if (s.equals("xpath") || s.equals("xpth")) return "xpath";
        if (s.equals("css") || s.equals("cssselector")) return "cssSelector";
        if (s.equals("classname") || s.equals("class")) return "className";
        if (s.equals("linktext") || s.equals("link")) return "linkText";
        if (s.equals("partiallinktext") || s.equals("partiallink")) return "partialLinkText";
        if (s.equals("tagname") || s.equals("tag")) return "tagName";

        return locatorType;
    }

    public static void main(String[] args) {

        // 1. SETUP WEBDRIVER
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        try {
            // 2. NAVIGATE TO URL
            String url = "https://the-internet.herokuapp.com/add_remove_elements/";
            System.out.println("Navigating to: " + url);
            driver.get(url);

            // 3. PREPARE THE LIST OF MAPS
            List<Map<String, Object>> locators = new ArrayList<>();

            // Entry 1: Invalid ID (To test fallback)
            Map<String, Object> map1 = new HashMap<>();
            map1.put("LocatorName", "id");
            map1.put("LocatorValue", "wrong_id_should_fail");
            map1.put("LocatorMaskedValue", "");
            locators.add(map1);

            // Entry 2: Valid XPath (Should work)
            Map<String, Object> map2 = new HashMap<>();
            map2.put("LocatorName", "xpath");
            map2.put("LocatorValue", "//button[text()='Add Element']");
            map2.put("LocatorMaskedValue", "");
            locators.add(map2);

            // 4. MOCK REQUEST MODEL
            NlpRequestModel request = new NlpRequestModel() {
                private Map<String, Object> attributes = new HashMap<>();

                @Override
                public Map<String, Object> getAttributes() {
                    if (attributes.isEmpty()) {
                        attributes.put("Element", locators); // Matches the @InputParam name
                    }
                    return attributes;
                }

                @Override
                public WebDriver getWebDriver() {
                    return driver;
                }
            };

            // 5. EXECUTE THE NLP
            Click_On_Element_Mac nlp = new Click_On_Element_Mac();
            System.out.println("Executing NLP...");
            
            NlpResponseModel result = nlp.execute(request);

            // 6. PRINT RESULTS
            System.out.println("--------------------------------------------------");
            System.out.println("Status:  " + result.getStatus());
            System.out.println("Message: " + result.getMessage());
            System.out.println("--------------------------------------------------");

            Thread.sleep(2000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}