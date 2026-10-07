package sonicWall;

import java.net.URL;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v85.emulation.Emulation;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteMockLocation {
	public static void main(String[] args) throws Exception {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--use-fake-ui-for-media-stream");

		URL gridUrl = new URL("http://localhost:4444/wd/hub"); // Your Grid/Remote Node
		WebDriver driver = new RemoteWebDriver(gridUrl, options);
		driver = new Augmenter().augment(driver);
		// Access DevTools via RemoteWebDriver
		DevTools devTools = ((HasDevTools) driver).getDevTools();
		devTools.createSession();

		double latitude = 12.9716;
		double longitude = 77.5946;
		double accuracy = 100;

		devTools.send(
				Emulation.setGeolocationOverride(Optional.of(latitude), Optional.of(longitude), Optional.of(accuracy)));

		driver.get("https://my-location.org/");
	}
}
