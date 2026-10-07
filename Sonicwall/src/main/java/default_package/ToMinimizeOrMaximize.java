
package default_package;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class ToMinimizeOrMaximize implements Nlp {
	@InputParams({ @InputParam(name = "Maximize/Minimize", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Action = (String) programElementsInput.get("Maximize/Minimize");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {

			if (nlpRequestModel.getWebDriver().toString().toLowerCase().contains("mac2")) {
				nlpResponseModel.setMessage("Succesfully performed Action in mac ");

			} else {

				if (Action.toLowerCase().contains("max")) {
					nlpRequestModel.getWebDriver().manage().window().maximize();
				} else {
					nlpRequestModel.getWebDriver().manage().window().minimize();

				}
				
				nlpResponseModel.setMessage("Succesfully performed Action " + Action);

			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			// Uncomment the below line and modify the message, only if you want to override
			// the pass message defined for the program element.
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.pass);
			// Uncomment the below line and modify the message, only if you want to override
			// the fail message defined for the program element.
			nlpResponseModel.setMessage("Failed to Peform Action");
		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

}