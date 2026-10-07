package business_logics_Android;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;




public class ResizeTheImage implements Nlp {
    @InputParams({ @InputParam(name = "Source image path", type = "java.lang.String"),
    	@InputParam(name = "Resized Image path", type = "java.lang.String"),
    	@InputParam(name = "width", type = "java.lang.Integer"),
    	@InputParam(name = "height", type = "java.lang.Integer")})
    @ReturnType(name = "Output file Path", type = "java.lang.String")

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
         
          String sourceImagePath = (String) attributes.get("Source image path");
          String resizedImagePath = (String) attributes.get("Resized Image path");
          Integer width = (Integer) attributes.get("width");
          Integer height = (Integer) attributes.get("height");
          String path=null;
          
          
//         
        
          AndroidDriver driver=(AndroidDriver)nlpRequestModel.getAndroidDriver();
         
          try {
          
        	  File inputFile = new File(sourceImagePath);
              BufferedImage inputImage = ImageIO.read(inputFile);
              
              Image scaledImage = inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
              
              BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
              
              Graphics2D g2d = outputImage.createGraphics();
              g2d.drawImage(scaledImage, 0, 0, null);
              g2d.dispose();
              
              File outputFile = new File(resizedImagePath);
              ImageIO.write(outputImage, "png", outputFile);
              path=resizedImagePath;
        
         nlpResponseModel.setMessage("Image Resized Successfully");
         nlpResponseModel.setStatus(CommonConstants.pass);
        
          
          
          }catch (Exception e) {
			// TODO: handle exception
        	  nlpResponseModel.setMessage("Image is not Resized" + e +"");
              nlpResponseModel.setStatus(CommonConstants.fail);
		}
          
          
          
          
//          String string3 = "Return Value";
          nlpResponseModel.getAttributes().put("Output file Path", path);
          return nlpResponseModel;
      }
    
      
  } 
