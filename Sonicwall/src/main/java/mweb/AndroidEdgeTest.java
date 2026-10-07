package mweb;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

public class AndroidEdgeTest {

	private static final String DEVICE_ID = "RZ8TC02ECPE";
	private static final String EDGE_PACKAGE = "com.microsoft.emmx";
	private static final String EDGE_ACTIVITY = "org.chromium.chrome.browser.ChromeTabbedActivity";

	private static final String TEST_URL = "https://shop.qweendom.in/";

	public static void main(String[] args) throws Exception {

		AndroidDriver driver = null;

		try {

			System.out.println("Stopping Android Edge...");

			run("adb", "-s", DEVICE_ID, "shell", "am", "force-stop", EDGE_PACKAGE);

			Thread.sleep(2000);

			System.out.println("Launching Android Edge...");

			run("adb", "-s", DEVICE_ID, "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d",
					TEST_URL, "-p", EDGE_PACKAGE);

//			Thread.sleep(6000);

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

//			options.setCapability("appium:chromedriverExecutable", CHROMEDRIVER);
			options.setCapability("appium:chromedriverAutodownload", true);
			options.setCapability("appium:chromedriverDisableBuildCheck", true);

			options.setCapability("appium:chromeOptions", chromeOptions);

			options.setCapability("appium:waitForWebviewMs", 30000);

			options.setCapability("appium:showChromedriverLog", true);

			options.setCapability("appium:enableWebviewDetailsCollection", true);

			System.out.println("Creating Appium session...");

			driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

			System.out.println("Appium session created");

			System.out.println("Package: " + driver.getCurrentPackage());

			System.out.println("Activity: " + driver.currentActivity());

			System.out.println("Contexts: " + driver.getContextHandles());

			System.out.println("Switching to CHROMIUM...");

			driver.context("CHROMIUM");

			System.out.println("Web context connected");

			System.out.println("Current context: " + driver.getContext());

			System.out.println("Current URL: " + driver.getCurrentUrl());

			System.out.println("Title: " + driver.getTitle());

			System.out.println("Navigating...");

//			driver.get(TEST_URL);

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

			wait.until(ExpectedConditions.urlContains("shop.qweendom.in"));

			System.out.println("URL: " + driver.getCurrentUrl());

			System.out.println("Title: " + driver.getTitle());

			System.out.println("Android Edge web automation is working");

			wait.until(
					ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/nav/div/div[2]/div[2]")));

			driver.findElement(By.xpath("/html/body/div[1]/nav/div/div[2]/div[2]")).click();

			System.out.println("Click completed");

			Thread.sleep(5000);

		} finally {

			if (driver != null) {
				try {
					driver.quit();
					run("adb", "-s", DEVICE_ID, "shell", "am", "force-stop", EDGE_PACKAGE);
				} catch (Exception ignored) {
				}
			}

			System.out.println("Session closed");
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
}