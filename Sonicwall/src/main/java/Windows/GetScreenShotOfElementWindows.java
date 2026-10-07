package Windows;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.windows.WindowsDriver;

public class GetScreenShotOfElementWindows implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement") })
	@ReturnType(name = "filePath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement element = (WebElement) programElementsInput.get("Element");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getScreenShot(element, nlpRequestModel.getWebDriver());
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully taken element screenshot");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Successfully taken element screenshot");
		}
		nlpResponseModel.getAttributes().put("filePath", returnValue);
		return nlpResponseModel;
	}

	public static String getScreenShot(WebElement element, WebDriver driver) throws Exception {
		File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		File tempFile = File.createTempFile("FF_ElementScreenshot", ".png");
		String elementXml = element.getAttribute("BoundingRectangle");
		System.out.println("BoundingRectangle: " + elementXml);
		int left = Integer.parseInt(elementXml.split("Left:")[1].split(" ")[0].trim());
		int top = Integer.parseInt(elementXml.split("Top:")[1].split(" ")[0].trim());
		int width = Integer.parseInt(elementXml.split("Width:")[1].split(" ")[0].trim());
		int height = Integer.parseInt(elementXml.split("Height:")[1].trim());
		BufferedImage img = ImageIO.read(screenshot);
		BufferedImage cropped = img.getSubimage(left, top, width, height);
		ImageIO.write(cropped, "png", tempFile);
		return tempFile.getAbsolutePath();
	}

	public static void main(String[] args) throws Exception {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:app", "Root");
		capabilities.setCapability("appium:platformName", "Windows");
		capabilities.setCapability("appium:deviceName", "WindowsPC");
		capabilities.setCapability("appium:automationName", "Windows");
		capabilities.setCapability("appium:newCommandTimeout", 3600);
		WebDriver driver = new WindowsDriver(new URL("http://localhost:4723/wd/hub"), capabilities);
		System.out.println(getScreenShot(
				driver.findElement(By.xpath("//*[@Name='RealVNC Viewer']//*[@LocalizedControlType='image']")), driver));
	}
}