package clickUp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PushResultsIntoClickUp {

	private static final String BASE_URL = "https://api.clickup.com/api/v2";

	private final HttpClient httpClient;
	private final ObjectMapper mapper;

	public PushResultsIntoClickUp() {
		this.httpClient = HttpClient.newHttpClient();
		this.mapper = new ObjectMapper();
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

		// Suite name is required when folderName is null.
//		String suiteName = firstText(fireFlinkResult, "SuiteName", "suiteName", "Suite Name");

		// 3. Find the supplied Folder, or create a new one.
		String folderId;

		if (folderName != null && !folderName.isBlank()) {

			JsonNode foldersResponse = get("/space/" + spaceId + "/folder", clickUpAuth);

			folderId = findIdByName(foldersResponse.path("folders"), folderName, "Folder");

		} else {

			requireText(suiteName, "Suite Name");

			ObjectNode folderBody = mapper.createObjectNode();
			folderBody.put("name", suiteName);

			JsonNode folderResponse = post("/space/" + spaceId + "/folder", clickUpAuth, folderBody);

			folderId = requiredId(folderResponse, "Folder");
		}

		// 4. Execution ID becomes the ClickUp List name.
//		String executionId = firstText(fireFlinkResult, "ExecutionId", "executionId", "Execution ID");

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
//		JsonNode scripts = findScripts(fireFlinkResult);
//
//		if (!scripts.isArray() && !scripts.isObject()) {
//			throw new IllegalArgumentException("No script results found in FireFlink result");
//		}
//
//		int createdCount = 0;
//
//		Iterator<JsonNode> iterator = scripts.isArray() ? scripts.elements()
//				: java.util.Collections.singleton(scripts).iterator();
//
//		while (iterator.hasNext()) {
//			JsonNode script = iterator.next();

//			String scriptName = firstText(script, "ScriptName", "scriptName", "TestCaseName", "testCaseName");
//
//			String status = firstText(script, "Status", "status");
//
//			JsonNode manualDetails = firstNode(script, "ManualTestcaseDetails", "ManualTestCaseDetails",
//					"manualTestcaseDetails", "manualTestCaseDetails", "Manual Testcase details", "manualTestCase",
//					"manualTestcase", "description");

		requireText(scriptName, "ScriptName");
		requireText(status, "Status");

//			if (manualDetails == null || manualDetails.isNull()) {
//				throw new IllegalArgumentException("Manual test case details missing for: " + scriptName);
//			}

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
		if (!items.isArray()) {
			return null;
		}

		for (JsonNode item : items) {
			if (item.path("name").asText().equalsIgnoreCase(name.trim())) {
				return item.path("id").asText();
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

	private JsonNode findScripts(JsonNode root) {
		for (String field : new String[] { "Scripts", "scripts", "Results", "results", "TestCases", "testCases" }) {
			JsonNode value = root.get(field);

			if (value != null && (value.isArray() || value.isObject())) {
				return value;
			}
		}

		if (firstText(root, "ScriptName", "scriptName") != null) {
			return mapper.createArrayNode().add(root);
		}

		return mapper.createArrayNode();
	}

	private String firstText(JsonNode node, String... fields) {
		for (String field : fields) {
			JsonNode value = node.get(field);

			if (value != null && !value.isNull()) {
				String text = value.asText();

				if (!text.isBlank()) {
					return text.trim();
				}
			}
		}

		return null;
	}

	private JsonNode firstNode(JsonNode node, String... fields) {
		for (String field : fields) {
			JsonNode value = node.get(field);

			if (value != null && !value.isNull()) {
				return value;
			}
		}

		return null;
	}

	private void requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required");
		}
	}

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		JsonNode fireFlinkResult = mapper.readTree("""
				{
				  "SuiteName": "Regression",
				  "ExecutionId": "FF-EXEC-1001",
				  "Scripts": [
				    {
				      "ScriptName": "Valid Login",
				      "Status": "PASSED",
				      "ManualTestcaseDetails": "Verify login with valid credentials."
				    },
				    {
				      "ScriptName": "Invalid Login",
				      "Status": "FAILED",
				      "ManualTestcaseDetails": "Verify invalid login error handling."
				    }
				  ]
				}
				""");

		PushResultsIntoClickUp service = new PushResultsIntoClickUp();

		JsonNode result = service.publishExecutionResults("FireFlink", "Automation", null,
				System.getenv("CLICKUP_API_TOKEN"), fireFlinkResult);

		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	}
}