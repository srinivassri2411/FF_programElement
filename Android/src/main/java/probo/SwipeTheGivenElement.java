
package probo;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;

public class SwipeTheGivenElement implements Nlp {
	@InputParams({ @InputParam(name = "Source Element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Destination Element", type = "org.openqa.selenium.WebElement") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement srcElement = (WebElement) programElementsInput.get("Source Element");
		WebElement dstElement = (WebElement) programElementsInput.get("Destination Element");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			swipe(nlpRequestModel.getAndroidDriver(), srcElement, dstElement);
			nlpResponseModel.setMessage("Successfully swiped");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to swipe => " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public void swipe(AndroidDriver driver, WebElement srcElement, WebElement dstElement) {
		Actions act = new Actions(driver);
		int X = dstElement.getLocation().getX();
		int Y = dstElement.getLocation().getY();
		System.out.println(X + "  " + Y);
		act.clickAndHold(srcElement).moveByOffset(X + 300, Y).pause(Duration.ofMillis(500)).release().build().perform();
	}
}