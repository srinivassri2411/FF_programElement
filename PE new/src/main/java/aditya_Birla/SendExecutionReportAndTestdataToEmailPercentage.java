package aditya_Birla;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.mail.util.ByteArrayDataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
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

public class SendExecutionReportAndTestdataToEmailPercentage implements Nlp {
	@InputParams({ @InputParam(name = "Recipients List", type = "java.lang.String"),
			@InputParam(name = "Sender Email", type = "java.lang.String"),
			@InputParam(name = "Sender Password", type = "java.lang.String"),
			@InputParam(name = "Super Admin Email", type = "java.lang.String"),
			@InputParam(name = "Super Admin Password", type = "java.lang.String"),
			@InputParam(name = "File Names", type = "java.lang.String"),
			@InputParam(name = "Suite Name", type = "java.lang.String"),
			@InputParam(name = "Project Name", type = "java.lang.String"),
			@InputParam(name = "License Id", type = "java.lang.String"),
			@InputParam(name = "Pass Percentage", type = "java.lang.String") })

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
		String specifiedFilesPath = (String) programElementsInput.get("File Names");
		String currentLicenseId = (String) programElementsInput.get("License Id");
		String passPercentage = (String) programElementsInput.get("Pass Percentage");
		try {
			RestAssured.baseURI = "https://app.fireflink.com";
			String accessToken = getAccessToken(mail, pass, currentLicenseId);
			// log.info("Access Token " + accessToken);
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
			// log.info("Project ID is " + projectID);
			String suiteId = getsuiteId(accessToken, suiteName, projectID);
			String executionId = getRecentExecutionId(accessToken, suiteId, projectID);
			// log.info("Execution ID " + executionId);
			int totalCount = getNumberOfScriptsInSuit(accessToken, projectID, suiteId);
			// log.info(totalCount + " at start");
			List<String[]> result = getCurrentResult(accessToken, projectID, executionId, totalCount);
			int passCount = 0, failCount = 0, warningCount = 0;
			String statusOfResult = null;
			for (String[] array : result) {
				for (int i = 3; i < array.length; i += 3) {
					switch (array[i]) {
					case "PASS":
						passCount++;
						break;
					case "FAIL":
						failCount++;
						break;
					case "WARNING":
						warningCount++;
						break;
					}
				}
			}
			int total = passCount + failCount + warningCount;
			double passPercent = (passCount * 100.0) / total;
//			double failPercent = (failCount * 100.0) / total;
//			double warningPercent = (warningCount * 100.0) / total;
			System.out.println("passPercent: " + passPercent);
			if (passPercent >= Double.parseDouble(passPercentage)) {
				statusOfResult = "Passed";
			} else {
				statusOfResult = "Failed";
			}
			System.out.println("Suite Result " + result);

			// Get Execution System details
			HashMap<String, String> getSystemDetails = getSystemDetails(accessToken, projectID, executionId);
			String totalScripts = getSystemDetails.get("totalScripts");
			String totalPassed = getSystemDetails.get("totalPassed");
			String totalFailed = getSystemDetails.get("totalFailed");
			String totalSkipped = getSystemDetails.get("totalSkipped");
			String totalWarning = getSystemDetails.get("totalWarning");
			String pdfFile = create(result, executionId, suiteName, projectName, statusOfResult,
					getSystemDetails.get("executedOn"), getSystemDetails.get("executionRunType"),
					getSystemDetails.get("executionDuration"), getSystemDetails.get("machine"),
					getSystemDetails.get("browser"), getSystemDetails.get("os"));

			Properties props = generateProperty();

			// Create a session with authentication
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(senderEmail, senderPassword);
				}
			});

			// Create a message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(senderEmail));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			msg.setSubject(suiteName + " : Execution Completed Notification - Sequential execution");
			// Create the multipart message
			Multipart multipart = new MimeMultipart();
			// body
			String mailBody = "Dear Fireflink User,\r\n\r\n"
					+ "Please find the attached PDF report along with Test data files used in the suite execution.\r\n"
					+ "\r\n" + "Project Name: " + projectName + "\r\n" + "Suite Name: " + suiteName + "\r\n"
					+ "Execution Status: " + totalPassed + " scripts is passed out of " + totalScripts + " scripts"
					+ "\r\n" + "Number of scripts in pass status: " + totalPassed + "\r\n"
					+ "Number of scripts in skipped status: " + totalSkipped + "\r\n"
					+ "Number of scripts in warning status: " + totalWarning + "\r\n" + "Executed On: "
					+ getSystemDetails.get("executedOn") + "\r\n" + "Execution Duration: "
					+ getSystemDetails.get("executionDuration") + "\r\n" + "Execution ID: " + executionId + "\r\n";

			// Get File Ids From Test Data Folders
			List<String> filePaths = new LinkedList<String>();
			if (!specifiedFilesPath.isEmpty()) {
				for (int i = 0; i < specifiedFilesPath.split(",").length; i++) {
					String path = "";
					for (int j = 2; j < specifiedFilesPath.split(",")[i].split("/").length; j++) {
						path = path + "/" + specifiedFilesPath.split(",")[i].split("/")[j].trim();
					}
					filePaths.add(path.substring(1, path.length()));
				}
			}

			// Add test data
			Response responseFileIds = getRequest("alltrees/optimize/v1/trees/folders", accessToken, projectID);
			Map<String, String> map = new LinkedHashMap<String, String>();
			ObjectMapper objectMapper1 = new ObjectMapper();
			JsonNode root1 = objectMapper1.readTree(responseFileIds.asString());
			extractKeyValue(root1, "", map);
			Map<String, String> fileIdAndName = getTestDataDetails(filePaths, map, responseFileIds);
			for (Entry<String, String> fileIdName : fileIdAndName.entrySet()) {
				// Download Files From Test Data Section
				Response response = getRequest("project/optimize/v1/file/download/cloud/" + fileIdName.getKey(),
						accessToken, "");
				// Attach byte array
				byte[] byteArray = response.getBody().asByteArray();
				DataSource dataSource = new ByteArrayDataSource(byteArray, "application/octet-stream");

				// Check if the file is already attached
				boolean isAlreadyAttached = false;
				for (int i = 0; i < multipart.getCount(); i++) {
					MimeBodyPart bodyPart = (MimeBodyPart) multipart.getBodyPart(i);
					if (bodyPart.getFileName().equals(fileIdName.getValue())) {
						isAlreadyAttached = true;
						break;
					}
				}
				// If the file is not already attached, attach it
				if (!isAlreadyAttached) {
					MimeBodyPart attachmentPart = new MimeBodyPart();
					attachmentPart.setDataHandler(new DataHandler(dataSource));
					attachmentPart.setFileName(fileIdName.getValue());
					multipart.addBodyPart(attachmentPart);
				}
			}

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
			System.out.println("body" + body);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(body);
			Map<String, String> map = new LinkedHashMap<String, String>();
			extractKeyValue(node, "", map);
			System.out.println("Result Map" + map);
			int slno = 1;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				System.out.println(key + "-->" + entry.getValue());
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

	public static String getAccessToken(String email, String password, String currentLicenseId) throws Exception {
		password = encryptPassword(password);
		String body = given().header("Content-Type", "text/plain").header("Accept", "*/*")
				.header("Connection", "keep-alive").header("Accept-Encoding", "gzip, deflate, br")
				.header("Content-Type", "application/json")
				.body("" + "{\n" + "    \"currentLicenseId\": \"" + currentLicenseId + "\",\n" + "    \"emailId\": \""
						+ email + "\",\n" + "    \"password\": \"" + password + "\",\n"
						+ "    \"lastSessionData\": \"/signin\"\n" + "}")
				.when().post("appmanagement/optimize/v1/public/user/client-signin").then().extract().response()
				.asString();
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
			System.out.println("suiteName" + name);
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

	public static String create(List<String[]> data, String exeID, String suiteName, String projectName, String status,
			String executedOn, String executionType, String totalDuration, String machine, String browser, String os)
			throws DocumentException, IOException {
		Document document = new Document();
		String file = System.getProperty("user.home") + File.separator + "ExecutionReport.pdf";
		FileOutputStream output = new FileOutputStream(file);
		PdfWriter.getInstance(document, output);
		document.open();

		// Logo + Title in a horizontal table
		PdfPTable logoTable = new PdfPTable(2);
		logoTable.setWidthPercentage(100);
		logoTable.setWidths(new float[] { 2, 5 });

		Image logo = getLogoFromBase64(
				"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");
		logo.scaleToFit(150, 150);
		PdfPCell logoCell = new PdfPCell(logo, false);
		logoCell.setBorder(Rectangle.NO_BORDER);
		logoCell.setRowspan(2);
		logoCell.setVerticalAlignment(Element.ALIGN_TOP);

		Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
		PdfPCell titleCell = new PdfPCell(new Phrase("Execution Report", titleFont));
		titleCell.setBorder(Rectangle.NO_BORDER);
		titleCell.setVerticalAlignment(Element.ALIGN_RIGHT);
		titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

		logoTable.addCell(logoCell);
		logoTable.addCell(titleCell);

		document.add(logoTable);

		// Execution Details - Section Header
		Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
		Paragraph detailsHeader = new Paragraph("\n\n" + "Execution Status : " + status + "\nExecution Details :    ",
				sectionFont);
		detailsHeader.setSpacingAfter(10f);
		document.add(detailsHeader);

		// Summary Table 1
		PdfPTable summaryTable1 = new PdfPTable(4);
		summaryTable1.setWidthPercentage(100);
		summaryTable1.setSpacingAfter(5f);
		summaryTable1.addCell(getCell("Execution ID", true));
		summaryTable1.addCell(getCell("Suite Name", true));
		summaryTable1.addCell(getCell("Project Name", true));
		summaryTable1.addCell(getCell("Status", true));
		summaryTable1.addCell(getCell(exeID));
		summaryTable1.addCell(getCell(suiteName));
		summaryTable1.addCell(getCell(projectName));
		summaryTable1.addCell(getCell(status));
		document.add(summaryTable1);

		// Summary Table 2 (Updated: Removed "Run Initiated By")
		PdfPTable summaryTable2 = new PdfPTable(3);
		summaryTable2.setWidthPercentage(100);
		summaryTable2.setSpacingAfter(5f);
		summaryTable2.addCell(getCell("Executed On", true));
		summaryTable2.addCell(getCell("Execution Type", true));
		summaryTable2.addCell(getCell("Total Duration", true));
		summaryTable2.addCell(getCell(executedOn));
		summaryTable2.addCell(getCell(executionType));
		summaryTable2.addCell(getCell(totalDuration));
		document.add(summaryTable2);

		// Summary Table 3
		PdfPTable summaryTable3 = new PdfPTable(3);
		summaryTable3.setWidthPercentage(100);
		summaryTable3.setSpacingAfter(10f);
		summaryTable3.addCell(getCell("Machine", true));
		summaryTable3.addCell(getCell("Browser", true));
		summaryTable3.addCell(getCell("OS", true));
		summaryTable3.addCell(getCell(machine));
		summaryTable3.addCell(getCell(browser));
		summaryTable3.addCell(getCell(os));
		document.add(summaryTable3);

		// Scripts Table Header
		Paragraph scriptsHeader = new Paragraph("Scripts Details : ", sectionFont);
		scriptsHeader.setSpacingBefore(10f);
		scriptsHeader.setSpacingAfter(5f);
		document.add(scriptsHeader);

		// Scripts Table
		PdfPTable table = new PdfPTable(5);
		table.setWidthPercentage(100);
		table.setSpacingBefore(5f);

		table.addCell(getCell("SI No", true));
		table.addCell(getCell("Module Name", true));
		table.addCell(getCell("Script Name", true));
		table.addCell(getCell("Status", true));
		table.addCell(getCell("Duration", true));

		for (String[] row : data) {
			for (int i = 0; i < row.length; i++) {
				String cell = row[i];
				if (i == 3) { // Status column
					if (cell.equalsIgnoreCase("PASS")) {
						table.addCell(getCell(cell, BaseColor.GREEN));
					} else if (cell.equalsIgnoreCase("FAIL")) {
						table.addCell(getCell(cell, BaseColor.RED));
					} else {
						table.addCell(getCell(cell, BaseColor.ORANGE));
					}
				} else {
					table.addCell(getCell(cell));
				}
			}
		}

		document.add(table);
		document.close();
		output.close();

		System.out.println("Styled Execution Report PDF generated.");
		return file;
	}

	private static PdfPCell getCell(String text) {
		return getCell(text, false);
	}

	private static PdfPCell getCell(String text, boolean header) {
		Font font = new Font(Font.FontFamily.HELVETICA, 10, header ? Font.BOLD : Font.NORMAL);
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5f);
		return cell;
	}

	private static PdfPCell getCell(String text, BaseColor bgColor) {
		Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBackgroundColor(bgColor);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5f);
		return cell;
	}

	private static Image getLogoFromBase64(String base64) throws IOException, BadElementException {
		byte[] decodedBytes = Base64.getDecoder().decode(base64);
		Image logo = Image.getInstance(decodedBytes);
		logo.scaleToFit(60, 60); // Resize if needed
		logo.setAlignment(Element.ALIGN_LEFT);
		return logo;
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

	public HashMap<String, String> getSystemDetails(String token, String projId, String executionId) {
		HashMap<String, String> returnsysDetails = new HashMap<String, String>();
		Map<String, Object> header = new HashMap<>();
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Authorization", token);
		header.put("Connection", "keep-alive");
		header.put("projectId", projId);
		String response = given().headers(header).when()
				.get("/executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId).then()
				.assertThat().statusCode(200).extract().response().asString();
		returnsysDetails.put("executionDuration", getJsonStringValue(response,
				"$.responseObject.executionResponses[*].executionDurationInHourMinSecFormat"));
		returnsysDetails.put("executionRunType",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].executionRunType"));
		returnsysDetails.put("executedOn",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].executedOn"));
		returnsysDetails.put("os",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].platformStatistics[*].os"));
		returnsysDetails.put("browser",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].platformStatistics[*].browser"));
		returnsysDetails.put("machine",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].platformStatistics[*].machine"));
		returnsysDetails.put("scriptStats",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].scriptStats"));
		returnsysDetails.put("totalScripts",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].scriptStats.total"));
		returnsysDetails.put("totalPassed",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].scriptStats.totalPassed"));
		returnsysDetails.put("totalFailed",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].scriptStats.totalFailed"));
		returnsysDetails.put("totalSkipped",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].scriptStats.totalSkipped"));
		returnsysDetails.put("totalWarning",
				getJsonStringValue(response, "$.responseObject.executionResponses[*].scriptStats.totalWarning"));
		return returnsysDetails;
	}

	public static String getJsonStringValue(String jsonBody, String jsonPath) {
		String value = "";
		try {
			value = JsonPath.read(jsonBody, jsonPath).toString().trim();
			value = value.substring(value.indexOf("[") + 1, value.lastIndexOf("]"));
		} catch (Exception e) {
//	            e.printStackTrace();
		}
		return value;
	}

	public Map<String, String> getTestDataDetails(List<String> filePaths, Map<String, String> map,
			Response responseFileIds) {
		Map<String, String> fileIdAndName = new LinkedHashMap<String, String>();
		for (int i = 0; i < filePaths.size(); i++) {
			int folderCount = 1;
			String fileNamePath = filePaths.get(i);
			String parentId = "";
			String createdPath = "";
			for (Entry<String, String> keyValue : map.entrySet()) {
				if (keyValue.getKey().endsWith("title") && keyValue.getValue().equals("Root")) {
					parentId = getJsonValue(responseFileIds.asString(), keyValue.getKey().replace("title", "key"));
					createdPath = keyValue.getValue();
				} else if (!createdPath.equals(fileNamePath) && keyValue.getKey().endsWith("title")
						&& getJsonValue(responseFileIds.asString(), keyValue.getKey().replace("title", "parentId"))
								.equals(parentId)) {
					String folderName = filePaths.get(i).split("/")[folderCount];
					if (keyValue.getValue().equals(folderName)) {
						parentId = getJsonValue(responseFileIds.asString(), keyValue.getKey().replace("title", "key"));
						createdPath = createdPath + "/" + keyValue.getValue();
						folderCount++;
					}
					if (createdPath.equals(fileNamePath)) {
						String fileId = getJsonValue(responseFileIds.asString(),
								keyValue.getKey().replace("title", "key"));
						String name = getJsonValue(responseFileIds.asString(),
								keyValue.getKey().replace("title", "name"));
						fileIdAndName.put(fileId, name);
						break;
					}
				} else {
					continue;
				}
			}
		}
		return fileIdAndName;
	}

	public Properties generateProperty() {
		// Email properties configuration
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.starttls.enable", "true"); // Enable TLS
		props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
		props.put("mail.smtp.timeout", "10000"); // 10 seconds
		props.put("mail.smtp.writetimeout", "10000"); // 10 seconds
		return props;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel NlpRequestModel = new NlpRequestModel();
		NlpRequestModel.getAttributes().put("Sender Email", "srinivas.m.2411@gmail.com");
		NlpRequestModel.getAttributes().put("Sender Password", "zsii rrry wejr dpem");
		NlpRequestModel.getAttributes().put("Recipients List", "sachin.b@fireflink.com");
		NlpRequestModel.getAttributes().put("Super Admin Email", "srinivas.m@testyantra.com");
		NlpRequestModel.getAttributes().put("Super Admin Password", "Srini@123");
		NlpRequestModel.getAttributes().put("File Names", "");
		NlpRequestModel.getAttributes().put("Suite Name", "GitHub exe");
		NlpRequestModel.getAttributes().put("Project Name", "HRM_RmgYantra_BackEnd");
		NlpRequestModel.getAttributes().put("License Id", "LIC16627");
		NlpRequestModel.getAttributes().put("Pass Percentage", "80");
		System.out.println(new SendExecutionReportAndTestdataToEmailPercentage().execute(NlpRequestModel));
	}

}