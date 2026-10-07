
package browser_capability;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LaunchChromeWithProfile implements Nlp {
	@InputParams({@InputParam(name = "Profile Path", type = "java.lang.String"),
		@InputParam(name = "Hub URL", type = "java.lang.String")})


	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String profilePath = (String) programElementsInput.get("Profile Path");
		String hubURL = (String) programElementsInput.get("Hub URL");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			// Your program element business logic starts here ...
			ChromeOptions options = new ChromeOptions();
			LoggingPreferences logPrefs = new LoggingPreferences();
			logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
			logPrefs.enable(LogType.BROWSER, Level.ALL);  // Capture browser console logs
			options.setCapability("goog:loggingPrefs", logPrefs);
			options.addArguments("--disable-notifications");
			options.addArguments("--user-data-dir="+profilePath);

			// Initialize RemoteWebDriver
			WebDriver driver = new RemoteWebDriver(new URI(hubURL).toURL(), options);
			nlpResponseModel.setWebDriver(driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
		}
		catch(Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			throw new NlpException(e.getMessage());

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

} 