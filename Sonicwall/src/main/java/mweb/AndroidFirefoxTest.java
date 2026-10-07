package mweb;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.options.BaseOptions;

public class AndroidFirefoxTest {

	public static void main(String[] args) throws Exception {

		BaseOptions options = new BaseOptions();

		options.setCapability("platformName", "Windows");
		options.setCapability("appium:automationName", "Gecko");
		options.setCapability("appium:deviceName", "RZ8TC02ECPE");
		options.setCapability("browserName", "Firefox");

		Map<String, Object> firefoxOptions = new HashMap<>();
		firefoxOptions.put("androidPackage", "org.mozilla.firefox");
//		firefoxOptions.put("androidActivity", "org.mozilla.fenix.HomeActivity");
		firefoxOptions.put("androidDeviceSerial", "RZ8TC02ECPE");
//		firefoxOptions.put("noReset", true);

		options.setCapability("moz:firefoxOptions", firefoxOptions);

		AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

		System.out.println("Firefox launched successfully");

		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability("appium:platformName", "android");
		cap.setCapability("appium:automationName", "UiAutomator2");
//		cap.setCapability("appium:platformName", "");
		AndroidDriver driver2 = new AndroidDriver(new URL("http://localhost:4723"), cap);
		driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver2.findElement(By.xpath("//android.widget.TextView[@text=\"Continue\"]")).click();
		driver2.findElement(By.xpath("//android.widget.Button[@text=\"Cancel\"]")).click();
		driver2.findElement(By.xpath("//android.widget.TextView[@text=\"Not now\"]")).click();
		driver2.findElement(By.xpath("//android.widget.TextView[@text=\"Continue\"]")).click();
		driver2.findElement(By.xpath("//android.view.View[@resource-id=\"recent.tabs\"]")).click();
		driver2.quit();
//		System.out.println(driver.getContext());
//		System.out.println(driver.getContextHandles());
//		driver.context("NATIVE");
//		System.out.println(driver.getPageSource());
//		new Scanner(System.in).next();

		System.out.println("Before navigation:");
		System.out.println("URL: " + driver.getCurrentUrl());
		System.out.println("Title: " + driver.getTitle());

		System.out.println("Navigating...");

		driver.get("https://shop.qweendom.in/");
//		driver.get("https://www.gmail.com/");

//		Thread.sleep(5000);

		System.out.println("After navigation:");
		System.out.println("URL: " + driver.getCurrentUrl());
		System.out.println("Title: " + driver.getTitle());

//		new Scanner(System.in).next();
		WebElement element = driver.findElement(By.xpath("/html/body/div[1]/nav/div/div[2]/div[2]"));

		System.out.println("Tag: " + element.getTagName());
		System.out.println("Class: " + element.getAttribute("class"));
		System.out.println("Text: " + element.getText());
		System.out.println("OuterHTML: " + element.getAttribute("outerHTML"));
		element.click();
		System.out.println("clicked");
		new Scanner(System.in).next();
		driver.quit();
	}
}
