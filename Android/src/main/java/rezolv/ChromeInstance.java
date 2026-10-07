package rezolv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ChromeInstance implements Nlp {
	// int port;

	@InputParams({ @InputParam(name = "Port", type = "java.lang.Integer") })
	@ReturnType(name = "isActive", type = "java.lang.Integer")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		// Chrome c=new Chrome();
		int port = (Integer) programElementsInput.get("Port");
		// Integer number2 = (Integer) programElementsInput.get("Number 2");
		// Integer returnValue=null;
		WebDriver driver = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (port == 0) {
				port = new Random().nextInt(10000);
				launchChrome(port);
				Thread.sleep(3000);
				driver = chromeConnector(port, nlpResponseModel);
				driver.manage().window().maximize();

			} else {
				System.out.println("else");
				// launchChrome(port);
				// Thread.sleep(3000);
				driver = chromeConnector(port, nlpResponseModel);
				driver.manage().window().maximize();
				driver.get("https://chatgpt.com/?model=auto");
			}
			// if (!isChromeRunningOnDebugPort(port)) {
			// launchChrome();
			// Thread.sleep(3000);
			// chromeConnector();
			// System.out.println("Chrome launched.");
			// } else {
			// System.out.println("Chrome already running on port 9222.");
			// }
			nlpResponseModel.setWebDriver(driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
			// Uncomment the below line and modify the message, only if you want to override
			// the pass message defined for the program element.
			nlpResponseModel.setMessage("Sucessfully Launched the browser with" + port);

			// set the list of local file absolute paths (if any) to be uploaded to server
			// which can later be downloaded from the step result section.
			// nlpResponseModel.setOutputLocalFilePaths(List.of("/absolute/file/path1","/absolute/file/path2"));
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

			// Uncomment the below line and modify the message, only if you want to override
			// the fail message defined for the program element.
			nlpResponseModel.setMessage("Failed to Launch " + e);
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("isActive", port);
		return nlpResponseModel;
	}

	public static boolean isChromeRunningOnDebugPort(int port) {
		try {
			URL url = new URL("http://localhost:" + port + "/json/version");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(1000); // 1 second timeout
			connection.connect();
			int code = connection.getResponseCode();
			return (code == 200);
		} catch (IOException e) {
			return false;
		}
	}

	/*
	 * public static void launchChrome(int port) throws IOException { ProcessBuilder
	 * builder = new ProcessBuilder( "cmd.exe", "/c",
	 * "\"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\" " +
	 * "--remote-debugging-port=" + port + " " +
	 * "--user-data-dir=C:\\chrome-debug-profile " +
	 * "--profile-directory=\"Default\" " + "--no-first-run " +
	 * "--no-default-browser-check" );
	 *
	 * // ProcessBuilder builder = new ProcessBuilder( // "cmd.exe", "/c", //
	 * "\"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\" ^\r\n" // +
	 * "  --remote-debugging-port=9222 ^\r\n" // +
	 * "  --user-data-dir=\"C:\\chrome-dev-profile\"\r\n" // +
	 * "--force-device-scale-factor=0.75" // ); builder.start(); }
	 */

	public static void launchChrome(int port) throws IOException {
		String userDataDir = "C:\\Chrome_Data_" + port; // unique per port

		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"\"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\" " + "--remote-debugging-port=" + port
						+ " " + "--guest");
//						+ " " + "--user-data-dir=" + userDataDir + " " + "--profile-directory=\"Default\" "
//						+ "--no-first-run " + "--no-default-browser-check " + "--window-size=1280,720 "
//						+ "--force-device-scale-factor=1 " + "--disable-password-manager " + "--password-store=basic "
//						+ "--disable-save-password-bubble");
		builder.start();

	}

	public static WebDriver chromeConnector(int port, NlpResponseModel nlpResponseModel)
			throws MalformedURLException, InterruptedException {
		DesiredCapabilities cap = new DesiredCapabilities();

		ChromeOptions opt = new ChromeOptions();

//		Map<String, Object> prefs = new HashMap<>();
//
//		prefs.put("credentials_enable_service", false);
//		prefs.put("profile.password_manager_enabled", false);
//		prefs.put("profile.password_manager_leak_detection", false);
//		prefs.put("autofill.profile_enabled", false);
//		opt.setExperimentalOption("prefs", prefs);

		opt.setExperimentalOption("debuggerAddress", "localhost:" + port);
		System.out.println(port);
		// cap.setCapability("debuggerAddress", "localhost:"+port+"");
		cap.merge(opt);
		WebDriver driver = new ChromeDriver(opt);
		// nlpResponseModel.setWebDriver(driver);
		// driver.get("https://mail.google.com/mail/u/0/?tab=rm&ogbl#inbox");
		// Thread.sleep(3000);
		// System.out.println(driver.getCurrentUrl());
		return driver;

		// WebDriver driver = new RemoteWebDriver(new URL("http://localhost:9515"),
		// opt);

		// driver.get("https://www.fireflink.com/");
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpResponseModel = new NlpRequestModel();
		Map<String, Object> programElementsInput = nlpResponseModel.getAttributes();
		programElementsInput.put("Port", 7674);
		new ChromeInstance().execute(nlpResponseModel);
//		programElementsInput.put("", "");

	}

}
