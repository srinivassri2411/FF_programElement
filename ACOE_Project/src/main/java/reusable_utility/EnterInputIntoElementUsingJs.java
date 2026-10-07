
package reusable_utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class EnterInputIntoElementUsingJs implements Nlp {
	@InputParams({ @InputParam(name = "Js_Path", type = "java.lang.String"),
		@InputParam(name = "Input", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String jsPath = (String) programElementsInput.get("Js_Path");
		String input = (String) programElementsInput.get("Input");
		
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			JavascriptExecutor jse = (JavascriptExecutor) nlpRequestModel.getWebDriver();
			WebElement element = (WebElement) jse.executeScript("return "+jsPath);
			element.sendKeys(input);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Entered input into Element.");
		} catch(Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to enter."+writer.toString());
		}
		return nlpResponseModel;
	}

} 