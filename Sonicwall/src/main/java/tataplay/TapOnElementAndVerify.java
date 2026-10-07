package tataplay;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;

public class TapOnElementAndVerify implements Nlp {

	@InputParams({ @InputParam(name = "Tap Element", type = "org.openqa.selenium.WebElement", findElement = false),
			@InputParam(name = "Verify Element", type = "org.openqa.selenium.WebElement", findElement = false),
			@InputParam(name = "Max Retry", type = "java.lang.Integer", findElement = false) })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		List<Map> tapElement = (List<Map>) programElementsInput.get("Tap Element");

		List<Map> verifyElement = (List<Map>) programElementsInput.get("Verify Element");

		Integer maxRetry = (Integer) programElementsInput.get("Max Retry");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {

			AndroidDriver driver = nlpRequestModel.getAndroidDriver();

			if (maxRetry == null || maxRetry <= 0) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Max Retry must be greater than 0.");
				return nlpResponseModel;
			}

			if (tapElement == null || tapElement.isEmpty()) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Tap Element locator is missing.");
				return nlpResponseModel;
			}

			if (verifyElement == null || verifyElement.isEmpty()) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Verify Element locator is missing.");
				return nlpResponseModel;
			}

			for (int attempt = 1; attempt <= maxRetry; attempt++) {

				for (Map ele : tapElement) {

					String locatorName = (String) ele.get("LocatorName");

					String locatorValue = (String) ele.get("LocatorValue");

					By locator = getBy(locatorName, locatorValue);

					// Try click
					if (tryElementClick(locator, driver)) {

						if (waitForVerifyElement(verifyElement, driver)) {

							nlpResponseModel.setStatus(CommonConstants.pass);

							nlpResponseModel.setMessage("Successfully tapped and verified "
									+ "using element.click() on attempt " + attempt + " of " + maxRetry + ".");

							return nlpResponseModel;
						}
					}

					// Try pointer tap
					if (tryPointerTap(locator, driver)) {

						if (waitForVerifyElement(verifyElement, driver)) {

							nlpResponseModel.setStatus(CommonConstants.pass);

							nlpResponseModel.setMessage("Successfully tapped and verified "
									+ "using pointer input on attempt " + attempt + " of " + maxRetry + ".");

							return nlpResponseModel;
						}
					}
				}

				if (waitForVerifyElement(verifyElement, driver)) {

					nlpResponseModel.setStatus(CommonConstants.pass);

					nlpResponseModel.setMessage("Verify element is visible after attempt " + attempt + ".");

					return nlpResponseModel;
				}

				if (attempt < maxRetry) {
					Thread.sleep(300);
				}
			}

			nlpResponseModel.setStatus(CommonConstants.fail);

			nlpResponseModel.setMessage("Failed to tap and verify the element after " + maxRetry + " attempt(s).");

		} catch (Exception e) {

			StringWriter exception = new StringWriter();

			e.printStackTrace(new PrintWriter(exception));

			nlpResponseModel.setStatus(CommonConstants.fail);

			nlpResponseModel.setMessage("Failed to tap and verify: " + exception.toString());
		}

		return nlpResponseModel;
	}

	private boolean tryElementClick(By locator, AndroidDriver driver) {

		try {

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

			WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));

			element.click();

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	private boolean tryPointerTap(By locator, AndroidDriver driver) {

		try {

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

			Point location = element.getLocation();

			int centerX = location.getX() + (element.getSize().getWidth() / 2);

			int centerY = location.getY() + (element.getSize().getHeight() / 2);

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

			Sequence tap = new Sequence(finger, 0);

			tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY));

			tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

			tap.addAction(new Pause(finger, Duration.ofMillis(100)));

			tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(Collections.singletonList(tap));

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	private boolean waitForVerifyElement(List<Map> verifyElement, AndroidDriver driver) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

		for (Map ele : verifyElement) {

			try {

				String locatorName = (String) ele.get("LocatorName");

				String locatorValue = (String) ele.get("LocatorValue");

				By locator = getBy(locatorName, locatorValue);

				WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

				if (element.isDisplayed()) {
					return true;
				}

			} catch (Exception e) {
				// Try next locator
			}
		}

		return false;
	}

	private static By getBy(String type, String value) {

		if (type == null || value == null) {
			throw new IllegalArgumentException("Locator type/value cannot be null.");
		}

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

}