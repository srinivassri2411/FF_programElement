package driver_utility;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class GetListOfTextFromWebElementsForPaginationPattern implements Nlp {
	@InputParams({ @InputParam(name = "Xpath of WebElements", type = "java.lang.String"),
		@InputParam(name = "Xpath of Go to Next page", type = "java.lang.String") })
	@ReturnType(name = "ListOfText", type = "java.util.List")

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
		String xpathOfGoToNextPage = (String) attributes.get("Xpath of Go to Next page");

		// Your program element business logic goes here ...
		List<String> listOfText = new LinkedList<String>();
		boolean flag = true;
		WebDriver driver = nlpRequestModel.getWebDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		while (true) {
			try {
				List<WebElement> listOfWebElements = driver.findElements(By.xpath(xpathOfWebElements));
				for (WebElement webElement : listOfWebElements) {
					listOfText.add(webElement.getText());
				}
				Thread.sleep(1000);
				WebElement disableCheck = driver.findElement(By.xpath("//a[@aria-label='Go to next page']/.."));
				WebElement goToNextPage = driver.findElement(By.xpath(xpathOfGoToNextPage));

				if (!disableCheck.getAttribute("class").contains("disabled")) {
					goToNextPage.click();
				} else {
					break;
				}
				Thread.sleep(1000);
			}
			catch (Exception e) {
				nlpResponseModel.setMessage("Exception-> " + e);
				nlpResponseModel.setStatus(CommonConstants.fail);
				flag = false;
				break;
			}
		}

		if (flag) {
			nlpResponseModel.setMessage("Fetched List of Text from WebElements for Pagination Pattern...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		}
		nlpResponseModel.getAttributes().put("ListOfText", listOfText);
		return nlpResponseModel;
	}
}