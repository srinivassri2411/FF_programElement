
package businessLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class option implements Nlp {
    @InputParams({@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities")})
    @ReturnType(name = "string3", type = "org.openqa.selenium.remote.DesiredCapabilities")

      @Override
      public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();
        return params;
      }

      @Override
      public StringBuilder getTestCode() throws NlpException {
        StringBuilder sb = new StringBuilder();
        return sb;
      }
      @Override
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> attributes = nlpRequestModel.getAttributes();
          DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
//          String string2 = (String) attributes.get("string2");

          // Your program element business logic goes here ...
          
          try {
			
        	  ChromeOptions options = new ChromeOptions();
    		  Map<String, Object> prefs = new HashMap<>();
    		  prefs.put("download.default_directory", "C:\\Users\\User\\Downloads\\");
    //
//    	        // Enable headless mode
    		  options.addArguments("--headless");
    		  options.addArguments("--disable-dev-shm-usage");
    		  options.addArguments("--no-sandbox");
    		  options.addArguments("--disable-gpu");
    		  options.addArguments("--window-size=1920,1080");
    		  options.setExperimentalOption("prefs", prefs);
    		  cap.setCapability(ChromeOptions.CAPABILITY, options);
    		  nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Fail to Fetch the file size");
		} catch (Exception e) {
			// TODO: handle exception
			 nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Fail to Fetch the file size");
		}

          String string3 = "Return Value";
          nlpResponseModel.getAttributes().put("string3", cap);
          return nlpResponseModel;
      }
  } 