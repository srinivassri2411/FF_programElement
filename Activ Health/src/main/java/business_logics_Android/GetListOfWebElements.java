package business_logics_Android;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;


public class GetListOfWebElements implements Nlp  {
    @InputParams({ 
    	@InputParam(name = "device", type = "java.lang.String"),
    	@InputParam(name = "xpath", type = "java.lang.String")})
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
          String D = (String) attributes.get("device");
          String xpath = (String) attributes.get("xpath");
          List<WebElement> list=new ArrayList<>();
          
//         
        
          AndroidDriver driver=(AndroidDriver)nlpRequestModel.getAndroidDriver();
          IOSDriver driver1=(IOSDriver)nlpRequestModel.getIosDriver();
          // Your program element business logic goes here ...
          String device=D.toLowerCase();
          try {
          
        if(device.contains("iphone")) {
        
        	 list = driver1.findElements(By.xpath(xpath));
           
            nlpResponseModel.setMessage("Successfully fetched the List of Web Elements");
            nlpResponseModel.setStatus(CommonConstants.pass);
        }else {
         
           list = driver.findElements(By.xpath(xpath));
         nlpResponseModel.setMessage("Successfully fetched the List of Web Elements");
         nlpResponseModel.setStatus(CommonConstants.pass);
        }
          
          
          }catch (Exception e) {
			// TODO: handle exception
        	  nlpResponseModel.setMessage("Failed to fetch the web Elements" + e +"");
              nlpResponseModel.setStatus(CommonConstants.pass);
		}
          
          
          
          
//          String string3 = "Return Value";
         nlpResponseModel.getAttributes().put("List", list);
          return nlpResponseModel;
      }
    
      
  } 
