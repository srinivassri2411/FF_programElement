package Mac;





import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.mac.Mac2Driver;

public class LaunchTheBrowser implements Nlp {
	@InputParams({ @InputParam(name = "URL", type = "java.lang.String")})

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
		String value ="";
		System.setProperty("java.awt.headless", "false");

		try {
			String url = (String) attributes.get("URL");
			Thread thread1 = new Thread(() -> {
	           WebDriver driver = nlpRequestModel.getWebDriver();
//	            driver.manage().window().maximize();     
	            driver.get(url);
//	            nlpResponseModel.setWebDriver(driver);
	        });
	        Thread thread2 = new Thread(() -> {
	        	try {
	        		Thread.sleep(12000);
	        		Robot r=new Robot();
	        		r.keyPress(KeyEvent.VK_NUMPAD1);
	        		r.keyRelease(KeyEvent.VK_NUMPAD1);
	        		Thread.sleep(200);
	        		r.keyPress(KeyEvent.VK_NUMPAD2);
	        		r.keyRelease(KeyEvent.VK_NUMPAD2);
	        		Thread.sleep(200);
	        		r.keyPress(KeyEvent.VK_NUMPAD3);
	        		r.keyRelease(KeyEvent.VK_NUMPAD3);
	        		Thread.sleep(200);
	        		r.keyPress(KeyEvent.VK_NUMPAD4);
	        		r.keyRelease(KeyEvent.VK_NUMPAD4);
	        		Thread.sleep(200);
	        		r.keyPress(KeyEvent.VK_ENTER);
	        		r.keyRelease(KeyEvent.VK_ENTER);
	        		System.out.println("Robot-----");
				} catch (Exception e) {
					
				}
	            
	        });
	        thread1.start();
	        thread2.start();
		
			Thread.sleep(1000);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully launched the browser");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			// Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("failed to launch the browser" + exceptionAsString);
		}
		return nlpResponseModel;
	}

}
