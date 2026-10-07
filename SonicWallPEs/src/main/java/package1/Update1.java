package package1;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
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

public class Update1 implements Nlp {

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

				MobileDeviceProfile profile = MobileDeviceProfile.getProfile(deviceName);

				Map<String, Object> deviceMetrics = new HashMap<>();
				deviceMetrics.put("width", profile.width);
				deviceMetrics.put("height", profile.height);
				deviceMetrics.put("pixelRatio", profile.pixelRatio);

				Map<String, Object> mobileEmulation = new HashMap<>();
				mobileEmulation.put("deviceMetrics", deviceMetrics);
				mobileEmulation.put("userAgent", profile.userAgent);

				ChromeOptions options = new ChromeOptions();
				options.setExperimentalOption("mobileEmulation", mobileEmulation);

				cap.merge(options);
				driver = new RemoteWebDriver(new URL(hubUrl), cap);
			}

			else if (browser.contains("edge")) {

				MobileDeviceProfile profile = MobileDeviceProfile.getProfile(deviceName);

				Map<String, Object> deviceMetrics = new HashMap<>();
				deviceMetrics.put("width", profile.width);
				deviceMetrics.put("height", profile.height);
				deviceMetrics.put("pixelRatio", profile.pixelRatio);

				Map<String, Object> mobileEmulation = new HashMap<>();
				mobileEmulation.put("deviceMetrics", deviceMetrics);
				mobileEmulation.put("userAgent", profile.userAgent);

				EdgeOptions options = new EdgeOptions();
				options.setExperimentalOption("mobileEmulation", mobileEmulation);

				cap.merge(options);
				driver = new RemoteWebDriver(new URL(hubUrl), cap);
			}

			else if (browser.contains("firefox")) {

				FirefoxOptions options = new FirefoxOptions();
				cap.merge(options);

				driver = new RemoteWebDriver(new URL(hubUrl), cap);
				driver.manage().window().setSize(new Dimension(375, 812));
			}

			else {
				response.setStatus(CommonConstants.fail);
				response.setMessage("Unsupported browser: " + browser);
				return response;
			}

			verifyViewport(driver, deviceName);

			response.setWebDriver(driver);
			response.setStatus(CommonConstants.pass);
			response.setMessage("Remote " + browser + " launched in mobile mode (" + deviceName + ")");

		} catch (Exception e) {
			response.setStatus(CommonConstants.fail);
			response.setMessage("Failed to launch mobile browser: " + e.getMessage());
		}

		return response;
	}

	private void verifyViewport(WebDriver driver, String deviceName) {

		Map<?, ?> viewport = (Map<?, ?>) ((JavascriptExecutor) driver).executeScript("return {"
				+ "width: window.innerWidth," + "height: window.innerHeight," + "dpr: window.devicePixelRatio" + "};");

		System.out.println("====================================");
		System.out.println("📱 Device  : " + deviceName);
		System.out.println("📐 Viewport: " + viewport);
		System.out.println("====================================");
	}

	// 🔥 Standalone run (for local testing)
	public static void main(String[] args) throws Exception {

		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
		ChromeOptions op = new ChromeOptions();
		op.addArguments("--force-device-scale-factor=0.75");
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("Capability", cap);
		req.getAttributes().put("DeviceName", "Pixel 7");
		req.getAttributes().put("HubURL", "http://localhost:4444/wd/hub");

		NlpResponseModel resp = new Update1().execute(req);
		WebDriver driver = resp.getWebDriver();

		driver.get("https://staging.covermagix.com/product/custom-outdoor-dining-table-covers-6-dimensions/");
	}
}
