package Mac;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

public class ZoomBrowser {
    public static void main(String[] args) {
        // Set the path to the ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");

        // Initialize the WebDriver (in this case, Chrome)
        WebDriver driver = new ChromeDriver();

        try {
            // Open a website (e.g., Google's homepage)
            driver.get("https://www.google.com");

            // Wait for the page to load completely
            Thread.sleep(2000);  // Simple delay for demo purposes, use WebDriverWait in production code

            // Create an Actions object to perform keyboard actions
            Actions actions = new Actions(driver);

            // Perform Zoom In (Command + '+' on macOS or Ctrl + '+' on Windows/Linux)
            // For macOS:
            actions.keyDown(Keys.COMMAND).sendKeys(Keys.ADD).keyUp(Keys.COMMAND).perform();
            // For Windows/Linux:
            // actions.keyDown(Keys.CONTROL).sendKeys(Keys.ADD).keyUp(Keys.CONTROL).perform();

            // Wait to observe the zoom
            Thread.sleep(2000);  // Wait 2 seconds to see the effect of zoom

            // Perform Zoom Out (Command + '-' on macOS or Ctrl + '-' on Windows/Linux)
            // For macOS:
            actions.keyDown(Keys.COMMAND).sendKeys(Keys.SUBTRACT).keyUp(Keys.COMMAND).perform();
            // For Windows/Linux:
            // actions.keyDown(Keys.CONTROL).sendKeys(Keys.SUBTRACT).keyUp(Keys.CONTROL).perform();

            // Wait to observe the zoom out
            Thread.sleep(2000);  // Wait 2 seconds to see the effect of zoom

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }
    }
}
