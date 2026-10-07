package business_logics_Android;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
public class GenrateRandomNumberInTheRange implements Nlp {
    @InputParams({@InputParam(name = "StartingRange", type = "java.lang.Integer"),
    	          @InputParam(name = "EndingRange", type = "java.lang.Integer")})
    @ReturnType(name = "RandomNumber", type = "java.lang.Integer")
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
          int range1 = (Integer) attributes.get("StartingRange");
          int range2 = (Integer) attributes.get("EndingRange");
          int RandomNumber = 0 ;
          // Your program element business logic goes here ...
            try {
            	RandomNumber = ThreadLocalRandom.current().nextInt(range1, range2);
            	nlpResponseModel.setStatus(CommonConstants.pass);
          	  nlpResponseModel.setMessage("The Random Number is "+RandomNumber );
            }
          catch(Exception e) {
        	  nlpResponseModel.setStatus(CommonConstants.fail);
        	  nlpResponseModel.setMessage("Failed to generate Random Number" );
          }
          nlpResponseModel.getAttributes().put("RandomNumber", RandomNumber);
          return nlpResponseModel;
      }
  }