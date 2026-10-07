package com.fireflink.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.restassured.path.json.JsonPath;

public class FireflinkApis {
	private String baseURI = "https://app.fireflink.com/";

	private String v3Base;

	public JsonNode getSuiteDetails(String accessToken, String projId, String suiteId)
			throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseURI + "project/optimize/v1/suite/" + suiteId))
				.header("Content-Type", "application/json").header("Authorization", "Bearer " + accessToken)
				.header("projectId", projId).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(response.body());
		return rootNode;
	}

	public JsonNode selectedModulesAndScripts(JsonNode suiteDetails) {
		return suiteDetails.path("responseObject").path("selectedModulesAndScripts");
	}

	public JsonNode getMachineObject(JsonNode clientSystems, String machineName) {
		for (JsonNode clientSystem : clientSystems) {
			String hostName = clientSystem.path("machine").path("hostName").asText();
			if (machineName.contains(hostName)) {
//                log.info("Found machine: " + machineName);
				return clientSystem;
			}
		}
		return null;
	}

	public List<JsonNode> buildMachineObject(JsonNode clientMachines,
			Map<String, Map<String, Object>> machineInstanceDetails) throws IOException {
		List<JsonNode> machineObjects = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();

		for (Map.Entry<String, Map<String, Object>> entry : machineInstanceDetails.entrySet()) {
			String machineName = entry.getKey();
			Map<String, Object> details = entry.getValue();

			JsonNode inputNode = getMachineObject(clientMachines, machineName);
			if (inputNode != null) {
				try {
					ObjectNode outputNode = objectMapper.createObjectNode();
					outputNode.put("clientId", inputNode.get("clientId").asText());
					ObjectNode machineInstance = objectMapper.createObjectNode();
					machineInstance.put("clientId", inputNode.get("clientId").asText());
					machineInstance.put("numberOfRuns", 1);
					machineInstance.put("executionEnv", "Local");
					machineInstance.put("browserName", "Google Chrome");
					machineInstance.put("browserVersion",
							((List) com.jayway.jsonpath.JsonPath.parse(inputNode.toPrettyString())
									.read("$.systemConfigDetails[?(@.name=='Google Chrome')].version")).get(0)
									.toString());
					machineInstance.put("systemUrl", "");

					ObjectNode machineInfo = objectMapper.createObjectNode();
					machineInfo.put("osName", inputNode.path("machine").path("osName").asText());
					machineInfo.put("osVersion", inputNode.path("machine").path("osVersion").asText());
					machineInfo.put("hostName", inputNode.path("machine").path("hostName").asText());
					machineInstance.set("machineInfo", machineInfo);

					ArrayNode deviceInfoArray = objectMapper.createArrayNode();
					if (details.containsKey("deviceInfo")) {
						List<Map<String, String>> deviceInfoList = (List<Map<String, String>>) details
								.get("deviceInfo");
						for (Map<String, String> deviceInfoMap : deviceInfoList) {
							ObjectNode deviceInfoNode = objectMapper.createObjectNode();
							for (Map.Entry<String, String> deviceEntry : deviceInfoMap.entrySet()) {
								deviceInfoNode.put(deviceEntry.getKey(), deviceEntry.getValue());
							}
							deviceInfoArray.add(deviceInfoNode);
						}
					}
					machineInstance.set("deviceInfo", deviceInfoArray);
					machineInstance.put("headless", false);

					ArrayNode machineInstances = objectMapper.createArrayNode();
					machineInstances.add(machineInstance);
					outputNode.set("machineInstances", machineInstances);

					machineObjects.add(outputNode);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return machineObjects;
	}

	public JsonNode getMachineDetails(String accessToken, String projectId) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseURI + "project/optimize/v1/clientSystemConfig/all"))
				.header("Content-Type", "application/json").header("Authorization", "Bearer " + accessToken)
				.header("projectid", projectId).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        log.info(response.toString());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(response.body());
		return rootNode.path("responseObject").path("clientSystems");
	}

	public JsonNode generateSuiteBody(JsonNode selectedModulesAndScriptsNode, List<JsonNode> selectedMachinesJsonNodes,
			JsonNode waitTimeNode, JsonNode resultSettingNode, JsonNode executionTerminationNode,
			JsonNode autoRunSuite) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode rootNode = objectMapper.createObjectNode();

			// Determine if parallel execution is needed
			boolean parallelExecution = false;
			for (JsonNode machineNode : selectedMachinesJsonNodes) {
				JsonNode machineInstancesNode = machineNode.path("machineInstances");
				if (machineInstancesNode.isArray() && machineInstancesNode.size() > 1) {
					parallelExecution = true;
					break;
				}
			}

			// Set the parallel execution flag
			String isParallel = parallelExecution ? "true" : "false";

			// Execution Termination
			// Execution Termination
//			ObjectNode executionTerminationNode = objectMapper.createObjectNode();
			rootNode.set("executionTermination", executionTerminationNode);

			// Machines
			ObjectNode machinesNode = objectMapper.createObjectNode();
			machinesNode.put("multiple", isParallel);
			ArrayNode selectedMachinesNode = objectMapper.createArrayNode();
			selectedMachinesNode.addAll(selectedMachinesJsonNodes);
			machinesNode.set("selectedMachines", selectedMachinesNode);
			if (isParallel.equals("true")) {
				machinesNode.put("executionType", "SEND_SUITE_TO_ALL");
			}

			rootNode.set("machines", machinesNode);

			// Result Setting
//			ObjectNode resultSettingNode = objectMapper.createObjectNode();
//			resultSettingNode.put("captureScreenshots", "For Failed steps only");
//			resultSettingNode.put("captureVideoForFailedTestScript", "Don't Capture");
//			resultSettingNode.put("displayLogsInScriptResult", "On Suite Execution Completion / Termination");
			rootNode.set("resultSetting", resultSettingNode);

			// Wait Time
//			ObjectNode waitTimeNode = objectMapper.createObjectNode();
//			waitTimeNode.put("implicitWait", waitConfig.getOrDefault("implicitWaitInSeconds", "20"));
//			waitTimeNode.put("implicitWaitUnit", "Secs");
//			waitTimeNode.put("explicitlyWait", waitConfig.getOrDefault("explicitlyWait", "30"));
//			waitTimeNode.put("explicitlyWaitUnit", "Secs");
//			waitTimeNode.put("delayBetweenSteps", waitConfig.getOrDefault("delayBetweenStepsInSeconds", "0"));
//			waitTimeNode.put("delayBetweenStepsUnit", "Secs");
			rootNode.set("waitTime", waitTimeNode);

			// Selected Modules And Scripts
			rootNode.set("selectedModulesAndScripts", selectedModulesAndScriptsNode);

			// Delete Spill Over Memory
			rootNode.put("deleteSpillOverMemory", false);
			if (autoRunSuite != null) {
				rootNode.put("autoRunSuite", autoRunSuite.asBoolean() + "");
			}
			// Convert to JSON string
			String suitePayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
			System.out.println("Suite Payload: Generated");
			return rootNode;

		} catch (Exception e) {
			System.err.println("Error generating JSON body: " + e);
			return null;
		}
	}

	public String triggerSuite(String accessToken, JsonNode payload, String projId, String suiteId)
			throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		ObjectMapper objectMapper = new ObjectMapper();
		String body = objectMapper.writeValueAsString(payload);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseURI + "dashboardexecution/optimize/v1/dashboard/execution/suite/" + suiteId))
				.header("Content-Type", "application/json").header("Authorization", "Bearer " + accessToken)
				.header("projectId", projId).POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return JsonPath.from(response.body()).get("responseObject.id");
	}

}