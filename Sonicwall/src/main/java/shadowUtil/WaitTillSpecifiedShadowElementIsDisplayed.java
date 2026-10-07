package shadowUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.github.sukgu.Shadow;

public class WaitTillSpecifiedShadowElementIsDisplayed implements Nlp {
	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String", findElement = false),
			@InputParam(name = "Wait Time", type = "java.lang.String") })

	@Override
	public List<String> getTestParameters() throws NlpException {
		return new ArrayList<>();
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		return new StringBuilder();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String eles = (String) nlpRequestModel.getAttributes().get("Xpath");
		String wait = (String) nlpRequestModel.getAttributes().get("Wait Time");
		Duration impli = null;
		WebDriver driver = null;
		try {
			driver = nlpRequestModel.getWebDriver();
			Shadow shadow = new Shadow(driver);
			boolean success = false;
			impli = driver.manage().timeouts().getImplicitWaitTimeout();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
			int count = 0;
			while (true) {
				try {
					WebElement element = findShadowElement(eles, driver, shadow);
					if (count == Integer.parseInt(wait)) {
						success = false;
						break;
					}
					if (element == null) {
						count++;
					} else {
						success = true;
						break;
					}
				} catch (Exception e) {
					System.out.println("Retry-able error: " + e.getMessage());
				}
			}

			if (success) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully waited until element is displayed");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed locator not working");
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Execution failed: " + sw.toString());
		}
		driver.manage().timeouts().implicitlyWait(impli);
		return nlpResponseModel;
	}

	public static WebElement findShadowElement(String xpath, WebDriver driver, Shadow shadow) {
		return shadow.findElementByXPath(xpath);
	}

	public void waitUntil(WebElement element, WebDriver driver, String wait) {
		new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(wait)))
				.until(ExpectedConditions.visibilityOf(element));
	}

	public static void main(String[] args) throws InterruptedException {
		WebDriver d = new ChromeDriver();
		d.manage().window().maximize();
		d.get("https://demoapps.qspiders.com/ui/shadow?sublist=0");

		System.out.println("Log in and open PDF. Press ENTER when ready...");
		new Scanner(System.in).nextLine();
		WebElement var = findShadowElement("//input[@placeholder='Enter your username']", d, new Shadow(d));
		System.out.println(var);
		if (var != null) {
			System.out.println("ddssafafa");
		} else {
			System.out.println("hhjjjj");
		}
	}
}