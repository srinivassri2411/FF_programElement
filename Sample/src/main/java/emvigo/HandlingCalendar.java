package emvigo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPd3472434-2f98-41a4-8180-fb2f560d424e")
public class HandlingCalendar implements Nlp {
	@InputParams({ @InputParam(name = "Expected Year xpath", type = "java.lang.String"),
			@InputParam(name = "Expected Date xpath", type = "java.lang.String"),
			@InputParam(name = "Max count", type = "java.lang.String") })

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
		String expYears = (String) attributes.get("Expected Year xpath");
		String expDate = (String) attributes.get("Expected Date xpath");
		String maxTry = (String) attributes.get("Max count");
		Duration impTime = null;
		WebDriver driver = null;
		try {
			driver = nlpRequestModel.getWebDriver();
			impTime = driver.manage().timeouts().getImplicitWaitTimeout();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			handleCalender(driver, expYears, expDate, Integer.parseInt(maxTry), 20);
			driver.manage().timeouts().implicitlyWait(impTime);
			nlpResponseModel.setMessage("Successfully handled calendar ");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			driver.manage().timeouts().implicitlyWait(impTime);
			nlpResponseModel.setMessage("Failed to handle==> " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void handleCalender(WebDriver driver, String expYears, String dateXpath, int maxTry, long expTime) {
		int count = 0;
		List<WebElement> elementList = driver.findElements(By.xpath(expYears));
		while (elementList.isEmpty() || count <= maxTry) {
			driver.findElement(By.xpath("//*[@class=\"rdtNext\" or @aria-label=\"Go to next month\"]")).click();
			elementList = driver.findElements(By.xpath(expYears));
			if (!elementList.isEmpty()) {
				new WebDriverWait(driver, Duration.ofSeconds(expTime))
						.until(ExpectedConditions.elementToBeClickable(By.xpath(dateXpath))).click();
				break;
			} else {
				count++;
			}
		}
	}
}