
package Business_Logics;

import com.google.common.collect.ImmutableMap;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class DeleteFolderFromAndroid implements Nlp {
	@InputParams({ @InputParam(name = "Folder Path", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String folderPath = (String) programElementsInput.get("Folder Path");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			nlpRequestModel.getAndroidDriver().executeScript("mobile: shell",
					ImmutableMap.of("command", "rm -r " + folderPath, "args", ""));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully deleted folder");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to delete folder");
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		return nlpResponseModel;
	}

}