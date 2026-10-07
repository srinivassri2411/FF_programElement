
package Business_Logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.WebDriver;

public class VerifyTabWithExpectedUrl implements Nlp {
	@InputParams({ @InputParam(name = "URL", type = "java.lang.String") })
	@ReturnType(name = "isPresent", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String expectedUrl = (String) programElementsInput.get("URL");
		Boolean isTabPresent = false;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Set<String> allWindowHandles = driver.getWindowHandles();
			String originalWindow = driver.getWindowHandle();

			for (String windowHandle : allWindowHandles) {
				driver.switchTo().window(windowHandle); // Switch to the tab
				if (driver.getCurrentUrl().equals(expectedUrl)) {
					driver.switchTo().window(originalWindow); // Switch back to the original tab
					isTabPresent = true;
				}
			}
			driver.switchTo().window(originalWindow);
			if (isTabPresent) {
				System.out.println("Expected URL tab is present in the browser");
				nlpResponseModel.setStatus(CommonConstants.pass);
			}

			else {
				System.out.println("Expected URL tab is not present in the brower");

				nlpResponseModel.setStatus(CommonConstants.pass);
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			System.out.println("Failed to verify Expected URL tab in the browser." + e);
		}

		nlpResponseModel.getAttributes().put("isPresent", isTabPresent);
		return nlpResponseModel;
	}

}