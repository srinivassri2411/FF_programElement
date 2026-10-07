package package1;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class RemoteMobileBrowserLauncher implements Nlp {

	@InputParams({ @InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "DeviceName", type = "java.lang.String") })
	@Override
	public List<String> getTestParameters() throws NlpException {
		return new ArrayList<>();
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		return new StringBuilder();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel response = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		try {
			String hubUrl = (String) attributes.get("HubURL");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String deviceName = (String) attributes.get("DeviceName");

			String browser = cap.getBrowserName().toLowerCase();
			WebDriver driver;

			if (browser.contains("chrome")) {

				ChromeOptions options = new ChromeOptions();

				Map<String, Object> mobileEmulation = new HashMap<>();
				mobileEmulation.put("deviceName", deviceName);
				options.setExperimentalOption("mobileEmulation", mobileEmulation);

				cap.merge(options);
				driver = new RemoteWebDriver(new URL(hubUrl), cap);
			}

			else if (browser.contains("edge")) {

				EdgeOptions options = new EdgeOptions();

				Map<String, Object> mobileEmulation = new HashMap<>();
				mobileEmulation.put("deviceName", deviceName);
				options.setExperimentalOption("mobileEmulation", mobileEmulation);

				cap.merge(options);
				driver = new RemoteWebDriver(new URL(hubUrl), cap);
			} else if (browser.contains("firefox")) {

				FirefoxOptions options = new FirefoxOptions();
				cap.merge(options);

				driver = new RemoteWebDriver(new URL(hubUrl), cap);

				driver.manage().window().setSize(new Dimension(375, 812));
			}

			else {
				response.setStatus(CommonConstants.fail);
				response.setMessage("Browser not supported: " + browser);
				return response;
			}

			response.setWebDriver(driver);
			response.setStatus(CommonConstants.pass);
			response.setMessage("Remote " + browser + " launched in mobile mode");

		} catch (Exception e) {
			response.setStatus(CommonConstants.fail);
			response.setMessage("Failed to launch remote browser: " + e.getMessage());
		}

		return response;
	}

	public static void main(String[] args) throws Exception {

		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
		ChromeOptions op = new ChromeOptions();

		Map<String, Object> mobileEmulation = new HashMap<>();
		mobileEmulation.put("deviceName", "Pixel 7");
		op.setExperimentalOption("mobileEmulation", mobileEmulation);
//		op.addArguments("--force-device-scale-factor=0.75");
		cap.merge(op);
		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
//		NlpRequestModel req = new NlpRequestModel();
//		req.getAttributes().put("Capability", cap);
//		req.getAttributes().put("DeviceName", "Pixel 7");
//		req.getAttributes().put("HubURL", "http://localhost:4444/wd/hub");
//		NlpResponseModel exce = new RemoteMobileBrowserLauncher().execute(req);
//		System.out.println(exce);
//		WebDriver driver = exce.getWebDriver();

		driver.get("https://staging.covermagix.com/product/custom-outdoor-dining-table-covers-6-dimensions/");
	}
}
