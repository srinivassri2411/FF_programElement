package generic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
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

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP96e4b3fd-abfa-438f-8793-bce21fb8d708")
public class ExecuteJSandFetchDataOnElement implements Nlp {
	@InputParams({ @InputParam(name = "element", type = "java.lang.String"),
			@InputParam(name = "Script", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "java.lang.Object")

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
		String script = (String) attributes.get("Script");
		WebElement ele = (WebElement) attributes.get("element");
		Object returnValue = null;
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			returnValue = ((JavascriptExecutor) driver).executeScript(script, ele);
			nlpResponseModel.setMessage("Successfully executed javascript");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to execute ==> " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Result", returnValue);
		return nlpResponseModel;
	}
}