package businessLogics;
 
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
 
public class disablePopup implements Nlp {
	@ReturnType(name = "MapVal", type = "java.util.Map")
 
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
 
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Map<String, Object> chromePrefs = new HashMap<String, Object>();
 
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			chromePrefs.put("credentials_enable_service", false);
			chromePrefs.put("profile.password_manager_enabled", false);
			chromePrefs.put("profile.password_manager_leak_detection", false);
			chromePrefs.put("autofill.profile_enabled", false);
 
			nlpResponseModel.setStatus(CommonConstants.pass);
			 nlpResponseModel.setMessage("Fetched the map");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
 
			 nlpResponseModel.setMessage("Failed to get the map");
		}
 
		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("MapVal", chromePrefs);
		return nlpResponseModel;
	}
 
}