//import io.github.selcukes.video.Recorder;
//import io.github.selcukes.video.RecordingMode;
//import io.github.selcukes.video.SaveMode;
//import io.github.selcukes.video.VideoRecorder; // hypothetical classes
//import org.junit.jupiter.api.*;
//
//public class SampleTest {
//	@BeforeAll
//	public static void beforeAll() {
//		// init with default config or custom config
//		VideoRecorder.videoConfig().setVideoFolder("target/videos").enabled(true).recordMode(RecordingMode.ALL)
//				.saveMode(SaveMode.ALL).build();
//	}
//
//	@BeforeEach
//	public void setup(TestInfo testInfo) {
//		String testName = testInfo.getDisplayName();
//		VideoRecorder.start(testName);
//		// set up WebDriver after this or before, depending on your need
//	}
//
//	@Test
//	public void testGoogle() {
//		driver.get("https://www.google.com");
//		// do some actions
//		Assertions.assertTrue(driver.getTitle().contains("Google"));
//	}
//
//	@AfterEach
//	public void tearDown() {
//		VideoRecorder.stop();
//		// close driver
//	}
//}