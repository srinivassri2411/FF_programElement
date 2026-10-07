package driver_utility;

import java.time.Duration;
import java.util.ArrayList;
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


public class VerifyGivenDataIsPresentInTable implements Nlp {
	@InputParams({ @InputParam(name = "Column Name", type = "java.lang.String"),
			@InputParam(name = "Expected Data", type = "java.lang.String"),
			@InputParam(name = "Xpath of Go to Next page", type = "java.lang.String")})
	@ReturnType(name = "boolean", type = "java.util.Boolean")

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
		String xpathOfGoToNextPage = (String) attributes.get("Xpath of Go to Next page");
		String expectedData = (String) attributes.get("Expected Data");
		String columnName = (String) attributes.get("Column Name");

		// Your program element business logic goes here ...
		boolean flag = false;
		WebDriver driver = nlpRequestModel.getWebDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

		while (true) {
			try {
				flag = driver.findElement(
						By.xpath("//th[normalize-space(text())='"+columnName+"']/ancestor::table/descendant::td[text()='"
								+ expectedData + "']"))
						.isDisplayed();
				break;
			} catch (Exception e) {
				WebElement disableCheck = driver.findElement(By.xpath("//a[@aria-label='Go to next page']/.."));
				WebElement goToNextPage = driver.findElement(By.xpath(xpathOfGoToNextPage));
				if (!disableCheck.getAttribute("class").contains("disabled")) {
					goToNextPage.click();
				} else {
					break;
				}
			}
		}
		if (flag) {
			nlpResponseModel.setMessage("Given Data is present in Table");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} else {
			nlpResponseModel.setMessage("Given Data is NOT present in Table");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("boolean", flag);
		return nlpResponseModel;
	}
}