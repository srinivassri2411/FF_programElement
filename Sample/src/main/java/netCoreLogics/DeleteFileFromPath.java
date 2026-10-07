package netCoreLogics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP569070ce-ee5a-4b23-8c1c-53fbdaa82d84")
public class DeleteFileFromPath implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String json1Path = (String) programElementsInput.get("File Path");
		try {
			FileUtils.deleteQuietly(new File(json1Path));
			nlpResponseModel.setMessage("Successfully deleted the given file");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Fail to delete " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlprq = new NlpRequestModel();
		nlprq.getAttributes().put("File Path",
				"C:\\Users\\Public\\New folder\\Searched result_Your Dost_23-Jul-2024_11-03-01.PNG");
		System.out.println(new DeleteFileFromPath().execute(nlprq));
	}
}