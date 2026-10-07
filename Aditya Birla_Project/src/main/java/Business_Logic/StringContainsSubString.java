package Business_Logic;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class StringContainsSubString implements Nlp {
	@InputParams({ @InputParam(name = "Main String", type = "java.lang.String"),
			@InputParam(name = "Sub String", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.Boolean")
	
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
		String mainString = (String) attributes.get("Main String");
		String subString = (String) attributes.get("Sub String");

		boolean result = false;
		WebDriver driver = nlpRequestModel.getWebDriver();
		Duration implicitWait = driver.manage().timeouts().getImplicitWaitTimeout();
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
			if (mainString.contains(subString)) {
	            result=true;
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("The main string "+mainString+" contains the substring "+subString);
	        } else {
	        	result=false;
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("The main string "+mainString+" not contain the substring "+subString);
	        }

			
		}

		catch (Exception e) {
			result=false;
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("The main string "+mainString+" not contain the substring "+subString);
		}
		finally {
			driver.manage().timeouts().implicitlyWait(implicitWait);
		}

		nlpResponseModel.getAttributes().put("result", result);
		return nlpResponseModel;
	}
}