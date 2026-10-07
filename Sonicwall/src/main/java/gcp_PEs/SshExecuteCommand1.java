
package gcp_PEs;

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

public class SshExecuteCommand1 implements Nlp {
    @InputParams({@InputParam(name = "SSH Connection", type = "net.schmizz.sshj.SSHClient"), 
    	@InputParam(name = "Command", type = "java.lang.String")})
    @ReturnType(name = "OutPut", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          SSHClient sSHConnection = (SSHClient) programElementsInput.get("SSH Connection");
          String command = (String) programElementsInput.get("Command");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  Session session = sSHConnection.startSession();
        	  Command op = session.exec(command);
        	  op.join();
        	  Integer exitCode = op.getExitStatus();
        	  
        	  if (exitCode==0) {
        		 
        		  returnValue=readInputStream(op.getInputStream());
        		  
			} else {
				
				returnValue=readInputStream(op.getErrorStream());
			}
        	  nlpResponseModel.setMessage("Command executed successfully");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Unable to execute command - Reason: "+e);
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("OutPut", returnValue);
          return nlpResponseModel;
      }
    
    private static String readInputStream(InputStream inputStream) throws IOException {
	    StringBuilder result = new StringBuilder();
	    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            result.append(line).append("\n");
	        }
	    }
	    return result.toString();
	}

  } 