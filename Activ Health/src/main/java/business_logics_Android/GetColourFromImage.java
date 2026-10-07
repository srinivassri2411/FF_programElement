package business_logics_Android;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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

public class GetColourFromImage implements Nlp {
    @InputParams({@InputParam(name = "Xpath", type = "java.lang.String")})
    @ReturnType(name = "Color and Count", type = "java.lang.LinkedHashMap")
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String locatorValue = (String) programElementsInput.get("Xpath");
          LinkedHashMap<String, String> map= new LinkedHashMap<>();

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          
          try {
              // Your program element business logic starts here ...
        	  AndroidDriver driver = nlpRequestModel.getAndroidDriver();
        	  File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        	   @SuppressWarnings("unchecked")
        	   List<WebElement> elems= driver.findElements(By.xpath(locatorValue));
        	   List<String>hexCodes = new ArrayList<>();
        	   for(WebElement element : elems) {
        		   int elementX = element.getLocation().getX();
                   int elementY = element.getLocation().getY();

                   // Get the size (width and height) of the element
                   int elementWidth = element.getSize().getWidth();
                   int elementHeight = element.getSize().getHeight();

                   // Calculate the center coordinates
                   int centerX = elementX + (elementWidth / 2);
                   int centerY = elementY + (elementHeight / 2);
        		   
        		   BufferedImage image = ImageIO.read(scrFile);
        		   // Getting pixel color by position x and y 
        		   int clr = image.getRGB(centerX,centerY); 
        		   int r   = (clr & 0x00ff0000) >> 16;
        		   int g = (clr & 0x0000ff00) >> 8;
        		   int b  =  clr & 0x000000ff;
        		   String hex = String.format("#%02x%02x%02x", r, g, b);
        		   hexCodes.add(hex);
        		   Set<String>colors=new HashSet<String>();  
        		   
        		   for(String a:hexCodes) {
        			   colors.add(a);
        		   }
        		   
        		   for(String color:colors) {
        			   String ans = getColorName(color); 
        			   int colorCount = 0;
        			   for(int i=0;i<hexCodes.size();i++) {
        				  
        				   if(hexCodes.get(i).equals(color)) {
        					   colorCount++;
        				   }
        			   }
        			   String colorCount1 = String.valueOf(colorCount);
        			   map.put(ans, colorCount1);
        			   
        		   }
        		  
        			   
        		   
        	   }
        	   nlpResponseModel.setStatus(CommonConstants.pass);
   			nlpResponseModel.setMessage("Successfull in getting colors and ");
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
    			nlpResponseModel.setMessage("" +e);


             //Uncomment the below line and modify the message, only if you want to override the fail message defined for the program element.
             //nlpResponseModel.setMessage("Failed to add numbers");
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Color and Count", map);
          return nlpResponseModel;
          
          
          
      }

    static String getColorName(String hexCode) {
    	try {
    		@SuppressWarnings("deprecation")
			URL url = new URL("https://www.crispedge.com/color/" + hexCode.substring(1));
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    		connection.setRequestMethod("GET");

    		int responseCode = connection.getResponseCode();
    		if (responseCode == HttpURLConnection.HTTP_OK) {
    			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    			String inputLine;
    			StringBuilder response = new StringBuilder();

    			while ((inputLine = in.readLine()) != null) {
    				response.append(inputLine);
    			}
    			in.close();
    			String jsonString = response.toString();

    			int nameStartIndex = jsonString.indexOf("hex color  - ") + 13;
    			int nameEndIndex = jsonString.indexOf("</title>", nameStartIndex);
    			return jsonString.substring(nameStartIndex, nameEndIndex);
    		} else {
    			return "Unknown";
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    		return "Error";
    	}
     }





}
