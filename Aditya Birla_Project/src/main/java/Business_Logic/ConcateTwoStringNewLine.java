package Business_Logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;



public class ConcateTwoStringNewLine implements Nlp {
	@InputParams({ @InputParam(name = "string1", type = "java.lang.String"),
			@InputParam(name = "string2", type = "java.lang.String") })
	@ReturnType(name = "string3", type = "java.lang.String")

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String string1 = (String) attributes.get("string1");
		String string2 = (String) attributes.get("string2");
		String string3 = null;
		
		// Your program element business logic goes here ...
		try {
			 string3 = string1 + "\n" + string2 + "\n";
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Concatenated "+string1+" with "+string2+"");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to concatenate "+string1+" with "+string2+"");
		}

		nlpResponseModel.getAttributes().put("string3", string3);
		return nlpResponseModel;
	}
}