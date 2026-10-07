package end2end;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CompareActualAndExpectedHeaders implements Nlp {
	@InputParams({ @InputParam(name = "Extracted Headers File Path", type = "java.lang.String"),
			@InputParam(name = "Expected Header Order(Example:-H1,H2,H2)", type = "java.lang.String"),
			@InputParam(name = "Expected Header Names", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		String extractedHeaderFilePath = (String) programElementsInput.get("Extracted Headers File Path");
		String expectedHeaderOrder = (String) programElementsInput.get("Expected Header Order(Example:-H1,H2,H2)");
		String expectedHeaderNames = (String) programElementsInput.get("Expected Header Names");

		String result = "";

		try {
			List co = GetHeaderCountAndOrder(extractedHeaderFilePath);
			Map<String, Integer> headers = (Map<String, Integer>) co.get(0);
			String actualHeaderCount = headers.get("Total Headers").toString();
			String actualHeaderOrder = co.get(1).toString();
			String actualHeaderNames = gettextFromFile(extractedHeaderFilePath);
			String[] headersArray = expectedHeaderOrder.replace("[", "").replace("]", "").replaceAll("\\s+", "")
					.split(",");

			System.out.println(headersArray);

			String expectedHeaderCount = String.valueOf(headersArray.length);
			System.out.println("expected count " + expectedHeaderCount);

			result = compareHeaders1(actualHeaderCount, expectedHeaderCount, actualHeaderOrder, expectedHeaderOrder,
					actualHeaderNames, expectedHeaderNames);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully compare the actual and expected data");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);

			nlpResponseModel.setMessage("Failed to compare the actual and expected data " + e);
		}

		System.out.println("result->>>>>>>>>.       " + result);
		nlpResponseModel.getAttributes().put("result", result);
		return nlpResponseModel;
	}

	public static String compareHeaders1(String actualCountStr, String expectedCountStr, String actualOrder,
			String expectedOrder, String actualNames, String expectedNames) {
		StringBuilder message = new StringBuilder();

		List<String> actualHeaders = normalizeInput1(actualNames);
		List<String> expectedHeaders = normalizeInput1(expectedNames);

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
			System.out.println("Actual Header " + actualHeader);
//			String cleanedHeader = actualHeader.replaceAll("(?i)page \\d+", "").replaceAll("(?i)column \\d+", "")
//					.replaceAll("(?i)column+", "").replaceAll("^\\d+/\\d+", "").trim();
			String cleanedHeader = actualHeader.replaceAll("(?i)^.*?\\bpage \\d+", "").replaceAll("(?i)column \\d+", "")
					.replaceAll("(?i)column+", "").replaceAll("^\\d+/\\d+", "").trim();

			System.out.println("Cleaned Header " + cleanedHeader);
			String headerName = cleanedHeader.split("heading")[0].trim();
			if (!expectedHeaders.contains(headerName)) {
				System.out.println(expectedHeaders);
				System.out.println(headerName);
				message.append("Unexpected header found: \"").append(headerName).append("\".\n");
			}
		}

		// Compare expected and actual header levels if order information is provided
		List<String> actualOrderList = normalizeInput(actualOrder.replaceAll("[\\[\\]]", ""));
		List<String> expectedOrderList = normalizeInput(expectedOrder.replaceAll("[\\[\\]]", ""));

		System.out.println("expected " + expectedOrderList);
		System.out.println("Actual   " + actualOrderList);

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

	private static List<String> normalizeInput1(String input) {
		return Arrays.stream(input.split("[\n]")).map(String::trim).filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}

	public static List GetHeaderCountAndOrder(String filePath) {

		List ouput = new ArrayList<>();
		Map<String, Integer> headingCounts = new LinkedHashMap<>();
		List<String> headingOrder = new ArrayList<>();
		headingCounts.put("H1", 0);
		headingCounts.put("H2", 0);
		headingCounts.put("H3", 0);
		headingCounts.put("H4", 0);
		headingCounts.put("H5", 0);
//		headingCounts.put("Level 1", 0);
//		headingCounts.put("Level 2", 0);
//		headingCounts.put("Level 3", 0);
//		headingCounts.put("Level 4", 0);
//		headingCounts.put("Level 5", 0);
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

			System.out.println("Actual header count and order is " + ouput);
		} catch (Exception e) {
			System.out.println("Failed to get header count and order " + e);
		}

		return ouput;

	}

	public static void countHeadingLevels(String filePath, Map<String, Integer> headingCounts,
			List<String> headingOrder) throws IOException {

		Pattern pattern = Pattern.compile("heading\\s+level\\s+(\\d)");

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String level = matcher.group(1);
//					String headingKey = "Level " + level;
					String headingKey = "H" + level;

					headingCounts.put(headingKey, headingCounts.get(headingKey) + 1);
					headingOrder.add(headingKey); // Always add to the list

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

	public static String gettextFromFile(String FilePath) {
		String Content = "";

		try {
			Content = Files.readString(Paths.get(FilePath), StandardCharsets.UTF_8);
			System.out.println("Copied content from text file is : " + Content);
		}

		catch (java.nio.charset.MalformedInputException e) {
			try {
				Content = Files.readString(Paths.get(FilePath), StandardCharsets.ISO_8859_1);
				System.out.println("Copied content from text file is : " + Content);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Not able to copy content from text file" + e);
			}

		}

		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Not able to copy content from text file" + e);
		}

		System.out.println(Content);
		return Content;
	}

	public static void main1(String[] args) {
		CompareActualAndExpectedHeaders sdcf = new CompareActualAndExpectedHeaders();
		List aqs = sdcf.GetHeaderCountAndOrder("C:\\CIBC\\6249A_ActualHeaders.txt");
		System.out.println("qawsedfvg " + aqs);

		Map<String, Integer> headers = (Map<String, Integer>) aqs.get(0);
		String actualHeaderCount = headers.get("Total Headers").toString();
		String actualHeaderOrder = aqs.get(1).toString();
//		String actualHeaderNames = 
		System.out.println(actualHeaderCount);
		System.out.println(actualHeaderOrder);

	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		CompareActualAndExpectedHeaders rr = new CompareActualAndExpectedHeaders();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		programElementsInput.put("Extracted Headers File Path", "C:\\CIBC\\6249A_ActualHeaders.txt");
		programElementsInput.put("Expected Header Order(Example:-H1,H2,H2)",
				"H1, H2, H2, H2, H2, H2, H2, H2, H2, H3, H3, H3, H2, H3, H3, H3, H3, H3, H2, H3, H3, H3, H3, H3, H3, H2, H2, H3, H3, H3, H3, H3, H3, H3, H3, H3, H3, H3");
		programElementsInput.put("Expected Header Names",
				"Application for Creditor Insurance  for CIBC Personal Loans\r\n"
						+ "Step 1 – Information about you and your CIBC Personal Loan\r\n"
						+ "Step 2 – Insurance selection and average cost of insurance , per payment, for your personal loan\r\n"
						+ "Step 3 – Please read carefully before signing\r\n" + "Introduction\r\n" + "Who can apply\r\n"
						+ "When your Insurance begins\r\n" + "When your Insurance ends\r\n" + "Life Insurance\r\n"
						+ "Description of the Life Insurance benefit\r\n"
						+ "Special Provisions for Replacement Loans\r\n" + "What your Life Insurance costs\r\n"
						+ "Disability Insurance\r\n" + "Definition of Disability/Disabled\r\n"
						+ "Description of the Disability Insurance benefit\r\n"
						+ "When your Disability Insurance benefits begin\r\n"
						+ "When do your Disability Insurance benefit payments end\r\n"
						+ "What your Disability Insurance costs\r\n" + "CIBC Payment Protector Insurance\r\n"
						+ "Definition of Disability\r\n" + "Definition of Job Loss\r\n"
						+ "Description of the Job Loss Insurance benefit\r\n"
						+ "When your Job Loss Insurance benefits begin\r\n"
						+ "When your Job Loss Insurance benefits end\r\n"
						+ "What your CIBC Payment Protector Insurance costs\r\n"
						+ "When an Insurance benefit will not be paid\r\n"
						+ "Additional information regarding your Certificate of Insurance\r\n"
						+ "How to cancel your insurance coverage\r\n" + "How do you make a claim\r\n"
						+ "Who is the beneficiary of your insurance\r\n" + "Change of Insurer\r\n"
						+ "Other things you should know about your Insurance\r\n" + "Canada Life Complaints Process\r\n"
						+ "Provisions regarding legal action\r\n" + "Protecting your personal information\r\n"
						+ "Information about CIBC\r\n" + "The Group Policy\r\n"
						+ "How to contact Canada Life and CIBC Creditor Helpline");
		NlpResponseModel nlpresponceModel = rr.execute(nlpRequestModel);
//		System.out.println("repponec" + nlpresponceModel);
		System.out.println("output " + nlpresponceModel.getAttributes().get("result"));
	}
}
