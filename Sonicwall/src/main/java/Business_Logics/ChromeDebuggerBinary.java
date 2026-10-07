package Business_Logics;

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
import java.util.Optional;

import org.apache.commons.io.FileUtils;
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
	static WebDriver mainDriv = null;

	public static WebDriver getMainDriv() {
		return mainDriv;
	}

	public static void setMainDriv(WebDriver mainDriv) {
		ChromeDebuggerBinary.mainDriv = mainDriv;
	}

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
			String browserName = cap.getBrowserName().toLowerCase();
			String osName = System.getProperty("os.name");
			if (osName.contains("Windows")) {
				Thread.sleep(2000);
				if (browserName.contains("chrome")) {
					System.setProperty("webdriver.chrome.driver", binaryPath + File.separator + "chromedriver.exe");
					binaryPath = binaryPath + File.separator + "chrome.exe";
					killProcess(portNumber, binaryPath);
					waitForPortReleased(portNumber, 3000);
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
				if (browserName.contains("chrome")) {
					launchDebugger(binaryPath, port);
					killProcess(portNumber, binaryPath);
					waitForPortReleased(portNumber, 3000);
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
				else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
				}
			} else if (osName.toLowerCase().contains("mac")) {

				Thread.sleep(2000);

				if (browserName.contains("chrome")) {
					String chromeBinary = binaryPath
							+ "/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing";
					killProcess(portNumber, chromeBinary);
					waitForPortReleased(portNumber, 3000);
					try {
						FileUtils.cleanDirectory(new File(System.getProperty("user.home")
								+ "/Library/Application Support/Google/Chrome for Testing"));
					} catch (Exception e) {
					}
					String chromeDriver = binaryPath + "/chromedriver";

					System.setProperty("webdriver.chrome.driver", chromeDriver);

					launchDebugger(chromeBinary, port);

					ChromeOptions options = new ChromeOptions();
					options.setExperimentalOption("debuggerAddress", "localhost:" + port);
					options.addArguments("--remote-allow-origins=*");

					WebDriver driver = new ChromeDriver(options);

					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Chrome launched successfully on macOS");
				} else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
				}
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Os not Supported");
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	public static void killProcess(Integer port, String binaryPath) throws IOException, InterruptedException {
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		if (operatingSystem.contains("win")) {
			ProcessHandle.allProcesses().forEach(process -> {
				Optional<String> command = process.info().command();

				if (command.isPresent() && command.get().equalsIgnoreCase(binaryPath)) {
					System.out.println("Killing PID: " + process.pid());
					process.destroyForcibly();
				}
			});
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
			String cmd = "lsof -nP -iTCP:" + port + " -sTCP:LISTEN -t";
			Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmd });

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String pid;

			while ((pid = reader.readLine()) != null) {
				pid = pid.trim();
				if (pid.isEmpty())
					continue;

				Process check = Runtime.getRuntime()
						.exec(new String[] { "bash", "-c", "ps -p " + pid + " -o command=" });

				BufferedReader cr = new BufferedReader(new InputStreamReader(check.getInputStream()));

				String command = cr.readLine();

				System.out.println("[CHECK] PID=" + pid + " CMD=" + command);
				if (command != null && command.contains("--remote-debugging-port=" + port)
						&& (command.contains("Chrome") || command.contains("chromedriver"))) {

					System.out.println("[KILL] Chrome process on port " + port + " -> PID " + pid);

					Runtime.getRuntime().exec("kill " + pid);
					Thread.sleep(800);
					Runtime.getRuntime().exec("kill -9 " + pid);
				} else {
					System.out.println("[SKIP] Not Chrome for port " + port);
				}
			}
		}

//		else {
//			String bundleId = "com.google.chrome.for.testing";
//
//			ProcessBuilder pb = new ProcessBuilder(
//			    "osascript",
//			    "-e",
//			    "quit app id \"" + bundleId + "\""
//			);
//
//			Process process = pb.start();
//			process.waitFor();
//			String command = "lsof -t -i:" + port;
//			Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
//			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//				String pid;
//				while ((pid = reader.readLine()) != null) {
//					pid = pid.trim();
//					if (!pid.isEmpty()) {
//						System.out.println("**************id is " + pid);
//						Runtime.getRuntime().exec("kill -9 " + pid);
//						System.out.println("Killed PID: " + pid + " on port: " + port);
//						return;
//					}
//				}
//			}
//			System.out.println("No process found listening on port " + port);
//		}
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
		String[] command = { chromeExecutable, "--guest", "--remote-debugging-port=" + remoteDebuggingPort,
				"--no-first-run", "--no-default-browser-check" };
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

	public static void main(String[] args) throws NlpException, InterruptedException, IOException {

		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
//		WebDriver driver1 = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
//		driver1.get("http://localhost:4444/wd/hub");
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("Capability", cap);
		nlpreq.getAttributes().put("HubURL", "http://localhost:4444/wd/hub");
		nlpreq.getAttributes().put("Port", "2705");
		nlpreq.getAttributes().put("Binary Path", "/Users/user/ChromeBinary");
//		nlpreq.getAttributes().put("Binary Path", "C:\\Users\\User\\ChromeBinary");
		NlpResponseModel req = new ChromeDebuggerBinary().execute(nlpreq);
		// System.out.println(req.getWebDriver().getCurrentUrl());
//		setMainDriv(req.getWebDriver());
//		WebDriver driver = getMainDriv();
//		Thread.sleep(2000);
//		Mac2Options options = new Mac2Options();
//		options.setCapability("appium:automationName", "Mac2");
//		options.setCapability("platformName", "mac");
//		options.setCapability("appium:bundleId", "com.sonicwall.cse");
//		options.setCapability("appium:newCommandTimeout", "0");
//		Mac2Driver mac2Driver = new Mac2Driver(new URL("http://localhost:4723/"), options);
//		setMainDriv(mac2Driver);
//		mac2Driver = (Mac2Driver) getMainDriv();
//		mac2Driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
//		Thread.sleep(5000);
//		mac2Driver.findElement(By.xpath("//*[@label='menuitem-Services']")).click();
//		mac2Driver.findElement(By.xpath("//*[@value='Hosted Websites']")).click();
//		mac2Driver.findElement(By.xpath("//*[@placeholderValue='Search']")).click();
//		mac2Driver.findElement(By.xpath("//*[@placeholderValue='Search']")).sendKeys("Check_HW_Mac");
//		Thread.sleep(3000);
//		mac2Driver.findElement(By.xpath(
//				"//XCUIElementTypeTable/XCUIElementTypeTableColumn[4]/XCUIElementTypeCell[2]//XCUIElementTypeButton"))
//				.click();
//		// driver.switchTo().newWindow(WindowType.TAB);
//		new Scanner(System.in).next();
//		// System.out.println(req.getWebDriver().getCurrentUrl());
//		Set<String> data = driver.getWindowHandles();
//		System.out.println(data);
//		for (String string : data) {
//
//			driver.switchTo().window(string);
//
//			if (driver.getCurrentUrl().contains("https:")) {
//				driver.switchTo().window(string);
//				driver.findElement(By.xpath("//input[@autocomplete=\"username\"]")).sendKeys("fghj");
//			}
//
//		}
	}
//	public static void main1(String[] args) {
//		
//		String chromeDriver =  "/Users/sonicwall/ChromeBinary/chromedriver";
//
//		System.setProperty("webdriver.chrome.driver", chromeDriver);
//
//		launchDebugger(chromeBinary, port);
//
//		ChromeOptions options = new ChromeOptions();
//		//options.setExperimentalOption("debuggerAddress", "localhost:" + port);
//		options.addArguments("--remote-debugging-port=2705");
//		options.addArguments("--guest");
//		options.addArguments("--remote-allow-origins=*");
//
//		WebDriver driver = new ChromeDriver(options);
//
//	}

}