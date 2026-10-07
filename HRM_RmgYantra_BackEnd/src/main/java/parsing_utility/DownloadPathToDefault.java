package parsing_utility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
 


public class DownloadPathToDefault implements Nlp {
	@InputParams({ @InputParam(name = "Desired Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities") })
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
		DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Desired Capabilities");
		try {
			DesiredCapabilities desiredCapabilities=new DesiredCapabilities();
			ChromeOptions options = new ChromeOptions();
			Path filePath = Path.of(System.getProperty("user.dir") );
		//	System.out.println(filePath);
			//options.addArguments("--download.default_directory="+filePath);
			Map<String, Object> prefs = new HashMap<String, Object>();
	        prefs.put("download.default_directory", filePath.toString());
	        options.setExperimentalOption("prefs", prefs);
			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
			cap.merge(desiredCapabilities);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Chrome Options Argument Added Successfully");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed To Add Arguments");
		}
		return nlpResponseModel;
	}
}