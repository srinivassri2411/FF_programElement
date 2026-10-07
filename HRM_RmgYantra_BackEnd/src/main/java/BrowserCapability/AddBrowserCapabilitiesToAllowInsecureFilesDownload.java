
package BrowserCapability;

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




public class AddBrowserCapabilitiesToAllowInsecureFilesDownload implements Nlp {
	@InputParams({ @InputParam(name = "DesiredCapabilities_in", type = "org.openqa.selenium.remote.DesiredCapabilities")})
	@ReturnType(name = "DesiredCapabilities_out", type = "org.openqa.selenium.remote.DesiredCapabilities")

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
		DesiredCapabilities desiredCapabilities = (DesiredCapabilities) attributes.get("DesiredCapabilities_in");

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--unsafely-treat-insecure-origin-as-secure=http://106.51.90.215:8084");
		desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		
		nlpResponseModel.setStatus(CommonConstants.pass);
		nlpResponseModel.setMessage("Added browser capabilities.");

		nlpResponseModel.getAttributes().put("DesiredCapabilities_out", desiredCapabilities);
		return nlpResponseModel;
	}
}