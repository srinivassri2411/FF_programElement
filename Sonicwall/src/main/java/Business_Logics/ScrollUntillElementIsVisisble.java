
package Business_Logics;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.SikuliException;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.windows.WindowsDriver;

public class ScrollUntillElementIsVisisble implements Nlp {
	@InputParams({ @InputParam(name = "Name", type = "java.lang.String"),
		@InputParam(name = "Count", type = "java.lang.Integer") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String name = (String) programElementsInput.get("Name");
		int count = (int) programElementsInput.get("Count");
		System.setProperty("java.awt.headless", "false");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			Robot robot = new Robot();
			Screen s=new Screen();
//			Pattern p=new Pattern(name);
			boolean flag=false;
			 for (int i = 0; i < count; i++) {
				 try {
						System.out.println("try");
//						System.out.println(nlpRequestModel.getWebDriver());
                s.findText(name);
//					 nlpRequestModel.getWebDriver().findElement(By.name(name));
					 flag=true;
						System.out.println("try1");

					 break;
				} catch (Exception e) {
					System.out.println("catch");
//					robot.delay(100);
		            robot.keyPress(KeyEvent.VK_DOWN);
		            robot.keyRelease(KeyEvent.VK_DOWN);
		            robot.delay(50);
		            robot.keyPress(KeyEvent.VK_DOWN);
		            robot.keyRelease(KeyEvent.VK_DOWN);
				}
				 
			}
			 if (flag) {
					System.out.println("pass");

				 nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully scrolled till element is visible" );
			} else {
				 nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Failed to scroll" );
			}
			

		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed to execute " + e);

		}

		// Your program element business logic ends here ...
		// nlpResponseModel.getAttributes().put("Result", returnValue);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException, SikuliException, InterruptedException, MalformedURLException {
		/*
		 * NlpRequestModel nlpRequestModel= new NlpRequestModel(); new
		 * ClickUsingText().execute(nlpRequestModel);
//		 */
//		Thread.sleep(5000);
//		Screen sc = new Screen();
//		sc.findText("skybeyo5831").click();
		NlpRequestModel wsedf = new NlpRequestModel();

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium:platformName", "Windows");
		capabilities.setCapability("appium:deviceName", "Windows");
//		capabilities.setCapability("appium:newCommandTimeout", "3600");
		capabilities.setCapability("appium:app", "Root");
		WindowsDriver driver = new WindowsDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
		wsedf.setWebDriver(driver);
		System.out.println(driver);
//		wsedf.getAttributes().put("Name", "Auto Update");
		wsedf.getAttributes().put("Name", "View Log Files");
		wsedf.getAttributes().put("Count", 5);
		NlpResponseModel wasedf = new ScrollUntillElementIsVisisble().execute(wsedf);
	}
}