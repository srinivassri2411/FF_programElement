package testPack;

import java.time.Duration;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

import io.github.sukgu.Shadow;

public class FirefoxProfileTest {

	public static void main(String[] args) throws InterruptedException {

//        System.setProperty("webdriver.gecko.driver", "C:\\drivers\\geckodriver.exe");

		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("-profile");
		options.addArguments("C:\\Users\\User\\AppData\\Local\\Mozilla\\Firefox\\Profiles\\afo0wiw0.default-release");

		WebDriver driver = new FirefoxDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		driver.get("https://unittest.cliniops.com/investigator/configurestudy/icf-documents");

		driver.findElement(By.xpath("//input[@id='tmp_name']")).sendKeys("saibharadwaj");
		driver.findElement(By.xpath("//input[@id='tmp_pwd']")).sendKeys("Srisai@33");
		driver.findElement(By.xpath("//input[@id='authenticate']")).click();
		new Scanner(System.in).next();
		Thread.sleep(3000);
		driver.get("https://unittest.cliniops.com/investzigator/configurestudy/icf-documents");
		new Scanner(System.in).next();
		moveAndClickOnCentreOfElement(new Shadow(driver).findElementByXPath("//button[@data-name='sub_sig']"), driver);
	}

	public static void moveAndClickOnCentreOfElement(WebElement element, WebDriver driver) {
		Actions actions = new Actions(driver);
		actions.moveToElement(element).click().perform();
		// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
		// element);
	}

}