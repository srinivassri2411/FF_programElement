
package reusable_utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class ClickOnElementUsingJs implements Nlp {
	@InputParams({ @InputParam(name = "Js_Path", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String jsPath = (String) programElementsInput.get("Js_Path");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			JavascriptExecutor jse = (JavascriptExecutor) nlpRequestModel.getWebDriver();
			WebElement element = (WebElement) jse.executeScript("return "+jsPath);
			element.click();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Clicked on Element.");
		} catch(Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on element."+writer.toString());
		}
		return nlpResponseModel;
	}

} 