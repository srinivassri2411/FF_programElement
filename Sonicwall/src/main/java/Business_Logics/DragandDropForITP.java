package Business_Logics;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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
		File screenShot = null;
		try {
			String xpath = containerElement.toString().substring(containerElement.toString().lastIndexOf("xpath: ") + 7,
					containerElement.toString().lastIndexOf("]"));
			percentage = getElementHeightPercentage(driver, xpath);
			System.out.println("Initial " + percentage);
			JavascriptExecutor js = (JavascriptExecutor) driver;
			int i = 50;
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
			long start = System.currentTimeMillis();
			do {
				double per = percentage * ++i;
				// log.info("This is the excution of drag and drop from pe "+per+"");
				System.out.println(per);
				String response = (String) js.executeScript("return arguments[0].style.height='" + per + "%'",
						driver.findElement(By.xpath(xpath)));
				System.out.println(response + "------" + per + "%");
				// log.info("This is the excution of drag and drop from pe "+response + "------"
				// + per + "%"+"");

				if (!response.equals(per + "%")) {
					screenShot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					throw new Exception("Element is not found while searching");
				}
				Thread.sleep(1000);
				if ((System.currentTimeMillis() - start) > 90000) {
					throw new NoSuchElementException(expectedText + " text is not present in the table.");
				}
			} while (driver.findElements(By.xpath("//a[text()='" + expectedText + "']")).isEmpty());
			List<WebElement> allElements = driver.findElements(By.xpath(mainRowXpath));
			new Actions(driver)
					.scrollToElement(driver.findElement(By.xpath("//a[text()='" + expectedText
							+ "']//ancestor::div[@role=\"row\"]//div[@class=\"ag-drag-handle ag-row-drag\"]")))
					.moveToElement(driver.findElement(By.xpath("//a[text()='" + expectedText
							+ "']//ancestor::div[@role=\"row\"]//div[@class=\"ag-drag-handle ag-row-drag\"]")))
					.clickAndHold(driver.findElement(By.xpath("//a[text()='" + expectedText
							+ "']//ancestor::div[@role=\"row\"]//div[@class=\"ag-drag-handle ag-row-drag\"]")))
					.moveToElement(allElements.get(0)).pause(Duration.ofMillis(700)).release().perform();
			Thread.sleep(700);
			js.executeScript("arguments[0].style.height='" + percentage + "%'", driver.findElement(By.xpath(xpath)));
			driver.manage().timeouts().implicitlyWait(timeOut);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Element found");
		} catch (Exception e) {
			screenShot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			List<String> list = new ArrayList<String>();
			if (screenShot.exists()) {
				list.add(screenShot.getAbsolutePath());
			}
			nlpResponseModel.setOutputLocalFilePaths(list);
			nlpResponseModel.setMessage("Element not found " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
			try {
				driver.manage().timeouts().implicitlyWait(timeOut);
			} catch (Exception s) {
				nlpResponseModel.setMessage("Element not found and failed while setting time" + e.getMessage());
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
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
}