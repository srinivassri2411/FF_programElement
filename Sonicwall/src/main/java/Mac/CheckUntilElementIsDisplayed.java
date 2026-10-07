package Mac;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.mac.Mac2Driver;

public class CheckUntilElementIsDisplayed implements Nlp {
	@InputParams({ @InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver"),
		@InputParam(name = "Xpath", type = "java.lang.String") })
	@ReturnType(name = "Value", type = "java.lang.Boolean")

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
		Mac2Driver driver = (Mac2Driver) attributes.get("driver");
		String xpath = (String) attributes.get("Xpath");
		boolean value = false;
		try {
			Thread.sleep(1000);
			try {
				driver.findElement(By.xpath(xpath)).isDisplayed();
				value=true;
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (value) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("element is displayed");
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("element is not displayed");
			}
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			// Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("text not displayed" + exceptionAsString);
		}
		nlpResponseModel.getAttributes().put("Value", value);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws FindFailed, InterruptedException {

		Screen screen = new Screen();
		Thread.sleep(4000);
		Pattern imagePattern = new Pattern("/Test Project/src/main/java/Business_Logic/sonicwall.png");
		screen.find(imagePattern).click();

	}
}