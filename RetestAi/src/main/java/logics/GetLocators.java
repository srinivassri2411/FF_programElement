
package logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class GetLocators implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement", findElement = false) })
	@ReturnType(name = "stringValues", type = "java.lang.List")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		List<Map<String, Object>> locatorMaps = (List<Map<String, Object>>) nlpRequestModel.getAttributes()
				.get("Element");
		System.out.println(locatorMaps);
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		nlpResponseModel.setStatus(CommonConstants.pass);
		nlpResponseModel.setMessage("Passed");
		nlpResponseModel.getAttributes().put("stringValues", locatorMaps);
		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

}