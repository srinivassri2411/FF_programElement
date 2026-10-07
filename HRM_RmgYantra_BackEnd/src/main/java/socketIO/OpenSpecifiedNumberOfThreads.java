package socketIO;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class OpenSpecifiedNumberOfThreads implements Nlp {
	@InputParams({ @InputParam(name = "Number Of Threads", type = "java.lang.Integer"),
			@InputParam(name = "Url", type = "java.lang.String") })
	@ReturnType(name = "driversList", type = "java.util.List")

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
		Integer numberOfThreads = (Integer) attributes.get("Number Of Threads");
		String url = (String) attributes.get("Url");

		// Your program element business logic goes here ...

		try {
			List<WebDriver> driverList = openSpecifiedNumberOfThreads(numberOfThreads, url);
			nlpResponseModel.getAttributes().put("driversList", driverList);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Opened specified number of threads.");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to open specified number of threads." + e);
		}
		return nlpResponseModel;
	}

	public static List<WebDriver> openSpecifiedNumberOfThreads(int numberOfThreads, String url)
			throws MalformedURLException {
		List<WebDriver> driversList = new LinkedList<WebDriver>();
		for (int i = 0; i < numberOfThreads; i++) {
			DesiredCapabilities cap = new DesiredCapabilities();
			cap.setBrowserName("chrome");
			WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			driver.get(url);
			driversList.add(driver);
		}
		return driversList;
	}
}