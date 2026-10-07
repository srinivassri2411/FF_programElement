
package reusable_utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class AddCapabilitiesToDisablePasswordBreachPopup implements Nlp {
	@InputParams({@InputParam(name = "DesiredCapabilities_In", type = "org.openqa.selenium.remote.DesiredCapabilities")})
	@ReturnType(name = "DesiredCapabilities_Out", type = "org.openqa.selenium.remote.DesiredCapabilities")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		try {
			DesiredCapabilities capabilityIn = (DesiredCapabilities) programElementsInput.get("DesiredCapabilities_In");
			String browserName = capabilityIn.getBrowserName();

			if (browserName.contains("chrome") ) {
				ChromeOptions options = new ChromeOptions();
				Map<String, Object> prefs = new HashMap<>();
				prefs.put("credentials_enable_service", false);
				prefs.put("profile.password_manager_enabled", false);
				prefs.put("profile.password_manager_leak_detection", false);
				prefs.put("autofill.profile_enabled", false);
				options.setExperimentalOption("prefs", prefs);
				capabilityIn.setCapability(ChromeOptions.CAPABILITY, options);
			} else if(browserName.contains("firefox")) {

			} else if(browserName.contains("edge")) {
				EdgeOptions options = new EdgeOptions();
				Map<String, Object> prefs = new HashMap<>();
				prefs.put("credentials_enable_service", false);
				prefs.put("profile.password_manager_enabled", false);
				prefs.put("profile.password_manager_leak_detection", false);
				prefs.put("autofill.profile_enabled", false);
				options.setExperimentalOption("prefs", prefs);
				capabilityIn.setCapability(EdgeOptions.CAPABILITY, options);
			}
			nlpResponseModel.getAttributes().put("DesiredCapabilities_Out", capabilityIn);
			nlpResponseModel.setMessage("Added additional capabilities.");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to add additional capabilities."+writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

}