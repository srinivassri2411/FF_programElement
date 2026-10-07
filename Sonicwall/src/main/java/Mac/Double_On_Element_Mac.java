package Mac;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.mac.options.Mac2Options;

public class Double_On_Element_Mac implements Nlp {

    @InputParams({
        @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false)
    })

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel response = new NlpResponseModel();

        try {

            System.setProperty("java.awt.headless", "false");

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

            for (Map<String, Object> m : locatorMaps) {
                if (m == null) continue;

                Object nameObj = m.get("LocatorName");
                Object valObj = m.get("LocatorValue");

                if (nameObj == null || valObj == null) continue;

                String locatorType = String.valueOf(nameObj);
                String locatorValue = String.valueOf(valObj);

                try {
                    By by = getBy(locatorType, locatorValue);

                    if (by != null) {
                        element = driver.findElement(by);
                        if (element != null) {
                            usedLocatorInfo = locatorType + "=" + locatorValue;
                            break;
                        }
                    }
                } catch (Exception e) {
                    lastException = e;
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

            // Perform double click
            doubleOnCentreOfElement(element, driver);

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

    /** Click exactly at center of element using Robot */
    public void doubleOnCentreOfElement(WebElement element, WebDriver driver) {
        try {
            org.openqa.selenium.Point location = element.getLocation();
            int centerX = location.getX() + (element.getSize().getWidth() / 2);
            int centerY = location.getY() + (element.getSize().getHeight() / 2);

            Robot robot = new Robot();

            // Move mouse
            robot.mouseMove(centerX, centerY);

            // Small delay (important)
            robot.delay(100);

            // First click
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            // Very small delay (critical for double click)
            robot.delay(80);

            // Second click
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        } catch (AWTException e) {
            throw new RuntimeException("Robot initialization failed", e);
        }
    }

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

    public static void main(String[] args) throws InterruptedException, IOException, AWTException {

        AppiumDriver driver = null;

        Mac2Options options = new Mac2Options();
        options.setCapability("appium:automationName", "Mac2");
        options.setCapability("platformName", "mac");
        options.setCapability("appium:bundleId", "com.sonicwall.cse");
        options.setCapability("appium:newCommandTimeout", "0");
        options.setCapability("appium:noReset", "true");

        driver = new Mac2Driver(new URL("http://localhost:4723"), options);

        Thread.sleep(2000);

        WebElement element = driver.findElement(By.xpath(
                "//XCUIElementTypeStaticText[@value=\"Sign in to access your applications and services.\"]"));

        Thread.sleep(2000);

        element.click();
        element.click();
    }
}