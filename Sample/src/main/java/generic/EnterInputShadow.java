package generic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP532a6c16-e301-4946-b298-4c5136104ed0")
public class EnterInputShadow implements Nlp {
	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String"),
			@InputParam(name = "Input", type = "java.lang.String") })
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
		String xpath = (String) nlpRequestModel.getAttributes().get("Xpath");
		String input = (String) nlpRequestModel.getAttributes().get("Input");
		WebElement element = null;
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			element = shadow.findElementByXPath(xpath);
			enterInputElement(element, input);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Entered into then given element");
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Enter " + exception.toString());
		}
		return nlpResponseModel;
	}

	public void enterInputElement(WebElement element, String input) {
		element.sendKeys(input);
	}
}