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
////
////import com.tyss.optimize.common.util.CommonConstants;
////import com.tyss.optimize.nlp.util.Nlp;
////import com.tyss.optimize.nlp.util.NlpException;
////import com.tyss.optimize.nlp.util.NlpRequestModel;
////import com.tyss.optimize.nlp.util.NlpResponseModel;
////import com.tyss.optimize.nlp.util.annotation.InputParam;
////import com.tyss.optimize.nlp.util.annotation.InputParams;
////import com.tyss.optimize.nlp.util.annotation.ReturnType;
////
////import io.appium.java_client.android.AndroidDriver;
////import io.github.sukgu.Shadow;
////
////
////
////
////import org.springframework.stereotype.Component;
//
//import org.springframework.stereotype.Component;
//
//import com.tyss.optimize.nlp.util.Nlp;
//
//@Component("LIC11870_PJT1012_PE_NLP6b680f6f-7ca6-4b62-9729-4060c85f5a0f")
//public class GetTextFromElement implements Nlp {
////	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String"),
////			@InputParam(name = "isShadow", type = "java.lang.Boolean") })
////	@ReturnType(name = "Return", type = "java.lang.String")
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
////		String text = null;
////		try {
////			AndroidDriver androidDriver = nlpRequestModel.getAndroidDriver();
////			if (isShadow) {
////				Shadow shadow = new Shadow((WebDriver) androidDriver);
////				element = shadow.findElementByXPath(xpath);
////				text = element.getText();
////			} else {
////				element = androidDriver.findElement(By.xpath(xpath));
////				text = element.getText();
////			}
////			nlpResponseModel.setStatus(CommonConstants.pass);
////			nlpResponseModel.setMessage("Successfully fetched the text from the element");
////		} catch (Exception e) {
////			StringWriter exception = new StringWriter();
////			e.printStackTrace(new PrintWriter(exception));
////			nlpResponseModel.setStatus(CommonConstants.fail);
////			nlpResponseModel.setMessage("Failed to fetch " + exception.toString());
////		}
////		nlpResponseModel.getAttributes().put("Return", text);
////		return nlpResponseModel;
////	}
////}
package pamperClub;

