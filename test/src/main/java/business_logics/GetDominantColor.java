
package business_logics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetDominantColor implements Nlp {
	@InputParams({ @InputParam(name = "Image File Path", type = "java.lang.nlp.filepath") })
	@ReturnType(name = "Dominant Color", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		byte[] imageFilePath = (byte[]) programElementsInput.get("Image File Path");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(imageFilePath);
			BufferedImage image = ImageIO.read(bais);
			Color dominantColor = getDominantForegroundColor(image);

			if (dominantColor != null) {

				nlpResponseModel.setMessage("Dominant Color is  : " + getBasicColorFromRGB(dominantColor.getRed(),
						dominantColor.getGreen(), dominantColor.getBlue()));
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("No dominant foreground color could be determined.");
				nlpResponseModel.setStatus(CommonConstants.pass);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Fetch Dominant color in the Image " + exceptionAsString);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Dominant Color", returnValue);
		return nlpResponseModel;
	}

	public static String getBasicColorFromRGB(int r, int g, int b) {
		float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
		float hue = hsb[0] * 360;
		float saturation = hsb[1];
		float brightness = hsb[2];

		if (brightness < 0.2)
			return "Black";
		if (brightness > 0.9 && saturation < 0.1)
			return "White";
		if (saturation < 0.25)
			return "Grey";
		if (hue >= 0 && hue < 15)
			return "Red";
		if (hue >= 15 && hue < 45)
			return "Orange";
		if (hue >= 45 && hue < 70)
			return "Yellow";
		if (hue >= 70 && hue < 170)
			return "Green";
		if (hue >= 170 && hue < 210)
			return "Cyan";
		if (hue >= 210 && hue < 260)
			return "Blue";
		if (hue >= 260 && hue < 290)
			return "Purple";
		if (hue >= 290 && hue < 330)
			return "Pink";
		if (hue >= 330 && hue <= 360)
			return "Red";
		return "Unknown";
	}

	private static int getNormalizedRgb(int rgb) {
		// A simple normalization example: quantize colors to a palette of 16x16x16.
		// This is optional but can be very effective.

		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;

		// Quantize each component.
		// Here, we divide by 16 and then multiply back.
		r = (r / 16) * 16;
		g = (g / 16) * 16;
		b = (b / 16) * 16;

		return (r << 16) | (g << 8) | b;
	}

	public static Color getDominantForegroundColor(BufferedImage image) {
		if (image == null) {
			return null;
		}

		// We'll use a threshold to separate "light" background from "dark/vibrant"
		// foreground.
		// A good heuristic is to check the brightness of the pixel.
		// Brightness is often calculated from the Y component in YIQ or YUV color
		// space,
		// or a simple average of R, G, B.
		int brightnessThreshold = 200; // Adjust this value as needed (0-255).

		// Use a map to count the occurrences of each color.
		// A simple RGB value as an Integer key is sufficient.
		Map<Integer, Integer> colorCounts = new HashMap<>();

		// Iterate through all the pixels of the image.
		int width = image.getWidth();
		int height = image.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y);
				Color pixelColor = new Color(rgb);

				// Calculate a simple brightness value.
				int brightness = (pixelColor.getRed() + pixelColor.getGreen() + pixelColor.getBlue()) / 3;

				// If the pixel is "darker" than the threshold, consider it part of the number.
				if (brightness < brightnessThreshold) {
					// Normalize the color to handle slight variations (e.g., anti-aliasing).
					// For example, you can round the R, G, B values to the nearest multiple of 10.
					// This is an advanced step, for now, we'll use the exact color.

					int normalizedRgb = getNormalizedRgb(rgb);

					colorCounts.put(normalizedRgb, colorCounts.getOrDefault(normalizedRgb, 0) + 1);
				}
			}
		}

		// Find the color with the highest count.
		Entry<Integer, Integer> dominantColorEntry = null;
		for (Entry<Integer, Integer> entry : colorCounts.entrySet()) {
			if (dominantColorEntry == null || entry.getValue() > dominantColorEntry.getValue()) {
				dominantColorEntry = entry;
			}
		}

		// Convert the dominant RGB integer back to a Color object.
		if (dominantColorEntry != null) {
			return new Color(dominantColorEntry.getKey());
		}

		return null;
	}

	public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(new File("C:\\Users\\User\\Downloads\\choice.PNG"));

		Color dominantColor = getDominantForegroundColor(image);

		if (dominantColor != null) {

			System.out.println("Dominant Color is  : "
					+ getBasicColorFromRGB(dominantColor.getRed(), dominantColor.getGreen(), dominantColor.getBlue()));
		} else {
			System.out.println("No dominant foreground color could be determined.");
		}
	}

}