package shadowUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;

public class EnterTextInSpecifiedShadowElement implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false),
			@InputParam(name = "Value", type = "java.lang.String") })

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
		String textToEnter = (String) nlpRequestModel.getAttributes().get("Value");

		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			boolean success = false;

			for (Map ele : eles) {
				try {
					WebElement element = findShadowElement(ele, driver, shadow);
					if (element != null) {
						enterTextDeepInteraction(element, driver, textToEnter);
						success = true;
						break;
					}
				} catch (Exception e) {
					System.out.println("Retry-able error: " + e.getMessage());
				}
			}

			if (success) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully entered text: " + textToEnter);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Could not interact with the investigator_name field.");
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Execution failed: " + sw.toString());
		}
		return nlpResponseModel;
	}

	public WebElement findShadowElement(Map ele, WebDriver driver, Shadow shadow) {
		String locatorName = (String) ele.get("LocatorName");
		String locatorValue = (String) ele.get("LocatorValue");

		String cleanLocator = locatorValue;
		int preferredIndex = 0;

		if (locatorValue.contains("|")) {
			String[] parts = locatorValue.split("\\|");
			cleanLocator = parts[0];
			preferredIndex = Integer.parseInt(parts[1]) - 1;
		}

		List<WebElement> elements = locatorName.toLowerCase().contains("xpath")
				? shadow.findElementsByXPath(cleanLocator)
				: shadow.findElements(cleanLocator);

		if (elements == null || elements.isEmpty()) {
			throw new NoSuchElementException("No elements found for: " + cleanLocator);
		}

		// ADAPTIVE INDEX LOGIC:
		// Handles cases where sometimes 1 element matches and sometimes 2.
		// It will take the preferred index if available, otherwise it takes the last
		// available match.
		int finalIndex = (elements.size() > preferredIndex) ? preferredIndex : (elements.size() - 1);

		System.out.println("Matched " + elements.size() + " elements. Targeting index: " + (finalIndex + 1));
		return elements.get(finalIndex);
	}

	public void enterTextDeepInteraction(WebElement element, WebDriver driver, String text) {
//		JavascriptExecutor js = (JavascriptExecutor) driver;
//
//		// This script uses the native prototype setter to bypass framework locks and
//		// dispatches InputEvents with a data payload to trigger the PDF save logic.
//		String script = "var el = arguments[0];" + "var val = arguments[1];" +
//
//		// 1. Highlight and Visibility (Bypass opacity: 0)
//				"el.style.border = '3px solid red';" + "el.style.backgroundColor = 'yellow';"
//				+ "el.style.opacity = '1';" + "el.scrollIntoView({block: 'center'});" + "el.focus();" +
//
//				// 2. Native Setter: Critical for React/PSPDFKit integration
//				"var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
//				+ "setter.call(el, val);" +
//
//				// 3. Dispatch Events with Data payload
//				"var init = { bubbles: true, cancelable: true, data: val };"
//				+ "el.dispatchEvent(new InputEvent('beforeinput', init));"
//				+ "el.dispatchEvent(new InputEvent('input', init));"
//				+ "el.dispatchEvent(new Event('change', { bubbles: true }));" +
//
//				// 4. Blur to commit
//				"el.blur();" + "return true;";
//
//		js.executeScript(script, element, text);

		element.sendKeys(text);
	}

//    public static void main(String[] args) throws InterruptedException {
//        WebDriver d = new ChromeDriver();
//        d.manage().window().maximize();
//        d.get("https://ctmstaging.cliniops.com");
//
//        System.out.println("Log in and open PDF. Press ENTER when ready...");
//        new Scanner(System.in).nextLine();
//
//        Shadow shadow = new Shadow(d);
//        EnterTextInShadowElement obj = new EnterTextInShadowElement();
//
//        Map<String, String> map = new HashMap<>();
//        map.put("LocatorName", "xpath");
//        // We use the 'name' attribute which gives 1 or 2 matches.
//        map.put("LocatorValue", "//input[@name='investigator_name']|2");
//
//        try {
//            WebElement element = obj.findShadowElement(map, d, shadow);
//            obj.enterTextDeepInteraction(element, d, "Investigator Test Entry");
//            System.out.println("Script finished. Verify if the yellow field contains the text.");
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//    }
}