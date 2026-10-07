
package probo;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;

public class SeekUntilExpectedText implements Nlp {
	@InputParams({ @InputParam(name = "Seek Element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Expected Text", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement seekElement = (WebElement) programElementsInput.get("Seek Element");
		String expText = (String) programElementsInput.get("Expected Text");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			seek(nlpRequestModel.getAndroidDriver(), seekElement, expText);
			nlpResponseModel.setMessage("Successfully seeked till " + expText);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to seek => " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public void seek(AndroidDriver driver, WebElement seekElement, String expectedValue) {
		int startX = seekElement.getLocation().getX();
		int startY = seekElement.getLocation().getY();
		int endX = startX + seekElement.getSize().getWidth();
		int endY = startY + seekElement.getSize().getHeight();
		System.out.println(startX + "-----" + startY);
		System.out.println(endX + "-----" + endY);
		for (int i = startX; i < endX;) {
			PointerInput pointer = new PointerInput(PointerInput.Kind.TOUCH, "finger");
			Sequence sequence = new Sequence(pointer, 0);
			sequence.addAction(
					pointer.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), i, startY));
			sequence.addAction(pointer.createPointerDown(0));
			sequence.addAction(
					pointer.createPointerMove(Duration.ofMillis(200), PointerInput.Origin.viewport(), i += 20, startY));
			sequence.addAction(pointer.createPointerUp(0));
			driver.perform(List.of(sequence));
			if (Double.parseDouble(seekElement.getText()) + 1.0 == Double.parseDouble(expectedValue)) {
				break;
			}
		}
	}
}