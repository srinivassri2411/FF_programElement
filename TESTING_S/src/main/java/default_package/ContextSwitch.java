package default_package;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;


public class ContextSwitch implements Nlp {
@InputParams({ @InputParam(name = "Explicit in Secs", type = "java.lang.Integer"), @InputParam(name = "Class Name", type = "java.lang.String")})
//	@ReturnType(name = "boolean", type = "java.lang.Boolean")
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
String className=(String)attributes.get("Class Name");
int explicitTime=(int)attributes.get("Explicit in Secs");
		try {
			AndroidDriver driver = nlpRequestModel.getAndroidDriver();
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(explicitTime));
			waitForWebView( driver ,explicitTime);
			Set<String> contextHandles = driver.getContextHandles();
			for (String context : contextHandles) {
			    System.out.println("Available context: " + context);
			}
			driver.context("WEBVIEW_chrome");
//			 wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
//			 driver.switchTo().frame(driver.findElement(By.className(className)));
			        nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Passed");
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
           // log.info(exceptionAsString);
      	    nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed "+exceptionAsString);
			
		}
	//	nlpResponseModel.getAttributes().put("boolean",b );
		return nlpResponseModel;
	}
	public static void waitForWebView(AndroidDriver driver, int time) {
	    // Initialize the timeout (in seconds)
	    int timeout = time; 
	    int elapsedTime = 0;

	    // Keep checking until the context list contains "WEBVIEW_chrome"
	    while (elapsedTime < timeout) {
	        Set<String> contexts = driver.getContextHandles();
	        System.out.println("Available contexts: " + contexts);

	        // Check if the WebView context is present
	        for (String context : contexts) {
	            if (context.equals("WEBVIEW_chrome")) {
	                System.out.println("WebView context found: " + context);
	                driver.context(context); // Switch to the WebView context
	                return; // Exit the method once the context is found and switched
	            }
	        }

	        // Wait for 1 second before re-checking
	        try {
	            Thread.sleep(500);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        elapsedTime++;
	    }

	    // If the loop completes without finding the WebView context, throw an exception
	    throw new RuntimeException("WEBVIEW_chrome context not found within " + timeout + " seconds!");
	}

}