
package default_package;

import java.util.Map;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

public class GetExecDetails implements Nlp {
	@ReturnType(name = "map", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Map returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = nlpRequestModel.getExecutionIdentifiers();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Passed" + returnValue);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Fail" + returnValue);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("map", returnValue);
		return nlpResponseModel;
	}

}