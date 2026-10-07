package emvigo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPb93f90de-01b1-4c3d-8553-cb98755d0556")
public class ValidateTwoDataSame implements Nlp {
	@InputParams({ @InputParam(name = "Input 1", type = "java.lang.String"),
			@InputParam(name = "Input 2", type = "java.lang.String") })
	@ReturnType(name = "Output", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		String input1 = (String) programElementsInput.get("Input 1");
		String input2 = (String) programElementsInput.get("Input 2");
		String result = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			result = compareInputs(input1, input2);
			System.out.println(result.substring(result.indexOf("=") + 1, result.indexOf(",")));
			if (result.substring(result.indexOf("=") + 1, result.indexOf(",")).contains("true")) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Both the inputs are same");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Both the inputs are not same");
			}

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Something went wrong " + e.getLocalizedMessage());
		}
		nlpResponseModel.getAttributes().put("Output", result);
		return nlpResponseModel;
	}

	public static String compareInputs(String input1, String input2) {
		input1 = unescapeJson(input1);
		input2 = unescapeJson(input2);

		if (isValidJson(input1) && isValidJson(input2)) {
			return compareJsonKeys(input1, input2);
		} else {
			return compareStrings(input1, input2);
		}
	}

	private static boolean isValidJson(String str) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.readTree(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static String unescapeJson(String str) {
		// Remove extra escaping for JSON strings
		return str.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	private static String compareStrings(String str1, String str2) {
		boolean isKeySame = str1.equals(str2);
		return "{isKeySame=" + isKeySame + ",}";
	}

	private static String compareJsonKeys(String json1, String json2) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode node1 = objectMapper.readTree(json1);
			JsonNode node2 = objectMapper.readTree(json2);

			Set<String> keys1 = getAllKeys(node1, "");
			Set<String> keys2 = getAllKeys(node2, "");

			Set<String> uniqueToJson1 = new HashSet<>(keys1);
			uniqueToJson1.removeAll(keys2);

			Set<String> uniqueToJson2 = new HashSet<>(keys2);
			uniqueToJson2.removeAll(keys1);

			// Add array comparison logic here
			if (node1.isArray() && node2.isArray()) {
				Set<String> arrayDifferences1 = new HashSet<>();
				Set<String> arrayDifferences2 = new HashSet<>();
				for (int i = 0; i < Math.max(node1.size(), node2.size()); i++) {
					if (i < node1.size() && i < node2.size()) {
						if (!node1.get(i).equals(node2.get(i))) {
							arrayDifferences1.add(node1.get(i).toString());
							arrayDifferences2.add(node2.get(i).toString());
						}
					} else if (i < node1.size()) {
						arrayDifferences1.add(node1.get(i).toString());
					} else if (i < node2.size()) {
						arrayDifferences2.add(node2.get(i).toString());
					}
				}
				uniqueToJson1.addAll(arrayDifferences1);
				uniqueToJson2.addAll(arrayDifferences2);
			}

			boolean isKeySame = uniqueToJson1.isEmpty() && uniqueToJson2.isEmpty();
			return "{isKeySame=" + isKeySame + ", uniqueToJson1=" + uniqueToJson1 + ", uniqueToJson2=" + uniqueToJson2
					+ "}";
		} catch (Exception e) {
			return "Error comparing JSONs: " + e.getMessage();
		}
	}

	private static Set<String> getAllKeys(JsonNode node, String parentPath) {
		Set<String> keys = new HashSet<>();

		if (node.isObject()) {
			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				String fullPath = parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName;
				keys.add(fullPath);
				keys.addAll(getAllKeys(node.get(fieldName), fullPath));
			}
		} else if (node.isArray()) {
			for (int i = 0; i < node.size(); i++) {
				String arrayPath = parentPath + "[" + i + "]";
				keys.addAll(getAllKeys(node.get(i), arrayPath));
			}
		}

		return keys;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
//		String input1 = "{\"CreateCrossSellLeadResult\":{\"Data\":{\"CommSuccessMsg\":null,\"IsCommSent\":false,\"IsSaved\":false,\"Message\":\"Insufficient Talktime to create cross-sell Lead\",\"ReferralId\":null,\"StatusCode\":0},\"Error\":null,\"ErrorCode\":0}}";
//		String input2 = "{\"CreateCrossSellLeadResult\":{\"Data\":{\"CommSuccessMsg\":null,\"IsCommSent\":false,\"IsSaved\":false,\"Message\":\"Insufficient Talktime to create cross-sell Lead\",\"ReferralId\":null,\"StatusCode\":0},\"Error\":null,\"ErrorCode\":0}}";

//		String input1 = "Hello World";
//		String input2 = "Hello World";
//		String input2 = "[{\"SlotId\":1,\"Limit\":0,\"AppointmentCount\":1,\"ProcessId\":0,\"Id\":0,\"SelectedDate\":\"2024-11-16T00:00:00\",\"IsActive\":false}]";
//		String input1 = "[{\"SlotId\":1,\"Limit\":0,\"AppointmentCount\":1,\"ProcessId\":0,\"Id\":0,\"SelectedDate\":\"2024-11-16T00:00:00\",\"IsActive\":false}]";
//		String input2 = "[1,\"2\",\"abc\"]";
//		String input1 = "[\"1\",\"2\",\"abc\"]";
//		String input2 = "[]";
//		String input1 = "[]";
//		programElementsInput.put("Input 1", input1);
//		programElementsInput.put("Input 2", input2);
		ValidateTwoDataSame aaa = new ValidateTwoDataSame();
		System.out.println(aaa.execute(nlpRequestModel));

	}

}