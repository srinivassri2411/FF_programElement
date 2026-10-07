package generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateReport_workingCode_NoThread {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static void run(String url, String licenseId, String passtoken, String projectType, String userName)
			throws Exception {

		try {
			System.out.println("=================================================");
			System.out.println("Starting Generate Report");
			System.out.println("=================================================");

			System.out.println("Authenticating user...");

			Map<String, String> result = FireflinkAPIs.getAccessToken(
					url + "/appmanagement/optimize/v1/public/user/plugins-signin", userName, passtoken, licenseId);

			String token = result.get("accessToken");
			String licenseType = result.get("licenseType");
			String userId = result.get("userId");

			if (token == null || token.isBlank()) {
				System.err.println("Authentication failed. Access token is empty.");
				throw new ErrorHandler("SignIn Failed");
			}

			System.out.println("Authentication successful.");
			System.out.println("User ID: " + userId);
			System.out.println("License Type: " + licenseType);

			String scriptDetailsExcelPath = createWorkbook("ScriptDetails_");

			String sgDetailsExcelPath = createWorkbook("StepGroupDetails_");

			System.out.println("Script details workbook: " + scriptDetailsExcelPath);

			System.out.println("Step group details workbook: " + sgDetailsExcelPath);

			System.out.println("Fetching projects...");

			Map<String, String> projectMap = getAllProjectIDs(url, userId, token, licenseType);

			System.out.println("Total projects found: " + projectMap.size());

			if (projectMap.isEmpty()) {
				System.err.println("No projects found.");
				throw new ErrorHandler("No projects found for the user.");
			}

			int projectCount = 0;

			for (Map.Entry<String, String> entry : projectMap.entrySet()) {

				String projectName = entry.getKey();
				String projectId = entry.getValue();

				projectCount++;

				System.out.println();
				System.out.println("-------------------------------------------------");
				System.out.println("Processing project " + projectCount + " of " + projectMap.size());
				System.out.println("Project Name: " + projectName);
				System.out.println("Project ID: " + projectId);
				System.out.println("-------------------------------------------------");

				try {
					System.out.println("Fetching Script details for project: " + projectName);

					getScriptDetails(url, projectId, licenseId, token, scriptDetailsExcelPath, projectName);

					System.out.println("Script details completed for project: " + projectName);

				} catch (Exception e) {
					System.err.println("Failed to generate Script details for project: " + projectName);

					System.err.println("Reason: " + e.getMessage());
				}

				try {
					System.out.println("Fetching Step Group details for project: " + projectName);

					getSGDetails(url, projectId, licenseId, token, licenseType, projectType, projectName,
							sgDetailsExcelPath);

					System.out.println("Step Group details completed for project: " + projectName);

				} catch (Exception e) {
					System.err.println("Failed to generate Step Group details for project: " + projectName);

					System.err.println("Reason: " + e.getMessage());
				}
			}

			System.out.println();
			System.out.println("=================================================");
			System.out.println("Generate Report completed.");
			System.out.println("Script Details: " + scriptDetailsExcelPath);
			System.out.println("Step Group Details: " + sgDetailsExcelPath);
			System.out.println("=================================================");

		} catch (ErrorHandler e) {
			throw e;

		} catch (Exception e) {
			System.err.println("Generate Report failed: " + e.getMessage());

			throw new ErrorHandler(e.getMessage() != null ? e.getMessage() : "Failed to generate report");
		}
	}

	public static String createWorkbook(String workbookName) throws IOException {

		String userHome = System.getProperty("user.home");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

		String dateTime = LocalDateTime.now().format(formatter);

		if (workbookName.endsWith(".xlsx")) {
			workbookName = workbookName.substring(0, workbookName.length() - 5);
		}

		String fileName = workbookName + "_" + dateTime + ".xlsx";

		File file = new File(userHome, fileName);

		System.out.println("Creating workbook: " + file.getAbsolutePath());

		try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream outputStream = new FileOutputStream(file)) {
			workbook.createSheet("Sheet1");
			workbook.write(outputStream);
		}

		System.out.println("Workbook created successfully.");

		return file.getAbsolutePath();
	}

	public static void deleteSheetByName(Workbook workbook, String sheetName) {

		int sheetIndex = workbook.getSheetIndex(sheetName);

		if (sheetIndex != -1) {
			workbook.removeSheetAt(sheetIndex);

			System.out.println("Deleted sheet: " + sheetName);
		}
	}

	private static Map<String, String> getAllProjectIDs(String baseUrl, String userId, String token, String licenseType)
			throws IOException {

		Map<String, String> projectMap = new HashMap<>();

		String url = baseUrl + "/project/optimize/v1/projects/user/" + userId;

		System.out.println("Project API URL: " + url);

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpGet request = new HttpGet(url);

			request.setHeader(HttpHeaders.ACCEPT, "*/*");

			request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

			request.setHeader("licensetype", licenseType);

			request.setHeader("timezone", "Asia/Kolkata");

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				int statusCode = response.getStatusLine().getStatusCode();

				String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
						: "";

				System.out.println("Project API Status Code: " + statusCode);

				if (statusCode < 200 || statusCode >= 300) {

					throw new IOException(
							"Failed to get projects. HTTP Status: " + statusCode + ", Response: " + responseBody);
				}

				JsonNode rootNode = OBJECT_MAPPER.readTree(responseBody);

				JsonNode responseObject = rootNode.path("responseObject");

				if (!responseObject.isArray()) {

					throw new IOException("Expected responseObject to be an array.");
				}

				for (JsonNode item : responseObject) {

					JsonNode project = item.path("project");

					if (project.isMissingNode() || project.isNull()) {
						continue;
					}

					String projectId = project.path("id").asText();

					String projectName = project.path("name").asText();

					if (!projectId.isBlank() && !projectName.isBlank()) {

						projectMap.put(projectName, projectId);

						System.out.println("Project found: " + projectName + " [" + projectId + "]");
					}
				}
			}
		}

		return projectMap;
	}

	public static void getScriptDetails(String baseUrl, String projectId, String licenseId, String token,
			String excelPath, String sheetName) throws IOException {

		String assetUrl = baseUrl + "/customerextension/optimize/api/v3/project-assets" + "?assetType=SCRIPT"
				+ "&projectId=" + projectId + "&licenseId=" + licenseId;

		System.out.println("Script Assets API URL: " + assetUrl);

		List<ScriptDetails> scripts = new ArrayList<>();

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpGet request = new HttpGet(assetUrl);

			request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

			request.setHeader(HttpHeaders.ACCEPT, "application/json");

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				int statusCode = response.getStatusLine().getStatusCode();

				String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
						: "";

				System.out.println("Script Assets Status Code: " + statusCode);

				if (statusCode < 200 || statusCode >= 300) {

					throw new IOException(
							"Failed to get script assets. HTTP Status: " + statusCode + ", Response: " + responseBody);
				}

				JsonNode assets = OBJECT_MAPPER.readTree(responseBody);

				if (!assets.isArray()) {

					throw new IOException("Expected project-assets response to be an array.");
				}

				int assetCount = 0;

				for (JsonNode asset : assets) {

					String entityType = asset.path("entityType").asText();

					if (!"Script".equalsIgnoreCase(entityType)) {
						continue;
					}

					String key = asset.path("key").asText();

					if (key.isBlank()) {
						continue;
					}

					assetCount++;

					System.out.println("Fetching Script details for key: " + key);

					ScriptDetails scriptDetails = getScriptDetailsByKey(httpClient, baseUrl, key, projectId, token);

					if (scriptDetails != null) {

						scripts.add(scriptDetails);

						System.out.println("Script details fetched successfully: " + key);
					}
				}

				System.out.println("Total Script assets found: " + assetCount);
			}
		}

		writeScriptDetailsToExcel(excelPath, sheetName, scripts);

		System.out.println("Total scripts written to Excel for " + sheetName + ": " + scripts.size());
	}

	private static ScriptDetails getScriptDetailsByKey(CloseableHttpClient httpClient, String baseUrl, String key,
			String projectId, String token) throws IOException {

		String url = baseUrl + "/project/optimize/v1/modules/details/" + key;

		HttpGet request = new HttpGet(url);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		request.setHeader(HttpHeaders.ACCEPT, "*/*");

		request.setHeader("projectid", projectId);

		request.setHeader("sectionname", "Details");

		request.setHeader("timezone", "Asia/Kolkata");

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			int statusCode = response.getStatusLine().getStatusCode();

			String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
					: "";

			System.out.println("Script Details API Status Code for " + key + ": " + statusCode);

			if (statusCode < 200 || statusCode >= 300) {

				System.err.println("Failed to get Script details for key: " + key + ". Response: " + responseBody);

				return null;
			}

			JsonNode root = OBJECT_MAPPER.readTree(responseBody);

			JsonNode responseObject = root.path("responseObject");

			if (responseObject.isMissingNode() || responseObject.isNull()) {

				System.err.println("responseObject not found for Script key: " + key);

				return null;
			}

			ScriptDetails script = new ScriptDetails();

			script.setKey(responseObject.path("key").asText());

			script.setName(responseObject.path("name").asText());

			script.setPath(responseObject.path("path").asText());

			script.setCreatedOn(responseObject.path("createdOn").asText());

			script.setModifiedOn(responseObject.path("modifiedOn").asText());

			script.setModifiedByUname(responseObject.path("modifiedByUname").asText());

			script.setCreatedByUname(responseObject.path("createdByUname").asText());

			return script;
		}
	}

	public static void getSGDetails(String baseUrl, String projectId, String licenseId, String token,
			String licenseType, String projectType, String projectName, String excelPath) throws IOException {

		String assetUrl = baseUrl + "/customerextension/optimize/api/v3/project-assets" + "?assetType=STEP_GROUP"
				+ "&projectId=" + projectId + "&licenseId=" + licenseId;

		System.out.println("Step Group Assets API URL: " + assetUrl);

		List<StepGroupDetails> stepGroups = new ArrayList<>();

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpGet request = new HttpGet(assetUrl);

			request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

			request.setHeader(HttpHeaders.ACCEPT, "application/json");

			try (CloseableHttpResponse response = httpClient.execute(request)) {

				int statusCode = response.getStatusLine().getStatusCode();

				String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
						: "";

				System.out.println("Step Group Assets Status Code: " + statusCode);

				if (statusCode < 200 || statusCode >= 300) {

					throw new IOException("Failed to get Step Group assets. HTTP Status: " + statusCode + ", Response: "
							+ responseBody);
				}

				JsonNode assets = OBJECT_MAPPER.readTree(responseBody);

				if (!assets.isArray()) {

					throw new IOException("Expected Step Group assets response to be an array.");
				}

				int assetCount = 0;

				for (JsonNode asset : assets) {

					String entityType = asset.path("entityType").asText();

					if (!"StepGroup".equalsIgnoreCase(entityType)) {
						continue;
					}

					String key = asset.path("key").asText();

					if (key.isBlank()) {
						continue;
					}

					assetCount++;

					System.out.println("Fetching Step Group details for key: " + key);

					StepGroupDetails stepGroup = getSGDetailsByKey(httpClient, baseUrl, key, projectId, projectName,
							projectType, licenseType, token);

					if (stepGroup != null) {

						stepGroups.add(stepGroup);

						System.out.println("Step Group details fetched successfully: " + key);
					}
				}

				System.out.println("Total Step Group assets found: " + assetCount);
			}
		}

		writeStepGroupDetailsToExcel(excelPath, projectName, stepGroups);

		System.out.println("Total Step Groups written to Excel for " + projectName + ": " + stepGroups.size());
	}

	private static StepGroupDetails getSGDetailsByKey(CloseableHttpClient httpClient, String baseUrl, String key,
			String projectId, String projectName, String projectType, String licenseType, String token)
			throws IOException {

		String url = baseUrl + "/project/optimize/v1/libraries/" + key + "?resourceCount=true";

		HttpGet request = new HttpGet(url);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		request.setHeader(HttpHeaders.ACCEPT, "*/*");

		request.setHeader("licensetype", licenseType);

		request.setHeader("projectid", projectId);

		request.setHeader("projectname", projectName);

		request.setHeader("projecttype", projectType);

		request.setHeader("timezone", "Asia/Kolkata");

		System.out.println("Step Group Details API URL: " + url);

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			int statusCode = response.getStatusLine().getStatusCode();

			String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
					: "";

			System.out.println("Step Group Details API Status Code for " + key + ": " + statusCode);

			if (statusCode < 200 || statusCode >= 300) {

				System.err.println("Failed to get Step Group details for key: " + key + ". Response: " + responseBody);

				return null;
			}

			JsonNode root = OBJECT_MAPPER.readTree(responseBody);

			JsonNode responseObject = root.path("responseObject");

			if (responseObject.isMissingNode() || responseObject.isNull()) {

				System.err.println("responseObject not found for Step Group key: " + key);

				return null;
			}

			StepGroupDetails stepGroup = new StepGroupDetails();

			stepGroup.setKey(responseObject.path("key").asText());

			stepGroup.setName(responseObject.path("name").asText());

			stepGroup.setPath(responseObject.path("path").asText());

			stepGroup.setCreatedOn(responseObject.path("createdOn").asText());

			stepGroup.setModifiedOn(responseObject.path("modifiedOn").asText());

			stepGroup.setModifiedByUname(responseObject.path("modifiedByUname").asText());

			stepGroup.setCreatedByUname(responseObject.path("createdByUname").asText());

			return stepGroup;
		}
	}

	private static void writeScriptDetailsToExcel(String excelPath, String sheetName, List<ScriptDetails> scripts)
			throws IOException {

		File file = new File(excelPath);

		try (Workbook workbook = openWorkbook(file)) {

			Sheet sheet = workbook.getSheet(sheetName);

			if (sheet == null) {
				sheet = workbook.createSheet(sheetName);
			}

			deleteSheetByName(workbook, "Sheet1");

			clearSheet(sheet);

			CellStyle boldStyle = createBoldStyle(workbook);

			Row totalRow = sheet.createRow(0);

			Cell totalCell = totalRow.createCell(0);

			totalCell.setCellValue("Total number of scripts: " + scripts.size());

			totalCell.setCellStyle(boldStyle);

			String[] headers = { "Sl. No.", "Script ID/SG ID", "Script Name / SG Name", "Path", "Created On",
					"Modified On", "Modified By", "Created By" };

			createHeaderRow(sheet, boldStyle, headers);

			int rowNumber = 2;
			int serialNumber = 1;

			for (ScriptDetails script : scripts) {

				Row row = sheet.createRow(rowNumber++);

				row.createCell(0).setCellValue(serialNumber++);

				row.createCell(1).setCellValue(script.getKey());

				row.createCell(2).setCellValue(script.getName());

				row.createCell(3).setCellValue(script.getPath());

				row.createCell(4).setCellValue(script.getCreatedOn());

				row.createCell(5).setCellValue(script.getModifiedOn());

				row.createCell(6).setCellValue(script.getModifiedByUname());

				row.createCell(7).setCellValue(script.getCreatedByUname());
			}

			autoSizeColumns(sheet, headers.length);

			saveWorkbook(workbook, file);
		}

		System.out.println("Script Excel updated successfully: " + excelPath);
	}

	private static void writeStepGroupDetailsToExcel(String excelPath, String sheetName,
			List<StepGroupDetails> stepGroups) throws IOException {

		File file = new File(excelPath);

		try (Workbook workbook = openWorkbook(file)) {

			Sheet sheet = workbook.getSheet(sheetName);

			if (sheet == null) {
				sheet = workbook.createSheet(sheetName);
			}

			deleteSheetByName(workbook, "Sheet1");

			clearSheet(sheet);

			CellStyle boldStyle = createBoldStyle(workbook);

			Row totalRow = sheet.createRow(0);

			Cell totalCell = totalRow.createCell(0);

			totalCell.setCellValue("Total number of step groups: " + stepGroups.size());

			totalCell.setCellStyle(boldStyle);

			String[] headers = { "Sl. No.", "Script ID/SG ID", "Script Name / SG Name", "Path", "Created On",
					"Modified On", "Modified By", "Created By" };

			createHeaderRow(sheet, boldStyle, headers);

			int rowNumber = 2;
			int serialNumber = 1;

			for (StepGroupDetails stepGroup : stepGroups) {

				Row row = sheet.createRow(rowNumber++);

				row.createCell(0).setCellValue(serialNumber++);

				row.createCell(1).setCellValue(stepGroup.getKey());

				row.createCell(2).setCellValue(stepGroup.getName());

				row.createCell(3).setCellValue(stepGroup.getPath());

				row.createCell(4).setCellValue(stepGroup.getCreatedOn());

				row.createCell(5).setCellValue(stepGroup.getModifiedOn());

				row.createCell(6).setCellValue(stepGroup.getModifiedByUname());

				row.createCell(7).setCellValue(stepGroup.getCreatedByUname());
			}

			autoSizeColumns(sheet, headers.length);

			saveWorkbook(workbook, file);
		}

		System.out.println("Step Group Excel updated successfully: " + excelPath);
	}

	private static Workbook openWorkbook(File file) throws IOException {

		if (file.exists()) {

			try (FileInputStream fis = new FileInputStream(file)) {
				return WorkbookFactory.create(fis);
			}
		}

		return new XSSFWorkbook();
	}

	private static void saveWorkbook(Workbook workbook, File file) throws IOException {

		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
	}

	private static CellStyle createBoldStyle(Workbook workbook) {

		CellStyle boldStyle = workbook.createCellStyle();

		Font boldFont = workbook.createFont();

		boldFont.setBold(true);

		boldStyle.setFont(boldFont);

		return boldStyle;
	}

	private static void createHeaderRow(Sheet sheet, CellStyle boldStyle, String[] headers) {

		Row headerRow = sheet.createRow(1);

		for (int i = 0; i < headers.length; i++) {

			Cell cell = headerRow.createCell(i);

			cell.setCellValue(headers[i]);

			cell.setCellStyle(boldStyle);
		}
	}

	private static void autoSizeColumns(Sheet sheet, int columnCount) {

		for (int i = 0; i < columnCount; i++) {

			sheet.autoSizeColumn(i);
		}
	}

	private static void clearSheet(Sheet sheet) {

		for (int i = sheet.getLastRowNum(); i >= 0; i--) {

			Row row = sheet.getRow(i);

			if (row != null) {
				sheet.removeRow(row);
			}
		}
	}

	public static class ScriptDetails {

		private String key;
		private String name;
		private String path;
		private String createdOn;
		private String modifiedOn;
		private String modifiedByUname;
		private String createdByUname;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(String createdOn) {
			this.createdOn = createdOn;
		}

		public String getModifiedOn() {
			return modifiedOn;
		}

		public void setModifiedOn(String modifiedOn) {
			this.modifiedOn = modifiedOn;
		}

		public String getModifiedByUname() {
			return modifiedByUname;
		}

		public void setModifiedByUname(String modifiedByUname) {
			this.modifiedByUname = modifiedByUname;
		}

		public String getCreatedByUname() {
			return createdByUname;
		}

		public void setCreatedByUname(String createdByUname) {
			this.createdByUname = createdByUname;
		}
	}

	public static class StepGroupDetails {

		private String key;
		private String name;
		private String path;
		private String createdOn;
		private String modifiedOn;
		private String modifiedByUname;
		private String createdByUname;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(String createdOn) {
			this.createdOn = createdOn;
		}

		public String getModifiedOn() {
			return modifiedOn;
		}

		public void setModifiedOn(String modifiedOn) {
			this.modifiedOn = modifiedOn;
		}

		public String getModifiedByUname() {
			return modifiedByUname;
		}

		public void setModifiedByUname(String modifiedByUname) {
			this.modifiedByUname = modifiedByUname;
		}

		public String getCreatedByUname() {
			return createdByUname;
		}

		public void setCreatedByUname(String createdByUname) {
			this.createdByUname = createdByUname;
		}
	}
}
