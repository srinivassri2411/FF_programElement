
package Business_Logics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class SSHexecutecommand implements Nlp {
	@InputParams({ @InputParam(name = "session", type = "com.jcraft.jsch.Session"),
			@InputParam(name = "command", type = "java.lang.String") })
	@ReturnType(name = "response", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Session session = (Session) programElementsInput.get("session");
		String command = (String) programElementsInput.get("command");
		Integer returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		StringBuilder builder = new StringBuilder();
		try {
			System.out.println("Executing command: " + command);

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);

			// Capture standard output and error streams
			InputStream stdoutStream = channel.getInputStream();
			InputStream stderrStream = channel.getErrStream();

			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutStream, "UTF-8"));
			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrStream, "UTF-8"));

			// Start the channel
			channel.connect();

			StringBuilder stdoutOutput = new StringBuilder();
			StringBuilder stderrOutput = new StringBuilder();
			String line;

			// Read stdout in real time
			while ((line = stdoutReader.readLine()) != null) {
				stdoutOutput.append(line).append("\n");
				System.out.println("STDOUT: " + line); // Print to console
			}

			// Read stderr in real time
			while ((line = stderrReader.readLine()) != null) {
				stderrOutput.append(line).append("\n");
				System.err.println("STDERR: " + line); // Print to console
			}

			// Wait for the channel to close
			while (!channel.isClosed()) {
				Thread.sleep(100); // Check every 100ms if the command is complete
			}

			System.out.println("Command execution completed with exit status: " + channel.getExitStatus());
			System.out.println("Standard Output:\n" + stdoutOutput.toString());
			System.out.println("Standard Error:\n" + stderrOutput.toString());
			builder.append(stdoutOutput.toString()).append("\n").append(stderrOutput.toString());

			// Close resources
			stdoutReader.close();
			stderrReader.close();
			channel.disconnect();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("successfully executed command");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to execute command");
		}

		nlpResponseModel.getAttributes().put("response", builder.toString());
		return nlpResponseModel;

	}

}