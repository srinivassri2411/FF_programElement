package TCSPOC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class option implements Nlp {
	@InputParams({ @InputParam(name = "Cap", type = "org.openqa.selenium.remote.DesiredCapabilities") })
	@ReturnType(name = "string3", type = "org.openqa.selenium.remote.DesiredCapabilities")

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
		DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Cap");
		try {
			ChromeOptions options = new ChromeOptions();

			// Add any desired options to the ChromeOptions object
			options.addArguments("--start-maximized");
			cap.setCapability(ChromeOptions.CAPABILITY, options);
			nlpResponseModel.setMessage("pass");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// TODO: handle exception
			nlpResponseModel.setMessage("Failed" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
//          String string2 = (String) attributes.get("string2");

		// Your program element business logic goes here ...

//          String string3 = "Return Value";
		nlpResponseModel.getAttributes().put("string3", cap);
		return nlpResponseModel;
	}
}