
package clickUp;

import static io.restassured.RestAssured.baseURI;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.bson.Document;
import org.openqa.selenium.NotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PushResultToClickUP implements Nlp {
	public static String scriptName = "";
	public static String module = "";
	public static String token = "";
	public static String projectId = "";
	public static String scriptId = "";
	public static String status = "";
	public static String runId = "";
	public static Map<String, String> notUpdated = null;

	private static final String BASE_URL = "https://api.clickup.com/api/v2";

	private HttpClient httpClient;
	private ObjectMapper mapper;
	public static String executionId = "";
	public static String baseUri = "";
	private String licenseId;
	private String suiteId;

	@SuppressWarnings("unchecked")
	@InputParams({ @InputParam(name = "ClickUp TeamName", type = "java.lang.String"),
			@InputParam(name = "ClickUp SpaceName", type = "java.lang.String"),
			@InputParam(name = "ClickUp FolderName", type = "java.lang.String"),
			@InputParam(name = "ClickUp Auth", type = "java.lang.String"),
			@InputParam(name = "FireFlink Mail", type = "java.lang.String"),
			@InputParam(name = "FireFlink Password", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String clickUpTeamName = (String) programElementsInput.get("ClickUp TeamName");
		String clickUpSpaceName = (String) programElementsInput.get("ClickUp SpaceName");
		String clickUpFolderName = (String) programElementsInput.get("ClickUp FolderName");
		String clickUpAuth = (String) programElementsInput.get("ClickUp Auth");
		String fireFlinkMail = (String) programElementsInput.get("FireFlink Mail");
		String fireflinkPassword = (String) programElementsInput.get("FireFlink Password");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			httpClient = HttpClient.newHttpClient();
			mapper = new ObjectMapper();
			notUpdated = new LinkedHashMap<>();
			// Get Execution Identifiers
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
			executionId = exeIdentifiers.get("executionId");
			runId = exeIdentifiers.get("runId");
			licenseId = exeIdentifiers.get("licenseId");
			projectId = exeIdentifiers.get("projectId");
			suiteId = exeIdentifiers.get("suiteId");

//			executionId = "EXEC31809";
//			runId = "Runc826b9d2-9184-4adc-9802-9735b5bcd7fd";
//			licenseId = "LIC16627";
//			projectId = "PJT1218";
//			suiteId = "SUITE_0d83378c-0805-4bcc-afd0-17e70e723a5f";

			baseURI = "https://us-app.fireflink.com/";
			// Sign in API Request
			fireflinkPassword = encryptPassword(fireflinkPassword);
			String signInRequestPayload = "{\"emailId\": \"" + fireFlinkMail + "\", \"password\": \""
					+ fireflinkPassword + "\", \"currentLicenseId\": \"" + licenseId + "\"}";
			String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
					signInRequestPayload);

			String message = convertToString(getJsonValue(signInResponsePayload, "status"));
			if (message != null && !message.contains("SUCCESS")) {
				throw new java.security.InvalidParameterException(
						convertToString(getJsonValue(signInResponsePayload, "message")));
			}

			// Fetch Access Token
			token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

			// Fetch suite details from Local Mongo DB
			Map<String, String> queryMapSuiteResults = Map.of("executionId", executionId, "runId", runId);
			String suiteLevelMongoData = connectAndFetchMongoDocumentsAsJsonString(queryMapSuiteResults, null, null,
					licenseId + "_" + executionId + "_entity_results");
			System.out.println("suiteLevelMongoData" + suiteLevelMongoData);

			// Get Script ids as List
			List<String> scriptIdList = (List<String>) getJsonValue(suiteLevelMongoData,
					"[?(@.entityType=='Script')].originalId");
			System.out.println("Script Id List" + scriptIdList);

			// Get Manual Testcase Ids for Execution details
			String executionDetailsResponsePayload = getRequest(
					"dashboardexecution/optimize/v1/dashboard/project/licenseId/" + licenseId + "/projectId/"
							+ projectId + "/executionId/" + executionId + "/manual-test-mapping",
					"Authorization", "Bearer " + token);
			System.out.println("executionDetailsResponsePayload" + executionDetailsResponsePayload);
			Map<String, String> autoToManualMap = (Map<String, String>) getJsonValue(executionDetailsResponsePayload,
					"responseObject.autoToManualMap");

			for (int i = 0; i < scriptIdList.size() - 1; i++) { // Ignore last script
				scriptId = scriptIdList.get(i);

				// Get Script details
				String scriptStatus = ((List<String>) (getJsonValue(suiteLevelMongoData,
						"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].status"))).get(0);
				String moduleId = ((List<String>) (getJsonValue(suiteLevelMongoData,
						"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].parentId"))).get(0);
				scriptName = ((List<String>) (getJsonValue(suiteLevelMongoData,
						"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].name"))).get(0);
				module = ((List<String>) (getJsonValue(suiteLevelMongoData,
						"[?(@.entityType=='Module' && @.originalId=='" + moduleId + "')].name"))).get(0);
				String manualTcId = autoToManualMap.get(scriptId);
				String taskDescription = null;

				// Fetch manual Testcase details based on ID
				if (manualTcId != null) {
					System.out.println(
							"ScriptID==> " + scriptId + " moduleId==> " + moduleId + " manualTCID==> " + manualTcId);
					String suiteresultsResponsePayload = getRequest(
							"alltrees/optimize/v3/tree/execution/suite?sourceId=" + suiteId + "&action=start",
							"Authorization", "Bearer " + token, "projectId", projectId);
					System.out.println("suiteresultsResponsePayload" + suiteresultsResponsePayload);
					String suiteName = ((List<String>) (getJsonValue(suiteresultsResponsePayload,
							"responseObject.treeDataList[?(@.key=='" + suiteId + "')].name"))).get(0);

					String manualTestCaseLevelResponsePayload = getRequest(
							"project/optimize/v1/scripts/modules/" + moduleId + "/script/" + manualTcId,
							"Authorization", "Bearer " + token, "projectId", projectId);
					System.out.println("manualTestCaseLevelResponsePayload" + manualTestCaseLevelResponsePayload);
					JsonNode responseObject = mapper.readTree(manualTestCaseLevelResponsePayload);

					// Fetch description for clickup
					taskDescription = buildClickUpTaskDescription(responseObject);

//					System.out.println(taskDescription);
					if (scriptStatus.equals("FAIL") || scriptStatus.equals("PASS")) {
						publishExecutionResults(clickUpTeamName, clickUpSpaceName, clickUpFolderName, clickUpAuth,
								suiteName, executionId, scriptName, scriptStatus, taskDescription);
					} else {
						notUpdated.put(scriptName, scriptStatus);
					}
				} else {
					throw new NotFoundException(scriptName
							+ " doesnt have manual test case mapped please check manual testcase id is: " + manualTcId);
				}
			}
			if (notUpdated.isEmpty()) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Updated results into Click Up successfully");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to Update results into Click Up cause of results " + notUpdated);
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to update the issue =>> " + writer);
		}
		return nlpResponseModel;
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]);
		return ((Response) request.when().get(endpoint, new Object[0])).asString();
	}

	private static String postRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]);
		return ((Response) request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint, new Object[0]))
				.asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath, new com.jayway.jsonpath.Predicate[0]);
	}

	public static boolean convertToBoolean(Object obj) {
		return ((Boolean) obj).booleanValue();
	}

	public static String convertToString(Object obj) {
		return (String) obj;
	}

	public static double convertToDouble(Object obj) {
		return ((Double) obj).doubleValue();
	}

	public static String encryptPassword(String password) throws Exception {
		byte[] publicKeyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
				PSource.PSpecified.DEFAULT);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);

		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public static int convertToInteger(Object value) {
		if (value instanceof Double)
			return ((Double) value).intValue();
		if (value instanceof Integer)
			return ((Integer) value).intValue();
		if (value instanceof String)
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				try {
					return (int) Double.parseDouble((String) value);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("Cannot convert to int: " + String.valueOf(value));
				}
			}
		throw new IllegalArgumentException("Unsupported type: " + String.valueOf(value));
	}

//	public static void clearGlobals() {
//		failedStepId = "";
//		slNoAndRowNumber = 1;
//		slNoAndRowNumberForDefect = 1;
//		scriptName = "";
//		module = "";
//		createdDate = "";
//		reportSheet = null;
//		defectsSheet = null;
//		token = "";
//		projectId = "";
//		projectType = "";
//		executionFolderPath = "";
//		scriptFolderPath = "";
//		scriptId = "";
//		crNumber = "";
//		crName = "";
//		executedDate = "";
//		preConditionSteps = "";
//		status = "";
//		executedBy = "";
//		clientId = "";
//		runId = "";
//		failedIteration = null;
//		executionId = "";
//		baseUri = "";
//	}

	public static String connectAndFetchMongoDocumentsAsJsonString(Map<String, String> queryMap,
			Map<String, String> excludeMap, Map<String, List<Object>> notInMap, String collectionName) {

		MongoClient mongoClient = null;
		try {
			mongoClient = MongoClients.create("mongodb://localhost:27017");
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "Optimize_Dev_client");
			Query query = new Query();

			// Normal equality conditions
			if (queryMap != null) {
				for (Map.Entry<String, String> entry : queryMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if ("NOT_NULL".equals(value)) {
						query.addCriteria(Criteria.where(key).exists(true).ne(null));
					} else {
						query.addCriteria(Criteria.where(key).is(value));
					}
				}
			}

			// Exclude conditions
			if (excludeMap != null) {
				for (Map.Entry<String, String> entry : excludeMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (value == null) {
						query.addCriteria(Criteria.where(key).exists(true).ne(null));
					} else {
						query.addCriteria(Criteria.where(key).ne(value));
					}
				}
			}

			// NOT IN conditions
			if (notInMap != null) {
				for (Map.Entry<String, List<Object>> entry : notInMap.entrySet()) {
					String key = entry.getKey();
					List<Object> values = entry.getValue();

					if (values != null && !values.isEmpty()) {
						query.addCriteria(Criteria.where(key).nin(values));
					}
				}
			}

			// System.out.println("Generated Query : " + query);
			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);
			return documents.stream().map(Document::toJson).collect(java.util.stream.Collectors.joining(",", "[", "]"));
		} finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

	public String convertMillisToTimeFormat(int input) {
		long hours = input / (1000 * 60 * 60);
		long minutes = (input / (1000 * 60)) % 60;
		long seconds = (input / 1000) % 60;
		long milliseconds = input % 1000;

		String formatted = String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);

		return formatted;
	}

	public static String buildClickUpTaskDescription(JsonNode response) {

		JsonNode responseObject = response.has("responseObject") ? response.path("responseObject") : response;

		StringBuilder result = new StringBuilder();

		String[] fields = { "Requirement Id", "Status" };

		JsonNode details = responseObject.path("testCaseDetails");

		for (String field : fields) {
			String value = "";

			for (JsonNode detail : details) {
				if (field.equalsIgnoreCase(detail.path("label").asText())) {
					JsonNode values = detail.path("value");

					if (values.isArray()) {
						List<String> items = new ArrayList<>();
						for (JsonNode item : values) {
							items.add(item.asText());
						}
						value = String.join(", ", items);
					} else if (!values.isMissingNode() && !values.isNull()) {
						value = values.asText();
					}
					break;
				}
			}

			result.append("**").append(field).append(":** ").append(value).append("\n\n");
		}

		JsonNode rows = responseObject.path("testSteps").path("data");

		result.append("| TestSteps | Input | ExpectedResult |\n");
		result.append("|---|---|---|\n");

		if (rows.isArray() && !rows.isEmpty()) {
			for (int i = 1; i < rows.size(); i++) {
				JsonNode row = rows.get(i);

				result.append("| ").append(cell(row, 2)).append(" | ").append(cell(row, 3)).append(" | ")
						.append(cell(row, 4)).append(" |\n");
			}
		}

		return result.toString().trim();
	}

	private static String cell(JsonNode row, int index) {
		if (!row.isArray() || row.size() <= index) {
			return "";
		}

		return row.get(index).path("value").asText("").replace("|", "\\|").replace("\n", "<br>").replace("\r", "");
	}

	public JsonNode publishExecutionResults(String teamName, String spaceName, String folderName, String clickUpAuth,
			String suiteName, String executionId, String scriptName, String status, String description)
			throws Exception {

		requireText(teamName, "ClickUp Team Name");
		requireText(spaceName, "ClickUp Space Name");
		requireText(clickUpAuth, "ClickUp Auth");

		// 1. Find Workspace
		JsonNode teamsResponse = get("/team", clickUpAuth);
		String teamId = findIdByName(teamsResponse.path("teams"), teamName, "Workspace");

		// 2. Find Space
		JsonNode spacesResponse = get("/team/" + teamId + "/space", clickUpAuth);

		String spaceId = findIdByName(spacesResponse.path("spaces"), spaceName, "Space");

		// 3. Find the supplied Folder, or create a new one.
		String folderId;

		if (folderName != null && !folderName.isBlank() && !folderName.equalsIgnoreCase("null")) {

			JsonNode foldersResponse = get("/space/" + spaceId + "/folder", clickUpAuth);
			folderId = findIdByName(foldersResponse.path("folders"), folderName, "Folder");

		} else {

			requireText(suiteName, "Suite Name");

			JsonNode foldersResponse = get("/space/" + spaceId + "/folder", clickUpAuth);

			String existingFolderId = findOptionalIdByName(foldersResponse.path("folders"), suiteName);

			if (existingFolderId != null) {
				folderId = existingFolderId;
			} else {
				ObjectNode folderBody = mapper.createObjectNode();
				folderBody.put("name", suiteName);

				JsonNode folderResponse = post("/space/" + spaceId + "/folder", clickUpAuth, folderBody);

				folderId = requiredId(folderResponse, "Folder");
			}
		}

		// 4. Execution ID becomes the ClickUp List name.
		requireText(executionId, "FireFlink Execution ID");

		JsonNode listsResponse = get("/folder/" + folderId + "/list", clickUpAuth);

		String listId = findOptionalIdByName(listsResponse.path("lists"), executionId);

		if (listId == null) {
			ObjectNode listBody = mapper.createObjectNode();
			listBody.put("name", executionId);

			JsonNode listResponse = post("/folder/" + folderId + "/list", clickUpAuth, listBody);

			listId = requiredId(listResponse, "List");
		}

		// Read actual statuses configured for this List.
		JsonNode listDetails = get("/list/" + listId, clickUpAuth);

		JsonNode availableStatuses = listDetails.path("statuses");

		// 5. Find the FireFlink script results.
		requireText(scriptName, "ScriptName");
		requireText(status, "Status");
		// Resolve the exact status configured in ClickUp.
		String clickUpStatus = resolveStatus(status, availableStatuses);

		// 6. Create a task for this ScriptName.
		ObjectNode taskBody = mapper.createObjectNode();
		taskBody.put("name", scriptName);
		taskBody.put("status", clickUpStatus);

		taskBody.put("markdown_description", description);

		post("/list/" + listId + "/task", clickUpAuth, taskBody);

		ObjectNode result = mapper.createObjectNode();
		result.put("teamId", teamId);
		result.put("spaceId", spaceId);
		result.put("folderId", folderId);
		result.put("listId", listId);
		result.put("listName", executionId);
		result.put("success", true);

		return result;
	}

	private JsonNode get(String path, String auth) throws Exception {
		return send("GET", path, auth, null);
	}

	private JsonNode post(String path, String auth, JsonNode body) throws Exception {
		return send("POST", path, auth, body);
	}

	private JsonNode send(String method, String path, String auth, JsonNode body)
			throws IOException, InterruptedException {

		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path))
				.header("Authorization", auth).header("Content-Type", "application/json")
				.header("Accept", "application/json");

		if ("GET".equals(method)) {
			builder.GET();
		} else {
			builder.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
		}

		HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

		JsonNode responseBody = response.body().isBlank() ? mapper.createObjectNode()
				: mapper.readTree(response.body());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("ClickUp API failed. HTTP " + response.statusCode() + ", endpoint: " + path
					+ ", response: " + response.body());
		}

		if (responseBody.hasNonNull("err")) {
			throw new IOException("ClickUp API error: " + responseBody.path("err").asText());
		}

		return responseBody;
	}

	private String findIdByName(JsonNode items, String name, String resource) {
		String id = findOptionalIdByName(items, name);

		if (id == null) {
			throw new IllegalArgumentException(resource + " not found: " + name);
		}

		return id;
	}

	private String findOptionalIdByName(JsonNode items, String name) {
		if (items == null || !items.isArray() || name == null) {
			return null;
		}

		for (JsonNode item : items) {
			String itemName = item.path("name").asText("");

			if (itemName.equalsIgnoreCase(name.trim())) {
				String id = item.path("id").asText("");
				return id.isBlank() ? null : id;
			}
		}

		return null;
	}

	private String requiredId(JsonNode response, String resource) {
		String id = response.path("id").asText("");

		if (id.isBlank()) {
			throw new IllegalStateException("ClickUp did not return an ID for " + resource);
		}

		return id;
	}

	private String resolveStatus(String fireFlinkStatus, JsonNode availableStatuses) {
		String normalized = normalizeStatus(fireFlinkStatus);

		if (!availableStatuses.isArray()) {
			throw new IllegalStateException("ClickUp List did not return available statuses");
		}

		// Prefer an exact match, ignoring case.
		for (JsonNode status : availableStatuses) {
			String name = status.path("status").asText("");

			if (name.equalsIgnoreCase(fireFlinkStatus.trim())) {
				return name;
			}
		}

		// Support common equivalent names: PASSED/Pass, FAILED/Fail.
		String expected = switch (normalized) {
		case "passed", "pass", "success", "successful" -> "pass";
		case "failed", "fail", "failure" -> "fail";
		default -> normalized;
		};

		for (JsonNode status : availableStatuses) {
			String name = status.path("status").asText("");

			if (normalizeStatus(name).equals(expected)) {
				return name;
			}

			if (expected.equals("pass") && normalizeStatus(name).equals("passed")) {
				return name;
			}

			if (expected.equals("fail") && normalizeStatus(name).equals("failed")) {
				return name;
			}
		}

		throw new IllegalArgumentException("No matching ClickUp status for FireFlink status '" + fireFlinkStatus
				+ "'. Available statuses: " + availableStatuses);
	}

	private String normalizeStatus(String status) {
		return status == null ? "" : status.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

	private void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required");
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("FireFlink Mail", "srinivas.m@fireflink.com");
		model.getAttributes().put("FireFlink Password", "Qwerty@123");
		model.getAttributes().put("ClickUp Auth", "pk_234266524_RRTSACXYHRWX51WZ4B64QFFW2YQ4BWOV");
		model.getAttributes().put("ClickUp FolderName", "null");
		model.getAttributes().put("ClickUp SpaceName", "Fireflink Automation");
		model.getAttributes().put("ClickUp TeamName", "Fireflink");
		System.out.println(new PushResultToClickUP().execute(model));
	}
}