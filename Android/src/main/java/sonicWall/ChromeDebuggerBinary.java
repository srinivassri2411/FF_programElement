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

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
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
			String url = (String) attributes.get("HubURL");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String binaryPath = (String) attributes.get("Binary Path");
			String port = (String) attributes.get("Port");
			int portNumber = Integer.parseInt(port);
			String browserName = cap.getBrowserName();
			String osName = System.getProperty("os.name");
			if (osName.contains("Windows")) {
				killProcess(portNumber);
				waitForPortReleased(portNumber, 3000);
				Thread.sleep(2000);
				if (browserName.contains("chrome")) {
					System.setProperty("webdriver.chrome.driver", binaryPath + File.separator + "chromedriver.exe");
					binaryPath = binaryPath + File.separator + "chrome.exe";
					launchDebugger(binaryPath, port);
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
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
//					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
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
//					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
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
				waitForPortReleased(portNumber, 3000);
				if (browserName.contains("chrome")) {
					launchDebugger(binaryPath, port);
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
					cap.merge(op);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				}
//				else if (browserName.contains("fire")) {
//					FirefoxOptions options = new FirefoxOptions();
//					options.addArguments("--start-maximized");
//					options.setProfile(new FirefoxProfile(new File(path)));
//					cap.merge(options);
//					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
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
//					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
//					nlpResponseModel.setWebDriver(driver);
//					nlpResponseModel.setStatus(CommonConstants.pass);
//					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
//				} 
				else if (osName.toLowerCase().contains("mac")) {
					killProcess(portNumber);
					waitForPortReleased(portNumber, 3000);
					if (browserName.contains("chrome")) {
						launchDebugger(binaryPath, port);
						ChromeOptions op = new ChromeOptions();
						op.setExperimentalOption("debuggerAddress", "localhost:" + port);
						op.addArguments("--start-maximized");
						op.addArguments("--remote-allow-origins=*");
						cap.merge(op);
						WebDriver driver = new RemoteWebDriver(new URL(url), cap);
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

	public interface IPHlpAPI extends Library {
		IPHlpAPI INSTANCE = Native.load("Iphlpapi", IPHlpAPI.class, W32APIOptions.DEFAULT_OPTIONS);

		int GetExtendedTcpTable(Pointer pTcpTable, IntByReference pdwSize, boolean order, int ulAf, int tableClass,
				int reserved);
	}

	public static void killProcess(Integer port) throws IOException {
		String operatingSystem = System.getProperty("os.name").toLowerCase();

		if (operatingSystem.contains("win")) {
			int pid = getPidForPort(port);
			if (pid > 0) {
				System.out.println("**************id is " + pid);
				Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					System.out.println("Process details for PID " + pid + ":");
					reader.lines().forEach(System.out::println);
				} catch (IOException e) {
					System.err.println("Unable to fetch tasklist info for PID: " + pid);
				}
				Runtime.getRuntime().exec("taskkill /F /PID " + pid);
				System.out.println("Killed process with PID: " + pid + " on port: " + port);
			} else {
				System.out.println("No process found listening on port " + port);
			}
		} else {
			String command = "lsof -t -i:" + port;
			Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String pid;
				while ((pid = reader.readLine()) != null) {
					pid = pid.trim();
					if (!pid.isEmpty()) {
						System.out.println("**************id is " + pid);
						Runtime.getRuntime().exec("kill -9 " + pid);
						System.out.println("Killed PID: " + pid + " on port: " + port);
						return;
					}
				}
			}
			System.out.println("No process found listening on port " + port);
		}
	}

	private static int getPidForPort(int targetPort) throws IOException {
		final int AF_INET = 2; // IPv4
		final int TCP_TABLE_OWNER_PID_ALL = 5;
		final int bufferSize = 65536;

		Pointer buffer = new Memory(bufferSize);
		IntByReference sizePtr = new IntByReference(bufferSize);

		int result = IPHlpAPI.INSTANCE.GetExtendedTcpTable(buffer, sizePtr, true, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0);
		if (result != 0) {
			throw new IOException("GetExtendedTcpTable failed with code: " + result);
		}

		int numEntries = buffer.getInt(0);
		int rowSize = 24;
		int offset = 4;

		for (int i = 0; i < numEntries; i++) {
			int state = buffer.getInt(offset);
			int localAddr = buffer.getInt(offset + 4);
			int localPort = Short.reverseBytes(buffer.getShort(offset + 8)) & 0xFFFF;
			int pid = buffer.getInt(offset + 20);

			if (localPort == targetPort && state == 2 /* LISTEN */) {
				return pid;
			}

			offset += rowSize;
		}

		return -1;
	}

	public static void launchDebugger(String chromeExecutable, String remoteDebuggingPort) {
		String[] command = { chromeExecutable, " -remote-debugging-port=" + remoteDebuggingPort, "--guest" };
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void waitForPortReleased(int port, int timeoutMillis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeoutMillis) {
			try (java.net.Socket socket = new java.net.Socket("localhost", port)) {
			} catch (IOException e) {
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
		}
		System.err.println("Port " + port + " is still occupied after timeout.");
	}

	public static void main(String[] args) throws NlpException {
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("Capability", cap);
		nlpreq.getAttributes().put("HubURL", "http://localhost:4444/wd/hub");
		nlpreq.getAttributes().put("Port", "2705");
		nlpreq.getAttributes().put("Binary Path", "C:\\Users\\User\\ChromeBinary");
		System.out.println(new ChromeDebuggerBinary().execute(nlpreq));
	}
}