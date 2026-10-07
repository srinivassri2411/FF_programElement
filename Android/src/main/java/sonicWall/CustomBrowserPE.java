
package sonicWall;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

@Slf4j
public class CustomBrowserPE implements Nlp {
	@InputParams({ @InputParam(name = "Capability", type = "java.lang.Object"),
			@InputParam(name = "HubUrl", type = "java.lang.String"),
			@InputParam(name = "Binary Path", type = "java.lang.String"),
			@InputParam(name = "Port", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		DesiredCapabilities capability = (DesiredCapabilities) programElementsInput.get("Capability");
		String hubUrl = (String) programElementsInput.get("HubUrl");
		String binaryPath = (String) programElementsInput.get("Binary Path");
		String port = (String) programElementsInput.get("Port");
		int portNumber = Integer.parseInt(port);
		String browserName = capability.getBrowserName().toLowerCase();
		String osName = System.getProperty("os.name").toLowerCase();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
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

				WebDriver driver = buildChromeDriver(capability, port, hubUrl);
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