package image_utility;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;




public class Add2Numbers implements Nlp {
    @InputParams({@InputParam(name = "Num1", type = "java.lang.Integer"),
    			 @InputParam(name = "Num2", type = "java.lang.Integer")
    })
//    @ReturnType(name = "Sum", type = "java.lang.String")

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
          Integer string1 = (Integer) attributes.get("Num1");
          Integer string2 = (Integer) attributes.get("Num2");
          int sum=0;
          try {
        	  sum=string1+string2;
        	  nlpResponseModel.setMessage("Sucessfully Created Partition in Canvas Element" + sum);
  			nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
        	  nlpResponseModel.setMessage("Sucessfully Created Partition in Canvas Element");
  			nlpResponseModel.setStatus(CommonConstants.fail);
        	  
          }
          // Your program element business logic goes here ...

//          String string3 = "Return Value";
//          nlpResponseModel.getAttributes().put("string3", string3);
          return nlpResponseModel;
      }
  } 