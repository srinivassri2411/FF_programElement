package azure;

import static io.restassured.RestAssured.given;

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

public class UpdateTheResultInAzure implements Nlp {
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

	static UpdateTheResultInAzure urae = new UpdateTheResultInAzure();
	static String pat = "";
	static int suiteId = 0;
	static String azureOrg = "";
	static String azureProj = "";
	static String azurePlanId = "";
	static String azurePlanName = "";
	static String fireflinkHost = "";

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
		azurePlanName = (String) attributes.get("Azure Test Plan Name");

		try {
			String token = urae.getAccessToken(fireflinkUserName, fireflinkPassword);
			log.info(token);
			String execID = urae.getRecentExecutionId(token, fireflinkSuiteID, fireflinkProjectID);
			log.info(execID + "");
			int totalCount = urae.getNumberOfScriptsInSuit(token, fireflinkProjectID, fireflinkSuiteID);
			log.info(totalCount + " at start");

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
				log.info("Looped Result " + result + "");

				// Optionally add a delay between each iteration to prevent excessive resource
				// consumption
				try {
					Thread.sleep(3000); // Sleep for 3 seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Map scriptIdsFromSuite = urae.getCombinedMapToCreateSuite(result, testCaseMapping);
			System.out.println("Main Method " + scriptIdsFromSuite);
			List testCaseIds = urae.getTestCaseIdsToCreateSuite(result, testCaseMapping);
			System.out.println("Main Method " + testCaseIds);
			Map<String, String> poinIdsMap = urae.createSuit(testCaseIds, execID);
			String resultBody = urae.resultBodyGenerator(result, testCaseMapping, poinIdsMap);
			System.out.println(resultBody);
			urae.updateResult(suiteId, resultBody);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Result got Updated");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Update Result because " + e);
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

			Response response = given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId).then()
					.assertThat().statusCode(200).extract().response();

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
		System.out.println("getCurrentResult " + filteredRes);

		return filteredRes;
	}

	public String getRecentExecutionId(String token, String suiteID, String projId) {
		RestAssured.baseURI = fireflinkHost;
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", token);
		header.put("projectId", projId);
		header.put("projectType", "Web & Mobile");
		Response response = given().param("executionType", "SUITE").headers(header).when().get(
				"/dashboardexecution/optimize/v1/dashboard/projects/" + projId + "/" + suiteID + "?executionType=SUITE")
				.then().assertThat().statusCode(200).extract().response();
		String body = response.getBody().asString();
		JsonPath js = new JsonPath(body);
		String executionId = js.get("responseObject[0]._id");
		System.out.println(executionId);
		return executionId;
	}

	public String getAccessToken(String email, String password) throws Exception {
		password = encryptPassword(password);
		RestAssured.baseURI = fireflinkHost;
		String body = given().header("Content-Type", "text/plain").header("Accept", "*/*")
				.header("Content-Type", "application/json")
				.body("" + "{\n" + "    \"emailId\": \"" + email + "\",\n" + "    \"password\": \"" + password + "\",\n"
						+ "    \"lastSessionData\": \"/signin\"\n" + "}")
				.when().post("appmanagement/optimize/v1/public/user/signin").then().extract().response().asString();
		JsonPath js = new JsonPath(body);
		String token = js.get("responseObject.access_token");
		System.out.println(token);

		token = "Bearer " + token;
		return token;
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

	public int getNumberOfScriptsInSuit(String token, String projId, String suitId) {
		RestAssured.baseURI = fireflinkHost;
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Authorization", token);
		header.put("projectId", projId);
		header.put("projectType", "Web & Mobile");
		Response response = given().headers(header).when().get("/project/optimize/v1/suite/" + suitId + "").then()
				.assertThat().statusCode(200).extract().response();
		String body = response.getBody().asString();
		JsonPath js = new JsonPath(body);
		int noOfScripts = js.getInt("responseObject.noOfScripts");
		return noOfScripts;
	}

//     private List<Map<String, String>> getdataFromExcel(String excelPath, String sheetName)
//             throws IOException {
//
//         List<Map<String, String>> entireData = new LinkedList<Map<String, String>>();
//         InputStream inputStream = new FileInputStream(excelPath);
//         Workbook workbook = WorkbookFactory.create(inputStream);
//         Sheet sheet = workbook.getSheet(sheetName);
//         DataFormatter df = new DataFormatter();
//         FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
//
//         int rowCount = sheet.getLastRowNum();
//         for (int i = 0; i < rowCount; i++) {
//             Map<String, String> singleRowData = new LinkedHashMap<String, String>();
//             int columnCount = sheet.getRow(i).getLastCellNum();
//             for (int j = 0; j < columnCount; j++) {
//                 singleRowData.put(df.formatCellValue(sheet.getRow(0).getCell(j), evaluator),
//                         df.formatCellValue(sheet.getRow(i + 1).getCell(j), evaluator));
//             }
//             entireData.add(singleRowData);
//         }
//         workbook.close();
//         inputStream.close();
//         return entireData;
//     }

	/**
	 * This returns the Map which contains Script id from Fireflink and Script id
	 * From Azure
	 * 
	 * @param result
	 * @param ids
	 * @return
	 */
	public Map getCombinedMapToCreateSuite(Map<String, String> result, Map<String, String> ids) {
		Map<String, String> combinedMap = new HashMap<>();
		Map<String, String> firstMapVariable = ids;

		List<String> testcaseIds = new ArrayList<>();
		// Copy keys from fireflinkResult map and values from testcaseMap list
		for (Map.Entry<String, String> entry : result.entrySet()) {
			combinedMap.put(entry.getKey(), firstMapVariable.get(entry.getKey()));
		}

		// Create List to Create Suite in Azure
		for (String value : combinedMap.values()) {
			if (!value.equals("Azure Ids")) { // Exclude the value "Azure Ids"
				testcaseIds.add(value);
			}
		}

		System.out.println("Combined Map:" + combinedMap);
		return combinedMap;
	}

	public List getTestCaseIdsToCreateSuite(Map<String, String> result, Map<String, String> ids) {
		Map<String, String> combinedMap = new HashMap<>();
		Map<String, String> firstMapVariable = ids;

		List<String> testcaseIds = new ArrayList<>();
		// Copy keys from fireflinkResult map and values from testcaseMap list
		for (Map.Entry<String, String> entry : result.entrySet()) {
			combinedMap.put(entry.getKey(), firstMapVariable.get(entry.getKey()));
		}
		// Create List to Create Suite in Azure
		for (String value : combinedMap.values()) {
			if (!value.equals("Azure Ids")) { // Exclude the value "Azure Ids"
				testcaseIds.add(value);
			}
		}

		System.out.println("Test Cases to create Suite in Azure " + testcaseIds);
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
				.post("/" + azureOrg + "/" + azureProj + "/_apis/testplan/Plans/4/Suites");
		String body = response.getBody().asString();
		JsonPath js = new JsonPath(body);
		suiteId = js.get("id");
		int statusCode = response.statusCode();
		System.out.println("createSuit " + statusCode);
		if (statusCode == 200) {
			String Body = urae.generateBody(ids);
			Map<String, String> pointIds = urae.addtestCasesToSuit(suiteId, Body);
			return pointIds;
		}
		return null;
	}

	public Map addtestCasesToSuit(int suitId, String body) {
		System.out.println("Add Test Case Called");
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
		Response response = request.body(body)
				.post("/" + azureOrg + "/" + azureProj + "/_apis/testplan/Plans/4/Suites/" + suitId + "/TestCase");
		int statusCode = response.statusCode();
		System.out.println("addtestCasesToSuit " + statusCode);
		if (statusCode == 200) {
			System.out.println("Call Next Method to Get Points to the Suit");
			Map<String, String> pointIdMap = urae.getPoints(String.valueOf(suitId));
			System.out.println("idMap" + pointIdMap);
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
		System.out.println("Suite Id is " + suiteId);
		Response response = request.get(
				"/" + azureOrg + "/" + azureProj + "/_apis/test/Plans/4/Suites/" + suiteId + "/points?api-version=5.0");
		String resbody = response.getBody().asString();
//     response.prettyPrint();
		Map poinrIds = urae.pointIds(resbody);
		return poinrIds;
	}

	/***
	 * This converts Response body Point Ids to Map
	 * 
	 * @param response
	 * @return
	 */
	public Map pointIds(String response) {
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
		System.out.println("Point Ids with Test case Ids" + idMapping);
		return idMapping;
	}

	public String resultBodyGenerator(Map<String, String> fireflinkResult, Map<String, String> testcaseMap,
			Map<String, String> myPointIdMap) {
		Map<String, String> testcase = testcaseMap;
		System.out.println(testcaseMap);

		Map<String, String> combinedMap = new HashMap<>();

		// Copy keys from fireflinkResult map and values from testcaseMap list
		for (Map.Entry<String, String> entry : fireflinkResult.entrySet()) {
			combinedMap.put(entry.getKey(), testcase.get(entry.getKey()));
		}

		System.out.println("Combined Map: " + combinedMap);
		System.out.println("Point Id Map " + myPointIdMap);
		for (Map.Entry<String, String> entry : combinedMap.entrySet()) {
			System.out.println("Entry is " + entry);
			String key = entry.getKey();
			String value = entry.getValue();
			if (myPointIdMap.containsKey(value)) {
				combinedMap.put(key, String.valueOf(myPointIdMap.get(value)));
			}
		}

		System.out.println("Updated Map: " + combinedMap);

		JSONArray jsonArray = new JSONArray();
		for (Map.Entry<String, String> entry : combinedMap.entrySet()) {
			System.out.println("Body Entry " + entry);
			String value = entry.getValue();
			System.out.println("Integer Value of " + value);
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
//         System.out.println(jsonString);
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
		Response response = request.body(reqBody)
				.patch("/" + azureOrg + "/" + azureProj + "/_apis/testplan/Plans/4/Suites/" + suiteId
						+ "/TestPoint?includePointDetails=true&returnIdentityRef=true");
		String body = response.getBody().asString();
		System.out.println(body);
		JsonPath js = new JsonPath(body);
		int statusCode = response.statusCode();
		System.out.println("Update Result " + statusCode);
		if (statusCode == 200) {
			System.out.println("Result Updated");
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
}