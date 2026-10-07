package browserStackIssues;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class NEwTesr2 {
	public static void main(String[] args) throws MalformedURLException, InterruptedException {
		MutableCapabilities capabilities = new MutableCapabilities();
//		HashMap<String, Object> bs/stackOptions = new HashMap<String, Object>();
		capabilities.setCapability("browserName", "Chrome");
		HashMap<String, Object> bstackOptions = new HashMap<>();
		bstackOptions.put("userName", "srinivasm_ayOX0t");
		bstackOptions.put("accessKey", "aqxUhubpGd9Cq9945Tmg");
//		bstackOptions.put("browserstackSDK", "testng-javaagent/1.33.0");
//		bstackOptions.put("buildName", "browserstack build #3");
		bstackOptions.put("debug", false);
		bstackOptions.put("os", "Windows");
//		bstackOptions.put("source", "java:eclipse:v1.0.10");
//		bstackOptions.put("buildIdentifier", "#3");
		bstackOptions.put("consoleLogs", "errors");
		bstackOptions.put("networkLogs", false);
		bstackOptions.put("osVersion", "10");
//		bstackOptions.put("browser", "Chrome");
//		bstackOptions.put("browserVersion", "latest");
//		bstackOptions.put("browserName", "Chrome");
//		bstackOptions.put("projectName", "TestNGProjects123");
//		bstackOptions.put("useW3C", true);
		bstackOptions.put("accessibility", true);
		capabilities.setCapability("bstack:options", bstackOptions);
		WebDriver driver = new RemoteWebDriver(new URL("https://hub.browserstack.com/wd/hub"), capabilities);
		driver.get("https://www.browserstack.com/docs/automate/capabilities");
		Thread.sleep(3000);
		driver.quit();
	}
}
