
package Windows;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ScrollUntilElementIsVisible implements Nlp {
	@InputParams({ @InputParam(name = "LocatorType", type = "java.lang.String"),
			@InputParam(name = "LocatorValue", type = "java.lang.String"),
			@InputParam(name = "TimeOutSeconds", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String locatorType = (String) programElementsInput.get("LocatorType");
		String locatorValue = (String) programElementsInput.get("LocatorValue");
		String timeOutSeconds = (String) programElementsInput.get("TimeOutSeconds");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			boolean found = scroll(nlpRequestModel.getWebDriver(), locatorType, locatorValue, timeOutSeconds);
			if (found) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage(
						"Successfully scrolled till the element is visible " + locatorType + " = " + locatorValue);
			} else {
				nlpResponseModel.setMessage(
						"Failed to scroll till the element is visible " + locatorType + " = " + locatorValue);
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to scroll till the element is visible " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	private static By getBy(String type, String value) {
		switch (type.toLowerCase()) {
		case "id":
			return By.id(value);
		case "name":
			return By.name(value);
		case "xpath":
			return By.xpath(value);
		case "classname":
			return By.className(value);
		case "css":
			return By.cssSelector(value);
		default:
			throw new IllegalArgumentException("Unsupported locator type: " + type);
		}
	}

	public static boolean scroll(WebDriver driver, String locatorType, String locatorValue, String timeOutSeconds)
			throws AWTException {
		long start = System.currentTimeMillis();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		By by = getBy(locatorType, locatorValue);
		while (true) {
			try {
				if (driver.findElement(by).isDisplayed()) {
					return true;
				} else {
					System.out.println("element not found");
					Robot robot = new Robot();
					robot.keyPress(KeyEvent.VK_PAGE_DOWN);
					robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
				}
			} catch (NoSuchElementException | StaleElementReferenceException e) {
				System.out.println("exception element not found");
				Robot robot = new Robot();
				robot.keyPress(KeyEvent.VK_PAGE_DOWN);
				robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
			}
			if ((System.currentTimeMillis() - start) > Duration.ofSeconds(Long.parseLong(timeOutSeconds)).toMillis()) {
				return false;
			}
		}
	}
}