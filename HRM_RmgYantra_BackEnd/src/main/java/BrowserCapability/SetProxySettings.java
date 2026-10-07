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




public class SetProxySettings implements Nlp {
	@InputParams({ @InputParam(name = "Proxy IP address", type = "java.lang.String"),
			@InputParam(name = "Proxy Server Port Number", type = "java.lang.String"),
			@InputParam(name = "Desired Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities") })
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
		String ProxyIPaddress = (String) attributes.get("Proxy IP address");
		String ProxyServerPortNumber = (String) attributes.get("Proxy Server Port Number");
		DesiredCapabilities desiredCapabilities = (DesiredCapabilities) attributes.get("Desired Capabilities");
		try {
			ChromeOptions options = new ChromeOptions();
			   options.addArguments("--proxy-server=" + ProxyIPaddress.trim()+":"+ProxyServerPortNumber.trim());
			options.addArguments("--remote-allow-origins=*", "ignore-certificate-errors");

			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Proxy Added Successfully");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed To Add Proxy");
		}
		nlpResponseModel.getAttributes().put("DesiredCapabilities_out", desiredCapabilities);
		return nlpResponseModel;
	}
}