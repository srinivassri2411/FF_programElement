package ssh_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;


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


public class SSH_Login_PrivateKey implements Nlp {
	@InputParams({ @InputParam(name = "host", type = "java.lang.String"),
			@InputParam(name = "port", type = "java.lang.String"),
			@InputParam(name = "username", type = "java.lang.String"),
			@InputParam(name = "privateKeyPath", type = "java.lang.String") })
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
		String privateKeyPath = (String) attributes.get("privateKeyPath");
		String port = (String) attributes.get("port");
		Session session = null;

		JSch jsch = new JSch();

		try {
			
			// Load the private key
			jsch.addIdentity(privateKeyPath);

			// Create session
			session = jsch.getSession(username, host, Integer.parseInt(port));

			// Set additional configurations, e.g., for accepting unknown host keys
			session.setConfig("StrictHostKeyChecking", "no");

			// Connect to the remote server
			session.connect();
			System.out.println("Connected");

			nlpResponseModel.setMessage("SSH Connection to Server is Successful");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("SSH Connection to Server Failed" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("session", session);
		return nlpResponseModel;
	}

	public static void main(String[] args) {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("port", "22");
		nlpRequestModel.getAttributes().put("host", "49.249.29.5");
		nlpRequestModel.getAttributes().put("username", "tyss");
		nlpRequestModel.getAttributes().put("privateKeyPath", "C:\\Users\\User\\Desktop\\pemfile for ssh\\id_rsa");
		try {
			NlpResponseModel nlpResponseModel = new SSH_Login_PrivateKey().execute(nlpRequestModel);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
