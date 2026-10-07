package netCoreLogics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

@Component("LIC11870_PJT1012_PE_NLP9f9d69b7-85e5-48cc-ad86-1bc74a1423f1")
public class CompareTwoJSON implements Nlp {
	@InputParams({ @InputParam(name = "Json 1", type = "java.lang.String"),
			@InputParam(name = "Json 2", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.util.Map")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String json1Path = (String) programElementsInput.get("Json 1");
		String json2Path = (String) programElementsInput.get("Json 2");
		Map resultMap = null;
		try {
			JsonNode json1 = readJsonFile(json1Path);
			JsonNode json2 = readJsonFile(json2Path);
			resultMap = compareJsonNodes(json1, json2);
			if (resultMap == null) {
				nlpResponseModel.setMessage("Successfully Compared the data");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("Data is not matching");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Fail to fetch" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", resultMap);
		return nlpResponseModel;
	}

	private static Map compareJsonNodes(JsonNode json1, JsonNode json2) {
		Map map = null;
		if (json1.equals(json2)) {
			System.out.println("The JSON files are identical.");
		} else {
			System.out.println("The JSON files have differences:");
			map = findDifferences(json1, json2, "");
		}
		return map;
	}

	private static Map findDifferences(JsonNode json1, JsonNode json2, String parent) {
		LinkedHashMap resultMap = new LinkedHashMap();
		Iterator<Map.Entry<String, JsonNode>> fields = json1.fields();
		while (fields.hasNext()) {
			LinkedHashMap innerMap = new LinkedHashMap();
			Map.Entry<String, JsonNode> entry = fields.next();
			String fieldName = entry.getKey();
			JsonNode node1 = entry.getValue();
			JsonNode node2 = json2.get(fieldName);

			if (node2 == null) {
				System.out.println(
						"Field '" + fieldName + "' is missing in the second JSON at " + getPath(parent, fieldName));
			} else if (node1.isObject()) {
				findDifferences(node1, node2, getPath(parent, fieldName));
			} else if (!node1.equals(node2)) {
//				System.out.println("Different value at " + getPath(parent, fieldName) + ":");
//				System.out.println("First JSON value: " + node1);
//				System.out.println("Second JSON value: " + node2);
				innerMap.put("First JSON value: " + node1, "Second JSON value: " + node2);
				resultMap.put("Different value at " + getPath(parent, fieldName) + ":", innerMap);
			}
		}
		fields = json2.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			String fieldName = entry.getKey();
			if (json1.get(fieldName) == null) {
				System.out.println(
						"Field '" + fieldName + "' is missing in the first JSON at " + getPath(parent, fieldName));
			}
		}
		return resultMap;
	}

	private static String getPath(String parent, String fieldName) {
		return parent.isEmpty() ? fieldName : parent + "." + fieldName;
	}

	private static JsonNode readJsonFile(String filePath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(new File(filePath));
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
}