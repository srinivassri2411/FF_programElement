package netCoreLogics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP8182ff87-6c3d-4f3e-80b8-13996e3d37b9")
public class GetImageFromBase64 implements Nlp {
	@InputParams({ @InputParam(name = "Base64 String", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String base64String = (String) programElementsInput.get("Base64 String");
		String imgPath = null;
		try {
			Path filePath = decodeBase64ToFile(base64String);
			System.out.println("Image saved at: " + filePath.toAbsolutePath());
			imgPath = filePath.toAbsolutePath().toString();
			nlpResponseModel.setMessage("Successfully fetched the image");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Fail to fetch " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", imgPath);
		return nlpResponseModel;
	}

	public static Path decodeBase64ToFile(String base64String) throws IOException {
		String[] parts = base64String.split(",");
		String imageDataBytes = parts[1];
		String mimeType = parts[0].split(":")[1].split(";")[0];
		String fileExtension = getFileExtension(mimeType);

		if (fileExtension == null) {
			throw new IllegalArgumentException("Unsupported image type: " + mimeType);
		}
		byte[] imageBytes = Base64.decodeBase64(imageDataBytes);
		Path tempFile = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")),
				UUID.randomUUID().toString(), fileExtension);
		Files.write(tempFile, imageBytes);
		return tempFile;
	}

	private static String getFileExtension(String mimeType) {
		switch (mimeType) {
		case "image/jpeg":
			return ".jpg";
		case "image/png":
			return ".png";
		case "image/gif":
			return ".gif";
		default:
			return null;
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
}