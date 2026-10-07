package table;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;




public class MobileWeb implements Nlp {
	@InputParams({ @InputParam(name = "deviceName", type = "java.lang.String"),
			@InputParam(name = "browserName", type = "java.lang.String"),
			@InputParam(name = "url", type = "java.lang.String"),
			@InputParam(name = "execPath", type = "java.lang.String") })

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		String deviceName = (String) attributes.get("deviceName");
		String browserName = (String) attributes.get("browserName");
		String url = (String) attributes.get("url");
		String execPath = (String) attributes.get("execPath");

		AndroidDriver driver = null;
		try {
			UiAutomator2Options options = new UiAutomator2Options();
			options.setDeviceName(deviceName);
			options.setChromedriverExecutable(execPath);
			options.setCapability("browserName", browserName);
			driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), options);
//			driver = new RemoteWebDriver(new URL("http://localhost:4723/wd/hub"), options);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
			driver.get(url);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully launched the Mobile view ");
		} catch (Exception e) {

			e.printStackTrace();
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launch the Mobile view ");

		}
		nlpResponseModel.setWebDriver((WebDriver) driver);
		// nlpResponseModel.setWebDriver(driver);

		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		MobileWeb ua = new MobileWeb();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("deviceName", "a13nnxx");

		attributes.put("browserName", "chrome");
		attributes.put("url", "https://google.com");
		attributes.put("execPath", "C:\\Users\\User\\AppData\\Roaming\\fire-flink-client\\localnode\\chromedriver.exe");

		ua.execute(nl);

	}

}
