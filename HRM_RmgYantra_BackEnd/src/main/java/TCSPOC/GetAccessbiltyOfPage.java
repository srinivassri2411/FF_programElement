package TCSPOC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;


import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j

public class GetAccessbiltyOfPage implements Nlp {
	static List l = new ArrayList();

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
		try {
			Results results = null;
			// Analyze accessibility
			WebDriver driver = nlpRequestModel.getWebDriver();
			log.info("Analyzing accessibility...");
			results = new AxeBuilder().analyze(driver);
			log.info("result---------------" + results);
			// Print violations
			printViolations(results);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Accessiblity is " + l);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to get Accessiblity" + e);
		}
		nlpResponseModel.getAttributes().put("string3", l);
		return nlpResponseModel;
	}

	public static void printViolations(Results results) {
		int violationsCount = results.getViolations().size();
		System.out.println("Number of Violations: " + violationsCount);
		for (int i = 0; i < violationsCount; i++) {
			Rule rule = results.getViolations().get(i);
			/*
			 * log.info("Violation " + (i + 1) + ":"); log.info("Description: " +
			 * rule.getDescription()); log.info("Help: " + rule.getHelp());
			 * log.info("Impact: " + rule.getImpact());
			 * log.info("-------------------------------------------");
			 */
			l.add("Violation " + (i + 1) + ":" + " Description: " + rule.getDescription() + " Help: " + rule.getHelp()
					+ " Impact: " + rule.getImpact());
		}
	}
	// Required dependency

	/*
	 * <dependency> <groupId>com.deque.html.axe-core</groupId>
	 * <artifactId>selenium</artifactId> <version>4.1.0</version> </dependency>
	 */

}
