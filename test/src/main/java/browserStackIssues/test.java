package browserStackIssues;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class test {
	public static void main(String[] args) throws Exception {
//		System.setProperty("webdriver.chrome.driver",
//				"C:\\Users\\User\\AppData\\Roaming\\fire-flink-client\\localnode\\chromedriver.exe");
		DesiredCapabilities cap = new DesiredCapabilities();
		ChromeOptions op = new ChromeOptions();
		op.setExperimentalOption("debuggerAddress", "localhost:2705");
		op.addArguments("--start-maximized");
		op.addArguments("--remote-allow-origins=*");
//		cap.merge(op);
//		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		WebDriver driver = new ChromeDriver(op);
		driver.get("http://localhost:4444/wd/hub");
	}
}
