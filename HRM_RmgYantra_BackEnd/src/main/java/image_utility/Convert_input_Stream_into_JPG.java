package image_utility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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




public class Convert_input_Stream_into_JPG implements Nlp {
	@InputParams({@InputParam(name = "Image Stream", type = "java.lang.InputStream"),@InputParam(name = "Image Name", type = "java.lang.String"),
		@InputParam(name = "Folder Path", type = "java.lang.String")})
	@ReturnType(name = "File Path", type = "java.lang.String")

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
		String imageImage = (String) attributes.get("Image Name");
		String folderPath = (String) attributes.get("Folder Path");
		InputStream inputStream =(InputStream) attributes.get("Image Stream");
		try {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			saveBufferedImageAsJpeg(bufferedImage, folderPath, imageImage);
			nlpResponseModel.setMessage("Image got saved sucessfully");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to save the image");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Folder Path",folderPath);
		return nlpResponseModel;
	}
	private static void saveBufferedImageAsJpeg(BufferedImage image, String outputFolder, String outputFileName) throws IOException {
        // Create the output folder if it doesn't exist
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Specify the output file path
        File outputFile = new File(folder, outputFileName);

        // Write the BufferedImage to the output file
        ImageIO.write(image, "jpg", outputFile);

        System.out.println("Image saved to: " + outputFile.getAbsolutePath());
    }
} 