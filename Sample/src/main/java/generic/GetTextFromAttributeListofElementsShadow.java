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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.github.sukgu.Shadow;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPa18a3d73-0c70-49e6-8568-583846389d0c")
public class GetTextFromAttributeListofElementsShadow implements Nlp {
	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String"),
			@InputParam(name = "Attribute Value", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.util.List")
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
		String av = (String) nlpRequestModel.getAttributes().get("Attribute Value");
		List<String> elements = null;
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			elements = getListText(shadow.findElementsByXPath(xpath), av);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched the list");
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch " + exception.toString());
		}
		nlpResponseModel.getAttributes().put(av, elements);
		return nlpResponseModel;
	}

	public List<String> getListText(List<WebElement> elements, String av) {
		java.util.LinkedList<String> texts = new java.util.LinkedList<String>();
		for (WebElement element : elements) {
			texts.add(element.getAttribute(av));
		}
		return texts;
	}
}