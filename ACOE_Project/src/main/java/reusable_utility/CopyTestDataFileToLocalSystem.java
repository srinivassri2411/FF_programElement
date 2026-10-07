package reusable_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

public class CopyTestDataFileToLocalSystem implements Nlp {
	@InputParams({ @InputParam(name = "File InputStream", type = "java.io.InputStream"),
		@InputParam(name = "File Name with Extension", type = "java.lang.String") })
	@ReturnType(name = "Local File Path", type = "java.lang.String")

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
		String fileName = "";
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = (InputStream) attributes.get("File InputStream");
			String fileNameExtension = (String) attributes.get("File Name with Extension");
			String userHomeDirectory = System.getProperty("user.home");
			String fireFlinkHomeDirectoryString = userHomeDirectory + File.separator + "Fireflink Home";
			File fireFlinkHomeDirectoryFile = new File(fireFlinkHomeDirectoryString);
			if (!fireFlinkHomeDirectoryFile.exists()) {
				fireFlinkHomeDirectoryFile.mkdir();
			}
			fileName = fireFlinkHomeDirectoryString + File.separator + fileNameExtension;
			File outputFile = new File(fileName);
			System.out.println(outputFile.getName());
			outputStream = new FileOutputStream(outputFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Copied test data file to local system.");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to copy test data file to local system. " + sw.toString());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				e.printStackTrace();
			}
		}
		nlpResponseModel.getAttributes().put("Local File Path", fileName);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws FileNotFoundException, NlpException {
		InputStream ips = new FileInputStream("C:\\Users\\User\\Downloads\\6249A (1).pdf");
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("File InputStream", ips);
		nlpRequestModel.getAttributes().put("File Name with Extension", "abc.pdf");

		System.out.println(new CopyTestDataFileToLocalSystem().execute(nlpRequestModel).getAttributes().get("Local File Path"));
	}
}