
package Business_Logics;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class DragandDropForITP implements Nlp {
	@InputParams({ @InputParam(name = "Container Element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Expected Text", type = "java.lang.String"),
			@InputParam(name = "mainRowXpath", type = "java.lang.String") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement containerElement = (WebElement) programElementsInput.get("Container Element");
		String expectedText = (String) programElementsInput.get("Expected Text");
		String mainRowXpath = (String) programElementsInput.get("mainRowXpath");
		double percentage = 0.00;
		WebDriver driver = nlpRequestModel.getWebDriver();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Duration timeOut = driver.manage().timeouts().getImplicitWaitTimeout();
		try {
			String xpath = containerElement.toString().substring(containerElement.toString().lastIndexOf("xpath: ") + 7,
					containerElement.toString().lastIndexOf("]"));
			percentage = getElementHeightPercentage(driver, xpath);
			System.out.println(percentage);
			JavascriptExecutor js = (JavascriptExecutor) driver;
			int i = 10;
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
			while (driver.findElements(By.xpath("//a[text()='" + expectedText + "']")).isEmpty()) {
				js.executeScript("arguments[0].style.height='" + percentage * ++i + "%'", xpath);
				Thread.sleep(1000);
			}
			List<WebElement> allElements = driver.findElements(By.xpath(mainRowXpath));
			System.out.println(allElements.get(0).getText());
			System.out.println(allElements.get(allElements.size() - 1).getText());
			new Actions(driver).scrollToElement(allElements.get(allElements.size() - 1))
					.moveToElement(allElements.get(allElements.size() - 1))
					.clickAndHold(allElements.get(allElements.size() - 1)).moveToElement(allElements.get(0)).release()
					.perform();
			Thread.sleep(1500);
			js.executeScript("arguments[0].style.height='" + percentage + "%'",
					driver.findElement(By.xpath("//section/div/div[@role=\"presentation\"]")));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Element found");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Element not found");
			nlpResponseModel.setStatus(CommonConstants.fail);
		} finally {
			driver.manage().timeouts().implicitlyWait(timeOut);
		}
		return nlpResponseModel;
	}

	public static double getElementHeightPercentage(WebDriver driver, String xpath) {
		WebElement element = driver.findElement(By.xpath(xpath));
		int elementHeight = element.getSize().height;
		WebElement parentElement = element.findElement(By.xpath(".."));
		int parentHeight = parentElement.getSize().height;
		return ((double) elementHeight / parentHeight) * 100;
	}

//	public static void main(String[] args) {
//		String cpath = "[[RemoteWebDriver: chrome on Windows 10 (acf6cfab4ce6f47ca4ddf07dcfcd7938)] -> xpath: //button[@title='Log in to your account']]";
//		System.out.println(cpath.substring(cpath.lastIndexOf("xpath: ") + 7, cpath.lastIndexOf("]")));
//	}
}