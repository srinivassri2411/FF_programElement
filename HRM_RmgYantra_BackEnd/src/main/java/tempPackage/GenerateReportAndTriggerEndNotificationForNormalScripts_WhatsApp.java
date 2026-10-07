package tempPackage;

import static io.restassured.RestAssured.baseURI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class GenerateReportAndTriggerEndNotificationForNormalScripts_WhatsApp implements Nlp {
	public static String failedStepId = "";
	public static int slNoAndRowNumber = 1;
	public static int slNoAndRowNumberForDefect = 1;
	public static String scriptName = "";
	public static String module = "";
	public static String createdDate = "";
	public static Sheet reportSheet = null;
	public static Sheet defectsSheet = null;
	public static String token = "";
	public static String projectId = "";
	public static String projectType = "";
	public static String executionFolderPath = "";
	public static String scriptFolderPath = "";
	public static String scriptId = "";
	public static String manualTcId = "";
	public static String crNumber = "";
	public static String crName = "";
	public static String executedDate = "";
	public static String preConditionSteps = "";
	public static String status = "";
	public static String executedBy = "";
	public static String clientId = "";
	public static String runId = "";
	public static Object failedIteration = null;
	public static Map<String, String> variableMap = new LinkedHashMap<>();
	public static Map<String, String> scriptParentMap = new LinkedHashMap<>();
	public static String timeStamp = "";
	public static List<String> sourceList = new LinkedList<>();
	private static List<String> statusList = new ArrayList<>();
	private static List<String> expectedBehaviourList = new ArrayList<>();
	private static List<String> actualBehaviourList = new ArrayList<>();
	private static boolean exitFlagForFailedStep = false;
	private static boolean exitFlagForActualBehaviourAddition = false;
	private static AndroidDriver driver = null;

	public static String executionId = "";
	public static String variableData = "";
	public static String baseUri = "";

	@InputParams({ @InputParam(name = "Base_Url", type = "java.lang.String"),
			@InputParam(name = "Fireflink_Username", type = "java.lang.String"),
			@InputParam(name = "Fireflink_Password", type = "java.lang.String"),
			@InputParam(name = "Variable_Data", type = "java.lang.String"),
			@InputParam(name = "From_MailId", type = "java.lang.String"),
			@InputParam(name = "To_MailId", type = "java.lang.String"),
			@InputParam(name = "Whatsapp_Group", type = "java.lang.String") })
	@ReturnType(name = "Report_Paths", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, String> reportPaths = new LinkedHashMap<String, String>();
		try {
			baseUri = (String) programElementsInput.get("Base_Url");
			String fireflinkUsername = (String) programElementsInput.get("Fireflink_Username");
			String fireflinkPassword = (String) programElementsInput.get("Fireflink_Password");
			variableData = (String) programElementsInput.get("Variable_Data");
			String fromMailId = (String) programElementsInput.get("From_MailId");
			String toMailId = (String) programElementsInput.get("To_MailId");
			String group = (String) programElementsInput.get("Whatsapp_Group");

			// Get Execution Identifiers
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
			executionId = exeIdentifiers.get("executionId");
			runId = exeIdentifiers.get("runId");
			projectId = exeIdentifiers.get("projectId");

//			executionId = "EXEC35440";
//			runId = "Run488762c4-b680-45da-b141-3fe20da32dd0";
//			projectId = "PJT1002";

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

			// Create execution folder
			String homeDir = System.getProperty("user.home");
			String currentTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String folderName = "Fireflink" + File.separator + executionId + "_" + currentTimeStamp;

			executionFolderPath = createDirectory(homeDir, folderName);
			System.out.println("Created Folder at: " + executionFolderPath);

			String reportHeaders = "Sl No,CR No,CR Name,Source,Module,Current Plan,Target Plan,DTH/NDTH,Scenario,Precondition + Scenario Description with Steps,Expected Behaviour,Actual Behaviour,Status,Step/ Sub Step of issue occurrence,Issue/ Error Description,Executed Date,Executed By,Defect ID,Defect Source,Execution ID,Type,Time Stamp";
			String defectHeaders = "Sl No,CR No,CR Name,Module,Test case,Step/ Sub Step of issue occurrence,Source,Steps To reproduce,Issue/ Error Description,Created Date,Attachment of Screenshot,Defect Source,Defect ID,Defect Status,Resolution,Closed Date";

			if (!variableData.isEmpty() && !variableData.equals("null") && !variableData.equals("---")) {
				storeVariableDataToList(token);
				for (Entry<String, String> entry : variableMap.entrySet()) {
					reportHeaders = reportHeaders + "," + entry.getKey();
					defectHeaders = defectHeaders + "," + entry.getKey();
				}
			}

			// Create Excel File and Sheets with Headers
			String excelFilePath = createExcelFile(executionFolderPath, executionId);

			// Return file paths
			reportPaths.put("Exe_FolderPath", executionFolderPath);
			reportPaths.put("Excel_FilePath", excelFilePath);

			InputStream inputStream = new FileInputStream(excelFilePath);
			Workbook workbook = WorkbookFactory.create(inputStream);

			reportSheet = createSheetAndAddHeaders(workbook, "Report", reportHeaders);
			defectsSheet = createSheetAndAddHeaders(workbook, "Defects", defectHeaders);

			// Generate created date
			createdDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

			// Get Executed By and Scripts
			String executionDetailsResponsePayload = getRequest(
					"dashboardexecution/optimize/v3/dashboard/execution/" + executionId + "", "Authorization",
					"Bearer " + token, "projectId", projectId);

			executedBy = convertToString(
					getJsonValue(executionDetailsResponsePayload, "responseObject.modifiedByUname"));

			executedDate = convertToString(
					getJsonValue(executionDetailsResponsePayload, "responseObject.executionStartTime"));

			List<String> scriptIdList = (List<String>) getJsonValue(executionDetailsResponsePayload,
					"responseObject.preExecution.executionTree..children[?(@.type=='Script')].key");

			for (int i = 1; i < scriptIdList.size() - 1; i++) { // Ignore first and last script
				scriptId = scriptIdList.get(i);

				// Create Folder to Store Screenshots
				scriptFolderPath = createDirectory(executionFolderPath, "Screenshots" + File.separator + scriptId);

				// Script level Api Request
				String scriptLevelResponsePayload = getRequest("executionresult/optimize/v3/execution-result/run/"
						+ executionId + "/" + runId + "/" + scriptId, "Authorization", "Bearer " + token, "projectId",
						projectId);

				// Add Script status to Status List
				String scriptStatus = convertToString(
						getJsonValue(scriptLevelResponsePayload, "responseObject.status"));
				statusList.add(scriptStatus);

				// Get module Id and manual test case Id
				String moduleId = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.parentId"));
				String manualTcId = convertToString(
						getJsonValue(scriptLevelResponsePayload, "responseObject.manualId"));

				if (manualTcId != null) {
					// Manual test case level Api Request
					String manualTestCaseLevelResponsePayload = getRequest(
							"project/optimize/v1/scripts/modules/" + moduleId + "/script/" + manualTcId,
							"Authorization", "Bearer " + token, "projectId", projectId);

					// Get CR Number, CR Name and pre condition from manual test case
					List<Map<String, Object>> testCaseDetails = (List<Map<String, Object>>) getJsonValue(
							manualTestCaseLevelResponsePayload, "responseObject.manualTestCase.testCaseDetails");

					for (Map<String, Object> detail : testCaseDetails) {
						if (detail.get("label").toString().equals("Requirement Id")) {
							List<String> valueList = (List<String>) detail.get("value");
							if (!valueList.isEmpty()) {
								crName = valueList.get(0).split("_")[0];
								crNumber = valueList.get(0).split("_")[1];
							}

						} else if (detail.get("label").toString().equals("Pre condition and steps")) {
							List<String> valueList = (List<String>) detail.get("value");
							if (!valueList.isEmpty()) {
								preConditionSteps = valueList.get(0).split("_")[0];
							}
						}
					}
				}

				// Fetch details from automation response payload
				scriptName = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.title"));
				module = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.path"));
				module = module.substring(0, module.lastIndexOf("/"));
				module = module.substring(module.lastIndexOf("/") + 1, module.length());
				timeStamp = convertToString(
						getJsonValue(scriptLevelResponsePayload, "responseObject.executionDurationInHourMinSecFormat"));

				if (!scriptStatus.equals("SKIP")) {
					// Iterate Steps
					iterateSteps(scriptLevelResponsePayload);// Need to check whether pre and post array is steps or
																// stepresults..................................................
				}

				if (scriptStatus.equals("FAIL")) {
					writeDetailsToExcelRowIfFailed();
					exitFlagForFailedStep = false;
					failedStepId = "";
				} else if (scriptStatus.equals("WARNING") || scriptStatus.equals("PASS")
						|| scriptStatus.equals("SKIP")) {
					status = scriptStatus;
					writeReportDetailsToExcelRow();
				}

				exitFlagForActualBehaviourAddition = false;
				crNumber = "";
				crName = "";
				preConditionSteps = "";
				expectedBehaviourList.clear();
				actualBehaviourList.clear();
			}

			for (int i = 0; i < reportHeaders.split(",").length; i++) {
				reportSheet.autoSizeColumn(i);
			}

			for (int i = 0; i < defectHeaders.split(",").length; i++) {
				defectsSheet.autoSizeColumn(i);
			}

			// Save Excel File
			try (OutputStream outputStream = new FileOutputStream(excelFilePath)) {
				workbook.write(outputStream);
			}

			workbook.close();
			inputStream.close();

			// Send End Mail and Whatsapp Notification
			String whatsappMessage = "Suite execution for execution Id " + executionId + " is completed and it is ";
			System.out.println(statusList);
			if (statusList.contains("FAIL")) {
				sendMail(fromMailId, toMailId, "FAIL");
				whatsappMessage = whatsappMessage + "FAIL";
				sendMessage(whatsappMessage, group);
			} else if (statusList.contains("WARNING")) {
				sendMail(fromMailId, toMailId, "WARNING");
				whatsappMessage = whatsappMessage + "WARNING";
				sendMessage(whatsappMessage, group);
			} else if (statusList.contains("PASS")) {
				sendMail(fromMailId, toMailId, "PASS");
				whatsappMessage = whatsappMessage + "PASS";
				sendMessage(whatsappMessage, group);
			} else {
				sendMail(fromMailId, toMailId, "SKIP");
				whatsappMessage = whatsappMessage + "SKIP";
				sendMessage(whatsappMessage, group);
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Generated report successfully.");
			List<String> reportPathList = new ArrayList<String>();
			nlpResponseModel.setOutputLocalFilePaths(reportPathList);
			System.out.println("Completed");

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to generate Report. " + writer.toString());
		}
		slNoAndRowNumber = 1;
		slNoAndRowNumberForDefect = 1;
		clearGlobals();
		nlpResponseModel.getAttributes().put("Report_Paths", reportPaths);
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

	public static void writeDataToExcelUsingColumnHeader(Sheet sheet, int rowNumber, String columnHeader, String data)
			throws Exception {
		DataFormatter dataFormatter = new DataFormatter();
		Row headerRow = sheet.getRow(0);
		if (headerRow == null)
			throw new IllegalStateException("Header row is missing in the sheet.");
		int columnIndex = -1;
		for (int j = 0; j < headerRow.getLastCellNum(); j++) {
			String headerValue = dataFormatter.formatCellValue(headerRow.getCell(j)).trim();
			if (headerValue.equalsIgnoreCase(columnHeader)) {
				columnIndex = j;
				break;
			}
		}
		if (columnIndex == -1)
			throw new IllegalArgumentException("Column '" + columnHeader + "' not found in sheet.");
		Row targetRow = sheet.getRow(rowNumber);
		if (targetRow == null)
			targetRow = sheet.createRow(rowNumber);
		Cell cell = targetRow.createCell(columnIndex, CellType.STRING);
		cell.setCellValue(data);
	}

	public static String addScreenshotToFolder(String endPoint, String body, String token, String projectId,
			String folderPath, String ScriptId) {
		Response response = RestAssured.given().header("Authorization", "Bearer " + token)
				.header("projectId", projectId).contentType(ContentType.JSON).body(body).when().post(endPoint);
		// Check if response is successful
		if (response.getStatusCode() == 200) {
			// Get image bytes from response
			byte[] imageBytes = response.asByteArray();

			// Define file path
			File outputFile = new File(folderPath + File.separator + ScriptId + ".png");

			// Write the image to the specified folder
			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
				fos.write(imageBytes);
				// System.out.println("Screenshot saved successfully at: " +
				// outputFile.getAbsolutePath());
				return outputFile.getAbsolutePath(); // Return the absolute path
			} catch (IOException e) {
				System.err.println("Error saving screenshot: " + e.getMessage());
			}
		} else {
			System.err.println("Failed to get screenshot. HTTP Status: " + response.getStatusCode());
		}
		return null;
	}

	public static String createDirectory(String parentPath, String childPath) {
		try {
			Path fullPath = Paths.get(parentPath, new String[] { childPath });
			Files.createDirectories(fullPath, (FileAttribute<?>[]) new FileAttribute[0]);
			return fullPath.toAbsolutePath().toString();
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + parentPath + "\\" + childPath);
			e.printStackTrace();
			return null;
		}
	}

	public static String createExcelFile(String directory, String workbookName) {
		String filePath = directory + File.separator + workbookName + ".xlsx"; // Full file path

		try (Workbook workbook = new XSSFWorkbook()) {
			// Create directory if not exists
			File dir = new File(directory);
			if (!dir.exists())
				dir.mkdirs();

			// Save Excel File
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				workbook.write(fileOut);
			}

			// System.out.println("Excel file created successfully at: " + filePath);
			return filePath;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Sheet createSheetAndAddHeaders(Workbook workbook, String sheetName, String headers) {
		Sheet sheet = workbook.createSheet(sheetName);
		Row row = sheet.createRow(0);
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		Font font = workbook.createFont();
		font.setColor(IndexedColors.WHITE.getIndex());
		font.setBold(true);
		style.setFont(font);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		String[] headerArray = headers.split(",");
		for (int i = 0; i < headerArray.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headerArray[i].trim());
			cell.setCellStyle(style);
			sheet.autoSizeColumn(i);
		}
		return sheet;
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

	public static void storeVariableDataToList(String token) {
		for (int i = 0; i < (variableData.split(",")).length; i++) {
			String variableType = variableData.split(",")[i].split("##")[0].trim();
			String variableName = variableData.split(",")[i].split("##")[1].trim();
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

	public static void writeReportDetailsToExcelRow() throws Exception {
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Sl No", Integer.toString(slNoAndRowNumber));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR No", crNumber);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR Name", crName);
		if (!sourceList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Source", sourceList.get(0));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Module", module);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Scenario", scriptName);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber,
				"Precondition + Scenario Description with Steps", preConditionSteps);
		if (!expectedBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Expected Behaviour",
					processListIntoString(expectedBehaviourList));
		if (!actualBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Actual Behaviour",
					processListIntoString(actualBehaviourList));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Status", status);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed Date",
				executedDate.replace("T", " "));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed By", executedBy);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Execution ID", executionId);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Time Stamp", timeStamp);
		for (Map.Entry<String, String> var : variableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
		}
		slNoAndRowNumber++;

	}

	public static void writeDetailsToExcelRowIfFailed() throws Exception {
		String screenShotPath;
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Sl No", Integer.toString(slNoAndRowNumber));
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Sl No",
				Integer.toString(slNoAndRowNumberForDefect));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR No", crNumber);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "CR No", crNumber);
		if (!sourceList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Source", sourceList.get(0));
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Source", sourceList.get(0));
		}
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR Name", crName);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "CR Name", crName);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Module", module);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Module", module);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Scenario", scriptName);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Test case", scriptName);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber,
				"Precondition + Scenario Description with Steps", preConditionSteps);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Steps To reproduce",
				preConditionSteps);
		if (!expectedBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Expected Behaviour",
					processListIntoString(expectedBehaviourList));
		if (!actualBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Actual Behaviour",
					processListIntoString(actualBehaviourList));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Status", status);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed Date",
				executedDate.replace("T", " "));
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Created Date", createdDate);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed By", executedBy);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Execution ID", executionId);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Time Stamp", timeStamp);
		for (Map.Entry<String, String> var : variableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, var.getKey(), var.getValue());
		}
		String failedStepResponsePayload = getRequest(
				"executionresult/optimize/v1/executionResponse/step/" + failedStepId,
				new String[] { "Authorization", "Bearer " + token, "projectId", projectId });
		String failedStep = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.name"));
		String stepId = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.stepId"));
		try {
			screenShotPath = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.screenshotPath"));
		} catch (Exception e) {
			screenShotPath = "";
		}
		String attachmentPath = "";
		if (screenShotPath.isEmpty()) {
			attachmentPath = "NA";
		} else {
			String requestBody = "{\"bucketName\":\"flinko-prod\",\"filePath\":[\"" + screenShotPath
					+ "\"],\"type\":\"cloudS3\",\"contentType\":\"screenshot\"}";
			attachmentPath = addScreenshotToFolder("dashboardexecution/optimize/v1/dashboard/download/storage",
					requestBody, token, projectId, scriptFolderPath, stepId);
		}
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Attachment of Screenshot",
				attachmentPath);
		if (!actualBehaviourList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Issue/ Error Description",
					actualBehaviourList.get(actualBehaviourList.size() - 1));
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Issue/ Error Description",
					actualBehaviourList.get(actualBehaviourList.size() - 1));
		}
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Step/ Sub Step of issue occurrence",
				failedStep);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Step/ Sub Step of issue occurrence",
				failedStep);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect Status", "New");
		String defectId = "DEFECT_" + (new SimpleDateFormat("yyyyMMddhhmmsss")).format(new Date());
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect ID", defectId);
		if (!sourceList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Defect Source",
					sourceList.get(sourceList.size() - 1));
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect Source",
					sourceList.get(sourceList.size() - 1));
		}
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Defect ID", defectId);
		slNoAndRowNumberForDefect++;
		slNoAndRowNumber++;
	}

	public static void iterateSteps(String scriptLevelResponsePayload) throws Exception {
		List<Map<String, Object>> stepResults = null;
		Map<String, Object> responseObjectMainScript = (Map<String, Object>) getJsonValue(scriptLevelResponsePayload,
				"responseObject");
		if (responseObjectMainScript.containsKey("dependantExecutionEntityResponses")) {
			List<Map<String, Object>> dependsOnScripts = (List<Map<String, Object>>) responseObjectMainScript
					.get("dependantExecutionEntityResponses");
			for (Map<String, Object> script : dependsOnScripts) {
				String dependScriptId = script.get("key").toString();
				String dependScriptLevelResponsePayload = getRequest("executionresult/optimize/v3/execution-result/run/"
						+ executionId + "/" + runId + "/" + dependScriptId, "Authorization", "Bearer " + token,
						"projectId", projectId);
				Map<String, Object> responseObjectDependsOnScript = (Map<String, Object>) getJsonValue(
						dependScriptLevelResponsePayload, "responseObject");
				if (responseObjectDependsOnScript.containsKey("preConditions")) {
					stepResults = (List<Map<String, Object>>) getJsonValue(dependScriptLevelResponsePayload,
							"responseObject.preConditions.stepResults");
					addSourceToListByIterating(stepResults);
				}
				if (responseObjectDependsOnScript.containsKey("steps")) {
					stepResults = (List<Map<String, Object>>) getJsonValue(dependScriptLevelResponsePayload,
							"responseObject.steps.stepResults");
					addSourceToListByIterating(stepResults);
				}
				if (responseObjectDependsOnScript.containsKey("postConditions")) {
					stepResults = (List<Map<String, Object>>) getJsonValue(dependScriptLevelResponsePayload,
							"responseObject.postConditions.stepResults");
					addSourceToListByIterating(stepResults);
				}
			}
		}
		if (responseObjectMainScript.containsKey("preConditions")) {
			stepResults = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload,
					"responseObject.preConditions.stepResults");
			addSourceToListByIterating(stepResults);
		}
		if (responseObjectMainScript.containsKey("steps")) {
			stepResults = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload,
					"responseObject.steps.stepResults");
			addSourceToListByIterating(stepResults);
		}
		if (responseObjectMainScript.containsKey("postConditions")) {
			stepResults = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload,
					"responseObject.postConditions.stepResults");
			addSourceToListByIterating(stepResults);
		}
	}

	public static void addSourceToListByIterating(List<Map<String, Object>> stepResults) throws Exception {
		for (Map<String, Object> step : stepResults) {
			if (exitFlagForFailedStep && exitFlagForActualBehaviourAddition)
				break;
			if (exitFlagForFailedStep && !step.get("name").equals("Generate_Actual_Behaviour")) {
				exitFlagForActualBehaviourAddition = true;
			}

			if (step.get("type").toString().equals("step") && step.get("status").toString().equals("FAIL")) {
				status = step.get("status").toString();
				failedStepId = step.get("uniqueId").toString();
				exitFlagForFailedStep = true;
			}

			if (step.containsKey("uniqueId") && step.get("type").toString().equals("step")) {
				String stepUniqueId = step.get("uniqueId").toString();
				String stepLevelResponsePayoad = getRequest(
						"/executionresult/optimize/v1/executionResponse/step/" + stepUniqueId,
						new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
								projectType });
				Map<String, Object> responseObject = (Map<String, Object>) getJsonValue(stepLevelResponsePayoad,
						"responseObject");
				if (responseObject == null) {
					continue;
				}
				if (responseObject.containsKey("stepInputs")) {
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>) responseObject.get("stepInputs");
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
				if (responseObject.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>) responseObject.get("stepReturn");
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

	public static String processListIntoString(List<String> list) {
		StringBuilder returnString = new StringBuilder();
		for (String string : list) {
			returnString.append(string).append("\r\n");
		}
		return returnString.toString();
	}

	private static void sendMail(String from, String to, String executionStatus)
			throws AddressException, MessagingException {
		String relayHost = "10.15.0.35";
		Properties props = new Properties();
		props.put("mail.smtp.host", relayHost);
		props.put("mail.smtp.port", "25");
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.from", from);

		Session session = Session.getInstance(props);

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject("Suite Execution Completed for " + executionId + " - " + executionStatus);

		String mailBody = "<html><body>" + "<div>Dear Fireflink User</div></b>"
				+ "<div><b>Suite execution is completed with Execution ID:</b> " + executionId + " and it is "
				+ executionStatus + "</div>"
				+ "<p>Looking forward to serving your complete Testing Automation needs.</p>" + "<div>FireFlink</div>"
				+ "<div>Extreme Automation</div>" + "</body></html>";

		// Create multipart message
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setContent(mailBody, "text/html; charset=utf-8");

		MimeMultipart multipart = new MimeMultipart();
		multipart.addBodyPart(textPart);

		message.setContent(multipart);
		// Send the message
		Transport.send(message);

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

	public static void clearGlobals() {
		failedStepId = "";
		slNoAndRowNumber = 1;
		slNoAndRowNumberForDefect = 1;
		scriptName = "";
		module = "";
		createdDate = "";
		reportSheet = null;
		defectsSheet = null;
		token = "";
		projectId = "";
		projectType = "";
		executionFolderPath = "";
		scriptFolderPath = "";
		scriptId = "";
		manualTcId = "";
		crNumber = "";
		crName = "";
		executedDate = "";
		preConditionSteps = "";
		status = "";
		executedBy = "";
		clientId = "";
		runId = "";
		failedIteration = null;
		timeStamp = "";
		executionId = "";
		variableData = "";
		baseUri = "";

		// Reset flags
		exitFlagForFailedStep = false;
		exitFlagForActualBehaviourAddition = false;

		// Clear collections
		variableMap.clear();
		scriptParentMap.clear();
		sourceList.clear();
		statusList.clear();
		expectedBehaviourList.clear();
		actualBehaviourList.clear();

		// Safely quit driver before null
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	public static void main(String[] args) throws NlpException {
//		module="/Root/Digital Governance/Missed Call/Pay Channels";
//		log.info(module.substring(module.lastIndexOf("/")+1, module.length()));
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Base_Url", "https://fireflink.tataplay.com/");
		model.getAttributes().put("Fireflink_Username", "naveen@fireflink.com");
		model.getAttributes().put("Fireflink_Password", "Password@123");
		model.getAttributes().put("Variable_Data", "GV##WA_RMN,GV##WA_SubID");
		model.getAttributes().put("From_MailId", "praveenj889@tataplay.com");
		model.getAttributes().put("To_MailId", "praveenj889@tataplay.com");
		model.getAttributes().put("Whatsapp_Group", "Acoe_test");
		System.out.println(new GenerateReportAndTriggerEndNotificationForNormalScripts_WhatsApp().execute(model));
	}
}
