
package gcp_PEs;

import java.io.File;
import java.io.IOException;
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
import net.schmizz.sshj.sftp.SFTPClient;

public class SshTransferFileFromRemoteToLocal implements Nlp {
	@InputParams({ @InputParam(name = "SSH Connection", type = "net.schmizz.sshj.SSHClient"),
			@InputParam(name = "Remote File Path", type = "java.lang.String") })
	@ReturnType(name = "filePath", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		SSHClient sSHConnection = (SSHClient) programElementsInput.get("SSH Connection");
		String remoteFilePath = (String) programElementsInput.get("Remote File Path");
		String localFolderPath = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {

			localFolderPath = downloadFileSFTP(sSHConnection, remoteFilePath);
			nlpResponseModel.setMessage("File transfered successfully");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Unable to transfer file - Reason: " + e);
		}
		nlpResponseModel.getAttributes().put("filePath", localFolderPath);
		return nlpResponseModel;
	}

	private static String downloadFileSFTP(SSHClient ssh, String remoteFilePath) throws IOException {
		String userHome = System.getProperty("user.home");
		String fileName = new File(remoteFilePath).getName();
		File localFile = new File(userHome, fileName);
		String localFilePath = localFile.getAbsolutePath();
		try (SFTPClient sftp = ssh.newSFTPClient()) {
			sftp.get(remoteFilePath, localFilePath);
		}
		return localFilePath;
	}

}