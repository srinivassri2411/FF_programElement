
package gcp_PEs;

import java.io.IOException;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;

public class SshTransferFileFromLocalToRemote implements Nlp {
    @InputParams({@InputParam(name = "SSH Connection", type = "net.schmizz.sshj.SSHClient"), 
    	@InputParam(name = "Local File Path", type = "java.lang.String"),
    	@InputParam(name = "Remote Folder Path", type = "java.lang.String")})

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          SSHClient sSHConnection = (SSHClient) programElementsInput.get("SSH Connection");
          String localFilePath = (String) programElementsInput.get("Local File Path");
          String remoteFolderPath = (String) programElementsInput.get("Remote Folder Path");
          
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  
        	  uploadFileSFTP(sSHConnection, localFilePath, remoteFolderPath);
        	  nlpResponseModel.setMessage("File transfered successfully");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Unable to transfer file - Reason: "+e);
          }

          // Your program element business logic ends here ...
          return nlpResponseModel;
      }
    
    private static void uploadFileSFTP(SSHClient ssh, String localFilePath, String remoteFilePath) throws IOException {
		try (SFTPClient sftp = ssh.newSFTPClient()) {
			sftp.put(localFilePath, remoteFilePath);
		}
	}
  } 