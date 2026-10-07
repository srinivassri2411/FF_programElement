package Business_Logics;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class SwipeUntillElementISVisible implements Nlp {
	@InputParams({ @InputParam(name = "Image Stream", type = "java.io.InputStream"),
			@InputParam(name = "SwipeRange", type = "java.lang.Integer") })

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
		Integer region = (Integer) attributes.get("SwipeRange");

		System.setProperty("java.awt.headless", "false");
		int pixelsToMove = 0;
		try {
			Thread.sleep(1000);
			BufferedImage bufferedImage = ImageIO.read(imageStream);
			Screen screen = new Screen();
			Pattern scrollbarHandle = new Pattern(bufferedImage);
			Match match = screen.find(scrollbarHandle);

			if (match != null) {
				System.out.println("Scrollbar handle found!");

				Location startPoint = match.getCenter();

				pixelsToMove = region;
				Location endPoint = new Location(startPoint.getX() + pixelsToMove, startPoint.getY());

				screen.dragDrop(startPoint, endPoint);
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Swiped horizontally by " + pixelsToMove + " pixels.");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
//			log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Scrollbar handle not found on the screen " + exceptionAsString);
		}
		return nlpResponseModel;
	}
}