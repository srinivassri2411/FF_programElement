package specializedAutomation_utility;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OpenDebuggingBrowser implements Nlp {
	@InputParams({ @InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "File Path", type = "java.lang.String") })

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
//Srinivas M
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		try {
			String homePath = System.getProperty("user.home");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String url = (String) attributes.get("HubURL");
			String userDir = (String) attributes.get("File Path");
			String browserName = cap.getBrowserName().toLowerCase();
			log.info("BROWSERNAME" + browserName);
			userDir = homePath + File.separator + userDir;
			if (new File(userDir).exists() == true) {
				if (browserName.contains("chrome")) {
					ChromeOptions chromeOptions = new ChromeOptions();
					chromeOptions.addArguments("--user-data-dir=" + userDir);
					chromeOptions.addArguments("--remote-allow-origins=*");
					chromeOptions.addArguments("--start-maximized");
					cap.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("fire")) {
					log.info("FireFox Block");
					File file = new File(userDir);
					FirefoxOptions options = new FirefoxOptions();
					options.addArguments("--start-maximized");
					options.setProfile(new FirefoxProfile(file));
					cap.merge(options);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("Edge")) {
					log.info("Edge Block");
					EdgeOptions options = new EdgeOptions();
					options.addArguments("--user-data-dir=" + userDir);
					options.addArguments("--remote-allow-origins=*");
					options.addArguments("--start-maximized");
					cap.merge(options);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
				}
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Given file doesnot exists in the path " + userDir);
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Open Browser" + e);
		}

		return nlpResponseModel;
	}

}