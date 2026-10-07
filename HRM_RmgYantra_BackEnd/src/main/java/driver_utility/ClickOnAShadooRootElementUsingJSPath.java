
package driver_utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class ClickOnAShadooRootElementUsingJSPath implements Nlp {
	@InputParams({ @InputParam(name = "JS Path", type = "java.lang.String") })

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

		try {
			WebDriver driver = (WebDriver) nlpRequestModel.getWebDriver();
			String jsPath = (String) attributes.get("JS Path");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			SearchContext shadow = (SearchContext) js.executeScript("return " + jsPath);
			((WebElement) shadow).click();

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Browser got launched successfully");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launch the browser because " + e);
		}
		// Your program element business logic goes here ...

		return nlpResponseModel;
	}

//	public static void main(String[] args) throws NlpException, MalformedURLException {
//		DesiredCapabilities cap = new DesiredCapabilities();
//		cap.setBrowserName("chrome");
//		ClickOnAShadooRootElementUsingJSPath ld = new ClickOnAShadooRootElementUsingJSPath();
//		NlpRequestModel nl = new NlpRequestModel();
//		Map<String, Object> attributes = nl.getAttributes();
//		attributes.put("JS Path",
//				"document.querySelector(\"body > div.globalnav-wrapper-c360 > hgf-c360nav\").shadowRoot.querySelector(\"header > div.c360-nav__header > div.c360-nav__wrapper > nav.utility-nav.show > ul > li.utility-icons-items.login > hgf-c360login\").shadowRoot.querySelector(\"hgf-popover > hgf-button\").shadowRoot.querySelector(\"button\")");
//		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
//		driver.manage().window().maximize();
//		driver.get("https://www.salesforce.com/in/");
//		new Scanner(System.in).next();
//		ld.execute(nl);
//	}
}
