package Business_Logic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class Provide_Capabilities implements Nlp {
	@InputParams({ @InputParam(name = "Desired Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities")})
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
		InputStream	inputStream	=(InputStream)attributes.get("File Stream");
		try {
			DesiredCapabilities dc = new DesiredCapabilities();

			if (cap.getBrowserName().contains("chrome")) {
				
				HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
				chromePrefs.put("autofill.profile_enabled", false);
				
				ChromeOptions options = new ChromeOptions();
				LoggingPreferences logPrefs = new LoggingPreferences();
				options.addArguments("--remote-allow-origins=*");
			
				options.setCapability("goog:loggingPrefs", logPrefs);
		        
				options.addArguments("--disable-autofill-addresses");
				options.addArguments("--max_old_space_size=6796"); 
				options.addArguments("--disable-images");
				options.addArguments("--disable-gpu");
				options.addArguments("--disable-extensions");
				options.addArguments("--start-maximized");
				options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
				options.setExperimentalOption("prefs", chromePrefs);
				dc.setCapability(ChromeOptions.CAPABILITY, options);
			  
				cap.merge(dc);
			} else if (cap.getBrowserName().contains("dge")) {
				EdgeOptions options = new EdgeOptions();
				LoggingPreferences logPrefs = new LoggingPreferences();
				options.addArguments("--remote-allow-origins=*");
	
				options.setCapability("goog:loggingPrefs", logPrefs);
				options.addArguments("--disable-autofill-addresses");
		       options.addArguments("--disable-autofill-profile");
		        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
				cap.merge(options);
				
			} 
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Success");



		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed To Add Arguments");
		}
		return nlpResponseModel;
	}
}