
package default_package;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class Subtract implements Nlp {
    @InputParams({@InputParam(name = "a", type = "java.lang.String"), @InputParam(name = "b", type = "java.lang.String")})
    @ReturnType(name = "c", type = "java.lang.Integer")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String a = (String) programElementsInput.get("a");
          String b = (String) programElementsInput.get("b");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              int c = Integer.parseInt(a)-Integer.parseInt(b);
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("c", returnValue);
          return nlpResponseModel;
      }

  } 