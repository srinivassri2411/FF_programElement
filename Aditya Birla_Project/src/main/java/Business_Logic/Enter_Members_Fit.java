package Business_Logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class Enter_Members_Fit implements Nlp {
    @InputParams({@InputParam(name = "Excel File Path", type = "java.lang.String"), @InputParam(name = "Sheet Name", type = "java.lang.String"), @InputParam(name = "Header", type = "java.lang.String"), @InputParam(name = "Value to Enter", type = "java.lang.String")})
//    @ReturnType(name = "string3", type = "java.lang.String")

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
      @Override
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> attributes = nlpRequestModel.getAttributes();
          String excelFilePath = (String) attributes.get("Excel File Path");
          String sheetName = (String) attributes.get("Sheet Name");
          String searchValue = (String) attributes.get("Header");
          String value = (String) attributes.get("Value to Enter");
//String nextCellValue=StringUtils.EMPTY;
//boolean valueFound = false;
          boolean valueFound = false;
        try(FileInputStream fis = new FileInputStream(new File(excelFilePath));
                Workbook workbook = WorkbookFactory.create(fis))  {
       
            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getLastRowNum(); 
            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i);

                if (row != null) {
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        Cell cell = row.getCell(j);

                        if (cell != null && cell.toString().trim().equalsIgnoreCase(searchValue)) {
                            Cell nextCell = row.getCell(j + 1);
                            if (nextCell == null) {
                                nextCell = row.createCell(j + 1); 
                            }

                            nextCell.setCellValue(value);
                            valueFound = true;
                            break; 
                        }
                    }
                }

                if (valueFound) {
                    break; 
                }
            }
            if (!valueFound) {
                System.out.println("Value '" + searchValue + "' not found in any column.");
            } else {
                try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                    workbook.write(fos);
                    nlpResponseModel.setStatus(CommonConstants.pass);
          		  nlpResponseModel.setMessage("The "+value+" entered for Header " + searchValue );
                }
            }
        	  
        }
catch(Exception e) {
 	  StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
	 
	  nlpResponseModel.setStatus(CommonConstants.fail);
	  nlpResponseModel.setMessage("Failed to Enter  "+value+" "+exceptionAsString);
	  
}
//         nlpResponseModel.getAttributes().put("string3", nextCellValue);
          return nlpResponseModel;
      }
  } 
