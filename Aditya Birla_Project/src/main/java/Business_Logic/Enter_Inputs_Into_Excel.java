
package Business_Logic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class Enter_Inputs_Into_Excel implements Nlp {
    static FileInputStream fis = null;
    static Workbook workbook = null;
    static FileOutputStream fos = null;
   // static Sheet sheet = null;
    @InputParams({@InputParam(name = "JSON File Stream", type = "java.io.FileInputStream"), @InputParam(name = "Excel File Path", type = "java.lang.String"), @InputParam(name = "Sheet Name", type = "java.lang.String")})
   // @ReturnType(name = "Sum of Two numbers", type = "java.lang.Integer")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          InputStream jsonStream = (InputStream) programElementsInput.get("JSON File Stream");
          String excelFilePath = (String) programElementsInput.get("Excel File Path");
          String sheetName = (String) programElementsInput.get("Sheet Name");
         // int returnValue=0;

          try {
    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jsonStream));
    		String jsonInput = "";
    		String tempString;
    		while ((tempString = bufferedReader.readLine()) != null) {
    			jsonInput = jsonInput + tempString;
    		}
    		bufferedReader.close();
    		 ObjectMapper objectMapper = new ObjectMapper();
             Map<String, Object> jsonData = objectMapper.readValue(jsonInput, Map.class);

             fis = new FileInputStream(excelFilePath);
             workbook = new XSSFWorkbook(fis);
             Sheet sheet = workbook.getSheet(sheetName); 
             fis.close();

             for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
                 String key = entry.getKey();
                 Object value = entry.getValue();

                 for (int rowIndex = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                     Row row = sheet.getRow(rowIndex);
                     if (row == null) continue;

                     for (int colIndex = 0; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                         Cell cell = row.getCell(colIndex);

                         if (cell != null && cell.getCellType() == CellType.STRING && cell.getStringCellValue().equals(key)) {
                             int nextAvailableCol = findNextEmptyCell(row, colIndex + 1); 

                             if (value instanceof List<?>) {
                                 List<?> valueList = (List<?>) value;
                                 for (Object val : valueList) {
                                     Cell nextCell = row.getCell(nextAvailableCol);
                                     if (nextCell == null) {
                                         nextCell = row.createCell(nextAvailableCol);
                                     }
                                     writeValueToCell(nextCell, val);
                                     nextAvailableCol++; 
                                 }
                                 valueList.clear();
                             } else {
                                 Cell nextCell = row.getCell(nextAvailableCol);
                                 if (nextCell == null) {
                                     nextCell = row.createCell(nextAvailableCol);
                                 }
                                 writeValueToCell(nextCell, value);
                             }

                             break; 
                         }
                     }
                 }
             }

             workbook.setForceFormulaRecalculation(true);
             fos = new FileOutputStream(excelFilePath);
             workbook.write(fos);
         
             fos.close();
             workbook.close();
             fis.close();
              nlpResponseModel.setMessage("MI plan Data");
              nlpResponseModel.setStatus(CommonConstants.pass);
              }
          catch (Exception e) {
  			StringWriter sw = new StringWriter();
           PrintWriter pw=new PrintWriter(sw);
              String exceptionAsString = sw.toString();
        	    nlpResponseModel.setStatus(CommonConstants.fail);
  			nlpResponseModel.setMessage("Failed "+exceptionAsString);
  			
  		}

          // Your program element business logic ends here ...
         // nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
          return nlpResponseModel;
      }

      @Override
      public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();
        return params;
      }

      @Override
      public StringBuilder getTestCode() throws NlpException {
         StringBuilder sb = new StringBuilder();
        return sb;
      }
      private static int findNextEmptyCell(Row row, int startIndex) {
          int colIndex = startIndex;
          while (row.getCell(colIndex) != null && row.getCell(colIndex).getCellType() != CellType.BLANK) {
              colIndex++;
          }
          return colIndex;
      }

     
      private static void writeValueToCell(Cell cell, Object value) {
          if (value instanceof Number) {
              cell.setCellType(CellType.NUMERIC);
              cell.setCellValue(((Number) value).doubleValue());
          } else {
              String textValue = value.toString();
              if (textValue.matches("-?\\d+(\\.\\d+)?")) { 
                  cell.setCellType(CellType.NUMERIC);
                  cell.setCellValue(Double.parseDouble(textValue));
              } else {
                  cell.setCellType(CellType.STRING);
                  cell.setCellValue(textValue);
              }
          }
      }
  } 