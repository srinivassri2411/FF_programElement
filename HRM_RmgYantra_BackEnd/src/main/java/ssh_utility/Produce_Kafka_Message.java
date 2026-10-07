package ssh_utility;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class Produce_Kafka_Message implements Nlp {
	@InputParams({ @InputParam(name = "session", type = "com.jcraft.jsch.Session"),
		@InputParam(name = "Message", type = "java.lang.String"),
		@InputParam(name = "Bootstrap Server Address", type = "java.lang.String"),
		@InputParam(name = "Topic", type = "java.lang.String") })

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
		String Message = (String) attributes.get("Message");
		String address = (String) attributes.get("Bootstrap Server Address");
		String topic = (String) attributes.get("Topic");


		try {
			String command = "nohup sh -c 'echo \"" + Message
					+ "\" | /usr/local/kafka/bin/kafka-console-producer.sh --topic " + topic + " --bootstrap-server "
					+ address + "' &";
			String response = executeCommand(session, command);
			nlpResponseModel.setMessage("Message " + Message + " Produced Successfully...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus("Failed to Produce Message..." + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	private static String executeCommand(Session session, String command)
			throws IOException, JSchException {
		ChannelExec channelExec = null;
		String res = "";

		// Execute a command (e.g., "whoami")
		channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(command);

		// Set up input and output streams
		InputStream in = channelExec.getInputStream();
		channelExec.connect();

		// Read the command output
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			res = res + line + "\n";
		}
		channelExec.disconnect();
		return res;
	}
}