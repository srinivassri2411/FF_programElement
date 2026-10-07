package browserStackIssues;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class NEwTesr {
	public static void main(String[] args) throws MalformedURLException, InterruptedException {
		MutableCapabilities capabilities = new MutableCapabilities();
		HashMap<String, Object> bstackOptions = new HashMap<String, Object>();
		capabilities.setCapability("browserName", "Chrome");
		bstackOptions.put("os", "Windows");
		bstackOptions.put("osVersion", "10");
		bstackOptions.put("browserVersion", "120.0");
		bstackOptions.put("userName", "srinivasm_ayOX0t");
		bstackOptions.put("accessKey", "aqxUhubpGd9Cq9945Tmg");
		bstackOptions.put("consoleLogs", "info");
//		bstackOptions.put("browserstack.accessibility", true);
//		Map<String, Object> accessibilityOptions = new HashMap<>();
//		accessibilityOptions.put("wcagVersion", "wcag21aa");
//		bstackOptions.put("browserstack.accessibilityOptions", accessibilityOptions);
		capabilities.setCapability("bstack:options", bstackOptions);
		WebDriver driver = new RemoteWebDriver(new URL("https://hub.browserstack.com/wd/hub"), capabilities);
		driver.get("https://www.browserstack.com/docs/automate/capabilities");
		Thread.sleep(3000);
		driver.quit();
	}
}
