package mweb;

import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.By;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

public class AndroidChromeTest {

	public static void main(String[] args) throws Exception {

		UiAutomator2Options options = new UiAutomator2Options();

		options.setPlatformName("Android");
		options.setDeviceName("RZ8TC02ECPE");
		options.withBrowserName("Chrome");

		options.setCapability("appium:chromedriverAutodownload", true);

		AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

		System.out.println("Chrome launched successfully");
		driver.get("https://shop.qweendom.in/");
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.findElement(By.xpath("/html/body/div[1]/nav/div/div[2]/div[2]")).click();
		Thread.sleep(5000);
		driver.quit();
	}
}