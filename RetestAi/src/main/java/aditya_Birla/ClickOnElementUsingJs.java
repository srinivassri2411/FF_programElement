
package aditya_Birla;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ClickOnElementUsingJs implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false) })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		List<Map<String, Object>> element = (List<Map<String, Object>>) programElementsInput.get("Element");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		WebDriver driver = null;
		Duration impli = null;
		try {
			String LocatorValue = null;
			int failedLoc = 0;
			driver = nlpRequestModel.getWebDriver();
			impli = driver.manage().timeouts().getImplicitWaitTimeout();
			for (Map<String, Object> map : element) {
				if (failedLoc >= 1) {
					driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));
				}
				try {
					LocatorValue = (String) map.get("LocatorValue");
					LocatorValue = LocatorValue.replaceAll("=\"([^\"]*)\"", "='$1'");
					if (driver.findElement(By.xpath(LocatorValue)) != null) {
						JavascriptExecutor jse = (JavascriptExecutor) driver;
						String script = "var element = document.evaluate(\"" + LocatorValue
								+ "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
								+ "if(element){"
								+ "   ['pointerdown','mousedown','pointerup','mouseup','click'].forEach(function(eventType){"
								+ "       var event = new MouseEvent(eventType, {" + "           view: window,"
								+ "           bubbles: true," + "           cancelable: true" + "       });"
								+ "       element.dispatchEvent(event);" + "   });" + "}";

						jse.executeScript(script);
						break;
					}
				} catch (NoSuchElementException e) {
					failedLoc += 1;
				}
			}
			if (failedLoc == element.size()) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("No such element ==> " + element);
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Click on given element " + element);
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click => " + writer);

		}
		driver.manage().timeouts().implicitlyWait(impli);
		return nlpResponseModel;
	}

}