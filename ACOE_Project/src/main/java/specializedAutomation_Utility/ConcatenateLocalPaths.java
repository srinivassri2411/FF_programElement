
package specializedAutomation_Utility;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.List;




public class ConcatenateLocalPaths implements Nlp {
	@InputParams({@InputParam(name = "Parent", type = "java.lang.String"), @InputParam(name = "Child", type = "java.lang.String")})
	@ReturnType(name = "Complete_Path", type = "java.lang.String")

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
		String parent = (String) attributes.get("Parent");
		String child = (String) attributes.get("Child");

		// Your program element business logic goes here ...
		String completePath = parent + File.separator + child;
		nlpResponseModel.setStatus(CommonConstants.pass);
		nlpResponseModel.setMessage("Concatenated "+parent+" with "+child);
		nlpResponseModel.getAttributes().put("Complete_Path", completePath);
		return nlpResponseModel;
	}
	
	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("Parent", "C:\\Users\\User\\Fireflink_Home");
		req.getAttributes().put("Child", "Channa");
		NlpResponseModel res = new ConcatenateLocalPaths().execute(req);
		System.out.println(res.getAttributes().get("Complete_Path"));
	}
} 