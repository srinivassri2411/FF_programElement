
package businessLogic;

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

public class UploadMaster implements Nlp {
	@InputParams({ @InputParam(name = "DataFile ID", type = "java.lang.String"),
			@InputParam(name = "MasterFile ID", type = "java.lang.String"),
			@InputParam(name = "File Path", type = "java.lang.String") })
//    @ReturnType(name = "string3", type = "java.lang.String")

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
		String data = (String) attributes.get("DataFile ID");
		String path = (String) attributes.get("File Path");
		String master = (String) attributes.get("MasterFile ID");
//          String pid = (String) attributes.get("PID");

		// Your program element business logic goes here ...
		try {

			Utility u = new Utility();

			u.uploadMaster(data, master, path);
			nlpResponseModel.setMessage("Pass");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("failed" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

//          String string3 = "Return Value";
//          nlpResponseModel.getAttributes().put("string3", string3);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlp = new NlpRequestModel();
		Map<String, Object> attributes = nlp.getAttributes();
//		attributes.put("Data Folder", "Testing");
		attributes.put("DataFile ID", "FILE1100");
//		attributes.put("Master Folder", "Master Files");
		attributes.put("MasterFile ID", "FILE1237");
		attributes.put("File Path", "C:\\Users\\User\\Downloads\\");
//          attributes.put("PID", "FILE1027");
		UploadMaster d = new UploadMaster();
		d.execute(nlp);

	}
}