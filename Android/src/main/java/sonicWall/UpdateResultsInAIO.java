
package sonicWall;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class UpdateResultsInAIO implements Nlp {
	@InputParams({ @InputParam(name = "AioURL", type = "java.lang.String"),
			@InputParam(name = "AioAuth", type = "java.lang.String"),
			@InputParam(name = "AioProjectKeyId", type = "java.lang.String"),
			@InputParam(name = "AioCycleId (Empty to create cycle)", type = "java.lang.String"),
			@InputParam(name = "AioTestCasePrefix", type = "java.lang.String"),
			@InputParam(name = "FireflinkMailId", type = "java.lang.String"),
			@InputParam(name = "FireflinkPassword", type = "java.lang.String"),
			@InputParam(name = "ProjectNameGiven", type = "java.lang.String"),
			@InputParam(name = "ExecutionId", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String aIOBaseURL = (String) programElementsInput.get("AioURL");
		String aIOAuth = (String) programElementsInput.get("AioAuth");
		String aIOProjectKeyId = (String) programElementsInput.get("AioProjectKeyId");
		String aIOCycleId = (String) programElementsInput.get("AioCycleId (Empty to create cycle)");
		String aIOTCId = (String) programElementsInput.get("AioTestCasePrefix");
		String fireflinkMailId = (String) programElementsInput.get("FireflinkMailId");
		String fireflinkPassword = (String) programElementsInput.get("FireflinkPassword");
		String projectNameGiven = (String) programElementsInput.get("ProjectNameGiven");
		String executionId = (String) programElementsInput.get("ExecutionId");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			pushResults(aIOBaseURL, aIOAuth, aIOProjectKeyId, aIOCycleId, aIOTCId, fireflinkMailId, fireflinkPassword,
					projectNameGiven, executionId);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Given execution details has been updated to aio");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to update => " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void pushResults(String aIOBaseURL, String aIOAuth, String aIOProjectKeyId, String aIOCycleId,
			String aIOTCId, String fireflinkMailId, String fireflinkPassword, String projectNameGiven,
			String executionId) throws Exception {
		if (aIOBaseURL == null || aIOBaseURL.isEmpty() || aIOAuth == null || aIOAuth.isEmpty()
				|| aIOProjectKeyId == null || aIOProjectKeyId.isEmpty() || fireflinkMailId == null
				|| fireflinkMailId.isEmpty() || fireflinkPassword == null || fireflinkPassword.isEmpty()
				|| projectNameGiven == null || projectNameGiven.isEmpty() || executionId == null
				|| executionId.isEmpty() || aIOTCId == null || aIOTCId.isEmpty()) {
			throw new InvalidParameterException("Given values contain null or empty strings. Please check the inputs.");
		}
		aIOAuth = "AioAuth " + aIOAuth;
		baseURI = "https://app.fireflink.com/";
		String token = "";
		String projectId = "";
		String projectName = "";
		String suiteName = "";
		String executedOn = "";
		fireflinkPassword = encryptPassword(fireflinkPassword);
		String signInRequestPayload = "{ \"emailId\": \"" + fireflinkMailId + "\", \"password\": \"" + fireflinkPassword
				+ "\"}";
		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
				signInRequestPayload);

		String message = convertToString(getJsonValue(signInResponsePayload, "message"));

		// Provide message if EmailID/Password is Invalid
		if (message != null && message.contains("Email or Password is invalid")) {
			throw new InvalidParameterException(message);
		}
		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId,
				"Authorization", "Bearer " + token);
		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));
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
			throw new InvalidParameterException("Invalid Project name given, please try again...");
		}

		// Get All AIO TestCases
		String aIOProjects = getRequest(aIOBaseURL + "/project/" + aIOProjectKeyId + "/testcase", "Authorization",
				aIOAuth);
		System.out.println(aIOProjects);
		List<Object> listOfTCIds = getJsonListValue(aIOProjects, "$.items[*].key");
		System.out.println(listOfTCIds);

		// GetScriptDetails
		String allScriptDetails = getRequest("/alltrees/optimize/v1/trees/modules/?sort=true", "Authorization",
				"Bearer " + token, "projectId", projectId);
		List<Object> scriptTitles = getJsonListValue(allScriptDetails,
				"responseObject.moduleTree[*]..children[?(@.type=='Script')].title");

		// Map results
		Map<String, String> getAioStatusMap = getAioStatusMap();

		// Get ExeId details
		String expandAllResponsePayload = getRequest(
				"executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId, "Authorization",
				"Bearer " + token, "projectId", projectId);
		System.out.println(expandAllResponsePayload);

		// check for AIO cycleID
		suiteName = getJsonStringValue(expandAllResponsePayload, "responseObject.suiteName");
		executedOn = getJsonStringValue(expandAllResponsePayload, "responseObject.ExecutedOn");
		if (aIOCycleId.isEmpty()) {
			System.out.println(".....Creating new cycle now......");
			Response createdCycle = postRequestWithHeader(aIOBaseURL, createCycleBody(suiteName, executedOn),
					"/project/" + aIOProjectKeyId + "/testcycle/detail", "Authorization###" + aIOAuth);
			aIOCycleId = getJsonStringValue(createdCycle.body().asString(), "key");
		}
		System.out.println("AIOCycleId===>" + aIOCycleId);

		//
		List<Object> resultsList = getJsonListValue(expandAllResponsePayload,
				"responseObject.executionResponses[*].executionStatusObject");
		System.out.println(resultsList);
		LinkedHashMap<String, String> allResult = new LinkedHashMap<>();
		for (Object result : resultsList) {
			HashMap<String, String> resultMap = (HashMap<String, String>) result;
			for (Map.Entry<String, String> resultMapIterator : resultMap.entrySet()) {
				String scriptID = resultMapIterator.getKey();
				if (scriptID.contains("SCR")) {
					String VALUE = resultMapIterator.getValue();
					String aioID = getAioID(
							scriptTitles.stream().filter(scriptTitle -> scriptTitle.toString().contains(scriptID))
									.collect(Collectors.toList()).get(0).toString(),
							aIOTCId);
					if (aioID != null && aioID.isEmpty()) {
						throw new NullPointerException("Error script name format error");
					}
					allResult.put(aioID, getAioStatusMap.get(VALUE));
				}
			}
		}
		String aIOMapPostBody = createJsonFromMap(allResult);
		try {
			validateKeysInList(listOfTCIds, allResult);
			Response pushedMapResult = postRequestWithHeader(aIOBaseURL, aIOMapPostBody,
					"/project/" + aIOProjectKeyId + "/testcycle/" + aIOCycleId + "/bulk/testrun/update",
					"Authorization###" + aIOAuth);
			if (pushedMapResult.getStatusCode() > 205
					|| getJsonStringValue(pushedMapResult.getBody().asString(), "status").equals("SUCCESS_WITH_ERROR"))
				throw new Exception("Failed=>> " + pushedMapResult.getBody().asString());
			else
				System.out.println("Created and pass=>> " + pushedMapResult.getBody().asString());
		} catch (IllegalArgumentException e) {
			throw new Exception("Failed=>> " + e);
		}
	}

	private static String createCycleBody(String suiteName, String executionTime) throws JSONException {
		JSONObject finalJson = new JSONObject();
		finalJson.put("title", "FireFlink_" + suiteName + "_" + executionTime);
		return finalJson.toString(2);
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

	public static Map<String, String> getAioStatusMap() {
		Map<String, String> statusMap = new LinkedHashMap<>();
		statusMap.put("FAIL", "Failed");
		statusMap.put("PASS", "Passed");
		statusMap.put("WARNING", "Blocked");
		return statusMap;
	}
//    public static Response getRequest(String baseUrl, String endPoint, String... headersKey_Value) {
//        RequestSpecification requestSpecification = RestAssured.given();
//        if (headersKey_Value.length > 0) {
//            for (String keyValue : headersKey_Value) {
//                String[] keyValueArr = keyValue.split("###");
//                requestSpecification.header(keyValueArr[0], keyValueArr[1]);
//            }
//        }
//        Response response = requestSpecification.contentType(ContentType.JSON).when().get(baseUrl + endPoint).then().extract().response();
//
//        return response;
//    }

	public static Properties getProperty(InputStream inputStream) {
		Properties pro = new Properties();
		try {
			pro.load(inputStream);
			return pro;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		String value = "";
		try {
			value = JsonPath.read(jsonBody, jsonPath).toString().trim();
		} catch (Exception ignored) {
		}
		return value;
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
		return request.when().get(endpoint).then().assertThat().statusCode(200).extract().asString();
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
		return request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint).then().assertThat()
				.statusCode(200).extract().asString();
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

	public static String getJsonListStringValue(String json, String jsonPath) {
		List<Integer> ids = JsonPath.read(json, "$." + jsonPath);
		return ids.isEmpty() ? null : String.valueOf(ids.get(0));
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
		model.getAttributes().put("AioURL", "https://tcms.aiojiraapps.com/aio-tcms/api/v1");
		model.getAttributes().put("AioAuth",
				"NWNmODI2ZGItOTNjOC0zMjAyLTljMGItYjllZWRmNWYwMjY3LjM0Nzc1ODA0LTQ3NjctNDBhZC1hNjcwLTA1ZmEyNjQ3MjA4NQ==");
		model.getAttributes().put("AioProjectKeyId", "AA");
		model.getAttributes().put("AioCycleId", "");
		model.getAttributes().put("AioTestCaseId", "");
		model.getAttributes().put("FireflinkMailId", "srinivas.m@testyantra.com");
		model.getAttributes().put("FireflinkPassword", "Srini@123");
		model.getAttributes().put("ProjectNameGiven", "Android");
		model.getAttributes().put("ExecutionId", "EXEC3151");
		System.out.println(new UpdateResultsInAIO().execute(model));
	}
}