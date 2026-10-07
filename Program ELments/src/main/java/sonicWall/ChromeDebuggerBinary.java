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
import org.openqa.selenium.chrome.ChromeDriver;
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

public class ChromeDebuggerBinary implements Nlp {
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
		try {
			System.setProperty("webdriver.chrome.driver", "C:\\Users\\User\\Downloads\\chrome-win64\\chromedriver.exe");
			String url = (String) attributes.get("HubURL");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String binaryPath = (String) attributes.get("Binary Path");
			String port = (String) attributes.get("Port");
			int portNumber = Integer.parseInt(port);
			String browserName = cap.getBrowserName();
			String osName = System.getProperty("os.name");
			if (osName.contains("Windows")) {
				killProcess(portNumber);
				if (browserName.contains("chrome")) {
					binaryPath = binaryPath + File.separator + "chrome.exe";
					launchDebugger(binaryPath, port);
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
					cap.merge(op);
					WebDriver driver = new ChromeDriver(op);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				}
//				else if (browserName.contains("fire")) {
//					FirefoxOptions options = new FirefoxOptions();
//					options.addArguments("--start-maximized");
//					options.setProfile(new FirefoxProfile(new File(path)));
//					cap.merge(options);
//					WebDriver driver=new ChromeDriver(op);
//					nlpResponseModel.setWebDriver(driver);
//					nlpResponseModel.setStatus(CommonConstants.pass);
//					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
//				} else if (browserName.contains("Edge")) {
//					// log.info("Edge Block");
//					EdgeOptions options = new EdgeOptions();
//					options.addArguments("--user-data-dir=" + path);
//					options.addArguments("--remote-allow-origins=*");
//					options.addArguments("--start-maximized");
//					cap.merge(options);
//					WebDriver driver=new ChromeDriver(op);
//					nlpResponseModel.setWebDriver(driver);
//					nlpResponseModel.setStatus(CommonConstants.pass);
//					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
//				} 
				else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
				}
			} else if (osName.contains("nux")) {
				killProcess(portNumber);
				if (browserName.contains("chrome")) {
					launchDebugger(binaryPath, port);
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
					cap.merge(op);
					WebDriver driver = new ChromeDriver(op);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				}
//				else if (browserName.contains("fire")) {
//					FirefoxOptions options = new FirefoxOptions();
//					options.addArguments("--start-maximized");
//					options.setProfile(new FirefoxProfile(new File(path)));
//					cap.merge(options);
//					WebDriver driver=new ChromeDriver(op);
//					nlpResponseModel.setWebDriver(driver);
//					nlpResponseModel.setStatus(CommonConstants.pass);
//					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
//				} else if (browserName.contains("Edge")) {
//					// log.info("Edge Block");
//					EdgeOptions options = new EdgeOptions();
//					options.addArguments("--user-data-dir=" + path);
//					options.addArguments("--remote-allow-origins=*");
//					options.addArguments("--start-maximized");
//					cap.merge(options);
//					WebDriver driver=new ChromeDriver(op);
//					nlpResponseModel.setWebDriver(driver);
//					nlpResponseModel.setStatus(CommonConstants.pass);
//					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
//				} 
				else if (osName.toLowerCase().contains("mac")) {
					killProcess(portNumber);
					if (browserName.contains("chrome")) {
						launchDebugger(binaryPath, port);
						ChromeOptions op = new ChromeOptions();
						op.setExperimentalOption("debuggerAddress", "localhost:" + port);
						op.addArguments("--start-maximized");
						op.addArguments("--remote-allow-origins=*");
						cap.merge(op);
						WebDriver driver = new ChromeDriver(op);
						nlpResponseModel.setWebDriver(driver);
						nlpResponseModel.setStatus(CommonConstants.pass);
						nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
					}
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
			// log.error(exception.toString());
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

	public static void launchDebugger(String chromeExecutable, String remoteDebuggingPort) {
		String[] command = { chromeExecutable, " -remote-debugging-port=" + remoteDebuggingPort };
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws NlpException {
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("Chrome");
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("HubURL", "http://localhost:4723/wd/hub");
		nlpRequestModel.getAttributes().put("Capability", cap);
		nlpRequestModel.getAttributes().put("Binary Path", "C:\\Users\\User\\Downloads\\chrome-win64");
		nlpRequestModel.getAttributes().put("Port", "2705");
		System.out.println(new ChromeDebuggerBinary().execute(nlpRequestModel));
	}
}