package Windows;

import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;
import java.util.Collections;

public class RightClickOnElement {
	public static void main(String[] args) throws Exception {
		DesiredCapabilities dc = new DesiredCapabilities();
		dc.setCapability("appium:platformName", "Windows");
		dc.setCapability("appium:automationName", "Windows");
		dc.setCapability("appium:app", "Root");

		WebDriver driver = new WindowsDriver(new URL("http://localhost:4723/wd/hub"), dc);
		System.out.println("launched");
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
//        Name	Show Hidden Icons

		driver.findElement(By.name("Show Hidden Icons")).click();
		Thread.sleep(1500);
//        Name	 Cloud Secure Edge

//        WebElement element = driver.findElement(By.name("Cloud Secure Edge"));
		WebElement element = driver.findElement(By.xpath("//*[contains(@Name,'Cloud Secure Edge')]"));
		rightClick(driver, element);
		driver.findElement(By.name("Quit")).click();
	}

	public static void rightClick(WebDriver driver, WebElement element) {
		PointerInput mouse = new PointerInput(PointerInput.Kind.PEN, "pen");
		Sequence rightClick = new Sequence(mouse, 1);
		rightClick.addAction(
				mouse.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.fromElement(element), 0, 0));
		rightClick.addAction(mouse.createPointerDown(PointerInput.MouseButton.RIGHT.asArg()));
		rightClick.addAction(mouse.createPointerUp(PointerInput.MouseButton.RIGHT.asArg()));

		((WindowsDriver) driver).perform(Collections.singletonList(rightClick));
	}
}
