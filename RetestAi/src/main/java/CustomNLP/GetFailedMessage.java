
package CustomNLP;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.jayway.jsonpath.JsonPath;
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

public class GetFailedMessage implements Nlp {
	private static List sourceList;
	private static List expectedBehaviourList;
	private static List actualBehaviourList;
	private static Map<String, String> variableMap;
	private static String projectId;
	private static String scriptId;
	private static String executionId;
	private static String fireflinkUsername;
	private static String fireflinkPassword;
	private static String licenseId;

	@InputParams({ @InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Variables Data", type = "java.lang.String"),
			@InputParam(name = "Iteration Count", type = "java.lang.String") })
	@ReturnType(name = "MapData", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String iterationCount = (String) programElementsInput.get("Iteration Count");
		fireflinkUsername = (String) programElementsInput.get("Username");
		fireflinkPassword = (String) programElementsInput.get("Password");
		String variableData = (String) programElementsInput.get("Variables Data");
		String returnValue = null;
		Boolean isIteration = false;
		Integer iterationValue = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (iterationCount != null && !iterationCount.equalsIgnoreCase("null")) {
				isIteration = true;
				iterationValue = Integer.parseInt(iterationCount) - 1;
			}
			Map<String, String> identifiers = nlpRequestModel.getExecutionIdentifiers();
			projectId = "PJT1002";
			executionId = "EXEC35291";
			scriptId = "SCR7951";
			licenseId = "";
//			executionId=identifiers.get("executionId");
//			projectId = identifiers.get("projectId");
//			scriptId = identifiers.get("scriptId");
//			licenseId = identifiers.get("licenseId");
//			String ffToken = getBearerTokenV3(licenseId, fireflinkUsername, fireflinkPassword);
//			storeVariableDataToList(ffToken, projectId, variableData);
			MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
					"Optimize_Dev_client");
			Query query = new Query();
			query.addCriteria(Criteria.where("scriptRunDetails.scriptId").is(scriptId));
			query.addCriteria(Criteria.where("scriptRunDetails.executionId").is(executionId));
//			List<Map<String, Object>> stepResultsList = (List<Map<String, Object>>) (List<?>) mongoTemplate.find(query,
//					Map.class, "StepResult");
			List<Document> stepResultsList = mongoTemplate.find(query, Document.class, "StepResult");
			stepResultsList.stream().forEach(value -> System.out.println(value.toJson()));
			ArrayList stepResults = new ArrayList<>();
			variableMap = new LinkedHashMap();
			sourceList = new ArrayList<>();
			expectedBehaviourList = new ArrayList<>();
			actualBehaviourList = new ArrayList<>();
//			addSourceToListByIterating(stepResultsList);

			for (Map doc : stepResultsList) {
				System.out.println("\n");
				System.out.println(doc);
				if (isIteration) {
					if (doc.containsKey("iteration") && doc.get("iteration").equals(iterationValue)
							&& doc.get("status").toString().equalsIgnoreCase("Fail")) {
						stepResults.add(doc);
					}
				} else {
					if (!doc.containsKey("iteration") && doc.get("status").toString().equalsIgnoreCase("Fail")) {
						stepResults.add(doc);
					}
				}
			}

			System.out.println("--------------------------------------------");
			System.out.println(stepResults.size());
			System.out.println("--------------------------------------------");
			System.out.println(stepResults);
			System.out.println("--------------------------------------------");
			System.out.println(actualBehaviourList);
			System.out.println("--------------------------------------------");
			System.out.println(expectedBehaviourList);
			System.out.println("--------------------------------------------");
			System.out.println(sourceList);
			// Your program element business logic starts here ...
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		System.out.println("-----------------------");
		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("MapData", returnValue);
		return nlpResponseModel;
	}

	private static String getDetailedFailureReport(List stepResults, String token) {
		try {

			StringBuilder reportBuilder = new StringBuilder();
			reportBuilder.append("❌ *Suite Execution FAILED*\n\n");
			reportBuilder.append("📋 *Failure Report*\n");
			reportBuilder.append("═══════════════════════════\n\n");

			int scriptCounter = 0;
			for (Object stepDetailNode : stepResults) {
				Map stepDetails = (Map) stepDetailNode;
				String searchScriptID = postRequest("/analytics/optimize/v3/analytics/search",
						"{\"module\":\"modulesScripts\",\"searchText\":\"" + scriptId
								+ "\",\"responseFilterRequired\":true,\"facetQueries\":[],\"collectionName\":\"module_data\"}",
						"Authorization", "Bearer " + token, "projectId", projectId);
				System.out.println(searchScriptID);
				Object scriptDetails = getJsonValue(searchScriptID,
						"responseObject.treeDataList[?(@.key=='" + scriptId + "' && @.entityType=='Script')]");
				System.out.println(scriptDetails);
				Map scriptInfo = (Map) convertToList(scriptDetails).get(0);
				scriptCounter++;

				String scriptDisplayName = (String) scriptInfo.get("name");

				String path = (String) scriptInfo.get("path");
				String module = null;
				if (path != null) {
					String[] parts = path.split("/");
					module = parts.length > 1 ? parts[parts.length - 2] : "";
				}

				String formattedDate = formatExecutedOn(LocalDateTime.now().toString());

				// Process steps to collect sources and find failures
				String failedStepMessage = "";
				String failedStepName = "";

				failedStepName = stepDetails.get("name") != null ? (String) stepDetails.get("name") : "Unknown Step";
				String source = getSourceFromList();
				if (!actualBehaviourList.isEmpty()) {
					failedStepMessage = (String) actualBehaviourList.get(actualBehaviourList.size() - 1);
				}

				System.out.println("For script " + scriptId + " - Source: " + source + ", Failed step: "
						+ failedStepName + ", Message: " + failedStepMessage);
				// Build report
				reportBuilder.append("*Failure ").append(scriptCounter).append("*\n");
				reportBuilder.append("─────────────────\n");
				reportBuilder.append("*Source:* ").append(source).append("\n");
				reportBuilder.append("*Module:* ").append(module).append("\n");
				reportBuilder.append("*Scenario:* ").append(scriptId).append(" - ").append(scriptDisplayName)
						.append("\n");
				reportBuilder.append("*Exec ID:* ").append(executionId).append("\n");
				reportBuilder.append("*Date/Time:* ").append(formattedDate).append("\n");

				// Add variables if available
				if (!variableMap.isEmpty()) {
					for (Map.Entry<String, String> var : variableMap.entrySet()) {
						reportBuilder.append("*").append(var.getKey()).append(":* ").append(var.getValue())
								.append("\n");
					}
				}

				if (!failedStepName.isEmpty()) {
					reportBuilder.append("*Failed Step:* ").append(failedStepName).append("\n");
				}

				if (!failedStepMessage.isEmpty()) {
					reportBuilder.append("*Issue:* ").append(failedStepMessage).append("\n\n");
				} else {
					reportBuilder.append("\n");
				}
			}

			return reportBuilder.toString();

		} catch (

		Exception e) {
			e.printStackTrace();
			return "*Suite Execution FAILED*\n\nError fetching details: " + e.getMessage();
		}
	}

	public static List convertToList(Object obj) {
		return (List) obj;
	}

	private static String getSourceFromList() {
		if (!sourceList.isEmpty()) {
			return (String) sourceList.get(0);
		}
		return "NA";
	}

	public static void addSourceToListByIterating(List<Map<String, Object>> stepResults) throws Exception {
		for (Map<String, Object> step : stepResults) {
			if (step.get("type").equals("step")) {
				if (step.containsKey("stepInputs")) {
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>) step.get("stepInputs");
					for (Map<String, Object> stepInputMap : stepInputs) {
						if (stepInputMap.containsKey("reference")) {
							String reference = stepInputMap.get("reference").toString();
							if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
									|| reference.equals("PROJECT_ENVIRONMENT"))
									&& (stepInputMap.get("value").toString().startsWith("URL_")
											|| stepInputMap.get("value").toString().startsWith("APP_")))
								sourceList.add(stepInputMap.get("value").toString().split("_")[1]);
						}
					}
				}
				if (step.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>) step.get("stepReturn");
					if (step.get("name").equals("Generate_Expected_Behaviour")) {
						if (stepReturn.containsKey("value"))
							expectedBehaviourList.add(stepReturn.get("value").toString());
						continue;
					}
					if (step.get("name").equals("Generate_Actual_Behaviour") && stepReturn.containsKey("value"))
						actualBehaviourList.add(stepReturn.get("value").toString());
				}
				continue;
			}
			if (step.containsKey("stepResults")) {
				List<Map<String, Object>> subStep = (List<Map<String, Object>>) step.get("stepResults");
				addSourceToListByIterating(subStep);
			}
		}
	}

	private static String formatExecutedOn(String executedOn) {
		if (executedOn == null)
			return "";
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
			SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Date date = inputFormat.parse(executedOn);
			return outputFormat.format(date);
		} catch (Exception e) {
			return executedOn;
		}
	}

	private static void storeVariableDataToList(String fftoken, String projectId, String variableData) {
		if (variableData == null || variableData.isEmpty())
			return;

		String[] variables = variableData.split(",");
		for (String variable : variables) {
			String[] parts = variable.split("##");
			if (parts.length == 2) {
				String variableType = parts[0].trim();
				String variableName = parts[1].trim();

				try {
					if (variableType.equals("GV")) {
						String response = getRequest("project/optimize/v1/variable/type/GLOBAL",
								new String[] { "Authorization", "Bearer " + fftoken, "projectId", projectId });
						List<Map<String, Object>> variablesList = (List<Map<String, Object>>) getJsonValue(response,
								"responseObject");

						for (Map<String, Object> varMap : variablesList) {
							if (variableName.equals(varMap.get("name"))) {
								variableMap.put(variableName, (String) varMap.get("value"));
								break;
							}
						}
					} else if (variableType.equals("PEV")) {
						String response = getRequest("project/optimize/v1/variable/type/PROJECT_ENVIRONMENT");
						List<Map<String, Object>> variablesList = (List<Map<String, Object>>) getJsonValue(response,
								"responseObject");

						for (Map<String, Object> varMap : variablesList) {
							if (variableName.equals(varMap.get("name"))) {
								variableMap.put(variableName, (String) varMap.get("value"));
								break;
							}
						}
					}
				} catch (Exception e) {
					System.err.println("Error fetching variable: " + variableName);
				}
			}
		}
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]);
		return ((Response) request.when().get(endpoint, new Object[0])).asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath, new com.jayway.jsonpath.Predicate[0]);
	}

	public static String encryptPasswordV3(String password) throws Exception {
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

	public String getBearerTokenV3(String licId, String email, String password) throws Exception {
		password = encryptPasswordV3(password);
		String body = "{ \"currentLicenseId\": \"" + licId + "\", \"emailId\": \"" + email + "\", \"password\": \""
				+ password + "\"}";

		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin", body,
				new String[0]);
		String token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));
		return token;
	}

	public static boolean convertToBoolean(Object obj) {
		return ((Boolean) obj).booleanValue();
	}

	public static int convertToInteger(Object obj) {
		return ((Integer) obj).intValue();
	}

	public static String convertToString(Object obj) {
		return (String) obj;
	}

	public static double convertToDouble(Object obj) {
		return ((Double) obj).doubleValue();
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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Iteration Count", "1");
		System.out.println(new GetFailedMessage().execute(model));
	}
}