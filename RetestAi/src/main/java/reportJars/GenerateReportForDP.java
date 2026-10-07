package reportJars;

import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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
import java.nio.file.attribute.FileAttribute;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
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

public class GenerateReportForDP {
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

	public static Map<String, String> dpVariableMap = new LinkedHashMap<>();

	public static Map<String, String> dpInputsMap = new LinkedHashMap<>();

	public static List<String> sourceList = new ArrayList<>();

	public static List<String> statusList = new ArrayList<>();

	public static List<String> durationList = new ArrayList<>();

	private static List<String> expectedBehaviourList = new ArrayList<>();

	private static List<String> actualBehaviourList = new ArrayList<>();

	private static boolean exitFlagForFailedStep = false;

	private static boolean exitFlagForActualBehaviourAddition = false;

	public static String executionId = null;

	public static String variableData = null;

	public static String dataProviderVariables = null;

	public static String dataProviderInputs = null;

	public static String baseUri = null;

	public static void main(String[] args) throws Exception {
		String fireflinkUsername = null;
		String fireflinkPassword = null;
		String licenseNameGiven = null;
		String projectNameGiven = null;
		System.out.println("Base URL : " + baseUri);
		System.out.println("Email Id : " + fireflinkUsername);
		System.out.println("Password : " + fireflinkPassword);
		System.out.println("License Name : " + licenseNameGiven);
		System.out.println("Project Name : " + projectNameGiven);
		System.out.println("Execution Id : " + executionId);
		System.out.println("Variable Data : " + variableData);
		System.out.println("Data Provider Inputs : " + dataProviderInputs);
		System.out.println("Data Provider Variables : " + dataProviderVariables);
		RestAssured.baseURI = baseUri;
		String licenseId = "";
		fireflinkPassword = encryptPassword(fireflinkPassword);
		String signInRequestPayload = "{ \"emailId\": \"" + fireflinkUsername + "\", \"password\": \""
				+ fireflinkPassword + "\"}";
		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
				signInRequestPayload, new String[0]);
		String message = convertToString(getJsonValue(signInResponsePayload, "message"));
		if (message != null && message.contains("Email or Password is invalid")) {
			System.err.println(message);
			System.exit(1);
		}
		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));
		String getAllLicensesResponsePayload = getRequest("appmanagement/optimize/v1/license/licenses",
				new String[] { "Authorization", "Bearer " + token });
		int noOfLicences = convertToInteger(getJsonValue(getAllLicensesResponsePayload, "responseObject.length()"));
		for (int j = 0; j < noOfLicences; j++) {
			String licenseName = convertToString(
					getJsonValue(getAllLicensesResponsePayload, "responseObject[" + j + "].licenseName"));
			if (licenseName.equals(licenseNameGiven))
				licenseId = convertToString(
						getJsonValue(getAllLicensesResponsePayload, "responseObject[" + j + "].id"));
		}
		if (licenseId.equals("")) {
			System.err.println("Invalid License name given, please try again...");
			System.exit(1);
		}
		String selectLicenseRequestPayload = "{\"defaultLicenseId\":\"" + licenseId + "\"}";
		putRequest("appmanagement/optimize/v1/user/updateUserData/" + userId, selectLicenseRequestPayload,
				new String[] { "Authorization", "Bearer " + token });
		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId,
				new String[] { "Authorization", "Bearer " + token });
		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));
		for (int k = 0; k < noOfProjects; k++) {
			String projectName = convertToString(
					getJsonValue(getAllProjectsResponsePayload, "responseObject[" + k + "].project.name"));
			if (projectName.equals(projectNameGiven)) {
				projectId = convertToString(
						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + k + "].project.id"));
				projectType = convertToString(
						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + k + "].project.type"));
				break;
			}
		}
		if (projectId.equals("")) {
			System.err.println("Invalid Project name given, please try again...");
			System.exit(1);
		}
		String homeDir = System.getProperty("user.home");
		String timestamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
		String folderName = "Fireflink" + File.separator + executionId + "_" + timestamp;
		executionFolderPath = createDirectory(homeDir, folderName);
		System.out.println("Created Folder at: " + executionFolderPath);
		String reportHeaders = "Sl No,CR No,CR Name,Source,Module,Current Plan,Target Plan,DTH/NDTH,Scenario,Precondition + Scenario Description with Steps,Expected Behaviour,Actual Behaviour,Status,Step/ Sub Step of issue occurrence,Issue/ Error Description,Executed Date,Executed By,Defect ID,Defect Source,Execution ID,Type,Time Stamp";
		String defectHeaders = "Sl No,CR No,CR Name,Module,Test case,Step/ Sub Step of issue occurrence,Source,Steps To reproduce,Issue/ Error Description,Created Date,Attachment of Screenshot,Defect Source,Defect ID,Defect Status,Resolution,Closed Date";
		if (!variableData.isEmpty()) {
			storeVariableDataToList(token);
			for (Map.Entry<String, String> entry : variableMap.entrySet()) {
				reportHeaders = reportHeaders + "," + reportHeaders;
				defectHeaders = defectHeaders + "," + defectHeaders;
			}
		}
		if (!dataProviderInputs.isEmpty())
			for (int n = 0; n < (dataProviderInputs.split(",")).length; n++) {
				reportHeaders = reportHeaders + "," + reportHeaders;
				defectHeaders = defectHeaders + "," + defectHeaders;
			}
		if (!dataProviderVariables.isEmpty())
			for (int n = 0; n < (dataProviderVariables.split(",")).length; n++) {
				reportHeaders = reportHeaders + "," + reportHeaders;
				defectHeaders = defectHeaders + "," + defectHeaders;
			}
		String excelFilePath = createExcelFile(executionFolderPath, executionId);
		InputStream inputStream = new FileInputStream(excelFilePath);
		Workbook workbook = WorkbookFactory.create(inputStream);
		reportSheet = createSheetAndAddHeaders(workbook, "Report", reportHeaders);
		defectsSheet = createSheetAndAddHeaders(workbook, "Defects", defectHeaders);
		createdDate = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
		String executionDashboardResponsePayload = getRequest(
				"dashboardexecution/optimize/v1/dashboard/execution/" + executionId, new String[] { "Authorization",
						"Bearer " + token, "projectId", projectId, "projectType", projectType });
		try {
			executedBy = convertToString(
					getJsonValue(executionDashboardResponsePayload, "responseObject.modifiedByUname"));
		} catch (Exception exception) {
		}
		try {
			processReportDetails();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int m;
		for (m = 0; m < (reportHeaders.split(",")).length; m++)
			reportSheet.autoSizeColumn(m);
		for (m = 0; m < (defectHeaders.split(",")).length; m++)
			defectsSheet.autoSizeColumn(m);
		Exception exception1 = null, exception2 = null;
		try {
			OutputStream outputStream = new FileOutputStream(excelFilePath);
			try {
				workbook.write(outputStream);
			} finally {
				if (outputStream != null)
					outputStream.close();
			}
		} finally {
			exception2 = null;
			if (exception1 == null) {
				exception1 = exception2;
			} else if (exception1 != exception2) {
				exception1.addSuppressed(exception2);
			}
		}
		System.out.println("Completed");
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

	private static String putRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]);
		return ((Response) request.body(requestBody).contentType(ContentType.JSON).when().put(endpoint, new Object[0]))
				.asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath, new com.jayway.jsonpath.Predicate[0]);
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

	public static String encryptPassword(String password) throws Exception {
		byte[] publicKeyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(1, publicKey);
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

			System.out.println("Excel file created successfully at: " + filePath);
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

	public static int convertToInt(Object value) {
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

	public static void processReportDetails() throws Exception {
		String executionLevelResponsePayload = getRequest(
				"executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId, new String[] {
						"Authorization", "Bearer " + token, "projectId", projectId, "projectType", projectType });
		executedDate = convertToString(getJsonValue(executionLevelResponsePayload, "responseObject.ExecutedOn"));
		int noOfPlatforms = convertToInteger(
				getJsonValue(executionLevelResponsePayload, "responseObject.platformStatistics.length()"));
		for (int j = 0; j < noOfPlatforms; j++) {
			clientId = convertToString(getJsonValue(executionLevelResponsePayload,
					"responseObject.platformStatistics[" + j + "].clientId"));
			runId = convertToString(
					getJsonValue(executionLevelResponsePayload, "responseObject.platformStatistics[" + j + "].runId"));
			List<Map<String, Object>> scriptsTree = (List<Map<String, Object>>) getJsonValue(
					executionLevelResponsePayload, "responseObject.executionResponses[" + j + "].children");
			iterateScriptsTree(scriptsTree);
			for (Map.Entry<String, String> map : scriptParentMap.entrySet()) {
				scriptId = map.getKey();
				String parentModuleId = map.getValue();
				scriptFolderPath = createDirectory(executionFolderPath, "Screenshots" + File.separator + scriptId);
				String scriptTestDevLevelResponsePayload = getRequest(
						"/project/optimize/v1/scripts/modules/" + parentModuleId + "/script/" + scriptId,
						new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
								projectType });
				String fileId = convertToString(
						getJsonValue(scriptTestDevLevelResponsePayload, "responseObject.dataProvider[0].fileId"));
				String sheetName = convertToString(
						getJsonValue(scriptTestDevLevelResponsePayload, "responseObject.dataProvider[0].sheetName"));
				String fromRowString = convertToString(
						getJsonValue(scriptTestDevLevelResponsePayload, "responseObject.dataProvider[0].fromRow"));
				String toRowString = convertToString(
						getJsonValue(scriptTestDevLevelResponsePayload, "responseObject.dataProvider[0].toRow"));
				int fromRow = Integer.parseInt(fromRowString);
				int toRow = Integer.parseInt(toRowString);
				int dpExcelRows = toRow - fromRow + 1;
				String moduleLevelResponsePayoad = getRequest(
						"/alltrees/optimize/v1/trees/modules/" + parentModuleId + "?sort=true",
						new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
								projectType });
				List<Map<String, Object>> treeForManualTc = (List<Map<String, Object>>) getJsonValue(
						moduleLevelResponsePayoad, "responseObject.moduleTree");
				iterateAndGetManualTcId(treeForManualTc);
				if (!manualTcId.equals("")) {
					String manualTcLevelResponsePayoad = getRequest(
							"/project/optimize/v1/scripts/modules/" + parentModuleId + "/script/" + manualTcId,
							new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
									projectType });
					int statusCode = convertToInt(getJsonValue(manualTcLevelResponsePayoad, "responseCode"));
					if (statusCode == 200) {
						List<Map<String, Object>> manualTcDetails = (List<Map<String, Object>>) getJsonValue(
								manualTcLevelResponsePayoad, "responseObject.manualTestCase.testCaseDetails");
						for (Map<String, Object> fieldsMap : manualTcDetails) {
							String label = fieldsMap.get("label").toString();
							if (label.equals("Requirement Id")) {
								List<String> valueList = (List<String>) fieldsMap.get("value");
								if (!valueList.isEmpty())
									try {
										crNumber = ((String) valueList.get(0)).split("_")[0];
										crName = ((String) valueList.get(0)).split("_")[1];
									} catch (Exception exception) {
									}
								continue;
							}
							if (label.equals("Pre condition and steps")) {
								List<String> valueList = (List<String>) fieldsMap.get("value");
								if (!valueList.isEmpty())
									preConditionSteps = valueList.get(0);
							}
						}
					}
				}
				String scriptLevelResponsePayload = getRequest(
						"executionresult/optimize/v1/executionResponse/result/" + executionId + "/" + clientId + "/"
								+ runId + "/" + scriptId,
						new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
								projectType });
				scriptName = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.title"));
				module = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.path"));
				module = module.substring(0, module.lastIndexOf("/"));
				System.out.println(module);
				String[] modu = module.split("/");
				module = modu[modu.length - 1];
				timeStamp = convertToString(
						getJsonValue(scriptLevelResponsePayload, "responseObject.executionDurationInHourMinSecFormat"));
				iterateStepsUntilDataProvider(scriptLevelResponsePayload);
				List<String> expectedBehaviourListCopy = new ArrayList<>();
				List<String> actualBehaviourListCopy = new ArrayList<>();
				List<Map<String, Object>> stepResults = (List<Map<String, Object>>) getJsonValue(
						scriptLevelResponsePayload, "responseObject.stepResults");
				List<Map<String, Object>> dpResults = null;
				for (Map<String, Object> step : stepResults) {
					if (step.get("type").equals("Iteration"))
						dpResults = (List<Map<String, Object>>) step.get("stepResults");
				}
				int dpStepsCount = dpResults.size();
				int lastIteraion = ((Integer) ((Map) dpResults.get(dpResults.size() - 1)).get("iteration")).intValue();
				int noOfIterations = dpStepsCount / (lastIteraion + 1);
				int start = 0;
				int end = noOfIterations;
				if (dpExcelRows != lastIteraion + 1)
					System.err.println(
							"Current Test script Data Provider fromRow and toRow is NOT EQUAL to Test Execution Data Provider fromRow and toRow");
				for (int i = 0; i < lastIteraion + 1; i++) {
					System.out.println("Running.......");
					for (int k = start; k < end; k++) {
						List<Map<String, Object>> dpStepList = new ArrayList<>();
						dpStepList.add(dpResults.get(k));
						addSourceVariableAndBehaviourDataOfIteration(dpStepList);
					}
					timeStamp = sumTimes(durationList);
					dpInputsMap = readInputsFromDataProviderExcel(fileId, sheetName, fromRow);
					if (statusList.contains("FAIL")) {
						status = "FAIL";
						exitFlagForFailedStep = false;
						writeDetailsToExcelRowIfFailed(i + 1);
						slNoAndRowNumber++;
						fromRow++;
						failedStepId = "";
					} else if (statusList.contains("WARNING")) {
						status = "WARNING";
						writeReportDetailsToExcelRow();
						slNoAndRowNumber++;
						fromRow++;
					} else if (statusList.contains("PASS")) {
						status = "PASS";
						writeReportDetailsToExcelRow();
						slNoAndRowNumber++;
						fromRow++;
					} else {
						status = "SKIP";
						writeReportDetailsToExcelRow();
						slNoAndRowNumber++;
						fromRow++;
					}
					durationList.clear();
					start = end;
					end += noOfIterations;
					exitFlagForActualBehaviourAddition = false;
					expectedBehaviourList = expectedBehaviourListCopy;
					actualBehaviourList = actualBehaviourListCopy;
					expectedBehaviourList.clear();
					actualBehaviourList.clear();
					statusList.clear();
					dpVariableMap.clear();
				}
				expectedBehaviourList.clear();
				actualBehaviourList.clear();
				crNumber = "";
				crName = "";
				preConditionSteps = "";
			}
		}
	}

	public static void iterateScriptsTree(List<Map<String, Object>> jsonArray) throws Exception {
		for (Map<String, Object> map : jsonArray) {
			String key = map.get("key").toString();
			if (key.startsWith("SCR")) {
				String parentModule = map.get("parentId").toString();
				scriptParentMap.put(key, parentModule);
			}
			if (map.containsKey("children")) {
				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("children");
				iterateScriptsTree(subJsonArray);
			}
		}
	}

	public static void iterateAndGetManualTcId(List<Map<String, Object>> jsonArray) throws Exception {
		List<Map<String, Object>> children = (List<Map<String, Object>>) ((Map) jsonArray.get(0)).get("children");
		for (Map<String, Object> childMap : children) {
			String manualId = "";
			boolean automationFound = false;
			if (childMap.get("key").toString().startsWith("SCR")) {
				List<Map<String, Object>> testCaseType = (List<Map<String, Object>>) childMap.get("testCaseType");
				for (Map<String, Object> testCase : testCaseType) {
					String id = (String) testCase.get("id");
					String type = (String) testCase.get("type");
					if ("Automation".equalsIgnoreCase(type) && scriptId.equals(id)) {
						automationFound = true;
						continue;
					}
					if ("manual".equalsIgnoreCase(type))
						manualId = id;
				}
				if (automationFound && manualId != null)
					manualTcId = manualId;
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
		for (Map.Entry<String, String> input : dpInputsMap.entrySet()) {
			writeDataToExcelUsingColumnHeader(reportSheet, slNoAndRowNumber, input.getKey(), input.getValue());
			writeDataToExcelUsingColumnHeader(defectsSheet, slNoAndRowNumberForDefect, input.getKey(),
					input.getValue());
		}
		for (Map.Entry<String, String> var : dpVariableMap.entrySet()) {
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
					requestBody, token, projectId, scriptFolderPath, "" + iteration + "_" + iteration);
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
	}

	public static void iterateStepsUntilDataProvider(String scriptLevelResponsePayload) throws Exception {
		List<Map<String, Object>> stepResults = null;
		Map<String, Object> responseObjectMainScript = (Map<String, Object>) getJsonValue(scriptLevelResponsePayload,
				"responseObject");
		if (responseObjectMainScript.containsKey("dependantExecutionEntityResponses")) {
			List<Map<String, Object>> dependsOnScripts = (List<Map<String, Object>>) responseObjectMainScript
					.get("dependantExecutionEntityResponses");
			if (dependsOnScripts != null)
				for (Map<String, Object> script : dependsOnScripts) {
					String dependScriptId = script.get("key").toString();
					String dependScriptLevelResponsePayload = getRequest(
							"executionresult/optimize/v1/executionResponse/result/" + executionId + "/" + clientId + "/"
									+ runId + "/" + dependScriptId,
							new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
									projectType });
					Map<String, Object> responseObjectDependsOnScript = (Map<String, Object>) getJsonValue(
							dependScriptLevelResponsePayload, "responseObject");
					if (responseObjectDependsOnScript.containsKey("preConditions")) {
						stepResults = (List<Map<String, Object>>) getJsonValue(dependScriptLevelResponsePayload,
								"responseObject.preConditions.stepResults");
						addSourceToListByIterating(stepResults);
					}
					if (responseObjectDependsOnScript.containsKey("stepResults")) {
						stepResults = (List<Map<String, Object>>) getJsonValue(dependScriptLevelResponsePayload,
								"responseObject.stepResults");
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
		if (responseObjectMainScript.containsKey("stepResults")) {
			stepResults = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload,
					"responseObject.stepResults");
			addSourceToListByIterating(stepResults);
		}
	}

	public static void addSourceToListByIterating(List<Map<String, Object>> stepResults) throws Exception {
		for (Map<String, Object> step : stepResults) {
			if (step.get("type").equals("Iteration"))
				break;
			if (step.containsKey("uniqueId")) {
				String stepUniqueId = step.get("uniqueId").toString();
				String stepLevelResponsePayoad = getRequest(
						"/executionresult/optimize/v1/executionResponse/step/" + stepUniqueId,
						new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
								projectType });
				Map<String, Object> responseObject = (Map<String, Object>) getJsonValue(stepLevelResponsePayoad,
						"responseObject");
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
		String returnString = "";
		for (String string : list)
			returnString = returnString + returnString + "\r\n";
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
		return String.format("%02d:%02d:%02d:%03d", new Object[] { Long.valueOf(hours), Long.valueOf(minutes),
				Long.valueOf(seconds), Long.valueOf(millis) });
	}

	public static Map<String, String> readInputsFromDataProviderExcel(String fileId, String sheetName, int row)
			throws IOException {
		Map<String, String> inputMap = new LinkedHashMap<>();
		Response response = (Response) RestAssured.given().header("Authorization", "Bearer " + token, new Object[0])
				.get("project/optimize/v1/file/download/cloud/" + fileId, new Object[0]);
		byte[] excelFile = response.getBody().asByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(excelFile);
		XSSFWorkbook xSSFWorkbook = new XSSFWorkbook(inputStream);
		Sheet sheet = xSSFWorkbook.getSheet(sheetName);
		if (sheet == null)
			throw new IllegalArgumentException("Sheet with name " + sheetName + " not found.");
		int columnCount = sheet.getRow(0).getLastCellNum();
		for (int i = 0; i < (dataProviderInputs.split(",")).length; i++) {
			for (int j = 0; j < columnCount; j++) {
				String actHeader = sheet.getRow(0).getCell(j).toString();
				if (actHeader.equals(dataProviderInputs.split(",")[i])) {
					inputMap.put(dataProviderInputs.split(",")[i], sheet.getRow(row).getCell(j).toString());
					break;
				}
			}
		}
		xSSFWorkbook.close();
		return inputMap;
	}

	public static void addSourceVariableAndBehaviourDataOfIteration(List<Map<String, Object>> stepResults)
			throws Exception {
		for (Map<String, Object> step : stepResults) {
			if (exitFlagForFailedStep && exitFlagForActualBehaviourAddition)
				break;
			if (exitFlagForFailedStep && !step.get("name").equals("Generate_Actual_Behaviour"))
				exitFlagForActualBehaviourAddition = true;
			if (step.get("type").toString().equals("step") && step.get("status").toString().equals("FAIL")
					&& !exitFlagForFailedStep) {
				status = step.get("status").toString();
				failedStepId = step.get("uniqueId").toString();
				exitFlagForFailedStep = true;
			}
			String statusAllSteps = step.get("status").toString();
			statusList.add(statusAllSteps);
			String duration = step.get("executionDurationInHourMinSecFormat").toString();
			durationList.add(duration);
			if (step.containsKey("uniqueId") && !step.get("name").equals("Start for loop")) {
				String stepUniqueId = step.get("uniqueId").toString();
				String stepLevelResponsePayoad = getRequest(
						"/executionresult/optimize/v1/executionResponse/step/" + stepUniqueId,
						new String[] { "Authorization", "Bearer " + token, "projectId", projectId, "projectType",
								projectType });
				Map<String, Object> responseObject = (Map<String, Object>) getJsonValue(stepLevelResponsePayoad,
						"responseObject");
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
					for (int l = 0; l < (dataProviderVariables.split(",")).length; l++) {
						for (Map<String, Object> stepInputMap : stepInputs) {
							if (stepInputMap.containsKey("reference")) {
								String reference = stepInputMap.get("reference").toString();
								if ((reference.equals("LOCAL") || reference.equals("GLOBAL")
										|| reference.equals("PROJECT_ENVIRONMENT") || reference.equals("DATAPROVIDER"))
										&& stepInputMap.get("value").equals(dataProviderVariables.split(",")[l].trim())
										&& stepInputMap.containsKey("actualValue"))
									dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
											stepInputMap.get("actualValue").toString());
							}
						}
					}
				}
				if (responseObject.containsKey("stepReturn")) {
					Map<String, Object> stepReturn = (Map<String, Object>) responseObject.get("stepReturn");
					for (int l = 0; l < (dataProviderVariables.split(",")).length; l++) {
						if (stepReturn.containsKey("type")) {
							String type = stepReturn.get("type").toString();
							if ((type.equals("LOCAL") || type.equals("GLOBAL") || type.equals("PROJECT_ENVIRONMENT")
									|| type.equals("DATAPROVIDER"))
									&& stepReturn.get("name").equals(dataProviderVariables.split(",")[l].trim())
									&& stepReturn.containsKey("value"))
								dpVariableMap.put(dataProviderVariables.split(",")[l].trim(),
										stepReturn.get("value").toString());
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
				continue;
			}
			if (step.containsKey("stepResults")) {
				List<Map<String, Object>> subStep = (List<Map<String, Object>>) step.get("stepResults");
				addSourceVariableAndBehaviourDataOfIteration(subStep);
			}
		}
	}
}
