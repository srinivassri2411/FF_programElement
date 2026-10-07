package netCoreLogics;

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
import java.util.Scanner;

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


import org.springframework.stereotype.Component;

@Slf4j
@Component("LIC11870_PJT1012_PE_NLP9c21cff4-5488-4e89-8eca-8daf45c2cfef")
public class ChromeDebugger implements Nlp {
	@InputParams({ @InputParam(name = "HubURL", type = "java.lang.String"),
			@InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "Profile Path", type = "java.lang.String"),
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
			String url = (String) attributes.get("HubURL");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String path = (String) attributes.get("Profile Path");
			String port = (String) attributes.get("Port");
			int portNumber = Integer.parseInt(port);
			String browserName = cap.getBrowserName();
			String osName = System.getProperty("os.name");
			if (osName.contains("Windows")) {
				killProcess(portNumber);
				if (browserName.contains("chrome")) {
					Process process = Runtime.getRuntime().exec(
							"reg query \"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe\" /ve");
					Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A");
					String output = scanner.hasNext() ? scanner.next() : "";
					int startIndex = output.indexOf("REG_SZ") + "REG_SZ".length();
					String chromePath = null;
					try {
						chromePath = output.substring(startIndex).trim();
					} catch (IndexOutOfBoundsException e) {
						chromePath = System.getProperty("user.home") + File.separator + "AppData" + File.separator
								+ "Local" + File.separator + "Google" + File.separator + "Chrome" + File.separator
								+ "Application" + File.separator + "chrome.exe";
					}
					launchDebugger(chromePath, path, port);
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
					cap.merge(op);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("fire")) {
					FirefoxOptions options = new FirefoxOptions();
					options.addArguments("--start-maximized");
					options.setProfile(new FirefoxProfile(new File(path)));
					cap.merge(options);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("Edge")) {
					log.info("Edge Block");
					EdgeOptions options = new EdgeOptions();
					options.addArguments("--user-data-dir=" + path);
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
			} else if (osName.contains("nux")) {
				killProcess(portNumber);
				if (browserName.contains("chrome")) {
					Process process = Runtime.getRuntime().exec("which google-chrome");
					Scanner scanner = new Scanner(process.getInputStream());
					String output = scanner.hasNext() ? scanner.next() : "";
					launchDebugger(output, path, port);
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
					cap.merge(op);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("fire")) {
					FirefoxOptions options = new FirefoxOptions();
					options.addArguments("--start-maximized");
					options.setProfile(new FirefoxProfile(new File(path)));
					cap.merge(options);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else if (browserName.contains("Edge")) {
					log.info("Edge Block");
					EdgeOptions options = new EdgeOptions();
					options.addArguments("--user-data-dir=" + path);
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
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Browser is Launched Successfully");
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			log.error(exception.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launched " + exception.toString());
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

	public static void launchDebugger(String chromeExecutable, String userDataDir, String remoteDebuggingPort) {
		String[] command = { chromeExecutable, " -remote-debugging-port=" + remoteDebuggingPort, "--no-first-run",
				"--no-default-browser-check", "--user-data-dir=" + userDataDir };
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}