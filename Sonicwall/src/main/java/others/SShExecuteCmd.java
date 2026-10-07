package others;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class SShExecuteCmd implements Nlp {
    @InputParams({@InputParam(name = "channelExec", type = "com.jcraft.jsch.ChannelExec"),
        @InputParam(name = "command", type = "java.lang.String")})
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          ChannelExec channelExec = (ChannelExec) programElementsInput.get("channelExec");
          String command = (String) programElementsInput.get("command");
          String res="";
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              channelExec.setCommand(command);
             InputStream in = channelExec.getInputStream();
                channelExec.connect();

                // Read the command output
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    res = res+line+"\n";
                }
              
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Succesfully executed command "+res);
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("failed to execute");
          }
          
          return nlpResponseModel;
      }

  }