
package BrowserCapability;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import io.appium.java_client.ios.IOSDriver;

import java.util.*;

public class DisablePasswordPopup implements Nlp {
	@ReturnType(name = "mapValue", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Map<String, Object> chromePrefs = new HashMap<>();

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			chromePrefs.put("credentials_enable_service", false);
			chromePrefs.put("profile.password_manager_enabled", false);
			chromePrefs.put("profile.password_manager_leak_detection", false);
			chromePrefs.put("autofill.profile_enabled", false);
			nlpResponseModel.setMessage("Successfully added map");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setMessage("Failed to add map");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("mapValue", chromePrefs);
		return nlpResponseModel;
	}

}