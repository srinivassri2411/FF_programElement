package ssh_utility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class SSH_Login_BasicAuth implements Nlp {
	@InputParams({@InputParam(name = "host", type = "java.lang.String"),
		@InputParam(name = "port", type = "java.lang.String"),
		@InputParam(name = "username", type = "java.lang.String"), 
		@InputParam(name = "password", type = "java.lang.String"),})
	@ReturnType(name = "session", type = "com.jcraft.jsch.Session")

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
		String host = (String) attributes.get("host");
		String username = (String) attributes.get("username");
		String password = (String) attributes.get("password");
		String port = (String) attributes.get("port");
		Session session = null;

		try {
			JSch jsch = new JSch();

			// Create session
			session = jsch.getSession(username, host, Integer.parseInt(port));
			session.setPassword(password);

			// Set additional configurations, e.g., for accepting unknown host keys
			session.setConfig("StrictHostKeyChecking", "no");
			
			// Connect to the remote server
			session.connect();

			nlpResponseModel.setMessage("SSH Connection to Unix Box is Successful");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("SSH Connection to Unix Box is Failed");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("session", session);
		return nlpResponseModel;
	}
} 