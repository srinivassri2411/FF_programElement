
package package1;

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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.apache.http.client.ClientProtocolException;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateScriptAIO implements Nlp {
	@InputParams({ @InputParam(name = "AioURL", type = "java.lang.String"),
			@InputParam(name = "AioAuth", type = "java.lang.String"),
			@InputParam(name = "AioProjectKeyId", type = "java.lang.String"),
			@InputParam(name = "AioCycleId (Empty to create cycle)", type = "java.lang.String"),
			@InputParam(name = "AioTestCasePrefix", type = "java.lang.String"),
			@InputParam(name = "FireflinkMailId", type = "java.lang.String"),
			@InputParam(name = "FireflinkPassword", type = "java.lang.String"),
			@InputParam(name = "ProjectNameGiven", type = "java.lang.String"),
			@InputParam(name = "Script Id", type = "java.lang.String"),
			@InputParam(name = "Update for (All/Pass/Fail)", type = "java.lang.String") })
	@ReturnType(name = "cycleID", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String aIOBaseURL = (String) programElementsInput.get("AioURL");
		String aIOAuth = (String) programElementsInput.get("AioAuth");
		String aIOProjectKeyId = (String) programElementsInput.get("AioProjectKeyId");
		String aIOCycleId = (String) programElementsInput.get("AioCycleId (Empty to create cycle)");
		String aIOTCId = (String) programElementsInput.get("AioTestCasePrefix");
//		String aIOBaseURL = "https://tcms.aiojiraapps.com/aio-tcms/api/v1";
//		String aIOAuth = "OGJlM2QwZjgtYmI5OS0zZjJjLWFiMjEtNzljNzQ0ZTdlN2E3LmJmMmFhMjMxLWJmY2ItNGQ3Ny04ZDk0LTM1ODFmZmY5Njg2OQ==";
//		String aIOProjectKeyId = "SCRUM"
//		String aIOTCId = "SCRUM-TC";
		String fireflinkMailId = (String) programElementsInput.get("FireflinkMailId");
		String fireflinkPassword = (String) programElementsInput.get("FireflinkPassword");
//		String projectNameGiven = "SonicWallPEs";
		String projectNameGiven = (String) programElementsInput.get("ProjectNameGiven");
		String scriptId = (String) programElementsInput.get("Script Id");
		String update = (String) programElementsInput.get("Update for (All/Pass/Fail)");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
//			List<String> scriptDetails = new ArrayList<String>();
//			scriptDetails.add("PASS");
//			scriptDetails.add("EXEC3381");
//			scriptDetails.add("SUITE1129");
			List<String> scriptDetails = getScriptDetails(scriptId);
			aIOCycleId = pushResults(aIOBaseURL, aIOAuth, aIOProjectKeyId, aIOCycleId, aIOTCId, fireflinkMailId,
					fireflinkPassword, projectNameGiven, scriptId, scriptDetails.get(0), scriptDetails.get(1),
					scriptDetails.get(2), update);
			if (aIOCycleId.contains("::")) {
				aIOCycleId = "";
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel
						.setMessage("Given execution details has not been updated to aio since condition didnt match");
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Given execution details has been updated to aio");
			}
		} catch (ClientProtocolException e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Update failed since its script level");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to update => " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("cycleID", aIOCycleId);
		return nlpResponseModel;
	}

	public static List<String> getScriptDetails(String scriptId) throws Exception {
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
				"Optimize_Dev_client");
		Query query = new Query();
		query.addCriteria(Criteria.where("scriptRunDetails.scriptId").is(scriptId));
		List<Document> stepResults = mongoTemplate.find(query, Document.class, "StepResult");
		ArrayList<String> allResults = new ArrayList<>();
		Set<String> execIDs = new LinkedHashSet<>();
		Set<String> suiteIDs = new LinkedHashSet<>();
		for (Document doc : stepResults) {
			log.info(doc.toJson());
			allResults.add(JsonPath.read(doc.toJson(), "$.status"));
			execIDs.add(JsonPath.read(doc.toJson(), "$.scriptRunDetails.executionId"));
			try {
				suiteIDs.add(JsonPath.read(doc.toJson(), "$.scriptRunDetails.suiteId"));
			} catch (Exception e) {
				throw new ClientProtocolException("Its a script");
			}
		}
		mongoClient.close();
		log.info("allResults==> " + allResults);
		String scriptResult = null;
		if (allResults.contains("FAIL")) {
			scriptResult = "FAIL";
		} else if (allResults.contains("PASS")) {
			scriptResult = "PASS";
		} else if (allResults.contains("WARNING")) {
			scriptResult = "WARNING";
		}
		String execID = null;
		if (execIDs.size() == 1) {
			execID = execIDs.toArray()[0].toString();
		} else {
			throw new Exception("Contains duplicate EXEC IDs==>" + execIDs);
		}
		String suiteID = null;
		if (suiteIDs.size() == 1) {
			suiteID = suiteIDs.toArray()[0].toString();
		} else {
			throw new Exception("Contains duplicate Suite IDs==>" + suiteIDs);
		}
		List<String> returnList = new ArrayList<String>();
		returnList.add(scriptResult);
		returnList.add(execID);
		returnList.add(suiteID);
		log.info("getScriptDetails==> " + returnList);
		return returnList;
	}

	public static String pushResults(String aIOBaseURL, String aIOAuth, String aIOProjectKeyId, String aIOCycleId,
			String aIOTCId, String fireflinkMailId, String fireflinkPassword, String projectNameGiven, String scriptId,
			String scriptStatus, String execID, String suiteID, String update) throws Exception {
		StringBuilder missingParams = new StringBuilder();

		if (aIOBaseURL == null || aIOBaseURL.isEmpty())
			missingParams.append("aIOBaseURL, ");
		if (aIOAuth == null || aIOAuth.isEmpty())
			missingParams.append("aIOAuth, ");
		if (aIOProjectKeyId == null || aIOProjectKeyId.isEmpty())
			missingParams.append("aIOProjectKeyId, ");
		if (aIOTCId == null || aIOTCId.isEmpty())
			missingParams.append("aIOTCId, ");
		if (fireflinkMailId == null || fireflinkMailId.isEmpty())
			missingParams.append("fireflinkMailId, ");
		if (fireflinkPassword == null || fireflinkPassword.isEmpty())
			missingParams.append("fireflinkPassword, ");
		if (projectNameGiven == null || projectNameGiven.isEmpty())
			missingParams.append("projectNameGiven, ");
		if (scriptId == null || scriptId.isEmpty())
			missingParams.append("scriptId, ");
		if (scriptStatus == null || scriptStatus.isEmpty())
			missingParams.append("scriptStatus, ");

		if (missingParams.length() > 0) {
			missingParams.setLength(missingParams.length() - 2);
			throw new InvalidParameterException("Missing or empty parameters: " + missingParams.toString());
		}
		aIOAuth = "AioAuth " + aIOAuth;
		baseURI = "https://app.fireflink.com/";
		String token = "";
		String projectId = "";
		String projectName = "";
		String suiteName = "";
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

		// Map results
		Map<String, String> getAioStatusMap = getAioStatusMap();

		// Get All AIO TestCases
		String aIOProjects = getRequest(aIOBaseURL + "/project/" + aIOProjectKeyId + "/testcase", "Authorization",
				aIOAuth);
		log.info(aIOProjects);
		List<Object> listOfTCIds = getJsonListValue(aIOProjects, "$.items[*].key");
		log.info(listOfTCIds + "");

		// GetScriptDetails
		String allScriptDetails = getRequest("/alltrees/optimize/v1/trees/modules/?sort=true", "Authorization",
				"Bearer " + token, "projectId", projectId);
		List<Object> scriptTitles = getJsonListValue(allScriptDetails,
				"responseObject.moduleTree[*]..children[?(@.type=='Script')].title");
		log.info(scriptTitles + "");

		// GetSuiteDetails
		String allSuiteDetails = getRequest("/project/optimize/v1/suite/getAll/all", "Authorization", "Bearer " + token,
				"projectId", projectId);
		suiteName = getJsonListStringValue(allSuiteDetails, "responseObject[?(@.id=='" + suiteID + "')].name");
		log.info(suiteName + "");

		try {
			Response pushedMapResult = null;
			if (update.equalsIgnoreCase(scriptStatus)) {
				// Check if cycle ID is empty
				if (aIOCycleId == null || aIOCycleId.isEmpty()) {
					log.info(".....Checking if cycle exists.....");
					aIOCycleId = checkCycle(aIOBaseURL, aIOAuth, suiteName, execID, aIOProjectKeyId);
					if (aIOCycleId == null || aIOCycleId.isEmpty()) {
						log.info(".....Creating new cycle now......");
						Response createdCycle = postRequestWithHeader(aIOBaseURL, createCycleBody(suiteName, execID),
								"/project/" + aIOProjectKeyId + "/testcycle/detail", "Authorization###" + aIOAuth);
						aIOCycleId = getJsonStringValue(createdCycle.body().asString(), "key");
					}
				}
				log.info("AIOCycleId ===> " + aIOCycleId);

				LinkedHashMap<String, String> allResult = new LinkedHashMap<>();
				log.info(scriptTitles.stream().filter(scriptTitle -> scriptTitle.toString().contains(scriptId))
						.collect(Collectors.toList()) + "");
				String aioID = getAioID(
						scriptTitles.stream().filter(scriptTitle -> scriptTitle.toString().contains(scriptId))
								.collect(Collectors.toList()).get(0).toString(),
						aIOTCId);
				if (aioID == null || aioID.isEmpty()) {
					throw new NullPointerException("Error script name format error AIO ID not found");
				}
				allResult.put(aioID, getAioStatusMap.get(scriptStatus));
				log.info(allResult + "");
				String aIOMapPostBody = createJsonFromMap(allResult);
				validateKeysInList(listOfTCIds, allResult);
				pushedMapResult = postRequestWithHeader(aIOBaseURL, aIOMapPostBody,
						"/project/" + aIOProjectKeyId + "/testcycle/" + aIOCycleId + "/bulk/testrun/update",
						"Authorization###" + aIOAuth);
			} else if (update.equalsIgnoreCase("All")) {
				// Check if cycle ID is empty
				if (aIOCycleId == null || aIOCycleId.isEmpty()) {
					log.info(".....Checking if cycle exists.....");
					aIOCycleId = checkCycle(aIOBaseURL, aIOAuth, suiteName, execID, aIOProjectKeyId);
					if (aIOCycleId == null || aIOCycleId.isEmpty()) {
						log.info(".....Creating new cycle now......");
						Response createdCycle = postRequestWithHeader(aIOBaseURL, createCycleBody(suiteName, execID),
								"/project/" + aIOProjectKeyId + "/testcycle/detail", "Authorization###" + aIOAuth);
						aIOCycleId = getJsonStringValue(createdCycle.body().asString(), "key");
					}
				}
				log.info("AIOCycleId ===> " + aIOCycleId);

				LinkedHashMap<String, String> allResult = new LinkedHashMap<>();
				log.info(scriptTitles.stream().filter(scriptTitle -> scriptTitle.toString().contains(scriptId))
						.collect(Collectors.toList()) + "");
				String aioID = getAioID(
						scriptTitles.stream().filter(scriptTitle -> scriptTitle.toString().contains(scriptId))
								.collect(Collectors.toList()).get(0).toString(),
						aIOTCId);
				if (aioID == null || aioID.isEmpty()) {
					throw new NullPointerException("Error script name format error AIO ID not found");
				}
				allResult.put(aioID, getAioStatusMap.get(scriptStatus));
				log.info(allResult + "");
				String aIOMapPostBody = createJsonFromMap(allResult);
				validateKeysInList(listOfTCIds, allResult);
				pushedMapResult = postRequestWithHeader(aIOBaseURL, aIOMapPostBody,
						"/project/" + aIOProjectKeyId + "/testcycle/" + aIOCycleId + "/bulk/testrun/update",
						"Authorization###" + aIOAuth);
			} else {
				return aIOCycleId + "::";
			}
			if (pushedMapResult.getStatusCode() > 205
					|| getJsonStringValue(pushedMapResult.getBody().asString(), "status").equals("SUCCESS_WITH_ERROR"))
				throw new Exception("Failed=>> " + pushedMapResult.getBody().asString());
			else
				log.info("Created and pass=>> " + pushedMapResult.getBody().asString());
		} catch (IllegalArgumentException e) {
			throw new Exception("Failed=>> " + e);
		}
		return aIOCycleId;
	}

	private static String createCycleBody(String suiteName, String execID) throws JSONException {
		JSONObject finalJson = new JSONObject();
		finalJson.put("title", "FireFlink_" + suiteName + "_" + execID);
		return finalJson.toString(2);
	}

	private static String checkCycle(String aIOBaseURL, String aIOAuth, String suiteName, String execID,
			String aIOProjectKeyId) {
		String cycleName = "FireFlink_" + suiteName + "_" + execID;
		String cycleId = "";
		try {
			cycleId = getJsonListStringValue(
					getRequest(aIOBaseURL + "/project/" + aIOProjectKeyId + "/testcycle", "Authorization", aIOAuth),
					"$.items[?(@.title=='" + cycleName + "')].key");
		} catch (Exception e) {
//			log.info("checkCycle" + e.getMessage());
		}
		log.info("checkCycle" + cycleId);
		return cycleId;
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
				"OGJlM2QwZjgtYmI5OS0zZjJjLWFiMjEtNzljNzQ0ZTdlN2E3LmJmMmFhMjMxLWJmY2ItNGQ3Ny04ZDk0LTM1ODFmZmY5Njg2OQ==");
		model.getAttributes().put("AioProjectKeyId", "SCRUM");
		model.getAttributes().put("AioCycleId (Empty to create cycle)", "");
		model.getAttributes().put("AioTestCasePrefix", "SCRUM-TC");
		model.getAttributes().put("FireflinkMailId", "srinivas.m@fireflink.com");
		model.getAttributes().put("FireflinkPassword", "Qwerty@123");
		model.getAttributes().put("ProjectNameGiven", "SonicWallPEs");
		model.getAttributes().put("Script Id", "SCR5652");
		model.getAttributes().put("Update for (All/Pass/Fail)", "Fail");
		log.info(new UpdateScriptAIO().execute(model) + "");
	}
}