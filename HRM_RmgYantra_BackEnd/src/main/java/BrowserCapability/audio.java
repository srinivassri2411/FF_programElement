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


public class audio implements Nlp {
	@InputParams({ @InputParam(name = "WAV File", type = "java.lang.String"),
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

		 String file = (String) attributes.get("WAV File");
		
		DesiredCapabilities desiredCapabilities = (DesiredCapabilities) attributes.get("Desired Capabilities");
		try {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--remote-allow-origins=*");
			options.addArguments("--allow-file-access-from-files", "--use-fake-ui-for-media-stream",
					"--allow-file-access");
			options.addArguments("--use-fake-device-for-media-stream");
			options.addArguments("--use-file-for-fake-audio-capture" + file);

			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
			
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully Added Audio To The Browser");
		
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed Add Audio");
		}
		nlpResponseModel.getAttributes().put("DesiredCapabilities_out", desiredCapabilities);
		return nlpResponseModel;
	}
}