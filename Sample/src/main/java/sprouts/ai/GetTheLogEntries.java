package sprouts.ai;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Component;

@Slf4j
@Component("LIC11870_PJT1012_PE_NLP0698ce3d-2d6c-420c-98d9-63d35e040d8e")
public class GetTheLogEntries implements Nlp {
	@InputParams({ @InputParam(name = "Request URL", type = "java.lang.String"),
			@InputParam(name = "Request/Response", type = "java.lang.Stirng") })
	@ReturnType(name = "ReturnData", type = "java.util.List")

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
		String requestURL = (String) nlpRequestModel.getAttributes().get("Request URL");
		String reqRes = (String) nlpRequestModel.getAttributes().get("Request/Response");
		List<String> returnResult = new ArrayList<String>();
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
			for (LogEntry logEntry : entries) {
				if (logEntry.getMessage().contains(requestURL) && logEntry.getMessage().contains(reqRes)) {
					returnResult.add(logEntry.getMessage());
				}
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully captured the network logs");
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			log.error(exception.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to capture " + exception.toString());
		}
		nlpResponseModel.getAttributes().put("ReturnData", returnResult);
		return nlpResponseModel;
	}
}