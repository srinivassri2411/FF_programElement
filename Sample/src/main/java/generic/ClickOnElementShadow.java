package generic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP44f97b44-0d8e-421f-8592-ce2bcb6616fd")
public class ClickOnElementShadow implements Nlp {
	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String") })
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
		String xpath = (String) nlpRequestModel.getAttributes().get("Xpath");
		WebElement element = null;
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			element = shadow.findElementByXPath(xpath);
			clickOnCentreOfElement(element, driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Clicked on centre of element");
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click " + exception.toString());
		}
		return nlpResponseModel;
	}

	public void clickOnCentreOfElement(WebElement element, WebDriver driver) {
		new Actions(driver).moveToElement(element, 0, 0).click().perform();
	}
}