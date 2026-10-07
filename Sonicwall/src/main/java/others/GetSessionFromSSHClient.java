
package others;

import java.io.IOException;
import java.io.InputStream;
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
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

public class GetSessionFromSSHClient implements Nlp {
    @InputParams({@InputParam(name = "SSH Connection", type = "net.schmizz.sshj.SSHClient")})
    @ReturnType(name = "Session", type = "net.schmizz.sshj.connection.channel.direct.Session")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          SSHClient sSHConnection = (SSHClient) programElementsInput.get("SSH Connection");
//          String command = (String) programElementsInput.get("Command");
//          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Session session = null;
          try {
        	   session = sSHConnection.startSession();
        	 
        	  nlpResponseModel.setMessage("Session Initialized");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to initialize session - Reason: "+e);
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Session", session);
          return nlpResponseModel;
      }
    


  } 