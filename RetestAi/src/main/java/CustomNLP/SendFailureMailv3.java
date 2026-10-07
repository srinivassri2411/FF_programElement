
package CustomNLP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.jayway.jsonpath.DocumentContext;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class SendFailureMailv3 implements Nlp {
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
	public static String timeStamp = "";
	public static Map<String, String> variableMap = new LinkedHashMap<>();
	public static Map<String, String> dpVariableMap = new LinkedHashMap<>();
	public static Map<String, String> dpInputsMap = new LinkedHashMap<>();
	public static List<String> sourceList = new ArrayList<>();
	public static List<String> statusList = new ArrayList<>();
	public static List<String> durationList = new ArrayList<>();
	private static List<String> expectedBehaviourList = new ArrayList<>();
	private static List<String> actualBehaviourList = new ArrayList<>();
	private static boolean exitFlagForFailedStep = false;
	private static boolean exitFlagForActualBehaviourAddition = false;
	public static Map<String, Map<String, String>> failedStepDetailsMap = new LinkedHashMap<>();
	public static List<String> currentStepPath = new ArrayList<>();
	public static String returnValue;
	public static String stepsToReproduce = "";

	// Input parameters
	public static String executionId = "";
	public static String variableData = "";
	public static String dataProviderVariables = "";
	public static String dataProviderInputs = "";
	public static String baseUri = "";
	public static String fireflinkUsername = "";
	public static String fireflinkPassword = "";
	public static String licenseNameGiven = "";
	public static String projectNameGiven = "";
	public static String suiteID = "";
	public static Integer iterationCount = null;
	// Email parameters
	public static String fromMailId = "";
	public static String toMailId = "";
	public static String emailNotification = "";
	public static String fieldString = "";
	public static String clientId = "";
	public static String runId = "";
	public static Object failedIteration = null;
	public static Map<String, String> scriptParentMap = new LinkedHashMap<String, String>();

	@InputParams({ @InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Suite ID", type = "java.lang.String"),
			@InputParam(name = "Dataprovider Variables", type = "java.lang.String"),
			@InputParam(name = "From Email", type = "java.lang.String"),
			@InputParam(name = "To Email", type = "java.lang.String"),
			@InputParam(name = "Email FieldString", type = "java.lang.String"),
			@InputParam(name = "DataProvider Inputs", type = "java.lang.String"),
			@InputParam(name = "Variable Data", type = "java.lang.String"),
			@InputParam(name = "Iteration Count", type = "java.lang.Integer") })
	@ReturnType(name = "File Path", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		fireflinkUsername = (String) programElementsInput.get("Username");
		fireflinkPassword = (String) programElementsInput.get("Password");
		suiteID = (String) programElementsInput.get("Suite ID");
		dataProviderVariables = (String) programElementsInput.get("Dataprovider Variables");
		dataProviderInputs = (String) programElementsInput.get("DataProvider Inputs");
		fromMailId = (String) programElementsInput.get("From Email");
		toMailId = (String) programElementsInput.get("To Email");
		fieldString = (String) programElementsInput.get("Email FieldString");
		variableData = (String) programElementsInput.get("Variable Data");
		iterationCount = (Integer) programElementsInput.get("Iteration Count");
		returnValue = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			Map<String, String> identifiers = nlpRequestModel.getExecutionIdentifiers();
			projectId = "PJT1002";
			executionId = "EXEC35291";
			scriptId = "SCR7951";
			// executionId=identifiers.get("executionId");
//			projectId = identifiers.get("projectId");
//			scriptId = identifiers.get("scriptId");
			mainMethod();
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Throwable e) {
			e.printStackTrace();
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("File Path", returnValue);
		return nlpResponseModel;
	}

	public static void mainMethod() throws Throwable {
		licenseNameGiven = "fire-flink-LIC4900";
		projectNameGiven = "TataPlay";
		dataProviderInputs = "";
		baseUri = "https://fireflink.tataplay.com";
		emailNotification = "true";

		System.out.println("Base URL : " + baseUri);
		System.out.println("Email Id : " + fireflinkUsername);
		System.out.println("License Name : " + licenseNameGiven);
		System.out.println("Project Name : " + projectNameGiven);
		System.out.println("Execution Id : " + executionId);
		System.out.println("Variable Data : " + variableData);
		System.out.println("Data Provider Inputs : " + dataProviderInputs);
		System.out.println("Data Provider Variables : " + dataProviderVariables);
		System.out.println("Email Notification : " + emailNotification);
		System.out.println("From Mail Id : " + fromMailId);
		System.out.println("To Mail Id : " + toMailId);
		System.out.println("Field String : " + fieldString);

		resetStaticVariables();
		processReport();
	}

	private static void resetStaticVariables() {
		slNoAndRowNumber = 1;
		slNoAndRowNumberForDefect = 1;
		failedStepId = "";
		scriptName = "";
		module = "";
		crNumber = "";
		crName = "";
		preConditionSteps = "";
		status = "";
		timeStamp = "";
		executedDate = "";
		executedBy = "";

		variableMap.clear();
		dpVariableMap.clear();
		dpInputsMap.clear();
		sourceList.clear();
		statusList.clear();
		durationList.clear();
		expectedBehaviourList.clear();
		actualBehaviourList.clear();
		failedStepDetailsMap.clear();
		currentStepPath.clear();
		exitFlagForFailedStep = false;
		exitFlagForActualBehaviourAddition = false;
		stepsToReproduce = "";
	}

	public static void processReport() throws Throwable {

		RestAssured.baseURI = baseUri;
		String licenseId = "";

		fireflinkPassword = encryptPasswordV3(fireflinkPassword);

		// ---------- SOURCE COLUMN: No hardcoding; will be populated from step inputs
		// ----------
		// sourceList.add("Whatsapp"); // REMOVED – now dynamic

		String signInRequestPayload = "{ \"emailId\": \"" + fireflinkUsername + "\", \"password\": \""
				+ fireflinkPassword + "\"}";
		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/signin",
				signInRequestPayload);

		String message = convertToString(getJsonValue(signInResponsePayload, "message"));
		if (message != null && message.contains("Email or Password is invalid")) {
			System.err.println(message);
			throw new RuntimeException(message);
		}

		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

		String getAllLicensesResponsePayload = getRequest("appmanagement/optimize/v1/license/licenses", "Authorization",
				"Bearer " + token);
		int noOfLicences = convertToInteger(getJsonValue(getAllLicensesResponsePayload, "responseObject.length()"));

		for (int i2 = 0; i2 < noOfLicences; i2++) {
			String licenseName = convertToString(
					getJsonValue(getAllLicensesResponsePayload, "responseObject[" + i2 + "].licenseName"));
			if (licenseName.equals(licenseNameGiven)) {
				licenseId = convertToString(
						getJsonValue(getAllLicensesResponsePayload, "responseObject[" + i2 + "].id"));
				break;
			}
		}

		if (licenseId.isEmpty()) {
			System.err.println("Invalid License name given, please try again...");
			throw new RuntimeException("Invalid License name given, please try again...");
		}

		String selectLicenseRequestPayload = "{\"defaultLicenseId\":\"" + licenseId + "\"}";
		putRequest("appmanagement/optimize/v1/user/updateUserData/" + userId, selectLicenseRequestPayload,
				"Authorization", "Bearer " + token);

		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId,
				"Authorization", "Bearer " + token);
		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));

		for (int i3 = 0; i3 < noOfProjects; i3++) {
			String projectName = convertToString(
					getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i3 + "].project.name"));
			if (projectName.equals(projectNameGiven)) {
				projectId = convertToString(
						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i3 + "].project.id"));
				projectType = convertToString(
						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i3 + "].project.type"));
				break;
			}
		}

		if (projectId.isEmpty()) {
			System.err.println("Invalid Project name given, please try again...");
			throw new RuntimeException("Invalid Project name given, please try again...");
		}

		String homeDir = System.getProperty("user.home");
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String folderName = "Fireflink" + File.separator + executionId + "_" + timestamp;
		executionFolderPath = createDirectory(homeDir, folderName);
		scriptFolderPath = executionFolderPath + File.separator + "Screenshots";
		new File(scriptFolderPath).mkdirs();
		System.out.println("Screenshots folder: " + scriptFolderPath);
		System.out.println("Created Folder at: " + executionFolderPath);

		String reportHeaders = "Sl No,CR No,CR Name,Source,Module,Current Plan,Target Plan,DTH/NDTH,Scenario,Precondition + Scenario Description with Steps,Expected Behaviour,Actual Behaviour,Status,Step/ Sub Step of issue occurrence,Executed Date,Executed By,Defect ID,Defect Source,Execution ID,Type,Time Stamp";
		String defectHeaders = "Sl No,CR No,CR Name,Module,Test case,Step/ Sub Step of issue occurrence,Source,Steps To reproduce,Issue/ Error Description,Created Date,Attachment of Screenshot,Defect Source,Defect ID,Defect Status,Resolution,Closed Date";

		if (!variableData.isEmpty()) {
			storeVariableDataToList(token);
			for (Entry<String, String> entry : variableMap.entrySet()) {
				reportHeaders = reportHeaders + "," + entry.getKey();
				defectHeaders = defectHeaders + "," + entry.getKey();
			}
		}

		if (!dataProviderInputs.isEmpty()) {
			for (int i = 0; i < dataProviderInputs.split(",").length; i++) {
				reportHeaders = reportHeaders + "," + dataProviderInputs.split(",")[i].trim();
				defectHeaders = defectHeaders + "," + dataProviderInputs.split(",")[i].trim();
			}
		}

		if (!dataProviderVariables.isEmpty()) {
			for (int i = 0; i < dataProviderVariables.split(",").length; i++) {
				reportHeaders = reportHeaders + "," + dataProviderVariables.split(",")[i].trim();
				defectHeaders = defectHeaders + "," + dataProviderVariables.split(",")[i].trim();
			}
		}

		// Create Excel File and Sheets with Headers
		String excelFilePath = createExcelFile(executionFolderPath, executionId);
		InputStream inputStream = new FileInputStream(excelFilePath);
		Workbook workbook = WorkbookFactory.create(inputStream);

		reportSheet = createSheetAndAddHeaders(workbook, "Report", reportHeaders);
		defectsSheet = createSheetAndAddHeaders(workbook, "Defects", defectHeaders);

		// Generate created date
		createdDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

		// Get Executed By
		String treeResponsePayload = getRequest(
				"alltrees/optimize/v3/tree/execution/suite?sourceId=" + executionId + "&action=expandAll",
				"Authorization", "Bearer " + token, "projectId", projectId, "projectType", projectType);
		System.out.println(treeResponsePayload);
		int treeResponseCode = convertToInteger(getJsonValue(treeResponsePayload, "responseCode"));
		System.out.println("Tree Response Code: " + treeResponseCode);

		if (treeResponseCode != 200) {
			System.err.println("Error fetching tree data: " + treeResponsePayload);
			return;
		}

		List<Map<String, Object>> treeDataList = (List<Map<String, Object>>) getJsonValue(treeResponsePayload,
				"responseObject.treeDataList");
		System.out.println(treeDataList);
		System.out.println("Found " + treeDataList.size() + " nodes in tree");
		for (Map<String, Object> node : treeDataList) {
			if ("Execution".equals(node.get("entityType")) && executionId.equals(node.get("executionId"))) {
				executedBy = (String) node.get("runInitiatedBy");
				System.out.println("Found Execution node, executedBy: " + executedBy);
				break;
			}
		}

		// Fill Reports Sheet
		processReportDetails();

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

		System.out.println("Completed");
		returnValue = excelFilePath;
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = RestAssured.given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.when().get(endpoint).asString();
	}

	private static String postRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = RestAssured.given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint).asString();
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

	private static String putRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = RestAssured.given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.body(requestBody).contentType(ContentType.JSON).when().put(endpoint).asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath);
	}

	public static boolean convertToBoolean(Object obj) {
		return (boolean) obj;
	}

	public static int convertToInteger(Object obj) {
		return (int) obj;
	}

	public static String convertToString(Object obj) {
		return (String) obj;
	}

	public static List convertToList(Object obj) {
		return (List) obj;
	}

	public static double convertToDouble(Object obj) {
		return (double) obj;
	}

	public static String encryptPassword(String password) throws Exception {
		byte[] publicKeyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public static void writeDataToExcelUsingColumnHeader(Sheet sheet, int rowNumber, String columnHeader, String data)
			throws Exception {

		DataFormatter dataFormatter = new DataFormatter();
		Row headerRow = sheet.getRow(0);
		if (headerRow == null) {
			throw new IllegalStateException("Header row is missing in the sheet.");
		}

		int columnIndex = -1;

		// Find the index of the specified column
		for (int j = 0; j < headerRow.getLastCellNum(); j++) {
			String headerValue = dataFormatter.formatCellValue(headerRow.getCell(j)).trim();
			if (headerValue.equalsIgnoreCase(columnHeader)) {
				columnIndex = j;
				break;
			}
		}

		if (columnIndex == -1) {
			throw new IllegalArgumentException("Column '" + columnHeader + "' not found in sheet.");
		}

		// Get the target row and create cell if necessary
		Row targetRow = sheet.getRow(rowNumber);
		if (targetRow == null) {
			targetRow = sheet.createRow(rowNumber);
		}

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
			// Combine parent and child paths
			Path fullPath = Paths.get(parentPath, childPath);

			// Create directories if they don't exist
			Files.createDirectories(fullPath);

			// Return the absolute path
			return fullPath.toAbsolutePath().toString();
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + parentPath + "\\" + childPath);
			e.printStackTrace();
			return null; // Return null in case of failure
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

		// Create Cell Style for Dark Blue Background with Borders
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);

		// Set Font Color (White) for better visibility
		Font font = workbook.createFont();
		font.setColor(IndexedColors.WHITE.getIndex());
		font.setBold(true);
		style.setFont(font);

		// Set Borders for Header Cells
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);

		// Write Headers to First Row
		String[] headerArray = headers.split(",");
		for (int i = 0; i < headerArray.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headerArray[i].trim()); // Trim spaces
			cell.setCellStyle(style);
			sheet.autoSizeColumn(i);
		}

		return sheet;
	}

	public static int convertToInt(Object value) {
		if (value instanceof Double) {
			return ((Double) value).intValue();
		} else if (value instanceof Integer) {
			return (int) value;
		} else if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				try {
					return (int) Double.parseDouble((String) value);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("Cannot convert to int: " + value);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported type: " + value);
		}
	}

	public static void storeVariableDataToList(String token) {

		for (int i = 0; i < variableData.split(",").length; i++) {
			String variableType = variableData.split(",")[i].split("##")[0].trim();
			String variableName = variableData.split(",")[i].split("##")[1].trim();
			String variableDetailResponse = "";
			boolean flag = false;
			if (variableType.equals("GV")) {
				variableDetailResponse = getRequest("project/optimize/v1/variable/type/GLOBAL", "Authorization",
						"Bearer " + token, "projectId", projectId);

				List<Map> variables = (List<Map>) getJsonValue(variableDetailResponse, "responseObject");
				for (Map varMap : variables) {
					if (varMap.get("name").equals(variableName)) {
						String variableValue = (String) varMap.get("value");
						variableMap.put(variableName, variableValue);
						// System.out.println("Value of variable "+variableName+" is :" +
						// variableValue);
						flag = true;
						break;
					}
				}
				if (!flag) {
					System.err
							.println("Variable: " + variableName + " Not Available in " + variableType + " Variables");
				}

			} else if (variableType.equals("PEV")) {
				variableDetailResponse = getRequest("project/optimize/v1/variable/type/PROJECT_ENVIRONMENT",
						"Authorization", "Bearer " + token, "projectId", projectId);

				List<Map> variables = (List<Map>) getJsonValue(variableDetailResponse, "responseObject");
				for (Map varMap : variables) {
					if (varMap.get("name").equals(variableName)) {
						String variableValue = (String) varMap.get("value");
						variableMap.put(variableName, variableValue);
						// System.out.println("Value of variable "+variableName+" is :" +
						// variableValue);
						flag = true;
						break;
					}
				}

				if (!flag) {
					System.err
							.println("Variable: " + variableName + " Not Available in " + variableType + " Variables");
				}
			} else {
				System.err.println("Invalid Variable Type");
			}
		}
	}

	public static void processReportDetails() throws Exception {
		System.out.println(scriptId);
		String searchScriptID = postRequest("/analytics/optimize/v3/analytics/search",
				"{\"module\":\"modulesScripts\",\"searchText\":\"" + scriptId
						+ "\",\"responseFilterRequired\":true,\"facetQueries\":[],\"collectionName\":\"module_data\"}",
				"Authorization", "Bearer " + token, "projectId", projectId, "projectType", projectType);
		System.out.println(searchScriptID);
		Object scriptDetails = getJsonValue(searchScriptID,
				"responseObject.treeDataList[?(@.key=='" + scriptId + "' && @.entityType=='Script')]");
		System.out.println(scriptDetails);
		Map scriptInfo = (Map) convertToList(scriptDetails).get(0);
		System.out.println(scriptInfo);
		manualTcId = (String) scriptInfo.get("manualId");
		String parentModuleId = (String) scriptInfo.get("parentId");
		// Create Folder to Store Screenshots
		scriptFolderPath = createDirectory(executionFolderPath, "Screenshots" + File.separator + scriptId);

		if (!manualTcId.equals("")) {
			String manualTcLevelResponsePayoad = getRequest(
					"/project/optimize/v1/scripts/modules/" + parentModuleId + "/script/" + manualTcId, "Authorization",
					"Bearer " + token, "projectId", projectId, "projectType", projectType);

			List<Map<String, Object>> manualTcDetails = (List<Map<String, Object>>) getJsonValue(
					manualTcLevelResponsePayoad, "responseObject.manualTestCase.testCaseDetails");

			for (Map<String, Object> fieldsMap : manualTcDetails) {
				String label = fieldsMap.get("label").toString();
				if (label.equals("Requirement Id")) {
					List<String> valueList = (List<String>) fieldsMap.get("value");
					if (!valueList.isEmpty()) {
						crNumber = valueList.get(0).split("_")[0];
						crName = valueList.get(0).split("_")[1];
					}
				} else if (label.equals("Pre condition and steps")) {
					List<String> valueList = (List<String>) fieldsMap.get("value");
					if (!valueList.isEmpty()) {
						preConditionSteps = valueList.get(0);
					}
				}
			}
		}
		// Fill Automation Details
		scriptName = (String) scriptInfo.get("name");
		module = (String) scriptInfo.get("path");
		module = module.substring(0, module.lastIndexOf("/"));
		System.out.println(module);
		// -----------------------------------------------------------------------------------------------------
//		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
				"Optimize_Dev_client");
		Query query = new Query();
		query.addCriteria(Criteria.where("scriptRunDetails.scriptId").is(scriptId));
		query.addCriteria(Criteria.where("scriptRunDetails.executionId").is(executionId));
		List<Document> stepResultsList = mongoTemplate.find(query, Document.class, "StepResult");

		// To Get behavior and source
		iterateStepsUntilDataProvider(stepResultsList); // behavior, source
		List<String> expectedBehaviourListCopy = new ArrayList<String>();
		List<String> actualBehaviourListCopy = new ArrayList<String>();

		ArrayList<Document> dpResults = new ArrayList<>();
		for (Document doc : stepResultsList) {
			if (doc.containsKey("iteration") && doc.getInteger("iteration").equals(iterationCount)) {
				dpResults.add(doc);
			}
		}

		addSourceVariableAndBehaviourDataOfIteration(dpResults);
		if (statusList.contains("FAIL")) {
			status = "FAIL";
			exitFlagForFailedStep = false;
			failedStepId = "";
		} else if (statusList.contains("WARNING")) {
			status = "WARNING";
		} else if (statusList.contains("PASS")) {
			status = "PASS";
		} else {
			status = "SKIP";
		}
		System.out.println(statusList);
		System.out.println(status);
		System.out.println(expectedBehaviourList);
		System.out.println(actualBehaviourList);
	}

	public static void iterateScriptsTree(List<Map<String, Object>> jsonArray) throws Exception {
		for (Map<String, Object> map : jsonArray) {
			String key = map.get("key").toString();
			if (key.startsWith("SCR")) {
				String parentModule = map.get("parentId").toString();
				scriptParentMap.put(key, parentModule);
			}

			// If stepResults exist, recurse and break on failure
			if (map.containsKey("children")) {
				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("children");
				iterateScriptsTree(subJsonArray);
			}
		}
	}

	public static void iterateAndGetManualTcId(List<Map<String, Object>> jsonArray) throws Exception {
		for (Map<String, Object> map : jsonArray) {
			String key = map.get("key").toString();
			if (key.equals(scriptId)) {
				List<Map<String, Object>> testCaseType = (List<Map<String, Object>>) map.get("testCaseType");
				try {
					manualTcId = testCaseType.get(1).get("id").toString();
					break;
				} catch (Exception e) {
					System.err.println("Manual test case not found.");
				}
			}

			// If stepResults exist, recurse and break on failure
			if (map.containsKey("children")) {
				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("children");
				iterateAndGetManualTcId(subJsonArray);
			}
		}
	}

	public static void writeReportDetailsToExcelRow() throws Exception {
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Sl No", Integer.toString(slNoAndRowNumber));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR No", crNumber);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR Name", crName);
		if (!sourceList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Source", sourceList.get(0));
		}
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Module", module);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Scenario", scriptName);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber,
				"Precondition + Scenario Description with Steps", preConditionSteps);
		if (!expectedBehaviourList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Expected Behaviour",
					processListIntoString(expectedBehaviourList));
		}
		if (!actualBehaviourList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Actual Behaviour",
					processListIntoString(actualBehaviourList));
		}
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Status", status);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed Date", executedDate);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed By", executedBy);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Execution ID", executionId);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Time Stamp", timeStamp);

		for (Entry<String, String> var : variableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
		}

		for (Entry<String, String> input : dpInputsMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, input.getKey(), input.getValue());
		}

		for (Entry<String, String> var : dpVariableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
		}
	}

	public static void writeDetailsToExcelRowIfFailed(int iteration) throws Exception {
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Sl No", Integer.toString(slNoAndRowNumber));
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Sl No",
				Integer.toString(slNoAndRowNumberForDefect));

		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "CR No", crNumber);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "CR No", crNumber);

		if (!sourceList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Source", sourceList.get(0));
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumber, "Source", sourceList.get(0));
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

		if (!expectedBehaviourList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Expected Behaviour",
					processListIntoString(expectedBehaviourList));
		}
		if (!actualBehaviourList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Actual Behaviour",
					processListIntoString(actualBehaviourList));
		}
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Status", status);

		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed Date", executedDate);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Created Date", createdDate);

		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed By", executedBy);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Execution ID", executionId);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Time Stamp", timeStamp);

		for (Entry<String, String> var : variableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, var.getKey(), var.getValue());
		}

		for (Entry<String, String> input : dpInputsMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, input.getKey(), input.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, input.getKey(),
					input.getValue());
		}

		for (Entry<String, String> var : dpVariableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, var.getKey(), var.getValue());
		}

		// Attachment
		String failedStepResponsePayload = getRequest(
				"executionresult/optimize/v1/executionResponse/step/" + failedStepId, "Authorization",
				"Bearer " + token, "projectId", projectId);

		String failedStep = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.name"));
		String stepId = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.stepId"));
		String screenShotPath;
		try {
			screenShotPath = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.screenshotPath"));
		} catch (Exception e) {
			screenShotPath = "";
		}

		String attachmentPath = "";
		// Attach Screenshot
		if (screenShotPath.isEmpty()) {
			attachmentPath = "NA";
		} else {
			String requestBody = "{\"bucketName\":\"flinko-prod\",\"filePath\":[\"" + screenShotPath
					+ "\"],\"type\":\"cloudS3\",\"contentType\":\"screenshot\"}";
			attachmentPath = addScreenshotToFolder("dashboardexecution/optimize/v1/dashboard/download/storage",
					requestBody, token, projectId, scriptFolderPath, iteration + "_" + stepId);
		}
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Attachment of Screenshot",
				attachmentPath);

		// Cause or Issue/ Error Description
		String cause = "";
		try {
			cause = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.errorLog.cause"));
		} catch (Exception e) {
			cause = "None";
		}
		if (cause.contains("Exception")) {
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Issue/ Error Description",
					cause.split("Exception", 2)[0]);
		} else {
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Issue/ Error Description",
					cause);
		}

		// Failed Step
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Step/ Sub Step of issue occurrence",
				failedStep);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumberForDefect, "Step/ Sub Step of issue occurrence",
				failedStep);

		// Defect Status
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect Status", "New");

		// Defect Id
		String defectId = "DEFECT_" + new SimpleDateFormat("yyyyMMddhhmmsss").format(new Date());
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect ID", defectId);
		if (!sourceList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Defect Source",
					sourceList.get(sourceList.size() - 1));
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect Source",
					sourceList.get(sourceList.size() - 1));
		}

		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Defect ID", defectId);
		slNoAndRowNumberForDefect++;
	}

	public static void iterateStepsUntilDataProvider(List<Document> scriptLevelResponsePayload) throws Exception {
		ArrayList<Document> stepResults = new ArrayList<>();
		for (Document doc : scriptLevelResponsePayload) {
			if (!doc.containsKey("iteration") && !doc.getInteger("iteration").equals(iterationCount)) {
				stepResults.add(doc);
			}
		}
		addSourceToListByIterating(scriptLevelResponsePayload);

	}

	public static void addSourceToListByIterating(List stepResults) throws Exception {
		for (Object obj : stepResults) {
			Document step = (Document) obj;
			if (step.containsKey("uniqueId")) {
				if (step.containsKey("stepInputs")) {
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>) step.get("stepInputs");
					for (Map<String, Object> stepInputMap : stepInputs) {
						if (stepInputMap.containsKey("reference")) {
							String reference = stepInputMap.get("reference").toString();
							if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
									|| reference.equals("PROJECT_ENVIRONMENT"))) {
								if (stepInputMap.get("value").toString().startsWith("URL_")
										|| stepInputMap.get("value").toString().startsWith("APP_")) {
									sourceList.add(stepInputMap.get("value").toString().split("_")[1]);
								}
							}
						}
					}
				}

				if (step.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>) step.get("stepReturn");

					if (step.get("name").equals("Generate_Expected_Behaviour")) {
						if (stepReturn.containsKey("value")) {
							expectedBehaviourList.add(stepReturn.get("value").toString());
						}
					} else if (step.get("name").equals("Generate_Actual_Behaviour")) {
						if (stepReturn.containsKey("value")) {
							actualBehaviourList.add(stepReturn.get("value").toString());
						}
					}
				}

			} else if (step.containsKey("stepResults")) {
				List<Map<String, Object>> subStep = (List<Map<String, Object>>) step.get("stepResults");
				addSourceToListByIterating(subStep);
			}
		}
	}

	public static String processListIntoString(List<String> list) {
		String returnString = "";
		for (String string : list) {
			returnString = returnString + "* " + string + "\r\n";
		}
		return returnString;
	}

	public static String sumTimes(List<String> timeStrings) {
		long totalMillis = 0;

		for (String time : timeStrings) {
			String[] parts = time.split(":");
			if (parts.length != 4) {
				throw new IllegalArgumentException("Invalid time format: " + time);
			}

			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			int seconds = Integer.parseInt(parts[2]);
			int millis = Integer.parseInt(parts[3]);

			long currentMillis = millis;
			currentMillis += seconds * 1000;
			currentMillis += minutes * 60 * 1000;
			currentMillis += hours * 60 * 60 * 1000;

			totalMillis += currentMillis;
		}

		long hours = totalMillis / (60 * 60 * 1000);
		totalMillis %= (60 * 60 * 1000);

		long minutes = totalMillis / (60 * 1000);
		totalMillis %= (60 * 1000);

		long seconds = totalMillis / 1000;
		long millis = totalMillis % 1000;

		return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis);
	}

	public static Map<String, String> readInputsFromDataProviderExcel(String fileId, String sheetName, int row)
			throws IOException {
		Map<String, String> inputMap = new LinkedHashMap<String, String>();
		Response response = RestAssured.given().header("Authorization", "Bearer " + token)
				.get("project/optimize/v1/file/download/cloud/" + fileId);
		byte[] excelFile = response.getBody().asByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(excelFile);
		Workbook workbook = new XSSFWorkbook(inputStream);

		// Get the sheet by name
		Sheet sheet = workbook.getSheet(sheetName);
		if (sheet == null) {
			throw new IllegalArgumentException("Sheet with name " + sheetName + " not found.");
		}

		int columnCount = sheet.getRow(0).getLastCellNum();

		for (int i = 0; i < dataProviderInputs.split(",").length; i++) {

			for (int j = 0; j < columnCount; j++) {
				String actHeader = sheet.getRow(0).getCell(j).toString();
				if (actHeader.equals(dataProviderInputs.split(",")[i])) {
					inputMap.put(dataProviderInputs.split(",")[i], sheet.getRow(row).getCell(j).toString());
					break;
				}
			}
		}
		workbook.close();
		return inputMap;
	}

	public static void addSourceVariableAndBehaviourDataOfIteration(List stepResults) throws Exception {
		for (Object obj : stepResults) {
			Document step = (Document) obj;
			if (exitFlagForFailedStep && exitFlagForActualBehaviourAddition) {
				break;
			}

			if (exitFlagForFailedStep && !step.get("name").equals("Generate_Actual_Behaviour")) {
				exitFlagForActualBehaviourAddition = true;
			}

			if (step.get("type").toString().equals("step") && step.get("status").toString().equals("FAIL")
					&& !exitFlagForFailedStep) {
				status = step.get("status").toString();
				failedStepId = step.get("uniqueId").toString();
				exitFlagForFailedStep = true;
			}

			String statusAllSteps = step.get("status").toString();
			statusList.add(statusAllSteps);
			if (step.containsKey("uniqueId")) {

				if (step.containsKey("stepInputs")) {
					// Source
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>) step.get("stepInputs");
					for (Map<String, Object> stepInputMap : stepInputs) {
						if (stepInputMap.containsKey("reference")) {
							String reference = stepInputMap.get("reference").toString();
							if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
									|| reference.equals("PROJECT_ENVIRONMENT"))) {
								if (stepInputMap.get("value").toString().startsWith("URL_")
										|| stepInputMap.get("value").toString().startsWith("APP_")) {
									sourceList.add(stepInputMap.get("value").toString().split("_")[1]);
								}
							}
						}
					}

					// Variable Data
					for (int l = 0; l < dataProviderVariables.split(",").length; l++) {
						for (Map<String, Object> stepInputMap : stepInputs) {
							if (stepInputMap.containsKey("reference")) {
								String reference = stepInputMap.get("reference").toString();
								if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
										|| reference.equals("PROJECT_ENVIRONMENT") || reference.equals("DATAPROVIDER"))
										&& stepInputMap.get("value")
												.equals(dataProviderVariables.split(",")[l].trim())) {
									dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
											stepInputMap.get("actualValue").toString());
								}
							}
						}
					}
				}

				if (step.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>) step.get("stepReturn");

					if (step.get("name").equals("Generate_Expected_Behaviour")) {
						if (stepReturn.containsKey("value")) {
							expectedBehaviourList.add(stepReturn.get("value").toString());
						}
					} else if (step.get("name").equals("Generate_Actual_Behaviour")) {
						if (stepReturn.containsKey("value")) {
							actualBehaviourList.add(stepReturn.get("value").toString());
						}
					}
				}

			} else if (step.containsKey("stepResults")) {
				List<Map<String, Object>> subStep = (List<Map<String, Object>>) step.get("stepResults");
				addSourceVariableAndBehaviourDataOfIteration(subStep);
			}
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Username", "naveen@fireflink.com");
		model.getAttributes().put("Password", "Password@123");
		model.getAttributes().put("Suite ID", "SUITE_1f77a1c8-5340-4bb3-8273-cfeed575698a");
		model.getAttributes().put("Dataprovider Variables", "Prod_RMN,subscriberID,add_Message,dateAndTime,orderID");
		model.getAttributes().put("From Email", "praveenj889@tataplay.com");
		model.getAttributes().put("To Email", "praveenj889@tataplay.com");
		model.getAttributes().put("Email FieldString", "Sub ID=GV##WA_SubID,RMN=GV##WA_RMN");
		model.getAttributes().put("DataProvider Inputs", "Channel_Number,Channel_Name");
		model.getAttributes().put("Variable Data", "");
		model.getAttributes().put("Iteration Count", 0);
		System.out.println(new SendFailureMailv3().execute(model));
	}
}