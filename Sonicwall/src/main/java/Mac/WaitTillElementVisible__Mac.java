package Mac;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
import io.appium.java_client.mac.Mac2Driver;
public class WaitTillElementVisible__Mac implements Nlp {
	@InputParams({ @InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver"),
		@InputParam(name = "Xpath", type = "java.lang.String"),
		@InputParam(name = "Wait Time", type = "java.lang.Long")})
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
		Long wait = (Long) attributes.get("Wait Time");
		try {
			Thread.sleep(1000);
			int time=0;
//		new WebDriverWait(driver, Duration.ofSeconds(wait)).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(xpath))));
		while (time<wait) {
		try {
			driver.findElement(By.xpath(xpath));
			break;
		} catch (Exception e) {
			// TODO: handle exception
			Thread.sleep(1000);
			time++;
		}
		}
//			driver.findElement(By.xpath(xpath)).click();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully waited for the element ");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			// Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Element is not visible " + exceptionAsString);
		}
		return nlpResponseModel;
	}
	public static void main(String[] args) throws FindFailed, InterruptedException {
		Screen screen = new Screen();
		Thread.sleep(4000);
		Pattern imagePattern = new Pattern("/Test Project/src/main/java/Business_Logic/sonicwall.png");
		screen.find(imagePattern).click();
	}
}