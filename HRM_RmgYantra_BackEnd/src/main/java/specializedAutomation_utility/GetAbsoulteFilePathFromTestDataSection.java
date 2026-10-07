package specializedAutomation_utility;

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

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GetAbsoulteFilePathFromTestDataSection implements Nlp {
	@InputParams({ @InputParam(name = "Input Stream Object", type = "java.io.InputStream"),
			@InputParam(name = "File Name with Extension", type = "java.lang.String") })
	@ReturnType(name = "File Name", type = "java.lang.String")

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
		String fileName = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = (InputStream) attributes.get("Input Stream Object");
			String fileNameExten = (String) attributes.get("File Name with Extension");
			String home = System.getProperty("user.home");
			fileName = home + File.separator + fileNameExten;
			log.info("File is getting created in below path " + fileName);
			File outputFile = new File(fileName);
			System.out.println(outputFile.getName());
			outputStream = new FileOutputStream(outputFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched the Absoulte File Path");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log.error("Exception is: \n{}", sw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch the Absoulte File Path " + e);
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
				log.error("Exception is: \n{}", sw);
				e.printStackTrace();
			}
		}
		nlpResponseModel.getAttributes().put("File Name", fileName);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws FileNotFoundException, NlpException {
		GetAbsoulteFilePathFromTestDataSection gae = new GetAbsoulteFilePathFromTestDataSection();
		InputStream ip = new FileInputStream("C:\\Users\\Srinivas m\\Downloads\\file.wav");
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("Input Stream Object", ip);
		System.out.println(gae.execute(nlpRequestModel));
	}
}