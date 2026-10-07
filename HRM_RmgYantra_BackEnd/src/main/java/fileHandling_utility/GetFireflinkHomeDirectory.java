
package fileHandling_utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class GetFireflinkHomeDirectory implements Nlp {
	@ReturnType(name = "Fireflink_Directory", type = "java.lang.String")

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

		// Your program element business logic goes here ...
		try {
			String fireFlinkDirectory = System.getProperty("user.home")+File.separator+"Fireflink_Home";
			checkAndCreateDirectory(fireFlinkDirectory);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fireflink Home Directory is: "+fireFlinkDirectory);
			nlpResponseModel.getAttributes().put("Fireflink_Directory", fireFlinkDirectory);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed to get Fireflink Home Directory.");
		}
		return nlpResponseModel;
	}

	private static void checkAndCreateDirectory(String directoryPath) throws IOException {
		Path path = Paths.get(directoryPath);
		// Check if the directory exists
		if (!Files.exists(path)) {
			// Create the directory and any nonexistent parent directories
			Files.createDirectories(path);
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		NlpResponseModel res = new GetFireflinkHomeDirectory().execute(req);
		System.out.println(res.getAttributes().get("Fireflink_Directory"));
	}
} 