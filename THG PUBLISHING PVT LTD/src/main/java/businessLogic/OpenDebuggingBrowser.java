package businessLogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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


public class OpenDebuggingBrowser implements Nlp {
	@InputParams({ @InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "Port", type = "java.lang.String") })

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
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String url = (String) attributes.get("HubURL");
			String port = (String) attributes.get("Port");
			String path = (String) attributes.get("File Path");
			String browserName = cap.getBrowserName().toLowerCase();
			// log.info("BROWSERNAME" + browserName);
			killProcess(Integer.parseInt(port));
			if (new File(path).exists() == true) {
				if (browserName.contains("chrome")) {
					ChromeOptions chromeOptions = new ChromeOptions();
					chromeOptions.addArguments("--remote-debugging-port=" + Integer.parseInt(port));
					chromeOptions.addArguments("--user-data-dir=" + path);
					chromeOptions.addArguments("--remote-allow-origins=*");
					chromeOptions.addArguments("--start-maximized");
					Map<String, Object> prefs = new HashMap<String, Object>();
					prefs.put("profile.exit_type", "Normal");
					chromeOptions.setExperimentalOption("prefs", prefs);
					cap.merge(chromeOptions);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("fire")) {
					// log.info("FireFox Block");
					FirefoxOptions options = new FirefoxOptions();
					options.addArguments("--start-maximized");
					options.setProfile(new FirefoxProfile(new File(path)));
					cap.merge(options);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("Edge")) {
					// log.info("Edge Block");
					EdgeOptions options = new EdgeOptions();
					options.addArguments("--user-data-dir=" + path);
					options.addArguments("--remote-debugging-port=" + Integer.parseInt(port));
					options.addArguments("--remote-allow-origins=*");
					options.addArguments("--start-maximized");
					Map<String, Object> prefs = new HashMap<String, Object>();
					prefs.put("profile.exit_type", "Normal");
					options.setExperimentalOption("prefs", prefs);
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
				nlpResponseModel.setMessage("Given file doesnot exists in the path " + path);
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Open Browser" + e);
		}
		return nlpResponseModel;
	}

	public static void killProcess(Integer port) throws IOException {
		String line;
		String command;
		String operatingSystem = System.getProperty("os.name");
		if (operatingSystem.contains("Windows")) {
			command = "netstat -ano";
		} else {
			command = "netstat -nlp | grep :" + port;
		}
		Process p = Runtime.getRuntime().exec(command);
		try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			while ((line = input.readLine()) != null) {
				if (line.contains(Integer.toString(port))) {
					String pid = null;
					if (operatingSystem.contains("Windows")) {
						if (line.contains(":" + port)) {
							String[] parts = line.trim().split("\\s+");
							pid = Integer.parseInt(parts[parts.length - 1]) + "";
						}
						System.out.println("**************id is " + pid);
					} else {
						pid = line.substring(line.indexOf("LISTEN") + 6, line.lastIndexOf("/")).replaceAll(" ", "");
						System.out.println("**************id is " + pid);
					}
					try {
						if (operatingSystem.contains("Windows")) {
							Runtime.getRuntime().exec("taskkill /F /PID " + pid);
							System.out.println();
						} else {
							Runtime.getRuntime().exec("kill " + pid);
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Failed to kill process with PID " + pid);
					}
				}
			}
		}
	}
}