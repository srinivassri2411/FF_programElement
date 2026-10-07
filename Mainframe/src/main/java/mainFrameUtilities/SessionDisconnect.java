
package mainFrameUtilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.tn5250.Screen5250;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class SessionDisconnect implements Nlp {
    @InputParams({@InputParam(name = "TN5250 Session", type = "org.tn5250j.Session5250")})
//    @ReturnType(name = "screen", type = "org.tn5250j.framework.tn5250.Screen5250")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Session5250 session = (Session5250) programElementsInput.get("TN5250 Session");
//          Screen5250 returnValue=null;
    
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	session.disconnect();
             

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Session disconnected successfully");

          }
          catch(Exception e) {
        	  
        	     e.printStackTrace();
                 StringWriter sw = new StringWriter();
                 e.printStackTrace(new PrintWriter(sw));

                 nlpResponseModel.setStatus(CommonConstants.fail);
                 nlpResponseModel.setMessage("Failed to disconnect session : " + sw);
                       }

//          nlpResponseModel.getAttributes().put("session", returnValue);
          return nlpResponseModel;
      }

  } 