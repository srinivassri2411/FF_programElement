package Mac;





import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.sikuli.script.FindFailed;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.mac.Mac2Driver;
import io.appium.java_client.mac.options.Mac2Options;

public class OpenOktaApp implements Nlp {
//    @InputParams({@InputParam(name = "Command", type = "java.lang.String")})
    @ReturnType(name = "instance", type = "io.appium.java_client.android.AndroidDriver")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          
//          String name = (String) programElementsInput.get("Command");

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
         // Mac2Driver driver=null;
          AndroidDriver driver =null;
          try {
        	  DesiredCapabilities cap = new DesiredCapabilities();
      		cap.setCapability("appium:platformName", "Android");
      		cap.setCapability("appium:automationName", "UiAutomator2");
      		cap.setCapability("appium:DeviceName", "a13nnxxF");
      		cap.setCapability("appium:appPackage", "com.okta.android.auth");
      		cap.setCapability("appium:appActivity", "com.okta.android.auth.activity.SplashActivity");
      		cap.setCapability("appium:noReset", "true");
      		 driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), cap);
      		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(25));
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
          nlpResponseModel.getAttributes().put("instance", driver);
          return nlpResponseModel;
      }
    public static void main(String[] args) throws FindFailed, InterruptedException, NlpException {

        NlpRequestModel nlpResponseModel = new NlpRequestModel();
        Map<String, Object> programElementsInput = nlpResponseModel.getAttributes();
//        programElementsInput.put("Command", "sudo installer -pkg /Users/rahulkulkarni/Downloads/Banyan-3.22.2.pkg -target /");
        new OpenOktaApp().execute(nlpResponseModel);

		
	}

  } 
