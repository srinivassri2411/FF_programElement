package testPack;

import java.io.File;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;

import io.github.selcukes.commons.Await;
import io.github.selcukes.commons.os.Platform;
import io.github.selcukes.video.Recorder;
import io.github.selcukes.video.RecorderFactory;
import io.github.selcukes.video.enums.RecorderType;

public class VideoTest {
	public static void main(String[] args) throws InterruptedException {

		if (Platform.isWindows()) {
			Recorder recorder = RecorderFactory.getRecorder(RecorderType.MONTE);
			recorder.start();
			WebDriver driver = new ChromeDriver();
			driver.get("https://www.fireflink.com");
			Thread.sleep(Duration.ofMinutes(5));
//			Await.until(5);
			File file = recorder.stopAndSave("test");
			Assert.assertTrue(file.getAbsolutePath().contains("mp4"));
		}

	}
}