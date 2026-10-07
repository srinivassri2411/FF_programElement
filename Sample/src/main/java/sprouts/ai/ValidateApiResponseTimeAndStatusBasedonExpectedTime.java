package sprouts.ai;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.logging.LogEntry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPa8a763d4-950c-4507-a8db-07c0f52177da")
public class ValidateApiResponseTimeAndStatusBasedonExpectedTime implements Nlp {
	@InputParams({ @InputParam(name = "Log Entries", type = "java.util.List<LogEntry>"),
			@InputParam(name = "Expected Time(Seconds)", type = "java.lang.String"),
			@InputParam(name = "URL", type = "java.lang.String") })
	@ReturnType(name = "ReturnData", type = "java.lang.String")

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

	@SuppressWarnings("unchecked")
	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		List<LogEntry> entries = (List<LogEntry>) attributes.get("Log Entries");
		String expTime = (String) attributes.get("Expected Time(Seconds)");
		String url = (String) attributes.get("URL");
		String jsonString = null;
		try {
			double seconds = Double.parseDouble(expTime);
			ArrayList<Object> payloads = checkForLogDetails(entries, seconds, url);
			if (payloads.size() >= 1) {
				LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
				resultMap.put("finalResult", payloads);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				jsonString = gson.toJson(resultMap);
				System.out.println(jsonString);
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to validate all the api response with expected time");
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Validated all the api response with expected time");
			}

		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed " + writer);
		}

		nlpResponseModel.getAttributes().put("ReturnData", jsonString);
		return nlpResponseModel;
	}

	public ArrayList<Object> checkForLogDetails(List<LogEntry> entries, double seconds, String url) {
		seconds = convertSecondsIntoMilliSeconds(seconds);
		ArrayList<Object> payloads = new ArrayList<Object>();
		for (LogEntry entry : entries) {
			if (entry.getMessage().contains("Network.responseReceived")
					&& !entry.getMessage().contains("Network.responseReceivedExtraInfo")
					&& entry.getMessage().contains(url)) {
				try {
					int status = ((int) (JsonPath.parse(entry.getMessage()).read("$.message.params.response.status")));
					@SuppressWarnings("unchecked")
					LinkedHashMap<Object, Object> response = (LinkedHashMap<Object, Object>) JsonPath
							.read(entry.getMessage(), "$.message.params.response");
					double receiveHeadersEnd = Double.parseDouble((Object) JsonPath.read(entry.getMessage(),
							"$.message.params.response.timing.receiveHeadersEnd") + "");
					double sendEnd = Double.parseDouble(
							(Object) JsonPath.read(entry.getMessage(), "$.message.params.response.timing.sendEnd")
									+ "");
					double totalTimeTaken = receiveHeadersEnd + sendEnd;
					if (status < 400 && totalTimeTaken >= seconds) {
						payloads.add(response);
					} else if (status >= 400) {
						payloads.add(response);
					} else {
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			} else {
				continue;
			}
		}
		return payloads;
	}

	public static long convertSecondsIntoMilliSeconds(double seconds) {
		long milliSeconds = (long) (seconds * 1000);
		return milliSeconds;

	}
}