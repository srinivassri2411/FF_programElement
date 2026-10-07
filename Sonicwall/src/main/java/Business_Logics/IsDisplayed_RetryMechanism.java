package Business_Logics;

import java.util.Map;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class IsDisplayed_RetryMechanism implements Nlp {

	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement") })
	@ReturnType(name = "Return", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement element = (WebElement) programElementsInput.get("Element");

		Boolean isDisplayed = false;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			while (!isDisplayed) {
				try {

					isDisplayed = element.isDisplayed();
					break;
				}

				catch (StaleElementReferenceException e) {
					Thread.sleep(2000);
				}

			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Element is displayed and handled");

		} catch (Exception e) {

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Element is not displayed or handled " + e);
		}

		nlpResponseModel.getAttributes().put("Return", isDisplayed);
		return nlpResponseModel;
	}

}
