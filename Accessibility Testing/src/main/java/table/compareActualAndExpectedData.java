package table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class compareActualAndExpectedData implements Nlp {
	@InputParams({ @InputParam(name = "actualHeaderCount", type = "java.lang.String"),
			@InputParam(name = "expectedHeaderCount", type = "java.lang.String"),
			@InputParam(name = "actualHeaderOrder", type = "java.lang.String"),
			@InputParam(name = "expectedHeaderOrder", type = "java.lang.String"),
			@InputParam(name = "actualHeaderNames", type = "java.lang.String"),
			@InputParam(name = "expectedHeaderNames", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String actualHeaderCount = (String) programElementsInput.get("actualHeaderCount");
		String expectedHeaderCount = (String) programElementsInput.get("expectedHeaderCount");
		String actualHeaderOrder = (String) programElementsInput.get("actualHeaderOrder");
		String expectedHeaderOrder = (String) programElementsInput.get("expectedHeaderOrder");
		String actualHeaderNames = (String) programElementsInput.get("actualHeaderNames");
		String expectedHeaderNames = (String) programElementsInput.get("expectedHeaderNames");

		String result = "";

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			result = compareHeaders1(actualHeaderCount, expectedHeaderCount, actualHeaderOrder, expectedHeaderOrder,
					actualHeaderNames, expectedHeaderNames);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully compare the actual and expected data");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);

			nlpResponseModel.setMessage("Failed to compare the actual and expected data " + e);
		}

		nlpResponseModel.getAttributes().put("result", result);
		return nlpResponseModel;
	}

	public static String compareHeaders(String actualCountStr, String expectedCountStr, String actualOrder,
			String expectedOrder, String actualNames, String expectedNames) {
		StringBuilder message = new StringBuilder();

		// Normalize input for actual and expected header names
		List<String> actualHeaders = normalizeInput(actualNames);
		List<String> expectedHeaders = normalizeInput(expectedNames);
		List<String> expectedLevels = normalizeInput(expectedOrder.replaceAll("[\\[\\]]", ""));

		// Convert header counts from String to Integer
		int actualCount = Integer.parseInt(actualCountStr.trim());
		int expectedCount = Integer.parseInt(expectedCountStr.trim());

		// Compare header counts
		if (actualCount != expectedCount) {
			message.append("Mismatch in header count. Expected ").append(expectedCount).append(", but found ")
					.append(actualCount).append(".\n");
		}

		// Check for missing expected headers with their levels
		List<String> missingHeaders = new ArrayList<>();
		for (int i = 0; i < expectedHeaders.size(); i++) {
			String expectedHeader = expectedHeaders.get(i);
			String expectedLevel = expectedLevels.size() > i ? expectedLevels.get(i) : "unknown";
			boolean found = false;

			for (String actualHeader : actualHeaders) {
				// Extract the header name and level from the actual header
				String[] parts = actualHeader.split("heading", 2);
				String actualHeaderName = parts[0].trim();
				String actualHeaderLevel = parts.length > 1 ? parts[1].trim() : "";

				// Check for a match on the header name
				if (actualHeaderName.equalsIgnoreCase(expectedHeader)) {
					found = true;

					// Check if the level matches
					if (!actualHeaderLevel.equalsIgnoreCase(expectedLevel)) {
						message.append("Header \"").append(expectedHeader).append("\" is expected to be at  ")
								.append(expectedLevel).append(", but found at  ").append(actualHeaderLevel)
								.append(".\n");
					}
					break; // Exit the loop once we find a match
				}
			}

			if (!found) {
				missingHeaders.add("Header \"" + expectedHeader + "\" is expected to be at level " + expectedLevel);
			}
		}

		if (!missingHeaders.isEmpty()) {
			message.append("Missing headers: ").append(String.join("; ", missingHeaders)).append(". ")
					.append(missingHeaders.size()).append(" headers are missing.\n");
		}

		// Check for unexpected actual headers
		List<String> unexpectedHeaders = new ArrayList<>();
		for (String actualHeader : actualHeaders) {
			boolean isExpected = expectedHeaders.stream()
					.anyMatch(expectedHeader -> actualHeader.toLowerCase().startsWith(expectedHeader.toLowerCase()));
			if (!isExpected) {
				unexpectedHeaders.add(actualHeader);
			}
		}

		if (!unexpectedHeaders.isEmpty()) {
			message.append("Unexpected headers found: ").append(String.join(", ", unexpectedHeaders)).append(".\n");
		}

		// Compare expected and actual header levels
		List<String> actualOrderList = normalizeInput(actualOrder.replaceAll("[\\[\\]]", ""));
		if (!actualOrderList.equals(expectedLevels)) {
			message.append("Mismatch in header levels. Expected levels: ").append(expectedLevels)
					.append(", but found levels: ").append(actualOrderList).append(".\n");
		}

		// If no mismatches or missing headers
		if (message.length() == 0) {
			return "Headers and levels match as expected.";
		}

		return message.toString();
	}

	/*
	 * // Utility function to normalize input by handling varying whitespace private
	 * static List<String> normalizeInput(String input) { return
	 * Arrays.stream(input.split("\n")).map(line -> line.trim().replaceAll("\\s+",
	 * " ")) // Replace multiple // spaces with a // single space
	 * .collect(Collectors.toList()); }
	 */

	public static String compareHeaders1(String actualCountStr, String expectedCountStr, String actualOrder,
			String expectedOrder, String actualNames, String expectedNames) {
		StringBuilder message = new StringBuilder();

		List<String> actualHeaders = normalizeInput(actualNames);
		List<String> expectedHeaders = normalizeInput(expectedNames);

		int actualCount = Integer.parseInt(actualCountStr.trim());
		int expectedCount = Integer.parseInt(expectedCountStr.trim());

		// Compare header counts
		if (actualCount != expectedCount) {
			message.append("Mismatch in header count. Expected ").append(expectedCount).append(", but found ")
					.append(actualCount).append(".\n");
		}

		// Check for missing expected headers
		for (String expectedHeader : expectedHeaders) {
			if (!actualHeaders.stream().anyMatch(header -> header.contains(expectedHeader))) {
				message.append("Missing header: \"").append(expectedHeader).append("\" is expected to be at  ")
						.append(getExpectedLevel(expectedHeader, expectedOrder)).append(".\n");
			}
		}

		// Check for unexpected actual headers
		for (String actualHeader : actualHeaders) {
		      String cleanedHeader = actualHeader.replaceAll("(?i)page \\d+", "").replaceAll("(?i)column \\d+", "").replaceAll("(?i)column+", "").replaceAll("^\\d+/\\d+", "").trim();
	            String headerName = cleanedHeader.split("heading")[0].trim();
			if (!expectedHeaders.contains(headerName)) {
				message.append("Unexpected header found: \"").append(headerName).append("\".\n");
			}
		}

		// Compare expected and actual header levels if order information is provided
		List<String> actualOrderList = normalizeInput(actualOrder.replaceAll("[\\[\\]]", ""));
		List<String> expectedOrderList = normalizeInput(expectedOrder.replaceAll("[\\[\\]]", ""));

		for (int i = 0; i < Math.min(actualOrderList.size(), expectedOrderList.size()); i++) {
			if (!actualOrderList.get(i).equalsIgnoreCase(expectedOrderList.get(i))) {
				message.append("Header \"").append(expectedHeaders.get(i)).append("\" is expected to be at ")
						.append(expectedOrderList.get(i)).append(", but found at  ").append(actualOrderList.get(i))
						.append(".\n");
			}
		}

		if (message.length() == 0) {
			// System.out.println("Headers and levels match as expected.");
			return "Headers and levels match as expected.";
		} else {
			// System.out.println(message.toString());
			return message.toString();
		}

	}

	private static String getExpectedLevel(String header, String expectedOrder) {
		List<String> expectedLevels = normalizeInput(expectedOrder.replaceAll("[\\[\\]]", ""));
		// Logic to determine expected level based on header name or index
		// For simplicity, return the first level (or implement more complex logic if
		// needed)
		return expectedLevels.size() > 1 ? expectedLevels.get(1) : "unknown"; // Adjust index based on your needs
	}

	private static List<String> normalizeInput(String input) {
		return Arrays.stream(input.split("[,\n]")).map(String::trim).filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}
}
