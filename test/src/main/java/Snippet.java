import java.time.Duration;
import java.time.LocalTime;
import java.util.Scanner;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.sukgu.Shadow;

public class Snippet {
	public static void main(String[] args) throws InterruptedException {
		WebDriver d = new ChromeDriver();
		d.manage().window().maximize();
		d.get("https://demoapps.qspiders.com/ui/shadow?sublist=0");

		System.out.println("Log in and open PDF. Press ENTER when ready...");
		new Scanner(System.in).nextLine();
//		WebElement var = findShadowElement("//input[@placeholder='Enter your username2']", d, new Shadow(d));
//		System.out.println(var);
//		if (var != null) {
//			System.out.println("ddssafafa");
//		} else {
//			System.out.println("hhjjjj");
//		}\
		System.out.println(LocalTime.now());
		findElement(d, "20", "//input[@placeholder='Enter your username2']");
		System.out.println(LocalTime.now());
	}

	public static WebElement findShadowElement(String xpath, WebDriver driver, Shadow shadow) {
		return shadow.findElementByXPath(xpath);
	}

	public static void findElement(WebDriver driver, String wait, String eles) {
		Shadow shadow = new Shadow(driver);
		boolean success = false;
		Duration impli = driver.manage().timeouts().getImplicitWaitTimeout();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		int count = 0;
		while (true) {
			System.out.println(count);
			try {
				if (count == Integer.parseInt(wait)) {
					success = false;
					break;
				}
				WebElement element = findShadowElement(eles, driver, shadow);
				if (element == null) {
					count++;
				} else {
					success = true;
					break;
				}
			} catch (Exception e) {
				System.out.println("Retry-able error: " + e.getMessage());
				count++;
			}
		}

		if (success) {
			System.out.println("Successfully waited until element is displayed");
		} else {
			System.out.println("Failed locator not working");
		}
	}
}