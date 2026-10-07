
package gcp_PEs;

import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import net.schmizz.sshj.SSHClient;

public class CloseSshConnection implements Nlp {
    @InputParams({@InputParam(name = "SSH Connection", type = "net.schmizz.sshj.SSHClient")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          SSHClient sSHConnection = (SSHClient) programElementsInput.get("SSH Connection");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  sSHConnection.disconnect();
        	  nlpResponseModel.setMessage("Disconnected");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
        	  nlpResponseModel.setMessage("Failed to Disconnect - Reason:"+e);
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          return nlpResponseModel;
      }

  } 