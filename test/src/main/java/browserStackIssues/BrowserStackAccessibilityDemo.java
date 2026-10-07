package browserStackIssues;

import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BrowserStackAccessibilityDemo {

	// ================ BrowserStack Credentials ===================
	private static final String USERNAME = "srinivasm_ayOX0t";
	private static final String ACCESS_KEY = "aqxUhubpGd9Cq9945Tmg";

	// =============================================================

	public static void main(String[] args) {

		WebDriver driver = null;

		try {

			ChromeOptions options = new ChromeOptions();

			// ---------------- BrowserStack Options ----------------

			Map<String, Object> bstackOptions = new HashMap<>();

			bstackOptions.put("userName", USERNAME);
			bstackOptions.put("accessKey", ACCESS_KEY);

			bstackOptions.put("os", "Windows");
			bstackOptions.put("osVersion", "11");

			bstackOptions.put("projectName", "Accessibility Demo");
			bstackOptions.put("buildName", "Build-001");
			bstackOptions.put("sessionName", "Accessibility Scan");

			options.setCapability("bstack:options", bstackOptions);

			// ---------------- Browser ----------------

			options.setBrowserVersion("latest");

			// ---------------- Accessibility ----------------

//			options.setCapability("browserstack.accessibility", true);

			Map<String, Object> accessibilityOptions = new HashMap<>();

			accessibilityOptions.put("wcagVersion", "wcag21aa");

//			options.setCapability("browserstack.accessibilityOptions", accessibilityOptions);

			// ---------------- Create Driver ----------------

			driver = new RemoteWebDriver(new URL("https://hub.browserstack.com/wd/hub"), options);

			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			// ---------------- Test ----------------

			driver.get("https://www.wikipedia.org");

			Thread.sleep(3000);

			driver.findElement(By.id("searchInput")).sendKeys("BrowserStack");

			Thread.sleep(2000);

			driver.findElement(By.cssSelector("button[type='submit']")).click();

			Thread.sleep(5000);

			System.out.println("Test Executed Successfully.");

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			if (driver != null) {
				driver.quit();
			}

		}

	}
}