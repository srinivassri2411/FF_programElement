package workingPackage_TataPlay;

import static io.restassured.RestAssured.baseURI;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
public class TriggerFailureNotificationForNormalScripts implements Nlp {
	private String licenseId;
	public static String failedStepId = "";
	public static String scriptName = "";
	public static String module = "";
	public static String token = "";
	public static String projectId = "";
	public static String executionFolderPath = "";
	public static String scriptId = "";
	public static String executedDate = "";
	public static String status = "";
	public static String executedBy = "";
	public static String clientId = "";
	public static String runId = "";
	public static Map<String, String> variableMap = new LinkedHashMap<>();
	public static List<String> sourceList = new LinkedList<>();
	public static List<String> expectedBehaviourList = new ArrayList<>();
	public static List<String> actualBehaviourList = new ArrayList<>();
	public static boolean exitFlagForFailedStep = false;
	public static String fieldString = "";
	public static StringBuilder fieldsKeyValue = null;
	public static String executionId = "";
	public static String baseUri = "";
	public static String mailSource = "";
	public static String failedStepName = "";
	public static String screenShotPath = "";
	public static String issue = "";
	public static AndroidDriver driver = null;

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
			executionId = exeIdentifiers.get("executionId");
			runId = exeIdentifiers.get("runId");
			projectId = exeIdentifiers.get("projectId");
			scriptId = exeIdentifiers.get("scriptId");
			licenseId = exeIdentifiers.get("licenseId");

//			executionId = "EXEC9522";
//			runId = "Runee778251-19b1-4283-b243-78e81c8140f1";
//			projectId = "PJT1002";
//			scriptId = "SCR4946";
//			licenseId = "LIC2026757";

			// Set BASE URI
			baseURI = baseUri;

			// Sign in API Request
			fireflinkPassword = encryptPassword(fireflinkPassword);
			String signInRequestPayload = "{ \"emailId\": \"" + fireflinkUsername + "\", \"password\": \""
					+ fireflinkPassword + "\"}";
			String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/signin",
					signInRequestPayload);

			String message = convertToString(getJsonValue(signInResponsePayload, "message"));

			// Provide message if EmailID/Password is Invalid
			if (message != null && message.contains("Email or Password is invalid")) {
				System.err.println(message);
				throw new RuntimeException(message);
			}

			// Access Token
			token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

			// Connect to Mongo and get script results
			String collectionName = projectId + "_step_results";
			// Connect to Mongo and get script results
			Map<String, String> initialMap = new LinkedHashMap<String, String>();
			initialMap.put("scriptId", scriptId);
			initialMap.put("executionId", executionId);
			initialMap.put("runId", runId);
			initialMap.put("type", "step");
			List<Map<String, Object>> scriptResults = connectAndFetchMongoDocuments(initialMap, null, null,
					collectionName);
			log.info(scriptResults.size() + "");

//			log.info("scriptResults" + convertToJson(scriptResults));

			// Fetch behavior and source until failed step
			addSourceAndBehaviourToListByIterating(scriptResults);

			log.info("failedStepId ->" + failedStepId);
			if (!failedStepId.isEmpty()) {

				// Fetch given variables
				storeVariableDataToList();

				Map<String, String> suiteCollectionMap = new LinkedHashMap<String, String>();
//				suiteCollectionMap.put("scriptId", scriptId);
				suiteCollectionMap.put("executionId", executionId);
				suiteCollectionMap.put("runId", runId);
//				suiteCollectionMap.put("entityType", "Script");
				List<Map<String, Object>> suiteMongoDetails = connectAndFetchMongoDocuments(suiteCollectionMap, null,
						null, licenseId + "_" + executionId + "_" + "entity_results");

//				String parentId = "";
				for (Map<String, Object> suiteInfo : suiteMongoDetails) {
					if (suiteInfo.get("entityType").toString().contains("Script")
							&& suiteInfo.get("originalId").toString().contains(scriptId)) {
						scriptName = suiteInfo.get("name").toString();
//						parentId = suiteInfo.get("parentId").toString();
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
				finalMsg.append("*Failure occurred in script*").append("\n");
				finalMsg.append("─────────────────\n");
				finalMsg.append("*Source:* ").append(mailSource).append("\n");
				finalMsg.append("*Module:* ").append(module).append("\n");
				finalMsg.append("*Scenario:* ").append(scriptId).append(" - ").append(scriptName).append("\n");
				finalMsg.append("*Exec ID:* ").append(executionId).append("\n");

				// Add variables from fieldString
				if (!fieldString.isEmpty() && !fieldString.equals("null") && !fieldString.equals("---")) {
					String[] fieldsArr = fieldString.split(",");
					for (String field : fieldsArr) {
						String[] typeName = field.split("##");
						String varType = typeName[0].trim();
						String varName = typeName[1].trim();

						if (varType.equalsIgnoreCase("GV") || varType.equalsIgnoreCase("PEV")) {
							String varValue = variableMap.get(varName);
							finalMsg.append("*" + varName + ":* ").append(varValue != null ? varValue : "")
									.append("\n");
						}
					}
				}

				finalMsg.append("*Failed Step:* ").append(failedStepName).append("\n");
				finalMsg.append("*Issue*: ").append(issue).append("\n");

				// Send Whatsapp message
				sendMessage(finalMsg.toString(), group);

				// Set Pass Message and status
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Failure Notifiaction triggered.");
			} else {
				// Set Pass Message and status
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Failure Notification is NOT triggered.");
			}
			System.out.println("Completed");
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			// Set Fail Message and status
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to trigger failure notification. " + writer.toString());
		}
		clearGlobals();
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

	public static void storeVariableDataToList() {
		for (int i = 0; i < (fieldString.split(",")).length; i++) {
			String variableType = fieldString.split(",")[i].split("##")[0].trim();
			String variableName = fieldString.split(",")[i].split("##")[1].trim();
			String variableDetailResponse = "";
			boolean flag = false;
			System.out.println(variableType);
			System.out.println(variableName);
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
					System.err
							.println("Variable: " + variableName + " Not Available in " + variableType + " Variables");
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
					System.err
							.println("Variable: " + variableName + " Not Available in " + variableType + " Variables");
			} else {
				System.err.println("Invalid Variable Type");
			}
		}
	}

	public static void addSourceAndBehaviourToListByIterating(List<Map<String, Object>> stepResults) throws Exception {
		for (Map<String, Object> step : stepResults) {
			if (step.get("type").equals("step") && step.get("status").toString().equals("FAIL")) {
				log.info("Failed Step info " + step);
				failedStepId = step.get("uniqueId").toString();
				failedStepName = step.get("name").toString();
				boolean isScreenShotApplicable = convertToBoolean(step.get("isScreenshotNotApplicable"));
				if (isScreenShotApplicable) {
					screenShotPath = step.get("screenshotPath").toString();
				}
				break;
			}

			if (step.containsKey("uniqueId") && step.get("type").equals("step")) {
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
			}
		}
	}

	private static void sendEmailNotification(String fromMailId, String toMailId) throws Exception {
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
			String requestBody = "{\"bucketName\":\"flinko-prod\",\"filePath\":[\"" + screenShotPath
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
		if (fieldString != null && !fieldString.isEmpty()) {
			String[] fieldsArr = fieldString.split(",");
			for (String field : fieldsArr) {
				String[] typeName = field.split("##");
				String varType = typeName[0].trim();
				String varName = typeName[1].trim();

				if (varType.equalsIgnoreCase("GV") || varType.equalsIgnoreCase("PEV")) {
					String varValue = variableMap.get(varName);
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
			System.out.println("Email sent successfully to: " + to);

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
				System.out.println("Recents cleared");
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

	public static List<Map<String, Object>> connectAndFetchMongoDocuments(Map<String, String> queryMap,
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

			System.out.println("Generated Query : " + query);

			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);

			return (List<Map<String, Object>>) (List<?>) documents;

		} finally {

			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

	public static void clearGlobals() {
		failedStepId = "";
		scriptName = "";
		module = "";
		token = "";
		projectId = "";
		executionFolderPath = "";
		scriptId = "";
		executedDate = "";
		status = "";
		executedBy = "";
		clientId = "";
		runId = "";
		fieldString = "";
		executionId = "";
		baseUri = "";
		mailSource = "";
		failedStepName = "";
		screenShotPath = "";
		issue = "";

		exitFlagForFailedStep = false;

		// Clear collections
		variableMap.clear();
		sourceList.clear();
		expectedBehaviourList.clear();
		actualBehaviourList.clear();

		// Reset objects
		fieldsKeyValue = null;
		driver = null;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Base_Url", "https://v3demo.fireflink.com/");
		model.getAttributes().put("Fireflink_Username", "srinivas.m@fireflink.com");
		model.getAttributes().put("Fireflink_Password", "Qwerty@123");
		model.getAttributes().put("From_MailId", "praveenj889@tataplay.com");
		model.getAttributes().put("To_MailId", "praveenj889@tataplay.com");
		model.getAttributes().put("Email_Field_String",
				"DV##Prod_RMN,DV##subscriberID,DI##Missed_Call_Number,DI##Pack_Name");
		model.getAttributes().put("Whatsapp Group", "Acoe_test");
		log.info(new TriggerFailureNotificationForNormalScripts().execute(model) + "");
	}
}
