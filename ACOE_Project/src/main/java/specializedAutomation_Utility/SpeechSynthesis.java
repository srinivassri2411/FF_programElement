package specializedAutomation_Utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class SpeechSynthesis implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String text = (String) programElementsInput.get("Text");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			WebDriver driver = (WebDriver) nlpRequestModel.getWebDriver();
			speechsynthesis(driver, text);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Speech synthesis completed successfully");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to complete speech synthesis due to "+e);
		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public static void speechsynthesis(WebDriver driver, String text) throws InterruptedException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;

		String injectScript = "const speakText = arguments[0];" + // store text safely
				"const btn = document.createElement('button');" + "btn.id = 'seleniumSpeakBtn';"
				+ "btn.innerText = 'Speak Now';" + "btn.style.position = 'fixed';" + "btn.style.top = '10px';"
				+ "btn.style.left = '10px';" + "btn.style.zIndex = '99999';" + "btn.style.display = 'block';" + ""
				+ "btn.onclick = function() {" + "   speechSynthesis.cancel();"
				+ "   const utterance = new SpeechSynthesisUtterance(speakText);"
				+ "   speechSynthesis.speak(utterance);" + "   setTimeout(() => btn.remove(), 500);" + "};"
				+ "document.body.appendChild(btn);";

		jse.executeScript(injectScript, text);

		driver.findElement(By.id("seleniumSpeakBtn")).click();
	}

}