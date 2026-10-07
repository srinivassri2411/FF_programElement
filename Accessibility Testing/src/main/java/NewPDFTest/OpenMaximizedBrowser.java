package NewPDFTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class OpenMaximizedBrowser implements Nlp {
	@InputParams({ @InputParam(name = "Browser", type = "java.lang.String") })

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
		String browser = (String) attributes.get("Browser");
		WebDriver driver = null;

		String browserName = browser;
		try {
			if (browserName.toLowerCase().contains("chrome")) {
				// System.setProperty("webdriver.chrome.driver", chromeDriverPath);
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments("start-maximized");
				driver = new ChromeDriver(chromeOptions);
			}

			else if (browserName.toLowerCase().contains("fire") || browserName.toLowerCase().contains("gecko")) {
				// System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				firefoxOptions.addArguments("--start-maximized");
				driver = new FirefoxDriver(firefoxOptions);
			} else if (browserName.toLowerCase().contains("edge") || browserName.toLowerCase().contains("ms")) {
				// System.setProperty("webdriver.edge.driver", edgeDriverPath);
				EdgeOptions edgeOptions = new EdgeOptions();
				edgeOptions.addArguments("start-maximized");
				driver = new EdgeDriver(edgeOptions);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Invalid browser provided");
			}

			if (driver != null) {
				nlpResponseModel.setWebDriver(driver);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully launched " + browser + " browser");

			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to launch browser-instance is null");

			}
		} catch (WebDriverException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to launch browser " + e);
		}

		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {

		OpenMaximizedBrowser ua = new OpenMaximizedBrowser();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("Browser", "chrome");
		ua.execute(nl);
	}

}