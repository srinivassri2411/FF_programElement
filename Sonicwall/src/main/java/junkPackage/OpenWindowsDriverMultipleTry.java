
package junkPackage;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
			if (returnValue == null) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to lunch the windows App " + execptions);
			} else {
//				maximizeApp("SonicWall Cloud Secure Edge");
				returnValue.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
				nlpResponseModel.setWebDriver(returnValue);
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
		log.info("All exceptions found--->" + execptions);
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
			log.info("Window maximized and brought to front: " + windowTitle);
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
		capabilities.setCapability("appium:ms:waitForIdleTimeout", 0); // Time (ms) to wait for UI thread to become idle
		if (!app.contains("Root")) {
			capabilities.setCapability("appium:ms:waitForAppLaunch", 60); // Wait time (seconds) for app to be ready
																			// before returning a session

		}
		capabilities.setCapability("appium:ms:experimental-webdriver", true);// Improves UI tree sync
		capabilities.setCapability("appium:ms:uiautomationEnableNativeEvents", false);// Sometimes helps with real
																						// clicks
		WindowsDriver driver = new WindowsDriver(new URL(hubUrl), capabilities);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		return driver;
	}

	private static WindowsDriver createDriverInstance(String app, String hubUrl)
			throws IOException, InterruptedException {
		for (int i = 1; i <= 3; i++) {
			log.info("Trying --------" + i);
			Runtime.getRuntime().exec("taskkill /F /IM \"SonicWall Cloud Secure Edge.exe\"");
			Thread.sleep(500);
			Runtime.getRuntime().exec("taskkill /F /IM \"SonicWall Cloud Secure Edge.exe\"");
			Thread.sleep(500);
			Runtime.getRuntime().exec("adb forward --remove-all");
			Thread.sleep(500);
			WindowsDriver driver;
			try {
				driver = launchWindowsInstance(app, hubUrl);
				String windowName = "SonicWall Cloud Secure Edge";
				boolean res1 = bringUpFront(windowName);
				Thread.sleep(500);
				boolean res2 = maximize(windowName);
				log.info("Session created via driver successfully with exe");
				return driver;
			} catch (SessionNotCreatedException e) {
				StringWriter wr1 = new StringWriter();
				e.printStackTrace(new PrintWriter(wr1));
				execptions.add(wr1.toString());
				if (e.toString().contains("Failed to locate opened application window with")) {
					try {
						Thread.sleep(200);
						driver = launchWindowsInstance("Root", hubUrl);
						String windowName = "SonicWall Cloud Secure Edge";
						boolean res1 = bringUpFront(windowName);
						Thread.sleep(500);
						boolean res2 = maximize(windowName);
						log.info("Retry Session created via driver successfully");
//						driver.findElement(By.xpath("//Button[@Name='Banyan - 1 running window']")).click();
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
						String windowName = "SonicWall Cloud Secure Edge";
						boolean res1 = bringUpFront(windowName);
						Thread.sleep(500);
						boolean res2 = maximize(windowName);
						log.info("Retry 2 Session created via driver successfully");
//						driver.findElement(By.xpath("//Button[@Name='Banyan - 1 running window']")).click();
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

//	public static WindowsDriver startFromApplevel(WebElement activeWindow, String hubUrl) throws MalformedURLException {
//		String handle = activeWindow.getAttribute("NativeWindowHandle");
//		int windowHandleInt = Integer.parseInt(handle);
//		String hexHandle = Integer.toHexString(windowHandleInt);
//		DesiredCapabilities cap = new DesiredCapabilities();
//		cap.setCapability("appium:appTopLevelWindow", hexHandle);
//		cap.setCapability("appium:platformName", "Windows");
//		cap.setCapability("appium:automationName", "Windows");
//		return new WindowsDriver(new URL(hubUrl), cap);
//	}

	public static void launchBanyanExe() throws InterruptedException {
		log.info("Try with vbs files");
		try {
			String scriptContent = "Set objShell = CreateObject(\"WScript.Shell\")\n"
					+ "objShell.Run \"\"\"C:\\Program Files\\SonicWall Cloud Secure Edge\\SonicWall Cloud Secure Edge.exe\"\"\", 3, False";

			File tempVbs = File.createTempFile("launchBanyan", ".vbs");
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempVbs))) {
				bw.write(scriptContent);
			}
			ProcessBuilder builder = new ProcessBuilder("wscript.exe", tempVbs.getAbsolutePath());
			builder.inheritIO();
			Process process = builder.start();
			Thread.sleep(2000);
			builder.start();
			// tempVbs.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void maximizeApp(String windowTitle) throws IOException {
		String command = "powershell -command \"(Get-Process | Where-Object {$_.MainWindowTitle -eq '" + windowTitle
				+ "'}).MainWindowHandle | ForEach-Object { (New-Object -TypeName psobject -Property @{hWnd=$_}) } | ForEach-Object { $sig = '[DllImport(\\\"user32.dll\\\")]public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);'; $type = Add-Type -MemberDefinition $sig -Name Win32ShowWindowAsync -Namespace Win32Functions -PassThru; $type::ShowWindowAsync($_.hWnd, 3) }\"";
		Runtime.getRuntime().exec(command);
	}

	public static void main(String[] args) throws NlpException, InterruptedException, IOException {
//		Runtime.getRuntime().exec("taskkill /F /IM \"SonicWall Cloud Secure Edge.exe\"");
//		launchBanyanExe();
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("App File Path",
				"C:\\Program Files\\SonicWall Cloud Secure Edge\\SonicWall Cloud Secure Edge.exe");
		model.getAttributes().put("HubURL", "http://localhost:4723/wd/hub");
		NlpResponseModel response = new OpenWindowsDriverMultipleTry().execute(model);
		System.out.println(response);
//
//		System.out.println(response);
//		WebDriver driver = response.getWebDriver();
//		driver.findElement(By.name("Enter Invite Code:")).sendKeys(Keys.ENTER);
//		driver.findElement(By.name("Enter Invite Code:")).sendKeys("qa-automation-testorg1");
//		driver.findElement(By.name("CONTINUE")).sendKeys(Keys.ENTER);
	}
}