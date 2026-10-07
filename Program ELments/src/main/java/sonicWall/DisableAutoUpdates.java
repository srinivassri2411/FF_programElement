
package sonicWall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisableAutoUpdates implements Nlp {
	@ReturnType(name = "result", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String logFilePath = System.getProperty("user.home") + File.separator + "DisableAutoUpdates_Log.txt";
		String vbsFilePath = System.getProperty("user.home") + File.separator + "disableAutoUpdates.vbs";
		String batFilePath = System.getProperty("user.home") + File.separator + "disableAutoUpdates.bat";
		String result = null;
		try {
			if (!new File(batFilePath).exists() && !new File(vbsFilePath).exists())
				throw new FileNotFoundException("disableAutoUpdates.bat and disableAutoUpdates.vat file not exists "
						+ System.getProperty("user.home") + File.separator);
			FileUtils.writeStringToFile(new File(logFilePath), "", "UTF-8");
			result = enableBitLocker(vbsFilePath, logFilePath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Disabled Auto Updates successfully");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to Disable Auto Updates -> " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("result", result);
		return nlpResponseModel;
	}

	public String enableBitLocker(String vbsFilePath, String logFilePath) throws Exception {
		List<String> command = new ArrayList<>();
		command.add("cscript");
		command.add("//B");
		command.add(vbsFilePath);
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		int exitCode = process.waitFor();
		log.info("Batch file execution completed with exit code: " + exitCode);
		Thread.sleep(2500);
		String result = FileUtils.readFileToString(new File(logFilePath), "UTF-8");
		System.out.println(result);
		if (result.contains("ERROR: Access is denied.")) {
			throw new RuntimeException("Disable autoupdate failed check logs");
		}
		return result;
	}
}