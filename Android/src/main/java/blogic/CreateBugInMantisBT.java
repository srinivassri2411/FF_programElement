//package blogic;
//
//import static io.restassured.RestAssured.baseURI;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.KeyFactory;
//import java.security.PublicKey;
//import java.security.spec.EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import javax.crypto.Cipher;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.jayway.jsonpath.JsonPath;
//import com.tyss.optimize.common.util.CommonConstants;
//import com.tyss.optimize.nlp.util.Nlp;
//import com.tyss.optimize.nlp.util.NlpException;
//import com.tyss.optimize.nlp.util.NlpRequestModel;
//import com.tyss.optimize.nlp.util.NlpResponseModel;
//import com.tyss.optimize.nlp.util.annotation.InputParam;
//import com.tyss.optimize.nlp.util.annotation.InputParams;
//import com.tyss.optimize.nlp.util.annotation.ReturnType;
//
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import io.restassured.response.Response;
//import io.restassured.specification.RequestSpecification;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class CreateBugInMantisBT implements Nlp {
//	public static List<String> listOfSteps = null;
//	public static String failedStepId = "";
//	public static int slNoAndRowNumber = 1;
//	public static String scriptName = "";
//	public static String module = "";
//	public static String createdDate = "";
//	public static Boolean isFirstBugAdded = false;
//	public static String scope = null; // AllFailedSteps, OnlyFirstFailedStep
//	public static String token = "";
//	public static String projectId = "";
//	public static String scriptFolderPath = "";
//	public static String scriptId = "";
//	public static Object failedIteration = null;
//	public static String variableType = ""; // GLOBAL, PROJECT_ENVIRONMENT
//	public static String variableName = "";
//	public static String variableValue = "";
//	public static List<Map<String, Object>> iterationsList = null;
//	public static boolean isItrCompleted = false;
//	public static StringBuilder platformDetails = null;
//	public static String mantisBTUrl = null;
//	public static String mantisAuth = null;
//	public static String mantisProjID = null;
//	public static String mantisProjName = null;
//	public static String mantisCategory = null;
//	// Added issues list
//	public static String executionFolderPath = "";
//	public static StringBuilder html = null;
//	public static int slNo = 0;
//
//	@InputParams({ @InputParam(name = "Mantis URL", type = "java.lang.String"),
//			@InputParam(name = "Mantis Auth", type = "java.lang.String"),
//			@InputParam(name = "Mantis ProjId", type = "java.lang.String"),
//			@InputParam(name = "Mantis ProjName", type = "java.lang.String"),
//			@InputParam(name = "Mantis Category", type = "java.lang.String"),
//			@InputParam(name = "Scope(AllFailedSteps / OnlyFirstFailedStep)", type = "java.lang.String"),
//			@InputParam(name = "Fireflink Email", type = "java.lang.String"),
//			@InputParam(name = "Fireflink Password", type = "java.lang.String"),
//			@InputParam(name = "Fireflink Lic ID", type = "java.lang.String"),
//			@InputParam(name = "Fireflink Project", type = "java.lang.String"),
//			@InputParam(name = "Fireflink Exce ID", type = "java.lang.String") })
//	@ReturnType(name = "AddedIssue", type = "java.lang.String")
//	@Override
//	public List<String> getTestParameters() throws NlpException {
//		List<String> params = new ArrayList<>();
//		return params;
//	}
//
//	@Override
//	public StringBuilder getTestCode() throws NlpException {
//		StringBuilder sb = new StringBuilder();
//		return sb;
//	}
//
//	@Override
//	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
//
//		NlpResponseModel nlpResponseModel = new NlpResponseModel();
//		Map<String, Object> attributes = nlpRequestModel.getAttributes();
//		mantisBTUrl = ensureTrailingSlash((String) attributes.get("Mantis URL"));
//		mantisAuth = (String) attributes.get("Mantis Auth");
//		mantisProjID = (String) attributes.get("Mantis ProjId");
//		mantisProjName = (String) attributes.get("Mantis ProjName");
//		mantisCategory = (String) attributes.get("Mantis Category");
//		scope = (String) attributes.get("Scope(AllFailedSteps / OnlyFirstFailedStep)");
//		String ffmail = (String) attributes.get("Fireflink Email");
//		String ffPassword = (String) attributes.get("Fireflink Password");
//		String ffLic = (String) attributes.get("Fireflink Lic ID");
//		String ffproj = (String) attributes.get("Fireflink Project");
//		String ffExce = (String) attributes.get("Fireflink Exce ID");
//		try {
//			slNo = 1;
//			listOfSteps = new LinkedList<String>();
//			raiseId(ffmail, ffPassword, ffproj, ffExce, ffLic);
//			nlpResponseModel.setMessage("Created Issues in mantisBT");
//			nlpResponseModel.setStatus(CommonConstants.pass);
//		} catch (Exception e) {
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			e.printStackTrace(pw);
//			nlpResponseModel.setStatus(CommonConstants.fail);
//			nlpResponseModel.setMessage("Failed to create Issues in mantisBT" + sw.toString());
//			nlpResponseModel.setStatus(CommonConstants.fail);
//		}
//		nlpResponseModel.getAttributes().put("AddedIssue", executionFolderPath + File.separator + "createdisuues.html");
////		addedIssue.clear();
//		return nlpResponseModel;
//	}
//
//	private static void raiseId(String ffemail, String ffPassword, String projectNameGiven, String executionId,
//			String licId) throws Exception {
//		// Logic Starts
//		String projectType = "";
//
//		// API Base URL
//		baseURI = "https://app.fireflink.com/";
//
//		// Sign in API Request
//		ffPassword = encryptPassword(ffPassword);
//		String signInRequestPayload = "{ \"emailId\": \"" + ffemail + "\", \"password\": \"" + ffPassword
//				+ "\" , \"currentLicenseId\": \"" + licId + "\"}";
//		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
//				signInRequestPayload);
//
//		String message = convertToString(getJsonValue(signInResponsePayload, "message"));
//
//		// Provide message if EmailID/Password is Invalid
//		if (message != null && message.contains("Email or Password is invalid")) {
//			System.err.println(message);
//			System.exit(1);
//		}
//
//		// Fetch UserId and Access Token
//		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
//		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));
//
//		// Get All projects API Request
//		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId,
//				"Authorization", "Bearer " + token);
//		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));
//
//		// Iterate and check given Project exists, If YES fetch ProjectId and
//		// ProjectType
//		for (int i = 0; i < noOfProjects; i++) {
//			String projectName = convertToString(
//					getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.name"));
//			if (projectName.equals(projectNameGiven)) {
//				projectId = convertToString(
//						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.id"));
//				projectType = convertToString(
//						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.type"));
//				break;
//			}
//		}
//
//		// If given Project doesn't exists throw error message
//		if (projectId.equals("")) {
//			System.err.println("Invalid Project name given, please try again...");
//			System.exit(1);
//		}
//		// Create execution folder
//		String homeDir = System.getProperty("user.home");
//		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//		String folderName = "Fireflink" + File.separator + executionId + "_" + timestamp;
//
//		executionFolderPath = createDirectory(homeDir, folderName);
//		System.out.println("Created Folder at: " + executionFolderPath);
//
//		String executionLevelResponsePayload = getRequest(
//				"executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId, "Authorization",
//				"Bearer " + token, "projectId", projectId, "projectType", projectType);
//
//		int noOfPlatforms = convertToInteger(
//				getJsonValue(executionLevelResponsePayload, "responseObject.platformStatistics.length()"));
//		startHtml(executionId);
//		for (int j = 0; j < noOfPlatforms; j++) {
//
//			String clientId = convertToString(getJsonValue(executionLevelResponsePayload,
//					"responseObject.platformStatistics[" + j + "].clientId"));
//			String runId = convertToString(
//					getJsonValue(executionLevelResponsePayload, "responseObject.platformStatistics[" + j + "].runId"));
//
//			Map<String, Object> executionStatus = (Map<String, Object>) getJsonValue(executionLevelResponsePayload,
//					"responseObject.executionResponses[" + j + "].executionStatusObject");
//
//			// List to store failed scripts
//			List<String> failedScripts = new LinkedList<String>();
//
//			// Iterate over key-value pairs
//			for (Map.Entry<String, Object> entry : executionStatus.entrySet()) {
//				if (entry.getKey().startsWith("SCR") && entry.getValue().equals("FAIL")) {
//					failedScripts.add(entry.getKey());
//				}
//			}
//
//			createdDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
//
//			// System.out.println(failedScripts);
//			for (int i = 0; i < failedScripts.size(); i++) {
//				scriptId = failedScripts.get(i);
//				scriptFolderPath = createDirectory(executionFolderPath, "Screenshots" + File.separator + scriptId);
//				String scriptLevelResponsePayload = getRequest(
//						"executionresult/optimize/v1/executionResponse/result/" + executionId + "/" + clientId + "/"
//								+ runId + "/" + scriptId,
//						"Authorization", "Bearer " + token, "projectId", projectId, "projectType", projectType);
//				scriptName = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.title"));
//				module = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.path"));
//				module = module.substring(0, module.lastIndexOf("/"));
//
////                System.out.println("scriptLevelResponsePayload" + "\n" + scriptLevelResponsePayload);
//				// platformDetails
//				platformDetails = new StringBuilder();
//				String exeEnvi = convertToString(
//						getJsonValue(scriptLevelResponsePayload, "responseObject.selectedSystem.executionEnv"));
//				if (exeEnvi.equalsIgnoreCase("local")) {
//					platformDetails.append("Browser: "
//							+ convertToString(getJsonValue(scriptLevelResponsePayload,
//									"responseObject.selectedSystem.browserInfo.name"))
//							+ " " + convertToString(getJsonValue(scriptLevelResponsePayload,
//									"responseObject.selectedSystem.browserInfo.version"))
//							+ "\n");
//					platformDetails.append("Operating System: "
//							+ convertToString(getJsonValue(scriptLevelResponsePayload,
//									"responseObject.selectedSystem.machineInfo.osName"))
//							+ " " + convertToString(getJsonValue(scriptLevelResponsePayload,
//									"responseObject.selectedSystem.machineInfo.osVersion"))
//							+ "\n");
//					platformDetails.append("Execution environment: " + convertToString(
//							getJsonValue(scriptLevelResponsePayload, "responseObject.selectedSystem.executionEnv"))
//							+ "\n");
//					platformDetails.append("Machine: " + convertToString(getJsonValue(scriptLevelResponsePayload,
//							"responseObject.selectedSystem.machineInfo.hostName")));
//				} else {
//					platformDetails.append("Browser: " + convertToString(
//							getJsonValue(scriptLevelResponsePayload, "responseObject.selectedSystem.browserInfo.name"))
//							+ " "
//							+ convertToString(
//									getJsonValue(scriptLevelResponsePayload, "responseObject.selectedSystem.version"))
//							+ "\n");
//					List<Object> listOfDevices = io.restassured.path.json.JsonPath.from(scriptLevelResponsePayload)
//							.getList("responseObject.selectedSystem.deviceInfo");
//					for (Object device : listOfDevices) {
//						HashMap deviceMap = (HashMap) device;
//						int deviceCount = 0;
//						platformDetails.append("Device" + ++deviceCount + ": " + deviceMap.get("name") + " "
//								+ deviceMap.get("version") + "\n");
//					}
//					platformDetails.append("Operating System: "
//							+ convertToString(getJsonValue(scriptLevelResponsePayload,
//									"responseObject.selectedSystem.machineInfo.osName"))
//							+ " " + convertToString(getJsonValue(scriptLevelResponsePayload,
//									"responseObject.selectedSystem.machineInfo.osVersion"))
//							+ "\n");
//					platformDetails.append("Execution environment: " + convertToString(
//							getJsonValue(scriptLevelResponsePayload, "responseObject.selectedSystem.executionEnv"))
//							+ "\n");
//					platformDetails.append("Machine: " + convertToString(getJsonValue(scriptLevelResponsePayload,
//							"responseObject.selectedSystem.machineInfo.hostName")));
//				}
//
////                System.out.println("platformDetails--" + platformDetails);
//
//				// Handle Depends on script
//				try {
//					List<Map<String, Object>> dependsOnScripts = (List<Map<String, Object>>) getJsonValue(
//							scriptLevelResponsePayload, "responseObject.dependantExecutionEntityResponses");
//					if (dependsOnScripts != null) {
//						listOfSteps.add("dependsOnScript:");
//						for (Map<String, Object> script : dependsOnScripts) {
//							String dependScriptId = script.get("key").toString();
//
//							String dependScriptLevelResponsePayload = getRequest(
//									"executionresult/optimize/v1/executionResponse/result/" + executionId + "/"
//											+ clientId + "/" + runId + "/" + dependScriptId,
//									"Authorization", "Bearer " + token, "projectId", projectId, "projectType",
//									projectType);
//
//							addStepsToList(dependScriptLevelResponsePayload,
//									"responseObject.preConditions.stepResults");
//							if (!isFirstBugAdded) {
//								addStepsToList(dependScriptLevelResponsePayload, "responseObject.stepResults");
//								if (!isFirstBugAdded) {
//									addStepsToList(dependScriptLevelResponsePayload,
//											"responseObject.postConditions.stepResults");
//								}
//							}
//						}
//					}
//				} catch (Exception e) {
//				}
//
//				listOfSteps.add("script:");
//				addStepsToList(scriptLevelResponsePayload, "responseObject.preConditions.stepResults");
//				if (!isFirstBugAdded) {
//					addStepsToList(scriptLevelResponsePayload, "responseObject.stepResults");
//					if (!isFirstBugAdded) {
//						addStepsToList(scriptLevelResponsePayload, "responseObject.postConditions.stepResults");
//					}
//				}
//				// Clear the list
//				listOfSteps.clear();
//				isFirstBugAdded = false;
//			}
//		}
//		endHtml();
//	}
//
//	private static void addStepsToList(String scriptLevelResponsePayload, String jsonPath) {
//		List<Map<String, Object>> stepResults;
//		try {
//			stepResults = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload, jsonPath);
//			listOfSteps.add(jsonPath.split("\\.")[1] + ":");
//			if (stepResults == null) {
//				stepResults = List.of();
//			}
//			iterateStepResultsAndAddToSet(stepResults, 0);
//		} catch (Exception e) {
//			StringWriter writer = new StringWriter();
//			e.printStackTrace(new PrintWriter(writer));
//			System.out.println(writer);
//			stepResults = List.of();
//		}
//	}
//
//	private static String getRequest(String endpoint, String... headers) {
//		RequestSpecification request = RestAssured.given();
//
//		// Add headers if provided (expects key-value pairs)
//		if (headers.length % 2 != 0) {
//			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
//		}
//
//		for (int i = 0; i < headers.length; i += 2) {
//			request.header(headers[i], headers[i + 1]);
//		}
//		return request.when().get(endpoint).asString();
//	}
//
//	private static String postRequest(String endpoint, String requestBody, String... headers) {
//		RequestSpecification request = RestAssured.given();
//
//		// Add headers if provided (expects key-value pairs)
//		if (headers.length % 2 != 0) {
//			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
//		}
//
//		for (int i = 0; i < headers.length; i += 2) {
//			request.header(headers[i], headers[i + 1]);
//		}
//		return request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint).asString();
//	}
//
//	private static String putRequest(String endpoint, String requestBody, String... headers) {
//		RequestSpecification request = RestAssured.given();
//
//		// Add headers if provided (expects key-value pairs)
//		if (headers.length % 2 != 0) {
//			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
//		}
//
//		for (int i = 0; i < headers.length; i += 2) {
//			request.header(headers[i], headers[i + 1]);
//		}
//		return request.body(requestBody).contentType(ContentType.JSON).when().put(endpoint).asString();
//	}
//
//	public static Object getJsonValue(String jsonBody, String jsonPath) {
//		return JsonPath.read(jsonBody, "$." + jsonPath);
//	}
//
//	public static boolean convertToBoolean(Object obj) {
//		return (boolean) obj;
//	}
//
//	public static int convertToInteger(Object obj) {
//		return (int) obj;
//	}
//
//	public static String convertToString(Object obj) {
//		return (String) obj;
//	}
//
//	public static double convertToDouble(Object obj) {
//		return (double) obj;
//	}
//
//	public static String encryptPassword(String password) throws Exception {
//		byte[] publicKeyBytes = Base64.getDecoder().decode(
//				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
//		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
//		return Base64.getEncoder().encodeToString(encryptedBytes);
//	}
//
//	public static void createIssueToMantis() throws Exception {
//		String failedStepResponsePayload = getRequest(
//				"executionresult/optimize/v1/executionResponse/step/" + failedStepId, "Authorization",
//				"Bearer " + token, "projectId", projectId);
//		String failedStep = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.message"));
//		String stepId = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.stepId"));
//		String screenShotPath;
//		try {
//			screenShotPath = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.screenshotPath"));
//		} catch (Exception e) {
//			screenShotPath = "";
//		}
//
//		String cause = "";
//		try {
//			cause = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.errorLog.cause"));
//		} catch (Exception e) {
//			cause = "None";
//		}
//
//		String attachmentPath = "";
//		// Attach Screenshot
//		if (screenShotPath.isEmpty()) {
//			attachmentPath = "";
//		} else {
//			String requestBody = "{\"bucketName\":\"flinko-prod\",\"filePath\":[\"" + screenShotPath
//					+ "\"],\"type\":\"cloudS3\",\"contentType\":\"screenshot\"}";
//			attachmentPath = addScreenshotToFolder("dashboardexecution/optimize/v1/dashboard/download/storage",
//					requestBody, token, projectId, scriptFolderPath, stepId);
//		}
//		String summary = null;
//		if (failedIteration != null) {
//			summary = "Failed Iteraion = " + (convertToInteger(failedIteration) + 1) + "\n" + failedStep;
//		} else {
//			summary = failedStep;
//		}
//		String description = null;
//		if (cause.contains("Exception")) {
//			description = scriptName + " ,\nReason is \n" + cause.split("Exception", 2)[0];
//		} else {
//			description = scriptName + " ,\nReason is \n" + cause;
//		}
//		LinkedHashMap<String, String> attachments = new LinkedHashMap<>();
//		String createdBody = null;
//		if (attachmentPath.isEmpty()) {
//			attachments.put("", "");
//			createdBody = createBody(summary, description, processSteps(listOfSteps), attachments,
//					String.valueOf(platformDetails));
//		} else {
//			byte[] fileContent = Files.readAllBytes(Paths.get(attachmentPath));
//			String encodedString = Base64.getEncoder().encodeToString(fileContent);
//			attachments.put(Paths.get(attachmentPath).getFileName().toString(), encodedString);
//			createdBody = createBody(summary, description, processSteps(listOfSteps), attachments,
//					String.valueOf(platformDetails));
//		}
//		Map<String, String> headers = new LinkedHashMap<>();
//		headers.put("Authorization", mantisAuth);
//		headers.put("Content-Type", "application/json");
//		try {
//			Response issueCreated = createBug(mantisBTUrl, createdBody, headers);
////	            System.out.println(issueCreated.getBody().asString());
//			int createdIsuue = convertToInt(JsonPath.read(issueCreated.getBody().asString(), "$.issue.id"));
//			addLineToHTML(slNo++, scriptName, failedStep, createdIsuue + "");
//		} catch (Exception e) {
////			new FileWriter("C:\\Users\\Srinivas m\\Desktop\\yryry.txt").append(createdBody);
//			e.printStackTrace();
//		}
//	}
//
//	public static void iterateStepResultsAndAddToSet(List<Map<String, Object>> jsonArray, int spaces) throws Exception {
//		for (Map<String, Object> map : jsonArray) {
//			int iteration = 0;
//			try {
//				iteration = convertToInt(map.get("iteration"));
//			} catch (Exception e) {
//			}
//
//			if (map.get("type").equals("Group") && iteration > 0) {
//				getFailedIteration(jsonArray);
//				break;
//			} else if (map.get("type").equals("step") && iteration > 0) {
//				getFailedIteration(jsonArray);
//				break;
//			} else if (map.get("type").equals("Iteration")) {
//				iterationsList = (List<Map<String, Object>>) map.get("stepResults");
//				int iterationsCount = convertToInt(iterationsList.get(iterationsList.size() - 1).get("iteration")) + 1;
//				listOfSteps.add(spaces + "#" + map.get("title").toString() + "(Iterations = " + iterationsCount + ")");
//			} else if (map.get("type").equals("Group") || map.get("type").equals("EndIteration")) {
//				listOfSteps.add(spaces + "#" + map.get("title").toString() + "(Group)");
//			} else if (map.get("type").equals("step")) {
//				listOfSteps.add(spaces + "#" + map.get("title").toString() + "(Step)");
//			}
//
////				if (map.get("type").equals("Group") && map.get("status").equals("FAIL")) {
////					try {
////						failedIteration = convertToInt(map.get("iteration"));
////					} catch (Exception e) {
////					}
////				}
//
//			// If current step is failed, break and return true
//			if (map.get("type").equals("step") && map.get("status").equals("FAIL")) {
//				failedStepId = map.get("uniqueId").toString();
//				if (scope.equals("OnlyFirstFailedStep") && !isFirstBugAdded) {
//					createIssueToMantis();
//					slNoAndRowNumber++;
//					isFirstBugAdded = true;
//					break;
//				} else if (scope.equals("AllFailedSteps")) {
//					createIssueToMantis();
//					slNoAndRowNumber++;
//				}
//			}
//			if (isItrCompleted) {
//				isItrCompleted = false;
//				break;
//			}
//
//			// If stepResults exist, recurse and break on failure
//			if (map.containsKey("stepResults")) {
//				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("stepResults");
//				iterateStepResultsAndAddToSet(subJsonArray, spaces + 1);
//			}
//		}
//	}
//
//	public static String processSteps(List<String> listOfSteps) {
//		Map<Integer, Integer> numMap = new LinkedHashMap<Integer, Integer>();
//		int number = 1;
//		int restart = 1;
//		String steps = "";
//		for (int j = 0; j < listOfSteps.size(); j++) {
//			String step = listOfSteps.get(j);
//			if (j != 0
//					&& (listOfSteps.get(j - 1).equals("preConditions:") || listOfSteps.get(j - 1).equals("stepResults:")
//							|| listOfSteps.get(j - 1).equals("postConditions:"))) {
//				number = restart;
//				numMap.put(Integer.parseInt(step.split("#")[0]), number);
//			} else if (!step.equals("dependsOnScript:") && !step.equals("script:") && !step.equals("preConditions:")
//					&& !step.equals("stepResults:") && !step.equals("postConditions:")) {
//				if (numMap.containsKey(Integer.parseInt(step.split("#")[0]))
//						&& Integer.parseInt(listOfSteps.get(j - 1).split("#")[0]) >= Integer
//								.parseInt(listOfSteps.get(j).split("#")[0])) {
//					number = numMap.get(Integer.parseInt(step.split("#")[0])) + 1;
//					numMap.put(Integer.parseInt(step.split("#")[0]), number);
//				} else if (numMap.containsKey(Integer.parseInt(step.split("#")[0]))
//						&& Integer.parseInt(listOfSteps.get(j - 1).split("#")[0]) < Integer
//								.parseInt(listOfSteps.get(j).split("#")[0])) {
//					number = restart;
//					numMap.put(Integer.parseInt(step.split("#")[0]), number);
//				} else {
//					number = restart;
//					numMap.put(Integer.parseInt(step.split("#")[0]), number);
//				}
//			}
//			String spaces = "";
//			try {
//				int noOfSpaces = Integer.parseInt(step.split("#")[0]);
//				for (int i = 0; i < noOfSpaces; i++) {
//					spaces = spaces + " ";
//				}
//				steps = steps + spaces + number + "." + step.split("#")[1] + "\n";
//			} catch (Exception e) {
//				steps = steps + step + "\n";
//			}
//		}
//		return steps;
//	}
//
//	public static String addScreenshotToFolder(String endPoint, String body, String token, String projectId,
//			String folderPath, String ScriptId) {
//		Response response = RestAssured.given().header("Authorization", "Bearer " + token)
//				.header("projectId", projectId).contentType(ContentType.JSON).body(body).when().post(endPoint);
//		// Check if response is successful
//		if (response.getStatusCode() == 200) {
//			// Get image bytes from response
//			byte[] imageBytes = response.asByteArray();
//
//			// Define file path
//			File outputFile = new File(folderPath + File.separator + ScriptId + ".png");
//
//			// Write the image to the specified folder
//			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
//				fos.write(imageBytes);
//				// System.out.println("Screenshot saved successfully at: " +
//				// outputFile.getAbsolutePath());
//				return outputFile.getAbsolutePath(); // Return the absolute path
//			} catch (IOException e) {
//				System.err.println("Error saving screenshot: " + e.getMessage());
//			}
//		} else {
//			System.err.println("Failed to get screenshot. HTTP Status: " + response.getStatusCode());
//		}
//		return null;
//	}
//
//	public static int convertToInt(Object value) {
//		if (value instanceof Double) {
//			return ((Double) value).intValue();
//		} else if (value instanceof Integer) {
//			return (int) value;
//		} else if (value instanceof String) {
//			try {
//				return Integer.parseInt((String) value);
//			} catch (NumberFormatException e) {
//				try {
//					return (int) Double.parseDouble((String) value);
//				} catch (NumberFormatException ex) {
//					throw new IllegalArgumentException("Cannot convert to int: " + value);
//				}
//			}
//		} else {
//			throw new IllegalArgumentException("Unsupported type: " + value);
//		}
//	}
//
//	public static void getFailedIteration(List<Map<String, Object>> jsonArray) throws Exception {
//		for (Map<String, Object> map : jsonArray) {
//			// If current step is failed, break and return true
//			if (map.get("type").equals("step") && map.get("status").equals("FAIL")) {
//				failedStepId = map.get("uniqueId").toString();
//				if (scope.equals("OnlyFirstFailedStep") && !isFirstBugAdded) {
//					failedIteration = convertToInt(map.get("iteration"));
//					createIssueToMantis();
//					slNoAndRowNumber++;
//					isFirstBugAdded = true;
//					isItrCompleted = true;
//					break;
//				} else if (scope.equals("AllFailedSteps")) {
//					failedIteration = convertToInt(map.get("iteration"));
//					createIssueToMantis();
//					slNoAndRowNumber++;
//					isItrCompleted = true;
//				}
//			}
//
//			// If stepResults exist, recurse and break on failure
//			if (map.containsKey("stepResults")) {
//				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("stepResults");
//				getFailedIteration(subJsonArray);
//			}
//		}
//	}
//
//	public static Response createBug(String url, String jsonBody, Map<String, String> headers) {
//		Response response = null;
//		try {
//			response = RestAssured.given().headers(headers).body(jsonBody).when().post(url + "api/rest/issues").then()
////	                    .assertThat().statusCode(201)
//					.extract().response();
//		} catch (Exception e) {
//			System.err.println("Error while making the POST request: " + e.getMessage());
//			e.printStackTrace();
//		}
//		return response;
//	}
//
//	public static String createBody(String summary, String description, String stepsToReproduce,
//			HashMap<String, String> attachments, String additional_information) throws JSONException {
//		// Create the main JSON object
//		JSONObject jsonBody = new JSONObject();
//
//		// Add summary and description
//		jsonBody.put("summary", summary);
//		jsonBody.put("description", description);
//
//		// Create and add project ID
//		JSONObject project = new JSONObject();
//		project.put("id", Integer.parseInt(mantisProjID));
//		project.put("name", mantisProjName);
//		jsonBody.put("project", project);
//
//		// Create and add additional information
//		jsonBody.put("additional_information", additional_information);
//
//		// Create and add category
//		JSONObject category = new JSONObject();
//		category.put("name", mantisCategory);
//		jsonBody.put("category", category);
//
//		// Create and add files (attachments)
//		JSONArray files = new JSONArray();
//		for (Map.Entry<String, String> attachmentMap : attachments.entrySet()) {
//			JSONObject file = new JSONObject();
//			file.put("name", attachmentMap.getKey());
//			file.put("content", attachmentMap.getValue());
//			files.put(file);
//		}
//		jsonBody.put("files", files);
//		jsonBody.put("steps_to_reproduce", stepsToReproduce);
//		return jsonBody.toString();
//	}
//
//	public static void startHtml(String title) {
//		String logoLeft = "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6/SNLl68uNGbEm3evJkbbriB3NxctmzZwowZMxrMDjh69GiuuOIKfXGzDh48yJw5c1AUhbi4uEabAttaamoqPXv2hOp+7G3btukXgeom2oceeojPPvsMIQSjR4/2DUBbtmwZBw8e1L+kVSorK33rYLFY6gwcAygtLfU7uGqpyMhIzjzzTKj+vN98802TsxJaon///gwdOhSATZs2NXjsNKW1+6m22leOX375JV999RVXXXVVvb761n4vmjN8+HBGjBgBwLZt23zBeW1CCP7880+obhKvCdr9ZbVaWbFiBVR/32v2Z2u3X3sd5/4SQvDLL79AdathQ/sL4KKLLvJdgDz66KPs3bu3XktPDY/Hwx9//MGiRYvqZXGUWkcGAn+D2k1lNpuNgoIC/SJQHQRcfPHFHDhwgKSkJH7//XffD1GN888/n759+wKwcOFCvvzyy3o/oGVlZdx1111s3rzZ17zpdDp55JFHmhwH0K9fP7+nFQUHB2OxWHC73UyaNImTTz7Zl4743//+N++++267fHFjYmK49dZbofozzZgxo8E0t4sXL2bWrFm4XC4URaFz585cc801ABQUFDB79ux6J4vCwkLf+ramqb2hfbtlyxa/r+Za4/LLLyc+Ph6At956i7Vr19apF0LUOYn421wcHh7uu+eF2+3mqaeeqnfslJWV+U40+u3V2v1UW+0rRyEEw4cPrzNlsLbWfi+aEh4ezj333IOqqpSWljYYrOzYscM3OPbCCy/kpJNOqlNfo6Fjg+pR/suXLwfglltu8X2G1m6/I3Gc12joHij79+/nxx9/hOrZH+PHj9cvAtXTDGtaL4QQnHrqqfVaemrMmTOHcePGcf755zNgwAA2btyoX0RqKX1iAal91aTsrUnCQa30p9nZ2SI7O1usWLFCXHnllUJVVaGqqrj11ltFWVmZ/q18Vq9e7UtdC4grrrhCbNq0SWRnZ/vSynbt2tWXZGThwoW+Zc855xyRk5Pje689e/aIrl27iri4uDqZBauqqnzJYgBx8cUXi8LCQl+9EEJ89dVXIigoqM5na+hRO/2qqE7HW5NExWKxiP/97391sojl5+f7Ep4A4qGHHqqTKMfpdIobbrjBV5+cnCzef/99kZmZ6UujajQaxYwZM+r83dqZ8qjeFps2bRL5+fli4cKFIiUlxZdxDRDTpk0TaWlpvjSojamd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";
//		String logoRight = "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x9w8D9zyYfVfQTHtkja9p3a4bg+ycjLktnYY16urUke0RFIYCIiACIiACIiACIiAIW7Y7gmzWPx++/dkSkfbZcMtzru3+I6/syb7iKJrPpsStN57JW5/Uct1OfK/XwUcN+Q81cxmKtamzEWIo7gE/nSgbhgViOCzkr0WOoML7Ep23H6VPmidIVtLYhkTWh1l44pJCOZJUq6992XdPwnN85+Da6fwVTT2Kho1Iw1jB4j5uPmtqidUwdElpdgiIg9CIiACIiACIiACIiACIiACIiACIiACIiACIiACIiACIiACIiAPLmhw2cAR7rltRaBxGfjcXQiKZ3+Y3kuqRBGUVJaZ8953Qec08974o3WajTyc3qucZYjlJY8cLuhBX1M5jXt4XNDh6EbrjtRdnGFzofM2LuLR+VzDsN/oFVKpPwZmR0yE+8OzIN2MZ67tXsbFbrNaE1Dp95LYjdrjzjbtsFzsduJzywngeORYeoVEoSRi34ltX3IuTwNswujd59D6KXey3WDstROHvu/jao2Dj+pvkom59Ugu2cPk6+XpEiaB3ER5OHoVKqfF6Lun5Xoz4vwz6hVVqdPZ2tqHDw5Cq/drxs4eh81tk0dQntbCIiD0IiIAIiIAFWbU7atWWd52axvESr3kuY7QLvwOisnI12zzC4N+qCMnpbIDfM65l8hbcd+8sP2Ptxclbe6WSaOpVYZLMx4WtCx4pDDWjjjaX2JT4WDqSpi7NtBnGM/F8owOuS82NcPkCXjDlLbOdpxXk3ucvBn9n2g26brG5d2kvzDcn9q75UVUwdFCCgtIIiIJBERABERABERABERABERABERABERABERABERABERABERABERABERABERABERAHl7GyNLXtDmnqCuUz/Z/hs5GQYRDJ+5nJdaqIIyipLTIDzPZtm8MXy03fEV29G7blcq+Z8D+7uQPhd08QX1KQHDYgEHyK02W0tiMxGW2qrCSOrRsq5VJmdf0yqzvHsyF9A6q/wALZsVppd8XbO3M8o3eq+gGPbJG17CC1w3BHooez3Y250MkmOtcvKIgkrpOzy9macLsFn4JGTQD8qZx5SD0H0UoppaZfjRnUvTn3JAREUhwIiIAIiIAKK+2jJOZiaWOr7umnm2LB1I2UqLnrGl617Uzcxb2k7pgbDGejT6oIWRco6RxfZz2eGjw5bLsD7Dmju2OHyqVAAAABsAgAA2HIIhBCChHSKoiIJhERABERABERABERABERABERAH/2Q==";
//		html = new StringBuilder();
//		html.append("<!DOCTYPE html>\n<html>\n<head>\n<title>" + title + "</title>\n");
//		html.append("<style>\n");
//		html.append("body { font-family: Arial, sans-serif; text-align: center; }\n");
//		html.append(".header { display: flex; justify-content: space-between; align-items: center; padding: 10px; }\n");
//		html.append(
//				".title { font-size: 24px; font-weight: bold; background-color: none; padding: 5px; display: inline-block; }\n");
//		html.append("table { width: 50%; margin: auto; border-collapse: collapse; }\n");
//		html.append("th, td { border: 1px solid black; padding: 8px; text-align: left; }\n");
//		html.append("</style>\n</head>\n<body>\n");
//
//		// Header with Base64 images
//		html.append("<div class='header'>\n");
//		if (!logoLeft.isEmpty()) {
//			html.append("<img src='data:image/png;base64,").append(logoLeft).append("' alt='Left Logo' height='55'>\n");
//		}
//		html.append("<div class='title'>" + title + "</div>\n");
//		if (!logoRight.isEmpty()) {
//			html.append("<img src='data:image/png;base64,").append(logoRight)
//					.append("' alt='Right Logo' height='80'>\n");
//		}
//		html.append("</div>\n");
//		html.append(
//				"<table border='1'>\n<tr><th>Sl.No</th><th>Script Name</th><th>Failure Step</th><th>MantisBT Id</th></tr>\n");
//	}
//
//	public static void addLineToHTML(int slNo, String scriptName, String stepName, String mantisBT) {
////		html.append("<tr><td>").append(slNo).append("</td><td>").append(scriptName).append("</td><td>").append(stepName)
////				.append("</td><td><a href=\"http://localhost/mantisbt/view.php?id=120\" target=\"_blank\">").append(mantisBT).append("</a></td></tr>\n");
//
//		html.append("<tr><td>").append(slNo).append("</td><td>").append(scriptName).append("</td><td>").append(stepName)
//				.append("</td><td><a href=\"").append(mantisBTUrl + "view.php?id=" + mantisBT)
//				.append("\" target=\"_blank\">").append(mantisBT).append("</a></td></tr>\n");
//	}
//
//	public static void endHtml() {
//		html.append("</table>\n</body>\n</html>");
//		writeToFile(executionFolderPath + File.separator + "createdisuues.html", html.toString());
//	}
//
//	public static void writeToFile(String filename, String content) {
//		try (FileWriter writer = new FileWriter(filename)) {
//			writer.write(content);
//			System.out.println("HTML file created: " + filename);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static String createDirectory(String parentPath, String childPath) {
//		try {
//			// Combine parent and child paths
//			Path fullPath = Paths.get(parentPath, childPath);
//
//			// Create directories if they don't exist
//			Files.createDirectories(fullPath);
//
//			// Return the absolute path
//			return fullPath.toAbsolutePath().toString();
//		} catch (IOException e) {
//			System.err.println("Failed to create directory: " + parentPath + "\\" + childPath);
//			e.printStackTrace();
//			return null; // Return null in case of failure
//		}
//	}
//
//	public static String ensureTrailingSlash(String baseUri) {
//		if (baseUri == null || !baseUri.endsWith("/")) {
//			baseUri += "/";
//		}
//		return baseUri;
//	}
//
//	public static void main(String[] args) throws NlpException {
//		NlpRequestModel nlpreq = new NlpRequestModel();
//		nlpreq.getAttributes().put("Mantis URL", "http://localhost:80/mantisbt");
//		nlpreq.getAttributes().put("Mantis Auth", "hiX1cc96fBhFQM8JEoqL0_t9ODWSVMV4");
//		nlpreq.getAttributes().put("Mantis ProjId", "1");
//		nlpreq.getAttributes().put("Mantis ProjName", "Test Proj");
//		nlpreq.getAttributes().put("Mantis Category", "General");
//		nlpreq.getAttributes().put("Scope(AllFailedSteps / OnlyFirstFailedStep)", "AllFailedSteps");// AllFailedSteps,
//																									// OnlyFirstFailedStep
//		nlpreq.getAttributes().put("Fireflink Email", "channa.s@fireflink.com");
//		nlpreq.getAttributes().put("Fireflink Password", "@Channu123");
//		nlpreq.getAttributes().put("Fireflink Lic ID", "LIC16627");
//		nlpreq.getAttributes().put("Fireflink Project", "HRM_RmgYantra_BackEnd");
//		nlpreq.getAttributes().put("Fireflink Exce ID", "EXEC20293");
//		System.out.println(new CreateBugInMantisBT().execute(nlpreq));
//	}
//}
package blogic;

