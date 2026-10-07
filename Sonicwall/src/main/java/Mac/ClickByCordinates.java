
package Mac;


import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
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

public class ClickByCordinates implements Nlp {
//    @InputParams({@InputParam(name = "xpath", type = "java.lang.String"),@InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver"),})
//    @ReturnType(name = "Sum of Two numbers", type = "io.appium.java_client.mac.Mac2Driver")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          
//          String xpath = (String) programElementsInput.get("xpath");
//          Mac2Driver driver = (Mac2Driver) programElementsInput.get("driver");


          NlpResponseModel nlpResponseModel = new NlpResponseModel();
//          Mac2Driver driver=null;
          try {
              new ProcessBuilder("osascript", "-e", "tell application \"System Events\"\n" +
                      "   tell application \"Sonicwall Cloud Secure Edge\" to activate\n" +
                      "   repeat 11 times\n" +
                      "       key code 48\n" +
                      "       delay 0.1\n" +
                      "   end repeat\n" +
                      "   delay 0.3\n" +   // wait a bit longer before pressing Enter
                      "   key code 36\n" + // Enter
                      "end tell").start();
                  nlpResponseModel.setStatus(CommonConstants.pass);
                  nlpResponseModel.setMessage("successfully clicked on the coordinates of the element");
                 System.out.println("done");
          
          }
          catch(Exception e) {
              StringWriter sw = new StringWriter();
              e.printStackTrace(new PrintWriter(sw));
              String exceptionAsString = sw.toString();
              nlpResponseModel.setMessage("Failed to click : " + exceptionAsString);
             nlpResponseModel.setStatus(CommonConstants.fail);
             System.out.println(exceptionAsString);
          }

          // Your program element business logic ends here ...
//          nlpResponseModel.getAttributes().put("Sum of Two numbers", driver);
          return nlpResponseModel;
      }
    public static void main(String[] args) throws FindFailed, InterruptedException, NlpException {

        NlpRequestModel nlpResponseModel = new NlpRequestModel();
        Map<String, Object> programElementsInput = nlpResponseModel.getAttributes();
        programElementsInput.put("Command", "sudo installer -pkg /Users/rahulkulkarni/Downloads/Banyan-3.22.2.pkg -target /");
        new ClickByCordinates().execute(nlpResponseModel);

        
    }

  }