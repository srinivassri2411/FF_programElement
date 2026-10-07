
package sonicWall;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;

public class GetStringFromLocalFilePath implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String") })
	@ReturnType(name = "returnString", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("File Path");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = FileUtils.readFileToString(new File(filePath), "UTF-8");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched the string from local file");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch the string from local file " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		nlpResponseModel.getAttributes().put("returnString", returnValue);
		return nlpResponseModel;
	}

}