package table;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.emulation.Emulation;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.bonigarcia.wdm.WebDriverManager;




public class OpenBrowserBasedOnResolution implements Nlp {
	@InputParams({ @InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "Size", type = "java.lang.String") })

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
		try {
			System.setProperty("webdriver.http.factory", "jdk-http-client");
//			WebDriver driver = null;
			String Size = (String) attributes.get("Size");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			Size = Size.replaceAll(" ", "");
			String[] res = Size.split("x");
			int width = Integer.parseInt(res[0] + "");
			int height = Integer.parseInt(res[1] + "");
			String browserName = cap.getBrowserName().toLowerCase();
			if (browserName.contains("chrome")) {
				Map<String, Object> deviceMetrics = new HashMap<>();
				deviceMetrics.put("width", width);
				deviceMetrics.put("height", height);
				deviceMetrics.put("pixelRatio", 3.0);
				Map<String, Object> mobileEmulation = new HashMap<>();
				mobileEmulation.put("deviceMetrics", deviceMetrics);
				mobileEmulation.put("userAgent",
						"Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
				Map<String, Object> clientHints = new HashMap<>();
				clientHints.put("platform", "Android");
				clientHints.put("mobile", true);
				mobileEmulation.put("clientHints", clientHints);
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
				cap.setAcceptInsecureCerts(true);
				cap.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
				WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
				driver.manage().window().maximize();
//				driver.navigate().to("https://www.fireflink.com/");
				nlpResponseModel.setWebDriver(driver);
			} else if (browserName.contains("fire")) {
				WebDriverManager.firefoxdriver().setup();
//				FirefoxOptions op = new FirefoxOptions();
//				op.setAcceptInsecureCerts(true);
				FirefoxDriver driver = new FirefoxDriver();
				driver.manage().window().maximize();
				DevTools devTools = ((FirefoxDriver) driver).getDevTools();
				devTools.createSession();
				devTools.send(Emulation.setDeviceMetricsOverride(height, width, 1, false, java.util.Optional.empty(),
						java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(),
						java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(),
						java.util.Optional.empty()));
				devTools.send(Emulation.setTouchEmulationEnabled(true, java.util.Optional.empty()));
				nlpResponseModel.setWebDriver(driver);
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser in given resolution");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Open Browser in given resolution" + e);
		}

		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {

		OpenBrowserBasedOnResolution fvgb = new OpenBrowserBasedOnResolution();

		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		DesiredCapabilities capa = new DesiredCapabilities();
		capa.setBrowserName("chrome");
//		attributes.put("browserName", "chrome");
		attributes.put("Size", "360 x 640");
		attributes.put("Capability", capa);
		fvgb.execute(nlpRequestModel);

	}
}
