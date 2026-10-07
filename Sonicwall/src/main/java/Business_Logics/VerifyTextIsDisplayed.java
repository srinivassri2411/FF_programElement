
package Business_Logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.sikuli.script.Screen;

public class VerifyTextIsDisplayed implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Text = (String) programElementsInput.get("Text");
		boolean isTextDisplayed = false;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		System.setProperty("java.awt.headless", "false");

		try {
			Screen sc = new Screen();
			if (sc.hasText(Text)) {

				isTextDisplayed = sc.hasText(Text);

				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Text is displayed " + Text);

			} else {
				isTextDisplayed = false;
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Text is not displayed ");
			}

		} catch (Exception e) {
			isTextDisplayed = false;
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Text is not displayed " + e);

		}

		// Your program element business logic ends here ...

		nlpResponseModel.getAttributes().put("Result", isTextDisplayed);
		return nlpResponseModel;
	}

}