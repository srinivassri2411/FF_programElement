package sapLogics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LaunchSAP implements Nlp {
	@InputParams({ @InputParam(name = "SAP exe Path", type = "java.lang.String") })

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
		String path = (String) attributes.get("SAP exe Path");
		try {
			launchSAP(path);
			nlpResponseModel.setMessage("Launched SAP from this path " + path);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void launchSAP(String exePath) throws IOException, InterruptedException {
		String script = "On Error Resume Next\n" + "Dim WshShell\n" + "Set WshShell = CreateObject(\"WScript.Shell\")\n"
				+ "WshShell.Run \"\"\"" + exePath + "\"\"\", 1, false\n" + "If Err.Number <> 0 Then\n"
				+ "  WScript.Echo \"[ERROR] \" & Err.Description\n" + "Else\n"
				+ "  WScript.Echo \"[INFO] SAP Logon launched successfully.\"\n" + "End If\n" + "WScript.Sleep 10000\n";

		File tempScript = File.createTempFile("LaunchSAP", ".vbs");
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
			log.error("SAP launch script exited with error code: " + exitCode);
			throw new RuntimeException("SAP launch script exited with error code: " + exitCode);
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("SAP exe Path", "C:\\Program Files (x86)\\SAP\\FrontEnd\\SAPgui\\saplogon.exe");
		new LaunchSAP().execute(req);
	}
}