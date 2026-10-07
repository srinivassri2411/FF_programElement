package Windows;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.windows.WindowsDriver;

public class openbanyan {
	public static void main(String[] args) throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:app",
				"C:\\Program Files\\SonicWall Cloud Secure Edge\\SonicWall Cloud Secure Edge.exe");
//		capabilities.setCapability("appium:app", "Root");
		capabilities.setCapability("appium:platformName", "Windows");
		capabilities.setCapability("appium:deviceName", "WindowsPC");
		capabilities.setCapability("appium:automationName", "Windows");
		WindowsDriver driver = new WindowsDriver(new URL("http://localhost:4723/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
	}
}
