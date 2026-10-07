
package driver_utility;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;


public class GetListOfTextFromFrodAndroidBySwiping implements Nlp {
	@InputParams({ @InputParam(name = "Xpath of WebElements", type = "java.lang.String")})
	@ReturnType(name = "List", type = "java.util.List")

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String xpathOfWebElements = (String) attributes.get("Xpath of WebElements");

		List<String> listOfText = new LinkedList<String>();
		AndroidDriver driver = nlpRequestModel.getAndroidDriver();


		try {
			while (true) {
				Thread.sleep(1000);
				// Find all elements visible on the screen
				List<WebElement> listOfWebElements = driver
						.findElements(By.xpath(xpathOfWebElements));

				// Add All visible elements to the list
				List<String> breakList = new LinkedList<String>();
				for (WebElement element : listOfWebElements) {
					if (!listOfText.contains(element.getText().split(":")[1].trim())) {
						breakList.add(element.getText().split(":")[1].trim());
					}
				}

				if (!verifyListContainsList(breakList, listOfText)) {
					// Add All visible elements to the list
					for (WebElement element : listOfWebElements) {
						if (!listOfText.contains(element.getText().split(":")[1].trim())) {
							listOfText.add(element.getText().split(":")[1].trim());
						}
					}
					// Get screen dimensions
					Dimension size = driver.manage().window().getSize();

					// Calculate middle coordinates
					int startX = size.width / 2;
					int startY = size.height / 2 ;
					int endY =  250;

					// Perform the swipe action using PointerInput
					PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
					Sequence swipe = new Sequence(finger, 1);
					swipe.addAction(finger.createPointerMove(Duration.ofMillis(0),
							PointerInput.Origin.viewport(), startX, startY));
					swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
					swipe.addAction(new Pause(finger, Duration.ofMillis(600)));
					swipe.addAction(finger.createPointerMove(Duration.ofMillis(600),
							PointerInput.Origin.viewport(), startX, endY));
					swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
					driver.perform(Arrays.asList(swipe));
					
				} else {
					break;
				}
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fetching list of text is successful..");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Exception -> " + e);
		}

		nlpResponseModel.getAttributes().put("List", listOfText);
		return nlpResponseModel;
	}

	private static boolean verifyListContainsList(List<String> listA, List<String> listB) {
		boolean allPresent = true;
		for (String element : listA) {
			if (!listB.contains(element)) {
				allPresent = false;
				break;
			}
		}
		if (allPresent) {
			return true;
		} else {
			return false;
		}
	}
}