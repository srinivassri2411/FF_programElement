package Business_Logics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.sikuli.script.Image;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;



public class ClickBasedOnImage implements Nlp {
    @InputParams({@InputParam(name = "Image Stream", type = "java.io.InputStream"),@InputParam(name = "Region", type = "java.lang.String")})

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
		InputStream imageStream = (InputStream) attributes.get("Image Stream");
       String region = (String) attributes.get("Region");
       region= region.toLowerCase();

       System.setProperty("java.awt.headless", "false");
       try
       {
          Thread.sleep(1000);
          BufferedImage bufferedImage = ImageIO.read(imageStream);
          
//          File tempfile = File.createTempFile("Sikuli_image", ".png");
//          ImageIO.write(imageStream, "png", tempfile);
          Screen screen = new Screen();
//          Image sikuliImage =new Image(bufferedImage);
//          Pattern imagePattern = new Pattern(tempfile.getAbsolutePath());
          Pattern imagePattern = new Pattern(bufferedImage);
          switch (region) {
          case "below":
             screen.find(imagePattern).belowAt().click();

             break;
          case "above":
             screen.find(imagePattern).aboveAt().click();
             break;
          case "right":
             screen.find(imagePattern).rightAt().click();
             break;
          case "left":
             screen.find(imagePattern).leftAt().click();
             break;
          case "centre":
             screen.find(imagePattern).click();
             break;

          default:
             break;
          }


          nlpResponseModel.setStatus(CommonConstants.pass);
          nlpResponseModel.setMessage("Successfully performed Click Operation ");    
       }
       catch(Throwable e) {
          StringWriter sw = new StringWriter();
          e.printStackTrace(new PrintWriter(sw));
          String exceptionAsString = sw.toString();
          //log.info(exceptionAsString);
          nlpResponseModel.setStatus(CommonConstants.fail);
          nlpResponseModel.setMessage("Failed to perform action "+exceptionAsString);    
       }
       return nlpResponseModel;
    }
    public static void main(String[] args) throws NlpException, InterruptedException{
    	System.out.println("started");
    	Thread.sleep(3000);
        NlpRequestModel model=new NlpRequestModel();
        Map<String, Object> val =model.getAttributes();
        val.put("Image Stream", "C:\\Users\\rudutha\\Downloads\\regs.png");
        val.put("Region", "centre");
        ClickBasedOnImage a=new ClickBasedOnImage();
        a.execute(model);
        System.out.println("done");
		
	}

}