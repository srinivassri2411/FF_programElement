
package specializedAutomation_Utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerateExcelWithWebrtcStats implements Nlp {
    @InputParams({@InputParam(name = "WebRTC Stats File Path", type = "java.lang.String"), @InputParam(name = "UserName", type = "java.lang.String"), @InputParam(name = "Excel Folder Path", type = "java.lang.String")})
    @ReturnType(name = "Excel File Path", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String webRTCStatsFilePath = (String) programElementsInput.get("WebRTC Stats File Path");
          String userName = (String) programElementsInput.get("UserName");
          String excelFolderPath = (String) programElementsInput.get("Excel Folder Path");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  returnValue=exportToExcel(webRTCStatsFilePath, userName, excelFolderPath);
        	  nlpResponseModel.setMessage("Excel file generated successfully at: " + returnValue);
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
        	 nlpResponseModel.setMessage("Failed to generate Excel file");
             nlpResponseModel.setStatus(CommonConstants.fail);
             throw new NlpException(e.getMessage());

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Excel File Path", returnValue);
          return nlpResponseModel;
      }
    
    public static String exportToExcel(String inputFilePath,
			String sheetName,
			String outputFolderPath) throws Exception {

		Map<String, String> stats = extractStats(inputFilePath);

		Map<String, List<String>> parsedData = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : stats.entrySet()) {

			String cleaned = entry.getValue()
					.replace("[", "")
					.replace("]", "");

			List<String> values = Arrays.stream(cleaned.split(","))
					.map(String::trim)
					.toList();

			parsedData.put(entry.getKey(), values);
		}

		int maxRows = parsedData.values().stream()
				.mapToInt(List::size)
				.max()
				.orElse(0);

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(sheetName);

		// Header Row
		Row headerRow = sheet.createRow(0);
		int colIndex = 0;

		for (String key : parsedData.keySet()) {
			headerRow.createCell(colIndex++).setCellValue(key);
		}

		// Data Rows
		for (int i = 0; i < maxRows; i++) {

			Row row = sheet.createRow(i + 1);
			colIndex = 0;

			for (List<String> values : parsedData.values()) {

				Cell cell = row.createCell(colIndex++);

				if (i < values.size()) {
					try {
						cell.setCellValue(Double.parseDouble(values.get(i)));
					} catch (NumberFormatException e) {
						cell.setCellValue(values.get(i));
					}
				}
			}
		}

		// Auto-size columns
		for (int i = 0; i < parsedData.size(); i++) {
			sheet.autoSizeColumn(i);
		}

		// Create folder if not exists
		File folder = new File(outputFolderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		// ✅ Generate timestamp
		DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

		String timeStamp = LocalDateTime.now().format(formatter);

		// ✅ File name with timestamp
		String outputFilePath = outputFolderPath + File.separator
				+sheetName+ "WebRTC_Stats_" + timeStamp + ".xlsx";

		try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
			workbook.write(fos);
		}

		workbook.close();

		return outputFilePath;
	}


	public static Map<String, String> extractStats(String filePath) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(new File(filePath));

		Map<String, String> result = new HashMap<>();

		List<String> requiredMetrics = Arrays.asList(
				"packetsLost",
				"jitter",
				"framesDecoded",
				"framesDropped",
				"roundTripTime",
				"packetsSent",
				"packetsReceived"
				);

		JsonNode peerConnections = root.get("PeerConnections");

		if (peerConnections == null) {
			return result;
		}

		Iterator<String> pcIds = peerConnections.fieldNames();

		while (pcIds.hasNext()) {
			String pcId = pcIds.next();
			JsonNode stats = peerConnections.get(pcId).get("stats");

			if (stats == null) continue;

			Iterator<String> statKeys = stats.fieldNames();

			while (statKeys.hasNext()) {
				String key = statKeys.next();

				for (String metric : requiredMetrics) {

					if (key.endsWith("-" + metric)) {

						JsonNode metricNode = stats.get(key);
						String valuesStr = metricNode.get("values").asText();

						result.put(metric, valuesStr);
					}
				}
			}
		}

		return result;
	}

  } 