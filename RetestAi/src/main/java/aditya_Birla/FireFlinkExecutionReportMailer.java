
package aditya_Birla;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

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
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class FireFlinkExecutionReportMailer implements Nlp {
	private static final String BASE_URL = "https://sg-app.fireflink.com";

	@InputParams({ @InputParam(name = "FireFlink Username", type = "java.lang.String"),
			@InputParam(name = "FireFlink Password", type = "java.lang.String"),
			@InputParam(name = "From mail ID", type = "java.lang.String"),
			@InputParam(name = "From Mail Password", type = "java.lang.String"),
			@InputParam(name = "To Mail ID", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String mail = (String) programElementsInput.get("FireFlink Username");
		String pass = (String) programElementsInput.get("FireFlink Password");
		String senderEmail = (String) programElementsInput.get("From mail ID");
		String senderPassword = (String) programElementsInput.get("From Mail Password");
		String to = (String) programElementsInput.get("To Mail ID");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			RestAssured.baseURI = "https://sg-app.fireflink.com";
			Map<String, String> loginResult = loginToFireflink(mail, pass);
			System.out.println(loginResult);
			if (!"SUCCESS".equals(loginResult.get("status"))) {
				throw new Exception("Login Failed: " + loginResult.get("errorMessage"));
			}
			String accessToken = loginResult.get("accessToken");
			// System.out.println(accessToken);

			// 🔹 2. GET EXECUTION IDENTIFIERS FROM CONTEXT
			Map<String, String> identifiers = nlpRequestModel.getExecutionIdentifiers();
			// output of this method is //{licenseId=LIC16627, projectId=PJT1179,
			// executionId=EXEC29601, scriptId=SCR5007,
			// runId=Run0d08eb07-3860-476a-9f29-58873fa9ced2,
			// suiteId=SUITE_6e7f1c7f-6836-41e2-8185-91527d1591f8}

			// for local running i am hard coding the values
//              Map<String, String> identifiers = new HashMap<String, String>();
//              identifiers.put("executionId", "EXEC20616");
//              identifiers.put("projectId", "PJT1005");
//              identifiers.put("suiteId", "SUITE_fd072aaa-cce9-489f-b26b-25d3892ad6c5");
//              identifiers.put("licenseId", "LIC19774");
//              identifiers.put("runId", "Runa1d7aab0-a722-4bbc-a5af-5abce725b034");
//              identifiers.put("scriptId", "SCR1822");

			String executionId = identifiers.get("executionId");
			String projectId = identifiers.get("projectId");
			String suiteId = identifiers.get("suiteId");
			String runId = identifiers.get("runId");
			String licenseId = identifiers.get("licenseId");
			String currentScriptId = identifiers.get("scriptId");

			if (executionId == null || projectId == null || suiteId == null) {
				throw new Exception("Execution identifiers missing from context.");
			}
			// String executionDetailsPayload = getExecutionDetails(accessToken,
			// executionId, projectId);
			String executionDetailsPayload = getExecutionDetails(accessToken, executionId, projectId, licenseId);
			// System.out.println("executionDetailsPayload : " + executionDetailsPayload);
			// System.out.println(executionDetailsPayload);
			List<String> scriptIds = getScriptIds(executionDetailsPayload);
			System.out.println("scriptIds : " + scriptIds);
			// sample out put scriptIds : [SCR5007, SCR5116, SCR5117, SCR5118]
			List<String[]> results = new java.util.ArrayList<>();
			int sl = 1;
			for (String scriptId : scriptIds) {

				// Ignore the script which is executing this report code
				if (scriptId.equals(currentScriptId)) {
					continue;
				}
				String scriptResponse = getScriptResult(accessToken, projectId, executionId, runId, scriptId);
				String[] summary = extractScriptSummary(scriptResponse, sl);
				results.add(summary);
				sl++;
			}
			System.out.println("Summary Rows: " + results.size());
			Map<String, Integer> stats = calculateSuiteStats(results);

			System.out.println("Total Scripts : " + stats.get("totalScripts"));
			System.out.println("Passed        : " + stats.get("totalPassed"));
			System.out.println("Failed        : " + stats.get("totalFailed"));
			System.out.println("Skipped       : " + stats.get("totalSkipped"));

			Map<String, String> systemDetails = getSystemDetails(executionDetailsPayload);

			System.out.println("Executed On : " + systemDetails.get("executedOn"));
			System.out.println("Duration    : " + systemDetails.get("executionDuration"));
			System.out.println("Run Type    : " + systemDetails.get("executionRunType"));
			System.out.println("Machine     : " + systemDetails.get("machine"));
			System.out.println("Browser     : " + systemDetails.get("browser"));
			System.out.println("OS          : " + systemDetails.get("os"));
			System.out.println("suiteName   : " + systemDetails.get("suiteName"));
			System.out.println("projectName : " + systemDetails.get("projectName"));

			String suiteStatus = getSuiteStatus(stats);
			String pdfFile = create(results, executionId, systemDetails.get("suiteName"),
					systemDetails.get("projectName"), suiteStatus, systemDetails.get("executedOn"),
					systemDetails.get("executionRunType"), systemDetails.get("executionDuration"),
					systemDetails.get("machine"), systemDetails.get("browser"), systemDetails.get("os"));
			System.out.println("pdfFile : " + pdfFile);
			String executionEndTime = calculateExecutionEndTime(systemDetails.get("executedOn"),
					systemDetails.get("executionDuration"));

			System.out.println("Execution End Time : " + executionEndTime);

			sendEmailWithAttachment(senderEmail, senderPassword, to, pdfFile, systemDetails.get("suiteName"),
					systemDetails.get("projectName"));
			System.out.println("Succ mail send");

			// Your program element business logic starts here ...
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("pass");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Fail to check " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
			throw new NlpException("Error--> " + e.toString());

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public static void sendEmailWithAttachment(String senderEmail, String senderPassword, String recipient,
			String pdfFile, String suiteName, String projectName) throws Exception {

		java.util.Properties props = generateMailProperties();

		javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return new javax.mail.PasswordAuthentication(senderEmail, senderPassword);
			}
		});

		javax.mail.Message msg = new javax.mail.internet.MimeMessage(session);

		msg.setFrom(new javax.mail.internet.InternetAddress(senderEmail));
		msg.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(recipient));

		msg.setSubject("Execution Completed - " + suiteName);

		String mailBody = "Dear FireFlink User,\n\n" + "Execution report attached.\n\n" + "Project : " + projectName
				+ "\n" + "Suite   : " + suiteName + "\n\n" + "Regards,\nFireFlink";

		javax.mail.Multipart multipart = new javax.mail.internet.MimeMultipart();

		// TEXT PART
		javax.mail.internet.MimeBodyPart textPart = new javax.mail.internet.MimeBodyPart();
		textPart.setText(mailBody);
		multipart.addBodyPart(textPart);

		// ATTACH PDF
		javax.mail.internet.MimeBodyPart attachmentPart = new javax.mail.internet.MimeBodyPart();

		javax.activation.DataSource source = new javax.activation.FileDataSource(pdfFile);

		attachmentPart.setDataHandler(new javax.activation.DataHandler(source));
		attachmentPart.setFileName("ExecutionReport.pdf");

		multipart.addBodyPart(attachmentPart);

		msg.setContent(multipart);

		javax.mail.Transport.send(msg);

		System.out.println("Email Sent Successfully");
	}

	public static java.util.Properties generateMailProperties() {

		java.util.Properties props = new java.util.Properties();

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		props.put("mail.smtp.connectiontimeout", "10000");
		props.put("mail.smtp.timeout", "10000");
		props.put("mail.smtp.writetimeout", "10000");

		return props;
	}

	public static String calculateExecutionEndTime(String startTime, String duration) {

		try {

			java.time.Instant start = java.time.Instant.parse(startTime);

			String[] parts = duration.split(":");
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			int seconds = Integer.parseInt(parts[2]);

			java.time.Duration dur = java.time.Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);

			java.time.Instant end = start.plus(dur);

			return end.toString();

		} catch (Exception e) {
			return "Unknown";
		}
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

	public static String create(List<String[]> data, String exeID, String suiteName, String projectName, String status,
			String executedOn, String executionType, String totalDuration, String machine, String browser, String os)
			throws DocumentException, IOException {

		Document document = new Document();
		String file = System.getProperty("user.home") + File.separator + "ExecutionReport.pdf";

		FileOutputStream output = new FileOutputStream(file);
		PdfWriter.getInstance(document, output);
		document.open();

		// Logo + Title
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

		// Execution Status
		Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

		Paragraph detailsHeader = new Paragraph("\n\nExecution Status : " + status + "\nExecution Details : ",
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

		// Summary Table 2 (FIXED ALIGNMENT)
		PdfPTable summaryTable2 = new PdfPTable(2);
		summaryTable2.setWidthPercentage(100);
		summaryTable2.setSpacingAfter(5f);

		summaryTable2.addCell(getCell("Executed On", true));
		summaryTable2.addCell(getCell("Execution Type", true));

		summaryTable2.addCell(getCell(executedOn));
		summaryTable2.addCell(getCell(executionType));

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

		// Scripts Section
		Paragraph scriptsHeader = new Paragraph("Scripts Details : ", sectionFont);
		scriptsHeader.setSpacingBefore(10f);
		scriptsHeader.setSpacingAfter(5f);

		document.add(scriptsHeader);

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

				if (i == 3) {

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

	public static String getSuiteStatus(Map<String, Integer> stats) {

		if (stats.get("totalFailed") > 0) {
			return "Failed";
		}
		return "Passed";
	}

	public static Map<String, String> getSystemDetails(String executionDetailsPayload) {

		JsonPath json = new JsonPath(executionDetailsPayload);

		Map<String, String> details = new HashMap<>();

		String executedOn = json.getString("responseObject.executionStartTime");

		String executionDuration = json
				.getString("responseObject.scriptStatistics.executionDurationInHourMinSecFormat");

		String machine = json.getString("responseObject.preExecution.selectedSystem[0].machineInstances[0].systemUrl");

		String browser = json
				.getString("responseObject.preExecution.selectedSystem[0].machineInstances[0].browserName");

		String os = json
				.getString("responseObject.preExecution.selectedSystem[0].machineInstances[0].machineInfo.osName");

		String runType = json.getString("responseObject.suite.executionSettings.machines.executionType");

		String suiteName = json.getString("responseObject.suite.name");

		String projectName = json.getString("responseObject.preExecution.projectName");

		details.put("executedOn", executedOn);
		details.put("executionDuration", executionDuration);
		details.put("machine", machine);
		details.put("browser", browser);
		details.put("os", os);
		details.put("executionRunType", runType);
		details.put("suiteName", suiteName);
		details.put("projectName", projectName);

		return details;
	}

	public static Map<String, Integer> calculateSuiteStats(List<String[]> results) {

		int totalScripts = results.size();
		int totalPassed = 0;
		int totalFailed = 0;
		int totalSkipped = 0;

		for (String[] row : results) {

			String status = row[3];

			if ("PASS".equalsIgnoreCase(status)) {
				totalPassed++;
			} else if ("FAIL".equalsIgnoreCase(status)) {
				totalFailed++;
			} else {
				totalSkipped++;
			}
		}

		Map<String, Integer> stats = new HashMap<>();
		stats.put("totalScripts", totalScripts);
		stats.put("totalPassed", totalPassed);
		stats.put("totalFailed", totalFailed);
		stats.put("totalSkipped", totalSkipped);

		return stats;
	}

	public static String[] extractScriptSummary(String scriptResponse, int slNo) {

		JsonPath json = new JsonPath(scriptResponse);

		if (json.get("responseObject") == null) {

			return new String[] { String.valueOf(slNo), "Unknown", "Unknown", "NOT_EXECUTED", "00:00:00:000" };
		}

		String path = json.getString("responseObject.path");
		String status = json.getString("responseObject.status");
		String duration = json.getString("responseObject.executionDurationInHourMinSecFormat");

		String moduleName = "Unknown";
		String scriptName = "Unknown";

		if (path != null && path.contains("/")) {

			moduleName = path.substring(0, path.lastIndexOf("/")).replace("/Root/", "");

			scriptName = path.substring(path.lastIndexOf("/") + 1);
		}

		if (status == null)
			status = "NOT_EXECUTED";
		if (duration == null)
			duration = "00:00:00:000";

		System.out.println("moduleName : " + moduleName + " scriptName : " + scriptName + " status : " + status
				+ " duration : " + duration);

		return new String[] { String.valueOf(slNo), moduleName, scriptName, status, duration };
	}

	public static List<String> getScriptIds(String executionDetailsPayload) {

		JsonPath jsonPath = new JsonPath(executionDetailsPayload);

		List<List<List<String>>> nested = jsonPath
				.getList("responseObject.preExecution.executionTree.children.children.key");

		List<String> scriptIds = new java.util.ArrayList<>();

		for (List<List<String>> level1 : nested) {
			for (List<String> level2 : level1) {
				scriptIds.addAll(level2);
			}
		}

		return scriptIds;
	}

	public static String getScriptResult(String token, String projectId, String executionId, String runId,
			String scriptId) {

		RestAssured.baseURI = "https://sg-app.fireflink.com";

		Response response = RestAssured.given().header("Authorization", "Bearer " + token)
				.header("projectId", projectId).when()
				.get("/executionresult/optimize/v3/execution-result/run/" + executionId + "/" + runId + "/" + scriptId)
				.then().extract().response();
		//// output
		//// {"responseCode":200,"responseObject":{"_id":"bfd1b3c3-d477-448b-951b-8db983f7de6a-1a0237b2-fdbc-4a65-ae53-c6c6aa0a4627","key":"SCR5007","name":"checking
		//// click","title":"checking
		//// click","type":"Script","status":"PASS","selectedSystem":{"machineInstanceId":"2cf74fa9-2526-4fa9-98a5-a6db64cb4e90","executionEnv":"Local","browserName":"Google
		//// Chrome","browserInfo":{"version":"145.0.7632.160","name":"Google
		//// Chrome","type":"driver","subType":"browser","osName":"windows","platform":"chrome","driverVersion":"145.0.7632.117"},"browserVersion":"145.0.7632.160","systemUrl":"FFE-Yashas-YS","systemId":"SCDR1023283","version":"145.0.7632.160","machineInfo":{"osName":"Windows
		//// 11
		//// Pro","osVersion":"10.0.26100","hostName":"FFE-Yashas-YS"},"deviceInfo":[],"clientId":"flinko-client-win-6fef2fc3-241f-ad74-7204-17d79c3104ab","headless":false,"numberOfRuns":1,"instanceId":"","machInstanceId":"f7abf271-2efe-4a9a-b32e-e1c209a36f7b","fireCloudProjectId":"","fireCloudProjectName":""},"executedOn":"2026-03-12T11:05:52","executionDuration":4663,"executionDurationInHourMinSecFormat":"00:00:04:663","folder":false,"scriptType":"Web
		//// & Mobile (Android &
		//// iOS)","executionId":"EXEC30308","entityId":"bfd1b3c3-d477-448b-951b-8db983f7de6a-1a0237b2-fdbc-4a65-ae53-c6c6aa0a4627","terminateScriptOnTimeExceed":false,"testCaseExecutionType":"Automation","runId":"Run6c69a129-c9ad-466d-8772-a66dc7117397","licenseId":"LIC16627","projectLabels":[],"uploadedToServer":false,"videoPaths":[],"resultPercentage":{"passed":100.0,"failed":0.0,"warning":0.0,"skipped":0.0,"terminated":0.0,"aborted":0.0,"notExecuted":0.0},"executedBy":"USR143223","includeIteration":false,"path":"/Root/Chrome
		//// extention automation/checking
		//// click","parentId":"MODb31f0406-efc3-42e7-99d1-82c856a7fda6","_class":"com.tyss.optimize.data.models.dto.results.ExecutionEntityResponse","manualId":null,"steps":{"stepResults":[{"name":"Open
		//// Browser","title":"Open
		//// Browser","type":"Group","stepResults":[{"_id":"c3f8611c-e284-45e4-8f80-95da08a286e8-417323ae-db7c-46cc-902f-8f5303aacd26","name":"DesiredCapability
		//// create instance of DesiredCapabilities","title":"DesiredCapability create
		//// instance of
		//// DesiredCapabilities","type":"step","status":"PASS","message":"Instance of
		//// Desired Capabilities is
		//// created","stepInputs":[{"value":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION","name":"ifCheckPointIsFailed","type":"java.lang.String","actualValue":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION"}],"executionDuration":223,"executionDurationInHourMinSecFormat":"00:00:00:223","isScreenshotNotApplicable":false,"stepReturn":{"stepNumber":0,"name":"Capability","type":"GLOBAL","value":"Capabilities
		//// {}","referenceId":"VAR2bc58aeb-7024-4e60-8aab-2879a3cfe2a9"},"executedOn":"2026-03-12T11:05:52","stepId":"STP1001111110","toolTip":"Create
		//// instance of Desired
		//// Capabilities","nlpName":"CreateCapabilitiesInstance","uniqueId":"c3f8611c-e284-45e4-8f80-95da08a286e8-417323ae-db7c-46cc-902f-8f5303aacd26","scriptRunDetails":{"scriptId":"SCR5007","executionId":"EXEC30308","runId":"Run6c69a129-c9ad-466d-8772-a66dc7117397","licenseId":"LIC16627","executionType":"SUITE","suiteId":"SUITE_e5b7b5a1-e1a4-4f14-b7f3-9ee5eb713d12","projectId":"PJT1179","reRun":false,"clientId":"flinko-client-win-6fef2fc3-241f-ad74-7204-17d79c3104ab"},"dataProviderStepInputs":[],"reRunStepResults":[],"nextStep":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/91e61087-bb37-421f-a21b-181b58410b58-20555eac-6e4d-4b1c-b2b4-9a8d04755968","stepSection":"step","nextStepSection":"step","searchKey":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/c3f8611c-e284-45e4-8f80-95da08a286e8-417323ae-db7c-46cc-902f-8f5303aacd26","displayOrder":"1.1"},{"_id":"91e61087-bb37-421f-a21b-181b58410b58-20555eac-6e4d-4b1c-b2b4-9a8d04755968","name":"DesiredCapability
		//// set browser name as browser","title":"DesiredCapability set browser name as
		//// browser","type":"step","status":"PASS","message":"Browser name is set as
		//// chrome in Desired
		//// Capability","stepInputs":[{"value":"Capability","name":"desiredCapabilities","type":"org.openqa.selenium.remote.DesiredCapabilities","reference":"GLOBAL","actualValue":"Capabilities
		//// {}"},{"value":"browser","name":"browserName","type":"java.lang.String","reference":"PROJECTENVIRONMENT","actualValue":"chrome"},{"value":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION","name":"ifCheckPointIsFailed","type":"java.lang.String","actualValue":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION"}],"executionDuration":87,"executionDurationInHourMinSecFormat":"00:00:00:87","isScreenshotNotApplicable":false,"stepReturn":{"stepNumber":0,"name":"BrowserCapability","type":"GLOBAL","value":"Capabilities
		//// {browserName:
		//// chrome}","referenceId":"VARb568b5e6-cb76-41f4-8ec7-f0b84dfe57e8"},"executedOn":"2026-03-12T11:05:53","stepId":"STP10012222221","toolTip":"Set
		//// browser name as browser in Desired
		//// Capability","nlpName":"SetBrowserName","uniqueId":"91e61087-bb37-421f-a21b-181b58410b58-20555eac-6e4d-4b1c-b2b4-9a8d04755968","scriptRunDetails":{"scriptId":"SCR5007","executionId":"EXEC30308","runId":"Run6c69a129-c9ad-466d-8772-a66dc7117397","licenseId":"LIC16627","executionType":"SUITE","suiteId":"SUITE_e5b7b5a1-e1a4-4f14-b7f3-9ee5eb713d12","projectId":"PJT1179","reRun":false,"clientId":"flinko-client-win-6fef2fc3-241f-ad74-7204-17d79c3104ab"},"dataProviderStepInputs":[],"reRunStepResults":[],"nextStep":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/07184d02-3b20-4301-8570-648c2d05eb8a-01748dd8-3dce-4fa1-923d-fb3c06e85f47","previousStep":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/c3f8611c-e284-45e4-8f80-95da08a286e8-417323ae-db7c-46cc-902f-8f5303aacd26","stepSection":"step","nextStepSection":"step","previousStepSection":"step","searchKey":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/91e61087-bb37-421f-a21b-181b58410b58-20555eac-6e4d-4b1c-b2b4-9a8d04755968","displayOrder":"1.2"},{"_id":"07184d02-3b20-4301-8570-648c2d05eb8a-01748dd8-3dce-4fa1-923d-fb3c06e85f47","name":"Open
		//// browser window","title":"Open browser
		//// window","type":"step","status":"PASS","message":"Browser window is
		//// opened","stepInputs":[{"value":"Web_HubURL","name":"hubURL","type":"java.lang.String","reference":"GLOBAL","actualValue":"http://localhost:4444/wd/hub"},{"value":"BrowserCapability","name":"capabilities","type":"DesiredCapabilities","reference":"GLOBAL","actualValue":"Capabilities
		//// {browserName:
		//// chrome}"},{"value":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION","name":"ifCheckPointIsFailed","type":"java.lang.String","actualValue":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION"}],"executionDuration":4323,"executionDurationInHourMinSecFormat":"00:00:04:323","isScreenshotNotApplicable":false,"stepReturn":{"stepNumber":0,"name":"Capability2","type":"GLOBAL","value":"RemoteWebDriver:
		//// chrome on Windows 11
		//// (73cd56cc0091fdacf809aea12ec7a7b1)","referenceId":"VAR8b9a1269-1051-45ee-bae9-698449c8aa58"},"executedOn":"2026-03-12T11:05:57","stepId":"STP10013333332","toolTip":"Browser
		//// window
		//// open","nlpName":"OpenBrowser","uniqueId":"07184d02-3b20-4301-8570-648c2d05eb8a-01748dd8-3dce-4fa1-923d-fb3c06e85f47","scriptRunDetails":{"scriptId":"SCR5007","executionId":"EXEC30308","runId":"Run6c69a129-c9ad-466d-8772-a66dc7117397","licenseId":"LIC16627","executionType":"SUITE","suiteId":"SUITE_e5b7b5a1-e1a4-4f14-b7f3-9ee5eb713d12","projectId":"PJT1179","reRun":false,"clientId":"flinko-client-win-6fef2fc3-241f-ad74-7204-17d79c3104ab"},"dataProviderStepInputs":[],"sessionId":"73cd56cc0091fdacf809aea12ec7a7b1","reRunStepResults":[],"nextStep":"/SCR5007/08f2d667-5a20-46ce-9379-62d368a4a2b3/9a121e39-b493-4cd4-a0d9-3edc958c3ca4-64eacebf-b939-4959-b93e-097b1b525173","previousStep":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/91e61087-bb37-421f-a21b-181b58410b58-20555eac-6e4d-4b1c-b2b4-9a8d04755968","stepSection":"step","nextStepSection":"step","previousStepSection":"step","searchKey":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/07184d02-3b20-4301-8570-648c2d05eb8a-01748dd8-3dce-4fa1-923d-fb3c06e85f47","displayOrder":"1.3"}],"status":"PASS","message":"Open
		//// Browser is
		//// passed","executionDuration":4633,"executionDurationInHourMinSecFormat":"00:00:04:633","isScreenshotNotApplicable":false,"stepResultStats":{"total":3,"totalPassed":3,"totalFailed":0,"totalWarning":0,"totalSkipped":0,"totalTerminated":0,"totalAborted":0,"totalNA":0,"totalPartiallyExecuted":0,"totalNonCascadedPreConditionFailed":0,"totalNonCascadedPostConditionFailed":0,"totalNonCascadedPreConditionWarning":0,"totalNonCascadedPostConditionWarning":0,"executionDuration":4633,"executionDurationInHourMinSecFormat":"00:00:04:633","totalOnHold":0,"totalDeScoped":0,"totalTobeValidated":0,"totalTobeTested":0},"executedOn":"2026-03-12T11:05:57","cascaded":"false","stepId":"STP32648M0D28H15M55S32M466R70","nlpName":"Open
		//// Browser","uniqueId":"b2ad0410-372d-4645-ab4a-e01cdd548a11","reRunStepResults":[],"stepType":"StepGroup","searchKey":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11","displayOrder":"1"},{"name":"Close
		//// Browser","title":"Close
		//// Browser","type":"Group","stepResults":[{"_id":"9a121e39-b493-4cd4-a0d9-3edc958c3ca4-64eacebf-b939-4959-b93e-097b1b525173","name":"Browser
		//// window close","title":"Browser window
		//// close","type":"step","status":"PASS","message":"Active browser window is
		//// closed","stepInputs":[{"value":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION","name":"ifCheckPointIsFailed","type":"java.lang.String","actualValue":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_STEP_GROUP_EXECUTION"}],"executionDuration":14,"executionDurationInHourMinSecFormat":"00:00:00:14","isScreenshotNotApplicable":false,"executedOn":"2026-03-12T11:05:57","stepId":"STP9010021353","toolTip":"Close
		//// active browser
		//// window","nlpName":"CloseBrowser","uniqueId":"9a121e39-b493-4cd4-a0d9-3edc958c3ca4-64eacebf-b939-4959-b93e-097b1b525173","scriptRunDetails":{"scriptId":"SCR5007","executionId":"EXEC30308","runId":"Run6c69a129-c9ad-466d-8772-a66dc7117397","licenseId":"LIC16627","executionType":"SUITE","suiteId":"SUITE_e5b7b5a1-e1a4-4f14-b7f3-9ee5eb713d12","projectId":"PJT1179","reRun":false,"clientId":"flinko-client-win-6fef2fc3-241f-ad74-7204-17d79c3104ab"},"dataProviderStepInputs":[],"reRunStepResults":[],"nextStep":"/SCR5007/f7d438c6-9866-4d1f-a3fd-eefd2d44ae94-d527cfb9-de1c-457d-b1a8-7620084b8af0","previousStep":"/SCR5007/b2ad0410-372d-4645-ab4a-e01cdd548a11/07184d02-3b20-4301-8570-648c2d05eb8a-01748dd8-3dce-4fa1-923d-fb3c06e85f47","stepSection":"step","nextStepSection":"step","previousStepSection":"step","searchKey":"/SCR5007/08f2d667-5a20-46ce-9379-62d368a4a2b3/9a121e39-b493-4cd4-a0d9-3edc958c3ca4-64eacebf-b939-4959-b93e-097b1b525173","displayOrder":"2.1"}],"status":"PASS","message":"Close
		//// Browser is
		//// passed","executionDuration":14,"executionDurationInHourMinSecFormat":"00:00:00:14","isScreenshotNotApplicable":false,"stepResultStats":{"total":1,"totalPassed":1,"totalFailed":0,"totalWarning":0,"totalSkipped":0,"totalTerminated":0,"totalAborted":0,"totalNA":0,"totalPartiallyExecuted":0,"totalNonCascadedPreConditionFailed":0,"totalNonCascadedPostConditionFailed":0,"totalNonCascadedPreConditionWarning":0,"totalNonCascadedPostConditionWarning":0,"executionDuration":14,"executionDurationInHourMinSecFormat":"00:00:00:14","totalOnHold":0,"totalDeScoped":0,"totalTobeValidated":0,"totalTobeTested":0},"executedOn":"2026-03-12T11:05:57","cascaded":"false","stepId":"STP46172M2D12H11M0S8M833R64","nlpName":"Close
		//// Browser","uniqueId":"08f2d667-5a20-46ce-9379-62d368a4a2b3","reRunStepResults":[],"stepType":"StepGroup","searchKey":"/SCR5007/08f2d667-5a20-46ce-9379-62d368a4a2b3","displayOrder":"2"},{"_id":"f7d438c6-9866-4d1f-a3fd-eefd2d44ae94-d527cfb9-de1c-457d-b1a8-7620084b8af0","type":"step","status":"PASS","message":"Passed","stepInputs":[{"value":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_SCRIPT_EXECUTION","name":"ifCheckPointIsFailed","type":"java.lang.String","actualValue":"MARK_THIS_STEP_AS_FAILED_AND_CONTINUE_SCRIPT_EXECUTION"}],"executionDuration":16,"executionDurationInHourMinSecFormat":"00:00:00:16","isScreenshotNotApplicable":false,"stepReturn":{"stepNumber":0,"name":"Capability","type":"GLOBAL","value":"{licenseId=LIC16627,
		//// projectId=PJT1179, executionId=EXEC30308, scriptId=SCR5007,
		//// runId=Run6c69a129-c9ad-466d-8772-a66dc7117397,
		//// suiteId=SUITE_e5b7b5a1-e1a4-4f14-b7f3-9ee5eb713d12}","masked":false,"referenceId":"VAR2bc58aeb-7024-4e60-8aab-2879a3cfe2a9"},"executedOn":"2026-03-12T11:05:57","stepId":"STP75873M2D12H11M2S28M635R4","toolTip":"default_package
		//// :
		//// GetIdentifiers","nlpName":"GetIdentifiers","uniqueId":"f7d438c6-9866-4d1f-a3fd-eefd2d44ae94-d527cfb9-de1c-457d-b1a8-7620084b8af0","scriptRunDetails":{"scriptId":"SCR5007","executionId":"EXEC30308","runId":"Run6c69a129-c9ad-466d-8772-a66dc7117397","licenseId":"LIC16627","executionType":"SUITE","suiteId":"SUITE_e5b7b5a1-e1a4-4f14-b7f3-9ee5eb713d12","projectId":"PJT1179","reRun":false,"clientId":"flinko-client-win-6fef2fc3-241f-ad74-7204-17d79c3104ab"},"dataProviderStepInputs":[],"platform":"None","reRunStepResults":[],"previousStep":"/SCR5007/08f2d667-5a20-46ce-9379-62d368a4a2b3/9a121e39-b493-4cd4-a0d9-3edc958c3ca4-64eacebf-b939-4959-b93e-097b1b525173","stepSection":"step","previousStepSection":"step","searchKey":"/SCR5007/f7d438c6-9866-4d1f-a3fd-eefd2d44ae94-d527cfb9-de1c-457d-b1a8-7620084b8af0","displayOrder":"3"}],"stepResultStats":{"total":3,"totalPassed":3,"totalFailed":0,"totalWarning":0,"totalSkipped":0,"totalTerminated":0,"totalAborted":0,"totalNA":0,"totalPartiallyExecuted":0,"totalNonCascadedPreConditionFailed":0,"totalNonCascadedPostConditionFailed":0,"totalNonCascadedPreConditionWarning":0,"totalNonCascadedPostConditionWarning":0,"executionDuration":4663,"executionDurationInHourMinSecFormat":"00:00:04:663","totalOnHold":0,"totalDeScoped":0,"totalTobeValidated":0,"totalTobeTested":0},"executionDurationInHourMinSecFormat":"00:00:04:663","status":"PASS"},"executedByUser":"Yashas
		//// Y
		//// S","treeKey":"R_Run6c69a129-c9ad-466d-8772-a66dc7117397"},"filterResponse":null,"errorCode":0,"message":null,"status":"SUCCESS","responseMap":null,"nlpAdvancedSearch":null}
		//// }

		return response.getBody().asString();
	}

	public static String getExecutionDetails(String token, String executionId, String projectId, String licenseId) {

		RestAssured.baseURI = "https://sg-app.fireflink.com";

		Response response = RestAssured.given().header("Authorization", "Bearer " + token)
				.header("projectId", projectId).header("licenseId", licenseId).when()
				.get("/dashboardexecution/optimize/v3/dashboard/execution/" + executionId).then().extract().response();

		System.out.println("Execution API Status: " + response.statusCode());
		// System.out.println("Execution API Body: " + response.asString());

		return response.asString();
	}

	public static Map<String, String> loginToFireflink(String username, String password) {

		Map<String, String> result = new HashMap<>();

		try {
			String encryptedPassword = encryptPasswordV3(password);

			String payload = String.format("{ \"emailId\": \"%s\", \"password\": \"%s\"}", username, encryptedPassword);

			Response response = RestAssured.given().baseUri(BASE_URL).contentType(ContentType.JSON).body(payload)
					.post("appmanagement/optimize/v1/public/user/signin");

			String body = response.asString();

			result.put("status", "SUCCESS");
			result.put("accessToken", com.jayway.jsonpath.JsonPath.read(body, "$.responseObject.access_token"));
			result.put("userId", com.jayway.jsonpath.JsonPath.read(body, "$.responseObject.id"));

		} catch (Exception e) {
			result.put("status", "FAILED");
			result.put("errorMessage", e.getMessage());
		}

		return result;
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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("FireFlink Username", "omitya.prasad@fireflink.com");
		nlpRequestModel.getAttributes().put("FireFlink Password", "Omitya.Prasad@2746");
		nlpRequestModel.getAttributes().put("From mail ID", "fireflinkreport@gmail.com");
		nlpRequestModel.getAttributes().put("From Mail Password", "cstg qjcu eecr rcug");
		nlpRequestModel.getAttributes().put("To Mail ID", "omitya.prasad@fireflink.com");
		NlpResponseModel resp = new FireFlinkExecutionReportMailer().execute(nlpRequestModel);
		System.out.println("Status" + resp.getStatus());
		System.out.println("Message" + resp.getStatus());

	}

}