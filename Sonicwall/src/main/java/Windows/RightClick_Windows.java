package Windows;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.openqa.selenium.WebDriver;
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

import io.appium.java_client.windows.WindowsDriver;

public class RightClick_Windows implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement element = (WebElement) programElementsInput.get("Element");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			rightClick(nlpRequestModel.getWebDriver(), element);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Performed Right click on given element");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform Right click on given element =>" + e);
		}
		return nlpResponseModel;
	}

	public static void rightClick(WebDriver driver, WebElement element) {
		PointerInput mouse = new PointerInput(PointerInput.Kind.PEN, "pen");
		Sequence rightClick = new Sequence(mouse, 1);
		rightClick.addAction(
				mouse.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.fromElement(element), 0, 0));
		rightClick.addAction(mouse.createPointerDown(PointerInput.MouseButton.RIGHT.asArg()));
		rightClick.addAction(mouse.createPointerUp(PointerInput.MouseButton.RIGHT.asArg()));

		((WindowsDriver) driver).perform(Collections.singletonList(rightClick));
	}
}