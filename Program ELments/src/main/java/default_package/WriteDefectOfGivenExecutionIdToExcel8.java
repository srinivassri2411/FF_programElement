//package default_package;
//
//import static io.restassured.RestAssured.baseURI;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.KeyFactory;
//import java.security.PublicKey;
//import java.security.spec.EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.text.SimpleDateFormat;
//import java.util.Base64;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import javax.crypto.Cipher;
//
//import org.apache.poi.ss.usermodel.BorderStyle;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.DataFormatter;
//import org.apache.poi.ss.usermodel.FillPatternType;
//import org.apache.poi.ss.usermodel.Font;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.poi.ss.usermodel.IndexedColors;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.VerticalAlignment;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import com.jayway.jsonpath.JsonPath;
//
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import io.restassured.response.Response;
//import io.restassured.specification.RequestSpecification;
//
//public class WriteDefectOfGivenExecutionIdToExcel8 {
//
//	public static List<String> listOfSteps = new LinkedList<String>();
//	public static String failedStepId = "";
//	public static int slNoAndRowNumber = 1;
//	public static String scriptName = "";
//	public static String module = "";
//	public static String createdDate = "";
//	public static Sheet sheet = null;
//	public static Boolean isFirstBugAdded = false;
//	public static String scope = "OnlyFirstFailedStep"; // AllFailedSteps, OnlyFirstFailedStep
//	public static String token = "";
//	public static String projectId = "";
//	public static String executionFolderPath = "";
//	public static String scriptFolderPath = "";
//	public static String scriptId = "";
//	public static Object failedIteration = null;
//	public static String variableType = ""; //GLOBAL, PROJECT_ENVIRONMENT
//	public static String variableName = "";
//	public static String variableValue = "";
//	public static List<Map<String, Object>> iterationsList = null;
//	public static boolean isItrCompleted = false;
//
//	public static void main(String[] args) throws Exception {
//		String fireflinkUsername = "channa.s@fireflink.com";
//		String fireflinkPassword = "@Channu123";
//		String projectNameGiven = "HRM_RmgYantra_BackEnd";
//		String sheetName = "defect";
//		String executionId = "EXEC20298";
//
//		// Logic Starts
//		String projectType = "";
//
//		// API Base URL
//		baseURI = "https://app.fireflink.com/";
//
//		// Sign in API Request
//		fireflinkPassword = encryptPassword(fireflinkPassword);
//		String signInRequestPayload = "{ \"emailId\": \"" + fireflinkUsername + "\", \"password\": \""
//				+ fireflinkPassword + "\"}";
//		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
//				signInRequestPayload);
//
//		String message = convertToString(getJsonValue(signInResponsePayload, "message"));
//
//		// Provide message if EmailID/Password is Invalid
//		if (message != null && message.contains("Email or Password is invalid")) {
//			System.err.println(message);
//			System.exit(1);
//		}
//
//		// Fetch UserId and Access Token
//		String userId = convertToString(getJsonValue(signInResponsePayload, "responseObject.id"));
//		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));
//
//		// Get All projects API Request
//		String getAllProjectsResponsePayload = getRequest("project/optimize/v1/projects/user/" + userId + "",
//				"Authorization", "Bearer " + token);
//		int noOfProjects = convertToInteger(getJsonValue(getAllProjectsResponsePayload, "responseObject.length()"));
//
//		// Iterate and check given Project exists, If YES fetch ProjectId and
//		// ProjectType
//		for (int i = 0; i < noOfProjects; i++) {
//			String projectName = convertToString(
//					getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.name"));
//			if (projectName.equals(projectNameGiven)) {
//				projectId = convertToString(
//						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.id"));
//				projectType = convertToString(
//						getJsonValue(getAllProjectsResponsePayload, "responseObject[" + i + "].project.type"));
//				break;
//			}
//		}
//
//		// If given Project doesn't exists throw error message
//		if (projectId.equals("")) {
//			System.err.println("Invalid Project name given, please try again...");
//			System.exit(1);
//		}
//
//		// Create execution folder
//		String homeDir = System.getProperty("user.home");
//		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//		String folderName = "Fireflink" + File.separator + executionId + "_" + timestamp;
//
//		executionFolderPath = createDirectory(homeDir, folderName);
//		System.out.println("Created Folder at: "+executionFolderPath);
//
//		String headers = "";
//		if (variableName.isEmpty() || variableType.isEmpty()) {
//			headers = "Sl No,CR No,CR Name,Module,Test case,Step/ Sub Step of issue occurrence,Source,Steps To reproduce,Issue/ Error Description,Subscriber ID/ RMN,Created Date,Attachment of Screenshot,Defect ID,Defect Status,Resolution,Closed Date";
//		} else {
//			variableValue = getVariableValue(token, projectId);
//			headers = "Sl No,CR No,CR Name,Module,Test case,Step/ Sub Step of issue occurrence,Source,Steps To reproduce,Issue/ Error Description,Subscriber ID/ RMN,Created Date,Attachment of Screenshot,Defect ID,Defect Status,Resolution,Closed Date,"+variableName;
//		}
//		String excelFilePath = createExcelFile(executionFolderPath, executionId, sheetName, headers);
//
//		String executionLevelResponsePayload = getRequest(
//				"executionresult/optimize/v1/executionResponse/result/expandAll/" + executionId + "", "Authorization",
//				"Bearer " + token, "projectId", projectId, "projectType", projectType);
//		
//		int noOfPlatforms = convertToInteger(
//				getJsonValue(executionLevelResponsePayload, "responseObject.platformStatistics.length()"));
//
//		// Open Excel
//		InputStream inputStream = new FileInputStream(excelFilePath);
//		Workbook workbook = WorkbookFactory.create(inputStream);
//		sheet = workbook.getSheet(sheetName);
//		if (sheet == null) {
//			throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in " + excelFilePath);
//		}
//
//		for (int j = 0; j < noOfPlatforms; j++) {
//			String clientId = convertToString(getJsonValue(executionLevelResponsePayload,
//					"responseObject.platformStatistics[" + j + "].clientId"));
//			String runId = convertToString(
//					getJsonValue(executionLevelResponsePayload, "responseObject.platformStatistics[" + j + "].runId"));
//
//			Map<String, Object> executionStatus = (Map<String, Object>) getJsonValue(executionLevelResponsePayload,
//					"responseObject.executionResponses[" + j + "].executionStatusObject");
//
//			// List to store failed scripts
//			List<String> failedScripts = new LinkedList<String>();
//
//			// Iterate over key-value pairs
//			for (Map.Entry<String, Object> entry : executionStatus.entrySet()) {
//				if (entry.getKey().toString().startsWith("SCR") && entry.getValue().equals("FAIL")) {
//					failedScripts.add(entry.getKey());
//				}
//			}
//
//			createdDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
//
//			// System.out.println(failedScripts);
//			for (int i = 0; i < failedScripts.size(); i++) {
//				scriptId = failedScripts.get(i);
//				scriptFolderPath = createDirectory(executionFolderPath, "Screenshots" + File.separator + scriptId);
//
//				String scriptLevelResponsePayload = getRequest(
//						"executionresult/optimize/v1/executionResponse/result/" + executionId + "/" + clientId + "/"
//								+ runId + "/" + scriptId,
//								"Authorization", "Bearer " + token, "projectId", projectId, "projectType", projectType);
//				scriptName = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.title"));
//				module = convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.path"));
//				module = module.substring(0, module.lastIndexOf("/"));
//
//				// Handle Depends on script
//				try {
//					List<Map<String, Object>> dependsOnScripts = (List<Map<String, Object>>) getJsonValue(
//							scriptLevelResponsePayload, "responseObject.dependantExecutionEntityResponses");
//					if (dependsOnScripts != null) {
//						listOfSteps.add("dependsOnScript:");
//						for (Map<String, Object> script : dependsOnScripts) {
//							String dependScriptId = script.get("key").toString();
//
//							String dependScriptLevelResponsePayload = getRequest(
//									"executionresult/optimize/v1/executionResponse/result/" + executionId + "/"
//											+ clientId + "/" + runId + "/" + dependScriptId,
//											"Authorization", "Bearer " + token, "projectId", projectId, "projectType",
//											projectType);
//
//							addStepsToList(dependScriptLevelResponsePayload, "responseObject.preConditions.stepResults");
//							if (!isFirstBugAdded) {
//								addStepsToList(dependScriptLevelResponsePayload, "responseObject.stepResults");
//								if (!isFirstBugAdded) {
//									addStepsToList(dependScriptLevelResponsePayload,"responseObject.postConditions.stepResults");
//								}
//							}
//						}
//					}
//				} catch (Exception e) {
//				}
//
//				listOfSteps.add("script:");
//				addStepsToList(scriptLevelResponsePayload, "responseObject.preConditions.stepResults");
//				if (!isFirstBugAdded) {
//					addStepsToList(scriptLevelResponsePayload, "responseObject.stepResults");
//					if (!isFirstBugAdded) {
//						addStepsToList(scriptLevelResponsePayload,"responseObject.postConditions.stepResults");
//					}
//				}
//				// Clear the list
//				listOfSteps.clear();
//				isFirstBugAdded = false;
//			}
//		}
//
//		// Close Excel
//		// Write the changes to the Excel file
//		try (OutputStream outputStream = new FileOutputStream(excelFilePath)) {
//			workbook.write(outputStream);
//		}
//	}
//
//	private static void addStepsToList(String scriptLevelResponsePayload, String jsonPath) {
//		List<Map<String, Object>> stepResults;
//		try {
//			stepResults = (List<Map<String, Object>>) getJsonValue(scriptLevelResponsePayload, jsonPath);
//			listOfSteps.add(jsonPath.split("\\.")[1]+":");
//			if (stepResults == null) {
//				stepResults = List.of();
//			}
//			iterateStepResultsAndAddToSet(stepResults, 0);
//		} catch (Exception e) {
//			stepResults = List.of();
//		}
//	}
//
//	private static String getRequest(String endpoint, String... headers) {
//		RequestSpecification request = RestAssured.given();
//
//		// Add headers if provided (expects key-value pairs)
//		if (headers.length % 2 != 0) {
//			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
//		}
//
//		for (int i = 0; i < headers.length; i += 2) {
//			request.header(headers[i], headers[i + 1]);
//		}
//		return request.when().get(endpoint).asString();
//	}
//
//	private static String postRequest(String endpoint, String requestBody, String... headers) {
//		RequestSpecification request = RestAssured.given();
//
//		// Add headers if provided (expects key-value pairs)
//		if (headers.length % 2 != 0) {
//			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
//		}
//
//		for (int i = 0; i < headers.length; i += 2) {
//			request.header(headers[i], headers[i + 1]);
//		}
//		return request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint).asString();
//	}
//
//	private static String putRequest(String endpoint, String requestBody, String... headers) {
//		RequestSpecification request = RestAssured.given();
//
//		// Add headers if provided (expects key-value pairs)
//		if (headers.length % 2 != 0) {
//			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
//		}
//
//		for (int i = 0; i < headers.length; i += 2) {
//			request.header(headers[i], headers[i + 1]);
//		}
//		return request.body(requestBody).contentType(ContentType.JSON).when().put(endpoint).asString();
//	}
//
//	public static Object getJsonValue(String jsonBody, String jsonPath) {
//		return JsonPath.read(jsonBody, "$." + jsonPath);
//	}
//
//	public static boolean convertToBoolean(Object obj) {
//		return (boolean) obj;
//	}
//
//	public static int convertToInteger(Object obj) {
//		return (int) obj;
//	}
//
//	public static String convertToString(Object obj) {
//		return (String) obj;
//	}
//
//	public static double convertToDouble(Object obj) {
//		return (double) obj;
//	}
//
//	public static String encryptPassword(String password) throws Exception {
//		byte[] publicKeyBytes = Base64.getDecoder().decode(
//				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
//		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
//		return Base64.getEncoder().encodeToString(encryptedBytes);
//	}
//
//	public static void writeDataToExcelUsingColumnHeader(Sheet sheet, int rowNumber, String columnHeader, String data)
//			throws Exception {
//
//		DataFormatter dataFormatter = new DataFormatter();
//		Row headerRow = sheet.getRow(0);
//		if (headerRow == null) {
//			throw new IllegalStateException("Header row is missing in the sheet.");
//		}
//
//		int columnIndex = -1;
//
//		// Find the index of the specified column
//		for (int j = 0; j < headerRow.getLastCellNum(); j++) {
//			String headerValue = dataFormatter.formatCellValue(headerRow.getCell(j)).trim();
//			if (headerValue.equalsIgnoreCase(columnHeader)) {
//				columnIndex = j;
//				break;
//			}
//		}
//
//		if (columnIndex == -1) {
//			throw new IllegalArgumentException("Column '" + columnHeader + "' not found in sheet.");
//		}
//
//		// Get the target row and create cell if necessary
//		Row targetRow = sheet.getRow(rowNumber);
//		if (targetRow == null) {
//			targetRow = sheet.createRow(rowNumber);
//		}
//
//		Cell cell = targetRow.createCell(columnIndex, CellType.STRING);
//		cell.setCellValue(data);
//	}
//
//	public static void writeBugDetailsToExcelRow() throws Exception {
//		String failedStepResponsePayload = getRequest(
//				"executionresult/optimize/v1/executionResponse/step/" + failedStepId, "Authorization",
//				"Bearer " + token, "projectId", projectId);
//
//		String failedStep = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.name"));
//		String stepId = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.stepId"));
//		String screenShotPath;
//		try {
//			screenShotPath = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.screenshotPath"));
//		} catch (Exception e) {
//			screenShotPath = "";
//		}
//
//		String cause = "";
//		try {
//			cause = convertToString(getJsonValue(failedStepResponsePayload, "responseObject.errorLog.cause"));
//		} catch (Exception e) {
//			cause = "None";
//		}
//
//		String attachmentPath = "";
//		// Attach Screenshot
//		if (screenShotPath.isEmpty()) {
//			attachmentPath = "NA";
//		} else {
//			String requestBody = "{\"bucketName\":\"flinko-prod\",\"filePath\":[\""+screenShotPath+"\"],\"type\":\"cloudS3\",\"contentType\":\"screenshot\"}";
//			attachmentPath = addScreenshotToFolder("dashboardexecution/optimize/v1/dashboard/download/storage", requestBody, token, projectId, scriptFolderPath, stepId);
//		}
//
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Sl No", Integer.toString(slNoAndRowNumber));
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Module", module);
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Test case", scriptName);
//		if (failedIteration!=null) {
//			writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Step/ Sub Step of issue occurrence", "Failed Iteraion = "+(convertToInteger(failedIteration)+1)+"\n"+failedStep);
//		} else {
//			writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Step/ Sub Step of issue occurrence", failedStep);
//		}
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Steps To reproduce", processSteps(listOfSteps));
//		if (cause.contains("Exception")) {
//			writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Issue/ Error Description", cause.split("Exception", 2)[0]);
//		} else {
//			writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Issue/ Error Description", cause);
//		}
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Created Date", createdDate);
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Attachment of Screenshot", attachmentPath);
//		writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, "Defect Status", "New");
//		if (!variableName.isEmpty() && !variableType.isEmpty()) {
//			writeDataToExcelUsingColumnHeader(sheet, slNoAndRowNumber, variableName, variableValue);
//		}
//	}
//
//	public static void iterateStepResultsAndAddToSet(List<Map<String, Object>> jsonArray, int spaces) throws Exception {
//		for (Map<String, Object> map : jsonArray) {
//			int iteration = 0;
//			try {
//				iteration = convertToInt(map.get("iteration"));
//			} catch (Exception e) {
//			}
//			
//			if (map.get("type").equals("Group") && iteration > 0) {
//				getFailedIteration(jsonArray);
//				break;
//			} else if(map.get("type").equals("step") && iteration > 0) {
//				getFailedIteration(jsonArray);
//				break;
//			} else if(map.get("type").equals("Iteration")) {
//				iterationsList = (List<Map<String, Object>>) map.get("stepResults");
//				int iterationsCount = convertToInt(iterationsList.get(iterationsList.size()-1).get("iteration"))+1;
//				listOfSteps.add(spaces + "#" + map.get("title").toString() + "(Iterations = "+iterationsCount+")");
//			} else if(map.get("type").equals("Group") || map.get("type").equals("EndIteration")) {
//				listOfSteps.add(spaces + "#" + map.get("title").toString() + "(Group)");
//			} else if(map.get("type").equals("step"))  {
//				listOfSteps.add(spaces + "#" + map.get("title").toString() + "(Step)");
//			}
//
////			if (map.get("type").equals("Group") && map.get("status").equals("FAIL")) {
////				try {
////					failedIteration = convertToInt(map.get("iteration"));
////				} catch (Exception e) {
////				}
////			}
//
//			// If current step is failed, break and return true
//			if (map.get("type").equals("step") && map.get("status").equals("FAIL")) {
//				failedStepId = map.get("uniqueId").toString();
//				if (scope.equals("OnlyFirstFailedStep") && !isFirstBugAdded) {
//					writeBugDetailsToExcelRow();
//					slNoAndRowNumber++;
//					isFirstBugAdded = true;
//					break;
//				} else if(scope.equals("AllFailedSteps")) {
//					writeBugDetailsToExcelRow();
//					slNoAndRowNumber++;
//				}
//			}
//			if (isItrCompleted) {
//				isItrCompleted = false;
//				break;
//			}
//			
//			// If stepResults exist, recurse and break on failure
//			if (map.containsKey("stepResults")) {
//				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("stepResults");
//				iterateStepResultsAndAddToSet(subJsonArray, spaces + 1);
//			}
//		}
//	}
//
//	public static String processSteps(List<String> listOfSteps) {
//		Map<Integer, Integer> numMap = new LinkedHashMap<Integer, Integer>();
//		int number = 1;
//		int restart = 1;
//		String steps = "";
//		for (int j = 0; j < listOfSteps.size(); j++) {
//			String step = listOfSteps.get(j);
//			if(j != 0 && (listOfSteps.get(j-1).equals("preConditions:") || listOfSteps.get(j-1).equals("stepResults:") || listOfSteps.get(j-1).equals("postConditions:"))) {
//				number = restart;
//				numMap.put(Integer.parseInt(step.split("#")[0]), number);
//			} else if(!step.equals("dependsOnScript:") && !step.equals("script:") && !step.equals("preConditions:") && !step.equals("stepResults:") && !step.equals("postConditions:") ) {
//				if (numMap.containsKey(Integer.parseInt(step.split("#")[0])) && Integer.parseInt(listOfSteps.get(j-1).split("#")[0]) >= Integer.parseInt(listOfSteps.get(j).split("#")[0])) {
//					number = numMap.get(Integer.parseInt(step.split("#")[0])) + 1;
//					numMap.put(Integer.parseInt(step.split("#")[0]), number);
//				} else if (numMap.containsKey(Integer.parseInt(step.split("#")[0])) && Integer.parseInt(listOfSteps.get(j-1).split("#")[0]) < Integer.parseInt(listOfSteps.get(j).split("#")[0])) {
//					number = restart;
//					numMap.put(Integer.parseInt(step.split("#")[0]), number);
//				} else {
//					number = restart;
//					numMap.put(Integer.parseInt(step.split("#")[0]), number);
//				}
//			}
//			String spaces = "";
//			try {
//				int noOfSpaces = Integer.parseInt(step.split("#")[0]);
//				for (int i = 0; i < noOfSpaces; i++) {
//					spaces = spaces + " ";
//				}
//				steps = steps + spaces + number+"." + step.split("#")[1] + "\n";
//			}catch(Exception e) {
//				steps = steps + step + "\n";
//			}
//		}
//		return steps;
//	}
//
//	public static String addScreenshotToFolder(String endPoint, String body, String token, String projectId, String folderPath, String ScriptId) {
//		Response response = RestAssured.given().header("Authorization","Bearer " + token).header("projectId", projectId).contentType(ContentType.JSON).body(body).when().post(endPoint);
//		// Check if response is successful
//		if (response.getStatusCode() == 200) {
//			// Get image bytes from response
//			byte[] imageBytes = response.asByteArray();
//
//			// Define file path
//			File outputFile = new File(folderPath + File.separator + ScriptId + ".png");
//
//			// Write the image to the specified folder
//			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
//				fos.write(imageBytes);
//				//				System.out.println("Screenshot saved successfully at: " + outputFile.getAbsolutePath());
//				return outputFile.getAbsolutePath(); // Return the absolute path
//			} catch (IOException e) {
//				System.err.println("Error saving screenshot: " + e.getMessage());
//			}
//		} else {
//			System.err.println("Failed to get screenshot. HTTP Status: " + response.getStatusCode());
//		}
//		return null;
//	}
//
//	public static String createDirectory(String parentPath, String childPath) {
//		try {
//			// Combine parent and child paths
//			Path fullPath = Paths.get(parentPath, childPath);
//
//			// Create directories if they don't exist
//			Files.createDirectories(fullPath);
//
//			// Return the absolute path
//			return fullPath.toAbsolutePath().toString();
//		} catch (IOException e) {
//			System.err.println("Failed to create directory: " + parentPath + "\\" + childPath);
//			e.printStackTrace();
//			return null; // Return null in case of failure
//		}
//	}
//
//	public static String createExcelFile(String directory, String workbookName, String sheetName, String headers) {
//		String filePath = directory + File.separator + workbookName + ".xlsx";  // Full file path
//
//		try (Workbook workbook = new XSSFWorkbook()) {
//			Sheet sheet = workbook.createSheet(sheetName);
//			Row row = sheet.createRow(0);
//
//			// Create Cell Style for Dark Blue Background with Borders
//			CellStyle style = workbook.createCellStyle();
//			style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
//			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//			style.setAlignment(HorizontalAlignment.CENTER);
//			style.setVerticalAlignment(VerticalAlignment.CENTER);
//
//			// Set Font Color (White) for better visibility
//			Font font = workbook.createFont();
//			font.setColor(IndexedColors.WHITE.getIndex());
//			font.setBold(true);
//			style.setFont(font);
//
//			// Set Borders for Header Cells
//			style.setBorderTop(BorderStyle.THIN);
//			style.setBorderBottom(BorderStyle.THIN);
//			style.setBorderLeft(BorderStyle.THIN);
//			style.setBorderRight(BorderStyle.THIN);
//
//			// Write Headers to First Row
//			String[] headerArray = headers.split(",");
//			for (int i = 0; i < headerArray.length; i++) {
//				Cell cell = row.createCell(i);
//				cell.setCellValue(headerArray[i].trim()); // Trim spaces
//				cell.setCellStyle(style);
//				sheet.autoSizeColumn(i);
//			}
//
//			// Create directory if not exists
//			File dir = new File(directory);
//			if (!dir.exists()) dir.mkdirs();
//
//			// Save Excel File
//			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
//				workbook.write(fileOut);
//			}
//
//			//			System.out.println("Excel file created successfully at: " + filePath);
//			return filePath;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	public static int convertToInt(Object value) {
//		if (value instanceof Double) {
//			return ((Double) value).intValue();
//		} else if (value instanceof Integer) {
//			return (int) value;
//		} else if (value instanceof String) {
//			try {
//				return Integer.parseInt((String) value);
//			} catch (NumberFormatException e) {
//				try {
//					return (int) Double.parseDouble((String) value);
//				} catch (NumberFormatException ex) {
//					throw new IllegalArgumentException("Cannot convert to int: " + value);
//				}
//			}
//		} else {
//			throw new IllegalArgumentException("Unsupported type: " + value);
//		}
//	}
//
//	public static String getVariableValue(String token, String projectId) {
//		String variableValue = "";
//		String variableDetailResponse = "";
//		if (variableType.equals("GLOBAL"))
//			variableDetailResponse = getRequest("project/optimize/v1/variable/type/GLOBAL",
//					"Authorization", "Bearer "+token, "projectId", projectId);
//		else if (variableType.equals("PROJECT_ENVIRONMENT"))
//			variableDetailResponse = getRequest("project/optimize/v1/variable/type/PROJECT_ENVIRONMENT",
//					"Authorization", "Bearer "+token, "projectId", projectId);
//		else {
//			System.err.println("Invalid Variable Type");
//		}
//
//		List<Map> variables = (List<Map>) getJsonValue(variableDetailResponse, "responseObject");
//		for (Map variableMap : variables) {
//			if (variableMap.get("name").equals(variableName)) {
//				variableValue = (String) variableMap.get("value");
//				//				System.out.println("Variable Value is " + variableValue);
//			}
//		}
//		System.err.println("Variable: " + variableName + " Not Available in " + variableType + " Variables");
//		return variableValue;
//
//	}
//	
//	public static void getFailedIteration(List<Map<String, Object>> jsonArray) throws Exception {
//		for (Map<String, Object> map : jsonArray) {
//			// If current step is failed, break and return true
//			if (map.get("type").equals("step") && map.get("status").equals("FAIL")) {
//				failedStepId = map.get("uniqueId").toString();
//				if (scope.equals("OnlyFirstFailedStep") && !isFirstBugAdded) {
//					failedIteration = convertToInt(map.get("iteration"));
//					writeBugDetailsToExcelRow();
//					slNoAndRowNumber++;
//					isFirstBugAdded = true;
//					isItrCompleted = true;
//					break;
//				} else if(scope.equals("AllFailedSteps")) {
//					failedIteration = convertToInt(map.get("iteration"));
//					writeBugDetailsToExcelRow();
//					slNoAndRowNumber++;
//					isItrCompleted = true;
//				}
//			}
//				
//			// If stepResults exist, recurse and break on failure
//			if (map.containsKey("stepResults")) {
//				List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get("stepResults");
//				getFailedIteration(subJsonArray);
//			}
//		}
//	}
//
//
//}
package default_package;

