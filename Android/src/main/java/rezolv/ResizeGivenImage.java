
package rezolv;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class ResizeGivenImage implements Nlp {
	@InputParams({ @InputParam(name = "Reference Image", type = "java.lang.String"),
			@InputParam(name = "Image to resize", type = "java.lang.String"),
			@InputParam(name = "Output path", type = "java.lang.String") })
	@ReturnType(name = "createdFile", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String referenceImage = (String) programElementsInput.get("Reference Image");
		String imagetoresize = (String) programElementsInput.get("Image to resize");
		String outputpath = (String) programElementsInput.get("Output path");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = resizeToMatch(referenceImage, imagetoresize, outputpath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Resized the given image");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Resize the given image");

		}

		nlpResponseModel.getAttributes().put("createdFile", returnValue);
		return nlpResponseModel;
	}

	public static String resizeToMatch(String referenceImagePath, String sourceImagePath, String outputFolderPath)
			throws Exception {
		BufferedImage referenceImage = ImageIO.read(new File(referenceImagePath));
		int targetWidth = referenceImage.getWidth();
		int targetHeight = referenceImage.getHeight();

		File sourceFile = new File(sourceImagePath);
		BufferedImage sourceImage = ImageIO.read(sourceFile);

		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.drawImage(sourceImage.getScaledInstance(targetWidth, targetHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null);
		g2d.dispose();

		String baseName = sourceFile.getName();
		int dotIndex = baseName.lastIndexOf('.');
		if (dotIndex > 0) {
			baseName = baseName.substring(0, dotIndex);
		}

		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String outputFileName = baseName + "_" + timestamp + ".png";

		File outputFolder = new File(outputFolderPath);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		File outputFile = new File(outputFolder, outputFileName);
		ImageIO.write(resizedImage, "png", outputFile);

		return outputFile.getAbsolutePath();
	}
}