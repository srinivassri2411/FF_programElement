package pamperClub;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP158852a2-fb30-4eb1-9724-6979e8cccd11")
public class MOB_SwitchToWindowBasedOnElement implements Nlp {
	@InputParams({ @InputParam(name = "Locator Type", type = "java.lang.String"),
			@InputParam(name = "Locator Value", type = "java.lang.String"),
			@InputParam(name = "Page Load Time Out", type = "java.lang.Long") })
	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String locatorType = (String) nlpRequestModel.getAttributes().get("Locator Type");
		String locatorValue = (String) nlpRequestModel.getAttributes().get("Locator Value");
		Long pageLoadTimeout = (Long) nlpRequestModel.getAttributes().get("Page Load Time Out");
		try {
			AndroidDriver androidDriver = nlpRequestModel.getAndroidDriver();
			Set<String> allWindowHandles = androidDriver.getWindowHandles();
			String currentWindowHandle = androidDriver.getWindowHandle();
			allWindowHandles.remove(currentWindowHandle);
			boolean isElementFound = switchTabBasedOnElement(locatorType, locatorValue, androidDriver, pageLoadTimeout,
					allWindowHandles);
			if (isElementFound) {
				System.out.println("Switched to browser window containing element with given locator.");
			} else {
				androidDriver.switchTo().window(currentWindowHandle);
				System.out.println("No browser window found containing element with given locator.");
			}
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to tap " + exception.toString());
		}
		return nlpResponseModel;
	}

	public boolean switchTabBasedOnElement(String locatorType, String locatorValue, AndroidDriver driver,
			Long pageLoadTimeout, Set<String> allWindowHandles) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		Duration originalTimeout = driver.manage().timeouts().getImplicitWaitTimeout();
		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
		boolean isElementFound = false;
		WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(pageLoadTimeout));
		try {
			for (String handle : allWindowHandles) {
				driver.switchTo().window(handle);
				try {
					webDriverWait.until(driver1 -> js.executeScript("return document.readyState").equals("complete"));
					driver.findElement(getElementBy(locatorType, locatorValue));
					isElementFound = true;
					break;
				} catch (Exception e) {
//	                    e.printStackTrace();
				}
			}
		} finally {
			driver.manage().timeouts().implicitlyWait(originalTimeout);
		}
		return isElementFound;
	}

	public By getElementBy(String locatorType, String locatorValue) {
		switch (locatorType.toLowerCase()) {
		case "id":
			return By.id(locatorValue);
		case "name":
			return By.name(locatorValue);
		case "class":
		case "classname":
			return By.className(locatorValue);
		case "tag":
		case "tagname":
			return By.tagName(locatorValue);
		case "xpath":
			return By.xpath(locatorValue);
		case "css":
		case "cssselector":
			return By.cssSelector(locatorValue);
		case "linktext":
			return By.linkText(locatorValue);
		case "partiallinktext":
			return By.partialLinkText(locatorValue);
		default:
			throw new IllegalArgumentException("Invalid locator type: " + locatorType);
		}
	}
}