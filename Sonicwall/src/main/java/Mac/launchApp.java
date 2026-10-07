
package Mac;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

import org.sikuli.script.FindFailed;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.mac.options.Mac2Options;

public class launchApp implements Nlp {
    @InputParams({@InputParam(name = "bundleId", type = "java.lang.String")})
    @ReturnType(name = "Sum of Two numbers", type = "io.appium.java_client.mac.Mac2Driver")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          
          String bundleId = (String) programElementsInput.get("bundleId");

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Mac2Driver driver=null;
          try {
        	  Mac2Options options=new Mac2Options();
      		options.setCapability("appium:automationName", "Mac2");
      		options.setCapability("platformName", "mac");
      		options.setCapability("appium:bundleId",bundleId);
      		options.setCapability("appium:newCommandTimeout", "0");
      		 driver = new Mac2Driver(new URL("http://localhost:4723"),options);
      		 //newly added line 20 june
      		nlpResponseModel.setWebDriver(driver);
      		 driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
      	//	/nlpRequestModel.setWebDriver(driver);
      Thread.sleep(15000);
      
                  nlpResponseModel.setStatus(CommonConstants.pass);
                  nlpResponseModel.setMessage("Application launched successfully!");
                 System.out.println("done");
          
          }
          catch(Exception e) {
        	  StringWriter sw = new StringWriter();
  			e.printStackTrace(new PrintWriter(sw));
  			String exceptionAsString = sw.toString();
  			nlpResponseModel.setMessage("Failed to launch : " + exceptionAsString);
             nlpResponseModel.setStatus(CommonConstants.fail);
             System.out.println(exceptionAsString);
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Sum of Two numbers", driver);
          return nlpResponseModel;
      }
    public static void main(String[] args) throws FindFailed, InterruptedException, NlpException {

        NlpRequestModel nlpResponseModel = new NlpRequestModel();
        Map<String, Object> programElementsInput = nlpResponseModel.getAttributes();
        programElementsInput.put("Command", "sudo installer -pkg /Users/rahulkulkarni/Downloads/Banyan-3.22.2.pkg -target /");
        new launchMac3().execute(nlpResponseModel);

		
	}

  } 