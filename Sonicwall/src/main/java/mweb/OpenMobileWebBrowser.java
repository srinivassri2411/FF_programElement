
package mweb;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.options.BaseOptions;

public class OpenMobileWebBrowser implements Nlp {
	private static final String EDGE_PACKAGE = "com.microsoft.emmx";
	private static final String EDGE_ACTIVITY = "org.chromium.chrome.browser.ChromeTabbedActivity";

	@InputParams({ @InputParam(name = "Browser Name", type = "java.lang.String"),
			@InputParam(name = "Mob_HubURL", type = "java.lang.String"),
			@InputParam(name = "Device UDID", type = "java.lang.String"),
			@InputParam(name = "URL", type = "java.lang.String"),
			@InputParam(name = "Implicit timeout", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String browserName = (String) programElementsInput.get("Browser Name");
		String deviceUDID = (String) programElementsInput.get("Device UDID");
		String mob_HubURL = (String) programElementsInput.get("Mob_HubURL");
		String url = (String) programElementsInput.get("URL");
		String impli = (String) programElementsInput.get("Implicit timeout");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			nlpResponseModel.setAndroidDriver(
					openApp(browserName, deviceUDID, url, mob_HubURL, Duration.ofSeconds(Long.parseLong(impli))));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage(browserName + " browser is successfully launched");
		} catch (Exception e) {
			nlpResponseModel.setMessage(browserName + " browser is failed launch--> " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	@SuppressWarnings("deprecation")
	private AndroidDriver openApp(String browserName, String udid, String url, String hubURL, Duration impli)
			throws Exception {
		if (browserName.toLowerCase().contains("chrome")) {
			UiAutomator2Options options = new UiAutomator2Options();
			options.setPlatformName("Android");
			options.setDeviceName(udid);
			options.withBrowserName("Chrome");
			options.setCapability("appium:chromedriverAutodownload", true);
			AndroidDriver driver = new AndroidDriver(new URL(hubURL), options);
			driver.get(url);
			driver.manage().timeouts().implicitlyWait(impli);
			return driver;
		} else if (browserName.toLowerCase().contains("edge")) {
			String DEVICE_ID = udid;
			run("adb", "-s", DEVICE_ID, "shell", "am", "force-stop", EDGE_PACKAGE);
			Thread.sleep(2000);
			run("adb", "-s", DEVICE_ID, "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", url,
					"-p", EDGE_PACKAGE);
			Thread.sleep(6000);
			Map<String, Object> chromeOptions = new HashMap<>();
			chromeOptions.put("androidPackage", EDGE_PACKAGE);
			chromeOptions.put("androidActivity", EDGE_ACTIVITY);
			chromeOptions.put("androidDeviceSerial", DEVICE_ID);
			chromeOptions.put("androidDeviceSocket", "chrome_devtools_remote");
			chromeOptions.put("androidExecName", "chrome");
			chromeOptions.put("androidUseRunningApp", true);
			UiAutomator2Options options = new UiAutomator2Options();
			options.setPlatformName("Android");
			options.setAutomationName("UiAutomator2");
			options.setDeviceName(DEVICE_ID);
			options.setUdid(DEVICE_ID);
			options.setCapability("appium:appPackage", EDGE_PACKAGE);
			options.setCapability("appium:appWaitPackage", EDGE_PACKAGE);
			options.setCapability("appium:appWaitActivity", EDGE_ACTIVITY);
			options.setCapability("appium:autoLaunch", false);
			options.setCapability("appium:noReset", true);
			options.setCapability("appium:dontStopAppOnReset", true);
			options.setCapability("appium:androidDeviceSocket", "chrome_devtools_remote");
			options.setCapability("appium:chromedriverAutodownload", true);
			options.setCapability("appium:chromedriverDisableBuildCheck", true);
			options.setCapability("appium:chromeOptions", chromeOptions);
			options.setCapability("appium:waitForWebviewMs", 30000);
			options.setCapability("appium:showChromedriverLog", true);
			options.setCapability("appium:enableWebviewDetailsCollection", true);
			AndroidDriver driver = new AndroidDriver(new URL(hubURL), options);
			driver.manage().timeouts().implicitlyWait(impli);
			driver.context("CHROMIUM");
			return driver;
		} else if (browserName.toLowerCase().contains("fire")) {
			BaseOptions<?> options = new BaseOptions<>();
			options.setCapability("platformName", "Windows");
			options.setCapability("appium:automationName", "Gecko");
			options.setCapability("appium:deviceName", udid);
			options.setCapability("browserName", "Firefox");
			Map<String, Object> firefoxOptions = new HashMap<>();
			firefoxOptions.put("androidPackage", "org.mozilla.firefox");
			firefoxOptions.put("androidDeviceSerial", udid);
			options.setCapability("moz:firefoxOptions", firefoxOptions);
			AndroidDriver driver = new AndroidDriver(new URL(hubURL), options);
			driver.manage().timeouts().implicitlyWait(impli);
			DesiredCapabilities cap = new DesiredCapabilities();
			cap.setCapability("appium:platformName", "android");
			cap.setCapability("appium:automationName", "UiAutomator2");
			AndroidDriver driver2 = new AndroidDriver(new URL(hubURL), cap);
			driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			driver2.findElement(By.xpath("//android.widget.TextView[@text=\"Continue\"]")).click();
			driver2.findElement(By.xpath("//android.widget.Button[@text=\"Cancel\"]")).click();
			driver2.findElement(By.xpath("//android.widget.TextView[@text=\"Not now\"]")).click();
			driver2.findElement(By.xpath("//android.widget.TextView[@text=\"Continue\"]")).click();
			driver2.findElement(By.xpath("//android.view.View[@resource-id=\"recent.tabs\"]")).click();
			driver2.quit();
			Thread.sleep(1000);
			driver.get(url);
			return driver;
		} else {
			return null;
		}
	}

	private static void run(String... command) throws Exception {

		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

		String output = new String(process.getInputStream().readAllBytes());

		int exitCode = process.waitFor();

		if (!output.isBlank()) {
			System.out.println(output.trim());
		}

		if (exitCode != 0) {
			throw new RuntimeException("Command failed: " + String.join(" ", command));
		}
	}

	public static void main(String[] args) throws NlpException, InterruptedException {
		AndroidDriver driver = null;
		try {
			NlpRequestModel model = new NlpRequestModel();
//			model.getAttributes().put("Browser Name", "Google Chrome");
			model.getAttributes().put("Browser Name", "FireFox");
//			model.getAttributes().put("Browser Name", "Microsoft edge");
			model.getAttributes().put("Mob_HubURL", "http://localhost:4723/wd/hub");
			model.getAttributes().put("Device UDID", "RZ8TC02ECPE");
			model.getAttributes().put("Implicit timeout", "20");
			model.getAttributes().put("URL", "https://shop.qweendom.in/");
			NlpResponseModel details = new OpenMobileWebBrowser().execute(model);
			System.out.println(details);
			driver = details.getAndroidDriver();
			System.out.println(details.getAndroidDriver().manage().timeouts().getImplicitWaitTimeout());
			details.getAndroidDriver().findElement(By.xpath("/html/body/div[1]/nav/div/div[2]/div[2]")).click();
			Thread.sleep(4000);
			System.out.println("Click completed");
		} finally {
			driver.quit();
		}
		System.out.println("Finished");
	}
}