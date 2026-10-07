
package sapLogics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SAP_SendEnterKeyWindows implements Nlp {
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			sendEnterKey();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Perfomed enter action");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perfom enter action");

		}
		return nlpResponseModel;
	}

	public static void sendEnterKey() throws IOException, InterruptedException {
		// VBScript content: send ENTER key to active window
		String script = "Set WshShell = CreateObject(\"WScript.Shell\")\n" + "WshShell.SendKeys \"{ENTER}\"\n"
				+ " WScript.Sleep 2000";

		// Create temp VBS file
		File tempScript = File.createTempFile("SendEnterKey", ".vbs");
		try (BufferedWriter out = new BufferedWriter(new FileWriter(tempScript))) {
			out.write(script);
		}

		// Build process to run VBScript with cscript (console mode, no logo)
		ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", tempScript.getAbsolutePath());
		Process process = pb.start();

		// Capture standard output
		try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

			String line;
			while ((line = stdOut.readLine()) != null) {
				if (line.startsWith("[ERROR]")) {
					log.error("VBScript Error: " + line);
					throw new RuntimeException("VBScript Error: " + line);
				} else {
					System.out.println("Log: " + line);
					log.info(line);
				}
			}
		}
		int exitCode = process.waitFor();
		boolean deleted = tempScript.delete();
		if (!deleted) {
			log.error("Warning: Temporary VBS script not deleted: " + tempScript.getAbsolutePath());
			throw new RuntimeException("Warning: Temporary VBS script not deleted: " + tempScript.getAbsolutePath());
		}
		if (exitCode != 0) {
			log.error("VBScript exited with error code: " + exitCode);
			throw new RuntimeException("VBScript exited with error code: " + exitCode);
		}
	}
}