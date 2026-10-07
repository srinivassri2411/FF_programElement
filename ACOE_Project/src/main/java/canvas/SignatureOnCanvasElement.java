
package canvas;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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

public class SignatureOnCanvasElement implements Nlp {
	@InputParams({ @InputParam(name = "Signature Text", type = "java.lang.String"),
			@InputParam(name = "Canvas Element", type = "org.openqa.selenium.WebElement", findElement = false) })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String signatureText = (String) programElementsInput.get("Signature Text");
		List<Map<String, String>> canvasElement = (List<Map<String, String>>) programElementsInput
				.get("Canvas Element");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		WebDriver driver = nlpRequestModel.getWebDriver();
		WebElement canvas = driver.findElement(By.xpath(canvasElement.get(0).get("LocatorValue")));
		try {
			int txtLength = signatureText.length();
			int partLength = (txtLength * 25) / 6;
			int startX = 50;
			int startY = 50;
			((JavascriptExecutor) driver)
					.executeScript("var ctx = arguments[0].getContext('2d');" + "ctx.font = '40px Segoe Script';" +
							"ctx.fillStyle = 'green';" +
							"ctx.fillText('" + signatureText + "', " + startX + ", " + startY + ");" +
							"ctx.beginPath();" + "ctx.lineWidth = 3;" +
							"ctx.strokeStyle = 'green';" + "ctx.moveTo(" + startX + ", " + (startY + 15) + ");" + 
							"ctx.quadraticCurveTo(" + (startX + partLength) + ", " + (startY + 20) + ", "
							+ (startX + (2 * partLength)) + ", " + (startY + 15) + ");" +
							"ctx.quadraticCurveTo(" + (startX + (3 * partLength)) + ", " + (startY + 10) + ", "
							+ (startX + (4 * partLength)) + ", " + (startY + 15) + ");" + 
							"ctx.quadraticCurveTo(" + (startX + (5 * partLength)) + ", " + (startY + 20) + ", "
							+ (startX + (6 * partLength)) + ", " + (startY + 15) + ");" + 
							"ctx.stroke();", canvas);
			Dimension dim = canvas.getSize();
			int height = dim.height;
			int width = dim.width;
			Actions builder = new Actions(driver);
			Actions act = builder.moveToElement(canvas, -(width / 2) + (startX + (6 * partLength) + 5),
					-(height / 2) + (startY + 15)).clickAndHold();
			act.moveToElement(canvas, -(width / 2) + (startX + (6 * partLength) + 10), -(height / 2) + (startY + 15))
					.release().build().perform();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Signed on the canvas Element");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Sign on the canvas Element");
		}
		return nlpResponseModel;
	}

}