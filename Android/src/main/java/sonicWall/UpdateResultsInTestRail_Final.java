
package sonicWall;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class UpdateResultsInTestRail_Final implements Nlp {
	@InputParams({ @InputParam(name = "TestRail URL", type = "java.lang.String"),
			@InputParam(name = "TestRail UserName", type = "java.lang.String"),
			@InputParam(name = "TestRail Password", type = "java.lang.String"),
			@InputParam(name = "Mapping file", type = "java.lang.String"),
			@InputParam(name = "TestRail Project Name", type = "java.lang.String"),
			@InputParam(name = "FireflinkMailId", type = "java.lang.String"),
			@InputParam(name = "FireflinkPassword", type = "java.lang.String"),
			@InputParam(name = "ProjectNameGiven", type = "java.lang.String"),
			@InputParam(name = "Suite Name", type = "java.lang.String") })
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String trBaseURL = (String) programElementsInput.get("TestRail URL");
			String tRUname = (String) programElementsInput.get("TestRail UserName");
			String tRPassword = (String) programElementsInput.get("TestRail Password");
			String mapFile = (String) programElementsInput.get("Mapping file");
			String fireflinkMailId = (String) programElementsInput.get("FireflinkMailId");
			String fireflinkPassword = (String) programElementsInput.get("FireflinkPassword");
			String projectNameGiven = (String) programElementsInput.get("ProjectNameGiven");
			String suiteName = (String) programElementsInput.get("Suite Name");
			String tRProjectName = (String) programElementsInput.get("TestRail Project Name");
			pushResults(trBaseURL, fireflinkMailId, fireflinkPassword, projectNameGiven, suiteName, tRUname, tRPassword,
					projectNameGiven, mapFile, tRProjectName);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Given execution details has been updated to aio");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			e.printStackTrace();
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	private void pushResults(String trBaseURL, String fireflinkMailId, String fireflinkPassword,
			String projectNameGiven, String suiteName, String tRUname, String tRPassword, String projectName,
			String mapFile, String tRProjectName) throws Exception {
		baseURI = "https://app.fireflink.com/";
		String tRAuth = encodeBasicAuth(tRUname, tRPassword);
		fireflinkPassword = encryptPassword(fireflinkPassword);
		String signInRequestPayload = "{ \"emailId\": \"" + fireflinkMailId + "\", \"password\": \"" + fireflinkPassword
				+ "\"}";
		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
				signInRequestPayload);

		String message = convertToString(getJsonValue(signInResponsePayload, "message"));

		// Provide message if EmailID/Password is Invalid
		if (message != null && message.contains("Email or Password is invalid")) {
			System.err.println(message);
			throw new InvalidParameterException(message);
		}
		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
		String token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId,
				"Authorization", "Bearer " + token);
		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));
		String projectId = null;
		for (int i = 0; i < noOfProjects; i++) {
			projectName = convertToString(
					getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.name"));
			if (projectName.equals(projectNameGiven)) {
				projectId = convertToString(
						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.id"));
				break;
			}
		}

		// If given Project doesn't exists throw error message
		if (projectId.isEmpty()) {
			System.err.println("Invalid Project name given, please try again...");
			throw new InvalidParameterException("Invalid Project name given, please try again...");
		}

		String suiteId = getSuiteId(token, suiteName, projectId);
		if (suiteId.isEmpty()) {
			throw new RuntimeException("Suite not found with name: " + suiteName);
		}
		System.out.println("Suite ID is " + suiteId);
		String executionId = getRecentExecutionId(token, suiteId, projectId);
		// System.out.println("Execution ID " + executionId);
		int totalCount = getNumberOfScriptsInSuit(token, projectId, suiteId);
		// System.out.println(totalCount + " at start");
		List<String[]> result1 = getCurrentResult(token, projectId, executionId, totalCount);
		List<Object> resultsList = new ArrayList<>();
		for (String[] row : result1) {
			HashMap<String, String> scriptMap = new HashMap<>();
			scriptMap.put(row[4], row[2]);
			resultsList.add(scriptMap);
		}
//		String expandAllResponsePayload = getRequest(
//				"executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId, "Authorization",
//				"Bearer " + token, "projectId", projectId);
////	        System.out.println(expandAllResponsePayload);
//		suiteName = getJsonStringValue(expandAllResponsePayload, "responseObject.suiteName");

		// Get TestRail Auth
		tRAuth = encodeBasicAuth(tRUname, tRPassword);

		// Get result status
		getAllProjectsResponsePayload = getRequest(trBaseURL + "/index.php?/api/v2/get_statuses", "Authorization",
				tRAuth);
		Map<String, String> tRStatusMap = getTRStatusMap(getAllProjectsResponsePayload);
//	        System.out.println(tRStatusMap);
		Properties prop = getProperty(new FileInputStream(mapFile));
		LinkedHashMap<String, String> allResult = new LinkedHashMap<>();
		for (Object result : resultsList) {
			HashMap<String, String> resultMap = (HashMap<String, String>) result;
			for (Map.Entry<String, String> resultMapIterator : resultMap.entrySet()) {
				String scriptID = resultMapIterator.getKey();
				scriptID = scriptID.substring(0, scriptID.indexOf(" -"));
				if (scriptID.contains("SCR")) {
					String VALUE = resultMapIterator.getValue();
					allResult.put(StringUtils.getDigits(prop.get(scriptID).toString()), tRStatusMap.get(VALUE));
				}
			}
		}

		// Get ProjectID
		String tRProjects = getRequest(trBaseURL + "/index.php?/api/v2/get_projects", "Authorization", tRAuth);
		String tRProjectId = getJsonStringValue(tRProjects, "projects[?(@.name=='" + tRProjectName + "')].id");

		String tRRunName;
		String tRRunId;
		// Get TestRunID or Create TestRun
//		if (tRRunName == null || tRRunName.isEmpty()) {
		String testRunName = "FireFlink_" + suiteName + "_" + executionId;
		String tRRunBody = createTestRun(testRunName, allResult.keySet());
		Response tRResponse = postRequestWithHeader(trBaseURL, tRRunBody, "/index.php?/api/v2/add_run/" + tRProjectId,
				"Authorization###" + tRAuth);
		if (tRResponse.getStatusCode() >= 201) {
			throw new RuntimeException("Test Run not created please check" + tRResponse.getBody().asPrettyString());
		}
		tRRunId = getJsonStringValue(tRResponse.getBody().asPrettyString(), "id");
//		} else {
//			String tRRunsList = getRequest(trBaseURL + "/index.php?/api/v2/get_runs/" + tRProjectId, "Authorization",
//					tRAuth);
//			tRRunId = getJsonStringValue(tRRunsList, "runs[?(@.name=='" + tRRunName + "')].id");
//		}

		String tRMapPostBody = createJsonFromMap(allResult);
		List<Object> listOfTCIds = getJsonListValue(
				getRequest(trBaseURL + "/index.php?/api/v2/get_tests/" + tRRunId, "Authorization", tRAuth),
				"tests..case_id");
		try {
//	            System.out.println(listOfTCIds);
//	            System.out.println(allResult);
			validateKeysInList(listOfTCIds, allResult);
			Response pushedMapResult = postRequestWithHeader(trBaseURL, tRMapPostBody,
					"/index.php?/api/v2/add_results_for_cases/" + tRRunId, "Authorization###" + tRAuth);
			if (pushedMapResult.getBody().asString().contains("unknown or not part of the test run"))
				throw new Exception("Failed=>> " + pushedMapResult.getBody().asString());
			else
				System.out.println("Created and pass=>> " + pushedMapResult.getBody().asString());
		} catch (IllegalArgumentException e) {
			System.out.println("Failed");
			throw new RuntimeException(e);
		}
	}

	public static String getRecentExecutionId(String token, String suiteID, String projId) {
//			System.out.println(token + "----->" + suiteID + "----->" + projId);
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", "Bearer " + token);
		header.put("projectId", projId);
		Response response = given().headers(header).when()
				.get("dashboardexecution/optimize/v1/dashboard/projects/" + projId + "/" + suiteID
						+ "?executionType=SUITE&pageNo=0&pageSize=12&archiveFlag=false")
				.then().assertThat().statusCode(200).extract().response();
		String body = response.getBody().asString();
		io.restassured.path.json.JsonPath js = new io.restassured.path.json.JsonPath(body);
		String executionId = js.get("responseObject[0]._id");
		System.out.println(executionId);
		return executionId;
	}

	public static int getNumberOfScriptsInSuit(String token, String projId, String suitId) {

		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Authorization", "Bearer " + token);
		header.put("Connection", "keep-alive");
		header.put("projectId", projId);
		Response response = given().headers(header).when().get("/project/optimize/v1/suite/" + suitId).then()
				.assertThat().statusCode(200).extract().response();
		String body = response.getBody().asString();
		io.restassured.path.json.JsonPath js = new io.restassured.path.json.JsonPath(body);
		int noOfScripts = js.getInt("responseObject.noOfScripts");
		System.out.println(noOfScripts + "");
		return noOfScripts;
	}

	public static List<String[]> getCurrentResult(String token, String projId, String executionId, int totalCount)
			throws IOException, InterruptedException {
		String body = null;
		List<String[]> data = new ArrayList<>();
		while (data.size() < totalCount - 1) {

			Map<String, Object> header = new HashMap<>();
			header.put("Access-Control-Allow-Origin", "*");
			header.put("Authorization", "Bearer " + token);
			header.put("Connection", "keep-alive");
			header.put("projectId", projId);
			Response response = given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId).then()
					.assertThat().statusCode(200).extract().response();
			given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/sync/" + executionId);
			body = response.getBody().asString();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(body);
			Map<String, String> map = new LinkedHashMap<String, String>();
			extractKeyValue(node, "", map);
			int slno = 1;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
//					System.out.println("Key: " + key + ", Value: " + map.get(key));
				if (key.endsWith("type") && map.get(key).contains("Script")) {
					String moduleName = map.get(key.replace("type", "path"))
							.substring(0, map.get(key.replace("type", "path")).lastIndexOf("/")).replaceFirst("/", "")
							.replaceAll("Root/", "");
//	                    String scriptName = map.get(key.replace("type", "path")).substring(
//	                            map.get(key.replace("type", "path")).lastIndexOf("/") + 1,
//	                            map.get(key.replace("type", "path")).length());
					String status = map.get(key.replace("type", "status"));
					String exeTime = map.get(key.replace("type", "executionDurationInHourMinSecFormat"));
					String title = map.get(key.replace("type", "title"));
					data.add(new String[] { slno + "", moduleName, status, exeTime, title });
					slno++;
				}
			}
			Thread.sleep(1000);
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

	public static String getSuiteId(String token, String suiteName, String projId) {
		String allSuiteResponse = given().header("Authorization", "Bearer " + token).header("projectId", projId).when()
				.get("project/optimize/v1/suite/getAll/all").then().extract().asString();
		System.out.println("allSuiteResponse" + allSuiteResponse);
		io.restassured.path.json.JsonPath jsonPath = new io.restassured.path.json.JsonPath(allSuiteResponse);
		int objectCount = jsonPath.getInt("responseObject.size()");
		int count = 0;
		String suiteID = null;
		for (int i = 0; i < objectCount; i++) {
			String name = jsonPath.getString("responseObject[" + i + "].name");
			if (name.contains(suiteName)) {
				suiteID = jsonPath.get("responseObject[" + count + "].id").toString();
				break;
			} else {
				count++;
			}
		}
		return suiteID;
	}

	public static Map<String, String> getTRStatusMap(String getAllProjectsResponsePayload) {
		Map<String, String> statusMap = new LinkedHashMap<>();
		statusMap.put("FAIL", getJsonListStringValue(getAllProjectsResponsePayload, "[?(@.label=='Failed')].id"));
		statusMap.put("PASS", getJsonListStringValue(getAllProjectsResponsePayload, "[?(@.label=='Passed')].id"));
		statusMap.put("WARNING", getJsonListStringValue(getAllProjectsResponsePayload, "[?(@.label=='Retest')].id"));
		return statusMap;
	}
//	    public static Response getRequest(String baseUrl, String endPoint, String... headersKey_Value) {
//	        RequestSpecification requestSpecification = RestAssured.given();
//	        if (headersKey_Value.length > 0) {
//	            for (String keyValue : headersKey_Value) {
//	                String[] keyValueArr = keyValue.split("###");
//	                requestSpecification.header(keyValueArr[0], keyValueArr[1]);
//	            }
//	        }
//	        Response response = requestSpecification.contentType(ContentType.JSON).when().get(baseUrl + endPoint).then().extract().response();
	//
//	        return response;
//	    }

	public static Properties getProperty(InputStream inputStream) {
		Properties pro = new Properties();
		try {
			pro.load(inputStream);
			return pro;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String createTestRun(String name, Set<String> caseIds) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", name);
		jsonObject.put("include_all", false);

		JSONArray caseIdsArray = new JSONArray(caseIds);
		jsonObject.put("case_ids", caseIdsArray);

		return jsonObject.toString(4); // Pretty print with indentation
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

	public static String getJsonStringValue(String jsonBody, String jsonPath) {
		try {
			Object result = JsonPath.read(jsonBody, "$." + jsonPath);

			if (result instanceof List) {
				List<?> list = (List<?>) result;
				if (!list.isEmpty() && list.get(0) != null) {
					return list.get(0).toString().trim();
				}
			} else if (result != null) {
				return result.toString().trim();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return "";
	}

	public static List<Object> getJsonListValue(String jsonBody, String jsonPath) {
		List<Object> value = new ArrayList<>();
		try {
			value = JsonPath.read(jsonBody, jsonPath);
		} catch (Exception ignored) {
		}
		return value;
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.when().get(endpoint).asString();
	}

	private static String postRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint).asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath);
	}

	public static boolean convertToBoolean(Object obj) {
		return (boolean) obj;
	}

	public static int convertToInteger(Object obj) {
		return (int) obj;
	}

	public static String convertToString(Object obj) {
		return (String) obj;
	}

	public static double convertToDouble(Object obj) {
		return (double) obj;
	}

	public static int convertToInt(Object value) {
		if (value instanceof Double) {
			return ((Double) value).intValue();
		} else if (value instanceof Integer) {
			return (int) value;
		} else if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				try {
					return (int) Double.parseDouble((String) value);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("Cannot convert to int: " + value);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported type: " + value);
		}
	}

//	    public static String getLatestExecutionId(String jsonBody) {
//	        List<String> executionStartTimes = (List<String>) getJsonValue(jsonBody, "responseObject[*].executionStartTime");
//	        List<String> executionIds = (List<String>) getJsonValue(jsonBody, "responseObject[*]._id");
	//
//	        if (executionStartTimes.isEmpty() || executionIds.isEmpty()) {
//	            return null; // No data found
//	        }
	//
//	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
//	        String latestId = null;
//	        Date latestDate = null;
	//
//	        for (int i = 0; i < executionStartTimes.size(); i++) {
//	            try {
//	                if (!executionStartTimes.get(i).equals("N/A")) {
//	                    Date currentDate = sdf.parse(executionStartTimes.get(i));
//	                    if (latestDate == null || currentDate.after(latestDate)) {
//	                        latestDate = currentDate;
//	                        latestId = executionIds.get(i);
//	                    }
//	                }
//	            } catch (ParseException e) {
//	                e.printStackTrace();
//	            }
//	        }
//	        return latestId;
//	    }

	public static String encryptPassword(String password) throws Exception {
		byte[] publicKeyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public static String encodeBasicAuth(String userName, String password) {
		String credentials = userName + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
	}

	public static String getJsonListStringValue(String json, String jsonPath) {
		List<Integer> ids = JsonPath.read(json, "$." + jsonPath);
		return ids.isEmpty() ? null : String.valueOf(ids.get(0));
	}

	public static String createJsonFromMap(Map<String, String> caseMap) throws NumberFormatException, JSONException {
		JSONArray resultsArray = new JSONArray();

		for (Map.Entry<String, String> entry : caseMap.entrySet()) {
			JSONObject caseObject = new JSONObject();
			caseObject.put("case_id", Integer.parseInt(entry.getKey()));
			caseObject.put("status_id", Integer.parseInt(entry.getValue()));
			resultsArray.put(caseObject);
		}

		JSONObject finalJson = new JSONObject();
		finalJson.put("results", resultsArray);

		return finalJson.toString(2);
	}

	public static void validateKeysInList(List<Object> list, Map<String, String> map) {
		List<String> stringList = list.stream().map(Object::toString).collect(Collectors.toList());
		for (String key : map.keySet()) {
			if (!stringList.contains(key)) {
				throw new IllegalArgumentException("Key '" + key + "' from map is not present in the list");
			}
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("TestRail URL", "https://srini2411.testrail.io");
		model.getAttributes().put("TestRail UserName", "sevej63411@euleina.com");
		model.getAttributes().put("TestRail Password", "Srini@123");
		model.getAttributes().put("TestRail Project Name", "CSE APP");
		model.getAttributes().put("Mapping file", "C:\\Users\\User\\Desktop\\testRailMap.properties");
		model.getAttributes().put("FireflinkMailId", "srinivas.m@testyantra.com");
		model.getAttributes().put("FireflinkPassword", "Srini@123");
		model.getAttributes().put("ProjectNameGiven", "Android");
		model.getAttributes().put("Execution Id", "EXEC3320");
		System.out.println(new UpdateResultsInTestRail_Final().execute(model) + "");
	}
}