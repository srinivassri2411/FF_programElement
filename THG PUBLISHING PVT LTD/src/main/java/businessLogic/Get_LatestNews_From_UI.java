
package businessLogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


public class Get_LatestNews_From_UI implements Nlp {
	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String"),
			@InputParam(name = "ExcelList", type = "java.util.List") })
	@ReturnType(name = "list", type = "java.util.List")

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
		String Xpath = (String) attributes.get("Xpath");
		List<String> ExcelListBefore = (java.util.ArrayList) attributes.get("ExcelList");
		List<String> ExcelListAfter = new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		try {
			for (int i = 0; i < ExcelListBefore.size(); i++) {
				ExcelListAfter.add(ExcelListBefore.get(i).toString().replace("ece/amp/", "ece"));
			}

			WebDriver driver = nlpRequestModel.getWebDriver();

			List<WebElement> elementslist = driver.findElements(By.xpath(Xpath));

			int uiListsize = elementslist.size();

			for (int i = 0; i < uiListsize; i++) {
				list.add(elementslist.get(i).getAttribute("href"));
			}

			Set<String> set = new HashSet();
			set.addAll(ExcelListAfter);
			set.addAll(list);
			set.removeAll(ExcelListAfter);
			list.clear();

			list.addAll(set);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched latest news from the UI");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch latest news from UI " + e);
		}

		nlpResponseModel.getAttributes().put("list", list);
		return nlpResponseModel;
	}
}
