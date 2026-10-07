
package default_package;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetIdentifiers implements Nlp {
	@ReturnType(name = "id", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		nlpResponseModel.getAttributes().put("id", nlpRequestModel.getExecutionIdentifiers());
		nlpResponseModel.setMessage("Passed");
		nlpResponseModel.setStatus(CommonConstants.pass);
		return nlpResponseModel;
	}

} 