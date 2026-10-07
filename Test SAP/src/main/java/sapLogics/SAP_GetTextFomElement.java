
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SAP_GetTextFomElement implements Nlp {
	@InputParams({ @InputParam(name = "element id", type = "java.lang.String") })
	@ReturnType(name = "text", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String element = (String) programElementsInput.get("element id");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String returnValue = "";
		try {
			returnValue = getText(element);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fetched text from given element " + element);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch text from given element " + element);

		}
		nlpResponseModel.getAttributes().put("text", returnValue);
		return nlpResponseModel;
	}

	public static String getText(String elementId) throws IOException, InterruptedException {
		File tempVbs = File.createTempFile("GetText", ".vbs");

		String script = "On Error Resume Next\n" + "Dim SapGuiAuto, application, connection, session, value\n"
				+ "Set SapGuiAuto = GetObject(\"SAPGUI\")\n" + "Set application = SapGuiAuto.GetScriptingEngine\n"
				+ "Set connection = application.Children(0)\n" + "Set session = connection.Children(0)\n"
				+ "value = session.findById(\"" + elementId + "\").Text\n" + "If Err.Number <> 0 Then\n"
				+ "  WScript.Echo \"[ERROR] \" & Err.Description\n" + "Else\n" + "  WScript.Echo value\n" + "End If\n";

		try (BufferedWriter out = new BufferedWriter(new FileWriter(tempVbs))) {
			out.write(script);
		}

		// Use cscript and suppress the logo
		ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", tempVbs.getAbsolutePath());
		pb.redirectErrorStream(true); // Merge stderr with stdout

		Process p = pb.start();

		// Read output (stdout + stderr)
		String line;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("[ERROR]")) {
					log.error("VBScript Error: " + line);
					throw new RuntimeException("VBScript Error: " + line);
				} else {
					System.out.println("SAP Field Text: " + line);
					log.info(line);
				}
			}
		}

		int exitCode = p.waitFor();
		boolean deleted = tempVbs.delete();
		if (!deleted) {
			log.error("Warning: Temporary VBS script not deleted: " + tempVbs.getAbsolutePath());
			throw new RuntimeException("Warning: Temporary VBS script not deleted: " + tempVbs.getAbsolutePath());
		}
		if (exitCode != 0) {
			log.error("VBScript exited with error code: " + exitCode);
			throw new RuntimeException("VBScript exited with error code: " + exitCode);
		}
		return line;
	}
}