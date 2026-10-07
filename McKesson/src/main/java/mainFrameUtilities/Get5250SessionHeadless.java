
package mainFrameUtilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class Get5250SessionHeadless implements Nlp {
    @InputParams({@InputParam(name = "Session Host", type = "java.lang.String"),
    	@InputParam(name = "Session Port", type = "java.lang.String")})
    @ReturnType(name = "session", type = "org.tn5250j.Session5250")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String host = (String) programElementsInput.get("Session Host");
          String port = (String) programElementsInput.get("Session Port");
          Session5250 returnValue=null;
    
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              Properties props = new Properties();
              
              props.put("SESSION_HOST", host);
              props.put("SESSION_PORT", port);
              props.setProperty("SESSION_NAME", "Hemnath");
       
              props.setProperty("screenSize", "27x132");
              props.put("SESSION_DEVICE_TYPE", "IBM-3477-FC");
              props.setProperty("screenSize", "1");
        	  returnValue =
                      SessionManager.instance().openSession(props,"","");
   
        	  returnValue.connect();
   
             

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Headless Session created successfully");

          }
          catch(Exception e) {
        	  
        	     e.printStackTrace();
                 StringWriter sw = new StringWriter();
                 e.printStackTrace(new PrintWriter(sw));

                 nlpResponseModel.setStatus(CommonConstants.fail);
                 nlpResponseModel.setMessage("Failed to create Headless session : " + sw);
                       }

          nlpResponseModel.getAttributes().put("session", returnValue);
          return nlpResponseModel;
      }

  } 