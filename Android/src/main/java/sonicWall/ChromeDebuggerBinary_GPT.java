package sonicWall;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
public class ChromeDebuggerBinary_GPT implements Nlp {
	@InputParams({ @InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "Binary Path", type = "java.lang.String"),
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
		String url = (String) attributes.get("HubURL");
		DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
		String binaryPath = (String) attributes.get("Binary Path");
		String port = (String) attributes.get("Port");
		int portNumber = Integer.parseInt(port);
		String browserName = cap.getBrowserName().toLowerCase();
		String osName = System.getProperty("os.name").toLowerCase();
		try {
			killProcess(portNumber);
			if (browserName.contains("chrome")) {
				if (osName.contains("mac")) {
					binaryPath = binaryPath + File.separator
							+ "Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing";
				} else if (osName.contains("win")) {
					binaryPath = binaryPath + File.separator + "chrome.exe";
				}

				log.info("Launching Chrome binary from: {}", binaryPath);
				launchDebugger(binaryPath, port);

				WebDriver driver = buildChromeDriver(cap, port, url);
				nlpResponseModel.setWebDriver(driver);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully launched Chrome browser");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Unsupported browser: " + browserName);
			}

		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			log.error("Exception occurred: {}", exception.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launch browser: " + exception.toString());
		}
		return nlpResponseModel;
	}

	private WebDriver buildChromeDriver(DesiredCapabilities cap, String port, String url) throws IOException {
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("debuggerAddress", "localhost:" + port);
		options.addArguments("--start-maximized");
		options.addArguments("--remote-allow-origins=*");
		cap.merge(options);
		return new RemoteWebDriver(new URL(url), cap);
	}

	public static void killProcess(Integer port) throws IOException {
		String os = System.getProperty("os.name").toLowerCase();
		String[] command = os.contains("win") ? new String[] { "cmd.exe", "/c", "netstat -ano" }
				: new String[] { "bash", "-c", "lsof -i :" + port };

		Process process = Runtime.getRuntime().exec(command);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String pid = null;
				if (os.contains("win") && line.contains(":" + port)) {
					String[] parts = line.trim().split("\s+");
					if (parts.length >= 5)
						pid = parts[parts.length - 1];
				} else if (line.contains("LISTEN") && !line.startsWith("COMMAND")) {
					String[] parts = line.trim().split("\s+");
					if (parts.length >= 2)
						pid = parts[1];
				}

				if (pid != null && pid.matches("\\d+")) {
					String[] killCommand = os.contains("win")
							? new String[] { "cmd.exe", "/c", "taskkill /F /PID " + pid }
							: new String[] { "bash", "-c", "kill -9 " + pid };
					Runtime.getRuntime().exec(killCommand);
				}
			}
		}
	}

	public static void launchDebugger(String chromeExecutable, String port) throws IOException {
		String[] command = { chromeExecutable, "--remote-debugging-port=" + port };
		Runtime.getRuntime().exec(command);
	}
}