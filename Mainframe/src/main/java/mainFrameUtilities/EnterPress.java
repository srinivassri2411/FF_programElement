
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

public class EnterPress implements Nlp {
	   @InputParams({@InputParam(name = "Screen", type = "org.tn5250j.framework.tn5250.Screen5250")	
	    })
//	    @ReturnType(name = "text", type = "java.lang.String")

	      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

	          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
	          Screen5250 screen = (Screen5250) programElementsInput.get("Screen");
//	          String string = (String) programElementsInput.get("String to Enter");
//	          Integer row = (Integer) programElementsInput.get("Row Index");
//	          Integer column = (Integer) programElementsInput.get("Column Index");
	          
	          NlpResponseModel nlpResponseModel = new NlpResponseModel();
	          try {
	              screen.sendKeys(KeyMnemonic.ENTER);

	              nlpResponseModel.setStatus(CommonConstants.pass);
	              nlpResponseModel.setMessage("Enter pressed Successfully");

	          }
	          catch(Exception e) {
	        	  
	        	     e.printStackTrace();
	                 StringWriter sw = new StringWriter();
	                 e.printStackTrace(new PrintWriter(sw));

	                 nlpResponseModel.setStatus(CommonConstants.fail);
	                 nlpResponseModel.setMessage("Failed to press Enter : " + sw);
	                       }

//	          nlpResponseModel.getAttributes().put("text", text);
	          return nlpResponseModel;
	      }

	  } 