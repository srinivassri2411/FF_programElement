
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
public class SAP_SendEnterKeyInsideSapObject implements Nlp {

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			send();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Enter action completed on sap object");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to do Enter action on sap object");

		}
		return nlpResponseModel;
	}

	public static void send() throws IOException, InterruptedException {
		File tempScript = File.createTempFile("SendEnterKey", ".vbs");

		String script = "On Error Resume Next\n" + "Dim SapGuiAuto, application, connection, session\n"
				+ "Set SapGuiAuto = GetObject(\"SAPGUI\")\n" + "If SapGuiAuto Is Nothing Then\n"
				+ "  WScript.Echo \"[ERROR] SAPGUI scripting engine not found.\"\n" + "  WScript.Quit 1\n" + "End If\n"
				+ "Set application = SapGuiAuto.GetScriptingEngine\n" + "If application Is Nothing Then\n"
				+ "  WScript.Echo \"[ERROR] SAP application scripting engine not found.\"\n" + "  WScript.Quit 1\n"
				+ "End If\n" + "Dim i\n" + "For i = 1 To 20\n" + "  Set connection = application.Children(0)\n"
				+ "  If Not connection Is Nothing Then Exit For\n" + "  WScript.Sleep 500\n" + "Next\n"
				+ "If connection Is Nothing Then\n" + "  WScript.Echo \"[ERROR] No SAP connection found.\"\n"
				+ "  WScript.Quit 1\n" + "End If\n" + "For i = 1 To 20\n" + "  Set session = connection.Children(0)\n"
				+ "  If Not session Is Nothing Then Exit For\n" + "  WScript.Sleep 500\n" + "Next\n"
				+ "If session Is Nothing Then\n" + "  WScript.Echo \"[ERROR] No SAP session found.\"\n"
				+ "  WScript.Quit 1\n" + "End If\n" + "session.findById(\"wnd[0]\").sendVKey 0\n"
				+ "If Err.Number <> 0 Then\n" + "  WScript.Echo \"[ERROR] \" & Err.Description\n" + "  WScript.Quit 1\n"
				+ "Else\n" + "  WScript.Echo \"[INFO] Enter key sent successfully.\"\n" + "End If\n";

		try (BufferedWriter out = new BufferedWriter(new FileWriter(tempScript))) {
			out.write(script);
		}

		ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", tempScript.getAbsolutePath());
		pb.redirectErrorStream(true); // Merge stderr with stdout

		Process p = pb.start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("[ERROR]")) {
					log.error("VBScript Error: " + line);
					throw new RuntimeException("VBScript Error: " + line);
				} else {
					System.out.println("Log: " + line);
					log.info(line);
				}
			}
		}
		int exitCode = p.waitFor();
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