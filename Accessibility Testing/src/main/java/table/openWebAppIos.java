package table;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.ios.IOSDriver;





public class openWebAppIos implements Nlp {
	@InputParams({ @InputParam(name = "DesiredCapability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "MobURL", type = "java.lang.String"),
			@InputParam(name = "ImplictTimeOut", type = "java.lang.String") })

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
		DesiredCapabilities DesiredCapability = (DesiredCapabilities) attributes.get("DesiredCapability");
		String MobURL = (String) attributes.get("MobURL");
		String ImplictTimeOut = (String) attributes.get("ImplictTimeOut");

		try {

			WebDriver driver = new IOSDriver(new URL(MobURL), DesiredCapability);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Integer.parseInt(ImplictTimeOut)));
			nlpResponseModel.setWebDriver(driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Opened App");

		} catch (Exception e) {// TODO: handle exception
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed To Open App");
		}

		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {

		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability("appium:platformName", "iOS");
		caps.setCapability("appium:platformVersion", "17.3.1");
		caps.setCapability("appium:deviceName", "iPhone test1");
		caps.setCapability("appium:automationName", "XCUITest");
		caps.setBrowserName("Safari");

		caps.setCapability("appium:udid", "00008110-001835823407801E");
		caps.setCapability("appium:noReset", true);
		caps.setCapability("appium:agentPath",
				"/Applications/FireFlinkClient.app/Contents/Resources/flinko-client/exec/appium-webdriveragent/WebDriverAgent.xcodeproj");
		caps.setCapability("appium:bootstrapPath",
				"/Applications/FireFlinkClient.app/Contents/Resources/flinko-client/exec/appium-webdriveragent");
		System.out.println(caps);

		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		attributes.put("DesiredCapability", caps);
		attributes.put("MobURL", "http://localhost:4723/wd/hub");
		attributes.put("ImplictTimeOut", "20");

		openWebAppIos op = new openWebAppIos();
		NlpResponseModel NlpResponseModel = op.execute(nlpRequestModel);

		NlpResponseModel.getWebDriver().get("https://fireflink.com");
	}
}
