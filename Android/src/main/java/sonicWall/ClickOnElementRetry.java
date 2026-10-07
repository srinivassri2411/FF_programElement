
package sonicWall;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.windows.WindowsDriver;

public class ClickOnElementRetry implements Nlp {
	@InputParams({ @InputParam(name = "Locator Type", type = "java.lang.String"),
			@InputParam(name = "Locator Value", type = "java.lang.String"),
			@InputParam(name = "maxAttempt", type = "java.lang.String"),
			@InputParam(name = "explicitSeconds", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String locatorType = (String) programElementsInput.get("Locator Type");
		String locatorValue = (String) programElementsInput.get("Locator Value");
		String maxAttempt = (String) programElementsInput.get("maxAttempt");
		String explicitSeconds = (String) programElementsInput.get("explicitSeconds");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			safeClick(nlpRequestModel.getWebDriver(), locatorType, locatorValue, Integer.parseInt(maxAttempt),
					Integer.parseInt(explicitSeconds));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Clicked on element with retry");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to click on element with retry " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void safeClick(WebDriver driver, String locatorType, String locatorValue, int maxAttempt,
			int explicitSeconds) {
		By locator = getBy(locatorType, locatorValue);
		int attempts = 0;

		while (attempts < maxAttempt) {
			try {
				driver.getPageSource();
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(explicitSeconds));
				WebElement element = wait
						.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(locator)));

				Thread.sleep(250);

				try {
					new Actions(driver).moveToElement(element).click().perform();
					return;
				} catch (Exception ignored) {
					try {
						element.click();
						return;
					} catch (Exception ignored2) {
						if (driver instanceof WindowsDriver) {
							String elementXml = element.getAttribute("BoundingRectangle");
							int left = Integer.parseInt(elementXml.split("Left:")[1].split(" ")[0].trim());
							int top = Integer.parseInt(elementXml.split("Top:")[1].split(" ")[0].trim());
							int width = Integer.parseInt(elementXml.split("Width:")[1].split(" ")[0].trim());
							int height = Integer.parseInt(elementXml.split("Height:")[1].trim());
					 
							int x = left + width / 2;
							int y = top + height / 2;
					 
						

							Robot robot = new Robot();
							robot.mouseMove(x, y);
							robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							return;
						} else {
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
							return;
						}
					}
				}

			} catch (StaleElementReferenceException | TimeoutException | NoSuchElementException
					| ElementClickInterceptedException e) {
				attempts++;
			} catch (InterruptedException | AWTException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Thread interrupted", e);
			}
		}
		throw new RuntimeException("Failed to click element after retries: " + locator.toString());
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

}