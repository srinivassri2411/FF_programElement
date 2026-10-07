package Business_Logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.*;

public class Swipe_And_Fetch_Text implements Nlp {

	@InputParams({ @InputParam(name = "Android_Xpath", type = "java.lang.String"),
			@InputParam(name = "Swipe_Direction", type = "java.lang.String") })
	@ReturnType(name = "list", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Android_Xpath = (String) programElementsInput.get("Android_Xpath");
		String Swipe_Direction = (String) programElementsInput.get("Swipe_Direction");

		AndroidDriver androidDriver;
		Set<String> elementTextSet = new HashSet<>();

		try {
			androidDriver = nlpRequestModel.getAndroidDriver();
		} catch (Exception e) {
			throw new NlpException("Failed to get Android Driver: " + e.getMessage());
		}

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			Dimension size = androidDriver.manage().window().getSize();
			int startX, endX, startY, endY;

			if ("right".equalsIgnoreCase(Swipe_Direction)) {
				startX = (int) (size.width * 0.8);
				endX = (int) (size.width * 0.2);
				startY = endY = size.height / 2;
			} else if ("left".equalsIgnoreCase(Swipe_Direction)) {
				startX = (int) (size.width * 0.2);
				endX = (int) (size.width * 0.8);
				startY = endY = size.height / 2;
			} else if ("down".equalsIgnoreCase(Swipe_Direction)) {
				startX = endX = size.width / 2;
				startY = (int) (size.height * 0.3);
				endY = (int) (size.height * 0.7);
			} else if ("up".equalsIgnoreCase(Swipe_Direction)) {
				startX = endX = size.width / 2;
				startY = (int) (size.height * 0.7);
				endY = (int) (size.height * 0.3);
			} else {
				throw new IllegalArgumentException("Invalid direction: " + Swipe_Direction);
			}

			PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

			boolean hasNewElement = true;
			while (hasNewElement) {
				hasNewElement = false;

				for (WebElement element : androidDriver.findElements(By.xpath(Android_Xpath))) {
					String elementText = element.getText().trim();
					if (!elementText.isEmpty() && elementTextSet.add(elementText)) {
						hasNewElement = true;
					}
				}

				if (hasNewElement) {
					Sequence swipe = new Sequence(finger, 0);
					swipe.addAction(
							finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
					swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
					swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(),
							endX, endY));
					swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
					androidDriver.perform(Collections.singletonList(swipe));
				}
				Thread.sleep(5000);
			}
			List<String> list = new ArrayList<>(elementTextSet);
			nlpResponseModel.getAttributes().put("list", list);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Swipe completed and elements collected successfully.");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform action: " + e.getMessage());
		}
		return nlpResponseModel;
	}
}
