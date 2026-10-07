package Business_Logics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetTextFromElement implements Nlp {
	@InputParams({ @InputParam(name = "Image Stream", type = "java.io.InputStream"),
			@InputParam(name = "Region", type = "java.lang.String"),
			@InputParam(name = "Height", type = "java.lang.Integer"),
			@InputParam(name = "Width", type = "java.lang.Integer")})
	@ReturnType(name = "Text", type = "java.lang.String")

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}
	//java.io.InputStream

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
//		String imageStream = (String) attributes.get("Image Stream");

		String regionMain = (String) attributes.get("Region");
		int hei = (int) attributes.get("Height");
		int wid = (int) attributes.get("Width");
		regionMain = regionMain.toLowerCase();
		System.setProperty("java.awt.headless", "false");
		String text = null;
		try {
			Thread.sleep(1000);
			BufferedImage bufferedImage = ImageIO.read(imageStream);
			Screen screen = new Screen();
			Pattern imagePattern = new Pattern(bufferedImage);
			Match match = screen.find(imagePattern);
			
			

			
            if (match != null) {
                Region region1 = null;

                // Define the size of the limited area
                int width = wid;  // Adjust width as needed
                int height =hei;  // Adjust height as needed

                switch (regionMain) {
                    case "above":
                        region1 = match.above();
                        break;
                    case "below":
                        region1 = match.below();
                        break;
                    case "left":
                        region1 = match.left();
                        break;
                    case "right":
                        region1 = match.right();
                        break;
                    case "center":
                    case "centre":
                        region1 = match;
                        break;
                }

                if (region1 != null) {
                    // Define the limited area around the match
                    Region limitedArea = new Region(region1.getX(), region1.getY(), width, height);
                    
                    // Highlight the region (optional, for visualization)
                    limitedArea.highlight(2);

                    // Get text from the limited area
                    text = limitedArea.text();
                    System.out.println(text);
                }

				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Extracted Text: " + text);

			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			// log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Invalid region attribute, Image not found on the screen " + exceptionAsString);
		}
		nlpResponseModel.getAttributes().put("Text", text);
		return nlpResponseModel;
	}
	 public static void main(String[] args) throws NlpException, InterruptedException{
	    	System.out.println("started");
	    	Thread.sleep(3000);
	        NlpRequestModel model=new NlpRequestModel();
	        Map<String, Object> val =model.getAttributes();
	        val.put("Image Stream", "C:\\Users\\rudutha\\Downloads\\set.png");
	        val.put("Region", "right");
	        val.put("Height", 60);
	        val.put("Width", 59);
	        GetTextFromElement a=new GetTextFromElement();
	        a.execute(model);
	        System.out.println("done");
			
		}

}