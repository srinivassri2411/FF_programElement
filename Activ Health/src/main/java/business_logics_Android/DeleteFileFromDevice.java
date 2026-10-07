package business_logics_Android;

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

public class DeleteFileFromDevice implements Nlp {
	@InputParams({ @InputParam(name = "File Destination Path", type = "org.openqa.selenium.String") })

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

		String destinationPath = (String) attributes.get("File Destination Path");

		try {
			ProcessBuilder processBuilder = new ProcessBuilder("adb", "shell", "rm", destinationPath);
			Process process = processBuilder.start();

			// Wait for the process to complete
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				nlpResponseModel.setMessage("File Deleted Succesfully");
			} else {
				nlpResponseModel.setMessage("No File To Delete");
			}

			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			// TODO: handle exception
			nlpResponseModel.setMessage(""+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		return nlpResponseModel;
	}

}
