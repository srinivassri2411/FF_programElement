package custom_logic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class OpenBrowserWithDisableTelemetry implements Nlp {
	@InputParams({ @InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities") })
//	@ReturnType(name = "Driver", type = "org.openqa.selenium.WebDriver")

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
		String hubURL = (String) attributes.get("HubURL");
		DesiredCapabilities desiredCapabilities = (DesiredCapabilities) attributes.get("Capabilities");

		try {
			WebDriver driver = null;
			URL remoteAddress = new URL(hubURL);
			if (desiredCapabilities == null) {
				throw new Exception("Please ensure that capabilities are set before this step");
			} else {
				driver = new RemoteWebDriver(remoteAddress, desiredCapabilities, false);
			}

//			nlpResponseModel.getAttributes().put("Driver", driver);
			nlpResponseModel.setWebDriver(driver);
			nlpResponseModel.setMessage("Successfully Lanuch Browser");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to Open Browser ==> " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

}