
package Custom_Map_Logic;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ConvertExcelSheetToMap implements Nlp {
    @InputParams({@InputParam(name = "File Path", type = "java.lang.String"), @InputParam(name = "Sheet Name", type = "java.lang.String")})
    @ReturnType(name = "MovieMap", type = "Map")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String filePath = (String) programElementsInput.get("File Path");
          String sheetName = (String) programElementsInput.get("Sheet Name");
          
          Map<String, String> movieMap = new HashMap<>();

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
              

              try (FileInputStream fis = new FileInputStream(new File(filePath));
                   Workbook workbook = new XSSFWorkbook(fis)) {

                  // 2. Access the specific sheet
                  Sheet sheet = workbook.getSheet(sheetName);
                  

                  // 3. Iterate through rows (Skipping header row at index 0)
                  for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                      Row row = sheet.getRow(i);
                      if (row != null) {
                          Cell titleCell = row.getCell(0); // Column A
                          Cell descCell = row.getCell(1);  // Column B

                          if (titleCell != null && descCell != null) {
                              String title = titleCell.getStringCellValue();
                              String description = descCell.getStringCellValue();
                              movieMap.put(title, description);
                              
                          }
                      }
                  }
              }
              nlpResponseModel.setStatus(CommonConstants.pass);
              
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("movieMap", movieMap);
          return nlpResponseModel;
      

  } 
}