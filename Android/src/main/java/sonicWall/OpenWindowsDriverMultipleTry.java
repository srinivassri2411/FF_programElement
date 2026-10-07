
package sonicWall;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.windows.WindowsDriver;

public class OpenWindowsDriverMultipleTry implements Nlp {
	public static Set<String> execptions = null;

	@InputParams({ @InputParam(name = "App File Path", type = "java.lang.String"),
			@InputParam(name = "HubURL", type = "java.lang.String") })
	@ReturnType(name = "driverObj", type = "org.openqa.selenium.WebDriver")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String appName = (String) programElementsInput.get("App File Path");
		String hubURL = (String) programElementsInput.get("HubURL");
		WebDriver returnValue = null;
		execptions = new LinkedHashSet<String>();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = createDriverInstance(appName, hubURL);
			nlpResponseModel.setWebDriver(returnValue);
			if (returnValue == null) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to lunch the windows App " + execptions);
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully lunched the windows App");
			}
		} catch (Exception e) {
			StringWriter wr = new StringWriter();
			e.printStackTrace(new PrintWriter(wr));
			nlpResponseModel
					.setMessage("Failed to lunch the windows App =>" + wr + "\n\n" + "Main exception " + execptions);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("driverObj", returnValue);
		execptions.clear();
		return nlpResponseModel;
	}

	public static boolean bringUpFront(String windowTitle) throws InterruptedException {
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
		if (hwnd == null) {
			throw new NotFoundException("Given " + windowTitle + " was not found");
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);
			Thread.sleep(200);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			return true;
		}
	}

	public static boolean maximize(String windowTitle) throws InterruptedException {
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			throw new NotFoundException("Given " + windowTitle + " was not found");
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
			Thread.sleep(200);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			System.out.println("Window maximized and brought to front: " + windowTitle);
			return true;
		}
	}

	private static WindowsDriver launchWindowsInstance(String app, String hubUrl) throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:app", app);
		capabilities.setCapability("appium:platformName", "Windows");
		capabilities.setCapability("appium:deviceName", "WindowsPC");
		capabilities.setCapability("appium:automationName", "Windows");
		capabilities.setCapability("appium:newCommandTimeout", 3600);
		return new WindowsDriver(new URL(hubUrl), capabilities);
	}

	private static WindowsDriver createDriverInstance(String app, String hubUrl)
			throws IOException, InterruptedException {
		for (int i = 1; i <= 3; i++) {
			System.out.println("Trying --------" + i);
			Runtime.getRuntime().exec("taskkill /F /IM banyan.exe");
			Runtime.getRuntime().exec("adb forward --remove-all");
			WindowsDriver driver;
			try {
				driver = launchWindowsInstance(app, hubUrl);
				String windowName = "CSE App";
				boolean res1 = bringUpFront(windowName);
				Thread.sleep(500);
				boolean res2 = maximize(windowName);
				System.out.println("Session created via driver successfully");
				return driver;
			} catch (SessionNotCreatedException e) {
				if (e.toString().contains("Failed to locate opened application window with")) {
					try {
						Thread.sleep(200);
						driver = launchWindowsInstance("Root", hubUrl);
						driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
						String windowName = "CSE App";
						boolean res1 = bringUpFront(windowName);
						Thread.sleep(500);
						boolean res2 = maximize(windowName);
						System.out.println("Retry Session created via driver successfully");
						driver.findElement(By.xpath("//Button[@Name='Banyan - 1 running window']")).click();
						return driver;
					} catch (Exception ex) {
						StringWriter wr = new StringWriter();
						e.printStackTrace(new PrintWriter(wr));
						execptions.add(wr.toString());
					}
				} else {
					try {
						launchBanyanExe();
						driver = launchWindowsInstance("Root", hubUrl);
						String windowName = "CSE App";
						boolean res1 = bringUpFront(windowName);
						Thread.sleep(500);
						boolean res2 = maximize(windowName);
						System.out.println("Retry 2 Session created via driver successfully");
						driver.findElement(By.xpath("//Button[@Name='Banyan - 1 running window']")).click();
						return driver;
					} catch (SessionNotCreatedException | InterruptedException e1) {
						StringWriter wr = new StringWriter();
						e.printStackTrace(new PrintWriter(wr));
						execptions.add(wr.toString());
					}
				}
			}
			Thread.sleep(1000);
		}
		return null;
	}

	public static void launchBanyanExe() {
		try {
			String scriptContent = "Set objShell = CreateObject(\"WScript.Shell\")\n"
					+ "objShell.Run \"\"\"C:\\Program Files\\Banyan\\Banyan.exe\"\"\", 1, False";

			File tempVbs = File.createTempFile("launchBanyan", ".vbs");
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempVbs))) {
				bw.write(scriptContent);
			}
			ProcessBuilder builder = new ProcessBuilder("wscript.exe", tempVbs.getAbsolutePath());
			builder.inheritIO();
			Process process = builder.start();
			tempVbs.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}