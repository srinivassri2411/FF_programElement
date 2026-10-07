package Windows;

import java.io.PrintWriter;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.sikuli.script.FindFailed;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.windows.WindowsDriver;
import lombok.extern.slf4j.Slf4j;

public class WaitTillElementVisible__Desktop implements Nlp {
	@InputParams({@InputParam(name = "Driver", type = "io.appium.java_client.windows.WindowsDriver"),@InputParam(name = "Xpath", type = "java.lang.String"),
		@InputParam(name = "Wait Time", type = "java.lang.Long")})

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
		//		Mac2Driver driver = (Mac2Driver) attributes.get("driver");
		WindowsDriver driver = (WindowsDriver) attributes.get("Driver");
		String xpath = (String) attributes.get("Xpath");
		Long wait = (Long) attributes.get("Wait Time");
		try {
			//			Thread.sleep(1000);
//			WindowsDriver driver=nlpRequestModel.getWindowsDriver();
	//		log.info("Windowsdriver "+driver);
			//			 DesiredCapabilities cap=new DesiredCapabilities();
			//			 cap.setCapability("appium:automationName", "Windows");
			//			 cap.setCapability("appium:platformName", "Windows");
			//			 cap.setCapability("appium:app", "Root");
			//			 WindowsDriver driver=new WindowsDriver(new URL("http://localhost:4723/wd/hub"), cap);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
			//		new WebDriverWait(driver, Duration.ofSeconds(wait)).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(xpath))));

			int count=0;
			while (count<wait) {
				try {
					System.out.println("try");
					driver.findElement(By.xpath(xpath));
					break;
				} catch (Exception e) {
					Thread.sleep(1000);
					count++;
					// TODO: handle exception
				}
				System.out.println(count);
			}
			try {
				driver.findElement(By.xpath(xpath));
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully waited for the element ");
			} catch (Exception e) {
				e.printStackTrace();
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Element is not displayed");
			}
			//			driver.findElement(By.xpath(xpath)).click();
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			// Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Element is not visible " + exceptionAsString);
		}
		return nlpResponseModel;
	}
	public static void main(String[] args) throws FindFailed, InterruptedException, NlpException, MalformedURLException {


		NlpRequestModel nlp=new NlpRequestModel();
		Map<String, Object> attributes = nlp.getAttributes();
		attributes.put("Xpath", "//HyperLink[@Name=\"menuitem-Home\"]");
		attributes.put("Wait Time", 20l);
		new WaitTillElementVisible__Desktop().execute(nlp);
		//		Mac2Driver driver = (Mac2Driver) attributes.get("driver");
	}
}