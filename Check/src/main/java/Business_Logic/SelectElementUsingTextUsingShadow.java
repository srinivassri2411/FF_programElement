package Business_Logic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectElementUsingTextUsingShadow implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false),
			@InputParam(name = "Text", type = "java.lang.String") })

	@Override
	public List<String> getTestParameters() throws NlpException {
		return new ArrayList<>();
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		return new StringBuilder();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		List<Map> eles = (List<Map>) nlpRequestModel.getAttributes().get("Element");
		String text = (String) nlpRequestModel.getAttributes().get("Text");
		log.info("ELES =>" + eles);
		log.info("text =>" + text);
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			boolean clicked = false;
			for (Map ele : eles) {
				try {
					WebElement element = findShadowElement(ele, driver, shadow);
					log.info("Element =>" + element);
					if (element != null) {
						new Select(element).selectByIndex(2);
						clicked = true;
						break;
					}
				} catch (Exception e) {
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					log.info(writer.toString());
					// Log internally and try next locator in list
				}
			}
			if (clicked) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully clicked on the shadow element.");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Element found but click could not be verified.");
			}
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click: " + exception.toString());
		}
		return nlpResponseModel;
	}

	public WebElement findShadowElement(Map ele, WebDriver driver, Shadow shadow) {
		log.info("ele==>" + ele);
		String locatorName = (String) ele.get("LocatorName");
		String locatorValue = (String) ele.get("LocatorValue");
		if (locatorName.toLowerCase().contains("xpath")) {
			log.info("got locator in xpath");
			return shadow.findElementByXPath(locatorValue);
		} else if (locatorName.toLowerCase().contains("cssselector")) {
			log.info("got locator in css");
			return shadow.findElement(locatorValue);
		} else {
			log.info("got locator in Null");
			return null;
		}
	}

	public void clickOnCentreOfElement(WebElement element, WebDriver driver) {
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//
//        // This script is the most aggressive way to trigger a click on a PDF widget
//        String script = "var el = arguments[0];" + "el.scrollIntoView({block: 'center'});" + "el.focus();" + // Ensure
//                                                                                                                // element
//                                                                                                                // has
//                                                                                                                // focus
//                "var rect = el.getBoundingClientRect();" + "var x = rect.left + rect.width / 2;"
//                + "var y = rect.top + rect.height / 2;"
//                + "var init = {bubbles: true, cancelable: true, view: window, clientX: x, clientY: y, button: 0};"
//                + "/* Sequence: MouseDown -> Focus -> MouseUp -> Click */"
//                + "el.dispatchEvent(new MouseEvent('mousedown', init));"
//                + "el.dispatchEvent(new MouseEvent('mouseup', init));"
//                + "el.dispatchEvent(new MouseEvent('click', init));" + "return true;";
//
//        js.executeScript(script, element);

		element.click();
	}

	public static void main(String[] args) throws InterruptedException {
		// Use Firefox if that's your target, otherwise Chrome is fine
		// WebDriver d = new FirefoxDriver();
		ChromeOptions option = new ChromeOptions();
		option.setExperimentalOption("debuggerAddress", "localhost:" + "2705");
		option.addArguments("--remote-allow-origins=*");
		WebDriver d = new ChromeDriver(option);
		new Select(new Shadow(d).findElementByXPath("//select[@name='Shopify plan']")).selectByIndex(3);
//        d.manage().window().maximize();
//        d.get("https://unittest.cliniops.com/login/index/consent/signatory/INVESTIGATOR/subjectCommonId/233/study/2/subjectId/PT130/dfid/0?env=DEV");
//
//        System.out.println("Log in, open PDF, and once ready press ENTER...");
//        new Scanner(System.in).nextLine();
//
//        Shadow shadow = new Shadow(d);
//        SelectElementUsingTextUsingShadow obj = new SelectElementUsingTextUsingShadow();
//
////        Map<String, String> map = new HashMap<>();
////        map.put("LocatorName", "xpath");
////        // We use |2 to target the second visible signature widget
////        map.put("LocatorValue", "//*[contains(@class, 'PSPDFKit-Annotation-Widget-Text')]|2");
//        Map<String, String> map = new HashMap<>();
//        map.put("LocatorName", "xpath");
//        // Use 'Signature' instead of 'Text'
//        map.put("LocatorValue", "//*[contains(@class, 'PSPDFKit-Annotation-Widget-Signature')]|2");
//
//        try {
//            WebElement element = obj.findShadowElement(map, d, shadow);
//            obj.clickOnCentreOfElement(element, d);
//            System.out.println("Click script executed.");
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
	}
}