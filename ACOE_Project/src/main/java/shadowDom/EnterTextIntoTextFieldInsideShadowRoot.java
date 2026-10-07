
package shadowDom;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class EnterTextIntoTextFieldInsideShadowRoot implements Nlp {
    @InputParams({@InputParam(name = "Shadow Root", type = "org.openqa.selenium.SearchContext"), @InputParam(name = "Css Loactor Value", type = "java.lang.String")
    , @InputParam(name = "Text", type = "java.lang.String")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          SearchContext shadowRoot = (SearchContext) programElementsInput.get("Shadow Root");
          String cssLoactorValue = (String) programElementsInput.get("Css Loactor Value");
          String text = (String) programElementsInput.get("Text");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  shadowRoot.findElement(By.cssSelector(cssLoactorValue)).sendKeys(text);
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage(e.getMessage());
             throw new NlpException(e.getMessage());

          }

          // Your program element business logic ends here ...
          return nlpResponseModel;
      }

  } 