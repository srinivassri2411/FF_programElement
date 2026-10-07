package tataPlay;

import java.io.File;
import java.io.IOException;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CopyFilePathBasedOnTime implements Nlp {
	@InputParams({ @InputParam(name = "Folder Name", type = "java.lang.String") })
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
		String path = (String) attributes.get("Folder Name");
		String date = "";
		try {
			String userDir = System.getProperty("user.home");
			String finalPath = userDir + File.separator + path;
			System.out.println(finalPath);
			date = System.getProperty("java.io.tmpdir");
			System.out.println(date);
			date = date + path;
			System.out.println(date);
			File copyFile = new File(date);
			FileUtils.copyDirectory(new File(finalPath), copyFile);
			nlpResponseModel.setMessage("Successfully copied the folder");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to copy the folder");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("string3", date);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws IOException {

	}
}