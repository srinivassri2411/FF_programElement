
package Business_Logics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.windows.WindowsDriver;

public class openWindowsApplication implements Nlp {
	@InputParams({ @InputParam(name = "AppPath", type = "java.lang.String") })
	@ReturnType(name = "WindowsDriver", type = "io.appium.java_client.windows.WindowsDriver")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String AppPath = (String) programElementsInput.get("AppPath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		WindowsDriver driver = null;
		String processName = "banyan.exe";
		try {
			boolean isProcessRunning = killprocess(processName);

			if (!isProcessRunning) {
				TimeUnit.SECONDS.sleep(3);
				System.out.println(processName + " has been successfully terminated.");
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setCapability("appium:platformName", "Windows");
				capabilities.setCapability("appium:deviceName", "Windows");
				capabilities.setCapability("appium:newCommandTimeout", "3600");
				capabilities.setCapability("appium:app", "C:\\Program Files\\Banyan\\Banyan.exe");
				driver = new WindowsDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
				nlpResponseModel.setWindowsDriver(driver);
				System.out.println("Application launched successfully");
				nlpResponseModel.setMessage("Application launched successfully");
				nlpResponseModel.setMessage("Application launched successfully");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to terminate " + processName);

			}

		} catch (org.openqa.selenium.SessionNotCreatedException e1) {
			try {
				boolean isProcessRunning = killprocess(processName);

				if (!isProcessRunning) {
					TimeUnit.SECONDS.sleep(3);
					System.out.println(processName + " has been successfully terminated1.");
					DesiredCapabilities capabilities = new DesiredCapabilities();
					capabilities.setCapability("appium:platformName", "Windows");
					capabilities.setCapability("appium:deviceName", "Windows");
					capabilities.setCapability("appium:newCommandTimeout", "3600");
					capabilities.setCapability("appium:app", "C:\\Program Files\\Banyan\\Banyan.exe");
					driver = new WindowsDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
					nlpResponseModel.setWindowsDriver(driver);
					System.out.println("Application launched successfully");
					nlpResponseModel.setMessage("Application launched successfully");
					nlpResponseModel.setStatus(CommonConstants.pass);
				} else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Failed to terminate " + processName);

				}
			} catch (Exception e) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to launch application " + e);
			}
		}

		catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launch application " + e);
		}
		nlpResponseModel.getAttributes().put("WindowsDriver", driver);
		return nlpResponseModel;
	}

	public static boolean killprocess(String processName) throws IOException, InterruptedException {
//	String processName = processName;
		Process killProcess = new ProcessBuilder("taskkill", "/F", "/IM", processName).start();
		killProcess.waitFor();

		boolean isProcessRunning = isProcessRunning(processName);
		int retryCount = 0;
		int maxRetries = 5;

		while (isProcessRunning && retryCount < maxRetries) {
			TimeUnit.SECONDS.sleep(1);
			isProcessRunning = isProcessRunning(processName);
			retryCount++;
		}
		return isProcessRunning;
	}

	private static boolean isProcessRunning(String processName) {
		boolean isRunning = false;
		try {
			Process listProcess = new ProcessBuilder("tasklist").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(listProcess.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(processName)) {
					isRunning = true;
					break;
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isRunning;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel wsedf = new NlpRequestModel();

		wsedf.getAttributes().put("AppPath", "C:\\Program Files\\Banyan\\Banyan.exe");
		NlpResponseModel wasedf = new openWindowsApplication().execute(wsedf);
		System.out.println(wasedf.getAttributes().get("WindowsDriver"));
	}

}
