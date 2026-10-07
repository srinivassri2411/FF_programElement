
package Business_Logics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
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

public class EnableBitLocker implements Nlp {
	@InputParams({ @InputParam(name = "hubURL", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String hubURL = (String) programElementsInput.get("hubURL");
		String logFilePath = System.getProperty("user.home") + File.separator + "EnableBitLocker_Log.txt";
		String vbsFilePath = System.getProperty("user.home") + File.separator + "enableBitLocker.vbs";
		String batFilePath = System.getProperty("user.home") + File.separator + "enableBitLocker.bat";
		String result = null;
		try {
			if (!new File(batFilePath).exists() && !new File(vbsFilePath).exists())
				throw new FileNotFoundException("enableBitLocker.bat and enableBitLocker.vat file not exists "
						+ System.getProperty("user.home") + File.separator);
			String openControl = "control /name Microsoft.BitLockerDriveEncryption";
			Process openControlProcess = Runtime.getRuntime().exec(openControl);
			openControlProcess.waitFor();
			// log.info("BitLocker management window opened.");
			FileUtils.writeStringToFile(new File(logFilePath), "", "UTF-8");
			WebDriver driver = checkDriver(nlpRequestModel, hubURL);
			result = enableBitLocker(vbsFilePath, driver, logFilePath, hubURL);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Enabled bitlocker successfully");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to Enable bitlocker -> " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("result", result);
		return nlpResponseModel;
	}

	public WebDriver checkDriver(NlpRequestModel nlpRequestModel, String hubURL)
			throws MalformedURLException, InterruptedException {
		WebDriver driver = nlpRequestModel.getWindowsDriver();
		if (driver == null) {
			DesiredCapabilities capability = new DesiredCapabilities();
			capability.setCapability("appium:automationName", "windows");
			capability.setCapability("appium:platformName", "windows");
			capability.setCapability("appium:app", "Root");
			capability.setCapability("appium:newCommandTimeout", 0);
			driver = new WindowsDriver(new URL(hubURL), capability);
			// log.info("Created");
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		upFront();
		maximize();
		Thread.sleep(2500);
		return driver;
	}

	public String enableBitLocker(String vbsFilePath, WebDriver driver, String logFilePath, String hubURL)
			throws Exception {
		List<String> command = new ArrayList<>();
		command.add("cscript");
		command.add("//B");
		command.add(vbsFilePath);
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		int exitCode = process.waitFor();
		// log.info("Batch file execution completed with exit code: " + exitCode);
		Thread.sleep(2500);
		String result = FileUtils.readFileToString(new File(logFilePath), "UTF-8");
		// log.info(result);
		if (!result.contains("Key Protectors Added:")
				|| !driver.findElement(By.name("‭C:‬ BitLocker Encrypting")).isDisplayed()) {
			throw new RuntimeException("Enable Bitlocker failed check logs");
		}
		long startTime = System.currentTimeMillis();
		while (true) {
			// log.info("loop entered");
			try {
				if (driver.findElement(By.name("C: BitLocker on")).isDisplayed()) {
					// log.info("Element found pass");
					break;
				} else {
					// log.info("Still encrypting");
				}
			} catch (NoSuchElementException e) {
				// log.info("No SUch element");
			} catch (WebDriverException e) {
				DesiredCapabilities capability = new DesiredCapabilities();
				capability.setCapability("appium:automationName", "windows");
				capability.setCapability("appium:platformName", "windows");
				capability.setCapability("appium:app", "Root");
				capability.setCapability("appium:newCommandTimeout", 0);
				driver = new WindowsDriver(new URL(hubURL), capability);
				// log.info("ReCreated");
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
				upFront();
				maximize();
			}
			Thread.sleep(50000);
			if ((System.currentTimeMillis() - startTime) / 1000 > 4500) {
				throw new RuntimeException("Duration exceeded");
			}
			upFront();
			maximize();
		}
		upFront();
		maximize();
//		driver.findElement(By.name("Close")).click();
		closeWindow();
		return result;
	}

	public static void maximize() {
		String windowTitle = "BitLocker Drive Encryption";
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			System.out.println("Window not found: " + windowTitle);
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			System.out.println("Window maximized and brought to front: " + windowTitle);
		}
	}

	public static void upFront() {
		String windowTitle = "BitLocker Drive Encryption";

		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			System.out.println("Window not found: " + windowTitle);
		} else {
			User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);
			User32.INSTANCE.SetForegroundWindow(hwnd);
			System.out.println("Window brought to front: " + windowTitle);
		}
	}

	public static void closeWindow() {
		String windowTitle = "BitLocker Drive Encryption";
		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

		if (hwnd == null) {
			System.out.println("Window not found: " + windowTitle);
		} else {
			// Send WM_CLOSE message to the window
			User32.INSTANCE.PostMessage(hwnd, User32.WM_CLOSE, null, null);
			System.out.println("Close message sent to window: " + windowTitle);
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("hubURL", "http://localhost:4723/wd/hub");
		System.out.println(new EnableBitLocker().execute(model));
	}
}