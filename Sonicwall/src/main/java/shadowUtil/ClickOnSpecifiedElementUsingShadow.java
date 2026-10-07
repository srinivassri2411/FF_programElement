package shadowUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;

public class ClickOnSpecifiedElementUsingShadow implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false) })

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
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			boolean clicked = false;
			for (Map ele : eles) {
				try {
					WebElement element = findShadowElement(ele, driver, shadow);
					if (element != null) {
						clickOnCentreOfElement(element, driver);
						clicked = true;
						break;
					}
				} catch (Exception e) {
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
		String locatorName = (String) ele.get("LocatorName");
		String locatorValue = (String) ele.get("LocatorValue");

		String cleanLocator = locatorValue;
		int index = 0;

		if (locatorValue.contains("|")) {
			String[] parts = locatorValue.split("\\|");
			cleanLocator = parts[0];
			index = Integer.parseInt(parts[1]) - 1;
		}

		List<WebElement> elements;
		if (locatorName.toLowerCase().contains("xpath")) {
			elements = shadow.findElementsByXPath(cleanLocator);
		} else if (locatorName.toLowerCase().contains("cssselector")) {
			elements = shadow.findElements(cleanLocator);
		} else {
			return null;
		}

		// Filtering for VISIBLE elements only to ensure index 2 is the 2nd one you SEE
		List<WebElement> visibleElements = new ArrayList<>();
		for (WebElement e : elements) {
			if (e.isDisplayed()) {
				visibleElements.add(e);
			}
		}

		if (visibleElements.size() > index) {
			return visibleElements.get(index);
		} else {
			throw new NoSuchElementException(
					"Found " + visibleElements.size() + " visible elements, but needed index " + (index + 1));
		}
	}

	public void clickOnCentreOfElement(WebElement element, WebDriver driver) {
//		JavascriptExecutor js = (JavascriptExecutor) driver;
//
//		// This script is the most aggressive way to trigger a click on a PDF widget
//		String script = "var el = arguments[0];" + "el.scrollIntoView({block: 'center'});" + "el.focus();" + // Ensure
//																												// element
//																												// has
//																												// focus
//				"var rect = el.getBoundingClientRect();" + "var x = rect.left + rect.width / 2;"
//				+ "var y = rect.top + rect.height / 2;"
//				+ "var init = {bubbles: true, cancelable: true, view: window, clientX: x, clientY: y, button: 0};"
//				+ "/* Sequence: MouseDown -> Focus -> MouseUp -> Click */"
//				+ "el.dispatchEvent(new MouseEvent('mousedown', init));"
//				+ "el.dispatchEvent(new MouseEvent('mouseup', init));"
//				+ "el.dispatchEvent(new MouseEvent('click', init));" + "return true;";
//
//		js.executeScript(script, element);
		
		element.click();
	}

	public static void main(String[] args) throws InterruptedException {
		// Use Firefox if that's your target, otherwise Chrome is fine
		// WebDriver d = new FirefoxDriver();
		WebDriver d = new ChromeDriver();

		d.manage().window().maximize();
		d.get("https://unittest.cliniops.com/login/index/consent/signatory/INVESTIGATOR/subjectCommonId/233/study/2/subjectId/PT130/dfid/0?env=DEV");

		System.out.println("Log in, open PDF, and once ready press ENTER...");
		new Scanner(System.in).nextLine();

		Shadow shadow = new Shadow(d);
		ClickOnSpecifiedElementUsingShadow obj = new ClickOnSpecifiedElementUsingShadow();

//        Map<String, String> map = new HashMap<>();
//        map.put("LocatorName", "xpath");
//        // We use |2 to target the second visible signature widget
//        map.put("LocatorValue", "//*[contains(@class, 'PSPDFKit-Annotation-Widget-Text')]|2");
		Map<String, String> map = new HashMap<>();
		map.put("LocatorName", "xpath");
		// Use 'Signature' instead of 'Text'
		map.put("LocatorValue", "//*[contains(@class, 'PSPDFKit-Annotation-Widget-Signature')]|2");

		try {
			WebElement element = obj.findShadowElement(map, d, shadow);
			obj.clickOnCentreOfElement(element, d);
			System.out.println("Click script executed.");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}