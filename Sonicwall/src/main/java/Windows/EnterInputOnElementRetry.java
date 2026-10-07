
package Windows;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
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

public class EnterInputOnElementRetry implements Nlp {
	@InputParams({ @InputParam(name = "Locator Type", type = "java.lang.String"),
			@InputParam(name = "Locator Value", type = "java.lang.String"),
			@InputParam(name = "Input", type = "java.lang.String"),
			@InputParam(name = "maxAttempt", type = "java.lang.String"),
			@InputParam(name = "explicitSeconds", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String locatorType = (String) programElementsInput.get("Locator Type");
		String locatorValue = (String) programElementsInput.get("Locator Value");
		String input = (String) programElementsInput.get("Input");
		String maxAttempt = (String) programElementsInput.get("maxAttempt");
		String explicitSeconds = (String) programElementsInput.get("explicitSeconds");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			safeEnter(nlpRequestModel.getWebDriver(), locatorType, locatorValue, input, Integer.parseInt(maxAttempt),
					Integer.parseInt(explicitSeconds));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Entered input into element with retry");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to enter on element with retry " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void safeEnter(WebDriver driver, String locatorType, String locatorValue, String text, int maxAttempt,
			int explicitSeconds) {
		By locator = getBy(locatorType, locatorValue);
		int attempts = 0;

		while (attempts < maxAttempt) {
			try {
				driver.getPageSource();

				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(explicitSeconds));
				WebElement element = wait
						.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(locator)));

				Thread.sleep(250);
				try {
					element.click();
					element.sendKeys(text);
				} catch (Exception e) {
					new Actions(driver).moveToElement(element).click().sendKeys(text).perform();
				}
				return;
			} catch (StaleElementReferenceException | TimeoutException | NoSuchElementException
					| ElementClickInterceptedException e) {
				attempts++;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Thread interrupted during safeEnter", e);
			}
		}

		throw new RuntimeException("Failed to enter text after " + maxAttempt + " attempts: " + locator);
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