
package businessLogic;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
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

import dev.amp.validator.ExitCondition;
import dev.amp.validator.ValidatorProtos;
import dev.amp.validator.parser.AMPHtmlParser;
import dev.amp.validator.parser.ParserException;

public class Amp_Validation implements Nlp {
	@InputParams({ @InputParam(name = "Test URL", type = "java.lang.String") })
	@ReturnType(name = "results", type = "java.util.Map")

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
		String testUrl = (String) attributes.get("Test URL");
		// testUrl = testUrl + "#development=amp";
		Map<String, String> result = null;
		WebDriver driver = nlpRequestModel.getWebDriver();
		try {
			result = validateAMP(testUrl, driver);
			if (result.get("Status") == "VALID AMP") {
				nlpResponseModel.setMessage("Current page is a Valid AMP Page");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else if (result.get("Status") == "INVALID AMP") {
				nlpResponseModel.setMessage("Current page is an Invalid AMP Page");
				nlpResponseModel.setStatus(CommonConstants.fail);
			} else if (result.get("Status") == "LINKED AMP") {
				nlpResponseModel.setMessage("Current page is a Linked AMP Page");
				nlpResponseModel.setStatus(CommonConstants.fail);
			} else if (result.get("Status") == "NOT AN AMP") {
				nlpResponseModel.setMessage("Current page is Not an AMP Page");
				nlpResponseModel.setStatus(CommonConstants.fail);

			} else {
				nlpResponseModel.setMessage("Failed to Validate AMP Page");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}

		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to validate AMP Page ->" + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("results", result);
		return nlpResponseModel;
	}

	public static Map<String, String> validateAMP(String url, WebDriver driver)
			throws IOException, ParseException, ParserException {
		Map<String, String> m = new HashMap<>();
		LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
		List<String> logErrors = new ArrayList<String>();
		int count = 0;
		for (LogEntry entry : logs.getAll()) {
			if (entry.getLevel().getName().equalsIgnoreCase("SEVERE")
					&& entry.getMessage().startsWith("https://cdn.ampproject.org/v0/validator_wasm.js")
					&& entry.getMessage().contains("see https://amp.dev/documentation")
					|| entry.getMessage().contains("AMP validation had errors:")
							&& (!entry.getMessage().endsWith("1:61933 \"CANCELLED\""))
							&& (!entry.getMessage().endsWith("1:61883 \"CANCELLED\""))
							&& (!entry.getMessage().endsWith("1:10646 "))) {
				count++;
				logErrors.add(entry.getMessage());
				// log.info(entry.getMessage());
			}
		}
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpGet);
		String inputHtml = EntityUtils.toString(((CloseableHttpResponse) response).getEntity());

		ValidatorProtos.HtmlFormat.Code htmlFormat = ValidatorProtos.HtmlFormat.Code.AMP;
		ExitCondition condition = ExitCondition.FULL_PARSING;
		int maxNodes = Integer.MAX_VALUE;
		final AMPHtmlParser ampHtmlParser = new AMPHtmlParser();
		final ValidatorProtos.ValidationResult validationResult = ampHtmlParser.parse(inputHtml, htmlFormat, condition,
				maxNodes);
		inputHtml = inputHtml.toLowerCase();
		String actualStatus = "";
		String validatorStatus = validationResult.getStatus().toString(); // [UNKNOWN, PASS, FAIL]
		// log.info(validatorStatus);
		List<ValidatorProtos.ValidationError> errorList = new ArrayList<>();
		List<ValidatorProtos.ValidationError> warningList = new ArrayList<>();
		for (ValidatorProtos.ValidationError validationError : validationResult.getErrorsList()) {
			warningList.add(validationError);
			if ((!validationError.getCode().name().equalsIgnoreCase("DUPLICATE_UNIQUE_TAG"))
					&& (!validationError.getCode().name().equalsIgnoreCase("WRONG_PARENT_TAG"))
					&& (!validationError.getCode().name().equalsIgnoreCase("CSS_SYNTAX_INVALID_DECLARATION"))
					&& (!validationError.getSpecUrl().equalsIgnoreCase(""))) {
				errorList.add(validationError);
			}
		}

		if (count >= 1) {
			if (errorList.size() >= 1) {
				actualStatus = "INVALID AMP";
				m.put("Errors", logErrors.toString());
				if (logErrors.toString().contains("AMP validation had errors:")) {
					m.put("ErrorCount", String.valueOf(logErrors.size() - 1));
					m.put("Warnings", warningList.toString());
				}

			} else {
				actualStatus = "INVALID AMP";
				m.put("Errors", logErrors.toString());
				if (logErrors.toString().contains("AMP validation had errors:")) {
					m.put("ErrorCount", String.valueOf(logErrors.size() - 1));
					m.put("Warnings", warningList.toString());
				}
			}
		} else if (validatorStatus.equalsIgnoreCase("FAIL")) {
			inputHtml = inputHtml.replace("html ?", "html amp");
			Document document = Jsoup.parse(inputHtml);
			boolean html = !document.select("html[amp]").isEmpty();
			boolean head = !document.select("head").isEmpty();
			boolean body = !document.select("body").isEmpty();
			boolean scriptAmpJS = !document.select("head > script[src=https://cdn.ampproject.org/v0.js][async]")
					.isEmpty();
			boolean linkCanonical = !document.select("head > link[rel=canonical][href^=http]").isEmpty();
			boolean metaCharset = !document.select("head > meta[charset=utf-8]").isEmpty();
			boolean metaViewport = !document.select("head > meta[name=viewport][content*=width=device-width]")
					.isEmpty();
			boolean ampBoilerplateStyles = !document.select("head > style[amp-boilerplate]").isEmpty();
			boolean ampBoilerplateStylesNoScript = !document.select("head > noscript > style[amp-boilerplate]")
					.isEmpty();

			boolean linkAMPHtml = !document.select("head > link[rel=amphtml][href^=http]").isEmpty();
			boolean metaRobots = !document.select("head > meta[name=robots]").isEmpty();

			if (linkAMPHtml && metaRobots) {
				actualStatus = "LINKED AMP";
				m.put("Errors", "N/A");
				m.put("ErrorCount", "N/A");
				m.put("Warnings", warningList.toString());
			} else if (inputHtml.contains("<!doctype html>") && html && head && body && scriptAmpJS
					&& ampBoilerplateStylesNoScript && linkCanonical && metaCharset && metaViewport
					&& ampBoilerplateStyles) {
				actualStatus = "VALID AMP";
				m.put("Errors", "N/A");
				m.put("ErrorCount", "N/A");
				m.put("Warnings", warningList.toString());
			} else {
				actualStatus = "NOT AN AMP";
				m.put("Errors", "N/A");
				m.put("ErrorCount", "N/A");
				m.put("Warnings", "N/A");
			}
		} else {
			actualStatus = validatorStatus;
			if (actualStatus == "PASS") {
				actualStatus = "VALID AMP";
				m.put("Errors", "N/A");
				m.put("ErrorCount", "N/A");
				m.put("Warnings", warningList.toString());
			} else {
				actualStatus = "UNKNOWN PAGE";
				m.put("Errors", "N/A");
				m.put("ErrorCount", "N/A");
				m.put("Warnings", "N/A");
			}
		}
		m.put("Status", actualStatus);
		return m; // [PASS, FAIL, LINKED, UNKNOWN, ERROR]
	}
}