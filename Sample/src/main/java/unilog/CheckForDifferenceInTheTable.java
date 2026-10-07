package unilog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPec74a8a7-ae7a-4df9-83af-d4def55bc65e")
@Slf4j
public class CheckForDifferenceInTheTable implements Nlp {
	@InputParams({ @InputParam(name = "Xpath", type = "java.lang.String"),
			@InputParam(name = "Expected Color Value", type = "java.lang.String"),
			@InputParam(name = "Default Color Value", type = "java.lang.String"),
			@InputParam(name = "Different Or Similar", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.lang.Boolean")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String Xpath = (String) programElementsInput.get("Xpath");
		String expectedColorValue = (String) programElementsInput.get("Expected Color Value");
		String defaultColorValue = (String) programElementsInput.get("Default Color Value");
		String diifOrSimi = (String) programElementsInput.get("Different Or Similar");
		Boolean resultData = false;
		try {
			WebDriver driver = nlpRequestModel.getWebDriver();
			resultData = checkForDuplicates(Xpath, driver, expectedColorValue, defaultColorValue, diifOrSimi);
			nlpResponseModel.setMessage("Sucessfully checked and validated the table with expected value");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (IllegalArgumentException e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Fail to check " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", resultData);
		return nlpResponseModel;
	}

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

	public static boolean checkForDuplicates(String xpath, WebDriver driver, String expectedColorValue,
			String defaultColorValue, String diifOrSimi) {
		List<WebElement> noRows = driver.findElements(By.xpath(xpath));
		for (int i = 1; i < noRows.size(); i++) {
//	            log.info("Row Count is => " + i);
			String rowName = driver.findElement(By.xpath("(" + xpath + ")[" + i + "]/td[not(@class)]/b")).getText();
			List<String> texts = driver.findElements(By.xpath("(" + xpath + ")[" + i + "]/td[@class]")).stream()
					.map(WebElement::getText).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
			List<String> color = driver.findElements(By.xpath("(" + xpath + ")[" + i + "]/td[@class]")).stream()
					.map(element -> element.getCssValue("background-color")).filter(StringUtils::isNotEmpty)
					.collect(Collectors.toList());
			if (texts.size() == color.size()) {
				int diffSize = new HashSet<>(texts).size();
//	                log.info(diffSize);
				if (diifOrSimi.equalsIgnoreCase("Similar")) {
					if (diffSize > 0 && diffSize <= 2) {
						Map<String, List<Integer>> colorIndexes = getDuplicateIndicesGrouped(color);
						for (Map.Entry<String, List<Integer>> entry : colorIndexes.entrySet()) {
							if (entry.getValue().size() > 1) {
								checkValues(color, entry.getValue(), expectedColorValue);
							} else {
								checkValues(color, entry.getValue(), defaultColorValue);
							}
						}
					} else {
						for (String value : color) {
							if (!value.trim().equals(defaultColorValue)) {
								throw new IllegalArgumentException(
										"List contains a value that is not the expected value: " + value
												+ " at the row name: " + rowName);
							}
						}
					}
				} else if (diifOrSimi.equalsIgnoreCase("Different")) {
					if (diffSize >= 2) {
						Map<String, List<Integer>> textIndexes = getDuplicateIndicesGrouped(texts);
						Map<String, List<Integer>> colorIndexes = getDuplicateIndicesGrouped(color);
						log.info(textIndexes + "");
						log.info(colorIndexes + "");
						log.info("---------------------------");
						for (Map.Entry<String, List<Integer>> entry : colorIndexes.entrySet()) {
							log.info("current=> " + entry.getValue());
							if (entry.getValue().size() >= 2 && !entry.getKey().equalsIgnoreCase(defaultColorValue)) {
								checkValues(color, entry.getValue(), expectedColorValue);
							} else if (entry.getValue().size() >= 2
									&& entry.getKey().equalsIgnoreCase(defaultColorValue)) {
								checkValues(color, entry.getValue(), defaultColorValue);
							} else if (entry.getValue().size() < 2
									&& entry.getKey().equalsIgnoreCase(defaultColorValue)) {
								checkValues(color, entry.getValue(), defaultColorValue);
							} else if (entry.getValue().size() < 2
									&& !entry.getKey().equalsIgnoreCase(defaultColorValue)) {
								checkValues(color, entry.getValue(), expectedColorValue);
							} else {
								throw new IllegalArgumentException("An Invalid parameter => " + entry.getKey() + " -- "
										+ entry.getValue() + " at the row name: " + rowName);
							}
						}
					} else {
						for (String value : color) {
							if (!value.trim().equals(defaultColorValue)) {
								throw new IllegalArgumentException(
										"List contains a value that is not the expected value: " + value
												+ " at the row name: " + rowName);
							}
						}
					}
				} else {
					throw new IllegalArgumentException("Invalid parameter used" + " at the row name: " + rowName);
				}
			} else {
				throw new IllegalArgumentException("List size doesn't match: texts => " + texts.size()
						+ " & color size => " + color.size() + " at the row name: " + rowName);
			}
		}
		return true;
	}

	public static Map<String, List<Integer>> getDuplicateIndicesGrouped(List<String> list) {
		Map<String, List<Integer>> indexMap = new LinkedHashMap<>();
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			indexMap.putIfAbsent(str, new ArrayList<>());
			indexMap.get(str).add(i);
		}
		return indexMap;
	}

	public static void checkValues(List<String> list, List<Integer> index, String expectedValue)
			throws IllegalArgumentException {
		for (int i : index) {
			log.info(list.get(i).trim() + "====>" + expectedValue);
			if (!list.get(i).trim().equals(expectedValue)) {
				throw new IllegalArgumentException(
						"List contains a value that is not the expected value: " + list.get(i).trim());
			}
		}
	}

//	public static void main(String[] args) throws NlpException {
//		NlpRequestModel req = new NlpRequestModel();
//		req.getAttributes().put("Sheet Url",
//				"https://docs.google.com/spreadsheets/d/1wCPp0FzjKO5eYCvPVw3-PBg72QI153rs0hX8bDU81LU/edit");
//		req.getAttributes().put("Sheet Name", "Vertical");
//		req.getAttributes().put("unique Data", "SC001");
//		req.getAttributes().put("unique Data Index", "0");
//		req.getAttributes().put("isTableVertical", "true");
//		log.info(
//				((Map) new GetDataFromSingleRowData().execute(req).getAttributes().get("Return")).get("Test_Case_ID"));
//	}
}