package workingPackage_TataPlay;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import io.appium.java_client.android.AndroidDriver;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TriggerFailureNotificationForDP implements Nlp {
	public static String scriptName = "";
	public static String module = "";
	public static String token = "";
	public static String projectId = "";
	public static String scriptId = "";
	public static String executedDate = "";
	public static String status = "";
	public static String licenseId = "";
	public static String clientId = "";
	public static String runId = "";
	public static Map<String, String> variableMap = new LinkedHashMap<>();
	public static Map<String, String> dpVariableMap = new LinkedHashMap<>();
	public static Map<String, String> dpInputsMap = new LinkedHashMap<>();
	public static List<String> sourceList = new ArrayList<>();
	public static List<String> statusList = new ArrayList<>();
	public static List<String> expectedBehaviourList = new ArrayList<>();
	public static List<String> actualBehaviourList = new ArrayList<>();
	public static boolean exitFlagForFailedStep = false;
	public static String fieldString = "";
	public static int iteration = 1;
	public static StringBuilder fieldsKeyValue = null;
	public static String executionId = "";
	public static String variables = "";
	public static String dataProviderVariables = "";
	public static String dataProviderInputs = "";
	public static String baseUri = "";
	public static String mailSource = "";
	public static String issue = "";
	public static String failedStepId = "";
	public static String failedStepName = "";
	public static String screenShotPath = "";
	public static AndroidDriver driver = null;
	public static boolean isRequiredIteration = false;
	private static boolean checkNextFailed = false;

	@InputParams({ @InputParam(name = "Base_Url", type = "java.lang.String"),
			@InputParam(name = "Fireflink_Username", type = "java.lang.String"),
			@InputParam(name = "Fireflink_Password", type = "java.lang.String"),
			@InputParam(name = "From_MailId", type = "java.lang.String"),
			@InputParam(name = "To_MailId", type = "java.lang.String"),
			@InputParam(name = "Email_Field_String", type = "java.lang.String"),
			@InputParam(name = "Whatsapp Group", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			baseUri = (String) programElementsInput.get("Base_Url");
			String fireflinkUsername = (String) programElementsInput.get("Fireflink_Username");
			String fireflinkPassword = (String) programElementsInput.get("Fireflink_Password");
			String fromMailId = (String) programElementsInput.get("From_MailId");
			String toMailId = (String) programElementsInput.get("To_MailId");
			fieldString = (String) programElementsInput.get("Email_Field_String");
			String group = (String) programElementsInput.get("Whatsapp Group");

			// Get Execution Identifiers
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
			log.info("exeIdentifiers" + exeIdentifiers);
			executionId = exeIdentifiers.get("executionId");
			runId = exeIdentifiers.get("runId");
			projectId = exeIdentifiers.get("projectId");
			scriptId = exeIdentifiers.get("scriptId");
			licenseId = exeIdentifiers.get("licenseId");

//			executionId = "EXEC39447";
//			runId = "Run4206af30-5a21-49c4-aa20-5d3dd168104d";
//			projectId = "PJT1002";
//			scriptId = "SCR8169";
//			licenseId = "LIC4900";

			// Set BASE URI
			baseURI = baseUri;

			// Sign in API Request
			fireflinkPassword = encryptPassword(fireflinkPassword);
			String signInRequestPayload = "{ \"emailId\": \"" + fireflinkUsername + "\", \"password\": \""
					+ fireflinkPassword + "\"}";
			String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/signin",
					signInRequestPayload);

			String message = convertToString(getJsonValue(signInResponsePayload, "message"));

			log.info("signInResponsePayload" + signInResponsePayload);

			// Provide message if EmailID/Password is Invalid
			if (message != null && message.contains("Email or Password is invalid")) {
				System.err.println(message);
				throw new RuntimeException(message);
			}

			// Access Token
			token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));
			log.info("token" + token);

			String collectionName = projectId + "_step_results";
			// Connect to Mongo and get script results
			Map<String, String> initialMap = new LinkedHashMap<String, String>();
			initialMap.put("scriptId", scriptId);
			initialMap.put("executionId", executionId);
			initialMap.put("runId", runId);
			List<Map<String, Object>> scriptResults = connectAndFetchMongoDocuments(initialMap, null, null,
					collectionName);
			log.info(scriptResults.size() + "");

//			log.info("scriptResults" + convertToJson(scriptResults));
			log.info(scriptResults.get(scriptResults.size() - 1) + "");
//			tempFile = new File(System.getProperty("user.home") + "\\JJJJJ.json");
//			tempFile.createNewFile();
//			FileWriter writer = new FileWriter(tempFile);
//			writer.write(convertToJson(scriptResults) + "-----------------------------------------------------");
//			writer.close();

			if (scriptResults.get(scriptResults.size() - 1).get("name").toString().startsWith("iteration ")) {

				// Fetch Last Iteration Count
				iteration = convertToInteger(
						scriptResults.get(scriptResults.size() - 1).get("name").toString().split(" ")[1]);
//				List<Object> ignoredParentId = getDocumentIdsForIterationVideos(scriptId, runId, executionId,
//						iteration + "", collectionName);
//				LinkedHashMap<String, List<Object>> excludeMap = new LinkedHashMap<String, List<Object>>();
//				excludeMap.put("parentId", ignoredParentId);
				String parentID = scriptResults.get(scriptResults.size() - 1).get("_id").toString();
//				initialMap.put("parentId", parentID);
				initialMap.put("searchKey", parentID);
				List<Map<String, Object>> currentItterationScriptResults = connectAndFetchMongoDocuments(initialMap,
						null, null, collectionName);

				log.info("iteration" + iteration);
				// Separate Variable types from Email Field String
				if (!fieldString.isEmpty() && !fieldString.equals("null") && !fieldString.equals("---")) {
					for (int i = 0; i < fieldString.split(",").length; i++) {
						String keyValue = fieldString.split(",")[i].trim();
						if (keyValue.startsWith("GV") || keyValue.startsWith("PEV")) {
							variables = variables + keyValue + ",";
						} else if (keyValue.startsWith("DV")) {
							dataProviderVariables = dataProviderVariables + keyValue + ",";
						} else if (keyValue.startsWith("DI")) {
							dataProviderInputs = dataProviderInputs + keyValue + ",";
						}
					}
				}

				// Remove Last comma
				if (!variables.isEmpty()) {
					variables = variables.substring(0, variables.length() - 1);
				}
				if (!dataProviderVariables.isEmpty()) {
					dataProviderVariables = dataProviderVariables.substring(0, dataProviderVariables.length() - 1);
				}
				if (!dataProviderInputs.isEmpty()) {
					dataProviderInputs = dataProviderInputs.substring(0, dataProviderInputs.length() - 1);
				}

//				FileWriter writer = new FileWriter(new File("C:\\Users\\praveenj889\\Desktop\\New Text Document.txt"));
//				writer.write(convertToJson(currentItterationScriptResults)
//						+ "-----------------------------------------------------");
//				writer.close();
				// Fetch behavior and source until failure in previous data provider
				addSourceAndBehaviourToListByIterating(currentItterationScriptResults);

				log.info("failedStepId ->" + failedStepId);
				if (!failedStepId.isEmpty()) {

					// Fetch Global or PEV Variable data
					storeVariableDataToList();

					// Get Executed By and Scripts
//					String executionDetailsResponsePayload = getRequest(
//							"dashboardexecution/optimize/v1/dashboard/project/licenseId/" + licenseId + "/projectId/"
//									+ projectId + "/executionId/" + executionId + "/manual-test-mapping",
//							"Authorization", "Bearer " + token, "projectId", projectId);

//					String executionDetailsResponsePayload = """
//														{
//							    "responseCode": 200,
//							    "responseObject": {
//							        "executionId": "EXEC9522",
//							        "licenseId": "LIC2026757",
//							        "projectId": "PJT1002",
//							        "executionStartTime": "13-05-2026 14:30",
//							        "runInitiatedBy": "Ujjwal",
//							        "totalScriptCount": 3,
//							        "autoToManualMap": {
//							            "SCR5280": "SCR5279",
//							            "SCR5276": "SCR5278",
//							            "SCR5277": null
//							        }
//							    },
//							    "filterResponse": null,
//							    "errorCode": 0,
//							    "message": "200 OK",
//							    "status": null,
//							    "responseMap": null,
//							    "nlpAdvancedSearch": null
//							}
//														""";

//					log.info("executionDetailsResponsePayload" + executionDetailsResponsePayload);
					Map<String, String> suiteCollectionMap = new LinkedHashMap<String, String>();
//					suiteCollectionMap.put("scriptId", scriptId);
					suiteCollectionMap.put("executionId", executionId);
					suiteCollectionMap.put("runId", runId);
//					suiteCollectionMap.put("entityType", "Script");
					List<Map<String, Object>> suiteMongoDetails = connectAndFetchMongoDocuments(suiteCollectionMap,
							null, null, licenseId + "_" + executionId + "_" + "entity_results");

//					String parentId = "";
					for (Map<String, Object> suiteInfo : suiteMongoDetails) {
						if (suiteInfo.get("entityType").toString().contains("Script")
								&& suiteInfo.get("originalId").toString().contains(scriptId)) {
							scriptName = suiteInfo.get("name").toString();
//							parentId = suiteInfo.get("parentId").toString();
						}
						if (suiteInfo.get("entityType").toString().contains("Module")) {
							module = module + "/" + suiteInfo.get("name").toString();
						}
					}
					log.info("module list" + module);
					// Fetch script details
					String[] moduleList = module.split("/");
					module = moduleList[moduleList.length - 1];

					// Send Email Notification
					sendEmailNotification(fromMailId, toMailId);

					// Send Whatsapp Notification
					StringBuilder finalMsg = new StringBuilder();
					finalMsg.append("*Failure occurred in iteration*").append("\n");
					finalMsg.append("─────────────────\n");
					finalMsg.append("*Source:* ").append(mailSource).append("\n");
					finalMsg.append("*Module:* ").append(module).append("\n");
					finalMsg.append("*Scenario:* ").append(scriptId).append(" - ").append(scriptName).append("\n");

					// Add variables from fieldString
					if (!fieldString.isEmpty() && !fieldString.equals("null") && !fieldString.equals("---")) {
						String[] fieldsArr = fieldString.split(",");
						for (String field : fieldsArr) {
							String[] typeName = field.split("##");
							String varType = typeName[0].trim();
							String varName = typeName[1].trim();
							System.out.println(varType);
							if (varType.equalsIgnoreCase("GV") || varType.equalsIgnoreCase("PEV")) {
								String varValue = variableMap.get(varName);
								finalMsg.append("*" + varName + ":* ").append(varValue != null ? varValue : "")
										.append("\n");
							} else if (varType.equalsIgnoreCase("DV")) {
								String varValue = dpVariableMap.get(varName);
								finalMsg.append("*" + varName + ":* ").append(varValue != null ? varValue : "")
										.append("\n");
							} else if (varType.equalsIgnoreCase("DI")) {
								String varValue = dpInputsMap.get(varName);
								finalMsg.append("*" + varName + ":* ").append(varValue != null ? varValue : "")
										.append("\n");
							}
						}
					}

					finalMsg.append("*Exec ID:* ").append(executionId).append("\n");
					finalMsg.append("*Failed Step:* ").append(failedStepName).append("\n");
					finalMsg.append("*Issue*: ").append(issue).append("\n");
					finalMsg.append("*Iteration count:* ").append(iteration).append("\n");
					sendMessage(finalMsg.toString(), group);
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Failure Notifiaction is triggered.");
					log.info("Completed");
				} else {
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Failure Notification is not triggered.");
				}
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Failure Notification is not triggered itteration not found");
			}
			log.info("Completed");

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to trigger failure notification. " + writer.toString());
		}
		log.info("variableMap" + variableMap);
		log.info("dpVariableMap" + dpVariableMap);
		log.info("dpInputsMap" + dpInputsMap);
		log.info("dataProviderVariables" + dataProviderVariables);
		log.info("dataProviderInputs" + dataProviderInputs);
		clearGlobals();
//		ArrayList temp = new ArrayList<>();
//		temp.add(tempFile.getAbsolutePath());
//		nlpResponseModel.setOutputLocalFilePaths(temp);
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
		RequestSpecification request = given();
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

	public static void addSourceAndBehaviourToListByIterating(List<Map<String, Object>> stepResults) throws Exception {
		int i = 1;
		for (Map<String, Object> step : stepResults) {
//			log.info(i++ + "count");
//			log.info(step.get("name") + "----" + step.containsKey("iteration") + "------" + step.get("status") + "--"
//					+ step);
			if (step.get("container").equals(false)) {
				status = step.get("status").toString();
				log.info(status);
				if (status.equals("FAIL") && !checkNextFailed) {
					log.info("Fail condition");
					failedStepId = step.get("uniqueId").toString();
					failedStepName = step.get("name").toString();
					boolean isScreenShotApplicable = convertToBoolean(step.get("isScreenshotNotApplicable"));
					if (isScreenShotApplicable) {
						screenShotPath = step.get("screenshotPath").toString();
					}
					log.info("Entire Failed step details" + step);
					checkNextFailed = true;
				} else if (status.equals("FAIL") && checkNextFailed) {
					findNextFailActualAndExpected(step);
					break;
				}
			}

			if (step.containsKey("uniqueId") && step.get("container").equals(false)) {
				if (step.containsKey("stepInputs")) {
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>) step.get("stepInputs");
					for (Map<String, Object> input : stepInputs) {

						String reference = (String) input.get("reference");
						if (input.containsKey("reference")) {
							if (reference.equals("LOCAL") || reference.equals("GLOBAL")
									|| reference.equals("PROJECT_ENVIRONMENT")) {
								String varName = (String) input.get("value");
								String varValue = (String) input.get("actualValue");
								if (varName != null && (varName.startsWith("URL_") || varName.startsWith("APP_"))) {
									sourceList.add(varName.split("_")[1]);
								}
								if (varName != null && varValue != null) {
									dpVariableMap.put(varName, varValue);
								}
							} else if (reference.equals("DATAPROVIDER")) {
								String varName = (String) input.get("value");
								String varValue = (String) input.get("actualValue");
								if (varName != null && varValue != null) {
									dpInputsMap.put(varName.split(":")[1], varValue);
								}
							}

						}
					}

					// Dp variables
					for (int l = 0; l < (dataProviderVariables.split(",")).length; l++) {
						for (Map<String, Object> stepInputMap : stepInputs) {
							log.info("StepInputs  " + stepInputMap);
							if (stepInputMap.containsKey("reference")) {
								String reference = stepInputMap.get("reference").toString();
								if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
										|| reference.equals("PROJECT_ENVIRONMENT") || reference.equals("DATAPROVIDER"))
										&& stepInputMap.get("value").equals(dataProviderVariables.split(",")[l].trim())
										&& stepInputMap.containsKey("actualValue")) {
									dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
											stepInputMap.get("actualValue").toString());
								} else if (reference.equals("STEPGROUPINPUT")
										&& stepInputMap.containsKey("inputReference")) {
									// String inputReference = stepInputMap.get("inputReference").toString();
									if (stepInputMap.get("inputReference").toString().contains("DATAPROVIDER")
											&& stepInputMap.get("inputName").toString().split(":")[1]
													.equals(dataProviderVariables.split(",")[l].trim())
											&& stepInputMap.containsKey("actualValue")) {
										dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
												stepInputMap.get("actualValue").toString());
									} else if ((stepInputMap.get("inputReference").equals("LOCAL")
											|| stepInputMap.get("inputReference").equals("GLOBAL")
											|| stepInputMap.get("inputReference").equals("PROJECT_ENVIRONMENT"))
											&& stepInputMap.get("value")
													.equals(dataProviderVariables.split(",")[l].trim())
											&& stepInputMap.containsKey("actualValue")) {
										dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
												stepInputMap.get("actualValue").toString());
									}
								}
								// else if ((reference.equals("STEPGROUPINPUT"))
								// &&
								// stepInputMap.get("inputName").toString().split(":")[1].equals(dataProviderVariables.split(",")[l].trim())
								// && stepInputMap.containsKey("actualValue")) {
								// dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
								// stepInputMap.get("actualValue").toString());
								// }
							}
						}
					}

					// for dp inputs
					for (int l = 0; l < (dataProviderInputs.split(",")).length; l++) {
						for (Map<String, Object> stepInputMap : stepInputs) {
							log.info("StepInputs  " + stepInputMap);
							if (stepInputMap.containsKey("reference")) {
								String reference = stepInputMap.get("reference").toString();
								if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
										|| reference.equals("PROJECT_ENVIRONMENT") || reference.equals("DATAPROVIDER"))
										&& stepInputMap.get("value").equals(dataProviderInputs.split(",")[l].trim())
										&& stepInputMap.containsKey("actualValue")) {
									dpInputsMap.put(dataProviderInputs.split(",")[l].trim(),
											stepInputMap.get("actualValue").toString());
								} else if (reference.equals("STEPGROUPINPUT")
										&& stepInputMap.containsKey("inputReference")) {
									// String inputReference = stepInputMap.get("inputReference").toString();
									if (stepInputMap.get("inputReference").toString().contains("DATAPROVIDER")
											&& stepInputMap.get("inputName").toString().split(":")[1]
													.equals(dataProviderInputs.split(",")[l].trim())
											&& stepInputMap.containsKey("actualValue")) {
										dpInputsMap.put(dataProviderInputs.split(",")[l].trim(),
												stepInputMap.get("actualValue").toString());
									} else if ((stepInputMap.get("inputReference").equals("LOCAL")
											|| stepInputMap.get("inputReference").equals("GLOBAL")
											|| stepInputMap.get("inputReference").equals("PROJECT_ENVIRONMENT"))
											&& stepInputMap.get("value").equals(dataProviderInputs.split(",")[l].trim())
											&& stepInputMap.containsKey("actualValue")) {
										dpInputsMap.put(dataProviderInputs.split(",")[l].trim(),
												stepInputMap.get("actualValue").toString());
									}
								}
								// else if ((reference.equals("STEPGROUPINPUT"))
								// &&
								// stepInputMap.get("inputName").toString().split(":")[1].equals(dataProviderVariables.split(",")[l].trim())
								// && stepInputMap.containsKey("actualValue")) {
								// dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
								// stepInputMap.get("actualValue").toString());
								// }
							}
						}
					}
				}

			}

			if (step.containsKey("stepReturn")) {
				Map<String, Object> stepReturn = (Map<String, Object>) step.get("stepReturn");
				for (int l = 0; l < (dataProviderVariables.split(",")).length; l++) {
					log.info("stepReturn  " + stepReturn);
					String reference = stepReturn.get("type").toString();
					String dpData = dataProviderVariables.split(",")[l].trim().split("##")[1];
					System.out.println(dpData);
					if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
							|| reference.equals("PROJECT_ENVIRONMENT") || reference.equals("DATAPROVIDER"))
							&& dpData.toString().contains(stepReturn.get("name").toString())
							&& stepReturn.containsKey("value")) {
						dpVariableMap.put(dpData, stepReturn.get("value").toString());

					}
				}
				if (step.get("name").equals("Generate_Expected_Behaviour")) {
					if (stepReturn.containsKey("value"))
						expectedBehaviourList.add(stepReturn.get("value").toString());
					continue;
				}
				if (step.get("name").equals("Generate_Actual_Behaviour") && stepReturn.containsKey("value"))
					actualBehaviourList.add(stepReturn.get("value").toString());
			}
		}
	}

	private static void findNextFailActualAndExpected(Map<String, Object> step) {
		if (step.containsKey("stepReturn")) {
			Map<String, Object> stepReturn = (Map<String, Object>) step.get("stepReturn");
			if (step.get("name").equals("Generate_Expected_Behaviour")) {
				if (stepReturn.containsKey("value"))
					expectedBehaviourList.add(stepReturn.get("value").toString());
			}
			if (step.get("name").equals("Generate_Actual_Behaviour") && stepReturn.containsKey("value"))
				actualBehaviourList.add(stepReturn.get("value").toString());
		}

	}

	public static void storeVariableDataToList() {
		if (!variables.isEmpty()) {
			for (int i = 0; i < (variables.split(",")).length; i++) {
				String variableType = variables.split(",")[i].split("##")[0].trim();
				String variableName = variables.split(",")[i].split("##")[1].trim();
				String variableDetailResponse = "";
				boolean flag = false;
				if (variableType.equals("GV")) {
					variableDetailResponse = getRequest("project/optimize/v1/variable/type/GLOBAL",
							new String[] { "Authorization", "Bearer " + token, "projectId", projectId });
					List<Map> variables = (List<Map>) getJsonValue(variableDetailResponse, "responseObject");
					for (Map varMap : variables) {
						if (varMap.get("name").equals(variableName)) {
							String variableValue = (String) varMap.get("value");
							variableMap.put(variableName, variableValue);
							flag = true;
							break;
						}
					}
					if (!flag)
						System.err.println(
								"Variable: " + variableName + " Not Available in " + variableType + " Variables");
				} else if (variableType.equals("PEV")) {
					variableDetailResponse = getRequest("project/optimize/v1/variable/type/PROJECT_ENVIRONMENT",
							new String[] { "Authorization", "Bearer " + token, "projectId", projectId });
					List<Map> variables = (List<Map>) getJsonValue(variableDetailResponse, "responseObject");
					for (Map varMap : variables) {
						if (varMap.get("name").equals(variableName)) {
							String variableValue = (String) varMap.get("value");
							variableMap.put(variableName, variableValue);
							flag = true;
							break;
						}
					}
					if (!flag)
						System.err.println(
								"Variable: " + variableName + " Not Available in " + variableType + " Variables");
				} else {
					System.err.println("Invalid Variable Type");
				}
			}
		}
	}

	private static void sendEmailNotification(String fromMailId, String toMailId) throws Exception {
		baseURI = baseUri;
		// Get source
		mailSource = "";
		if (!sourceList.isEmpty()) {
			mailSource = sourceList.get(0);
		}

		// Get issue from actual behaviour if available
		issue = "";
		if (!actualBehaviourList.isEmpty()) {
			issue = actualBehaviourList.get(actualBehaviourList.size() - 1);
		}

		byte[] imageBytes = null;

		if (!screenShotPath.isEmpty()) {
			String requestBody = "{\"bucketName\":\"fireflink-demo\",\"filePath\":[\"" + screenShotPath
					+ "\"],\"type\":\"cloudS3\",\"contentType\":\"screenshot\"}";
			Response response = RestAssured.given().header("Authorization", "Bearer " + token)
					.header("projectId", projectId).contentType(ContentType.JSON).body(requestBody).when()
					.post("dashboardexecution/optimize/v1/dashboard/download/storage");
			// Check if response is successful
			if (response.getStatusCode() == 200) {
				// Get image bytes from response
				imageBytes = response.asByteArray();
			}
		}

		String subject = "Product issue : " + mailSource + " | " + module + " | " + failedStepName;
		fieldsKeyValue = new StringBuilder();

		// Build email body – only the required fields
		fieldsKeyValue.append("Source").append("=").append(mailSource).append(",");
		fieldsKeyValue.append("Module").append("=").append(module).append(",");
		fieldsKeyValue.append("Scenario").append("=").append(scriptName).append(",");
		fieldsKeyValue.append("Exec ID").append("=").append(executionId).append(",");
		// fieldsKeyValue.append("Date and Time of
		// exe").append("=").append(executedDate.replace("T", " ")).append(",");

		// Add variables from fieldString
		if (fieldString != "null" && !fieldString.isEmpty() && !fieldString.equals("---")) {
			String[] fieldsArr = fieldString.split(",");
			for (String field : fieldsArr) {
				String[] typeName = field.split("##");
				String varType = typeName[0].trim();
				String varName = typeName[1].trim();

				if (varType.equalsIgnoreCase("GV") || varType.equalsIgnoreCase("PEV")) {
					String varValue = variableMap.get(varName);
					fieldsKeyValue.append(varName).append("=").append(varValue != null ? varValue : "").append(",");
				} else if (varType.equalsIgnoreCase("DV")) {
					String varValue = dpVariableMap.get(varName);
					fieldsKeyValue.append(varName).append("=").append(varValue != null ? varValue : "").append(",");
				} else if (varType.equalsIgnoreCase("DI")) {
					String varValue = dpInputsMap.get(varName);
					fieldsKeyValue.append(varName).append("=").append(varValue != null ? varValue : "").append(",");
				}
			}
		}

		fieldsKeyValue.append("Issue").append("=").append(issue).append(",");
		fieldsKeyValue.append("Failed Step").append("=").append(failedStepName).append(",");

		sendMail(fromMailId, toMailId, subject, fieldsKeyValue.toString(), imageBytes);
	}

	private static void sendMail(String from, String to, String subject, String fieldsString, byte[] screenshotBytes) {
		String relayHost = "10.15.0.35"; // Your SMTP relay host
		Properties props = new Properties();
		props.put("mail.smtp.host", relayHost);
		props.put("mail.smtp.port", "25");
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.from", from);

		Session session = Session.getInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);

			// Build HTML table
			StringBuilder table = new StringBuilder();
			table.append("<p>Kindly check below issue and provide RCA</p>");
			table.append(
					"<table border='1' cellspacing='0' cellpadding='5' style='border-collapse: collapse; font-family: Arial, sans-serif;'>");

			String[] fieldsArr = fieldsString.split(",");
			for (String field : fieldsArr) {
				String[] keyValue = field.split("=", 2);
				String key = keyValue[0].trim();
				String value = keyValue.length > 1 ? keyValue[1].trim() : "";
				table.append("<tr><td><b>").append(key).append("</b></td><td>").append(value).append("</td></tr>");
			}
			table.append("</table>");

			String htmlBody = table.toString();

			// Create message parts
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(htmlBody, "text/html");

			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(htmlPart);

			// Attach from byte[]
			if (screenshotBytes != null && screenshotBytes.length > 0) {

				MimeBodyPart attachmentPart = new MimeBodyPart();

				ByteArrayDataSource dataSource = new ByteArrayDataSource(screenshotBytes, "image/png");

				attachmentPart.setDataHandler(new DataHandler(dataSource));
				attachmentPart.setFileName("screenshot.png");

				multipart.addBodyPart(attachmentPart);
			}

			message.setContent(multipart);
			Transport.send(message);
			log.info("Email sent successfully to: " + to);

		} catch (Exception e) {
			System.err.println("Failed to send email: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static String convertToJson(List<Map<String, Object>> data) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			// Optional: pretty print (readable JSON)
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			return mapper.writeValueAsString(data);

		} catch (Exception e) {
			throw new RuntimeException("Error converting to JSON", e);
		}
	}

	private static void sendMessage(String message, String groupName) throws Exception {
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability("appium:automationName", "uiAutomator2");
		cap.setCapability("appium:platformName", "android");

		driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), cap);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

		clearRecents();

		driver.executeScript("mobile: shell",
				Map.of("command", "am", "args", List.of("start", "-n", "com.whatsapp/.Main")));
		driver.findElement(By.xpath("//*[contains(@text,'Search')]")).click();
		driver.findElement(By.id("com.whatsapp:id/search_input")).sendKeys(groupName);
		Thread.sleep(1500);
		if (!driver.findElement(By.xpath("//android.widget.TextView[@text='" + groupName + "']")).isDisplayed()) {
			throw new NotFoundException(groupName + " not found");
		}
		driver.findElement(By.xpath("//android.widget.TextView[@text='" + groupName + "']")).click();
		driver.findElement(By.id("com.whatsapp:id/entry")).sendKeys(message);
		driver.findElement(By.id("com.whatsapp:id/send")).click();
		driver.executeScript("mobile: shell", Map.of("command", "am", "args", List.of("force-stop", "com.whatsapp")));

		clearRecents();
		driver.quit();
	}

	private static void clearRecents() throws Exception {
		openRecentsPage();
		try {
			driver.findElement(By.xpath("//android.widget.Button[contains(@text,'Close')]")).click();
		} catch (Exception ignored) {
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		int maxSwipes = 10;
		for (int i = 0; i < maxSwipes; i++) {
			if (!driver.findElements(By.xpath("//android.widget.TextView[contains(@text,'recent')]")).isEmpty()) {
				log.info("Recents cleared");
				driver.executeScript("mobile: shell", Map.of("command", "input", "args", List.of("keyevent", "3")));
				break;
			}
			swipeUp();
			Thread.sleep(200);
			openRecentsPage();
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

	}

	private static void openRecentsPage() throws Exception {
		driver.executeScript("mobile: shell", Map.of("command", "input", "args", List.of("keyevent", "3")));
		Thread.sleep(500);
		driver.executeScript("mobile: shell", Map.of("command", "input", "args", List.of("keyevent", "187")));
		Thread.sleep(1000);
	}

	private static void swipeUp() {

		Dimension size = driver.manage().window().getSize();

		int startX = size.width / 2;
		int startY = (int) (size.height * 0.7);
		int endY = (int) (size.height * 0.2);

		PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
		Sequence swipe = new Sequence(finger, 1);

		swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));

		swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

		swipe.addAction(finger.createPointerMove(Duration.ofMillis(100), PointerInput.Origin.viewport(), startX, endY));

		swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

		driver.perform(Collections.singletonList(swipe));
	}

	public static void clearGlobals() {

		scriptName = "";
		module = "";
		token = "";
		projectId = "";
		scriptId = "";
		executedDate = "";
		status = "";
		licenseId = "";
		clientId = "";
		runId = "";
		fieldString = "";
		executionId = "";
		variables = "";
		dataProviderVariables = "";
		dataProviderInputs = "";
		baseUri = "";
		mailSource = "";
		issue = "";
		failedStepId = "";
		failedStepName = "";
		screenShotPath = "";

		iteration = 1;

		exitFlagForFailedStep = false;
		isRequiredIteration = false;
		checkNextFailed = false;

		// Clear collections safely
		if (variableMap != null) {
			variableMap.clear();
		}

		if (dpVariableMap != null) {
			dpVariableMap.clear();
		}

		if (dpInputsMap != null) {
			dpInputsMap.clear();
		}

		if (sourceList != null) {
			sourceList.clear();
		}

		if (statusList != null) {
			statusList.clear();
		}

		if (expectedBehaviourList != null) {
			expectedBehaviourList.clear();
		}

		if (actualBehaviourList != null) {
			actualBehaviourList.clear();
		}

		// Reset objects
		fieldsKeyValue = null;

		// Safely close driver
		try {

			if (driver != null) {

				driver.quit();
				driver = null;
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static List<Map<String, Object>> connectAndFetchMongoDocuments(Map<String, String> queryMap,
			Map<String, String> excludeMap, Map<String, List<Object>> notInMap, String collectionName) {

		MongoClient mongoClient = null;

		try {

			mongoClient = MongoClients.create("mongodb://localhost:27017");

			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "Optimize_Dev_client");

			Query query = new Query();

			// Normal conditions
			if (queryMap != null) {

				for (Map.Entry<String, String> entry : queryMap.entrySet()) {

					String key = entry.getKey();
					String value = entry.getValue();

					if ("NOT_NULL".equals(value)) {

						query.addCriteria(Criteria.where(key).exists(true).ne(null));

					} else {

						// Partial / contains search
						query.addCriteria(Criteria.where(key).regex(Pattern.quote(value)));
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

			System.out.println("Generated Query : " + query);

			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);

			return (List<Map<String, Object>>) (List<?>) documents;

		} finally {

			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

//	public static List<Map<String, Object>> connectAndFetchMongoDocuments(Map<String, String> queryMap,
//			Map<String, String> excludeMap, Map<String, List<Object>> notInMap, String collectionName) {
//
//		MongoClient mongoClient = null;
//
//		try {
//
//			mongoClient = MongoClients.create("mongodb://localhost:27017");
//
//			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "Optimize_Dev_client");
//
//			Query query = new Query();
//
//			// Normal equality conditions
//			if (queryMap != null) {
//
//				for (Map.Entry<String, String> entry : queryMap.entrySet()) {
//
//					String key = entry.getKey();
//					String value = entry.getValue();
//
//					if ("NOT_NULL".equals(value)) {
//
//						query.addCriteria(Criteria.where(key).exists(true).ne(null));
//
//					} else {
//
//						query.addCriteria(Criteria.where(key).is(value));
//					}
//				}
//			}
//
//			// Exclude conditions
//			if (excludeMap != null) {
//
//				for (Map.Entry<String, String> entry : excludeMap.entrySet()) {
//
//					String key = entry.getKey();
//					String value = entry.getValue();
//
//					if (value == null) {
//
//						query.addCriteria(Criteria.where(key).exists(true).ne(null));
//
//					} else {
//
//						query.addCriteria(Criteria.where(key).ne(value));
//					}
//				}
//			}
//
//			// NOT IN conditions
//			if (notInMap != null) {
//
//				for (Map.Entry<String, List<Object>> entry : notInMap.entrySet()) {
//
//					String key = entry.getKey();
//					List<Object> values = entry.getValue();
//
//					if (values != null && !values.isEmpty()) {
//
//						query.addCriteria(Criteria.where(key).nin(values));
//					}
//				}
//			}
//
//			System.out.println("Generated Query : " + query);
//
//			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);
//
//			return (List<Map<String, Object>>) (List<?>) documents;
//
//		} finally {
//
//			if (mongoClient != null) {
//				mongoClient.close();
//			}
//		}
//	}

	public static List<Object> getDocumentIdsForIterationVideos(String scriptId, String runId, String executionId,
			String iterationValue, String collectionName) {

		MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
				"Optimize_Dev_client");

		Query query = new Query();

		// Mandatory filters
		query.addCriteria(Criteria.where("scriptId").is(scriptId));
		query.addCriteria(Criteria.where("runId").is(runId));
		query.addCriteria(Criteria.where("executionId").is(executionId));

		// name starts with iteration
		// but exclude iteration {dynamicValue}
		if (iterationValue != null && !iterationValue.isEmpty()) {

			String excludedName = "iteration " + iterationValue;

			query.addCriteria(new Criteria().andOperator(Criteria.where("name").regex("^iteration"),
					Criteria.where("name").ne(excludedName)));

		} else {

			query.addCriteria(Criteria.where("name").regex("^iteration"));
		}

		// videoRecordingKey exists and is not null
		query.addCriteria(Criteria.where("videoRecordingKey").exists(true).ne(null));

		// Fetch only _id
		query.fields().include("_id");

		System.out.println("Generated Query : " + query);

		List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);

		List<Object> ids = new ArrayList<>();

		for (Document doc : documents) {
			ids.add(doc.getObjectId("_id").toHexString());
		}

		return ids;
	}
//	public static void main(String[] args) throws NlpException, InterruptedException, IOException {
//
//		// sendMessage("http://localhost:4723/wd/hub", "hi", "Acoe_test");
//		// baseURI = "https://fireflink.tataplay.com/";
//		//
//		// String failedStepResponsePayload = getRequest(
//		// "executionresult/optimize/v1/executionResponse/step/de49d5fc-c3c3-4442-9399-eb28b3318d2b-2e8f373e-ef30-4ba8-b6cb-d0ba8cead7ae",
//		// "Authorization",
//		// "Bearer
//		// eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItbVV3bi1KY1JyWjcxdFBxLUR1VF9yQmlNZFNQbGRWVE80VUNCTE1Dbm80In0.eyJleHAiOjE3NzQ1ODE0MzgsImlhdCI6MTc3NDUwOTQzOCwianRpIjoib25ydHJvOmM1ZDJmY2M1LTAwZWEtNGU5OC04MDZlLTM0ZjdhMDJmZDFmYSIsImlzcyI6Imh0dHA6Ly8xMC4xLjE1Mi45NDo4MDgwL3JlYWxtcy9GaXJlRmxpbmsiLCJhdWQiOiJ0YXJnZXQtY2xpZW50Iiwic3ViIjoiZjo3NzBjMWQ0MS0yZTJkLTQ1MDMtYWY5Yi1jN2ViMTI0YTJhNGU6bmF2ZWVuQGZpcmVmbGluay5jb20iLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJmbGluay1zZXJ2aWNlIiwic2lkIjoiMjU0ZDZmMzYtNTRkZS00NTI1LWE2NDEtODA0ZWFlYzU1MGViIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJjdXJyZW50TGljZW5zZUlkIjoiTElDNDkwMCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY3VycmVudFByaXZpbGVnZSI6IkFkbWluIiwiZnVsbE5hbWUiOiJOYXZlZW4iLCJhY3RpdmF0aW9uU3RhdHVzIjoiQUNUSVZFIiwicHJpdmlsZWdlIjoiQWRtaW4iLCJsaWNlbnNlTmFtZSI6ImZpcmUtZmxpbmstTElDNDkwMCIsInByZWZlcnJlZF91c2VybmFtZSI6Ik5hdmVlbiIsInVzZXJOYW1lIjoibmF2ZWVuQGZpcmVmbGluay5jb20iLCJiaWxsaW5nQ3ljbGUiOiJOL0EiLCJpZCI6IlVTUjEyODc5IiwibGljZW5zZUlkIjoiTElDNDkwMCIsImVtYWlsIjoibmF2ZWVuQGZpcmVmbGluay5jb20ifQ.GGceCQte-c_egJpB3Sv9xO1lqlPSUZ1vwvDmFSmBtzmOMCZ1meX0cieFwqCZfFiwcHzixDPIMYoPrTZORId-F26kTVvuEMcUm8JG-T3Yun3W1CMo5VU2D9blRXlayfRj0ggvthSG3hAvB5vFem3sVOErOjsBxiHXThK8BWB246LXl-daCmjPVhcFvIW54e0tSXLE0vd6AVhCzwFuA7uIe1S-Mt7nIqSoT9X8OTr-7bCxn4nZAt6tm0R19HYyE-u68OnaZP3YoDcKmLoCOByBK4RzdD0NIkHr0pG0Q_zkrW3T7DhXnlCtfCWyAVyz8JsRjoehT-P7MN9OY9e-hUvcFw",
//		// "projectId", "PJT1002");
//		// log.info(failedStepResponsePayload);
//
//		NlpRequestModel model = new NlpRequestModel();
//		model.getAttributes().put("Base_Url", "https://fireflink.tataplay.com/");
//		model.getAttributes().put("Fireflink_Username", "naveen@fireflink.com");
//		model.getAttributes().put("Fireflink_Password", "Password@123");
//		model.getAttributes().put("From_MailId", "praveenj889@tataplay.com");
//		model.getAttributes().put("To_MailId", "praveenj889@tataplay.com");
//		model.getAttributes().put("Email_Field_String",
//				"DV##Prod_RMN,DV##subscriberID,DI##Missed_Call_Number,DI##Pack_Name");
//		model.getAttributes().put("Whatsapp Group", "Acoe_test");
//		log.info(new TriggerFailureNotificationForDP().execute(model) + "");
//	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Base_Url", "https://fireflink.tataplay.com/");
		model.getAttributes().put("Fireflink_Username", "naveen@fireflink.com");
		model.getAttributes().put("Fireflink_Password", "Password@123");
		model.getAttributes().put("From_MailId", "praveenj889@tataplay.com");
		model.getAttributes().put("To_MailId", "praveenj889@tataplay.com");
		model.getAttributes().put("Email_Field_String",
				"DV##Prod_RMN,DV##subscriberID,DI##Missed_Call_Number,DI##Pack_Name");
		model.getAttributes().put("Whatsapp Group", "Acoe_test");
		log.info(new TriggerFailureNotificationForDP().execute(model) + "");
	}
}
