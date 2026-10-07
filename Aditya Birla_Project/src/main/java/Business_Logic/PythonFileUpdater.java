
package Business_Logic;

import java.nio.file.Files;
import java.nio.file.Paths;
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



public class PythonFileUpdater implements Nlp {
    @InputParams({@InputParam(name = "Excel File Path", type = "java.lang.String"), @InputParam(name = "Macro Name", type = "java.lang.String"), @InputParam(name = "Python File Path", type = "java.lang.String")})
//    @ReturnType(name = "string3", type = "java.lang.String")

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
          String excelFilePath = (String) attributes.get("Excel File Path");
          String macroName = (String) attributes.get("Macro Name");
          String pythonFilePath = (String) attributes.get("Python File Path");

        try {
        	 String pythonScript = new String(Files.readAllBytes(Paths.get(pythonFilePath)), "UTF-8");
             excelFilePath = excelFilePath.replace("\\", "\\\\");

             pythonScript = pythonScript.replaceFirst("excel_file_path = r\"[^\"]*\"",
                                                       "excel_file_path = r\"" + excelFilePath + "\"");
             pythonScript = pythonScript.replaceFirst("macro_name = \"[^\"]*\"",
                                                       "macro_name = \"" + macroName + "\"");

             Files.write(Paths.get(pythonFilePath), pythonScript.getBytes("UTF-8"));
             
             nlpResponseModel.setMessage("Python file edited");
             nlpResponseModel.setStatus(CommonConstants.pass);
        }
catch(Exception e) {
	nlpResponseModel.setMessage("Failed to edit");
    nlpResponseModel.setStatus(CommonConstants.fail);
}
//          String string3 = "Return Value";
//          nlpResponseModel.getAttributes().put("string3", string3);
          return nlpResponseModel;
      }
  } 