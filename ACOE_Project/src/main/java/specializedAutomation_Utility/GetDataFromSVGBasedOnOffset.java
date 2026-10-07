
package specializedAutomation_Utility;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;



public class GetDataFromSVGBasedOnOffset implements Nlp {
	@InputParams({ @InputParam(name = "SVG Element", type = "org.openqa.selenium.WebElement"),
			@InputParam(name = "Text Box Element", type = "java.lang.String") })
	@ReturnType(name = "string3", type = "java.util.List")

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
		WebElement svgElement = (WebElement) attributes.get("SVG Element");
		String textbox = (String) attributes.get("Text Box Element");
		List li = new ArrayList();
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			Actions actions = new Actions(driver);
			String s = svgElement + "";
			System.out.println(s);
			s = s.substring(s.indexOf("xpath: ") + 7, s.length() - 1);
			System.out.println(s);
			List<WebElement> graph = driver.findElements(By.xpath(s));
			for (WebElement webElement : graph) {
				actions.moveToElement(webElement).perform();
				li.add(driver.findElement(By.xpath(textbox)).getText());
			}
			nlpResponseModel.setMessage("Successfully fetched the data from svg tag");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch the data from svg tag " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("string3", li);
		return nlpResponseModel;
	}
}