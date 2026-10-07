
package credentialHealth;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetLogAsJson implements Nlp {
	@InputParams({ @InputParam(name = "Request/Response", type = "java.lang.String"),
			@InputParam(name = "Url", type = "java.lang.String"),
			@InputParam(name = "Method", type = "java.lang.Boolean") })
	@ReturnType(name = "returnDat", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String requestResponse = (String) programElementsInput.get("Request/Response");
		String url = (String) programElementsInput.get("Url");
		String method = (String) programElementsInput.get("Method");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			List<LogEntry> entries = nlpRequestModel.getWebDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
			for (LogEntry entry : entries) {
				try {
					String urlFrom = JsonPath.read(entry.getMessage(), "$.message.params.request.url");
					String methodType = JsonPath.read(entry.getMessage(), "$.message.params.request.method");
					if (entry.getMessage().contains(requestResponse) && urlFrom.contains(url)
							&& methodType.toLowerCase().contains(method)) {
						returnValue = entry.getMessage();
					}
				} catch (Exception e) {

				}
			}
			nlpResponseModel.setMessage("Fetched the network log");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch the network log");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("returnDat", returnValue);
		return nlpResponseModel;
	}

}