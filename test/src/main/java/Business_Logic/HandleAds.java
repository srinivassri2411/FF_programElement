package Business_Logic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class HandleAds implements Nlp {

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			handleAds(nlpRequestModel.getAndroidDriver(), nlpRequestModel.getIosDriver());
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Sucessfully Handled the Ads");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to Handle the Ads" + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void handleAds(AndroidDriver anddriver, IOSDriver iosdriver) {

		try {

			if (anddriver != null) {

				handleAndroidAds(anddriver);

			} else if (iosdriver != null) {

				handleIOSAds(iosdriver);
			}

		} catch (Exception e) {

			throw e;
		}
	}

	private static void handleAndroidAds(AppiumDriver driver) {

		try {

			String activity = "";

			try {

				activity = ((AndroidDriver) driver).currentActivity();

			} catch (Exception ignored) {
			}

			if (activity != null
					&& (activity.contains("AdActivity") || activity.contains("ads") || activity.contains("Ads")
							|| activity.contains("FullscreenActivity") || activity.contains("Interstitial"))) {

				System.out.println("ANDROID AD DETECTED");

				if (clickAdCloseButton(driver)) {

					System.out.println("Android ad closed");

					return;
				}

				driver.navigate().back();
			}

		} catch (Exception ignored) {
		}
	}

	private static void handleIOSAds(AppiumDriver driver) {

		try {
			String pageSource = driver.getPageSource().toLowerCase();
			if (pageSource.contains("cross")) {
				if (clickAdCloseButtonIos(driver)) {

					System.out.println("iOS ad closed");

					return;
				}
			}
			if (pageSource.contains("close advertisement")) {
				closeAdMultipleTime(driver);
				return;
			}
		} catch (Exception ignored) {
		}
	}

	private static void closeAdMultipleTime(AppiumDriver driver) throws InterruptedException {

		for (int i = 0; i <= 4; i++) {
			try {
				String pageSource = driver.getPageSource().toLowerCase();
				if (pageSource.contains("close advertisement")) {
					clickAdCloseButtonIos(driver);
				} else {
					break;
				}
			} catch (Exception e) {

			}
			Thread.sleep(1500);
		}
	}

	private static boolean clickAdCloseButtonIos(AppiumDriver driver) {

		List<By> locators = Arrays.asList(

				// Android
				AppiumBy.xpath("//*[(contains(@value,'Close') " + "or contains(@name,'Close') "
						+ "or contains(@label,'Close') " + "or contains(@text,'Skip') "
						+ "or contains(@text,'Dismiss') " + "or contains(@content-desc,'Close') "
						+ "or contains(@content-desc,'Skip') "
						+ "or contains(@content-desc,'Dismiss')) and @enabled='true']")

		);

		for (By locator : locators) {

			try {

				List<WebElement> elements = driver.findElements(locator);

				if (!elements.isEmpty()) {

					WebElement element = elements.get(0);

					if (element.isDisplayed()) {

						System.out.println("Found Ad Close Button");

						element.click();

						return true;
					}
				}

			} catch (Exception ignored) {
			}
		}

		return false;
	}

	private static boolean clickAdCloseButton(AppiumDriver driver) {

		List<By> locators = Arrays.asList(

				// Android
				AppiumBy.xpath("//*[contains(@text,'Close') " + "or @name='Cross' " + "or @name='Close Advertisement' "
						+ "or contains(@text,'Skip') " + "or contains(@text,'Dismiss') "
						+ "or contains(@content-desc,'Close') " + "or contains(@content-desc,'Skip') "
						+ "or contains(@content-desc,'Dismiss')]"),
				AppiumBy.accessibilityId("Close"), AppiumBy.accessibilityId("Close ad"),
				AppiumBy.accessibilityId("Skip"),

				// iOS

				AppiumBy.iOSNsPredicateString("label CONTAINS 'Close'"),

				AppiumBy.iOSNsPredicateString("name CONTAINS 'Close'"),

				AppiumBy.iOSNsPredicateString("label CONTAINS 'Skip'"),

				AppiumBy.iOSNsPredicateString("name CONTAINS 'Skip'"),

				AppiumBy.iOSNsPredicateString("label CONTAINS 'Dismiss'"),

				AppiumBy.iOSNsPredicateString("name CONTAINS 'Dismiss'"),

				AppiumBy.xpath("//XCUIElementTypeButton[contains(@name,'Close')]"),

				AppiumBy.xpath("//XCUIElementTypeButton[contains(@name,'Skip')]")

		);

		for (By locator : locators) {

			try {

				List<WebElement> elements = driver.findElements(locator);

				if (!elements.isEmpty()) {

					WebElement element = elements.get(0);

					if (element.isDisplayed()) {

						System.out.println("Found Ad Close Button");

						element.click();

						return true;
					}
				}

			} catch (Exception ignored) {
			}
		}

		return false;
	}

	public static void main(String[] args) throws NlpException, MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:platformName", "iOS");
		capabilities.setCapability("appium:platformVersion", "17.3.1");
		capabilities.setCapability("appium:deviceName", "iPhone test1");
		capabilities.setCapability("appium:udid", "00008110-001835823407801E");
		capabilities.setCapability("appium:automationName", "XCUITest");
//        capabilities.setCapability("appium:bundleId", "com.india.bb.test");

		// Set paths if necessary, though usually Appium can find them automatically
		capabilities.setCapability("appium:agentPath",
				"/Applications/FireFlinkClient.app/Contents/Resources/flinko-client/exec/appium-webdriveragent/WebDriverAgent.xcodeproj");
		capabilities.setCapability("appium:bootstrapPath",
				"/Applications/FireFlinkClient.app/Contents/Resources/flinko-client/exec/appium-webdriveragent");
//        capabilities.setCapability("driver.disableWindowAnimation", true);

		// Initialize the driver
		IOSDriver driver = new IOSDriver(new URL("http://localhost:4723/wd/hub"), capabilities);
		System.out.println("Started");
		new Scanner(System.in).next();
		NlpRequestModel model = new NlpRequestModel();
		model.setIosDriver(driver);
		System.out.println(new HandleAds().execute(model));
	}
}