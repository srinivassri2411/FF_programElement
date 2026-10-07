package table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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




public class compareFieldInDocument implements Nlp {
	@InputParams({ @InputParam(name = "filePath", type = "java.lang.String"),
			@InputParam(name = "expectedFields", type = "java.lang.String") })
	// @ReturnType(name = "mapData", type = "java.util.Map")
	@ReturnType(name = "returnData", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String filePath = (String) programElementsInput.get("filePath");
		String expectedFields = (String) programElementsInput.get("expectedFields");
	//	Map<String, List<String>> returnValue = null;
		String returnData = "";
		try {

			// Convert expected output string to a list by splitting on '#'
			List<String> expectedList = Arrays.asList(expectedFields.split("#\\s*")); // Split by '#' and optional space

			System.out.println("Expected List: " + expectedList);

			List<String> result = processFile(filePath);

			// Compare the processed list with the expected list
		//	returnValue = compareLists(result, expectedList);
			returnData = compareLists1(result, expectedList);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Succesfully compared and Validated  PDF");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

			// Uncomment the below line and modify the message, only if you want to override
			// the fail message defined for the program element.
			nlpResponseModel.setMessage("Failed to  compare nd Validated  PDF");
		}

		// Your program element business logic ends here ...
		// nlpResponseModel.getAttributes().put("mapData", returnValue);
		nlpResponseModel.getAttributes().put("returnData", returnData);

		return nlpResponseModel;
	}

	// Method to process the file and return a list of processed lines
	private static List<String> processFile(String filePath) {
		List<String> result = new ArrayList<>(); // List to store processed lines

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			// Regular expression to match both "edit **** blank" and "edit blank"
			String pattern = "edit\\s+.*?\\s+blank";
			Pattern regex = Pattern.compile(pattern);

			while ((line = reader.readLine()) != null) {
				Matcher matcher = regex.matcher(line);

				// Case 1: If the line contains "radio button not checked", replace it with
				// "<RB>"
				if (line.contains("radio button  not checked")) {
					line = line.replace("radio button  not checked", "<RB>");
				}
				// Case 2: If the line contains "combo box", replace it with "<DD>"
				else if (line.contains("combo box  1/1")) {
					line = line.replace("combo box  1/1", "<DD>");
				}
				// Case 3: If the line contains "edit **** blank" or "edit blank", replace with
				// "<TF>"
				else if (matcher.find()) {
					line = line.replace(matcher.group(0), "<TF>");
				}

				// Remove any spaces between the field text and tags (like "/Number <TF>"
				// becomes "/Number<TF>")
				line = line.replaceAll("\\s+<", "<"); // Removes spaces before the placeholder tags

				// Trim the line
				line = line.trim();

				// Add only lines containing '<' to the result list
				if (line.contains("<")) { // Only add lines that contain '<'
					result.add(line); // Add to result list
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Method to compare the processed lines with the expected list
	private static Map<String, List<String>> compareLists(List<String> result, List<String> expectedList) {
		List<String> orderMismatch = new ArrayList<>();
		List<String> dataMismatch = new ArrayList<>();
		List<String> matchingData = new ArrayList<>();
		List<String> finalresult = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<>();
		Boolean status = true;
		// Check for missing data
		for (String expected : expectedList) {
			if (!result.contains(expected)) {
				dataMismatch.add(expected); // Add to data mismatch if missing
			}
		}

		// Check for order mismatch and matching data
		for (int i = 0; i < Math.min(result.size(), expectedList.size()); i++) {
			if (result.get(i).equals(expectedList.get(i))) {
				matchingData.add(result.get(i)); // Add to matching data if they match
			} else {
				orderMismatch.add("Expected: " + expectedList.get(i) + ", Found: " + result.get(i));
			}
		}

		// Print the processed lines stored in the list in the same order
		System.out.println("Processed Lines:");
		for (String l : result) {
			System.out.println(l);
		}

		// Print matching data
		if (!matchingData.isEmpty()) {
			System.out.println("\nMatching Data:");
			for (String match : matchingData) {
				System.out.println(match);
			}
		} else {
			System.out.println("\nNo Matching Data.");
			status = false;
		}

		// Print any data mismatches
		if (!dataMismatch.isEmpty()) {
			System.out.println("\nData Mismatch:");
			for (String missing : dataMismatch) {
				System.out.println("Missing: " + missing);
				status = false;

			}
		} else {
			System.out.println("\nNo Data Mismatches.");
		}

		// Print any order mismatches
		if (!orderMismatch.isEmpty()) {
			System.out.println("\nOrder Mismatch:");
			for (String mismatch : orderMismatch) {
				System.out.println(mismatch);
				status = false;
			}
		} else {
			System.out.println("\nNo Order Mismatches.");
		}

		map.put("MatchingData", matchingData);
		map.put("OrderMismatchData", orderMismatch);
		map.put("DataMissing", dataMismatch);

		if (status) {
			finalresult.add("pass");
		} else {
			finalresult.add("fail");
		}
		map.put("status", finalresult);

		return map;

	}

	private static String compareLists1(List<String> result, List<String> expectedList) {
		List<String> orderMismatch = new ArrayList<>();
		List<String> dataMismatch = new ArrayList<>();
		List<String> matchingData = new ArrayList<>();
		List<String> finalresult = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<>();
		Boolean status = true;
		// Check for missing data
		for (String expected : expectedList) {
			if (!result.contains(expected)) {
				dataMismatch.add(expected); // Add to data mismatch if missing
			}
		}

		// Check for order mismatch and matching data
		for (int i = 0; i < Math.min(result.size(), expectedList.size()); i++) {
			if (result.get(i).equals(expectedList.get(i))) {
				matchingData.add(result.get(i)); // Add to matching data if they match
			} else {
				orderMismatch.add("Expected: " + expectedList.get(i) + ", Found: " + result.get(i));
			}
		}

		// Print the processed lines stored in the list in the same order
		System.out.println("Processed Lines:");
		for (String l : result) {
			System.out.println(l);
		}

		// Print matching data
		if (!matchingData.isEmpty()) {
			System.out.println("\nMatching Data:");
			for (String match : matchingData) {
				System.out.println(match);
			}
		} else {
			System.out.println("\nNo Matching Data.");
			status = false;
		}

		// Print any data mismatches
		if (!dataMismatch.isEmpty()) {
			System.out.println("\nData Mismatch:");
			for (String missing : dataMismatch) {
				System.out.println("Missing: " + missing);
				status = false;

			}
		} else {
			System.out.println("\nNo Data Mismatches.");
		}

		// Print any order mismatches
		if (!orderMismatch.isEmpty()) {
			System.out.println("\nOrder Mismatch:");
			for (String mismatch : orderMismatch) {
				System.out.println(mismatch);
				status = false;
			}
		} else {
			System.out.println("\nNo Order Mismatches.");
		}
		if (status) {
			finalresult.add("pass");
		} else {
			finalresult.add("fail");
		}

		StringBuilder bui = new StringBuilder();
		bui.append("MatchingData ").append("-->").append(matchingData).append("\n\n");
		bui.append("OrderMismatchData ").append("-->").append(orderMismatch).append("\n\n");
		bui.append("DataMissing ").append("-->").append(dataMismatch).append("\n\n");
		bui.append("Status ").append("-->").append(finalresult).append("\n\n");
		return bui.toString();

	}
}
