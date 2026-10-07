//package pamperClub;
////package pamperClub;
////
////import java.io.PrintWriter;
////import java.io.StringWriter;
////import java.util.ArrayList;
////import java.util.List;
////
////import org.openqa.selenium.By;
////import org.openqa.selenium.WebDriver;
////import org.openqa.selenium.WebElement;
////import org.openqa.selenium.interactions.Actions;
////
////import com.tyss.optimize.common.util.CommonConstants;
////import com.tyss.optimize.nlp.util.Nlp;
////import com.tyss.optimize.nlp.util.NlpException;
////import com.tyss.optimize.nlp.util.NlpRequestModel;
////import com.tyss.optimize.nlp.util.NlpResponseModel;
////import com.tyss.optimize.nlp.util.annotation.InputParam;
////import com.tyss.optimize.nlp.util.annotation.InputParams;
////
////import io.appium.java_client.android.AndroidDriver;
////import io.github.sukgu.Shadow;
////
////
////
////
////import org.springframework.stereotype.Component;
//
//@Component("LIC11870_PJT1012_PE_NLP932c2d9e-816a-4bad-ab1b-10896b28feba")
//public class TapOnCentreOfElement implements Nlp {
////	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String"),
////			@InputParam(name = "isShadow", type = "java.lang.Boolean") })
////	@Override
////	public List<String> getTestParameters() throws NlpException {
////		List<String> params = new ArrayList<>();
////		return params;
////	}
////
////	@Override
////	public StringBuilder getTestCode() throws NlpException {
////		StringBuilder sb = new StringBuilder();
////		return sb;
////	}
////
////	@Override
////	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
////
////		NlpResponseModel nlpResponseModel = new NlpResponseModel();
////		String xpath = (String) nlpRequestModel.getAttributes().get("Xpath");
////		Boolean isShadow = (Boolean) nlpRequestModel.getAttributes().get("isShadow");
////		WebElement element = null;
////		try {
////			AndroidDriver androidDriver = nlpRequestModel.getAndroidDriver();
////			if (isShadow) {
////				Shadow shadow = new Shadow((WebDriver) androidDriver);
////				element = shadow.findElementByXPath(xpath);
////				clickOnCentreOfElement(element, androidDriver);
////			} else {
////				element = androidDriver.findElement(By.xpath(xpath));
////				clickOnCentreOfElement(element, androidDriver);
////			}
////			nlpResponseModel.setStatus(CommonConstants.pass);
////			nlpResponseModel.setMessage("Tapped on centre of element");
////		} catch (Exception e) {
////			StringWriter exception = new StringWriter();
////			e.printStackTrace(new PrintWriter(exception));
////			nlpResponseModel.setStatus(CommonConstants.fail);
////			nlpResponseModel.setMessage("Failed to tap " + exception.toString());
////		}
////		return nlpResponseModel;
////	}
////
////	public void clickOnCentreOfElement(WebElement element, AndroidDriver androidDriver) {
////		new Actions((WebDriver) androidDriver).moveToElement(element, 0, 0).click().perform();
////	}
////}
package pamperClub;

