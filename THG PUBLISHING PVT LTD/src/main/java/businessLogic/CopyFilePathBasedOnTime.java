package businessLogic;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
//			System.out.println(finalPath);
			date = new SimpleDateFormat("dd-MM-yyyy HH-m-ss").format(new Date());
			date = System.getProperty("java.io.tmpdir") + "ABC" + date;
			date = date + File.separator + path;
//			System.out.println(date);
			File copyFile = new File(date);
//			System.out.println(copyFile);
			FileUtils.copyDirectory(new File(finalPath), copyFile);
			nlpResponseModel.setMessage("Successfully copied the folder");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (IOException e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			try {
				if (writer.toString().contains("lockfile")) {
					nlpResponseModel.setMessage("Successfully copied the folder");
					nlpResponseModel.setStatus(CommonConstants.pass);
				} else {
					throw new Exception();
				}
			} catch (Exception e1) {
				StringWriter writer1 = new StringWriter();
				e.printStackTrace(new PrintWriter(writer1));
				nlpResponseModel.setMessage("Failed to copy the folder " + writer1);
				nlpResponseModel.setStatus(CommonConstants.fail);
			}

		}

		nlpResponseModel.getAttributes().put("string3", date);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlp = new NlpRequestModel();
		nlp.getAttributes().put("Folder Name", "ChromeData");
		System.out.println(new CopyFilePathBasedOnTime().execute(nlp));
	}
}
