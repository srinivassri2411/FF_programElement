package specializedAutomation_Utility;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
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
public class OpenBrowserWithMockedCamera implements Nlp {
	@InputParams({ @InputParam(name = "WebHub Url", type = "java.lang.String"),
			@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
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
			System.setProperty("webdriver.http.factory", "jdk-http-client");
			String file = (String) attributes.get("File Path");
			String url = (String) attributes.get("WebHub Url");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String browserName = cap.getBrowserName().toLowerCase();
		//	log.info("BROWSERNAME" + browserName);
			if (browserName.contains("chrome")) {
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--remote-allow-origins=*");
				options.addArguments("--allow-file-access-from-files", "--use-fake-ui-for-media-stream",
						"--allow-file-access");
				options.addArguments("--use-fake-device-for-media-stream");
				options.addArguments("--use-file-for-fake-video-capture=" + file);
				cap.setBrowserName("Chrome");
				cap.merge(options);
				WebDriver driver = new RemoteWebDriver(new URL(url), cap);
				nlpResponseModel.setWebDriver(driver);
			}
//			else if (browserName.contains("fire")) {
//				log.info("FireFox Block");
//				WebDriverManager.firefoxdriver().setup();
//				FirefoxProfile profile = new FirefoxProfile();
//				profile.setPreference("media.navigator.permission.disabled", true);
//				profile.setPreference("media.navigator.streams.fake", true);
//				profile.setPreference("media.navigator.streams.fake.video", file);
//				FirefoxOptions options = new FirefoxOptions();
//				options.setProfile(profile);
//				WebDriver driver = new FirefoxDriver(options);
//				nlpResponseModel.setWebDriver(driver);
//			} 
			else if (browserName.contains("edge")) {
			//	log.info("Edge Block");
				EdgeOptions options = new EdgeOptions();
				options.addArguments("--remote-allow-origins=*");
				options.addArguments("--allow-file-access-from-files", "--use-fake-ui-for-media-stream",
						"--allow-file-access");
				options.addArguments("--use-fake-device-for-media-stream");
				options.addArguments("--use-file-for-fake-video-capture=" + file);
				cap.merge(options);
				WebDriver driver = new RemoteWebDriver(new URL(url), cap);
				nlpResponseModel.setWebDriver(driver);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Opened " + browserName);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Open Browser " + e);
		}

		return nlpResponseModel;
	}

}