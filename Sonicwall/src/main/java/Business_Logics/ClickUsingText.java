
package Business_Logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.python.modules.thread.thread;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Screen;
import org.sikuli.script.SikuliException;

public class ClickUsingText implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Text = (String) programElementsInput.get("Text");
		System.setProperty("java.awt.headless", "false");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			Thread.sleep(3000);
			Screen sc = new Screen();
			sc.findText(Text).click();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully clicked on text " + Text);

		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on text " + e);

		}

		// Your program element business logic ends here ...
		// nlpResponseModel.getAttributes().put("Result", returnValue);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException, SikuliException, InterruptedException {
		/*
		 * NlpRequestModel nlpRequestModel= new NlpRequestModel(); new
		 * ClickUsingText().execute(nlpRequestModel);
		 */
		Thread.sleep(5000);
		Screen sc = new Screen();
		sc.findText("skybeyo5831").click();
	}
}