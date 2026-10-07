package generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;


import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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
@Component("LIC11870_PJT1012_PE_NLP2fcbf896-4261-42bd-ad8f-f20527f97c08")
public class GetCookiefromNetworklogs implements Nlp {
	@InputParams({ @InputParam(name = "Request url", type = "java.lang.String") })
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

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String result = null;
		try {
			String reqUrl = (String) attributes.get("Request url");
			WebDriver driver = nlpRequestModel.getWebDriver();
			List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
			String url = null;
			String secondUrl = null;
			String nextUrl = null;
			for (LogEntry cook : entries) {
				try {
					url = JsonPath.parse(cook.getMessage()).read("$.message.params.request.url");
				} catch (PathNotFoundException e) {
					System.out.println("first catch " + e.getMessage());
					e.printStackTrace();
				}
				try {
					if (url != null && url.equals(reqUrl)) {
						System.out.println("first url" + url);
						secondUrl = JsonPath.parse(cook.getMessage()).read("$.message.params.documentURL");
						System.out.println("second url" + secondUrl);
						break;
					}
				} catch (PathNotFoundException e) {
					System.out.println("second catch " + e.getMessage());
					e.printStackTrace();
				}
			}
			for (LogEntry cook : entries) {
				try {
					nextUrl = JsonPath.parse(cook.getMessage()).read("$.message.params.headers.referer");
					if (nextUrl.equals(secondUrl)) {
						try {
							String path = "$.message..[?(@.referer=='" + nextUrl + "')].cookie";
							System.out.println("Got the url+=====>" + path);
							result = JsonPath.parse(cook.getMessage()).read(path).toString();
							if (result.length() > 3) {
								result = result.substring(2, result.length() - 2);
								break;
							}
						} catch (PathNotFoundException e) {
							System.out.println("nested catch " + e.getMessage());
							e.printStackTrace();
						}
					}
				} catch (PathNotFoundException e) {
					System.out.println("third catch " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch " + e);
		}
		nlpResponseModel.getAttributes().put("ReturnData", result);
		return nlpResponseModel;
	}

}