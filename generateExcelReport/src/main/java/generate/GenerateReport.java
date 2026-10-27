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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.security.auth.login.AccountNotFoundException;

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

public class GenerateReport {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static void run(String url, String licenseId, String passtoken, String userName) throws Exception {
		System.out.println("Start time" + LocalDateTime.now());
		try {

			Map<String, String> result = FireflinkAPIs.getAccessToken(
					url + "/appmanagement/optimize/v1/public/user/plugins-signin", userName, passtoken, licenseId);

			String token = result.get("accessToken");
			String licenseType = result.get("licenseType");
			String userId = result.get("userId");

			if (token == null || token.isBlank()) {
				throw new ErrorHandler("SignIn Failed");
			}

			String scriptDetailsExcelPath = createWorkbook(licenseId, "ScriptDetails_");

			String sgDetailsExcelPath = createWorkbook(licenseId, "StepGroupDetails_");

			Map<String, String> projectMap = getAllProjectIDs(url, userId, token, licenseType);

			System.out.println("Projects: " + projectMap);

			if (projectMap.isEmpty()) {
				throw new AccountNotFoundException("Projects are not found please check");
			}

			ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {

				Future<?> scriptFuture = executorService.submit(() -> {

					try {

						System.out.println("Script Details thread started.");

						try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

							for (Map.Entry<String, String> entry : projectMap.entrySet()) {

								String projectName = entry.getKey();
								String projectId = entry.getValue();

								System.out.println(
										"Fetching Script Details for project: " + projectName + " [" + projectId + "]");

								getScriptDetails(httpClient, url, projectId, licenseId, token, scriptDetailsExcelPath,
										projectName);
							}
						}

						System.out.println("Script Details thread completed.");

					} catch (Exception e) {

						System.err.println("Script Details thread failed: " + e.getMessage());

						throw new RuntimeException(e);
					}
				});

				Future<?> sgFuture = executorService.submit(() -> {

					try {

						System.out.println("Step Group Details thread started.");

						try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

							for (Map.Entry<String, String> entry : projectMap.entrySet()) {

								String projectName = entry.getKey();
								String projectId = entry.getValue();

								System.out.println("Fetching Step Group Details for project: " + projectName + " ["
										+ projectId + "]");

								getSGDetails(httpClient, url, projectId, licenseId, token, sgDetailsExcelPath,
										projectName, licenseType);
							}
						}

						System.out.println("Step Group Details thread completed.");

					} catch (Exception e) {

						System.err.println("Step Group Details thread failed: " + e.getMessage());

						throw new RuntimeException(e);
					}
				});

				try {

					scriptFuture.get();
					sgFuture.get();

				} catch (ExecutionException e) {

					Throwable cause = e.getCause();

					if (cause != null) {

						System.err.println("Parallel processing failed: " + cause.getMessage());

						throw new ErrorHandler(cause.getMessage());
					}

					throw new ErrorHandler("Parallel processing failed.");
				}

			} finally {

				executorService.shutdown();
			}
			System.out.println("End time" + LocalDateTime.now());
			System.out.println("Script Details and Step Group Details processing completed successfully. \n\n"
					+ "Please find the file paths:\n");
			System.out.println("Step Group Details Excel: " + sgDetailsExcelPath);
			System.out.println("Script Details Excel: " + scriptDetailsExcelPath);
		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();

			System.err.println("Report generation interrupted.");
			System.out.println("End time" + LocalDateTime.now());
			throw new ErrorHandler("Report generation was interrupted.");

		} catch (ErrorHandler e) {
			System.out.println("End time" + LocalDateTime.now());
			throw e;

		} catch (Exception e) {

			System.err.println("Report generation failed: " + e.getMessage());
			System.out.println("End time" + LocalDateTime.now());
			throw new ErrorHandler(e.getLocalizedMessage());
		}
	}

	public static String createWorkbook(String licenseId, String workbookName) throws IOException {

		String userHome = System.getProperty("user.home");

		File reportsFolder = new File(userHome, "Acoe Reports");

		if (!reportsFolder.exists()) {
			reportsFolder.mkdirs();
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String dateTime = LocalDateTime.now().format(formatter);

		String fileName = licenseId + "_" + workbookName + "__" + dateTime + ".xlsx";

		File file = new File(reportsFolder, fileName);

		try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream outputStream = new FileOutputStream(file)) {

			workbook.createSheet("Sheet1");
			workbook.write(outputStream);
		}

		return file.getAbsolutePath();
	}

	public static void deleteSheetByName(Workbook workbook, String sheetName) {

		int sheetIndex = workbook.getSheetIndex(sheetName);

		if (sheetIndex != -1) {
			workbook.removeSheetAt(sheetIndex);
		}
	}

	private static Map<String, String> getAllProjectIDs(String baseUrl, String userId, String token,
			String licenseType) {

		Map<String, String> projectMap = new HashMap<>();

		String url = baseUrl + "/project/optimize/v1/projects/user/" + userId;

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

					throw new RuntimeException(
							"API call failed. Status: " + statusCode + ", Response: " + responseBody);
				}

				JsonNode rootNode = OBJECT_MAPPER.readTree(responseBody);

				JsonNode responseObject = rootNode.path("responseObject");

				if (responseObject.isArray()) {

					for (JsonNode item : responseObject) {

						JsonNode project = item.path("project");

						if (!project.isMissingNode() && !project.isNull()) {

							String projectId = project.path("id").asText();

							String projectName = project.path("name").asText();

							if (!projectId.isBlank() && !projectName.isBlank()) {

								projectMap.put(projectName, projectId);
							}
						}
					}
				}

			}

		} catch (Exception e) {

			System.err.println("Failed to get project IDs: " + e.getMessage());

			throw new RuntimeException(e);
		}

		return projectMap;
	}

	public static void getScriptDetails(CloseableHttpClient httpClient, String baseUrl, String projectId,
			String licenseId, String token, String excelPath, String sheetName) throws IOException {

		String assetUrl = baseUrl + "/customerextension/optimize/api/v3/project-assets" + "?assetType=SCRIPT"
				+ "&projectId=" + projectId + "&licenseId=" + licenseId;

		List<ScriptDetails> scripts = new ArrayList<>();

		HttpGet request = new HttpGet(assetUrl);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		request.setHeader(HttpHeaders.ACCEPT, "application/json");

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			int statusCode = response.getStatusLine().getStatusCode();

			String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
					: "";

			System.out.println("Project Assets Script Status Code: " + statusCode);

			if (statusCode < 200 || statusCode >= 300) {

				throw new IOException(
						"Failed to get project assets. HTTP Status: " + statusCode + ", Response: " + responseBody);
			}

			JsonNode assets = OBJECT_MAPPER.readTree(responseBody);

			if (!assets.isArray()) {

				throw new IOException("Expected project-assets response to be a JSON array.");
			}

			for (JsonNode asset : assets) {

				String entityType = asset.path("entityType").asText();

				if (!"Script".equalsIgnoreCase(entityType)) {
					continue;
				}

				String key = asset.path("key").asText();

				if (key == null || key.trim().isEmpty()) {
					continue;
				}

				System.out.println("Fetching script details for key: " + key);

				ScriptDetails scriptDetails = getScriptDetailsByKey(httpClient, baseUrl, key, projectId, token);

				if (scriptDetails != null) {
					scripts.add(scriptDetails);
				}
			}
		}

		writeScriptDetailsToExcel(excelPath, sheetName, scripts);

		System.out.println("Total number of scripts for " + sheetName + ": " + scripts.size());
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

			System.out.println("Script Details Status Code for " + key + ": " + statusCode);

			if (statusCode < 200 || statusCode >= 300) {

				System.err.println("Failed to get details for script: " + key + ". Response: " + responseBody);

				return null;
			}

			JsonNode root = OBJECT_MAPPER.readTree(responseBody);

			JsonNode responseObject = root.path("responseObject");

			if (responseObject.isMissingNode() || responseObject.isNull()) {

				System.err.println("responseObject not found for key: " + key);

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

	public static void getSGDetails(CloseableHttpClient httpClient, String baseUrl, String projectId, String licenseId,
			String token, String excelPath, String sheetName, String licenseType) throws IOException {

		String assetUrl = baseUrl + "/customerextension/optimize/api/v3/project-assets" + "?assetType=STEP_GROUP"
				+ "&projectId=" + projectId + "&licenseId=" + licenseId;

		List<StepGroupDetails> stepGroups = new ArrayList<>();

		HttpGet request = new HttpGet(assetUrl);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		request.setHeader(HttpHeaders.ACCEPT, "application/json");

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			int statusCode = response.getStatusLine().getStatusCode();

			String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
					: "";

			System.out.println("Project Assets Step Group Status Code: " + statusCode);

			if (statusCode < 200 || statusCode >= 300) {

				throw new IOException(
						"Failed to get step group assets. HTTP Status: " + statusCode + ", Response: " + responseBody);
			}

			JsonNode assets = OBJECT_MAPPER.readTree(responseBody);

			if (!assets.isArray()) {

				throw new IOException("Expected project-assets response to be a JSON array.");
			}

			for (JsonNode asset : assets) {

				String entityType = asset.path("entityType").asText();

				if (!"StepGroup".equalsIgnoreCase(entityType)) {
					continue;
				}

				String key = asset.path("key").asText();

				if (key == null || key.trim().isEmpty()) {
					continue;
				}

				System.out.println("Fetching step group details for key: " + key);

				StepGroupDetails details = getSGDetailsByKey(httpClient, baseUrl, key, projectId, token, licenseType);

				if (details != null) {
					stepGroups.add(details);
				}
			}
		}

		writeStepGroupDetailsToExcel(excelPath, sheetName, stepGroups);

		System.out.println("Total number of step groups for " + sheetName + ": " + stepGroups.size());
	}

	private static StepGroupDetails getSGDetailsByKey(CloseableHttpClient httpClient, String baseUrl, String key,
			String projectId, String token, String licenseType) throws IOException {

		String url = baseUrl + "/project/optimize/v1/libraries/" + key + "?resourceCount=true";

		HttpGet request = new HttpGet(url);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		request.setHeader("licensetype", licenseType);

		request.setHeader("projectid", projectId);

		request.setHeader("projecttype", "Multi-Channel");

		request.setHeader("timezone", "Asia/Kolkata");

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			int statusCode = response.getStatusLine().getStatusCode();

			String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), "UTF-8")
					: "";

			System.out.println("Step Group Details Status Code for " + key + ": " + statusCode);

			if (statusCode < 200 || statusCode >= 300) {

				System.err.println("Failed to get step group details for: " + key + ". Response: " + responseBody);

				return null;
			}

			JsonNode root = OBJECT_MAPPER.readTree(responseBody);

			JsonNode responseObject = root.path("responseObject");

			if (responseObject.isMissingNode() || responseObject.isNull()) {

				System.err.println("responseObject not found for step group key: " + key);

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

		Workbook workbook;

		if (file.exists()) {

			try (FileInputStream fis = new FileInputStream(file)) {

				workbook = WorkbookFactory.create(fis);
			}

		} else {

			workbook = new XSSFWorkbook();
		}

		try (Workbook wb = workbook) {

			Sheet sheet = wb.getSheet(sheetName);

			if (sheet == null) {
				sheet = wb.createSheet(sheetName);
			}

			deleteSheetByName(wb, "Sheet1");

			clearSheet(sheet);

			CellStyle boldStyle = createBoldStyle(wb);

			Row totalRow = sheet.createRow(0);

			Cell totalCell = totalRow.createCell(0);

			totalCell.setCellValue("Total number of script: " + scripts.size());

			totalCell.setCellStyle(boldStyle);

			Row headerRow = sheet.createRow(1);

			String[] headers = { "Sl. No.", "Script ID/SG ID", "Script Name / SG Name", "Path", "Created On",
					"Modified On", "Modified By", "Created By" };

			createHeaders(headerRow, headers, boldStyle);

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

			try (FileOutputStream fos = new FileOutputStream(file)) {

				wb.write(fos);
			}
		}
	}

	private static void writeStepGroupDetailsToExcel(String excelPath, String sheetName,
			List<StepGroupDetails> stepGroups) throws IOException {

		File file = new File(excelPath);

		Workbook workbook;

		if (file.exists()) {

			try (FileInputStream fis = new FileInputStream(file)) {

				workbook = WorkbookFactory.create(fis);
			}

		} else {

			workbook = new XSSFWorkbook();
		}

		try (Workbook wb = workbook) {

			Sheet sheet = wb.getSheet(sheetName);

			if (sheet == null) {
				sheet = wb.createSheet(sheetName);
			}

			deleteSheetByName(wb, "Sheet1");

			clearSheet(sheet);

			CellStyle boldStyle = createBoldStyle(wb);

			Row totalRow = sheet.createRow(0);

			Cell totalCell = totalRow.createCell(0);

			totalCell.setCellValue("Total number of step groups: " + stepGroups.size());

			totalCell.setCellStyle(boldStyle);

			Row headerRow = sheet.createRow(1);

			String[] headers = { "Sl. No.", "Script ID/SG ID", "Script Name / SG Name", "Path", "Created On",
					"Modified On", "Modified By", "Created By" };

			createHeaders(headerRow, headers, boldStyle);

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

			try (FileOutputStream fos = new FileOutputStream(file)) {

				wb.write(fos);
			}
		}
	}

	private static CellStyle createBoldStyle(Workbook workbook) {

		CellStyle boldStyle = workbook.createCellStyle();

		Font boldFont = workbook.createFont();

		boldFont.setBold(true);

		boldStyle.setFont(boldFont);

		return boldStyle;
	}

	private static void createHeaders(Row headerRow, String[] headers, CellStyle boldStyle) {

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
