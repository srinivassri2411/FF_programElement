package ado;

import static io.restassured.RestAssured.given;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class UpdateResultInAzure implements Nlp {
	@InputParams({ @InputParam(name = "Fireflink User ID", type = "java.lang.String"),
			@InputParam(name = "Fireflink Password", type = "java.lang.String"),
			@InputParam(name = "Test Case Mapping", type = "java.lang.Map"),
			@InputParam(name = "Fireflink Suite ID", type = "java.lang.String"),
			@InputParam(name = "Fireflink Project ID", type = "java.lang.String"),
			@InputParam(name = "Fireflink Host", type = "java.lang.String"),
			@InputParam(name = "Azure Organization Name", type = "java.lang.String"),
			@InputParam(name = "Azure Project Name", type = "java.lang.String"),
			@InputParam(name = "Azure PAT", type = "java.lang.String"),
			@InputParam(name = "Azure Test Plan ID", type = "java.lang.String"),
			@InputParam(name = "Azure Test Plan Name", type = "java.lang.String"),
			@InputParam(name = "Fireflink LIC ID", type = "java.lang.String") })

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

	static UpdateResultInAzure urae = new UpdateResultInAzure();
	static String pat = "";
	static String suiteId = "";
	static String azureOrg = "";
	static String azureProj = "";
	static String azurePlanId = "";
	static String azurePlanName = "";
	static String fireflinkHost = "";
	static String licenseId = "";
	static int plansID;
	static int azuresuiteId = 0;

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String fireflinkUserName = (String) attributes.get("Fireflink User ID");
		String fireflinkPassword = (String) attributes.get("Fireflink Password");
		Map testCaseMapping = (Map) attributes.get("Test Case Mapping");
		String fireflinkSuiteID = (String) attributes.get("Fireflink Suite ID");
		String fireflinkProjectID = (String) attributes.get("Fireflink Project ID");
		fireflinkHost = (String) attributes.get("Fireflink Host");
		pat = (String) attributes.get("Azure PAT");
		azureOrg = (String) attributes.get("Azure Organization Name");
		azureProj = (String) attributes.get("Azure Project Name");
		azurePlanId = (String) attributes.get("Azure Test Plan ID");
		plansID = Integer.parseInt(azurePlanId) - 1;

		azurePlanName = (String) attributes.get("Azure Test Plan Name");
		licenseId = (String) attributes.get("Fireflink LIC ID");

		try {
			String token = urae.getAccessToken(fireflinkUserName, fireflinkPassword, licenseId);
			log.info("Username is " + fireflinkUserName);
			log.info("Password is is " + fireflinkPassword);
			log.info("Lic Id is " + licenseId);
			log.info("Access Token" + token);
			String execID = urae.getRecentExecutionId(token, fireflinkSuiteID, fireflinkProjectID);
			log.info("Execution Id is " + execID);
			int totalCount = urae.getNumberOfScriptsInSuit(token, fireflinkProjectID, fireflinkSuiteID);
			log.info(totalCount + "Total count at start");

			Map<String, String> result = urae.getCurrentResult(token, fireflinkProjectID, execID);
			log.info("The Result is " + result + "");
			int resultSize = result.size();
			log.info(resultSize + " at Start");
			while (resultSize < totalCount - 1) {
				log.info(resultSize + " in Loop");
				log.info(totalCount + "in Loop");
				log.info("Running");
				urae.syncStatus(token, fireflinkProjectID, execID);
				// Call getCurrentResult again to update the result
				result = urae.getCurrentResult(token, fireflinkProjectID, execID);
				resultSize = result.size();
				log.info("Looped Count " + resultSize);
				log.info("Looped Result " + result + "");
				try {
					Thread.sleep(3000); // Sleep for 3 seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Map scriptIdsFromSuite = urae.getCombinedMapToCreateSuite(result, testCaseMapping);
			log.info("Combined Map " + scriptIdsFromSuite);
			log.info("Combined Map " + scriptIdsFromSuite);
			List testCaseIds = urae.getTestCaseIdsToCreateSuite(result, testCaseMapping);
			log.info("Test case ids " + testCaseIds);
			Map<String, String> poinIdsMap = urae.createSuit(testCaseIds, execID);
			log.info("Fineshend Creating Suite " + poinIdsMap);
			String resultBody = urae.resultBodyGenerator(result, testCaseMapping, poinIdsMap);
			log.info(resultBody);
			urae.updateResult(azuresuiteId, resultBody);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Result got Updated");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Update Result because " + writer);
		}
		// Your program element business logic goes here ...

		return nlpResponseModel;
	}

	public Map getCurrentResult(String token, String projId, String executionId) {
		Map<String, String> res = null;

		// Continue executing until res is not null
		while (res == null) {
			RestAssured.baseURI = fireflinkHost;
			Map<String, Object> header = new HashMap<>();
			header.put("Authorization", token);
			header.put("projectId", projId);
			header.put("projectType", "Web & Mobile");
			given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/sync/" + executionId);
			Response response = given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId).then()
					.extract().response();

			String body = response.getBody().asString();

			JsonPath js = new JsonPath(body);
			res = js.get("responseObject.executionResponses[0].executionStatusObject");

			// Add some delay before making the next call (optional)
			try {
				Thread.sleep(1000); // Sleep for 1 second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Remove things other than Scripts
		Map<String, String> filteredRes = new HashMap<>();
		for (Map.Entry<String, String> entry : res.entrySet()) {
			if (entry.getKey().startsWith("SCR")) {
				filteredRes.put(entry.getKey(), entry.getValue());
			}
		}
		log.info("getCurrentResult " + filteredRes);

		return filteredRes;
	}

	public String getRecentExecutionId(String token, String suiteID, String projId) {
		log.info("Access Token " + token);
		log.info("Suite ID " + suiteID);
		log.info("projId " + projId);
		RestAssured.baseURI = fireflinkHost;
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", token);
		header.put("projectId", projId);
		header.put("projectType", "Web & Mobile");
		Response response = given().headers(header).when()
				.get("/dashboardexecution/optimize/v1/dashboard/projects/" + projId + "/" + suiteID
						+ "?executionType=SUITE&pageNo=0&pageSize=12&archiveFlag=false")
				.then().log().body().extract().response();
		String body = response.getBody().asString();
		log.info("Execution Body is " + body);
		JsonPath js = new JsonPath(body);
		String executionId = js.get("responseObject[0]._id");
		log.info("Execution ID is " + executionId);
		return executionId;
	}

	public String getAccessToken(String email, String password, String licenseId) throws Exception {
		password = encryptPassword(password);
		RestAssured.baseURI = fireflinkHost;

//	 RestAssured.baseURI="https://app.fireflink.com";
		String body = given().header("Content-Type", "application/json")
				.body("{\r\n" + "    \"currentLicenseId\": \"" + licenseId + "\",\r\n" + "    \"emailId\": \"" + email
						+ "\",\r\n" + "    \"password\": \"" + password + "\"\r\n" + "}")
				.when().post("/appmanagement/optimize/v1/public/user/signin").then().extract().response().asString();
		JsonPath js = new JsonPath(body);
		log.info("Login body is " + body);
		String token = js.get("responseObject.access_token");
		log.info(token);

		token = "Bearer " + token;
		return token;
	}

	public int getNumberOfScriptsInSuit(String token, String projId, String suitId) {
		RestAssured.baseURI = fireflinkHost;
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", token);
		header.put("projectId", projId);
		header.put("projectType", "Web & Mobile");
		Response response = given().headers(header).when().get("/project/optimize/v1/suite/" + suitId + "").then()
				.extract().response();
		String body = response.getBody().asString();
		log.info("Number of Scripts is " + body);
		JsonPath js = new JsonPath(body);
		int noOfScripts = js.getInt("responseObject.noOfScripts");
		return noOfScripts;
	}

	public Map<String, String> getCombinedMapToCreateSuite(Map<String, String> result, Map<String, String> ids) {
		log.info("getCombinedMapToCreateSuite");

		Map<String, String> combinedMap = new HashMap<>();
		log.info("IDs Map: " + ids);
		log.info("Result Map: " + result);

		for (Map.Entry<String, String> entry : result.entrySet()) {
			String key = entry.getKey();
			String value = ids.get(key);

			if (value != null) {
				combinedMap.put(key, value);
			}

			log.info("Entry: " + entry);
			log.info("Value from IDs: " + value);
		}

		log.info("Combined Map: " + combinedMap);

		List<String> testcaseIds = new ArrayList<>();
		for (String value : combinedMap.values()) {
			if (!"Azure Ids".equals(value)) {
				testcaseIds.add(value);
			}
		}

		log.info("Testcase IDs: " + testcaseIds);

		return combinedMap;
	}

	public List getTestCaseIdsToCreateSuite(Map<String, String> result, Map<String, String> ids) {
		log.info("getTestCaseIdsToCreateSuite");
		Map<String, String> combinedMap = new HashMap<>();
		Map<String, String> firstMapVariable = ids;

		List<String> testcaseIds = new ArrayList<>();
		// Copy keys from fireflinkResult map and values from testcaseMap list
		for (Map.Entry<String, String> entry : result.entrySet()) {
			if (firstMapVariable.get(entry.getKey()) == null) {
				continue;
			}
			combinedMap.put(entry.getKey(), firstMapVariable.get(entry.getKey()));

			log.info(firstMapVariable.get(entry.getKey()));
		}
		// Create List to Create Suite in Azure
		for (String value : combinedMap.values()) {
			if (!value.equals("Azure Ids")) { // Exclude the value "Azure Ids"
				testcaseIds.add(value);
			}
		}

		log.info("Test Cases to create Suite in Azure " + testcaseIds);
		return testcaseIds;
	}

	/***
	 * This will return Map to Test case Ids and there Respective Point Ids
	 * 
	 * @param ids
	 * @param execId
	 * @return
	 */
	public Map createSuit(List ids, String execId) {
		RestAssured.baseURI = "https://dev.azure.com";
		RequestSpecification request = RestAssured.given();
		String Credentials = ":" + pat + "";
		String encodedString = Base64.getEncoder().encodeToString(Credentials.getBytes());
		String Reqbody = "{" + "    \"suiteType\": 2," + "    \"name\": \" " + execId + "\"," + "    \"parentSuite\": {"
				+ "        \"id\": \"" + azurePlanId + "\"," + "        \"name\": \"" + azurePlanName + "\"" + "    }"
				+ "}";
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", "Basic " + encodedString);
		header.put("content-type", "application/json");
		header.put("accept",
				"application/json;api-version=7.1-preview.1;excludeUrls=true;enumsAsNumbers=true;msDateFormat=true;noArrayWrap=true");
		request.headers(header);
		Response response = request.body(Reqbody)
				.post("/" + azureOrg + "/" + azureProj + "/_apis/testplan/Plans/" + plansID + "/Suites");
		String body = response.getBody().asString();
		log.info("Create Suite Response" + body);
		JsonPath js = new JsonPath(body);
		azuresuiteId = js.get("id");
		log.info("Suite ID" + azuresuiteId);
		int statusCode = response.statusCode();
		log.info("createSuit " + statusCode);
		if (statusCode == 200) {
			String Body = urae.generateBody(ids);
			Map<String, String> pointIds = urae.addtestCasesToSuit(azuresuiteId, Body);
			return pointIds;
		}
		return null;
	}

	public Map addtestCasesToSuit(int suitId, String body) {
		log.info("Add Test Case Called");
		RestAssured.baseURI = "https://dev.azure.com";
		RequestSpecification request = RestAssured.given();
		String Credentials = ":" + pat;
		String encodedString = Base64.getEncoder().encodeToString(Credentials.getBytes());
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Authorization", "Basic " + encodedString);
		header.put("content-type", "application/json");
		header.put("accept",
				"application/json;api-version=7.1-preview.3;excludeUrls=true;enumsAsNumbers=true;msDateFormat=true;noArrayWrap=true");
		request.headers(header);
		log.info("Body to add Suite " + body);
		Response response = request.body(body).post("/" + azureOrg + "/" + azureProj + "/_apis/testplan/Plans/"
				+ plansID + "/Suites/" + suitId + "/TestCase");
		int statusCode = response.statusCode();
		log.info("addtestCasesToSuit " + statusCode);
		if (statusCode == 200) {
			log.info("Call Next Method to Get Points to the Suit");
			Map<String, String> pointIdMap = urae.getPoints(String.valueOf(suitId));
			log.info("idMap" + pointIdMap);
			return pointIdMap;
		}
		return null;
	}

	public String generateBody(List ids) {
		// Create a JSONArray to hold the JSON objects
		JSONArray jsonArray = new JSONArray();

		// Iterate over the list of IDs and create a JSON object for each ID
		for (Object id : ids) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("pointAssignments", new JSONArray());
			jsonObject.put("workItem", new JSONObject().put("id", id));
			jsonArray.put(jsonObject);
		}

		return jsonArray.toString();

	}

	public Map getPoints(String suiteId) {
		RestAssured.baseURI = "https://dev.azure.com";
		RequestSpecification request = RestAssured.given();
		String Credentials = ":" + pat;
		String encodedString = Base64.getEncoder().encodeToString(Credentials.getBytes());
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", "Basic " + encodedString);
		header.put("content-type", "application/json");
		header.put("accept",
				"application/json;api-version=7.1-preview.3;excludeUrls=true;enumsAsNumbers=true;msDateFormat=true;noArrayWrap=true");
		request.headers(header);
		log.info("Suite Id is " + suiteId);
		Response response = request.get("/" + azureOrg + "/" + azureProj + "/_apis/test/Plans/" + plansID + "/Suites/"
				+ suiteId + "/points?api-version=5.0");
		String resbody = response.getBody().asString();
//     response.prettyPrint();
		Map poinrIds = urae.pointIds(resbody);
		return poinrIds;
	}

	public Map pointIds(String response) {
		log.info("Entered pointIds");
		Map<String, Integer> idMapping = new HashMap<>();
		try {
			JSONArray jsonArray = new JSONArray(response);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				int testPointId = jsonObject.getInt("id");
				String testCaseId = jsonObject.getJSONObject("testCase").getString("id");
				idMapping.put(testCaseId, testPointId);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.info("Point Ids with Test case Ids" + idMapping);
		return idMapping;
	}

	public String resultBodyGenerator(Map<String, String> fireflinkResult, Map<String, String> testcaseMap,
			Map<String, String> myPointIdMap) {
		Map<String, String> testcase = testcaseMap;
		log.info(testcaseMap.toString());

		Map<String, String> combinedMap = new HashMap<>();

		// Copy keys from fireflinkResult map and values from testcaseMap list
		for (Map.Entry<String, String> entry : fireflinkResult.entrySet()) {
			combinedMap.put(entry.getKey(), testcase.get(entry.getKey()));
		}

		log.info("Combined Map: " + combinedMap);
		log.info("Point Id Map " + myPointIdMap);
		for (Map.Entry<String, String> entry : combinedMap.entrySet()) {
			log.info("Entry is " + entry);
			String key = entry.getKey();
			String value = entry.getValue();
			if (myPointIdMap.containsKey(value)) {
				combinedMap.put(key, String.valueOf(myPointIdMap.get(value)));
			}
		}

		log.info("Updated Map: " + combinedMap);

		JSONArray jsonArray = new JSONArray();
		for (Map.Entry<String, String> entry : combinedMap.entrySet()) {
			log.info("Body Entry " + entry);
			String value = entry.getValue();
			if (value == null) {
				continue;
			}
			log.info("Integer Value of " + value);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", value); // assuming id is an integer

			// Set outcome based on fireflinkResult
			String fireflinkValue = fireflinkResult.get(entry.getKey());
			int outcome = fireflinkValue.equals("FAIL") ? 3 : 2;
			JSONObject results = new JSONObject();
			results.put("outcome", outcome);
			jsonObject.put("results", results);

			jsonArray.put(jsonObject);
		}

		// Convert to JSON string
		String jsonString = jsonArray.toString();
//         log.info(jsonString);
		return jsonString;
	}

	public void updateResult(int suiteId, String reqBody) {
		RestAssured.baseURI = "https://dev.azure.com";
		RequestSpecification request = RestAssured.given();
		String Credentials = ":" + pat;
		String encodedString = Base64.getEncoder().encodeToString(Credentials.getBytes());
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", "Basic " + encodedString);
		header.put("content-type", "application/json");
		header.put("accept",
				"application/json;api-version=7.1-preview.2;excludeUrls=true;enumsAsNumbers=true;msDateFormat=true;noArrayWrap=true");
		request.headers(header);
		Response response = request.body(reqBody).patch("/" + azureOrg + "/" + azureProj + "/_apis/testplan/Plans/"
				+ plansID + "/Suites/" + suiteId + "/TestPoint?includePointDetails=true&returnIdentityRef=true");
		String body = response.getBody().asString();
		log.info(body);
		JsonPath js = new JsonPath(body);
		int statusCode = response.statusCode();
		log.info("Update Result " + statusCode);
		if (statusCode == 200) {
			log.info("Result Updated");
		}
	}

	public void syncStatus(String token, String projId, String execId) {
		RestAssured.baseURI = fireflinkHost;
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", token);
		header.put("projectId", projId);
		header.put("projectType", "Web & Mobile");
		Response response = given().headers(header).when()
				.get("/executionresult/optimize/v1/executionResponse/result/sync/" + execId).then().extract()
				.response();
		String body = response.getBody().asString();
		log.info("Sync " + body);
	}

	public static String encryptPassword(String plainText) throws Exception {
		byte[] publicKeyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public static void main(String[] args) throws NlpException {
//    	 Value of testCaseMap is {Fireflink Ids=Azure Ids, SCR2174=19, SCR2175=20, SCR2176=21, SCR2177=22, SCR2173=23}

		Map<String, String> testCase = new HashMap<>();
		testCase.put("SCR2176", "3");
		testCase.put("SCR2177", "4");
		testCase.put("SCR2179", "5");
		testCase.put("SCR2173", "6");
		testCase.put("SCR2174", "7");
		testCase.put("Fireflink Id", "Azure Ids");
		testCase.put("SCR2318", "5");
		testCase.put("SCR2319", "6");
		testCase.put("SCR2320", "7");
		testCase.put("SCR2321", "8");
		testCase.put("SCR2322", "9");
		UpdateResultInAzure ua = new UpdateResultInAzure();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("Fireflink User ID", "darshan.n@testyantra.com");
		attributes.put("Fireflink Password", "Password@1234");
		attributes.put("Test Case Mapping", testCase);
		attributes.put("Fireflink Suite ID", "SUITE1099");
		attributes.put("Fireflink Project ID", "PJT1007");
		attributes.put("Fireflink Host", "https://app.fireflink.com");
		attributes.put("Azure Organization Name", "adani-test");
		attributes.put("Azure Project Name", "adani");
		attributes.put("Azure Test Plan ID", "2");
		attributes.put("Azure Test Plan Name", "Demo");
		attributes.put("Fireflink LIC ID", "LIC16627");
		ua.execute(nl);
	}
}