package business_logics_Android;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
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

public class GetColour_FromImage implements Nlp {
	@InputParams({ @InputParam(name = "Image Path", type = "java.lang.String") })
	@ReturnType(name = "Color", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String imgPath = (String) programElementsInput.get("Image Path");
		String result = "";
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			result = isColorPresentInElement(imgPath);
			if (result.contains("false")) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Color not found");
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Color is found");
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Check" + e);
		}
		nlpResponseModel.getAttributes().put("Color", result);
		return nlpResponseModel;
	}

	public static String isColorPresentInElement(String imgPath) throws IOException {

		BufferedImage image = ImageIO.read(new File(imgPath));

		int width = image.getWidth();
		int height = image.getHeight();
		int step = 3;

		for (int x = 0; x < width; x += step) {
			for (int y = 0; y < height; y += step) {
				int clr = image.getRGB(x, y);
				int r = (clr >> 16) & 0xFF;
				int g = (clr >> 8) & 0xFF;
				int b = clr & 0xFF;

				return "(" + r + "," + g + "," + b + ")";
			}
		}
		return "false";
	}

//	public static String getBasicColorFromRGB(int r, int g, int b) {
//		float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
//		float hue = hsb[0] * 360;
//		float saturation = hsb[1];
//		float brightness = hsb[2];
//
//		if (brightness < 0.2)
//			return "Black";
//		if (brightness > 0.9 && saturation < 0.1)
//			return "White";
//		if (saturation < 0.25)
//			return "Grey";
//		if (hue >= 0 && hue < 15)
//			return "Red";
//		if (hue >= 15 && hue < 45)
//			return "Orange";
//		if (hue >= 45 && hue < 70)
//			return "Yellow";
//		if (hue >= 70 && hue < 170)
//			return "Green";
//		if (hue >= 170 && hue < 210)
//			return "Cyan";
//		if (hue >= 210 && hue < 260)
//			return "Blue";
//		if (hue >= 260 && hue < 290)
//			return "Purple";
//		if (hue >= 290 && hue < 330)
//			return "Pink";
//		if (hue >= 330 && hue <= 360)
//			return "Red";
//		return "Unknown";
//	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Image Path", "C:\\Users\\User\\Desktop\\Capture.PNG");
		System.out.println(new GetColour_FromImage().execute(model));
	}
}