
package spensersGift;

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

public class SelectGivenSliderValue implements Nlp {
	@InputParams({ @InputParam(name = "Slider element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Min Handle", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Max Handle", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Min Value", type = "java.lang.String"),
			@InputParam(name = "Max Value", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		WebElement sliderelement = (WebElement) programElementsInput.get("Slider element");
		WebElement minHandle = (WebElement) programElementsInput.get("Min Handle");
		WebElement maxHandle = (WebElement) programElementsInput.get("Max Handle");
		String minValue = (String) programElementsInput.get("Min Value");
		String maxValue = (String) programElementsInput.get("Max Value");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			setPriceRange(nlpRequestModel.getWebDriver(), sliderelement, minHandle, maxHandle,
					Integer.parseInt(minValue), Integer.parseInt(maxValue));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully performed Slider action on " + minValue + " to " + maxValue);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform Slider action " + e);
		}
		return nlpResponseModel;
	}

	public static void setPriceRange(WebDriver driver, WebElement slider, WebElement minHandle, WebElement maxHandle,
			int minValue, int maxValue) {
		JavascriptExecutor js = (JavascriptExecutor) driver;

		String script = "function setPriceRange(slider, minHandle, maxHandle, min, max) {"
				+ "  const sliderRect = slider.getBoundingClientRect();"
				+ "  const sliderMin = parseInt(minHandle.getAttribute('aria-valuemin')) || 0;"
				+ "  const sliderMax = parseInt(maxHandle.getAttribute('aria-valuemax')) || 100;" +

				"  min = Math.max(sliderMin, Math.min(min, sliderMax));"
				+ "  max = Math.max(sliderMin, Math.min(max, sliderMax));" +

				"  function valueToX(value) {"
				+ "    return sliderRect.left + ((value - sliderMin) / (sliderMax - sliderMin)) * sliderRect.width;"
				+ "  }" +

				"  function simulateDrag(handle, targetX) {" + "    const rect = handle.getBoundingClientRect();"
				+ "    const startX = rect.left + rect.width / 2;" + "    const startY = rect.top + rect.height / 2;" +

				"    handle.dispatchEvent(new MouseEvent('mousedown', {bubbles: true, clientX: startX, clientY: startY}));"
				+ "    handle.dispatchEvent(new MouseEvent('mousemove', {bubbles: true, clientX: targetX, clientY: startY}));"
				+ "    handle.dispatchEvent(new MouseEvent('mouseup',   {bubbles: true, clientX: targetX, clientY: startY}));"
				+ "  }" +

				"  simulateDrag(minHandle, valueToX(min));" + "  simulateDrag(maxHandle, valueToX(max));" +

				"  const minPercent = ((min - sliderMin) / (sliderMax - sliderMin)) * 100;"
				+ "  const maxPercent = ((max - sliderMin) / (sliderMax - sliderMin)) * 100;" +

				"  minHandle.style.left = minPercent + '%';" + "  maxHandle.style.left = maxPercent + '%';" +

				"  const rangeBar = slider.querySelector('.ui-slider-range');" + "  if (rangeBar) {"
				+ "    rangeBar.style.left = minPercent + '%';"
				+ "    rangeBar.style.width = (maxPercent - minPercent) + '%';" + "  }" +

				"  const startLabel = document.getElementById('start_price');"
				+ "  const endLabel = document.getElementById('end_price');"
				+ "  const priceValue = slider.closest('.price')?.querySelector('.price-value');"
				+ "  if (startLabel) startLabel.textContent = '$' + min;"
				+ "  if (endLabel) endLabel.textContent = '$' + max;"
				+ "  if (priceValue) priceValue.textContent = '$' + min + ' - $' + max;" +

				"  [minHandle, maxHandle].forEach(handle => {" + "    ['input','change','mouseup'].forEach(evtName => {"
				+ "      handle.dispatchEvent(new Event(evtName, {bubbles:true}));" + "    });" + "  });" + "} "
				+ "setPriceRange(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]);";

		js.executeScript(script, slider, minHandle, maxHandle, minValue, maxValue);
	}
}