package integrationLogics;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class UpdateFailResultAIO implements Nlp {
	@InputParams({ @InputParam(name = "AioURL", type = "java.lang.String"),
			@InputParam(name = "AioAuth", type = "java.lang.String"),
			@InputParam(name = "AioProjectKeyId", type = "java.lang.String"),
			@InputParam(name = "AioCycleId", type = "java.lang.String"),
			@InputParam(name = "AioTestCasePrefix", type = "java.lang.String"),
			@InputParam(name = "Jira UserName", type = "java.lang.String"),
			@InputParam(name = "Jira Token", type = "java.lang.String"),
			@InputParam(name = "Jira BaseURL", type = "java.lang.String"),
			@InputParam(name = "Excel Path", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String aIOBaseURL = (String) programElementsInput.get("AioURL");
		String aIOAuth = (String) programElementsInput.get("AioAuth");
		String aIOProjectKeyId = (String) programElementsInput.get("AioProjectKeyId");
		String aIOCycleId = (String) programElementsInput.get("AioCycleId");
		String aIOTCId = (String) programElementsInput.get("AioTestCasePrefix");
		String jiraUN = (String) programElementsInput.get("Jira UserName");
		String JiraToken = (String) programElementsInput.get("Jira Token");
		String jiraBaseURL = (String) programElementsInput.get("Jira BaseURL");
		String excelPath = (String) programElementsInput.get("Excel Path");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			aIOAuth = "AioAuth " + aIOAuth;
			Map<String, List<Map<String, String>>> tcResultListAll = getScripts(excelPath, aIOTCId);
			if (!tcResultListAll.get("FAIL").isEmpty()) {
				List<Map<String, String>> tcResultList = tcResultListAll.get("FAIL");
				System.out.println("tcResultList" + tcResultList);
				List<String> defectList = tcResultList.stream()
						.map(mapvalue -> mapvalue.values().stream().toList().get(0).trim()).toList();
				System.out.println(defectList.size());
				String encodedValue = getEncodedValue(jiraUN, JiraToken);
				Map<String, String> issueIdMap = getIssueIdMap(defectList, jiraBaseURL, encodedValue);
				System.out.println("issueIdMap" + issueIdMap);
				Map<String, Integer> runIDList = updateTestRunsAndGetIds(aIOBaseURL, aIOProjectKeyId, aIOCycleId,
						aIOAuth, convert(tcResultList));
				System.out.println("runIDList" + runIDList);
				for (Map<String, String> mapValue : tcResultList) {
					for (Map.Entry<String, String> entry : mapValue.entrySet()) {
						ArrayList<Integer> defectId = new ArrayList<>();
						defectId.add(Integer.parseInt(issueIdMap.get(entry.getValue()).toString().trim()));
						updateTestRunStatus(aIOBaseURL, aIOAuth, aIOProjectKeyId, aIOCycleId,
								Integer.parseInt(runIDList.get(entry.getKey()).toString().trim()), "Failed", defectId);
					}
				}
			}
			if (!tcResultListAll.get("PASS").isEmpty()) {
				List<Map<String, String>> tcResultList = tcResultListAll.get("PASS");
				System.out.println("tcResultList" + tcResultList);
				for (Map<String, String> resultMap : tcResultList) {
					String aIOMapPostBody = createJsonFromMap(resultMap);
					Response pushedMapResult = postRequestWithHeader(aIOBaseURL, aIOMapPostBody,
							"/project/" + aIOProjectKeyId + "/testcycle/" + aIOCycleId + "/bulk/testrun/update",
							"Authorization###" + aIOAuth);
					if (pushedMapResult.getStatusCode() > 205
							|| getJsonStringValue(pushedMapResult.getBody().asString(), "status")
									.equals("SUCCESS_WITH_ERROR")) {
						throw new Exception("Failed=>> " + pushedMapResult.getBody().asString());
					} else {
						System.out.println("Created and pass=>> " + pushedMapResult.getBody().asString());
					}
				}
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Given failed results is pushed to aio ");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to update => " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		return nlpResponseModel;
	}

	public static String getJsonStringValue(String jsonBody, String jsonPath) {
		String value = "";
		try {
			value = JsonPath.read(jsonBody, jsonPath).toString().trim();
		} catch (Exception ignored) {
		}
		return value;
	}

	public static Response postRequestWithHeader(String baseUrl, String requestBody, String endPoint,
			String... headersKey_Value) {
		RequestSpecification requestSpecification = RestAssured.given();
		for (String keyValue : headersKey_Value) {
			String[] keyValueArr = keyValue.split("###");
			requestSpecification.header(keyValueArr[0], keyValueArr[1]);
		}
		String url = baseUrl + endPoint;
		return requestSpecification.body(requestBody).contentType(ContentType.JSON).when().post(url).then().extract()
				.response();
	}

	public static void updateTestRunStatus(String baseUrl, String authToken, String projectKey, String testCycleId,
			long testRunId, String status, List<Integer> jiraDefectIds) {

		String endpoint = String.format("%s/project/%s/testcycle/%s/testrun/%d", baseUrl, projectKey, testCycleId,
				testRunId);
		System.out.println(endpoint);
		Map<String, Object> requestBody = Map.of("ID", testRunId, "testRunStatus", Map.of("name", status),
				"jiraDefectIDs", jiraDefectIds);
		System.out.println(requestBody);
		Response response = RestAssured.given().header("Authorization", authToken)
				.header("Content-Type", "application/json").body(requestBody).when().post(endpoint);

		// Fail fast if request is not successful
		if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
			throw new RuntimeException("Failed to update test run. Status: " + response.getStatusCode() + ", Response: "
					+ response.getBody().asString());
		}
	}

	public static String getEncodedValue(String username, String apiToken) {

		String valueToEncode = username + ":" + apiToken;

		return Base64.getEncoder().encodeToString(valueToEncode.getBytes());
	}

	public static Map<String, String> getIssueIdMap(List<String> issueKeys, String baseUrl, String encodedValue) {
		Map<String, String> result = new HashMap<>();

		for (String key : issueKeys) {
			try {
				Response response = RestAssured.given().header("Authorization", "Basic " + encodedValue).when()
						.get(baseUrl + key).then().extract().response();

				if (response.statusCode() == 200) {
					String id = response.jsonPath().getString("id");
					String returnedKey = response.jsonPath().getString("key");

					if (id != null && returnedKey != null) {
						result.put(returnedKey, id);
					}
				} else {
					System.err.println("Failed for key: " + key + " HTTP: " + response.statusCode());
				}

			} catch (Exception e) {
				System.err.println("Error processing key: " + key);
				e.printStackTrace();
			}
		}

		return result;
	}

	public static Map<String, Integer> updateTestRunsAndGetIds(String baseUrl, String projectKey, String testCycleKey,
			String authToken, List<Map<String, String>> testRuns) {

		String endpoint = String.format("%s/project/%s/testcycle/%s/bulk/testrun/update", baseUrl, projectKey,
				testCycleKey);
		System.out.println(endpoint);
		System.out.println("Token" + authToken);
		// Build request body dynamically
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("testRuns", testRuns);
		System.out.println("requestBody" + requestBody);
		Response response = given().header("accept", "application/json;charset=utf-8")
				.header("Authorization", authToken).header("Content-Type", "application/json").body(requestBody).when()
				.post(endpoint).then().statusCode(200).extract().response();

		// Extract processedData as a Map
		Map<String, Map<String, Object>> processedData = response.jsonPath().getMap("processedData");

		// Final result map
		Map<String, Integer> result = new HashMap<>();

		for (Map.Entry<String, Map<String, Object>> entry : processedData.entrySet()) {
			String testCaseKey = entry.getKey();
			Integer id = (Integer) entry.getValue().get("ID");
			result.put(testCaseKey, id);
		}

		return result;
	}

	public static String getAioID(String fullString, String partialID) {
		if (fullString == null || fullString.isEmpty()) {
			throw new IllegalArgumentException("Full string cannot be null or empty.");
		}
		if (partialID == null || partialID.isEmpty()) {
			throw new IllegalArgumentException("Partial ID cannot be null or empty.");
		}

		String regex = "(" + Pattern.quote(partialID) + ")-(\\d+)[-_ ]?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(fullString);

		if (matcher.find()) {
			return matcher.group(1) + "-" + matcher.group(2);
		}

		return null;
	}

	public static Map<String, List<Map<String, String>>> getScripts(String filePath, String aIOTCId) throws Exception {

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		result.put("PASS", new ArrayList<>());
		result.put("FAIL", new ArrayList<>());

		try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0);
			Row header = sheet.getRow(0);

			Map<String, Integer> columnIndex = new HashMap<>();

			for (Cell cell : header) {
				columnIndex.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
			}

			int jiraCol = columnIndex.get("Jira ID");
			int statusCol = columnIndex.get("Status");
			int scriptCol = columnIndex.get("Script Name");

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);

				if (row == null) {
					continue;
				}

				String jiraId = getCellValue(row.getCell(jiraCol)).trim();
				String status = getCellValue(row.getCell(statusCol)).trim().toUpperCase();
				String scriptName = getCellValue(row.getCell(scriptCol)).trim();

				if (scriptName.isEmpty()) {
					continue;
				}

				if (status.equals("PASSED")) {
					status = "PASS";
				}

				if (!status.equals("PASS") && !status.equals("FAIL")) {
					continue;
				}

				String tcId = getAioID(scriptName, aIOTCId);

				Map<String, String> entry = new HashMap<>();

				if (status.equals("PASS")) {
					entry.put(tcId, "Passed");
				} else {
					entry.put(tcId, jiraId);
				}

				result.get(status).add(entry);
			}
		}

		return result;
	}

	public static String createJsonFromMap(Map<String, String> caseMap) throws JSONException {
		JSONArray resultsArray = new JSONArray();

		for (Map.Entry<String, String> entry : caseMap.entrySet()) {
			JSONObject caseObject = new JSONObject();
			caseObject.put("testCaseKey", entry.getKey());
			caseObject.put("testRunStatus", entry.getValue());
			resultsArray.put(caseObject);
		}

		JSONObject finalJson = new JSONObject();
		finalJson.put("testRuns", resultsArray);

		return finalJson.toString(2);
	}

	// Helper method
	private static String getCellValue(Cell cell) {
		if (cell == null)
			return null;

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf((long) cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return null;
		}
	}

	public static List<Map<String, String>> convert(List<Map<String, String>> input) {
		List<Map<String, String>> result = new ArrayList<>();

		for (Map<String, String> m : input) {
			for (String key : m.keySet()) {
				Map<String, String> obj = new HashMap<>();
				obj.put("testCaseKey", key);
				obj.put("testRunStatus", "Failed");
				result.add(obj);
			}
		}

		return result;
	}

	public static Map<String, String> getAioStatusMap() {
		Map<String, String> statusMap = new LinkedHashMap<>();
		statusMap.put("FAIL", "Failed");
		statusMap.put("PASS", "Passed");
		statusMap.put("WARNING", "Blocked");
		return statusMap;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("AioURL", "https://tcms.aiojiraapps.com/aio-tcms/api/v1");
		model.getAttributes().put("AioAuth",
				"Yjk5NTRhODYtNDdiMS0zN2I2LTk2MjgtNDUxN2ZkYjQxNzE2LjU2ZTZlN2E1LTJiN2QtNGYzYS04MWFjLTZjYmZmZDY2Y2U0OA==");
		model.getAttributes().put("AioProjectKeyId", "BC");
		model.getAttributes().put("AioCycleId", "BC-CY-241");
//		model.getAttributes().put("AioCycleId", "BC-CY-327");
		model.getAttributes().put("AioTestCasePrefix", "BC-TC");
		model.getAttributes().put("Jira UserName", "akshatham@sonicwall.com");
		model.getAttributes().put("Jira Token",
				"ATATT3xFfGF0mf_nZbdZYpq9SmwOEBJGzDG2UHKwycIuWzULinDbHy8k37VVDEtwIJCGOoLrsNrIT-nyiUMPqUKwqlHMoM-LfkPVQYvnudk3DuRW3LmzYZSWdUzxkpS6SxqK_h-lm359ip_2Fywft0MqESlddo9ZjdM-07g_2Bx9tWIIWx6eCqI=8AB7D6ED");
		model.getAttributes().put("Jira BaseURL", "https://sonicwall.atlassian.net/rest/api/3/issue/");
		model.getAttributes().put("Excel Path", "C:\\Users\\User\\Downloads\\EXEC78462_Excel_File.xlsx");
		System.out.println(new UpdateFailResultAIO().execute(model) + "");
	}
}