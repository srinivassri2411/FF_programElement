
package business_logics_Android;

import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

import io.appium.java_client.android.AndroidDriver;

public class GetSource implements Nlp {
   

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          
          Integer returnValue=null;
          AndroidDriver driver=(AndroidDriver)nlpRequestModel.getAndroidDriver();
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  driver.getPageSource();
              // Your program element business logic starts here ...
             

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("got page source");
              //Uncomment the below line and modify the message, only if you want to override the pass message defined for the program element.
              //nlpResponseModel.setMessage("Added two numbers");
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("didn't get page source");

             //Uncomment the below line and modify the message, only if you want to override the fail message defined for the program element.
             //nlpResponseModel.setMessage("Failed to add numbers");
          }
		return nlpResponseModel;

          // Your program element business logic ends here ...
          
      }

  } 