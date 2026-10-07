
package shadowDom;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class SwitchToShadowDOM implements Nlp {
    @InputParams({@InputParam(name = "Root element", type = "org.openqa.selenium.WebElement")})
    @ReturnType(name = "Search Context", type = "org.openqa.selenium.SearchContext")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          WebElement root = (WebElement) programElementsInput.get("Root element");
          SearchContext returnValue=null;
          

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  returnValue = root.getShadowRoot();
        	  
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage(e.getMessage());
             throw new NlpException(e.getMessage());

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Search Context", returnValue);
          return nlpResponseModel;
      }

  } 