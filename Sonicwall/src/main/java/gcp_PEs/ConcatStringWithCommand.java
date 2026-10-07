
package gcp_PEs;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class ConcatStringWithCommand implements Nlp {
    @InputParams({@InputParam(name = "command1", type = "java.lang.String"),@InputParam(name = "command2", type = "java.lang.String")})
    @ReturnType(name = "returnData", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String command1 = (String) programElementsInput.get("command1");
          String command2 = (String) programElementsInput.get("command2");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  returnValue= command1.concat("\n").concat(command2);
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("returnData", returnValue);
          return nlpResponseModel;
      }
    public static void main(String[] args) throws NlpException {
    	NlpRequestModel model=new NlpRequestModel();
		model.getAttributes().put("command1", "cd connector-2.0.1/\n"
				+ "./test-connection.sh --domain \"release.bnntest.com\"");
		model.getAttributes().put("command2", "echo 'command_center_url: https://release.bnntest.com\n"
				+ "api_key_secret: -0SZngj3TqLYfsArJZQkwbC-ezmacE_bOpGs7TfEsYo\n"
				+ "connector_name: aefqwe13' > connector-config.yaml");
	System.out.println(new ConcatStringWithCommand().execute(model));	}

  } 