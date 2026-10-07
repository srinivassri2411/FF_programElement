package retail;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
 
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
 
public class StringToHex implements Nlp {
    @InputParams({@InputParam(name = "Native Window Handle", type = "java.lang.String")})
    @ReturnType(name = "Hex  Value", type = "java.lang.String")
 
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
 
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String nativeWindowHandle = (String) programElementsInput.get("Native Window Handle");
          String hexHandle =null;
 
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
                long windowHandleInt = Long.parseLong(nativeWindowHandle);
                 hexHandle = "0x"+Long.toHexString(windowHandleInt);
 
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Window Handle converted to Hex Value");
          }
          catch(Exception e) {
              nlpResponseModel.setStatus(CommonConstants.fail);
              StringWriter sw = new StringWriter();
              e.printStackTrace(new PrintWriter(sw));
              String exceptionAsString = sw.toString();
              nlpResponseModel.setMessage("Failed  : "+exceptionAsString);
 
          }
 
          nlpResponseModel.getAttributes().put("Hex  Value", hexHandle);
          return nlpResponseModel;
      }
 
  }