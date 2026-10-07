package sikuli;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;


import org.sikuli.script.Button;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class Capture_Image implements Nlp {
	@InputParams({@InputParam(name = "Image Stream", type = "java.io.InputStream"),@InputParam(name = "xCoordinate", type = "java.lang.Integer"), 
		@InputParam(name = "width", type = "java.lang.Integer"),
		@InputParam(name = "Direction", type = "java.lang.String"),@InputParam(name = "Path", type = "java.lang.String")})

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
		//String region = (String) attributes.get("Region");
		Integer xCoordinate = (Integer) attributes.get("xCoordinate");
		Integer width = (Integer) attributes.get("width");
		String direction = (String) attributes.get("Direction");
		String path = (String) attributes.get("Path");
		direction=direction.toLowerCase();

		//Integer ScrollCount = (Integer) attributes.get("ScrollCount");

		//region = region.toLowerCase();

		System.setProperty("java.awt.headless", "false");
		try {
			Thread.sleep(1000);
			BufferedImage bufferedImage = ImageIO.read(imageStream);
			Screen screen = new Screen();
			Pattern imagePattern = new Pattern(bufferedImage);
			Rectangle rect = screen.find(imagePattern).getRect();
			int x=rect.x;
			int y=rect.y;
			int w=rect.width;
			int h=rect.height;
			ScreenImage image=null;
			switch (direction) {
			case "left":
			
				 image = screen.capture(x-xCoordinate, y, w+width, h);
				image.save(path);
				break;
			case "right":
				 image = screen.capture(x+xCoordinate, y, w+width, h);
				image.save(path);
				break;

			default:
				break;
			}


			//screen.wheel(imagePattern, Button.WHEEL_DOWN, ScrollCount);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Captured Image Successfully");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Capture Image " + exceptionAsString);
		}
		return nlpResponseModel;
	}
}
