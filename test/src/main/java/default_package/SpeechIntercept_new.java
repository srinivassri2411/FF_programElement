package default_package;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SpeechIntercept_new {

	private WebDriver driver;
	private JavascriptExecutor js;

	// Java-side storage
	private Map<Integer, String> speechMap = new LinkedHashMap<>();

	public SpeechIntercept_new() {
		ChromeOptions options = new ChromeOptions();

		options.addArguments("--use-fake-ui-for-media-stream");
		options.addArguments("--use-fake-device-for-media-stream");
		options.addArguments("--use-file-for-fake-audio-capture=C:\\Users\\User\\Downloads\\Test.wav");
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		js = (JavascriptExecutor) driver;
	}

	public void open(String url) {
		driver.get(url);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		driver.findElement(By.xpath("//button[text()='Login']")).click();
		driver.findElement(By.xpath("//*[@id='email']")).sendKeys("dummy@yopmail.com");
		driver.findElement(By.xpath("//input[@type='password']")).sendKeys("Test@1234");
		driver.findElement(By.xpath("//button[text()='Login']")).click();
		new Scanner(System.in).nextLine();
	}

	// Inject the speech interception script
	public void enableSpeechInterception() {
		String script = """
				(() => {
				    if (!window.botSpeechLog) {
				        window.botSpeechLog = [];

				        const originalSpeak = window.speechSynthesis.speak;

				        window.speechSynthesis.speak = function (utterance) {
				            window.botSpeechLog.push({
				                text: utterance.text,
				                timestamp: new Date().toISOString()
				            });

				            console.log("Bot said:", utterance.text);
				            return originalSpeak.call(this, utterance);
				        };

				        console.log("Speech interception enabled.");
				    }
				})();
				""";

		js.executeScript(script);
	}

	public void speak(String text) {
		String injectScript = """
				const speakText = arguments[0];

				window.speechSynthesis.cancel();

				const utterance = new SpeechSynthesisUtterance(speakText);

				// Slow and clear speech
				utterance.rate = 0.55;
				utterance.pitch = 1.0;
				utterance.volume = 1.0;

				const voices = window.speechSynthesis.getVoices();

				utterance.voice =
				    voices.find(v => v.name === 'Google UK English Female') ||
				    voices.find(v => v.name === 'Microsoft Zira - English (United States)') ||
				    voices.find(v => v.name === 'Google US English') ||
				    voices[0];

				window.speechSynthesis.speak(utterance);
				""";

		js.executeScript(injectScript, text);
	}

	// Fetch all captured speech into Java Map
	public void refreshSpeechMap() {
		Object result = js.executeScript("return window.botSpeechLog || [];");

		speechMap.clear();

		if (result instanceof List<?> list) {
			int index = 1;
			for (Object item : list) {
				if (item instanceof Map<?, ?> map) {
					String text = String.valueOf(map.get("text"));
					speechMap.put(index++, text);
				}
			}
		}
	}

	public void clearSpeechMap() {
		js.executeScript("window.botSpeechLog=[];");
		refreshSpeechMap();
	}

	// Get the stored map
	public Map<Integer, String> getSpeechMap() {
		return speechMap;
	}

	// Get a single speech entry
	public String getSpeech(int index) {
		return speechMap.get(index);
	}

	public void close() {
		driver.quit();
	}

	public static void main(String[] args) throws InterruptedException {
		SpeechIntercept_new interceptor = new SpeechIntercept_new();

		interceptor.open("https://envision.webcluesstaging.com/");
//		interceptor.enableSpeechInterception();

//        new Scanner(System.in).nextLine();
//		interceptor.speak("Hi Robert How to create project?");
//		interceptor.refreshSpeechMap();
//		System.out.println("Captured speeches:");
//		System.out.println(interceptor.getSpeechMap());
//        interceptor.clearSpeechMap();
//        System.out.println("Captured speeches after clearing:");
//        System.out.println(interceptor.getSpeechMap());

//		System.out.println("First speech: " + interceptor.getSpeech(1));

//        interceptor.close();
	}
}