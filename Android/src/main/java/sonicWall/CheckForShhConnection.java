
package sonicWall;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CheckForShhConnection implements Nlp {
	@InputParams({ @InputParam(name = "Host Name", type = "java.lang.String"),
			@InputParam(name = "User Name", type = "java.lang.String") })
	@ReturnType(name = "filePath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String hostName = (String) programElementsInput.get("Host Name");
		String userName = (String) programElementsInput.get("User Name");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = checkConnection(userName, hostName);
			nlpResponseModel.setMessage("Successfully checked the connection");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to check the connection");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("filePath", returnValue);
		return nlpResponseModel;
	}

	private String checkConnection(String userName, String hostName) throws IOException, InterruptedException {
		String[] command = { "ssh", "-v", "-o", "BatchMode=yes", userName + "@" + hostName + "" };
		String logFilePath = File.createTempFile("fireflink_ssh_debug_log", ".txt").getAbsolutePath();
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath));
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					writer.write(line);
					writer.newLine();
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		Thread.sleep(5000);
		if (process.isAlive()) {
			process.destroyForcibly();
			System.out.println("🔴 SSH process killed after timeout.");
		}
		executor.shutdownNow();
		reader.close();
		writer.close();
		System.out.println("✅ SSH logs written to: " + logFilePath);
		return logFilePath;
	}
}