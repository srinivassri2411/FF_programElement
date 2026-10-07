package Windows;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
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
import io.appium.java_client.windows.options.WindowsOptions;

public class OpenBanyanRetry implements Nlp {
	public static Set<String> execptions = null;

	@InputParams({ @InputParam(name = "App Path", type = "java.lang.String"),
			@InputParam(name = "Hub URL", type = "java.lang.String") })
	@ReturnType(name = "driverObj", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String appPath = (String) programElementsInput.get("App Path");
		String hubURL = (String) programElementsInput.get("Hub URL");
		WebDriver returnValue = null;
		execptions = new LinkedHashSet<String>();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = openWindowsAppWithFallBack(hubURL, appPath);
			if (returnValue != null) {
				String windowName = "SonicWall Cloud Secure Edge";
				boolean res1 = bringUpFront(windowName);
				Thread.sleep(500);
				boolean res2 = maximize(windowName);
				System.out.println("Pass");
				nlpResponseModel.setWebDriver(returnValue);
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				System.out.println("Fail");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to open app" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("driverObj", returnValue);
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
//			log.info("Window maximized and brought to front: " + windowTitle);
			return true;
		}
	}

	public static WebDriver openWindowsAppWithFallBack(String hubURL, String exePath) throws Exception {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:app", exePath);
		capabilities.setCapability("appium:platformName", "Windows");
		capabilities.setCapability("appium:deviceName", "WindowsPC");
		capabilities.setCapability("appium:automationName", "Windows");
		capabilities.setCapability("appium:newCommandTimeout", 3600);
		capabilities.setCapability("appium:ms:waitForIdleTimeout", 0); // Time (ms) to wait for UI thread to become idle
		capabilities.setCapability("appium:ms:experimental-webdriver", true);// Improves UI tree sync
		capabilities.setCapability("appium:ms:uiautomationEnableNativeEvents", false);// Sometimes helps with real
																						// clicks

		for (int i = 0; i < 4; i++) {
			DesktopUtil.cleanupSonicWall();
			Runtime.getRuntime().exec("adb forward --remove-all");
			Thread.sleep(500);
			try {
				String exeName = (new File(exePath)).getName();
				List<WinDef.HWND> windowsBefore = DesktopUtil.getValidWindowHandles();
				WinDef.HWND hwnd = DesktopUtil.getWindowHandleMatchingExeName(windowsBefore, exeName);
				if (Objects.isNull(hwnd)) {
					(new ProcessBuilder(new String[] { "cmd", "/c", "start", "", exePath })).start();
					Thread.sleep(4000);
					hwnd = waitForNewWindow(windowsBefore);
				}
				if (Objects.isNull(hwnd))
					throw new RuntimeException("Could not find a running window for: " + exePath);
				WindowsOptions attachCaps = new WindowsOptions();
				capabilities.asMap().forEach((k, v) -> {
					if (!k.equals("appium:app") && !k.equals("app"))
						attachCaps.setCapability(k, v);
				});
				attachCaps.setCapability("appTopLevelWindow", Long.toHexString(Pointer.nativeValue(hwnd.getPointer())));
				return (WebDriver) new WindowsDriver(new URL(hubURL), (Capabilities) attachCaps);
			} catch (Exception e) {
				System.out.println("Excep " + e);
			}
		}
		return null;
	}

	private static WinDef.HWND waitForNewWindow(List<WinDef.HWND> windowHandles) throws InterruptedException {
		Set<WinDef.HWND> windowsSet = new HashSet<>(windowHandles);
		for (int i = 0; i < 20; i++) {
			for (WinDef.HWND hwnd : DesktopUtil.getValidWindowHandles()) {
				if (!windowsSet.contains(hwnd))
					return hwnd;
			}
			Thread.sleep(1000L);
		}
		return null;
	}

	public static void main(String[] args) throws NlpException {
		for (int i = 0; i < 20; i++) {
			NlpRequestModel model = new NlpRequestModel();
			model.getAttributes().put("App Path",
					"C:\\Program Files\\SonicWall Cloud Secure Edge\\SonicWall Cloud Secure Edge.exe");
			model.getAttributes().put("Hub URL", "http://localhost:4723/wd/hub");
			NlpResponseModel output = new OpenBanyanRetry().execute(model);
			System.out.println(output);
			output.getWebDriver().quit();
		}
	}
}

class DesktopUtil {
	public static int closeWindowsByTitle(String windowTitle) {
		List<WinDef.HWND> windows = getValidWindowHandles();
		int closedCount = 0;

		String target = normalize(windowTitle);

		for (WinDef.HWND hwnd : windows) {
			char[] buffer = new char[512];
			User32.INSTANCE.GetWindowText(hwnd, buffer, 512);

			String title = Native.toString(buffer);

			if (title.trim().isEmpty()) {
				continue;
			}

			String normalizedTitle = normalize(title);

			if (normalizedTitle.contains(target)) {
				System.out.println("Closing window: " + title);

				User32.INSTANCE.PostMessage(hwnd, 0x0010, // WM_CLOSE
						null, null);

				closedCount++;
			}
		}

		return closedCount;
	}

	public static void cleanupSonicWall() throws InterruptedException, IOException {

		String windowTitle = "SonicWall Cloud Secure Edge";
		String processName = "SonicWall Cloud Secure Edge.exe";

		// First close all matching windows gracefully
		int closed = DesktopUtil.closeWindowsByTitle(windowTitle);

		System.out.println("Closed " + closed + " SonicWall window(s).");

		Thread.sleep(1000);

		// Then kill any remaining processes
		Runtime.getRuntime().exec("taskkill /F /IM \"" + processName + "\"");

		Thread.sleep(1000);

		// Do it once more in case the application restarted/spawned another process
		Runtime.getRuntime().exec("taskkill /F /IM \"" + processName + "\"");

		Thread.sleep(500);
	}

	public static WinDef.HWND findWindowName(String windowName) {
		WinDef.HWND[] foundHandle = { null };

		User32.INSTANCE.EnumWindows((hwnd, data) -> {
			char[] windowText = new char[512];

			User32.INSTANCE.GetWindowText(hwnd, windowText, 512);

			String wName = Native.toString(windowText);

			if (!wName.isEmpty() && wName.contains(windowName)) {
				foundHandle[0] = hwnd;
				return false;
			}

			return true;

		}, Pointer.NULL);

		return foundHandle[0];
	}

	public static WindowsOptions normalizeToWindowsOption(DesiredCapabilities desiredCapabilities) {
		WindowsOptions options = new WindowsOptions();
		desiredCapabilities.asMap().forEach((key, value) -> {
			String cleanKey = key.startsWith("appium:") ? key.replace("appium:", "") : key;
			if (cleanKey.equalsIgnoreCase("app")) {
				options.setApp(String.valueOf(value));
			} else {
				options.setCapability(cleanKey, value);
			}
		});
		return options;
	}

	public static boolean isAppCapabilityFilePath(String appCapability) {
		if (Objects.isNull(appCapability))
			return false;
		File file = new File(appCapability);
		return (file.exists() && file.isFile());
	}

	public static WinDef.HWND waitAndGetWindowHandleMatchingExeName(String exeName) throws InterruptedException {
		for (int i = 0; i < 20; i++) {
			List<WinDef.HWND> validWindowHandles = getValidWindowHandles();
			if (!validWindowHandles.isEmpty()) {
				WinDef.HWND windowHandle = getWindowHandleMatchingExeName(validWindowHandles, exeName);
				if (windowHandle != null)
					return windowHandle;
			}
			Thread.sleep(1000L);
		}
		return null;
	}

	public static WinDef.HWND getWindowHandleMatchingExeName(List<WinDef.HWND> windows, String exeName) {
		String exeNormalized = normalize(exeName);
		for (WinDef.HWND hwnd : windows) {
			char[] buffer = new char[512];
			User32.INSTANCE.GetWindowText(hwnd, buffer, 512);
			String title = Native.toString(buffer);
			if (title.trim().isEmpty())
				continue;
			String titleNormalized = normalize(title);
			if (titleNormalized.contains(exeNormalized) || exeNormalized.contains(titleNormalized))
				return hwnd;
		}
		return null;
	}

	public static String normalize(String input) {
		if (input == null)
			return "";
		return input.toLowerCase().replaceAll("\\.exe$|\\s+", "");
	}

	public static List<WinDef.HWND> getValidWindowHandles() {
		List<WinDef.HWND> results = new ArrayList<>();

		User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
			if (!User32.INSTANCE.IsWindowVisible(hwnd)) {
				return true;
			}

			char[] buffer = new char[512];
			User32.INSTANCE.GetWindowText(hwnd, buffer, 512);

			String title = Native.toString(buffer);

			if (title.trim().isEmpty()) {
				return true;
			}

			if (title.equalsIgnoreCase("Program Manager")) {
				return true;
			}

			results.add(hwnd);
			return true;

		}, Pointer.NULL);

		return results;
	}
}