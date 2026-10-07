package business_logics_Android;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
public class WavPathUpdater implements Nlp {
    @InputParams({@InputParam(name = "Wav File Path", type = "java.lang.String"), @InputParam(name = "Python File Path", type = "java.lang.String")})
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
      public  NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> attributes = nlpRequestModel.getAttributes();
          String excelFilePath = (String) attributes.get("Wav File Path");
//          String macroName = (String) attributes.get("Macro Name");
          String pythonFilePath = (String) attributes.get("Python File Path");

        try {
        	replaceAudioFilePath(pythonFilePath, excelFilePath);
             nlpResponseModel.setMessage("Path Updated Successfully");
             nlpResponseModel.setStatus(CommonConstants.pass);
        }
catch(Exception e) {
	 nlpResponseModel.setMessage("path Update Unsuccessfull"+e);
     nlpResponseModel.setStatus(CommonConstants.pass);
	
}
//          String string3 = "Return Value";
//          nlpResponseModel.getAttributes().put("string3", string3);
          return nlpResponseModel;
      }
 
    public static void replaceAudioFilePath(String pythonFilePath, String newAudioFilePath) {
          try {
              // Read all lines from the Python file
              String content = new String(Files.readAllBytes(Paths.get(pythonFilePath)));

              // Replace the audioFilePath variable value
              String oldAudioFilePathRegex = "audioFilePath\\s*=\\s*r?'[^']*'"; // Matches audioFilePath = r'oldPath' or audioFilePath = 'oldPath'
              String newAudioFilePathLine = "audioFilePath = r'" + newAudioFilePath.replace("\\", "\\\\") + "'"; // Escape backslashes for Java and Python

              content = content.replaceAll(oldAudioFilePathRegex, newAudioFilePathLine);

              // Write the modified content back to the Python file
              Files.write(Paths.get(pythonFilePath), content.getBytes());

              System.out.println("Audio file path replaced successfully.");
              
          } catch (IOException e) {
              System.err.println("Error replacing audio file path: " + e.getMessage());
              e.printStackTrace();
          }
      }
  } 