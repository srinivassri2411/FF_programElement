
package business_Pacakge;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

public class PushFileToAndroid implements Nlp {
	@InputParams({ @InputParam(name = "File Stream", type = "java.io.InputStream"),
			@InputParam(name = "Android Path", type = "java.lang.String") })
	@ReturnType(name = "returnPath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		InputStream fileStream = (InputStream) programElementsInput.get("File Stream");
		String androidPath = (String) programElementsInput.get("Android Path");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = androidPath + "/driver_" + String.valueOf(Instant.now().toEpochMilli()) + ".png";
			nlpRequestModel.getAndroidDriver().pushFile(returnValue,
					Base64.getEncoder().encode(fileStream.readAllBytes()));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully pushed file into android device");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to push " + e.getMessage());
		}
		nlpResponseModel.getAttributes().put("returnPath", returnValue);
		return nlpResponseModel;
	}

}