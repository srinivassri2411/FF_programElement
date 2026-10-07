package Mac;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ReplaceString implements Nlp {
    @InputParams({@InputParam(name = "Action py file", type = "java.lang.String"), @InputParam(name = "new py file", type = "java.lang.String"),
    	@InputParam(name = "new String", type = "java.lang.String")})
//    @ReturnType(name = "Sum of Two numbers", type = "java.lang.Integer")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String old = (String) programElementsInput.get("Action py file");
          String newFile = (String) programElementsInput.get("new py file");
          String newString = (String) programElementsInput.get("new String");
//          String text = (String) programElementsInput.get("input");

          try {
        	  Path input = Paths.get(old);
              Path output = Paths.get(newFile);

//              Path input = Paths.get("C:/scripts/test.py");
//
//              // 🔥 MUST be different filename
//              Path output = Paths.get("C:/scripts/test_new.py");

              String content = Files.readString(input, StandardCharsets.UTF_8);

              content = content.replace("Password", newString);
//              if (!text.equals("")) {
//                  content = content.replace("input", text);
//			}

              // Always creates new file
              Files.writeString(output, content, StandardCharsets.UTF_8,
                      StandardOpenOption.CREATE,
                      StandardOpenOption.TRUNCATE_EXISTING);
              
              nlpResponseModel.setMessage("String replaces Succesfully");
              nlpResponseModel.setStatus(CommonConstants.pass);
              }
          catch(Exception e) {
           // Your program element Exception goes here ...
              nlpResponseModel.setMessage("Failed to replace string "+e);
              nlpResponseModel.setStatus(CommonConstants.fail);
              }

          // Your program element business logic ends here ...
//          nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
          return nlpResponseModel;
      }

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
  } 