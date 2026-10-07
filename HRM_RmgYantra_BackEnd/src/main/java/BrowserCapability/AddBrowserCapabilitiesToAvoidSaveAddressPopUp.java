
package BrowserCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class AddBrowserCapabilitiesToAvoidSaveAddressPopUp implements Nlp {
	@InputParams({
		@InputParam(name = "DesiredCapabilities_in", type = "org.openqa.selenium.remote.DesiredCapabilities"),
		@InputParam(name = "Browser_name", type = "java.lang.String") })
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
		String browserName = (String) attributes.get("Browser_name");

		if (browserName.contains("chrome")) {
			ChromeOptions options = new ChromeOptions();
			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("autofill.profile_enabled", false);
			options.setExperimentalOption("prefs", prefs);
			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);

		} else if(browserName.contains("firefox")) {
			FirefoxOptions options = new FirefoxOptions();
	        FirefoxProfile profile = new FirefoxProfile();
	        profile.setPreference("browser.formfill.enable", false);
	        options.setProfile(profile);
			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);

		} else if(browserName.contains("edge")){
			EdgeOptions	options = new EdgeOptions();
			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("autofill.profile_enabled", false);
			options.setExperimentalOption("prefs", prefs);
			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		}
		
		nlpResponseModel.setStatus(CommonConstants.pass);
		nlpResponseModel.setMessage("Added browser capabilities.");
		nlpResponseModel.getAttributes().put("DesiredCapabilities_out", desiredCapabilities);
		return nlpResponseModel;
	}
}