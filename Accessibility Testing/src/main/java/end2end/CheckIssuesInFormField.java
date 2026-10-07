package end2end;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

public class CheckIssuesInFormField implements Nlp {

	@InputParams({ @InputParam(name = "Actual Fields File Path", type = "java.lang.String"),
			@InputParam(name = "Expected Fields File Path", type = "java.lang.String") })
	@ReturnType(name = "returnData", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		String filePath = (String) nlpRequestModel.getAttributes().get("Actual Fields File Path");
		String ExpectedFieldsPath = (String) nlpRequestModel.getAttributes().get("Expected Fields File Path");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String returnData = "";

		try {

			String expectedFields = new String(Files.readAllBytes(Paths.get(ExpectedFieldsPath)));
			// Convert expected output string to a list
//			List<String> expectedList = Arrays.asList(expectedFields.split("\n"));
			List<String> expectedList = Arrays.stream(expectedFields.split("\\n")).map(String::trim) // Trim each line
					.collect(Collectors.toList());
//			System.out.println("Expected List: " + expectedList);

//			System.out.println("size  "+expectedList.size());

			List<String> result = processFile(filePath);
			System.out.println("result" + result);
			// Compare the processed list with the expected list
			returnData = compareLists(result, expectedList);
			if (returnData.substring(returnData.lastIndexOf("Status:"), returnData.length()).contains("PASS")) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Actual Field matching with expected fields");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Actual Field do not match with expected fields");
			}

		} catch (Exception e) {
			System.out.println(e);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to compare and validate the PDF.");
		}

		nlpResponseModel.getAttributes().put("returnData", returnData);
		return nlpResponseModel;
	}

	// Method to process the file and return a list of processed lines
	private static List<String> processFile(String filePath) {
		List<String> result = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			String pattern = "edit\\s+.*?\\s+blank";
			Pattern regex = Pattern.compile(pattern);

			while ((line = reader.readLine()) != null) {
				Matcher matcher = regex.matcher(line);

				if (line.contains("radio button  not checked")) {
					line = line.replace("radio button  not checked", "<RB>");
				} else if (line.contains("radio button  checked")) {
					line = line.replace("radio button  checked", "<RB>");
				} else if (line.contains("check box")) {
					if (line.contains("check box  not checked")) {
						line = line.replace("check box  not checked", "<CB>");
					} else {
						line = line.replace("check box", "<CB>");
					}
				}

				else if (line.contains("combo box  1/1")) {
					line = line.replace("combo box  1/1", "<DD>");
				} else if (line.contains("combo box")) {
					line = line.replace("combo box", "<DD>");
				} else if (matcher.find()) {
					line = line.replace(matcher.group(0), "<TF>");
				}

				else if (line.trim().endsWith("edit")) {
//						System.out.println("line "+line);
					// You can handle "edit" at the end of the line here, e.g., mark it differently
					line = line.replace("edit", "<TF>");
//		                System.out.println("next"+line);
				} else if (line.trim().endsWith("button")) {
//                                                System.out.println("line "+line);
					line = line.replace("button", "<B>");
//                                System.out.println("next"+line);
				}
				line = line.replaceAll("\\s+<", "<");
				line = line.trim();

				if (line.contains("<")) {
					result.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println("Actual List: " + result);

//		System.out.println("size  "+result.size());
		return result;
	}

	private static String compareLists(List<String> result, List<String> expectedList) {
		boolean status = true;
		StringBuilder MissingData = new StringBuilder();
		StringBuilder OrderMismatch = new StringBuilder();

		for (int i = 0; i < Math.min(result.size(), expectedList.size()); i++) {
			//
			if (!result.contains(expectedList.get(i).toString())) {
				if (result.get(i).equals(expectedList.get(i))) {
				} else {
//					dataMismatch.add("Expected: " + expectedList.get(i) + ", Found: " + result.get(i));
					MissingData.append(("Expected: " + expectedList.get(i) + " Found: " + result.get(i))).append("\n");
				}
			}
		}

		// Check for order mismatch and matching data
		for (int i = 0; i < Math.min(result.size(), expectedList.size()); i++) {
			if (result.contains(expectedList.get(i))) {
				if (result.get(i).equals(expectedList.get(i))) {
				} else {
//					orderMismatch.add("Expected: " + expectedList.get(i) + ", Found: " + result.get(i));
					OrderMismatch.append(("Expected: " + expectedList.get(i) + ", Found: " + result.get(i)))
							.append("\n");
				}
			}
		}

		StringBuilder builder = new StringBuilder();

		if (!MissingData.isEmpty() || !OrderMismatch.isEmpty()) {
			status = false;
			builder.append("Missing Data-->\n").append(MissingData).append("\n\n").append("Order Mismatch-->\n")
					.append(OrderMismatch);
		}

		if (status) {
			builder.append("Status: ").append("PASS").append("\n");
		} else {
			builder.append("Status: ").append("FAIL").append("\n");
		}
		System.out.println(builder.toString());

		return builder.toString();
	}

	public static void main(String[] args) throws NlpException {

		CheckIssuesInFormField ua = new CheckIssuesInFormField();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("Expected Fields File Path", "C:\\Users\\User\\Desktop\\Labelled_EXPECTED.txt");
		attributes.put("Actual Fields File Path", "C:\\Accessibility\\userConfig\\NVDARecord.txt");

		ua.execute(nl);
	}
}
