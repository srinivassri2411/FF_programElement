
package mainFrameUtilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.keyboard.KeyMnemonic;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class LoginToAS400 implements Nlp {
    @InputParams({@InputParam(name = "Screen", type = "org.tn5250j.framework.tn5250.Screen5250"),
    	@InputParam(name = "UserName", type = "java.lang.String")	,
    	@InputParam(name = "Password", type = "java.lang.String")	
    })
//    @ReturnType(name = "screen", type = "org.tn5250j.framework.tn5250.Screen5250")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Screen5250 screen = (Screen5250) programElementsInput.get("Screen");
          String userName = (String) programElementsInput.get("UserName");
          String password = (String) programElementsInput.get("Password");
         
          
    
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
             
        	  setTextAt(screen, 5, 25, userName);
        	  Thread.sleep(1000);

        	  setTextAt(screen, 6, 25, password);
        	  Thread.sleep(1000);

        	              screen.sendKeys(KeyMnemonic.ENTER);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Logged in successfully" );

          }
          catch(Exception e) {
        	  
        	     e.printStackTrace();
                 StringWriter sw = new StringWriter();
                 e.printStackTrace(new PrintWriter(sw));

                 nlpResponseModel.setStatus(CommonConstants.fail);
                 nlpResponseModel.setMessage("Failed to login : " + sw);
                       }

//          nlpResponseModel.getAttributes().put("session", returnValue);
          return nlpResponseModel;
      }
    public static void setTextAt(Screen5250 screen, int rowIndex, int columnIndex, String stringToSet) {

        screen.setCursor(rowIndex, columnIndex);
        screen.sendKeys(stringToSet);

    }
  } 