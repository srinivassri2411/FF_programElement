
package default_package;

import static io.restassured.RestAssured.given;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.crypto.Cipher;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.jayway.jsonpath.JsonPath;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendExecutionReportAndTestdataToEmail implements Nlp {
	@InputParams({ @InputParam(name = "Recipients List", type = "java.lang.String"),
			@InputParam(name = "Sender Email", type = "java.lang.String"),
			@InputParam(name = "Sender Password", type = "java.lang.String"),
			@InputParam(name = "Super Admin Email", type = "java.lang.String"),
			@InputParam(name = "Super Admin Password", type = "java.lang.String"),
//			@InputParam(name = "File Names", type = "java.lang.String"),
			@InputParam(name = "Suite Name", type = "java.lang.String"),
			@InputParam(name = "Project Name", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String to = (String) programElementsInput.get("Recipients List");
		String mail = (String) programElementsInput.get("Super Admin Email");
		String senderEmail = (String) programElementsInput.get("Sender Email");
		String senderPassword = (String) programElementsInput.get("Sender Password");
		String pass = (String) programElementsInput.get("Super Admin Password");
		String suiteName = (String) programElementsInput.get("Suite Name");
		String projectName = (String) programElementsInput.get("Project Name");
//		String specifiedFilesPath = (String) programElementsInput.get("File Names");
//		String suiteId = null;
		try {
			RestAssured.baseURI = "https://app.fireflink.com";
			String accessToken = getAccessToken(mail, pass);
			log.info("Access Token " + accessToken);
			Response responseProjects = getRequest("/project/optimize/v1/projects/dropdown", accessToken, "");
			ObjectMapper objectMapperPJ = new ObjectMapper();
			JsonNode rootPJ = objectMapperPJ.readTree(responseProjects.asString());
			Map<String, String> tootmapPJ = new LinkedHashMap<String, String>();
			extractKeyValue(rootPJ, "", tootmapPJ);
			String typeName = null;
			String projectID = null;
			System.out.println(tootmapPJ);
			for (Map.Entry<String, String> entry : tootmapPJ.entrySet()) {
				String key = entry.getKey();
				if (key.endsWith("name") && tootmapPJ.get(key).contains(projectName)) {
					projectID = tootmapPJ.get(key.replace("name", "id"));
					typeName = tootmapPJ.get(key.replace("name", "type"));
				}
			}
			System.out.println(projectID);
			System.out.println(typeName);
			log.info("Project ID is " + projectID);
			String suiteId = getsuiteId(accessToken, suiteName, projectID);
			String executionId = getRecentExecutionId(accessToken, suiteId, projectID);
			log.info("Execution ID " + executionId);
			int totalCount = getNumberOfScriptsInSuit(accessToken, projectID, suiteId);
			log.info(totalCount + " at start");
			List<String[]> result = getCurrentResult(accessToken, projectID, executionId, totalCount);
			String statusOfResult = null;
			for (String[] row : result) {
				for (String cell : row) {
					statusOfResult = statusOfResult + cell + ",";
				}
			}
			if (statusOfResult.contains("FAIL")) {
				statusOfResult = "Failed";
			} else {
				statusOfResult = "Passed";
			}
			System.out.println("Suite Result " + result);
			String pdfFile = create(result, executionId);

//			// Get File Ids From Test Data Folders
//			List<String> filePaths = new LinkedList<String>();
//			for (int i = 0; i < specifiedFilesPath.split(",").length; i++) {
//				String path = "";
//				for (int j = 2; j < specifiedFilesPath.split(",")[i].split("/").length; j++) {
//					path = path + "/" + specifiedFilesPath.split(",")[i].split("/")[j].trim();
//				}
//				filePaths.add(path.substring(1, path.length()));
//			}
			Response responseFileIds = getRequest("alltrees/optimize/v1/trees/folders", accessToken, projectID);
			Map<String, String> map = new LinkedHashMap<String, String>();
			ObjectMapper objectMapper1 = new ObjectMapper();
			JsonNode root1 = objectMapper1.readTree(responseFileIds.asString());
			extractKeyValue(root1, "", map);

//			Map<String, String> fileIdAndName = new LinkedHashMap<String, String>();
//			for (int i = 0; i < filePaths.size(); i++) {
//				int folderCount = 1;
//				String fileNamePath = filePaths.get(i);
//				String parentId = "";
//				String createdPath = "";
//
//				for (Entry<String, String> keyValue : map.entrySet()) {
//					if (keyValue.getKey().endsWith("title") && keyValue.getValue().equals("Root")) {
//						parentId = getJsonValue(responseFileIds.asString(), keyValue.getKey().replace("title", "key"));
//						createdPath = keyValue.getValue();
//					} else if (!createdPath.equals(fileNamePath) && keyValue.getKey().endsWith("title")
//							&& getJsonValue(responseFileIds.asString(), keyValue.getKey().replace("title", "parentId"))
//									.equals(parentId)) {
//						String folderName = filePaths.get(i).split("/")[folderCount];
//						if (keyValue.getValue().equals(folderName)) {
//							parentId = getJsonValue(responseFileIds.asString(),
//									keyValue.getKey().replace("title", "key"));
//							createdPath = createdPath + "/" + keyValue.getValue();
//							folderCount++;
//						}
//						if (createdPath.equals(fileNamePath)) {
//							String fileId = getJsonValue(responseFileIds.asString(),
//									keyValue.getKey().replace("title", "key"));
//							String name = getJsonValue(responseFileIds.asString(),
//									keyValue.getKey().replace("title", "name"));
//							fileIdAndName.put(fileId, name);
//							break;
//						}
//					} else {
//						continue;
//					}
//				}
//			}

			// Sender's email address and password// replace with your ID
			final String emailUsername = senderEmail;
			final String emailPassword = senderPassword;
			// Email properties configuration
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
			properties.put("mail.imaps.host", "imap.gmail.com");
			properties.put("mail.imaps.port", "993");
			properties.put("mail.imaps.ssl.enable", "false");
			properties.put("mail.imaps.ssl.trust", "imap.gmail.com");
			properties.put("mail.imaps.ssl.checkserveridentity", "false");
			properties.put("mail.imaps.ssl.trust", "*");
//			props.put("mail.smtp.host", "smtp.office365.com"); // Outlook SMTP server address
//			props.put("mail.smtp.port", "587"); // SMTP server port for TLS
//			props.put("mail.smtp.auth", "true");
//			props.put("mail.smtp.starttls.enable", "true"); // Enable TLS

			// Create a session with authentication
			Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(emailUsername, emailPassword);
				}
			});

			// Create a message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(emailUsername));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			msg.setSubject(statusOfResult + "-" + suiteName + " on "
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-YYYY hh:mm")) + " (" + executionId
					+ ")");

			// Create the multipart message
			Multipart multipart = new MimeMultipart();

			String mailBody = "Dear Fireflink User,\r\n\r\n"
					+ "Please find the attached PDF report along with Test data files used in the suite execution.\r\n"
					+ "\r\n";
//			for (Entry<String, String> fileIdName : fileIdAndName.entrySet()) {
//				// Download Files From Test Data Section
//				Response response = getRequest("project/optimize/v1/file/download/cloud/" + fileIdName.getKey(),
//						accessToken, "");
//				// Attach byte array
//				byte[] byteArray = response.getBody().asByteArray();
//				DataSource dataSource = new ByteArrayDataSource(byteArray, "application/octet-stream");
//
//				// Check if the file is already attached
//				boolean isAlreadyAttached = false;
//				for (int i = 0; i < multipart.getCount(); i++) {
//					MimeBodyPart bodyPart = (MimeBodyPart) multipart.getBodyPart(i);
//					if (bodyPart.getFileName().equals(fileIdName.getValue())) {
//						isAlreadyAttached = true;
//						break;
//					}
//				}
//				// If the file is not already attached, attach it
//				if (!isAlreadyAttached) {
//					MimeBodyPart attachmentPart = new MimeBodyPart();
//					attachmentPart.setDataHandler(new DataHandler(dataSource));
//					attachmentPart.setFileName(fileIdName.getValue());
//					multipart.addBodyPart(attachmentPart);
//				}
//			}

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(pdfFile);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName("ExecutionReport_" + executionId + ".pdf");
			multipart.addBodyPart(messageBodyPart);
			// Text content
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(mailBody);

			// Attach text part
			multipart.addBodyPart(textPart);

			// Set content
			msg.setContent(multipart);

			// Send the message
			Transport.send(msg);

			System.out.println("Email sent successfully with attachments.");
			nlpResponseModel.setMessage("Passed");
			nlpResponseModel.setStatus(CommonConstants.pass);
//			suiteId = getTheSuiteID("", suiteName);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Fail to check " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

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

	public static String getTheSuiteID(String jsonResponse, String suiteName) {
		io.restassured.path.json.JsonPath jsonPath = new io.restassured.path.json.JsonPath(jsonResponse);

		int objectCount = jsonPath.getInt("responseObject.size()");
		int count = 0;
		String data = null;
		for (int i = 0; i < objectCount; i++) {
			String name = jsonPath.getString("responseObject[" + i + "].name");
			if (name.contains(suiteName)) {
				data = jsonPath.get("responseObject[" + count + "].id").toString();
				break;
			} else {
				count++;
			}
		}
		return data;
	}

	public static List<String[]> getCurrentResult(String token, String projId, String executionId, int totalCount)
			throws JsonMappingException, JsonProcessingException {
		String body = null;
		List<String[]> data = new ArrayList<>();
		while (data.size() < totalCount - 1) {

			Map<String, Object> header = new HashMap<>();
			header.put("Access-Control-Allow-Origin", "*");
			header.put("Authorization", token);
			header.put("Connection", "keep-alive");
			header.put("projectId", projId);
			Response response = given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId).then()
					.assertThat().statusCode(200).extract().response();
			given().headers(header).when()
					.get("/executionresult/optimize/v1/executionResponse/result/sync/" + executionId);
			body = response.getBody().asString();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(body);
			Map<String, String> map = new LinkedHashMap<String, String>();
			extractKeyValue(node, "", map);
			int slno = 1;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				if (key.endsWith("type") && map.get(key).contains("Script")) {
					String moduleName = map.get(key.replace("type", "path"))
							.substring(0, map.get(key.replace("type", "path")).lastIndexOf("/")).replaceFirst("/", "")
							.replaceAll("Root/", "");
					String scriptName = map.get(key.replace("type", "path")).substring(
							map.get(key.replace("type", "path")).lastIndexOf("/") + 1,
							map.get(key.replace("type", "path")).length());
					String status = map.get(key.replace("type", "status"));
					String exeTime = map.get(key.replace("type", "executionDurationInHourMinSecFormat"));
					data.add(new String[] { slno + "", moduleName, scriptName, status, exeTime });
					slno++;
				}
			}
			try {
				Thread.sleep(1000); // Sleep for 1 second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static String getRecentExecutionId(String token, String suiteID, String projId) {

		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
		header.put("Accept-Encoding", "gzip, deflate, br");
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Authorization", token);
		header.put("Connection", "keep-alive");
		header.put("projectId", projId);
		Response response = given().param("executionType", "SUITE").headers(header).when().get(
				"/dashboardexecution/optimize/v1/dashboard/projects/" + projId + "/" + suiteID + "?executionType=SUITE")
				.then().assertThat().statusCode(200).extract().response();
		String body = response.getBody().asString();
		io.restassured.path.json.JsonPath js = new io.restassured.path.json.JsonPath(body);
		String executionId = js.get("responseObject[0]._id");
		System.out.println(executionId);
		return executionId;
	}

	public static String getAccessToken(String email, String password) throws Exception {
		password = encryptPassword(password);
		String body = given().header("Content-Type", "text/plain").header("Accept", "*/*")
				.header("Connection", "keep-alive").header("Accept-Encoding", "gzip, deflate, br")
				.header("Content-Type", "application/json")
				.body("" + "{\n" + "    \"emailId\": \"" + email + "\",\n" + "    \"password\": \"" + password + "\",\n"
						+ "    \"lastSessionData\": \"/signin\"\n" + "}")
				.when().post("appmanagement/optimize/v1/public/user/signin").then().extract().response().asString();
		io.restassured.path.json.JsonPath js = new io.restassured.path.json.JsonPath(body);
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

		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Authorization", token);
		header.put("Connection", "keep-alive");
		header.put("projectId", projId);
		Response response = given().headers(header).when().get("/project/optimize/v1/suite/" + suitId + "").then()
				.assertThat().statusCode(200).extract().response();
		String body = response.getBody().asString();
		io.restassured.path.json.JsonPath js = new io.restassured.path.json.JsonPath(body);
		int noOfScripts = js.getInt("responseObject.noOfScripts");
		System.out.println(noOfScripts);
		System.out.println(noOfScripts);
		return noOfScripts;
	}

	public static String getsuiteId(String token, String suiteName, String projId) {
		String allSuiteResponse = given().header("Authorization", token).header("projectId", projId).when()
				.get("https://app.fireflink.com/project/optimize/v1/suite/getAll/all").then().extract().asString();
		io.restassured.path.json.JsonPath jsonPath = new io.restassured.path.json.JsonPath(allSuiteResponse);
		int objectCount = jsonPath.getInt("responseObject.size()");
		int count = 0;
		String suiteID = null;
		for (int i = 0; i < objectCount; i++) {
			String name = jsonPath.getString("responseObject[" + i + "].name");
//			System.out.println("suiteName" + name);
			if (name.contains(suiteName)) {
				suiteID = jsonPath.get("responseObject[" + count + "].id").toString();
				break;
			} else {
				count++;
			}
		}
		System.out.println(suiteID);
		return suiteID;
	}

	public static String create(List<String[]> data, String exeID) throws DocumentException, IOException {
		Document document = new Document();
		String file = null;
		file = System.getProperty("user.home") + "\\sample.pdf";
		FileOutputStream ouput = new FileOutputStream(file);
		PdfWriter.getInstance(document, ouput);
		document.open();
		document.add(new Paragraph("Scripts Details for the " + exeID));
		document.add(new Paragraph("\n"));
		PdfPTable table = new PdfPTable(5); // 3 columns
		table.setWidthPercentage(100); // Set table width to 100% of page width
		table.addCell(getCell("SL NO", BaseColor.GRAY));
		table.addCell(getCell("Module Name", BaseColor.GRAY));
		table.addCell(getCell("Script Name", BaseColor.GRAY));
		table.addCell(getCell("Status", BaseColor.GRAY));
		table.addCell(getCell("Duration", BaseColor.GRAY));
		for (String[] row : data) {
			for (String cell : row) {
				if (cell.contains("PASS")) {
					table.addCell(getCell(cell, BaseColor.WHITE, BaseColor.GREEN));
				} else if (cell.contains("FAIL")) {
					table.addCell(getCell(cell, BaseColor.WHITE, BaseColor.RED));
				} else {
					table.addCell(getCell(cell, BaseColor.WHITE, BaseColor.BLACK));
				}
			}
		}
		document.add(table);

		document.close();
		ouput.close();
		System.out.println("PDF report with colorful table generated successfully!");
		return file;
	}

	private static PdfPCell getCell(String text, BaseColor backgroundColor) {
		PdfPCell cell = new PdfPCell(new Phrase(text));
		cell.setBackgroundColor(backgroundColor);
		return cell;
	}

	private static PdfPCell getCell(String text, BaseColor backgroundColor, BaseColor textColor) {
		PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, textColor)));
		cell.setBackgroundColor(backgroundColor);
		return cell;
	}

	public static void extractKeyValue(JsonNode node, String path, List<List<String>> list) {
		if (node.isObject()) {
			node.fields().forEachRemaining(entry -> {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				String newPath = path.isEmpty() ? key : path + "." + key;
				extractKeyValue(value, newPath, list);
			});
		} else if (node.isArray()) {
			for (int i = 0; i < node.size(); i++) {
				JsonNode arrayElement = node.get(i);
				extractKeyValue(arrayElement, path + "[" + i + "]", list);
			}
		} else {
			if (path.contains("type") && node.toString().contains("Script")) {
				List<String> listForEachScript = new LinkedList<String>();
				listForEachScript.add(path.replace("type", "path"));
				listForEachScript.add(path.replace("type", "parentId"));
				listForEachScript.add(path.replace("type", "key"));
				list.add(listForEachScript);
			}
		}
	}

	public static Response postRequest(String endpoint, String payload) {
		return RestAssured.given().body(payload.replace("\\\"", "\"")).contentType(ContentType.JSON).post(endpoint);
	}

	public static Response getRequest(String endpoint, String token, String header) {
		return RestAssured.given().header("Authorization", token).header("projectId", header).get(endpoint);
	}

	public static String getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath).toString();
	}

	public static void extractKeyValue(JsonNode node, String path, Map<String, String> map) {
		if (node.isObject()) {
			node.fields().forEachRemaining(entry -> {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				String newPath = path.isEmpty() ? key : path + "." + key;
				extractKeyValue(value, newPath, map);
			});
		} else if (node.isArray()) {
			for (int i = 0; i < node.size(); i++) {
				JsonNode arrayElement = node.get(i);
				extractKeyValue(arrayElement, path + "[" + i + "]", map);
			}
		} else {
			map.put(path, node.asText());
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel NlpRequestModel = new NlpRequestModel();
		NlpRequestModel.getAttributes().put("Sender Email", "srinivas.m.2411@gmail.com");
		NlpRequestModel.getAttributes().put("Sender Password", "zsii rrry wejr dpem");
		NlpRequestModel.getAttributes().put("Recipients List", "srinivas.m@fireflink.com");
		NlpRequestModel.getAttributes().put("Super Admin Email", "srinivas.m@testyantra.com");
		NlpRequestModel.getAttributes().put("Super Admin Password", "Srini@123");
//		NlpRequestModel.getAttributes().put("File Names",
//				"PJT1004/TestData/Root/THG_Publishing_Pvt_Ltd/THG_Publishing_Pvt_Ltd_TestData_Performance_Matric.xlsx");
		NlpRequestModel.getAttributes().put("Suite Name", "GitHub exe");
		NlpRequestModel.getAttributes().put("Project Name", "HRM_RmgYantra_BackEnd");
		System.out.println(new SendExecutionReportAndTestdataToEmail().execute(NlpRequestModel));
	}

}