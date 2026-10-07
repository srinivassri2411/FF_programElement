package Business_Logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class MultiRowToExcel implements Nlp {
    static String inputExcelPath = null; // Path to test inputs
    static String inputSheetName = null; // Sheet with test data
    static String targetExcelPath = null; // Path to target file
    static String targetSheetName = null; // Sheet where data will be written
    static String uniqueColumnName = null; // Unique column header to filter row
    static String searchSrNo = null; // Example: Fetch row where Sr.No. = 2

    @InputParams({@InputParam(name = "Test Input Excel File", type = "java.lang.nlp.filepath")
    , @InputParam(name = "Test Input Sheet Name", type = "java.lang.String")
    , @InputParam(name = "Unique Column", type = "java.lang.String")
    , @InputParam(name = "Unique Column Value", type = "java.lang.String")
    , @InputParam(name = "Target Excel File Path", type = "java.lang.String")
    , @InputParam(name = "Target Sheet Name", type = "java.lang.String")
    })
    @ReturnType(name = "Map", type = "java.util.Map")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String testInputExcelFilePath = (String) programElementsInput.get("Test Input Excel File"); // Using file path as String
        inputSheetName = (String) programElementsInput.get("Test Input Sheet Name");
        uniqueColumnName = (String) programElementsInput.get("Unique Column");
        searchSrNo = (String) programElementsInput.get("Unique Column Value");
        targetExcelPath = (String) programElementsInput.get("Target Excel File Path");
        targetSheetName = (String) programElementsInput.get("Target Sheet Name");

        Map<String, Object> rowData = null;
        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            inputExcelPath = createTempExcelFile(testInputExcelFilePath); // Pass file path instead of byte array
            if (inputExcelPath != null) {
                rowData = readExcelRow(inputExcelPath, inputSheetName, uniqueColumnName, searchSrNo);

                if (rowData.isEmpty()) {
                    nlpResponseModel.setMessage("No data found for Sr.No. = " + searchSrNo);
                    nlpResponseModel.setStatus(CommonConstants.fail);
                    return nlpResponseModel;
                }

                writeDataToTargetExcel(targetExcelPath, targetSheetName, rowData);
                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel.setMessage("Data entered successfully");
                new File(inputExcelPath).deleteOnExit();
            }
        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage(" Failed to enter the data ");
        }
        nlpResponseModel.getAttributes().put("Map", transformMap(rowData));
        return nlpResponseModel;
    }
    
    
    
    

    public static void main(String[] args) throws NlpException, IOException {

        String excelFile = "C:\\Users\\User\\Downloads\\ABHI_TestData.xlsx";
        String sheetName = "NXT_TestInputs";
        String uniqueColumn = "Test Case Id";
        String UniqueValue = "NXT_28";
        String targetFile = "C:\\Users\\User\\Downloads\\Activ One Calculators\\Activ One Calculators\\1.10 Activ One - MAX_NXT.xlsm";
        String TargetSheet = "MI";

        Map<String, Object> rowData = readExcelRow(excelFile, sheetName, uniqueColumn, UniqueValue);
        System.out.println(rowData);
       Map<String, Object> map = transformMap(rowData);
       System.out.println(map);
       
       MultiRowToExcel d = new MultiRowToExcel();
        NlpRequestModel nlp = new NlpRequestModel();
        Map<String, Object> attributes = nlp.getAttributes();

//        attributes.put("Test Input Excel File", excelFile); // Pass file path directly
//        attributes.put("Test Input Sheet Name", sheetName);
//        attributes.put("Unique Column", uniqueColumn);
//        attributes.put("Unique Column Value", UniqueValue);
//        attributes.put("Target Excel File Path", targetFile);
//        attributes.put("Target Sheet Name", TargetSheet); 

      //  d.execute(nlp);
    }
    
    
    
    
    
    
    
    
    
    
    

    public static Map<String, Object> readExcelRow(String filePath, String sheetName, String uniqueColumn, String srNo) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        Map<String, Object> dataMap = new LinkedHashMap<>();

        if (sheet == null) {
            System.out.println("Sheet '" + sheetName + "' not found.");
            workbook.close();
            return dataMap;
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            System.out.println("Header row is missing!");
            workbook.close();
            return dataMap;
        }

        int uniqueColIndex = -1;
        Map<Integer, String> headers = new HashMap<>();

        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim();
            headers.put(cell.getColumnIndex(), header);
            if (header.equalsIgnoreCase(uniqueColumn)) {
                uniqueColIndex = cell.getColumnIndex();
            }
        }

        if (uniqueColIndex == -1) {
            System.out.println("Unique column '" + uniqueColumn + "' not found.");
            workbook.close();
            return dataMap;
        }

        Row targetRow = null;

        for (Row row : sheet) {
            Cell srNoCell = row.getCell(uniqueColIndex);
            if (srNoCell != null) {
                String cellValue = "";

                if (srNoCell.getCellType() == CellType.NUMERIC) {
                    cellValue = String.valueOf((long) srNoCell.getNumericCellValue());
                } else if (srNoCell.getCellType() == CellType.STRING) {
                    cellValue = srNoCell.getStringCellValue().trim();
                }

                if (cellValue.equals(srNo)) {
                    targetRow = row;
                    break;
                }
            }
        }

        if (targetRow == null) {
            System.out.println("No data found for Sr.No. = " + srNo);
            workbook.close();
            return dataMap;
        }

        for (Cell cell : targetRow) {
            String key = headers.get(cell.getColumnIndex());
            if (key != null) {
                String value;
                if (cell.getCellType() == CellType.NUMERIC) {
                    value = String.valueOf(cell.getNumericCellValue());
                } else {
                    value = cell.getStringCellValue();
                }

                if (value.contains("||")) {
                    dataMap.put(key, Arrays.asList(value.split("\\|\\|")));
                } else {
                    dataMap.put(key, value);
                }
            }
        }

        workbook.close();
        return dataMap;
    }

    public static void writeDataToTargetExcel(String filePath, String sheetName, Map<String, Object> data) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        fis.close();

        if (sheet == null) {
            System.out.println("Target sheet '" + sheetName + "' not found.");
            workbook.close();
            return;
        }

        boolean updated = false;

        for (Row row : sheet) {
            Cell headerCell = row.getCell(0);
            if (headerCell == null || headerCell.getCellType() != CellType.STRING) continue;

            String header = headerCell.getStringCellValue().trim();
            if (!data.containsKey(header)) continue;

            Object value = data.get(header);
            int nextColIndex = 1;

            if (value instanceof List<?>) {
                List<?> values = (List<?>) value;
                for (Object val : values) {
                    Cell cell = row.getCell(nextColIndex);
                    if (cell == null) cell = row.createCell(nextColIndex);

                    setCellValueCorrectly(cell, val);
                    System.out.println("Writing " + val + " to row " + row.getRowNum() + ", col " + nextColIndex);

                    nextColIndex++;
                }
            } else {
                Cell cell = row.getCell(nextColIndex);
                if (cell == null) cell = row.createCell(nextColIndex);

                setCellValueCorrectly(cell, value);
                System.out.println("Writing " + value + " to row " + row.getRowNum() + ", col " + nextColIndex);
            }

            updated = true;
        }

        if (updated) {
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
        }

        workbook.close();
    }

    private static void setCellValueCorrectly(Cell cell, Object value) {
        if (value instanceof Number) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            String textValue = value.toString();
            if (textValue.matches("-?\\d+(\\.\\d+)?")) {
                try {
                    double numericValue = Double.parseDouble(textValue);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(numericValue);
                } catch (NumberFormatException e) {
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(textValue);
                }
            } else {
                cell.setCellType(CellType.STRING);
                cell.setCellValue(textValue);
            }
        }
        cell.getSheet().getWorkbook().setForceFormulaRecalculation(true);
    }

    public static String createTempExcelFile(String filePath) {
        try {
            File tempFile = File.createTempFile("excel_output_", ".xlsx", new File(System.getProperty("java.io.tmpdir")));
            try (FileInputStream fis = new FileInputStream(filePath);
                 FileOutputStream fileOut = new FileOutputStream(tempFile)) {

                Workbook workbook = new XSSFWorkbook(fis);
                workbook.write(fileOut);
                System.out.println("Excel content successfully written to temp file.");
                return tempFile.getAbsolutePath();
            }
        } catch (IOException e) {
            System.err.println("Error processing the file: " + e.getMessage());
        }
        return null;
    }

    public static Map<String, Object> transformMap(Map<String, Object> data) {
        Map<String, Object> transformed = new HashMap<>();
        
        return transformed;
    }
}
