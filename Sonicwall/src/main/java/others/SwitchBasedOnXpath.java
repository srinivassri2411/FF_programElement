
package others;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class SwitchBasedOnXpath implements Nlp {
	@InputParams({ @InputParam(name = "xpath", type = "java.lang.String"),
			@InputParam(name = "timeout", type = "java.lang.String") })
//	@ReturnType(name = "isSwitched", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String xpath = (String) programElementsInput.get("xpath");
		String timeout = (String) programElementsInput.get("timeout");
//		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Set<String> allWindowHandles = driver.getWindowHandles();
			String currentWindowHandle = driver.getWindowHandle();
			allWindowHandles.remove(currentWindowHandle);
			boolean isElementFound = switchWindow(driver, Long.parseLong(timeout), xpath, allWindowHandles);
			if (isElementFound) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Switched to browser window containing element");
			} else {
				driver.switchTo().window(currentWindowHandle);
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("No browser window found containing element");
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to switch " + e);

		}
//		nlpResponseModel.getAttributes().put("isSwitched", returnValue);
		return nlpResponseModel;
	}

	private boolean switchWindow(WebDriver driver, Long pageLoadTimeOut, String xpath, Set<String> allWindowHandles) {
		long seconds = driver.manage().timeouts().getImplicitWaitTimeout().getSeconds();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
		boolean isElementFound = false;
		WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(pageLoadTimeOut));
		for (String handle : allWindowHandles) {
			driver.switchTo().window(handle);
			try {
				webDriverWait.until((ExpectedCondition<Boolean>) wait -> String
						.valueOf(js.executeScript("return document.readyState;")).equals("complete"));
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(pageLoadTimeOut));
				driver.findElement(By.xpath(xpath));
				isElementFound = true;
				break;
			} catch (Exception ignored) {
			}
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
		return isElementFound;
	}
}