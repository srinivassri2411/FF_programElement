 package integrationLogics;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Proxy;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;

//@Slf4j

public class OpenBrowserEnablingLogs implements Nlp {
	@InputParams({ @InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities") })
	@ReturnType(name = "proxyData", type = "net.lightbody.bmp.BrowserMobProxy")

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
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		BrowserMobProxy proxy = new BrowserMobProxyServer();

		try {
			String url = (String) attributes.get("HubURL");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String browserName = cap.getBrowserName().toLowerCase();

			proxy.start(0);
			String proxyAddress = "localhost:" + proxy.getPort();

			Proxy seleniumProxy = new Proxy();
			seleniumProxy.setHttpProxy(proxyAddress);
			seleniumProxy.setSslProxy(proxyAddress);

			proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_CONTENT,
					CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_CONTENT);
			proxy.newHar("Test_" + System.currentTimeMillis());

			WebDriver driver;

			if (browserName.contains("chrome")) {
				Map<String, Object> prefs = new HashMap<>();
				prefs.put("download.default_directory", System.getProperty("user.home") + File.separator + "Downloads");
				prefs.put("download.prompt_for_download", false);
				prefs.put("download.directory_upgrade", true);
				prefs.put("safebrowsing.enabled", true);
				prefs.put("safebrowsing.disable_download_protection", true);

				ChromeOptions op = new ChromeOptions();
				op.setProxy(seleniumProxy);
				op.addArguments("--ignore-certificate-errors");
				op.addArguments("--proxy-server=http://" + proxyAddress); // enforce proxy
				op.setExperimentalOption("prefs", prefs);
				cap.merge(op);

				driver = new RemoteWebDriver(new URL(url), cap);

			} else if (browserName.contains("fire")) {
				FirefoxOptions options = new FirefoxOptions();
				options.setProxy(seleniumProxy);
				options.addArguments("--ignore-certificate-errors");
				cap.merge(options);

				driver = new RemoteWebDriver(new URL(url), cap);

			} else if (browserName.contains("edge")) {
				Map<String, Object> prefs = new HashMap<>();
				prefs.put("download.default_directory", System.getProperty("user.home") + File.separator + "Downloads");
				prefs.put("download.prompt_for_download", false);
				prefs.put("download.directory_upgrade", true);
				prefs.put("safebrowsing.enabled", true);
				prefs.put("safebrowsing.disable_download_protection", true);

				EdgeOptions options = new EdgeOptions();
				options.setProxy(seleniumProxy);
				options.addArguments("--ignore-certificate-errors");
				options.addArguments("--proxy-server=http://" + proxyAddress);
				options.setExperimentalOption("prefs", prefs);
				cap.merge(options);

				driver = new RemoteWebDriver(new URL(url), cap);

			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Not Supported Browser: " + browserName);
				nlpResponseModel.getAttributes().put("proxyData", proxy);
				return nlpResponseModel;
			}

			nlpResponseModel.setWebDriver(driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");

		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launch Browser: " + exception);
		}

		nlpResponseModel.getAttributes().put("proxyData", proxy);
		return nlpResponseModel;
	}
}