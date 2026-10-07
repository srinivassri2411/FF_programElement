
package Business_Logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.sikuli.script.Screen;

public class verifyImage implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Text = (String) programElementsInput.get("Text");
		boolean isTextDisplayed = true;
		System.setProperty("java.awt.headless", "false");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			Screen sc = new Screen();
			isTextDisplayed = sc.hasText(Text);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully clicked on text " + Text);

		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on text " + e);

		}

		// Your program element business logic ends here ...

		nlpResponseModel.getAttributes().put("Result", isTextDisplayed);
		return nlpResponseModel;
	}

}