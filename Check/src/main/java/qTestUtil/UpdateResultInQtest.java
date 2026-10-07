
package qTestUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateResultInQtest implements Nlp {
	private static String QTEST_BASE_URL = "";
	private static String TOKEN = "";
	private static String PROJECT_NAME = "";
	private static String RELEASE_NAME = "";
	private static String MODULE_NAME = "";

	private static Integer PROJECT_ID;
	private static Integer RELEASE_ID;
	private static Integer cycleId;
	private static Integer suiteId;
	private static Integer runId;
	private static String ffrunId;
	private static Integer moduleId;
	private static List<String> scriptIdList;

	private static Map<String, Integer> testCaseMap = new HashMap<>();
	private static List<List<String>> ffResultList = new ArrayList<>();

	private static final HttpClient client = HttpClient.newHttpClient();
	private static final ObjectMapper mapper = new ObjectMapper();
	private static String executionId;
	private static String currentLicenseId;

	@InputParams({ @InputParam(name = "Qtest Url", type = "java.lang.String"),
			@InputParam(name = "Qtest Token", type = "java.lang.String"),
			@InputParam(name = "Qtest Project Name", type = "java.lang.String"),
			@InputParam(name = "Qtest Release Name", type = "java.lang.String"),
			@InputParam(name = "Qtest Module Name", type = "java.lang.String"),
			@InputParam(name = "Qtest Cycle Id", type = "java.lang.String"),
			@InputParam(name = "Qtest Suite Id", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		QTEST_BASE_URL = (String) programElementsInput.get("Qtest Url");
		TOKEN = (String) programElementsInput.get("Qtest Token");
		PROJECT_NAME = (String) programElementsInput.get("Qtest Project Name");
		RELEASE_NAME = (String) programElementsInput.get("Qtest Release Name");
		MODULE_NAME = (String) programElementsInput.get("Qtest Module Name");
		String cycleId = (String) programElementsInput.get("Qtest Cycle Id");
		String suiteId = (String) programElementsInput.get("Qtest Suite Id");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			PROJECT_ID = fetchProjectId();
			RELEASE_ID = fetchReleaseId(PROJECT_ID);
			moduleId = fetchModuleId();
			fetchTestCases(moduleId);
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
//			Map<String, String> exeIdentifiers = Map.of("suiteId", "SUITE_5e0cc449-2d1f-40d6-936c-0be785762f63",
//					"executionId", "EXEC10793", "runId", "Runa7dd7d4a-244d-4b35-8d6f-40f07bacf6a6", "projectId",
//					"PJT1030", "licenseId", "LIC1026593");
			if (exeIdentifiers.containsKey("suiteId")) {
				log.info("exeIdentifiers" + exeIdentifiers);
				executionId = exeIdentifiers.get("executionId");
				ffrunId = exeIdentifiers.get("runId");
				currentLicenseId = exeIdentifiers.get("licenseId");
				getFFResults();
				log.info("ffResultMap" + ffResultList);
				log.info("ffResultMap" + testCaseMap);
				for (List<String> ffResult : ffResultList) {
					String testCaseTitle = ffResult.get(0);
					String status = ffResult.get(1);
					String exeStartDate = ffResult.get(2);
					String exeEndDate = ffResult.get(3);
					int statusID = mapStatus(status);
					createTestLog(
							createTestRun(Integer.parseInt(suiteId), testCaseMap.get(testCaseTitle), testCaseTitle),
							statusID, mapStatusById(statusID), exeStartDate, exeEndDate);
				}
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Updated results to QTest");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to update results to QTest " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

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

			// log.info("Generated Query : " + query);
			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);
			return documents.stream().map(Document::toJson).collect(java.util.stream.Collectors.joining(",", "[", "]"));
		} finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

	public static void getFFResults() throws JsonMappingException, JsonProcessingException {
		Map<String, String> queryMapSuiteResults = Map.of("executionId", executionId, "runId", ffrunId);
		String suiteLevelMongoData = connectAndFetchMongoDocumentsAsJsonString(queryMapSuiteResults, null, null,
				currentLicenseId + "_" + executionId + "_entity_results");
		log.info("suiteLevelMongoData==> " + suiteLevelMongoData);
//		FileWriter fw = new FileWriter(new File("C:\\Users\\User\\Desktop\\JJJJJ.json"));
//		fw.write(executionDetailsResponsePayload.toString());
//		fw.close();

		scriptIdList = (List<String>) getJsonValue(suiteLevelMongoData, "[?(@.entityType=='Script')].originalId");
		log.info("scriptIdList" + scriptIdList);
		scriptIdList.remove(scriptIdList.size() - 1);
		for (int i = 0; i < scriptIdList.size(); i++) { // Ignore first and last script
			String scriptId = scriptIdList.get(i);
			System.out.println(getJsonValue(suiteLevelMongoData,
					"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')]"));
			String runInitiatedOn = ((List<String>) (getJsonValue(suiteLevelMongoData,
					"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].runInitiatedOn"))).get(0);

			long executionDurationMillis = ((List<Integer>) (getJsonValue(suiteLevelMongoData,
					"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].executionDuration"))).get(0);

			Instant startTime = Instant.parse(runInitiatedOn);
			Instant endTime = startTime.plusMillis(executionDurationMillis);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);

			String exeStartDate = formatter.format(startTime);
			String exeEndDate = formatter.format(endTime);

			ffResultList.add(Arrays.asList(
					((List<String>) (getJsonValue(suiteLevelMongoData,
							"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].name"))).get(0),
					((List<String>) (getJsonValue(suiteLevelMongoData,
							"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].status"))).get(0),
					exeStartDate, exeEndDate));
		}
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return com.jayway.jsonpath.JsonPath.read(jsonBody, "$." + jsonPath, new com.jayway.jsonpath.Predicate[0]);
	}

	public static int mapStatus(String status) {

		switch (status) {
		case "PASS":
			return 601;
		case "FAIL":
			return 602;
		case "SKIP":
			return 605;
		default:
			return 602;
		}
	}

	public static String mapStatusById(int status) {

		switch (status) {
		case 601:
			return "Pass";
		case 602:
			return "Fail";
		case 605:
			return "Skip";
		default:
			return "Fail";
		}
	}

	private static JsonNode sendGet(String url) throws Exception {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + TOKEN)
				.header("Content-Type", "application/json").GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new RuntimeException(
					"GET Failed. Status: " + response.statusCode() + "\nResponse: " + response.body());
		}

		return mapper.readTree(response.body());
	}

	private static JsonNode sendPost(String url, String payload) throws Exception {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + TOKEN)
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new RuntimeException(
					"POST Failed. Status: " + response.statusCode() + "\nResponse: " + response.body());
		}

		return mapper.readTree(response.body());
	}

	public static int fetchProjectId() throws Exception {

		log.info("QTEST_BASE_URL = " + QTEST_BASE_URL);
		log.info("Final URL = " + QTEST_BASE_URL + "/api/v3/projects");

		JsonNode projects = sendGet(QTEST_BASE_URL + "/api/v3/projects");

		for (JsonNode project : projects) {

			if (PROJECT_NAME.equals(project.path("name").asText())) {
				return project.path("id").asInt();
			}
		}

		throw new RuntimeException("Project '" + PROJECT_NAME + "' not found");
	}

	public static int fetchReleaseId(int projectId) throws Exception {

		JsonNode releases = sendGet(QTEST_BASE_URL + "/api/v3/projects/" + projectId + "/releases");

		for (JsonNode release : releases) {

			if (RELEASE_NAME.equals(release.path("name").asText())) {
				return release.path("id").asInt();
			}
		}

		throw new RuntimeException("Release '" + RELEASE_NAME + "' not found");
	}

	public static int fetchModuleId() throws Exception {

		JsonNode modules = sendGet(QTEST_BASE_URL + "/api/v3/projects/" + PROJECT_ID + "/modules");

		for (JsonNode module : modules) {

			if (MODULE_NAME.equals(module.path("name").asText())) {
				return module.path("id").asInt();
			}
		}

		throw new RuntimeException("Module '" + MODULE_NAME + "' not found");
	}

	public static void fetchTestCases(int moduleId) throws Exception {

		JsonNode testCases = sendGet(QTEST_BASE_URL + "/api/v3/projects/" + PROJECT_ID + "/test-cases?parentId="
				+ moduleId + "&expandSteps=true");

		testCaseMap.clear();

		for (JsonNode testCase : testCases) {

			String testCaseName = testCase.path("name").asText();
			int testCaseId = testCase.path("id").asInt();

			testCaseMap.put(testCaseName, testCaseId);
		}

		log.info("Loaded " + testCaseMap.size() + " test cases from module");
	}

	public static int createTestCycle() throws Exception {

		String payload = String.format("""
				{
				  "name":"Auto Cycle - %s"
				}
				""");

		JsonNode response = sendPost(QTEST_BASE_URL + "/api/v3/projects/" + PROJECT_ID + "/test-cycles?parentId="
				+ RELEASE_ID + "&parentType=release", payload);

		return response.path("id").asInt();
	}

	public static int createTestSuite(int cycleId) throws Exception {

		String payload = String.format("""
				{
				  "name":"Auto Suite - %s"
				}
				""");

		JsonNode response = sendPost(QTEST_BASE_URL + "/api/v3/projects/" + PROJECT_ID + "/test-suites?parentId="
				+ cycleId + "&parentType=test-cycle", payload);

		return response.path("id").asInt();
	}

	public static int createTestRun(int suiteId, int testCaseId, String title) throws Exception {

		String payload = String.format("""
				{
				  "name":"FireFlink Run - %s",
				  "test_case":{
				    "id":%d
				  }
				}
				""", title, testCaseId);

		JsonNode response = sendPost(QTEST_BASE_URL + "/api/v3/projects/" + PROJECT_ID + "/test-runs?parentId="
				+ suiteId + "&parentType=test-suite", payload);

		return response.path("id").asInt();
	}

	public static void createTestLog(int runId, int status, String note, String exeStartDate, String exeEndDate)
			throws Exception {

		String payload = String.format("""
				{
				  "exe_start_date":"%s",
				  "exe_end_date":"%s",
				  "status":{
				    "id":%d
				  },
				  "note":"%s"
				}
				""", exeStartDate, exeEndDate, status,
				note == null ? "Executed via FireFlink" : note.replace("\"", "\\\""));

		sendPost(QTEST_BASE_URL + "/api/v3/projects/" + PROJECT_ID + "/test-runs/" + runId + "/test-logs", payload);
	}

	public static void initialize() throws Exception {

		PROJECT_ID = fetchProjectId();
		log.info("Resolved Project ID: " + PROJECT_ID);

		RELEASE_ID = fetchReleaseId(PROJECT_ID);
		log.info("Resolved Release ID: " + RELEASE_ID);

		moduleId = fetchModuleId();

		fetchTestCases(moduleId);

		if (cycleId != null && suiteId != null) {

			log.info("Using existing cycle and suite.");

		} else {

			cycleId = createTestCycle();
			suiteId = createTestSuite(cycleId);

			log.info("Created new cycle and suite.");
		}

		log.info("Cycle, Suite & Test Case Map Ready");
	}

	public static Integer getRunId() {
		return runId;
	}

	public static Integer getProjectId() {
		return PROJECT_ID;
	}

	public static Integer getReleaseId() {
		return RELEASE_ID;
	}

	public static Integer getCycleId() {
		return cycleId;
	}

	public static Integer getSuiteId() {
		return suiteId;
	}

	public static Map<String, Integer> getTestCaseMap() {
		return testCaseMap;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("Qtest Url", "https://mahadeva.qtestnet.com");
		nlpreq.getAttributes().put("Qtest Token", "a1ff80e3-c41b-435e-a60c-8dc7193718ae");
		nlpreq.getAttributes().put("Qtest Project Name", "QtestFireflink_Integration");
		nlpreq.getAttributes().put("Qtest Release Name", "Release 1");
		nlpreq.getAttributes().put("Qtest Module Name", "Smoke Test");
		nlpreq.getAttributes().put("Qtest Cycle Id", "806834");
		nlpreq.getAttributes().put("Qtest Suite Id", "1031956");
		System.out.println(new UpdateResultInQtest().execute(nlpreq));
	}
}