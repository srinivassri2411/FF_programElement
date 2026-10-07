
package emvigo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPf66bb340-ad4b-4d87-9358-6a35da1c049b")
public class GetAttributeValuesFromList implements Nlp {
	@InputParams({ @InputParam(name = "ListOfWebElements", type = "java.util.List"),
			@InputParam(name = "Attribute Name", type = "java.lang.String") })
	@ReturnType(name = "ListOfDropDownText", type = "java.util.List")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		List<WebElement> listOfWebElements = (List<WebElement>) programElementsInput.get("ListOfWebElements");
		String attrName = (String) programElementsInput.get("Attribute Name");
		List<String> returnValue = new ArrayList<String>();

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			for (WebElement dropdown : listOfWebElements) {
				returnValue.add(dropdown.getAttribute(attrName));
			}
			nlpResponseModel.setMessage("Fetched all the list of attribute values");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("ListOfDropDownText", returnValue);
		return nlpResponseModel;
	}
}