package others;

import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class SShReturnChannel implements Nlp {
    @InputParams({@InputParam(name = "session", type = "com.jcraft.jsch.Session")})
    @ReturnType(name = "channelExec", type = "com.jcraft.jsch.ChannelExec")
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Session session = (Session) programElementsInput.get("session");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          ChannelExec channelExec=null;
          try {
              channelExec = (ChannelExec)session.openChannel("exec");
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Succesfully returned channelExec "+channelExec);
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("failed to return channelExec");
          }
          nlpResponseModel.getAttributes().put("channelExec", channelExec);
          return nlpResponseModel;
      }

  }