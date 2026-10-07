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
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class Consume_Kafka_Message implements Nlp {
	@InputParams({ @InputParam(name = "session", type = "com.jcraft.jsch.Session"),
			@InputParam(name = "TopicName", type = "java.lang.String"),
			@InputParam(name = "Bootstrap Server Address", type = "java.lang.String") })
	@ReturnType(name = "Response", type = "java.lang.String")

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
		String topicName = (String) attributes.get("TopicName");
		String address = (String) attributes.get("Bootstrap Server Address");

		String response = "";
		try {
			String command = "nohup /usr/local/kafka/bin/kafka-console-consumer.sh --topic " + topicName
					+ " --bootstrap-server " + address + " --from-beginning --max-messages 1 &";
			response = executeCommand(session, command);
			nlpResponseModel.setMessage("Kafka Message is consumed successfully..." + response);
			nlpResponseModel.setStatus(CommonConstants.pass);
			session.disconnect();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus("Failed to Consume Message..." + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Response", response);
		return nlpResponseModel;
	}

	private static String executeCommand(Session session, String command) throws IOException, JSchException {
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
			res = res+line+"\n";
		}
		channelExec.disconnect();
		return res;
	}
}