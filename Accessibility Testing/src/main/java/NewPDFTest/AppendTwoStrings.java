package NewPDFTest;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;




public class AppendTwoStrings implements Nlp {
	@InputParams({ @InputParam(name = "String 1", type = "java.lang.String"),
			@InputParam(name = "String 2", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "java.lang.Object")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String string1 = (String) programElementsInput.get("String 1");
		String string2 = (String) programElementsInput.get("String 2");
		Object returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			StringBuilder comBuilder = new StringBuilder();
			comBuilder.append(string1).append("\n").append("  ...............................  ").append("\n")
					.append(string2);
			returnValue = comBuilder;

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("String appended to next line successfully");

		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to append String " + e);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Result", returnValue);
		return nlpResponseModel;
	}

}