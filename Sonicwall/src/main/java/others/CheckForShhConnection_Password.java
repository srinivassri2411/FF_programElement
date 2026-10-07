package others;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.pty4j.PtyProcess;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CheckForShhConnection_Password implements Nlp {
	@InputParams({ @InputParam(name = "Host Name", type = "java.lang.String"),
			@InputParam(name = "User Name", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String") })
	@ReturnType(name = "filePath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String hostName = (String) programElementsInput.get("Host Name");
		String userName = (String) programElementsInput.get("User Name");
		String password = (String) programElementsInput.get("Password");
		boolean returnValue = false;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = connectSSHAndCheckWelcome(userName, hostName, password);
			nlpResponseModel.setMessage("Successfully checked the connection");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to check the connection");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("filePath", returnValue);
		return nlpResponseModel;
	}

	public static boolean connectSSHAndCheckWelcome(String username, String host, String password) {

		PtyProcess process = null;

		try {
			String[] cmd = { "ssh", "-tt", "-v", username + "@" + host };

			process = PtyProcess.exec(cmd, null, null);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

			StringBuilder buffer = new StringBuilder();

			int c;

			while ((c = reader.read()) != -1) {

				char ch = (char) c;
				System.out.print(ch);

				buffer.append(ch);

				String output = buffer.toString();
				if (output.toLowerCase().contains("(yes/no/[fingerprint])?")) {
					writer.write("yes");
					writer.write("\r");
					writer.flush();

					buffer.setLength(0);
				}
				if (output.toLowerCase().contains("password:")) {

					writer.write(password);
					writer.write("\r");
					writer.flush();

					buffer.setLength(0);
				}
				if (output.toLowerCase().contains("welcome to ubuntu")) {
					writer.write("exit");
					writer.write("\r");
					writer.flush();

					reader.close();
					writer.close();

					if (process.isAlive()) {
						process.destroy();
					}

					process.waitFor(2, TimeUnit.MINUTES);

					return true;
				}
				if (buffer.length() > 1000) {
					buffer.delete(0, 800);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (process != null && process.isAlive()) {
				process.destroyForcibly();
			}
		}

		return false;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Host Name", "10.128.0.5");
		model.getAttributes().put("User Name", "sonicwall");
		model.getAttributes().put("Password", "S0nicwall");
		System.out.println(new CheckForShhConnection_Password().execute(model));
	}
}
