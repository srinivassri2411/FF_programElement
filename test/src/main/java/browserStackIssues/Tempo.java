package browserStackIssues;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Tempo{

	public static void main(String[] args) throws MalformedURLException {
		
				ChromeOptions options = new ChromeOptions();
		
				// Request Chrome major version 140
				options.setBrowserVersion("140");
		
//				WebDriver driver = new ChromeDriver(options);
				WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
		
				try {
					driver.get("https://www.google.com");
		
					System.out.println("Requested Chrome : 140");
//					System.out.println("Actual Chrome    : " + driver.getCapabilities().getBrowserVersion());
		
				} finally {
//					driver.quit();
				}

		//-----------------------------------------------------------------
//				DesiredCapabilities capabilities = new DesiredCapabilities();
//
//				capabilities.setBrowserName("chrome");
//				capabilities.setVersion("140");
//
//				ChromeOptions options = new ChromeOptions();
//				options.merge(capabilities);
//
//				WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
			}
}
