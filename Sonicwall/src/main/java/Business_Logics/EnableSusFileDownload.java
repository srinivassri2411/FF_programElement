
package Business_Logics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class EnableSusFileDownload implements Nlp {
	@ReturnType(name = "returnMap", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> prefs = null;
		try {
			prefs = new HashMap<>();
			prefs.put("download.default_directory", System.getProperty("user.home") + File.separator + "Downloads");
			prefs.put("download.prompt_for_download", false);
			prefs.put("download.directory_upgrade", true);
			prefs.put("safebrowsing.enabled", true);
			prefs.put("safebrowsing.disable_download_protection", true);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Enabled File download");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to enable File download");
		}
		nlpResponseModel.getAttributes().put("returnOptions", prefs);
		return nlpResponseModel;
	}

}