
package Business_Logics;

import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class SwitchWindowContainsURLs implements Nlp {
	@InputParams({ @InputParam(name = "URL", type = "java.lang.String") })
	@ReturnType(name = "Handle", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String uRL = (String) programElementsInput.get("URL");
		boolean returnValue = false;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Set<String> data = driver.getWindowHandles();
			System.out.println(data);
			for (String string : data) {
				driver.switchTo().window(string);
				if (driver.getCurrentUrl().contains(uRL)) {
					driver.switchTo().window(string);
					returnValue = true;
					break;
				}

			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Switched the url");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Switched" + e);
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Handle", returnValue);
		return nlpResponseModel;
	}

}