
package business_Pacakge;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class PushFileToDevice implements Nlp {
	@InputParams({ @InputParam(name = "Input Stream", type = "java.io.InputStream"),
			@InputParam(name = "Device Path", type = "java.lang.String") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		InputStream inputStream = (InputStream) programElementsInput.get("Input Stream");
		String devicePath = (String) programElementsInput.get("Device Path");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			nlpRequestModel.getAndroidDriver().pushFile(devicePath,
					Base64.getEncoder().encode(inputStream.readAllBytes()));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully pushed file into device");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to push" + e);
		}
		// Your program element business logic ends here ...
		return nlpResponseModel;
	}
}
