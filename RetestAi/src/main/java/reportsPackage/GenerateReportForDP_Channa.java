package reportsPackage;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

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
import java.nio.file.attribute.FileAttribute;
import java.security.KeyFactory;
import java.security.PublicKey;
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
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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

import com.jayway.jsonpath.JsonPath;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GenerateReportForDP_Channa {
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
	//	public static String manualTcId = "";
	public static String crNumber = "";
	public static String crName = "";
	public static String executedDate = "";
	public static String preConditionSteps = "";
	public static String status = "";
	public static String executedBy = "";
	public static String clientId = "";
	public static String runId = "Runc76cee47-5466-4cbc-a77f-73241694ca59";
	public static Object failedIteration = null;
	public static Map<String, String> variableMap = new LinkedHashMap<>();
	public static Map<String, String> scriptParentMap = new LinkedHashMap<>();
	public static String timeStamp = "";
	public static Map<String, String> dpVariableMap = new LinkedHashMap<>();
	public static Map<String, String> dpInputsMap = new LinkedHashMap<>();
	public static List<String> sourceList = new ArrayList<>();
	public static List<String> statusList = new ArrayList<>();
	public static List<String> durationList = new ArrayList<>();
	public static List<String> expectedBehaviourList = new ArrayList<>();
	public static List<String> actualBehaviourList = new ArrayList<>();
	public static boolean exitFlagForFailedStep = false;
	public static boolean exitFlagForActualBehaviourAddition = false;
	public static String iterationStepId = "";

	public static String executionId = "EXEC35269";
	public static String variableData = "GV##WA_RMN,GV##WA_SubID";
	public static String dataProviderVariables = "Prod_RMN,subscriberID,eventOrderNumber,time,message,orderDescription";
	public static String dataProviderInputs = "Missed_Call_Number,Pack_Name";
	public static String baseUri = "https://fireflink.tataplay.com/";
	public static String fromMailId = "praveenj889@tataplay.com";
	public static String toMailId = "praveenj889@tataplay.com";

	public static void main(String[] args) throws Exception {
		String fireflinkUsername = "naveen@fireflink.com";
		String fireflinkPassword = "Password@123";
		String licenseNameGiven = "fire-flink-LIC4900";
		String projectNameGiven = "TataPlay";

		System.out.println("Base URL : " + baseUri);
		System.out.println("Email Id : " + fireflinkUsername);
		System.out.println("Password : " + fireflinkPassword);
		System.out.println("License Name : " + licenseNameGiven);
		System.out.println("Project Name : " + projectNameGiven);
		System.out.println("Execution Id : " + executionId);
		System.out.println("Variable Data : " + variableData);
		System.out.println("Data Provider Inputs : " + dataProviderInputs);
		System.out.println("Data Provider Variables : " + dataProviderVariables);

		// Set BASE URI
		baseURI = baseUri;

		// Logic Starts	
		String licenseId = "";

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

		// Fetch UserId and Access Token
		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

		// Get all Licenses API Request
		String getAllLicensesResponsePayload = getRequest("appmanagement/optimize/v1/license/licenses", "Authorization",
				"Bearer " + token);
		int noOfLicences = convertToInteger(getJsonValue(getAllLicensesResponsePayload, "responseObject.length()"));

		// Iterate and check given License exists
		for (int i = 0; i < noOfLicences; i++) {
			String licenseName = convertToString(
					getJsonValue(getAllLicensesResponsePayload, "responseObject[" + i + "].licenseName"));
			if (licenseName.equals(licenseNameGiven)) {
				licenseId = convertToString(
						getJsonValue(getAllLicensesResponsePayload, "responseObject[" + i + "].id"));
			}
		}

		// If given License doesn't exists throw the error message
		if (licenseId.equals("")) {
			System.err.println("Invalid License name given, please try again...");
			throw new RuntimeException("Invalid License name given, please try again...");
		}

		// Select License API Request
		String selectLicenseRequestPayload = "{\"defaultLicenseId\":\"" + licenseId + "\"}";
		putRequest("appmanagement/optimize/v1/user/updateUserData/" + userId + "", selectLicenseRequestPayload,
				"Authorization", "Bearer " + token);

		// Get All projects API Request
		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId + "",
				"Authorization", "Bearer " + token);
		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));

		// Iterate and check given Project exists, If YES fetch ProjectId and
		// ProjectType
		for (int i = 0; i < noOfProjects; i++) {
			String projectName = convertToString(
					getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.name"));
			if (projectName.equals(projectNameGiven)) {
				projectId = convertToString(
						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.id"));
				break;
			}
		}

		// If given Project doesn't exists throw error message
		if (projectId.equals("")) {
			System.err.println("Invalid Project name given, please try again...");
			throw new RuntimeException("Invalid Project name given, please try again...");
		}

		// Create execution folder
		String homeDir = System.getProperty("user.home");
		String currentTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String folderName = "Fireflink" + File.separator + executionId + "_" + currentTimeStamp;

		executionFolderPath = createDirectory(homeDir, folderName);
		System.out.println("Created Folder at: "+executionFolderPath);

		String reportHeaders = "Sl No,CR No,CR Name,Source,Module,Current Plan,Target Plan,DTH/NDTH,Scenario,Precondition + Scenario Description with Steps,Expected Behaviour,Actual Behaviour,Status,Step/ Sub Step of issue occurrence,Issue/ Error Description,Executed Date,Executed By,Defect ID,Defect Source,Execution ID,Type,Time Stamp";
		String defectHeaders = "Sl No,CR No,CR Name,Module,Test case,Step/ Sub Step of issue occurrence,Source,Steps To reproduce,Issue/ Error Description,Created Date,Attachment of Screenshot,Defect Source,Defect ID,Defect Status,Resolution,Closed Date";

		if (!variableData.isEmpty()) {
			storeVariableDataToList(token);
			for (Entry<String, String> entry : variableMap.entrySet()) {
				reportHeaders = reportHeaders +","+ entry.getKey();
				defectHeaders = defectHeaders +","+ entry.getKey();
			}
		}

		if (!dataProviderInputs.isEmpty()) {
			for (int i = 0; i < dataProviderInputs.split(",").length; i++) {
				reportHeaders = reportHeaders +","+ dataProviderInputs.split(",")[i].trim();
				defectHeaders = defectHeaders +","+ dataProviderInputs.split(",")[i].trim();
			}
		}

		if (!dataProviderVariables.isEmpty()) {
			for (int i = 0; i < dataProviderVariables.split(",").length; i++) {
				reportHeaders = reportHeaders +","+ dataProviderVariables.split(",")[i].trim();
				defectHeaders = defectHeaders +","+ dataProviderVariables.split(",")[i].trim();
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

		// Get Executed By and Scripts
		String executionDetailsResponsePayload = getRequest(
				"dashboardexecution/optimize/v3/dashboard/execution/" + executionId + "", "Authorization",
				"Bearer " + token, "projectId", projectId);

		executedBy = convertToString(
				getJsonValue(executionDetailsResponsePayload, "responseObject.modifiedByUname"));

		executedDate = convertToString(
				getJsonValue(executionDetailsResponsePayload, "responseObject.executionStartTime"));

		List<String> scriptIdList = (List<String> )
				getJsonValue(executionDetailsResponsePayload, "responseObject.preExecution.executionTree..children[?(@.type=='Script')].key");

		//		for (int i = 1; i < scriptIdList.size()-1; i++) {   // to ignore first and last script
		for (int i = 0; i < scriptIdList.size(); i++) {

			scriptId = scriptIdList.get(i);

			// Create Folder to Store Screenshots
			scriptFolderPath = createDirectory(executionFolderPath, "Screenshots" + File.separator + scriptId);

			// Script level Api Request
			String scriptLevelResponsePayload = getRequest(
					"executionresult/optimize/v3/execution-result/run/" + executionId + "/"+runId+"/" + scriptId, "Authorization",
					"Bearer " + token, "projectId", projectId);

			// Add Script status to Status List
			statusList.add(convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.status")));

			// Get module Id and manual test case Id
			String moduleId = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.parentId"));
			String manualTcId = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.manualId"));

			if (manualTcId != null) {
				// Manual test case level Api Request
				String manualTestCaseLevelResponsePayload = getRequest(
						"project/optimize/v1/scripts/modules/"+ moduleId + "/script/" + manualTcId, "Authorization",
						"Bearer " + token, "projectId", projectId);

				// Get CR Number, CR Name and pre condition from manual test case
				List<Map<String, Object>> testCaseDetails = (List<Map<String, Object>>) getJsonValue(manualTestCaseLevelResponsePayload,
						"responseObject.manualTestCase.testCaseDetails");

				for (Map<String, Object> detail : testCaseDetails) {
					if (detail.get("label").toString().equals("Requirement Id")) {
						List<String> valueList = (List<String>) detail.get("value");
						if (!valueList.isEmpty()) {
							crName = valueList.get(0).split("_")[0];
							crNumber = valueList.get(0).split("_")[1];
						}

					} else if(detail.get("label").toString().equals("Pre condition and steps")) {
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
			timeStamp = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.executionDurationInHourMinSecFormat"));

			// Fetch behavior and source until Data Provider Step
			iterateStepsUntilDataProvider(scriptLevelResponsePayload); // Need to check whether pre and post array is steps or stepresults..................................................

			// Data Provider iteration starts
			List<String> expectedBehaviourListCopy = new ArrayList<String>();
			List<String> actualBehaviourListCopy = new ArrayList<String>();
			expectedBehaviourListCopy = expectedBehaviourList;
			actualBehaviourListCopy = actualBehaviourList;

			List<Map<String, Object>> dpIterationStepDetails = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload,
					"responseObject.steps.stepResults[?(@.uniqueId=='"+iterationStepId+"')]");

			for (Map<String, Object> map : dpIterationStepDetails) {
				if (!map.containsKey("stepResults")) {
					continue;
				}

				List<Map<String, Object>> dpStepResults = (List<Map<String, Object>>) map.get("stepResults");
				for (Map<String, Object> iteration : dpStepResults) {
					timeStamp = iteration.get("executionDurationInHourMinSecFormat").toString();

					// Fetch DP Inputs   // Currently if it is used and that step is working then only it is working
					List<Map<String, Object>> viewDataSet = (List<Map<String, Object>>) iteration.get("viewDataSet");
					for (int j = 0; j < dataProviderInputs.split(",").length; j++) {
						String requiredDpColumnName = dataProviderInputs.split(",")[j];
						for (Map<String, Object> dataSet : viewDataSet) {
							String actDpColumnName = dataSet.get("name").toString();
							if (requiredDpColumnName.equals(actDpColumnName) && dataSet.containsKey("value")) {
								String dpColumnValue = dataSet.get("value").toString();
								dpInputsMap.put(actDpColumnName, dpColumnValue);
							}
						}
					}

					// Iterate dp Steps 
					List<Map<String, Object>> stepResults = (List<Map<String, Object>>) iteration.get("stepResults");
					addSourceVariableAndBehaviourDataOfIteration(stepResults);

					// Write data to excel
					if (iteration.get("status").equals("FAIL")) {
						status = "FAIL";
						exitFlagForFailedStep = false;
						writeDetailsToExcelRowIfFailed(i + 1);
						slNoAndRowNumber++;
						failedStepId = "";
					} else if (iteration.get("status").equals("WARNING")) {
						status = "WARNING";
						writeReportDetailsToExcelRow();
						slNoAndRowNumber++;
					} else if (iteration.get("status").equals("PASS")) {
						status = "PASS";
						writeReportDetailsToExcelRow();
						slNoAndRowNumber++;
					} else {
						status = "SKIP";
						writeReportDetailsToExcelRow();
						slNoAndRowNumber++;
					}

					exitFlagForActualBehaviourAddition = false;
					expectedBehaviourList.clear();
					actualBehaviourList.clear();
					expectedBehaviourList = expectedBehaviourListCopy;
					actualBehaviourList = actualBehaviourListCopy;
					dpVariableMap.clear();
					dpInputsMap.clear();
				}
			}
			
			sourceList.clear();
			expectedBehaviourList.clear();
			actualBehaviourList.clear();
			crNumber = "";
			crName = "";
			preConditionSteps = "";

			for (int j = 0; j < reportHeaders.split(",").length; j++) {
				reportSheet.autoSizeColumn(j);
			}

			for (int j = 0; j < defectHeaders.split(",").length; j++) {
				defectsSheet.autoSizeColumn(j);
			}

			// Save Excel File
			try (OutputStream outputStream = new FileOutputStream(excelFilePath)) {
				workbook.write(outputStream);
			}

			workbook.close();
			inputStream.close();

			// Send Mail End Mail and Whatsapp Notification
			System.out.println(statusList);
			if (statusList.contains("FAIL")) {
				sendMail(fromMailId, toMailId, "FAIL");
			} else if (statusList.contains("WARNING")) {
				sendMail(fromMailId, toMailId, "WARNING");
			} else if (statusList.contains("PASS")) {
				sendMail(fromMailId, toMailId, "PASS");
			} else {
				sendMail(fromMailId, toMailId, "SKIP");
			}
			
			System.out.println("Completed");
		}
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs."); 
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]); 
		return ((Response)request.when().get(endpoint, new Object[0])).asString();
	}

	private static String postRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs."); 
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]); 
		return ((Response)request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint, new Object[0])).asString();
	}

	private static String putRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs."); 
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]); 
		return ((Response)request.body(requestBody).contentType(ContentType.JSON).when().put(endpoint, new Object[0])).asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath, new com.jayway.jsonpath.Predicate[0]);
	}

	public static boolean convertToBoolean(Object obj) {
		return ((Boolean)obj).booleanValue();
	}

	public static int convertToInteger(Object value) {
		if (value instanceof Double)
			return ((Double)value).intValue(); 
		if (value instanceof Integer)
			return ((Integer)value).intValue(); 
		if (value instanceof String)
			try {
				return Integer.parseInt((String)value);
			} catch (NumberFormatException e) {
				try {
					return (int)Double.parseDouble((String)value);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("Cannot convert to int: " + String.valueOf(value));
				} 
			}  
		throw new IllegalArgumentException("Unsupported type: " + String.valueOf(value));
	}

	public static String convertToString(Object obj) {
		return (String)obj;
	}

	public static double convertToDouble(Object obj) {
		return ((Double)obj).doubleValue();
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

	public static void writeDataToExcelUsingColumnHeader(Sheet sheet, int rowNumber, String columnHeader, String data) throws Exception {
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

	public static String addScreenshotToFolder(String endPoint, String body, String token, String projectId, String folderPath, String ScriptId) {
		Response response = RestAssured.given().header("Authorization","Bearer " + token).header("projectId", projectId).contentType(ContentType.JSON).body(body).when().post(endPoint);
		// Check if response is successful
		if (response.getStatusCode() == 200) {
			// Get image bytes from response
			byte[] imageBytes = response.asByteArray();

			// Define file path
			File outputFile = new File(folderPath + File.separator + ScriptId + ".png");

			// Write the image to the specified folder
			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
				fos.write(imageBytes);
				//				System.out.println("Screenshot saved successfully at: " + outputFile.getAbsolutePath());
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
			Files.createDirectories(fullPath, (FileAttribute<?>[])new FileAttribute[0]);
			return fullPath.toAbsolutePath().toString();
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + parentPath + "\\" + childPath);
			e.printStackTrace();
			return null;
		} 
	}

	public static String createExcelFile(String directory, String workbookName) {
		String filePath = directory + File.separator + workbookName + ".xlsx";

		try (Workbook workbook = new XSSFWorkbook()) {
			// Create directory if not exists
			File dir = new File(directory);
			if (!dir.exists()) dir.mkdirs();

			// Save Excel File
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				workbook.write(fileOut);
			}

			//			System.out.println("Excel file created successfully at: " + filePath);
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

	public static void storeVariableDataToList(String token) {
		for (int i = 0; i < (variableData.split(",")).length; i++) {
			String variableType = variableData.split(",")[i].split("##")[0].trim();
			String variableName = variableData.split(",")[i].split("##")[1].trim();
			String variableDetailResponse = "";
			boolean flag = false;
			if (variableType.equals("GV")) {
				variableDetailResponse = getRequest("project/optimize/v1/variable/type/GLOBAL", new String[] { "Authorization", "Bearer " + token, "projectId", projectId });
				List<Map> variables = (List<Map>)getJsonValue(variableDetailResponse, "responseObject");
				for (Map varMap : variables) {
					if (varMap.get("name").equals(variableName)) {
						String variableValue = (String)varMap.get("value");
						variableMap.put(variableName, variableValue);
						flag = true;
						break;
					} 
				} 
				if (!flag)
					System.err.println("Variable: " + variableName + " Not Available in " + variableType + " Variables"); 
			} else if (variableType.equals("PEV")) {
				variableDetailResponse = getRequest("project/optimize/v1/variable/type/PROJECT_ENVIRONMENT", new String[] { "Authorization", "Bearer " + token, "projectId", projectId });
				List<Map> variables = (List<Map>)getJsonValue(variableDetailResponse, "responseObject");
				for (Map varMap : variables) {
					if (varMap.get("name").equals(variableName)) {
						String variableValue = (String)varMap.get("value");
						variableMap.put(variableName, variableValue);
						flag = true;
						break;
					} 
				} 
				if (!flag)
					System.err.println("Variable: " + variableName + " Not Available in " + variableType + " Variables"); 
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
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Precondition + Scenario Description with Steps", preConditionSteps);
		if (!expectedBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Expected Behaviour", processListIntoString(expectedBehaviourList)); 
		if (!actualBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Actual Behaviour", processListIntoString(actualBehaviourList)); 
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Status", status);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed Date", executedDate.replace("T", " "));
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed By", executedBy);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Execution ID", executionId);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Time Stamp", timeStamp);
		for (Map.Entry<String, String> var : variableMap.entrySet())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue()); 
		for (Map.Entry<String, String> input : dpInputsMap.entrySet())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, input.getKey(), input.getValue()); 
		for (Map.Entry<String, String> var : dpVariableMap.entrySet())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue()); 
	}

	public static void writeDetailsToExcelRowIfFailed(int iteration) throws Exception {
		String screenShotPath;
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Sl No", Integer.toString(slNoAndRowNumber));
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Sl No", Integer.toString(slNoAndRowNumberForDefect));
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
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Precondition + Scenario Description with Steps", preConditionSteps);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Steps To reproduce", preConditionSteps);
		if (!expectedBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Expected Behaviour", processListIntoString(expectedBehaviourList)); 
		if (!actualBehaviourList.isEmpty())
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Actual Behaviour", processListIntoString(actualBehaviourList)); 
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Status", status);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed Date", executedDate.replace("T", " "));
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Created Date", createdDate);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Executed By", executedBy);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Execution ID", executionId);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Time Stamp", timeStamp);
		for (Map.Entry<String, String> var : variableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, var.getKey(), var.getValue());
		} 
		for (Map.Entry<String, String> input : dpInputsMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, input.getKey(), input.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, input.getKey(), input.getValue());
		} 
		for (Map.Entry<String, String> var : dpVariableMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, var.getKey(), var.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, var.getKey(), var.getValue());
		} 


		String failedStepResponsePayload = getRequest("executionresult/optimize/v1/executionResponse/step/" + 
				failedStepId, new String[] { "Authorization", "Bearer " + 
						token, "projectId", projectId });
		String failedStep = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.name"));
		try {
			screenShotPath = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.screenshotPath"));
		} catch (Exception e) {
			screenShotPath = "";
		} 
		String attachmentPath = "";
		if (screenShotPath.isEmpty()) {
			attachmentPath = "NA";
		} else {
			String requestBody = "{\"bucketName\":\"flinko-prod\",\"filePath\":[\"" + screenShotPath + "\"],\"type\":\"cloudS3\",\"contentType\":\"screenshot\"}";
			attachmentPath = addScreenshotToFolder("dashboardexecution/optimize/v1/dashboard/download/storage", requestBody, token, projectId, scriptFolderPath, "" + iteration + "_" + iteration);
		} 

		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Attachment of Screenshot", attachmentPath);
		if (!actualBehaviourList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Issue/ Error Description", actualBehaviourList.get(actualBehaviourList.size() - 1));
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Issue/ Error Description", actualBehaviourList.get(actualBehaviourList.size() - 1));
		} 
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Step/ Sub Step of issue occurrence", failedStep);
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Step/ Sub Step of issue occurrence", failedStep);
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect Status", "New");
		String defectId = "DEFECT_" + (new SimpleDateFormat("yyyyMMddhhmmsss")).format(new Date());
		writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect ID", defectId);
		if (!sourceList.isEmpty()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Defect Source", sourceList.get(sourceList.size() - 1));
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, "Defect Source", sourceList.get(sourceList.size() - 1));
		} 
		writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, "Defect ID", defectId);
		slNoAndRowNumberForDefect++;
	}

	public static void iterateStepsUntilDataProvider(String scriptLevelResponsePayload) throws Exception {
		List<Map<String, Object>> stepResults = null;
		Map<String, Object> responseObjectMainScript = (Map<String, Object>) getJsonValue(
				scriptLevelResponsePayload, "responseObject");
		if (responseObjectMainScript.containsKey("dependantExecutionEntityResponses")) {
			List<Map<String, Object>> dependsOnScripts = (List<Map<String, Object>>)responseObjectMainScript.get("dependantExecutionEntityResponses");
			if (dependsOnScripts != null)
				for (Map<String, Object> script : dependsOnScripts) {
					String dependScriptId = script.get("key").toString();
					String dependScriptLevelResponsePayload = getRequest("executionresult/optimize/v3/execution-result/run/" + executionId + "/"+ runId +"/" + dependScriptId, "Authorization", "Bearer " + token, "projectId", projectId);
					Map<String, Object> responseObjectDependsOnScript = (Map<String, Object>)getJsonValue(
							dependScriptLevelResponsePayload, "responseObject");
					if (responseObjectDependsOnScript.containsKey("preConditions")) {
						stepResults = (List<Map<String, Object>>)getJsonValue(
								dependScriptLevelResponsePayload, "responseObject.preConditions.stepResults");
						addSourceToListByIterating(stepResults);
					} 
					if (responseObjectDependsOnScript.containsKey("steps")) {
						stepResults = (List<Map<String, Object>>)getJsonValue(
								dependScriptLevelResponsePayload, "responseObject.steps.stepResults");
						addSourceToListByIterating(stepResults);
					} 
					if (responseObjectDependsOnScript.containsKey("postConditions")) {
						stepResults = (List<Map<String, Object>>)getJsonValue(
								dependScriptLevelResponsePayload, "responseObject.postConditions.stepResults");
						addSourceToListByIterating(stepResults);
					} 
				}  
		} 
		if (responseObjectMainScript.containsKey("preConditions")) {
			stepResults = (List<Map<String, Object>>)getJsonValue(
					scriptLevelResponsePayload, "responseObject.preConditions.stepResults");
			addSourceToListByIterating(stepResults);
		} 
		if (responseObjectMainScript.containsKey("steps")) {
			stepResults = (List<Map<String, Object>>)getJsonValue(
					scriptLevelResponsePayload, "responseObject.steps.stepResults");
			addSourceToListByIterating(stepResults);
		} 
	}

	public static void addSourceToListByIterating(List<Map<String, Object>> stepResults) throws Exception {
		for (Map<String, Object> step : stepResults) {
			if (step.get("type").equals("Iteration")) {
				iterationStepId = step.get("uniqueId").toString();
				break; 
			}
			if (step.containsKey("uniqueId") && step.get("type").equals("step") ) {
				String stepUniqueId = step.get("uniqueId").toString();
				String stepLevelResponsePayoad = getRequest("executionresult/optimize/v1/executionResponse/step/" + stepUniqueId, "Authorization", "Bearer " + token, "projectId", projectId);
				Map<String, Object> responseObject = (Map<String, Object>)getJsonValue(stepLevelResponsePayoad, 
						"responseObject");
				if (responseObject.containsKey("stepInputs")) {
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>)responseObject.get("stepInputs");
					for (Map<String, Object> stepInputMap : stepInputs) {
						if (stepInputMap.containsKey("reference")) {
							String reference = stepInputMap.get("reference").toString();
							if ((reference.equals("LOCAL") || reference.equals("GLOBAL") || reference.equals("PROJECT_ENVIRONMENT")) && (
									stepInputMap.get("value").toString().startsWith("URL_") || stepInputMap.get("value").toString().startsWith("APP_")))
								sourceList.add(stepInputMap.get("value").toString().split("_")[1]); 
						} 
					} 
				}
				if (responseObject.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>)responseObject.get("stepReturn");
					if (step.get("name").equals("Generate_Expected_Behaviour")) {
						if (stepReturn.containsKey("value"))
							expectedBehaviourList.add(stepReturn.get("value").toString()); 
						continue;
					} 
					if (step.get("name").equals("Generate_Actual_Behaviour") && 
							stepReturn.containsKey("value"))
						actualBehaviourList.add(stepReturn.get("value").toString()); 
				} 
				continue;
			} 
			if (step.containsKey("stepResults")) {
				List<Map<String, Object>> subStep = (List<Map<String, Object>>)step.get("stepResults");
				addSourceToListByIterating(subStep);
			} 
		} 
	}

	public static String processListIntoString(List<String> list) {
		String returnString = "";
		for (String string : list)
			returnString = string + returnString + "\r\n"; 
		return returnString;
	}

	public static String sumTimes(List<String> timeStrings) {
		long totalMillis = 0L;
		for (String time : timeStrings) {
			String[] parts = time.split(":");
			if (parts.length != 4)
				throw new IllegalArgumentException("Invalid time format: " + time); 
			int i = Integer.parseInt(parts[0]);
			int j = Integer.parseInt(parts[1]);
			int k = Integer.parseInt(parts[2]);
			int m = Integer.parseInt(parts[3]);
			long currentMillis = m;
			currentMillis += (k * 1000);
			currentMillis += (j * 60 * 1000);
			currentMillis += (i * 60 * 60 * 1000);
			totalMillis += currentMillis;
		} 
		long hours = totalMillis / 3600000L;
		totalMillis %= 3600000L;
		long minutes = totalMillis / 60000L;
		totalMillis %= 60000L;
		long seconds = totalMillis / 1000L;
		long millis = totalMillis % 1000L;
		return String.format("%02d:%02d:%02d:%03d", new Object[] { Long.valueOf(hours), Long.valueOf(minutes), Long.valueOf(seconds), Long.valueOf(millis) });
	}

	public static void addSourceVariableAndBehaviourDataOfIteration(List<Map<String, Object>> stepResults) throws Exception {
		for (Map<String, Object> step : stepResults) {
			System.out.println(step.get("name"));
			if (exitFlagForFailedStep && exitFlagForActualBehaviourAddition)
				break;
			if (exitFlagForFailedStep && !step.get("name").equals("Generate_Actual_Behaviour"))
				exitFlagForActualBehaviourAddition = true; 
			if (step.get("type").toString().equals("step") && step.get("status").toString().equals("FAIL") && !exitFlagForFailedStep) {
				status = step.get("status").toString();
				failedStepId = step.get("uniqueId").toString();
				exitFlagForFailedStep = true;
			} 
			if (step.containsKey("uniqueId") && step.get("type").toString().equals("step")) {
				String stepUniqueId = step.get("uniqueId").toString();
				String stepLevelResponsePayoad = getRequest("executionresult/optimize/v1/executionResponse/step/" + stepUniqueId, "Authorization", "Bearer " + token, "projectId", projectId);
				Map<String, Object> responseObject = (Map<String, Object>)getJsonValue(stepLevelResponsePayoad, "responseObject");
				if (responseObject == null) {
					continue;
				}
				if (responseObject.containsKey("stepInputs")) {
					List<Map<String, Object>> stepInputs = (List<Map<String, Object>>)responseObject.get("stepInputs");
					for (Map<String, Object> stepInputMap : stepInputs) {
						if (stepInputMap.containsKey("reference")) {
							String reference = stepInputMap.get("reference").toString();
							if ((reference.equals("LOCAL") || reference.equals("GLOBAL") || reference.equals("PROJECT_ENVIRONMENT")) && (
									stepInputMap.get("value").toString().startsWith("URL_") || stepInputMap.get("value").toString().startsWith("APP_")))
								sourceList.add(stepInputMap.get("value").toString().split("_")[1]); 
						} 
					} 
					for (int l = 0; l < (dataProviderVariables.split(",")).length; l++) {
						for (Map<String, Object> stepInputMap : stepInputs) {
							if (stepInputMap.containsKey("reference")) {
								String reference = stepInputMap.get("reference").toString();
								if ((reference.equals("LOCAL") || reference.equals("GLOBAL") || reference.equals("PROJECT_ENVIRONMENT") || reference.equals("DATAPROVIDER")) && stepInputMap.get("value").equals(dataProviderVariables.split(",")[l].trim()) && 
										stepInputMap.containsKey("actualValue"))
									dpVariableMap.put(dataProviderVariables.split(",")[l].trim(), stepInputMap.get("actualValue").toString()); 
							} 
						} 
					} 
				} 
				if (responseObject.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>)responseObject.get("stepReturn");
					for (int l = 0; l < (dataProviderVariables.split(",")).length; l++) {
						if (stepReturn.containsKey("type")) {
							String type = stepReturn.get("type").toString();
							if ((type.equals("LOCAL") || type.equals("GLOBAL") || type.equals("PROJECT_ENVIRONMENT") || type.equals("DATAPROVIDER")) && stepReturn.get("name").equals(dataProviderVariables.split(",")[l].trim()) && 
									stepReturn.containsKey("value"))
								dpVariableMap.put(dataProviderVariables.split(",")[l].trim(), stepReturn.get("value").toString()); 
						} 
					} 
					if (step.get("name").equals("Generate_Expected_Behaviour")) {
						if (stepReturn.containsKey("value"))
							expectedBehaviourList.add(stepReturn.get("value").toString()); 
						continue;
					} 
					if (step.get("name").equals("Generate_Actual_Behaviour") && 
							stepReturn.containsKey("value"))
						actualBehaviourList.add(stepReturn.get("value").toString()); 
				} 
				continue;
			} 
			if (step.containsKey("stepResults")) {
				List<Map<String, Object>> subStep = (List<Map<String, Object>>)step.get("stepResults");
				addSourceVariableAndBehaviourDataOfIteration(subStep);
			} 
		} 
	}

	private static void sendMail(String from, String to, String executionStatus) throws AddressException, MessagingException {
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
		message.setSubject("Suite Execution Completed for " + executionId+" - "+executionStatus);

		String mailBody = "<html><body>"
				+ "<div>Dear Fireflink User</div></b>"
				+ "<div><b>Suite execution is completed with Execution ID:</b> " + executionId + " and it is "+executionStatus+"</div>"
				+ "<p>Looking forward to serving your complete Testing Automation needs.</p>"
				+ "<div>FireFlink</div>"
				+ "<div>Extreme Automation</div>"
				+ "</body></html>";
		
		// Create multipart message
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setContent(mailBody, "text/html; charset=utf-8");

		MimeMultipart multipart = new MimeMultipart();
		multipart.addBodyPart(textPart);

		message.setContent(multipart);
		// Send the message
		Transport.send(message);
	}
}
