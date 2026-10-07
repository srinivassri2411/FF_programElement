package default_package;
 
import java.time.Duration;
import java.util.List;
 
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
 
public class scrollUntilElement {
 
    public static void main(String[] args) throws Exception {
 
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
 
        driver.get("https://account-uat.kldx.com");
 
        driver.findElement(By.name("username")).sendKeys("syfqh.e002+10@gmail.com");
        driver.findElement(By.name("password")).sendKeys("Password99!");
        driver.findElement(By.xpath("//button[text()='Log In']")).click();
 
        driver.findElement(By.xpath("//span[text()='Platforms']")).click();
        driver.findElement(By.xpath("//span[text()='Primary Listing']")).click();
 
        JavascriptExecutor js = (JavascriptExecutor) driver;
 
        String targetXpath =
                "//*[contains(@class,'inline-block rounded')]//"
              + "*[@color='white'][normalize-space()='LITEST011']"
              + "/ancestor::*[3]//button[normalize-space()='INVEST NOW']";
 
        boolean found = false;
 
        for (int i = 0; i < 25; i++) {
 
            List<WebElement> btn = driver.findElements(By.xpath(targetXpath));
 
            if (!btn.isEmpty()) {
                System.out.println("Button found!");
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn.get(0));
                found = true;
                break;
            }
 
            // scroll page
            js.executeScript("window.scrollBy(0,800)");
            Thread.sleep(1500);
 
            System.out.println("Scroll attempt: " + i);
        }
 
        if (!found) {
            System.out.println("Element not found after scrolling");
        }
    }
}