
package Windows;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import javax.imageio.ImageIO;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GetElementScreenShot implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement") })
	@ReturnType(name = "returnFile", type = "java.lang.String")

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
			StringWriter write = new StringWriter();
			e.printStackTrace(new PrintWriter(write));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed taken element screenshot " + write);
		}
		nlpResponseModel.getAttributes().put("returnFile", returnValue);
		return nlpResponseModel;
	}

	public String getScreenShot(WebElement element, WebDriver driver) throws Exception {
		File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		String elementXml = element.getAttribute("BoundingRectangle");
		System.out.println("BoundingRectangle: " + elementXml);
		int left = Integer.parseInt(elementXml.split("Left:")[1].split(" ")[0].trim());
		int top = Integer.parseInt(elementXml.split("Top:")[1].split(" ")[0].trim());
		int width = Integer.parseInt(elementXml.split("Width:")[1].split(" ")[0].trim());
		int height = Integer.parseInt(elementXml.split("Height:")[1].trim());
		BufferedImage img = ImageIO.read(screenshot);
		BufferedImage cropped = img.getSubimage(left, top, width, height);
		ImageIO.write(cropped, "png", screenshot);
		Thread.sleep(1500);
		String filePath = screenshot.getAbsolutePath();
		return filePath;
	}
}