package others;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class switchToTabWithTitleContains implements Nlp {
    @InputParams({@InputParam(name = "Partial Title", type = "java.lang.String")})
   // @ReturnType(name = "Sum of Two numbers", type = "java.lang.Integer")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String partialTitle = (String) programElementsInput.get("Partial Title");
          
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          WebDriver driver = nlpRequestModel.getWebDriver();
          try {
              String originalWindow = driver.getWindowHandle();

                for (String handle : driver.getWindowHandles()) {
                    driver.switchTo().window(handle);
                    String title = driver.getTitle();
                    if (title.contains(partialTitle)) {
                        System.out.println("Switched to tab: " + title);
                       break;
                    }
                }

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Successfully switched the tab");
              
          }
          catch(Exception e) {
               // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("failed to switched the tab");

          }

          // Your program element business logic ends here ...
       //   nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
          return nlpResponseModel;
      }

  }