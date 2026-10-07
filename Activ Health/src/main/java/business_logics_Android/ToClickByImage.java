package business_logics_Android;

import java.awt.image.BufferedImage;


import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
public class ToClickByImage implements Nlp {
	@InputParams({ @InputParam(name = "mainImagePath", type = "java.lang.String"),
			@InputParam(name = "subImagePath", type = "java.lang.String") })
	@ReturnType(name = "ElementCoordinates", type = "java.lang.String")
	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}
	@Override
	public StringBuilder getTestCode() throws NlpException  {
		StringBuilder sb = new StringBuilder();
		return sb;
	}
	public static List cooredinates(BufferedImage mainImage, BufferedImage subImage) {
		Pattern searchImage = new Pattern(mainImage).similar((float) 0.9);
		// String ScreenImage = subImage; // In this case, the image you want to search
		Finder objFinder = null;
		Match objMatch = null;
		objFinder = new Finder(subImage);
		objFinder.find(searchImage);
		int x = 0;
		int y = 0;
		List s1 = new ArrayList();
		int counter = 0;
		while (objFinder.hasNext()) {
			objMatch = objFinder.next(); // objMatch gives you the matching region.
			x = objMatch.getX();
			y = objMatch.getY();
			int w = objMatch.getW();
			int h = objMatch.getH();
			System.out.println("Match found at: " + x + ", " + y + ", " + (x + w) + ", " + (y + h));
			counter++;
		}
		if (counter != 0) {
			s1.add(x);
			s1.add(y);
			System.out.println("Match Found!");
		}
		return s1;
	}
	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String subImagePath = (String) attributes.get("subImagePath");
		String mainImagePath = (String) attributes.get("mainImagePath");
		String s = null;
		try {
			AndroidDriver driver = nlpRequestModel.getAndroidDriver();
			BufferedImage mainImage = ImageIO.read(new File(mainImagePath));
			BufferedImage subImage = ImageIO.read(new File(subImagePath));
			List s1 = cooredinates(mainImage, subImage);
			int xCoordinate = (int) s1.get(0) + (subImage.getWidth() / 2);
			int yCoordinate = (int) s1.get(1) + (subImage.getHeight() / 2);
			if (s1.size() > 0) {
				s = (xCoordinate * driver.manage().window().getSize().getWidth()) / mainImage.getWidth() + ","
						+ (yCoordinate * driver.manage().window().getSize().getHeight()) / mainImage.getHeight() + "";
				int xVal = (xCoordinate * driver.manage().window().getSize().getWidth()) / mainImage.getWidth();
				int yVal = (yCoordinate * driver.manage().window().getSize().getHeight()) / mainImage.getHeight();
				PointerInput finger = new PointerInput(PointerInput.Kind.MOUSE, "finger");
				Sequence tapSequence = new Sequence(finger, 0);
				tapSequence.addAction(
						finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), xVal, yVal));
				tapSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
				tapSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
				driver.perform(Arrays.asList(tapSequence));
				nlpResponseModel.setMessage("Successfully clicked on Given Image");
				s = (xCoordinate * driver.manage().window().getSize().getWidth()) / mainImage.getWidth() + ","
						+ (yCoordinate * driver.manage().window().getSize().getHeight()) / mainImage.getHeight() + "";
				nlpResponseModel.setStatus(CommonConstants.pass);
			}
		} catch (Exception e) {
			// log.error("Exception is: ", e);
			nlpResponseModel.setMessage("Sub-image not found in the main image exception" + e.getMessage());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("ElementCoordinates", s);
		return  nlpResponseModel;
	}
}