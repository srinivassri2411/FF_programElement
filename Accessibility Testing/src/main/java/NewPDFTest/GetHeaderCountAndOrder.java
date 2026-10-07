package NewPDFTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class GetHeaderCountAndOrder implements Nlp {
	@InputParams({ @InputParam(name = "pdfPath", type = "java.lang.String") })

	@ReturnType(name = "ouput", type = "java.util.List")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("pdfPath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		// String filePath = "C:\\CIBC\\extractedPDFText.txt";

		List ouput = new ArrayList<>();
		Map<String, Integer> headingCounts = new LinkedHashMap<>();
		List<String> headingOrder = new ArrayList<>();
		headingCounts.put("Level 1", 0);
		headingCounts.put("Level 2", 0);
		headingCounts.put("Level 3", 0);
		headingCounts.put("Level 4", 0);
		headingCounts.put("Level 5", 0);
		Map<String, Integer> filteredCounts = new LinkedHashMap<>();
		try {

			countHeadingLevels(filePath, headingCounts, headingOrder);
			int totalHeaders = headingCounts.values().stream().mapToInt(Integer::intValue).sum();
			headingCounts.put("Total Headers", totalHeaders);

			filteredCounts = filterHeadingCounts(headingCounts);
			// System.out.println(headingOrder);
			// System.out.println(filteredCounts);
			ouput.add(filteredCounts);
			ouput.add(headingOrder);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Adobe Acrobat has been launched. Continuing with pdf");
		} catch (Exception e) {
			System.out.println("Error reading the file: " + e.getMessage());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to open Adobe Acrobat, Continuing with pdf");
		}

		nlpResponseModel.getAttributes().put("ouput", ouput);
		return nlpResponseModel;
	}

	public static void countHeadingLevels(String filePath, Map<String, Integer> headingCounts,
			List<String> headingOrder) throws IOException {

		// Regex to capture headings (e.g., "heading level 1", "heading level 2", etc.)
		Pattern pattern = Pattern.compile("heading\\s+level\\s+(\\d)");

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Match the pattern against the line
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					// Get the level from the regex group
					String level = matcher.group(1);

					String headingKey = "Level " + level;

					// Count the heading and add to the order list
					headingCounts.put(headingKey, headingCounts.get(headingKey) + 1);
					headingOrder.add(headingKey); // Always add to the list

					/*
					 * switch (level) { case "1": headingCounts.put("Level 1",
					 * headingCounts.get("Level 1") + 1); break; case "2":
					 * headingCounts.put("Level 2", headingCounts.get("Level 2") + 1); break; case
					 * "3": headingCounts.put("Level 3", headingCounts.get("Level 3") + 1); break; }
					 */
				}
			}
		}
	}

	public static Map<String, Integer> filterHeadingCounts(Map<String, Integer> headingCounts) {
		Map<String, Integer> filteredCounts = new LinkedHashMap<>();
		headingCounts.forEach((key, value) -> {
			if (value > 0) {
				filteredCounts.put(key, value);
			}
		});
		return filteredCounts;
	}

}
