package Business_Logics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.SessionNotCreatedException;
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

public class ChromeDebuggerBinaryTesting implements Nlp {

	// ---- tuning values -------------------------------------------------------
	private static final int PORT_RELEASE_TIMEOUT_MS = 5000; // wait for old Chrome to free the port
	private static final int PORT_OPEN_TIMEOUT_MS = 20000; // wait for new Chrome to open the port
	private static final int MAX_LAUNCH_ATTEMPTS = 3; // launch + attach attempts

	static WebDriver mainDriv = null;

	public static WebDriver getMainDriv() {
		return mainDriv;
	}

	public static void setMainDriv(WebDriver mainDriv) {
		ChromeDebuggerBinaryTesting.mainDriv = mainDriv;
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

					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");

					WebDriver driver = launchAndAttach(binaryPath, portNumber, port, op);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
				}

			} else if (osName.contains("nux")) {
				if (browserName.contains("chrome")) {
					// FIX: kill old Chrome first, then launch (original code launched first and
					// then killed the Chrome it had just started)
					killProcess(portNumber, binaryPath);
					waitForPortReleased(portNumber, PORT_RELEASE_TIMEOUT_MS);
					launchDebugger(binaryPath, port);
					if (!waitForPortOpen(portNumber, PORT_OPEN_TIMEOUT_MS)) {
						throw new IllegalStateException("Chrome debug port " + port + " did not open in "
								+ (PORT_OPEN_TIMEOUT_MS / 1000) + "s");
					}
					ChromeOptions op = new ChromeOptions();
					op.setExperimentalOption("debuggerAddress", "localhost:" + port);
					op.addArguments("--start-maximized");
					op.addArguments("--remote-allow-origins=*");
					cap.merge(op);
					WebDriver driver = new RemoteWebDriver(new URL(url), cap);
					nlpResponseModel.setWebDriver(driver);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully Opened " + browserName + " Browser");
				} else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
				}

			} else if (osName.toLowerCase().contains("mac")) {

				Thread.sleep(2000);

				if (browserName.contains("chrome")) {
					String chromeBinary = binaryPath
							+ "/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing";
					String chromeDriver = binaryPath + "/chromedriver";
					System.setProperty("webdriver.chrome.driver", chromeDriver);

					ChromeOptions options = new ChromeOptions();
					options.setExperimentalOption("debuggerAddress", "localhost:" + port);
					options.addArguments("--remote-allow-origins=*");

					WebDriver driver = launchAndAttach(chromeBinary, portNumber, port, options);

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
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launched " + exception.toString());
		}
		return nlpResponseModel;
	}

	/**
	 * Kills any old Chrome, launches a fresh debug Chrome, waits until its debug
	 * port is really open, then attaches ChromeDriver. Retries the whole sequence if
	 * the session cannot be created.
	 */
	private WebDriver launchAndAttach(String chromeExecutable, int portNumber, String port, ChromeOptions options)
			throws IOException, InterruptedException {

		boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
		SessionNotCreatedException lastError = null;

		for (int attempt = 1; attempt <= MAX_LAUNCH_ATTEMPTS; attempt++) {
			killProcess(portNumber, chromeExecutable);
			waitForPortReleased(portNumber, PORT_RELEASE_TIMEOUT_MS);

			if (isMac) {
				try {
					FileUtils.cleanDirectory(new File(
							System.getProperty("user.home") + "/Library/Application Support/Google/Chrome for Testing"));
				} catch (Exception e) {
					// ignore - directory may not exist
				}
			}

			launchDebugger(chromeExecutable, port);

			if (!waitForPortOpen(portNumber, PORT_OPEN_TIMEOUT_MS)) {
				System.err.println("Attempt " + attempt + ": debug port " + port + " did not open in "
						+ (PORT_OPEN_TIMEOUT_MS / 1000) + "s");
				if (attempt == MAX_LAUNCH_ATTEMPTS) {
					throw new IllegalStateException("Chrome debug port " + port + " did not open after "
							+ MAX_LAUNCH_ATTEMPTS + " attempts");
				}
				continue;
			}

			try {
				return new ChromeDriver(options);
			} catch (SessionNotCreatedException e) {
				lastError = e;
				System.err.println("Attempt " + attempt + " of " + MAX_LAUNCH_ATTEMPTS
						+ ": session not created - " + e.getMessage());
				Thread.sleep(2000);
			}
		}
		throw lastError != null ? lastError
				: new IllegalStateException("Unable to launch Chrome on port " + port);
	}

	public interface IPHlpAPI extends Library {
		IPHlpAPI INSTANCE = Native.load("Iphlpapi", IPHlpAPI.class, W32APIOptions.DEFAULT_OPTIONS);

		int GetExtendedTcpTable(Pointer pTcpTable, IntByReference pdwSize, boolean order, int ulAf, int tableClass,
				int reserved);
	}

	public static void killProcess(Integer port, String binaryPath) throws IOException, InterruptedException {
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		if (operatingSystem.contains("win")) {
			// 1) kill every process started from the given chrome.exe
			ProcessHandle.allProcesses().forEach(process -> {
				Optional<String> command = process.info().command();

				if (command.isPresent() && command.get().equalsIgnoreCase(binaryPath)) {
					System.out.println("Killing PID: " + process.pid());
					process.destroyForcibly();
					try {
						process.onExit().get(5, TimeUnit.SECONDS); // wait until it is really gone
					} catch (Exception ignored) {
					}
				}
			});

			// 2) kill whatever is still listening on the port
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
				// /T also kills child processes, and waitFor() makes sure the kill is finished
				Process kill = Runtime.getRuntime().exec("taskkill /F /T /PID " + pid);
				kill.waitFor(10, TimeUnit.SECONDS);
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
	}

	private static int getPidForPort(int targetPort) throws IOException {
		final int AF_INET = 2; // IPv4
		final int TCP_TABLE_OWNER_PID_ALL = 5;
		final int ERROR_INSUFFICIENT_BUFFER = 122;

		// FIX: the TCP table can be larger than a fixed 64 KB buffer on a busy machine
		// (your port 2706 alone had ~200 connections). Grow the buffer when Windows asks.
		int bufferSize = 65536;
		Pointer buffer = null;
		int result = ERROR_INSUFFICIENT_BUFFER;
		IntByReference sizePtr = new IntByReference(bufferSize);

		for (int i = 0; i < 5 && result == ERROR_INSUFFICIENT_BUFFER; i++) {
			buffer = new Memory(bufferSize);
			sizePtr.setValue(bufferSize);
			result = IPHlpAPI.INSTANCE.GetExtendedTcpTable(buffer, sizePtr, true, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0);
			if (result == ERROR_INSUFFICIENT_BUFFER) {
				bufferSize = Math.max(sizePtr.getValue(), bufferSize) * 2;
			}
		}
		if (result != 0) {
			throw new IOException("GetExtendedTcpTable failed with code: " + result);
		}

		int numEntries = buffer.getInt(0);
		int rowSize = 24;
		int offset = 4;

		for (int i = 0; i < numEntries; i++) {
			int state = buffer.getInt(offset);
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

	/** Waits until nothing accepts connections on the port (old Chrome is gone). */
	public static void waitForPortReleased(int port, int timeoutMillis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeoutMillis) {
			try (Socket socket = new Socket("localhost", port)) {
				// still connectable -> still occupied
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

	/** Waits until the new Chrome is actually accepting connections on the port. */
	public static boolean waitForPortOpen(int port, int timeoutMillis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeoutMillis) {
			try (Socket socket = new Socket("localhost", port)) {
				return true;
			} catch (IOException e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignored) {
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws NlpException, InterruptedException, IOException {

		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("Capability", cap);
		nlpreq.getAttributes().put("HubURL", "localhost/wd/hub");
		nlpreq.getAttributes().put("Port", "2705");
		nlpreq.getAttributes().put("Binary Path", "C:\\Users\\User\\ChromeBinary");
		NlpResponseModel req = new ChromeDebuggerBinaryTesting().execute(nlpreq);
		System.out.println(req.getStatus() + " - " + req.getMessage());
	}
}