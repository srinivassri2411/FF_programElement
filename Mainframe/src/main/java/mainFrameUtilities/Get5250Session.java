
package mainFrameUtilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

import org.tn5250j.Session5250;
import org.tn5250j.SessionPanel;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.common.SessionManager;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class Get5250Session implements Nlp {
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
          	org.tn5250j.tools.LangTool.init();
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
   
              JFrame frame = new JFrame("Fireflink AS400");
              returnValue.addSessionListener(new SessionListener() {
   
                  @Override
                  public void onSessionChanged(SessionChangeEvent e) {
                      // State 0 is generally DISCONNECTED
                      // State 2 is generally CONNECTED
                      if (e.getState() == 0) {
                          if (frame != null) {
                              frame.dispose();
                              System.out.println("Session closed. Window disposed.");
                          }
                      }
                  }
   
            
              });
              SessionPanel sessionPanel = new SessionPanel(returnValue);
              frame.getContentPane().add(sessionPanel);
              frame.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
              frame.setSize(1024, 768);
              frame.setVisible(true);
              frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Session created successfully");

          }
          catch(Exception e) {
        	  
        	     e.printStackTrace();
                 StringWriter sw = new StringWriter();
                 e.printStackTrace(new PrintWriter(sw));

                 nlpResponseModel.setStatus(CommonConstants.fail);
                 nlpResponseModel.setMessage("Failed to create session : " + sw);
                       }

          nlpResponseModel.getAttributes().put("session", returnValue);
          return nlpResponseModel;
      }

  } 