package Windows;



import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.windows.WindowsDriver;

public class ToClickAndCheckItWillThrowException implements Nlp {
	@InputParams({ @InputParam(name = "Locator Type", type = "java.lang.String"),
			@InputParam(name = "Locator Value", type = "java.lang.String"),
			@InputParam(name = "maxAttempt", type = "java.lang.String"),
			@InputParam(name = "explicitSeconds", type = "java.lang.String") })
	@ReturnType(name = "filePath", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String locatorType = (String) programElementsInput.get("Locator Type");
		String locatorValue = (String) programElementsInput.get("Locator Value");
		String maxAttempt = (String) programElementsInput.get("maxAttempt");
		String explicitSeconds = (String) programElementsInput.get("explicitSeconds");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Boolean value=false;
		try {
			try {
			
			By locator = getBy(locatorType, locatorValue);
			nlpRequestModel.getWebDriver().findElement(locator).click();

			value=true;
			
			}
			catch (org.openqa.selenium.ElementClickInterceptedException e) {
				value=false;
				}
			catch (org.openqa.selenium.ElementNotInteractableException e) {
				value=false;
				}
			
			catch (org.openqa.selenium.WebDriverException e) {
				value=false;
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Clicked on element with retry");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to click on element with retry " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("filePath", value);
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

}