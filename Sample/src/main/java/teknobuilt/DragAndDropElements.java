package teknobuilt;

import java.io.PrintWriter;
import java.io.StringWriter;
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

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP911871fe-06e1-44bb-b30b-a230a85898d7")
@Slf4j
public class DragAndDropElements implements Nlp {
	@InputParams({ @InputParam(name = "From Element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "To Element", type = "org.openqa.selenium.WebElement") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement source = (WebElement) programElementsInput.get("From Element");
		WebElement to = (WebElement) programElementsInput.get("To Element");
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			dragAndDrop(driver, source, to);
			nlpResponseModel.setMessage("Sucessfully drag and dropped the given elements");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed=> " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

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

	public static void dragAndDrop(WebDriver driver, WebElement source, WebElement des) {
		Actions actions = new Actions(driver);
		Dimension size1 = source.getSize();
		int fx = size1.getWidth() / 2;
		int fy = size1.getHeight() / 2;
		actions.scrollToElement(source).moveToElement(source, 0, 0).clickAndHold().moveToElement(source, fx, fy)
				.scrollToElement(des).moveToElement(des);
		actions.release().perform();
	}
}