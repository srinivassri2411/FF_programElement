package Business_Logic;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ClickonElementandClose_error_popup implements Nlp {
	@InputParams({ @InputParam(name = "Element to Click", type = "org.openqa.selenium.WebElement"),
		           @InputParam(name = "Error Element", type = "org.openqa.selenium.WebElement"),
	@InputParam(name = "ExplicitTimeout", type = "java.lang.Integer")
	})
	//@ReturnType(name = "result", type = "java.lang.Boolean")

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
		WebElement element = (WebElement) attributes.get("Element to Click");
		WebElement errorElement = (WebElement) attributes.get("Error Element");
		Integer explicitDuration = (Integer) attributes.get("ExplicitTimeout");
		
		boolean res=false;
		
		WebDriver driver = nlpRequestModel.getWebDriver();
		Duration implicitWait = driver.manage().timeouts().getImplicitWaitTimeout();
		
		
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

			 WebDriverWait waitForJS = new WebDriverWait(driver, Duration.ofSeconds(explicitDuration));
            element.click();
            waitForJS.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            // 2. Wait for jQuery/AJAX calls to complete
            waitForJS.until(webDriver -> (Boolean) ((JavascriptExecutor) webDriver)
                    .executeScript("return window.jQuery ? jQuery.active == 0 : true"));
            waitForJS.until(webDriver -> (Boolean) ((JavascriptExecutor) webDriver)
                    .executeScript("return window.requestAnimationFrame "
                            + "? new Promise(requestAnimationFrame).then(() => true) : true"));
           String pageSource= driver.getPageSource();
           if(pageSource.contains("Oops! Something went wrong. Please contact your nearest ABHI branch or call our toll free number 1800-270-7000 for quick assistance")) {
        	if(errorElement.isDisplayed())
        	{
        	   errorElement.click();  
        	}
           }
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Element is Clicked");
			

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed to click on element "+e);
		}
		finally {
			driver.manage().timeouts().implicitlyWait(implicitWait);
		}

		nlpResponseModel.getAttributes().put("result", res);
		return nlpResponseModel;
	}
}

