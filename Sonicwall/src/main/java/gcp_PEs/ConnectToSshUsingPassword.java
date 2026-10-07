
package gcp_PEs;

import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class ConnectToSshUsingPassword implements Nlp {
	@InputParams({ @InputParam(name = "HostName", type = "java.lang.String"),
			@InputParam(name = "Port Number", type = "java.lang.Integer"),
			@InputParam(name = "UserName", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String") })
	@ReturnType(name = "SshConnection", type = "net.schmizz.sshj.SSHClient")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String hostName = (String) programElementsInput.get("HostName");
		String userName = (String) programElementsInput.get("UserName");
		String password = (String) programElementsInput.get("Password");
		int portNumber = (int) programElementsInput.get("Port Number");
		SSHClient ssh = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {

			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier()); // Skip strict host key checking
			ssh.connect(hostName, portNumber);
			ssh.authPassword(userName, password);
			if (ssh.isConnected()) {
				nlpResponseModel.setMessage("Connected to " + hostName);
				nlpResponseModel.setStatus(CommonConstants.pass);
			}
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setMessage("Unable to connect - Reason: " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("SshConnection", ssh);
		return nlpResponseModel;
	}

}