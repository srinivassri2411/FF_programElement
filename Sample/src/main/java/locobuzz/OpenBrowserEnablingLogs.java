package locobuzz;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
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
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;

import org.springframework.stereotype.Component;


@Slf4j
@Component("LIC11870_PJT1012_PE_NLP3007185c-b19e-462e-8f33-e31720859dec")
public class OpenBrowserEnablingLogs implements Nlp {
	@InputParams({ @InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities") })
	@ReturnType(name = "proxyData", type = "net.lightbody.bmp.BrowserMobProxy")

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
		BrowserMobProxy proxy = new BrowserMobProxyServer();
		try {
			String url = (String) attributes.get("HubURL");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String browserName = cap.getBrowserName();
			proxy.start(0);
			proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
			Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
			proxy.newHar("fireflink");
			if (browserName.contains("chrome")) {
				ChromeOptions op = new ChromeOptions();
				op.setProxy(seleniumProxy);
				op.setAcceptInsecureCerts(true);
				cap.merge(op);
				WebDriver driver = new RemoteWebDriver(new URL(url), cap);
				nlpResponseModel.setWebDriver(driver);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
			} else if (browserName.contains("fire")) {
				FirefoxOptions options = new FirefoxOptions();
				options.setProxy(seleniumProxy);
				options.setAcceptInsecureCerts(true);
				cap.merge(options);
				WebDriver driver = new RemoteWebDriver(new URL(url), cap);
				nlpResponseModel.setWebDriver(driver);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
			} else if (browserName.contains("Edge")) {
				log.info("Edge Block");
				EdgeOptions options = new EdgeOptions();
				options.setProxy(seleniumProxy);
				options.setAcceptInsecureCerts(true);
				cap.merge(options);
				WebDriver driver = new RemoteWebDriver(new URL(url), cap);
				nlpResponseModel.setWebDriver(driver);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
			}
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			log.error(exception.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launched " + exception.toString());
		}
		nlpResponseModel.getAttributes().put("proxyData", proxy);
		return nlpResponseModel;
	}
}