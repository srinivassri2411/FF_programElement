
package business_logics_Android;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

public class SuperCalculatorCheck implements Nlp {
    static String inputExcelPath = null; // Path to test inputs
    static String inputSheetName = null; // Sheet with test data
    static String targetExcelPath = null; // Path to target file
    static String targetSheetName = null; // Sheet where data will be written
     String uniqueColumnName = null; // Unique column header to filter row
     String searchSrNo = null; // Example: Fetch row where Sr.No. = 2
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
          byte[] testInputExcelFile =( byte[]) programElementsInput.get("Test Input Excel File");
           inputSheetName = (String) programElementsInput.get("Test Input Sheet Name");
           uniqueColumnName = (String) programElementsInput.get("Unique Column");
           searchSrNo = (String) programElementsInput.get("Unique Column Value");
           targetExcelPath = (String) programElementsInput.get("Target Excel File Path");
           targetSheetName = (String) programElementsInput.get("Target Sheet Name");
          Map<String, Object> rowData=null;
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	
        	     if (testInputExcelFile == null) {
        	    	
              // 	  log.info("The Byte Array value is NULL");
               	  }
        	     
        	  inputExcelPath = createTempExcelFile(testInputExcelFile);
              if (inputExcelPath != null) {
            	//  log.info("The inputExcelPath(temo) is : "+inputExcelPath);
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
    
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
       	  nlpResponseModel.setMessage(" = Failed = ");

          }
nlpResponseModel.getAttributes().put("Map", transformMap(rowData));
rowData.clear();
          return nlpResponseModel;
      }
    
   
    
    public static Map<String, Object> readExcelRow(String filePath, String sheetName, String uniqueColumn, String srNo) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        Map<String, Object> dataMap = new LinkedHashMap<>();

        if (sheet == null) {
          //  log.info("Sheet '" + sheetName + "' not found.");
            workbook.close();
            return dataMap;
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
        //    log.info("Header row is missing!");
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
         //   log.info("Unique column '" + uniqueColumn + "' not found.");
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
         //   log.info("No data found for Sr.No. = " + srNo);
            workbook.close();
            return dataMap;
        }

        for (Cell cell : targetRow) {
            String key = headers.get(cell.getColumnIndex());
            if (key != null) {
                String value;
                if (cell.getCellType() == CellType.NUMERIC) {
                	value = String.valueOf((long)cell.getNumericCellValue());
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
//log.info("The row data map is : "+dataMap);
        workbook.close();
       fis.close();
        return dataMap;
    }

    public static void writeDataToTargetExcel(String filePath, String sheetName, Map<String, Object> data) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
 

        if (sheet == null) {
           // log.info("Target sheet '" + sheetName + "' not found.");
            workbook.close();
            return;
        }

        boolean updated = false;

        updated |= handleRegularHeaders(sheet, data);
        updated |= handleMergedCells(sheet, data);
        updated |= handlePolicyholderDetails(sheet,data);

        if (updated) {
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
        }

        workbook.close();
        fis.close();
    }

 private static boolean handleMergedCells(Sheet sheet, Map<String, Object> data) {
        boolean updated = false;
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

        for (CellRangeAddress range : mergedRegions) {
            Row firstRow = sheet.getRow(range.getFirstRow());
            Cell firstCell = firstRow.getCell(range.getFirstColumn());

            if (firstCell != null && firstCell.getCellType() == CellType.FORMULA) {
                String mergedValue = firstCell.getCellFormula();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (mergedValue.contains(entry.getKey())) {
                        Cell nextCell = firstRow.getCell(range.getLastColumn() + 1);
                        if (nextCell == null) {
                            nextCell = firstRow.createCell(range.getLastColumn() + 1);
                        }
                        setCellValueCorrectly(nextCell, entry.getValue());
                       // log.info("Writing " + entry.getValue() + " to row " + range.getFirstRow() + ", col " + (range.getLastColumn() + 1) + " (next to merged cell)");
                        updated = true;
                    }
                }
            }
        }
        return updated;
    } 
private static boolean handleRegularHeaders(Sheet sheet, Map<String, Object> data) {
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
                   // log.info("Writing " + val + " to row " + row.getRowNum() + ", col " + nextColIndex);

                    nextColIndex++;
                }
            } else {
                Cell cell = row.getCell(nextColIndex);
                if (cell == null) cell = row.createCell(nextColIndex);

                setCellValueCorrectly(cell, value);
              //  log.info("Writing " + value + " to row " + row.getRowNum() + ", col " + nextColIndex);
            }

            updated = true;
        }
        return updated;
    }

private static boolean handlePolicyholderDetails(Sheet sheet, Map<String, Object> data) {
        boolean updated = false;
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell != null && cell.getCellType() == CellType.STRING && "Policyholder(s) Details".equals(cell.getStringCellValue().trim())) {
                    int startingCol = cell.getColumnIndex() + 1;
                    Row nextRow = sheet.getRow(row.getRowNum() + 1);
                    if (nextRow == null) {
                        nextRow = sheet.createRow(row.getRowNum() + 1);
                    }

                    int currentRow = row.getRowNum()+1;
                    int currentCol = startingCol;

                    for(int i = startingCol; i<row.getLastCellNum(); i++){
                        Cell headerCell = row.getCell(i);
                        if(headerCell == null || headerCell.getCellType() != CellType.STRING) continue;
                        String header = headerCell.getStringCellValue().trim();
                        if(data.containsKey(header)){
                            Object value = data.get(header);
                            if(value instanceof List<?>){
                                List<?> values = (List<?>) value;
                                for(Object val : values){
                                    Row writeRow = sheet.getRow(currentRow);
                                    if(writeRow == null){
                                        writeRow = sheet.createRow(currentRow);
                                    }
                                    Cell writeCell = writeRow.getCell(i);
                                    if(writeCell == null){
                                        writeCell = writeRow.createCell(i);
                                    }
                                    setCellValueCorrectly(writeCell, val);
                                  //  log.info("Writing Policyholder value " + val + " to row " + currentRow + ", col " + i);
                                    currentRow++;
                                    updated = true;
                                }
                                currentRow = row.getRowNum() + 1;
                            } else {
                                Cell writeCell = nextRow.getCell(i);
                                if(writeCell == null){
                                    writeCell = nextRow.createCell(i);
                                }
                                setCellValueCorrectly(writeCell, value);
                            //    log.info("Writing Policyholder value " + value + " to row " + (row.getRowNum() +1) + ", col " + i);
                                updated = true;
                            }
                        }
                    }
                }
            }
        }
        return updated;
    }
    
    private static void setCellValueCorrectly(Cell cell, Object value) {
        if (value instanceof Number) {
            // Directly store numbers as numeric values
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            String textValue = value.toString();

            // Check if text is purely numeric (no letters/special characters)
            if (textValue.matches("-?\\d+(\\.\\d+)?")) {
                try {
                    // Convert numeric string to actual number to avoid Excel treating it as text
                    double numericValue = Double.parseDouble(textValue);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(numericValue);
                } catch (NumberFormatException e) {
                    // If conversion fails, store as text
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(textValue);
                }
            } else {
                // Store non-numeric values as text
                cell.setCellType(CellType.STRING);
                cell.setCellValue(textValue);
            }
        }

        // Force recalculations to prevent hidden values
        cell.getSheet().getWorkbook().setForceFormulaRecalculation(true);
    }	       
 public static String createTempExcelFile(byte[] data) {
            try {
                File tempFile = File.createTempFile("excel_output_", ".xlsx", new File(System.getProperty("java.io.tmpdir")));
                try (FileOutputStream fileOut = new FileOutputStream(tempFile)) {
                    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                    		  Workbook workbook = new XSSFWorkbook(byteArrayInputStream); ) {
                      
                        workbook.write(fileOut);
                       // log.info("Excel content successfully written to temp file.");
                        return tempFile.getAbsolutePath(); 
                    } catch (IOException e) {
                       // log.info("Error processing the byte array as an Excel file: " + e.getMessage());
                    }
                } catch (IOException e) {
                   // log.info("Error writing the Excel file to temp file: " + e.getMessage());
                }

            } catch (IOException e) {
                //log.info("Error creating temp file: " + e.getMessage());
            }
            return null; 
        }
        public static Map<String, Object> transformMap(Map<String, Object> originalMap) {
            Map<String, Object> transformedMap = new HashMap<>();

            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                switch (key) {
                    case "SI":
                        if (value instanceof List) {
                            List<String> amounts = (List<String>) value;  // Treat values as String
                            List<String> formattedAmounts = new ArrayList<>();
                            for (String amount : amounts) {
                                formattedAmounts.add(convertToLac(amount));
                            }
                            transformedMap.put(key, formattedAmounts);
                        }
                        else {
                            	value=value.toString();
                            	value=String.valueOf((int) Double.parseDouble((String) value));
                                transformedMap.put(key, convertToLac(String.valueOf(value))); 
                        }
                        break;

                    case "HLTH MTR":
                    case "Super Credit":
                    case "Chronic Care (Day 1 In-patient Hospitalization)":
                    case "Chronic Management Program (OPD)":
                    case "Critical Illness cover":
                    case "Personal Accident Cover":
                    case "Claims Protect Enhanced":
                    case "Cancer Hospitalization Booster":
                    case "Compassionate Visit":
                    case "Annual Screening Package for Cancer Diagnosed Patients":
                    case "Second Medical Opinion for listed Major Illness":
                    case "Durable equipment cover":
                    case "Annual Health Check-Up":
                    case "Vaccine Cover":
                    case "Tele consultation OPD":
                    case "PPN":
                    case "Per Claim Deductible":
                    case "Chronic Care (Day 1 In-patient Hospitalization) Restriction":
                    case "Geographical extension to include US and Canada":
                    	
                        if (value instanceof List) {
                            List<String> statusList = (List<String>) value;
                            List<String> transformedStatus = new ArrayList<>();
                            for (String status : statusList) {
                                transformedStatus.add(status.equalsIgnoreCase("Yes") ? "Add +" : "- Remove");
                            }
                            transformedMap.put(key, transformedStatus);
                        }
                        else {
                        	 transformedMap.put(key, value.toString().equalsIgnoreCase("Yes") ? "Add +" : "- Remove");
                        }
                        
                        	
                        break;

                    case "User Input / Family Construct":
                        if (value instanceof List) {
                            List<String> members = (List<String>) value;
                            List<String> transformedMembers = new ArrayList<>();
                            int kidCounter = 1;

                            for (String member : members) {
                                if (member.equalsIgnoreCase("Self")) {
                                    transformedMembers.add("SELF");
                                } else if (member.equalsIgnoreCase("Legally Married Spouse")) {
                                    transformedMembers.add("SPOUSE");
                                } else if (member.equalsIgnoreCase("Mother")) {
                                    transformedMembers.add("MOTHER");
                                } else if (member.equalsIgnoreCase("Father")) {
                                    transformedMembers.add("FATHER");
                                } else if (member.equalsIgnoreCase("Mother-in-law")) {
                                    transformedMembers.add("MOTHER-IN-LAW");
                                } else if (member.equalsIgnoreCase("Father-in-law")) {
                                    transformedMembers.add("FATHER-IN-LAW");
                                } else {
                                    transformedMembers.add("KID" + kidCounter++);
                                }
                            }
                            transformedMap.put(key, transformedMembers);
                        } else {
                         //   log.info(value.toString());
                            
                            if (value.toString().equalsIgnoreCase("Self")) {
                                transformedMap.put("User Input / Family Construct", "SELF");
                            } else if (value.toString().equalsIgnoreCase("Legally Married Spouse")) {
                                transformedMap.put("User Input / Family Construct", "SPOUSE");
                            } else if (value.toString().equalsIgnoreCase("Mother")) {
                                transformedMap.put("User Input / Family Construct", "MOTHER");
                            } else if (value.toString().equalsIgnoreCase("Father")) {
                                transformedMap.put("User Input / Family Construct", "FATHER");
                            } else if (value.toString().equalsIgnoreCase("Mother-in-law")) {
                                transformedMap.put("User Input / Family Construct", "MOTHER-IN-LAW");
                            } else if (value.toString().equalsIgnoreCase("Father-in-law")) {
                                transformedMap.put("User Input / Family Construct", "FATHER-IN-LAW");
                            } else {
                               
                                transformedMap.put("User Input / Family Construct", "KID");
                            }
                        }
                        break;

                    case "Policy Term":
                        if (value instanceof String && value.equals("1 Year")||value.equals("1-Year")) {
                            transformedMap.put(key, "1 year");
                        } 
                        else if(value instanceof String && value.equals("2 Years")||value.equals("2-Years")) {
                            transformedMap.put(key, "2 year");
                        } 
                        else if(value instanceof String && value.equals("3 Years")||value.equals("3-Years")) {
                            transformedMap.put(key, "3 year");
                        } 	
                        
                        else {
                            transformedMap.put(key, value);
                        }
                        break;
                        

                    case "Chronic Condition":
                        if (value instanceof List) {
                            List<String> conditions = (List<String>) value;
                            List<String> transformedConditions = new ArrayList<>();
                            for (String condition : conditions) {
                                transformedConditions.add(transformChronicCondition(condition));
                            }
                            transformedMap.put(key, transformedConditions);
                        }
                        else {
                        	transformedMap.put(key, transformChronicCondition(value.toString()));
                        }
                        break;
                        
                    case "Risk Class":
                    	if (value instanceof List) {
                            List<String> riskclass = (List<String>) value;
                            List<String> transformedConditions = new ArrayList<>();
                            for (String condition : riskclass) {
                                transformedConditions.add(transformRiskClass(condition));
                            }
                            transformedMap.put(key, transformedConditions);
                        }
                    	else {
                        	transformedMap.put(key, transformRiskClass(value.toString()));
                        }
                        break;
                        
                        
                    case "Sum Insured":
                        if (value instanceof List) {
                            List<String> amounts = (List<String>) value;  // Treat values as String
                            List<String> formattedAmounts = new ArrayList<>();
                            for (String amount : amounts) {
                                formattedAmounts.add(convertToLakh(amount));
                            }
                            transformedMap.put(key, formattedAmounts);
                        }
                        else {
                        	value=value.toString();
                        	value=String.valueOf((int) Double.parseDouble((String) value));
                            transformedMap.put(key, convertToLakh(String.valueOf(value))); 
                        }
                        break;
                        
                    case "Deductible":
                        if (value instanceof List) {
                            List<String> amounts = (List<String>) value;  // Treat values as String
                            List<String> formattedAmounts = new ArrayList<>();
                            for (String amount : amounts) {
                                formattedAmounts.add(convertToLakh(amount));
                            }
                            transformedMap.put(key, formattedAmounts);
                        }
                        else {
                        	value=value.toString();
                        	value=String.valueOf((int) Double.parseDouble((String) value));
                            transformedMap.put(key, convertToLakh(String.valueOf(value))); 
                        }
                        break;
                    case "Age":
                        if (value instanceof List) {
                            List<String> ages = (List<String>) value;  // Treat values as String
                            List<String> formattedAmounts = new ArrayList<>();
                            
                            for (String age : ages) {
                            	
                                formattedAmounts.add(String.valueOf((int) Double.parseDouble(age)));
                            }
                            transformedMap.put(key, formattedAmounts);
                        }
                        else {
                        	transformedMap.put(key, String.valueOf((int) Double.parseDouble((String) value)));
                        }
                        break;
                    	
                    case "Age (in years)":
                        if (value instanceof List) {
                            List<String> ages = (List<String>) value;  // Treat values as String
                            List<String> formattedAmounts = new ArrayList<>();
                            
                            for (String age : ages) {
                            	
                                formattedAmounts.add(String.valueOf((int) Double.parseDouble(age)));
                            }
                            transformedMap.put(key, formattedAmounts);
                        }
                        else {
                        	transformedMap.put(key, String.valueOf((int) Double.parseDouble((String) value)));
                        }
                        break;
                    	
                    default:
                        transformedMap.put(key, value);
                        break;
                }
            }
            return transformedMap;
        }

        private static String convertToLac(String amount) {
            try {
                int numericAmount = Integer.parseInt(amount.trim());
      
                if (numericAmount == 1000000) return "10 Lac";
                if (numericAmount == 1500000) return "15 Lac";
                if (numericAmount == 2000000) return "20 Lac";
                if (numericAmount == 2500000) return "25 Lac";
                if (numericAmount == 5000000) return "50 Lac";
                if (numericAmount == 10000000) return "1 Cr";
                if (numericAmount == 20000000) return "2 Cr";
                if (numericAmount == 30000000) return "3 Cr";
                if (numericAmount == 40000000) return "4 Cr";
                if (numericAmount == 50000000) return "5 Cr";
                return amount; 
            } catch (NumberFormatException e) {
                return amount; 
            }
        }
        
        private static String convertToLakh(String amount) {
            try {
                int numericAmount = Integer.parseInt(amount.trim());
                if (numericAmount == 100000) return "1 Lakh";
                if (numericAmount == 200000) return "2 Lakh";
                if (numericAmount == 300000) return "3 Lakh";
                if (numericAmount == 400000) return "4 Lakh";
                if (numericAmount == 500000) return "5 Lakh";
                if (numericAmount == 700000) return "7 Lakh";
                if (numericAmount == 1000000) return "10 Lakh";
                if (numericAmount == 1500000) return "15 Lakh";
                if (numericAmount == 2000000) return "20 Lakh";
                if (numericAmount == 2500000) return "25 Lakh";
                if (numericAmount == 3000000) return "30 Lakh";
                if (numericAmount == 4000000) return "40 Lakh";
                if (numericAmount == 5000000) return "50 Lakh";
                if (numericAmount == 8500000) return "85 Lakh";
                if (numericAmount == 9000000) return "90 Lakh";
                if (numericAmount == 9500000) return "95 Lakh";
               
                return amount; 
            } catch (NumberFormatException e) {
                return amount; 
            }
        }


        private static String transformChronicCondition(String condition) {
            switch (condition) {
                case "PTCA":
                    return "Coronary Artery Disease (PTCA done prior to 1 year)";
                case "High BMI":
                    return "Obesity";
                default:
                    return condition;
            }
        }
        private static String transformRiskClass(String condition) {
            switch (condition) {
                case "Risk class 1":
                    return "ACCOUNTANT";
                case "Risk class 2":
                    return "Cargo-Staff";
                case "Risk class 3":
                	return "CARPENTER";
                default:
                    return condition;
            }
        }
}
       