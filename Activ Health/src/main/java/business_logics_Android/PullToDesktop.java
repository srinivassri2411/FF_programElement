
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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;




public class PullToDesktop implements Nlp {
    @InputParams({@InputParam(name = "Audio File Name", type = "java.lang.String"), @InputParam(name = "Audio File Path", type = "java.lang.String"),
    @InputParam(name = "Down File Path", type = "java.lang.String")})
    @ReturnType(name = "string3", type = "java.lang.String")

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
          String audioFileName = (String) attributes.get("Audio File Name");
          String audioFilePath = (String) attributes.get("Audio File Path");
          String downFilePath = (String) attributes.get("Down File Path");
          String mp3InputFile = downFilePath + "\\" + audioFileName;
          String wavOutputFile = downFilePath + "\\" + "convert.wav";
          // Your program element business logic goes here ...
         try  {
        	 
         
             File verFile=new File(mp3InputFile);
			if(verFile.exists()) {
				verFile.delete();
			}
			File convFile=new File(wavOutputFile);
			if(convFile.exists()) {
				convFile.delete();
			}
		 	ProcessBuilder processBuilder = new ProcessBuilder("adb", "pull", audioFilePath+"/"+audioFileName, downFilePath);
          Process process = processBuilder.start();

          // Read the output from the command
          BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String line;
          while ((line = reader.readLine()) != null) {
              System.out.println(line);
          }
          process.waitFor(); // Wait for the command to complete
          
          // Verify file has been pulled
          System.out.println("File pulled to: " + downFilePath);
          
          nlpResponseModel.setMessage("File pulled to: " + downFilePath);
          nlpResponseModel.setStatus(CommonConstants.pass);
         }catch (Exception e) {
			// TODO: handle exception
        	 nlpResponseModel.setMessage("Failed to pull the file");
             nlpResponseModel.setStatus(CommonConstants.pass);
		}

          String string3 = downFilePath;
         nlpResponseModel.getAttributes().put("string3", string3);
          return nlpResponseModel;
      }
  } 
