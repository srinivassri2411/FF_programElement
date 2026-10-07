package zepic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
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

import io.appium.java_client.mac.Mac2Driver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeDebuggerclassic implements Nlp {
	@InputParams({ @InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "HubUrl", type = "java.lang.String"),
			@InputParam(name = "Path", type = "java.lang.String"),
			@InputParam(name = "Port", type = "java.lang.String") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		DesiredCapabilities capability = (DesiredCapabilities) programElementsInput.get("Capability");
		String hubUrl = (String) programElementsInput.get("HubUrl");
		String path = (String) programElementsInput.get("Path");
		String port = (String) programElementsInput.get("Port");
		int portNumber = Integer.parseInt(port);
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			killProcess(portNumber);
			nlpResponseModel.setWebDriver(runDebugger(capability, path, port, hubUrl));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Browser Launched successfully");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to launch Browser ==>" + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public static WebDriver runDebugger(DesiredCapabilities cap, String path, String port, String url)
			throws IOException, InterruptedException {
		WebDriver driver = null;
		int portNumber = Integer.parseInt(port);
		String browserName = cap.getBrowserName().toLowerCase();
		String osName = System.getProperty("os.name").toLowerCase();
		path = System.getProperty("user.home") + File.separator + path;
		if (osName.contains("windows")) {
			killProcess(portNumber);
			waitForPortReleased(portNumber, 3000);
			Thread.sleep(2000);
			if (browserName.contains("chrome")) {
				Process process = Runtime.getRuntime().exec(
						"reg query \"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe\" /ve");
				Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A");
				String output = scanner.hasNext() ? scanner.next() : "";
				scanner.close();
				int startIndex = output.indexOf("REG_SZ") + "REG_SZ".length();
				String chromePath = null;
				try {
					chromePath = output.substring(startIndex).trim();
				} catch (Exception e) {
					chromePath = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local"
							+ File.separator + "Google" + File.separator + "Chrome" + File.separator + "Application"
							+ File.separator + "chrome.exe";
				}
				launchDebugger(chromePath, path, port);
				ChromeOptions op = new ChromeOptions();
				op.setExperimentalOption("debuggerAddress", "localhost:" + port);
				op.addArguments("--remote-allow-origins=*");
				cap.merge(op);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else if (browserName.contains("fire")) {
				FirefoxOptions options = new FirefoxOptions();
				options.setProfile(new FirefoxProfile(new File(path)));
				cap.merge(options);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else if (browserName.contains("edge")) {
				log.info("Edge Block");
				EdgeOptions options = new EdgeOptions();
				options.addArguments("--user-data-dir=" + path);
				options.addArguments("--remote-allow-origins=*");
				cap.merge(options);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else {
				log.info("Not Supported " + browserName + " Browser is selected");
			}
		} else if (osName.contains("nux")) {
			killProcess(portNumber);
			waitForPortReleased(portNumber, 3000);
			Thread.sleep(2000);
			if (browserName.contains("chrome")) {
				Process process = Runtime.getRuntime().exec("which google-chrome");
				Scanner scanner = new Scanner(process.getInputStream());
				String output = scanner.hasNext() ? scanner.next() : "";
				scanner.close();
				launchDebugger(output, path, port);
				ChromeOptions op = new ChromeOptions();
				op.setExperimentalOption("debuggerAddress", "localhost:" + port);
				op.addArguments("--remote-allow-origins=*");
				cap.merge(op);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else if (browserName.contains("fire")) {
				FirefoxOptions options = new FirefoxOptions();
				options.setProfile(new FirefoxProfile(new File(path)));
				cap.merge(options);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else if (browserName.contains("Edge")) {
				log.info("Edge Block");
				EdgeOptions options = new EdgeOptions();
				options.addArguments("--user-data-dir=" + path);
				options.addArguments("--remote-allow-origins=*");
				cap.merge(options);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else {
				log.info("Not Supported " + browserName + " Browser is selected");
			}
		} else if (osName.contains("mac")) {
			killProcess(portNumber);
			if (browserName.contains("chrome")) {
				String[] command = { "/bin/sh", "-c", "mdfind \"kMDItemCFBundleIdentifier == 'com.google.Chrome'\"" };
				Process process = Runtime.getRuntime().exec(command);
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				String chromePath = null;
				while ((line = reader.readLine()) != null) {
					chromePath = line;
				}
				reader.close();
				log.info(chromePath);
				launchDebugger(chromePath + "/Contents/MacOS/Google Chrome", path, port);
				ChromeOptions op = new ChromeOptions();
				op.setExperimentalOption("debuggerAddress", "localhost:" + port);
				op.addArguments("--remote-allow-origins=*");
				cap.merge(op);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else if (browserName.contains("fire")) {
				FirefoxOptions options = new FirefoxOptions();
				options.setProfile(new FirefoxProfile(new File(path)));
				cap.merge(options);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else if (browserName.contains("Edge")) {
				log.info("Edge Block");
				EdgeOptions options = new EdgeOptions();
				options.addArguments("--user-data-dir=" + path);
				options.addArguments("--remote-allow-origins=*");
				cap.merge(options);
				driver = new RemoteWebDriver(new URL(url), cap);
				log.info("Successfully Opened " + browserName + " Browser");

			} else {
				log.info("Not Supported " + browserName + " Browser is selected");
			}
		}
		return driver;
	}

	public interface IPHlpAPI extends Library {
		IPHlpAPI INSTANCE = Native.load("Iphlpapi", IPHlpAPI.class, W32APIOptions.DEFAULT_OPTIONS);

		int GetExtendedTcpTable(Pointer pTcpTable, IntByReference pdwSize, boolean order, int ulAf, int tableClass,
				int reserved);
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
			String cmd = "lsof -ti :" + port;
			Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String pid;
			while ((pid = reader.readLine()) != null) {
				log.info("Killing PID: " + pid);
				Runtime.getRuntime().exec("kill -9 " + pid);
			}
		}
	}

	public static void launchDebugger(String chromeExecutable, String userDataDir, String remoteDebuggingPort) {
		String[] command = { chromeExecutable, " -remote-debugging-port=" + remoteDebuggingPort, "--no-first-run",
				"--user-data-dir=" + userDataDir };
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
		WebDriver driver = new Mac2Driver(new DesiredCapabilities());
		String path = "Chrome";
		path = System.getProperty("user.home") + File.separator + path;
		log.info(path);
		ChromeDebuggerclassic cd = new ChromeDebuggerclassic();
		NlpRequestModel req = new NlpRequestModel();
//		log.info(cd.execute(req));
	}
}