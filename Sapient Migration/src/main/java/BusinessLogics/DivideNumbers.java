package BusinessLogics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class DivideNumbers implements Nlp {
    @InputParams({@InputParam(name = "Number 1", type = "java.lang.Double"), @InputParam(name = "Number 2", type = "java.lang.Double")})
    @ReturnType(name = "Division Result", type = "java.lang.Double")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          double a = (Double) programElementsInput.get("Number 1");
          double b = (Double) programElementsInput.get("Number 2");
          double c=0;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
           
          	
        	  c=a/b;
              nlpResponseModel.setStatus(CommonConstants.pass);
              //Uncomment the below line and modify the message, only if you want to override the pass message defined for the program element.
              //nlpResponseModel.setMessage("Added two numbers");

              //set the list of local file absolute paths (if any) to be uploaded to server which can later be downloaded from the step result section.
              //nlpResponseModel.setOutputLocalFilePaths(List.of("/absolute/file/path1","/absolute/file/path2"));
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);

             //Uncomment the below line and modify the message, only if you want to override the fail message defined for the program element.
             //nlpResponseModel.setMessage("Failed to add numbers");
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Division Result", c);
          return nlpResponseModel;
      }

  } 