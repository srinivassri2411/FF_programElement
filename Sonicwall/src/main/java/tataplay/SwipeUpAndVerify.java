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

public class SwipeUpAndVerify implements Nlp {

	@InputParams({ @InputParam(name = "Swipe Element", type = "org.openqa.selenium.WebElement", findElement = false),
			@InputParam(name = "Verify Element", type = "org.openqa.selenium.WebElement", findElement = false),
			@InputParam(name = "Max Retry", type = "java.lang.Integer", findElement = false) })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		List<Map> swipeElement = (List<Map>) programElementsInput.get("Swipe Element");

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

			if (swipeElement == null || swipeElement.isEmpty()) {

				nlpResponseModel.setStatus(CommonConstants.fail);

				nlpResponseModel.setMessage("Swipe Element locator is missing.");

				return nlpResponseModel;
			}

			if (verifyElement == null || verifyElement.isEmpty()) {

				nlpResponseModel.setStatus(CommonConstants.fail);

				nlpResponseModel.setMessage("Verify Element locator is missing.");

				return nlpResponseModel;
			}
			for (int attempt = 1; attempt <= maxRetry; attempt++) {

				for (Map ele : swipeElement) {

					String locatorName = (String) ele.get("LocatorName");

					String locatorValue = (String) ele.get("LocatorValue");

					By swipeLocator = getBy(locatorName, locatorValue);

//					if (isVerifyElementVisible(verifyElement, driver)) {
//
//						nlpResponseModel.setStatus(CommonConstants.fail);
//
//						nlpResponseModel.setMessage("Verify element is already visible before "
//								+ "swipe. Swipe action cannot be confirmed.");
//
//						return nlpResponseModel;
//					}

					boolean swipePerformed = performSwipeUp(swipeLocator, driver);

					if (!swipePerformed) {
						continue;
					}

					if (waitForVerifyElement(verifyElement, driver)) {

						nlpResponseModel.setStatus(CommonConstants.pass);

						nlpResponseModel.setMessage("Swipe up successfully performed and "
								+ "verify element appeared on attempt " + attempt + " of " + maxRetry + ".");

						return nlpResponseModel;
					}
				}

				if (attempt < maxRetry) {
					Thread.sleep(500);
				}
			}
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Swipe up was not successfully verified after " + maxRetry + " attempt(s).");

		} catch (Exception e) {

			StringWriter exception = new StringWriter();

			e.printStackTrace(new PrintWriter(exception));

			nlpResponseModel.setStatus(CommonConstants.fail);

			nlpResponseModel.setMessage("Failed to swipe up and verify: " + exception.toString());
		}

		return nlpResponseModel;
	}

	private boolean performSwipeUp(By locator, AndroidDriver driver) {

		try {

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

			WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

			Point location = element.getLocation();

			int width = element.getSize().getWidth();

			int height = element.getSize().getHeight();

			int centerX = location.getX() + (width / 2);
			int startY = location.getY() + (int) (height * 0.80);
			int endY = location.getY() + (int) (height * 0.20);
			if (startY <= endY) {
				return false;
			}

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

			Sequence swipe = new Sequence(finger, 0);

			swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, startY));

			swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

			swipe.addAction(new Pause(finger, Duration.ofMillis(150)));

			swipe.addAction(
					finger.createPointerMove(Duration.ofMillis(800), PointerInput.Origin.viewport(), centerX, endY));

			swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

			driver.perform(Collections.singletonList(swipe));

			return true;

		} catch (Exception e) {

			return false;
		}
	}

	private boolean isVerifyElementVisible(List<Map> verifyElement, AndroidDriver driver) {

		for (Map ele : verifyElement) {

			try {

				String locatorName = (String) ele.get("LocatorName");

				String locatorValue = (String) ele.get("LocatorValue");

				By locator = getBy(locatorName, locatorValue);

				List<WebElement> elements = driver.findElements(locator);

				for (WebElement element : elements) {

					if (element.isDisplayed()) {
						return true;
					}
				}

			} catch (Exception e) {

				// Try next locator
			}
		}

		return false;
	}

	private boolean waitForVerifyElement(List<Map> verifyElement, AndroidDriver driver) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

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
				wait = new WebDriverWait(driver, Duration.ofSeconds(1));
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
