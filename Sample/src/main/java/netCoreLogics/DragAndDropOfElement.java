package netCoreLogics;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
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




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP5acb1402-0cea-480c-874d-6007cd52c2a6")
public class DragAndDropOfElement implements Nlp {
	@InputParams({ @InputParam(name = "Source Element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Destination Element", type = "org.openqa.selenium.WebElement") })

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
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		WebElement fromElement = (WebElement) attributes.get("Source Element");
		WebElement toElement = (WebElement) attributes.get("Destination Element");

		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			dragAndDropObject(driver, fromElement, toElement);
			nlpResponseModel.setMessage("Successfully Moved the Element from Source to Destination");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to move==> " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	private static void dragAndDropObject(WebDriver driver, WebElement from, WebElement to) {
		Actions actions = new Actions(driver);
		Dimension size = to.getSize();
		int x = size.getWidth() / 2;
		int y = size.getHeight() / 2;
		Dimension size1 = from.getSize();
		int fx = size1.getWidth() / 2;
		int fy = size1.getHeight() / 2;
		actions.moveToElement(from, 0, 0).clickAndHold().moveToElement(from, fx, fy).moveToElement(to, x, y)
				.pause(Duration.ofMillis(500));
		actions.release().perform();
	}
}