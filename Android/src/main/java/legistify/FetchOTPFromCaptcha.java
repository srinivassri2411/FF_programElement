
package legistify;

import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FetchOTPFromCaptcha implements Nlp {
	@InputParams({ @InputParam(name = "Audio Button", type = "org.openqa.selenium.WebElement") })
	@ReturnType(name = "otp", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement audioButton = (WebElement) programElementsInput.get("Audio Button");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getOTP(nlpRequestModel.getWebDriver(), audioButton);
			if (returnValue != null || returnValue.length() <= 2) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully fetched the OTP");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed fetched the OTP (not found)");
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch the OTP" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		nlpResponseModel.getAttributes().put("otp", returnValue);
		return nlpResponseModel;
	}

	private String getOTP(WebDriver driver, WebElement ele) throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("""
					window._spokenCaptchaChars = [];

					const OriginalUtterance = window.SpeechSynthesisUtterance;
					window.SpeechSynthesisUtterance = new Proxy(OriginalUtterance, {
					  construct(target, args) {
					    const spokenText = args[0];
					    console.log("Captcha:", spokenText);
					    if (typeof spokenText === 'string') {
					      spokenText.split(/\\s+/).forEach(char => {
					        if (char.length === 1) {
					          window._spokenCaptchaChars.push(char);
					        }
					      });
					    }
					    return new target(...args);
					  }
					});

					window.getCaptchaText = function() {
					  return window._spokenCaptchaChars.join('');
					};
				""");
		ele.click();
		System.out.println("🔊 Speaker clicked");
		Thread.sleep(5000);
		return (String) js.executeScript("return window.getCaptchaText();");
	}
}