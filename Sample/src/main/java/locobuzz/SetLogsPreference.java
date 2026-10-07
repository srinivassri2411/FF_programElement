package locobuzz;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP31eea40b-941f-4b12-a938-a5c268bc96db")
public class SetLogsPreference implements Nlp {
	@InputParams({
			@InputParam(name = "DesiredCapabilities_in", type = "org.openqa.selenium.remote.DesiredCapabilities") })
	@ReturnType(name = "DesiredCapabilities_out", type = "org.openqa.selenium.remote.DesiredCapabilities")

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
		DesiredCapabilities desiredCapabilities = (DesiredCapabilities) attributes.get("DesiredCapabilities_in");
		try {
			String browser = desiredCapabilities.getBrowserName().toLowerCase();
			switch (browser) {
			case "chrome":
				ChromeOptions options = new ChromeOptions();
				LoggingPreferences logPrefs = new LoggingPreferences();
				logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
				options.setCapability("goog:loggingPrefs", logPrefs);
				desiredCapabilities.merge(options);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully set the log preference");
				break;
			case "firefox":
				FirefoxOptions ffOptions = new FirefoxOptions();
				LoggingPreferences logPrefs1 = new LoggingPreferences();
				logPrefs1.enable(LogType.PERFORMANCE, Level.ALL);
				ffOptions.setCapability("goog:loggingPrefs", logPrefs1);
				desiredCapabilities.merge(ffOptions);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully set the log preference");
				break;
			case "microsoftedge":
				FirefoxOptions egOptions = new FirefoxOptions();
				LoggingPreferences logPrefs2 = new LoggingPreferences();
				logPrefs2.enable(LogType.PERFORMANCE, Level.ALL);
				egOptions.setCapability("goog:loggingPrefs", logPrefs2);
				desiredCapabilities.merge(egOptions);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully set the log preference");
				break;
			default:
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to set the log preference invalid browser");
			}

		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed " + writer);
		}

		nlpResponseModel.getAttributes().put("DesiredCapabilities_out", desiredCapabilities);
		return nlpResponseModel;
	}
}