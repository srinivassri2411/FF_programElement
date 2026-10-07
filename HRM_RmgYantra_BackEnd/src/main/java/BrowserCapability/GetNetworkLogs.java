package BrowserCapability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class GetNetworkLogs implements Nlp {
	@ReturnType(name = "filteredLogs", type = "java.util.List")

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
		List<String> filteredLogs = new ArrayList<>();

		try {

			// Retrieve network logs
			WebDriver driver = nlpRequestModel.getWebDriver();
			LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);

			for (LogEntry entry : logEntries) {
				String message = entry.getMessage();

				// Filter logs for XHR (XMLHttpRequest)
				if (message.contains("\"method\":\"Network.requestWillBeSent\"")
						|| message.contains("\"method\":\"Network.responseReceived\"")) {
					filteredLogs.add(message);
				}

				// Filter logs for Fetch
				if (message.contains("\"method\":\"Fetch.requestPaused\"")
						|| message.contains("\"method\":\"Fetch.responseReceived\"")) {
					filteredLogs.add(message);
				}
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Fetched Network Logs");

		} catch (Exception e) {

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed To Fetch Network Logs");
		}
		nlpResponseModel.getAttributes().put("filteredLogs", filteredLogs);
		return nlpResponseModel;
	}
}