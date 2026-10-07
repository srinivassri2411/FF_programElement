package businessLogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class dsfvdsfsd {
	public static void main(String[] args) {
		try {
			List<String[]> list = getCurrentResult(null, null, "EXEC26934", 3);
			for (String[] va : list) {
				for (int i = 0; i < va.length; i++) {
					System.out.println(va[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> getCurrentResult(String token, String projId, String executionId, int totalCount)
			throws JsonMappingException, JsonProcessingException, InterruptedException {
		String body = null;
		List<String[]> data = new ArrayList<>();
		while (true) {
			System.out.println("Next node please");
			body = new Scanner(System.in).nextLine();
			System.out.println(body);
			List<Object> size = JsonPath.parse(body).read("$.responseObject.treeDataList[?(@.executionId=='"
					+ executionId + "' && @.type=='Script')].status");
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(body);
			Map<String, String> map = new LinkedHashMap<String, String>();
			extractKeyValue(node, "", map);
			int slno = 1;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
//				log.info("Key: " + key + ", Value: " + map.get(key));
				if (key.endsWith("type") && map.get(key).contains("Script")) {
					System.out.println(key);
					String moduleName = map.get(key.replace("type", "path"))
							.substring(0, map.get(key.replace("type", "path")).lastIndexOf("/")).replaceFirst("/", "")
							.replaceAll("Root/", "");
					String scriptName = map.get(key.replace("type", "path")).substring(
							map.get(key.replace("type", "path")).lastIndexOf("/") + 1,
							map.get(key.replace("type", "path")).length());
					String status = map.get(key.replace("type", "status"));
					String exeTime = map.get(key.replace("type", "executionDurationInHourMinSecFormat"));
					data.add(new String[] { slno + "", moduleName, scriptName, status, exeTime });
					slno++;
				}
			}
			Thread.sleep(1000);
			try {
				if (size.get(totalCount - 1).toString().equalsIgnoreCase("Running")) {
					break;
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return data;
	}

	public static void extractKeyValue(JsonNode node, String path, Map<String, String> map) {
		if (node.isObject()) {
			node.fields().forEachRemaining(entry -> {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				String newPath = path.isEmpty() ? key : path + "." + key;
				extractKeyValue(value, newPath, map);
			});
		} else if (node.isArray()) {
			for (int i = 0; i < node.size(); i++) {
				JsonNode arrayElement = node.get(i);
				extractKeyValue(arrayElement, path + "[" + i + "]", map);
			}
		} else {
			map.put(path, node.asText());
		}
	}

}
