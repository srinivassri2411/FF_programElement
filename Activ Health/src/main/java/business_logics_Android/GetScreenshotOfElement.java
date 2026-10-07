package business_logics_Android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;




public class GetScreenshotOfElement implements Nlp {
    @InputParams({ @InputParam(name = "element", type = "org.openqa.selenium.WebElement"),
    	@InputParam(name = "File Path", type = "org.openqa.String")})
    @ReturnType(name = "Stored file Path", type = "org.openqa.String")

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
         
          WebElement el = (WebElement) attributes.get("element");
          String filePath = (String) attributes.get("File Path");
          String path=null;
          
          
//         
        
          AndroidDriver driver=(AndroidDriver)nlpRequestModel.getAndroidDriver();
         
          try {
          
     
        	 // WebElement element = driver.findElement(By.xpath("//androidx.cardview.widget.CardView[@resource-id=\"com.adityabirlahealth.insurance:id/cardActivAge\"]/android.view.ViewGroup/android.widget.ImageView")); // Replace with your element locator

  	        // Take the screenshot of the element
  	        File screenshot = el.getScreenshotAs(OutputType.FILE);

  	        // Specify the path where you want to save the screenshot
  	        File destinationFile = new File(filePath);

  	        // Copy the screenshot to the specified location
  	        FileUtils.copyFile(screenshot, destinationFile);
  	       path = destinationFile.getAbsolutePath();
        
         nlpResponseModel.setMessage("Screenshot Captured Succesfully");
         nlpResponseModel.setStatus(CommonConstants.pass);
        
          
          
          }catch (Exception e) {
			// TODO: handle exception
        	  nlpResponseModel.setMessage("Screenshot capture is Unsuccessfull" + e +"");
              nlpResponseModel.setStatus(CommonConstants.fail);
		}
          
          
          
          
//          String string3 = "Return Value";
          nlpResponseModel.getAttributes().put("Stored file Path", path);
          return nlpResponseModel;
      }
    
      
  } 
