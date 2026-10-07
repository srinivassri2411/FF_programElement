package ssh_utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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


public class Execute_Linux_Command_And_Validate_Respone implements Nlp {
	@InputParams({ @InputParam(name = "session", type = "com.jcraft.jsch.Session"),
		@InputParam(name = "command", type = "java.lang.String"),
		@InputParam(name = "reference", type = "java.lang.String"),
		@InputParam(name = "expectedData", type = "java.lang.String") })

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

		Session session = (Session) attributes.get("session");
		String command = (String) attributes.get("command");
		String referance = (String) attributes.get("referance");
		String expectedData = (String) attributes.get("expectedData");
		ChannelExec channelExec = null;

		try {
			// Execute a command (e.g., "whoami")
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);

			// Set up input and output streams
			InputStream in = channelExec.getInputStream();
			channelExec.connect();

			// Read the command output
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;

			boolean flag = false;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(referance)) {
					if (line.contains(expectedData)) {
						nlpResponseModel.setMessage("Response line Starts with "+referance+" contains " + expectedData);
						nlpResponseModel.setStatus(CommonConstants.pass);
						flag = true;
						break;
					}
				}
			}
			if (flag == false) {
				nlpResponseModel.setMessage("Response line Starts with "+referance+" does not contains " + expectedData);
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to Verify Resonse");
			nlpResponseModel.setStatus(CommonConstants.fail);
		} finally {
			channelExec.disconnect();
		}
		return nlpResponseModel;
	}
}