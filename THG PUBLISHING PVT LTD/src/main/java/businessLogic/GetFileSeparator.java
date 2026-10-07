
package businessLogic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class GetFileSeparator implements Nlp {
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
		String string3 = null;

		try {
			string3 = File.separator;
			System.out.println(string3);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Sucessfully fetched the path separator");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Fail to fetch the path separator");
		}

		nlpResponseModel.getAttributes().put("string3", string3);
		return nlpResponseModel;
	}
	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlp = new NlpRequestModel();
		Map<String, Object> attributes = nlp.getAttributes();
		GetFileSeparator getFile=new GetFileSeparator();
		getFile.execute(nlp);
	}
}