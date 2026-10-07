package netCoreLogics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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


import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP8be60c8e-e019-4d93-9174-991849927957")
public class RotateImage implements Nlp {
	@InputParams({ @InputParam(name = "Image Path", type = "java.lang.String"),
			@InputParam(name = "isClockwise", type = "java.lang.Boolean"),
			@InputParam(name = "Angle", type = "java.lang.Double") })
	@ReturnType(name = "Return", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String imgPath = (String) programElementsInput.get("Image Path");
		Boolean isClockwise = (Boolean) programElementsInput.get("isClockwise");
		Double angle = (Double) programElementsInput.get("Angle");
		try {
			rotate(imgPath, angle.doubleValue(), isClockwise.booleanValue());
			nlpResponseModel.setMessage("Successfully rotated the given image");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Fail to rotate" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", imgPath);
		return nlpResponseModel;
	}

	public void rotate(String originalImage, double angle, Boolean isClockwise) throws Exception {
		BufferedImage image = ImageIO.read(new File(originalImage));
		if (isClockwise) {
			int width = image.getWidth();
			int height = image.getHeight();

			// Calculate the new dimensions of the image after rotation
			double radians = Math.toRadians(angle);
			double sin = Math.abs(Math.sin(radians));
			double cos = Math.abs(Math.cos(radians));
			int newWidth = (int) Math.floor(width * cos + height * sin);
			int newHeight = (int) Math.floor(height * cos + width * sin);

			BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
			Graphics2D g2d = rotatedImage.createGraphics();
			AffineTransform transform = new AffineTransform();
			transform.translate((newWidth - width) / 2, (newHeight - height) / 2);
			transform.rotate(radians, width / 2, height / 2);
			g2d.drawImage(image, transform, null);
			g2d.dispose();
			ImageIO.write(rotatedImage, "png", new File(originalImage));
			System.out.println("+90 rotated");
		} else if (isClockwise == false) {
			int width = image.getWidth();
			int height = image.getHeight();

			// Calculate the new dimensions of the image after rotation
			double radians = Math.toRadians(-angle);
			double sin = Math.abs(Math.sin(radians));
			double cos = Math.abs(Math.cos(radians));
			int newWidth = (int) Math.floor(width * cos + height * sin);
			int newHeight = (int) Math.floor(height * cos + width * sin);

			BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
			Graphics2D g2d = rotatedImage.createGraphics();
			AffineTransform transform = new AffineTransform();
			transform.translate((newWidth - width) / 2, (newHeight - height) / 2);
			transform.rotate(radians, width / 2, height / 2);
			g2d.drawImage(image, transform, null);
			g2d.dispose();
			ImageIO.write(rotatedImage, "png", new File(originalImage));
			System.out.println("-90 rotated");
		} else {
			throw new Exception("Not given data");
		}
	}

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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("CSV1 Path", "C:\\Users\\Srinivas m\\Downloads\\cust_data_sms_658.csv");
		req.getAttributes().put("CSV2 Path", "C:\\Users\\Srinivas m\\Downloads\\cust_data_sms_658_1.csv");
		System.out.println(new RotateImage().execute(req));
	}
}