package business_logics_Android;

import com.tyss.optimize.common.util.CommonConstants;

import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import io.appium.java_client.PushesFiles;
import io.appium.java_client.android.AndroidDriver;
import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PushFileFromLocalMachineToDevice implements Nlp {
    @InputParams({ @InputParam(name = "Destination Path", type = "org.openqa.selenium.String"),
    	@InputParam(name = "File Path", type = "org.openqa.String")})

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
         
         
          String filePath = (String) attributes.get("File Path");
          String destinationPath=(String) attributes.get("Destination Path");  
        
          AndroidDriver driver=(AndroidDriver)nlpRequestModel.getAndroidDriver();
         
          try {
        	  File file = new File(filePath); 
        	    if (file.exists()) {
        	        ((PushesFiles) driver).pushFile(destinationPath, file);    	       
        	    }
        	    File verFile=new File(filePath);
        		if(verFile.exists()) {
        			verFile.delete();
        		}
        		 nlpResponseModel.setMessage(" File pushed successfully ");
                 nlpResponseModel.setStatus(CommonConstants.pass);
          
          
          }catch (Exception e) {
			// TODO: handle exception
        	  nlpResponseModel.setMessage(" Fail to push the file " + e );
              nlpResponseModel.setStatus(CommonConstants.fail);
		}
 
          return nlpResponseModel;
      }
    
      
  } 
