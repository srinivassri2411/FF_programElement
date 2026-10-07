package sikuli;

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


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j


public class ScrollDownOrUp implements Nlp {
	@InputParams({ @InputParam(name = "Image Stream", type = "java.io.InputStream"),
		@InputParam(name = "Direction", type = "java.lang.String"),
		@InputParam(name = "ScrollCount", type = "java.lang.Integer") })

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
		String region = (String) attributes.get("Direction");
		Integer ScrollCount = (Integer) attributes.get("ScrollCount");

		region = region.toLowerCase();

		System.setProperty("java.awt.headless", "false");
		try {
			Thread.sleep(1000);
			BufferedImage bufferedImage = ImageIO.read(imageStream);
			Screen screen = new Screen();
			Pattern imagePattern = new Pattern(bufferedImage);
			switch (region) {
			case "up":
				screen.wheel(imagePattern, Button.WHEEL_UP, ScrollCount);
				break;
			case "down":
				screen.wheel(imagePattern, Button.WHEEL_DOWN, ScrollCount);
				break;

			default:
				break;
			}


			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Scrolled Successfully");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to scroll " + exceptionAsString);
		}
		return nlpResponseModel;
	}
}
