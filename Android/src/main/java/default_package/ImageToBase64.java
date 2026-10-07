
package default_package;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ImageToBase64 implements Nlp {
	@InputParams({ @InputParam(name = "FilePath", type = "java.lang.String") })
	@ReturnType(name = "base64Value", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("FilePath");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = encodeImageToBase64(filePath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Converted successfully to base64");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to convert");

		}
		nlpResponseModel.getAttributes().put("base64Value", returnValue);
		return nlpResponseModel;
	}

	public static String encodeImageToBase64(String filePath) throws IOException {
		File file = new File(filePath);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		fileInputStream.read(bytes);
		fileInputStream.close();
		return Base64.getEncoder().encodeToString(bytes);
	}
}