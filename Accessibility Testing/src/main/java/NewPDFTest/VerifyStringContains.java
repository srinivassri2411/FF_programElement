package NewPDFTest;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;




public class VerifyStringContains implements Nlp {
	@InputParams({ @InputParam(name = "String 1", type = "java.lang.String"),
			@InputParam(name = "String 2", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String string1 = (String) programElementsInput.get("String 1");
		String string2 = (String) programElementsInput.get("String 2");
		Boolean returnValue = false;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (string1.contains(string2)) {
				returnValue = true;
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("String 1 contains String 2");
			} else {

				returnValue = false;
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("String 1 doesn't contains String 2");
			}

		} catch (Exception e) {
			// Your program element Exception handling goes here ...\
			returnValue = false;
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed to compare " + e);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Result", returnValue);
		return nlpResponseModel;
	}

}