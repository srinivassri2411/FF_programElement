
package jiraConfluence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraConfluenceToExcel implements Nlp {
	@InputParams({ @InputParam(name = "Confluence PageId", type = "java.lang.String"),
			@InputParam(name = "Jira EmailID", type = "java.lang.String"),
			@InputParam(name = "Jira Token", type = "java.lang.String") })
	@ReturnType(name = "excelPath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String confluencePageId = (String) programElementsInput.get("Confluence PageId");
		String jiraEmailID = (String) programElementsInput.get("Jira EmailID");
		String jiraToken = (String) programElementsInput.get("Jira Token");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String pageId = extractPageId(confluencePageId);
			log.info("Using Page ID: " + pageId);

			String json = fetchPageJson(pageId, jiraEmailID, jiraToken);
			log.info("RAW JSON RESPONSE:\n" + json);

			JSONObject obj = new JSONObject(json);

			String xhtml;
			if (obj.getJSONObject("body").has("storage")) {
				xhtml = obj.getJSONObject("body").getJSONObject("storage").getString("value");
			} else {
				xhtml = obj.getJSONObject("body").getJSONObject("atlas_doc_format").getString("value");
			}

			log.info("Extracted HTML Body:\n" + xhtml);
			returnValue = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
					+ "Extracted Excel_" + new SimpleDateFormat("dd_MM_yy_hh_mm_ss").format(new Date()) + ".xlsx";

			convertHtmlTableToExcel(xhtml, returnValue);
			log.info("Excel created successfully at ConfluenceV2Tables.xlsx");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("excelPath", returnValue);
		return nlpResponseModel;
	}

	// Extract pageId from a Confluence URL
	public static String extractPageId(String url) {
		if (url.contains("pageId=")) {
			return url.substring(url.indexOf("pageId=") + 7);
		}
		if (url.contains("/pages/")) {
			return url.substring(url.lastIndexOf("/") + 1);
		}
		return url;
	}

	// Fetch JSON from Confluence API
	public static String fetchPageJson(String pageId, String email, String apiToken) throws IOException {
		String apiUrl = "https://fireflink-team-yo1fgdldemo.atlassian.net/wiki/api/v2/pages/" + pageId
				+ "?body-format=storage";

		URL url = new URL(apiUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		String auth = email + ":" + apiToken;
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
				StringBuilder errorMsg = new StringBuilder();
				String line;
				while ((line = err.readLine()) != null) {
					errorMsg.append(line);
				}
				throw new RuntimeException("HTTP " + conn.getResponseCode() + " : " + errorMsg);
			}
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			StringBuilder sb = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			return sb.toString();
		}
	}

	public static void convertHtmlTableToExcel(String html, String outputExcelPath) throws IOException {
		Document doc = Jsoup.parse(html);
		Elements tables = doc.select("table");
		if (tables.isEmpty()) {
			throw new IllegalArgumentException("No tables found in HTML content.");
		}

		Workbook workbook = new XSSFWorkbook();
		int tableCount = 1;

		for (Element table : tables) {
			Sheet sheet = workbook.createSheet("Table_" + tableCount++);
			Elements rows = table.select("tr");
			int rowIndex = 0;

			// Track occupied cells due to rowspan
			Map<String, String> spannedCells = new HashMap<>();

			for (Element row : rows) {
				Row excelRow = sheet.getRow(rowIndex);
				if (excelRow == null)
					excelRow = sheet.createRow(rowIndex);

				Elements cells = row.select("td,th");
				int colIndex = 0;

				for (Element cell : cells) {
					// Skip already filled cells due to rowspan
					while (spannedCells.containsKey(rowIndex + "_" + colIndex)) {
						String value = spannedCells.remove(rowIndex + "_" + colIndex);
						excelRow.createCell(colIndex).setCellValue(value);
						colIndex++;
					}

					String value = cell.text().trim();
					int rowspan = cell.hasAttr("rowspan") ? Integer.parseInt(cell.attr("rowspan")) : 1;
					int colspan = cell.hasAttr("colspan") ? Integer.parseInt(cell.attr("colspan")) : 1;

					// Write value
					Cell excelCell = excelRow.createCell(colIndex);
					excelCell.setCellValue(value);

					// Handle rowspan propagation
					if (rowspan > 1) {
						for (int r = 1; r < rowspan; r++) {
							spannedCells.put((rowIndex + r) + "_" + colIndex, value);
						}
					}

					// Merge visually if needed
					if (rowspan > 1 || colspan > 1) {
						int lastRow = rowIndex + rowspan - 1;
						int lastCol = colIndex + colspan - 1;
						sheet.addMergedRegion(new CellRangeAddress(rowIndex, lastRow, colIndex, lastCol));
					}

					colIndex += colspan;
				}

				rowIndex++;
			}

			// Auto size
			for (int i = 0; i < 20; i++) {
				sheet.autoSizeColumn(i);
			}
		}

		try (FileOutputStream fos = new FileOutputStream(outputExcelPath)) {
			workbook.write(fos);
		}
		workbook.close();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String excel = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
				+ "Extracted Excel_" + new SimpleDateFormat("dd_MM_yy_hh_mm_ss").format(new Date()) + ".xlsx";
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Table_" + 1);
		try (FileOutputStream fos = new FileOutputStream(excel)) {
			workbook.write(fos);
		}
		workbook.close();
	}
}