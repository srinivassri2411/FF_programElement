package MediBuddy;



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
import java.util.Arrays;
import java.util.List;




public class ListToArray implements Nlp {
    @InputParams({@InputParam(name = "String", type = "java.lang.String"),@InputParam(name = "Regex", type = "java.lang.String")})
    @ReturnType(name = "List", type = "java.util.List")

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
//          Arrays array = (Arrays) attributes.get("Array");
          String s = (String) attributes.get("String");
          String regex = (String) attributes.get("Regex");

          // Your program element business logic goes here ...
          List list=null;
          try {
        	  String[] x = s.split(regex);
      		 list = Arrays.asList(x);
//        	  list= (List) Arrays.asList(array);
      		  nlpResponseModel.setStatus(CommonConstants.pass);
       	  nlpResponseModel.setMessage("Successfully converted array into list");
        	  
        	  
          }
          catch (Exception e) {
        	  nlpResponseModel.setStatus(CommonConstants.fail);
        	  nlpResponseModel.setMessage("Failed to convert");
          }

//          String string3 = "Return Value";
          nlpResponseModel.getAttributes().put("List", list);
          return nlpResponseModel;
      }
  } 

