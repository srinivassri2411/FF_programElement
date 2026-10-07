
package sapLogics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SAP_ClickOnAnElement implements Nlp {
	@InputParams({ @InputParam(name = "element id", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String element = (String) programElementsInput.get("element id");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			click(element);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Clicked on given element " + element);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on given element " + element);

		}
		return nlpResponseModel;
	}

	public static void click(String elementId) throws IOException, InterruptedException {
		String script = "On Error Resume Next\n" + "Dim SapGuiAuto, application, connection, session\n"
				+ "Set SapGuiAuto = GetObject(\"SAPGUI\")\n" + "Set application = SapGuiAuto.GetScriptingEngine\n"
				+ "Set connection = application.Children(0)\n" + "Set session = connection.Children(0)\n"
				+ "session.findById(\"" + elementId + "\").press\n" + "If Err.Number <> 0 Then\n"
				+ "  WScript.Echo \"[ERROR] \" & Err.Description\n" + "Else\n"
				+ "  WScript.Echo \"[INFO] Clicked element: " + elementId + "\"\n" + "End If\n";

		File tempScript = File.createTempFile("ClickElement", ".vbs");
		try (BufferedWriter out = new BufferedWriter(new FileWriter(tempScript))) {
			out.write(script);
		}

		ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", tempScript.getAbsolutePath());
		pb.redirectErrorStream(true);
		Process process = pb.start();
		

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("[ERROR]")) {
					log.error("VBScript Error: " + line);
					throw new RuntimeException("VBScript Error: " + line);
				} else {
					System.out.println("Log: " + line);
					log.info("Log: " + line);
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