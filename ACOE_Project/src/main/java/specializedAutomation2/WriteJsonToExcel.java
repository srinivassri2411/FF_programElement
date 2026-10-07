package specializedAutomation2;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class WriteJsonToExcel implements Nlp {

    @InputParams({
        @InputParam(name = "JSON File Path", type = "java.lang.String"),
        @InputParam(name = "Excel Path", type = "java.lang.String")
    })
    @ReturnType(name = "Status", type = "java.lang.String")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> inputAttributes = nlpRequestModel.getAttributes();
        String jsonFilePath = (String) inputAttributes.get("JSON File Path");
        String excelPath = (String) inputAttributes.get("Excel Path");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        String statusMessage = "";

        try {
            // 1️⃣ Read JSON from file
            StringBuilder jsonDataBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonDataBuilder.append(line);
                }
            }
            String jsonData = jsonDataBuilder.toString();

            // 2️⃣ Convert JSON object to JSON array
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("barcodes");

            // 3️⃣ Read or create Excel file
            File file = new File(excelPath);
            Workbook workbook;
            Sheet sheet;

            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);

                // Clear all existing rows
                int lastRow = sheet.getLastRowNum();
                for (int i = lastRow; i >= 0; i--) {
                    Row row = sheet.getRow(i);
                    if (row != null) sheet.removeRow(row);
                }

            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Sheet1");
            }

            // 4️⃣ Dynamically generate headers
            Row headerRow = sheet.createRow(0);
            List<String> headers = new ArrayList<>();
            int colIndex = 0;

            JSONObject firstObj = jsonArray.getJSONObject(0);
            for (String key : firstObj.keySet()) {
                Object value = firstObj.get(key);
                if (value instanceof JSONArray) {
                    JSONArray arr = (JSONArray) value;
                    for (int i = 0; i < arr.length(); i++) {
                        String newHeader = key + "_" + (i + 1);
                        headers.add(newHeader);
                        headerRow.createCell(colIndex++).setCellValue(newHeader);
                    }
                } else {
                    headers.add(key);
                    headerRow.createCell(colIndex++).setCellValue(key);
                }
            }

            // 5️⃣ Write JSON data into Excel
            int rowNum = 1; // start from row after header
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Row row = sheet.createRow(rowNum++);
                colIndex = 0;

                for (String key : firstObj.keySet()) {
                    Object value = obj.get(key);
                    if (value instanceof JSONArray) {
                        JSONArray arr = (JSONArray) value;
                        for (int j = 0; j < arr.length(); j++) {
                            row.createCell(colIndex++).setCellValue(arr.get(j).toString());
                        }
                    } else {
                        row.createCell(colIndex++).setCellValue(value.toString());
                    }
                }
            }

            // 6️⃣ Save workbook
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();

            statusMessage = "JSON file data successfully written to Excel (existing data replaced).";
            nlpResponseModel.setStatus(CommonConstants.pass);

        } catch (Exception e) {
            statusMessage = "Failed to write JSON to Excel: " + e.getMessage();
            nlpResponseModel.setStatus(CommonConstants.fail);
        }

        nlpResponseModel.getAttributes().put("Status", statusMessage);
        return nlpResponseModel;
    }

    // -------------------- MAIN METHOD FOR TESTING --------------------
    public static void main(String[] args) {
        try {
            String jsonFilePath = "C:\\Users\\User\\walmart\\result.json";
            String excelPath = "C:\\Users\\User\\Downloads\\BarCodesData.xlsx";

            NlpRequestModel requestModel = new NlpRequestModel();
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("JSON File Path", jsonFilePath);
            attributes.put("Excel Path", excelPath);
            requestModel.setAttributes(attributes);

            WriteJsonToExcel nlp = new WriteJsonToExcel();
            NlpResponseModel response = nlp.execute(requestModel);

            System.out.println("Status: " + response.getAttributes().get("Status"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
