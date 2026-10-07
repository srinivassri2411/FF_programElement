package Windows;

import java.io.BufferedReader;
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

public class ConnectToRDP_Win implements Nlp {
	@InputParams({ @InputParam(name = "HostName", type = "java.lang.String"),
			@InputParam(name = "UserName", type = "java.lang.String"),
			@InputParam(name = "RDGateWay", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String hostName = (String) programElementsInput.get("HostName");
		String userName = (String) programElementsInput.get("UserName");
		String rDGateWay = (String) programElementsInput.get("RDGateWay");
		String password = (String) programElementsInput.get("Password");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			//new logic updated
			if (rDGateWay != null && !rDGateWay.isEmpty() && !rDGateWay.contains("null")) {
				saveCredentialWindows(rDGateWay.split(":")[0].trim(), userName, password);
				saveCredential(hostName, userName, password);
				connectRdpGateWay(hostName, rDGateWay);
			} else {
				saveCredential(hostName.split(":")[0].trim(), userName, password);
				connectRdp(hostName);
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully started RDP instance");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to start RDP instance" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		return nlpResponseModel;
	}

	public static void saveCredentialWindows(String target, String username, String password)
			throws IOException, InterruptedException {

		// Wrap password in quotes to prevent special character issues
		String command = String.format("cmdkey /add:%s /user:%s /pass:\"%s\"", target, username, password);

		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);

		Process process = pb.start();

		// Capture standard output
		BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = stdOutput.readLine()) != null) {
			System.out.println("OUTPUT: " + line);
		}

		// Capture error output
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ((line = stdError.readLine()) != null) {
			System.err.println("ERROR: " + line);
		}

		int exitCode = process.waitFor();
		System.out.println("Process exited with code: " + exitCode);
	}

	public static void saveCredential(String target, String username, String password)
			throws IOException, InterruptedException {

		// Wrap password in quotes to handle special characters
		String command = String.format("cmdkey /generic:TERMSRV/%s /user:%s /pass:\"%s\"", target, username, password);

		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);

		Process process = pb.start();

		// Capture standard output
		BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = stdOutput.readLine()) != null) {
			System.out.println("OUTPUT: " + line);
		}

		// Capture error output
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ((line = stdError.readLine()) != null) {
			System.err.println("ERROR: " + line);
		}

		int exitCode = process.waitFor();
		System.out.println("Process exited with code: " + exitCode);
	}

//	public static void saveCredentialWindows(String target, String username, String password)
//			throws IOException, InterruptedException {
//
//		ProcessBuilder pb = new ProcessBuilder("cmd", "/c",
//				"cmdkey /add:" + target + " /user:" + username + " /pass:" + password);
//
//		Process p = pb.start();
//		p.waitFor();
//	}

//	public static void saveCredential(String target, String username, String password)
//			throws IOException, InterruptedException {
//
//		ProcessBuilder pb = new ProcessBuilder("cmd", "/c",
//				"cmdkey /generic:TERMSRV/" + target + " /user:" + username + " /pass:" + password);
//
//		Process p = pb.start();
//		p.waitFor();
//	}

	public static void connectRdp(String ip) throws IOException {

		ProcessBuilder pb = new ProcessBuilder("mstsc", "/v:" + ip);

		pb.start();
	}

	public static void connectRdpGateWay(String ip, String gateway) throws IOException {

		ProcessBuilder pb = new ProcessBuilder("mstsc", "/v:" + ip, "/g:" + gateway);

		pb.start();
	}

	public static void main(String[] args) throws NlpException {
//		NlpRequestModel model = new NlpRequestModel();
//		model.getAttributes().put("HostName", "127.0.0.1:55300");
//		model.getAttributes().put("UserName", "windows");
//		model.getAttributes().put("RDGateWay", "");
//		model.getAttributes().put("Password", "C\\GE3LY:WwNO6I[");
//		System.out.println(new ConnectToRDP_Win().execute(model));

//		NlpRequestModel model = new NlpRequestModel();
//		model.getAttributes().put("HostName", "");
//		model.getAttributes().put("UserName", "");
//		model.getAttributes().put("RDGateWay", "");
//		model.getAttributes().put("Password", "");
//		System.out.println(new ConnectToRDP_Win().execute(model));
	}
}