package Business_Logic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class Provide_Members_Fit implements Nlp {
    @InputParams({
        @InputParam(name = "File Path", type = "java.lang.String"),
        @InputParam(name = "Sheet Name", type = "java.lang.String"),
        @InputParam(name = "Header", type = "java.lang.String"),
        @InputParam(name = "Header Value", type = "java.lang.String")
    })
   // @ReturnType(name = "Status", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> attributes = nlpRequestModel.getAttributes();
        String filePath = (String) attributes.get("File Path");
        String sheetName = (String) attributes.get("Sheet Name");
        String searchValue = (String) attributes.get("Header");
        String newValue = (String) attributes.get("Header Value");
        
        boolean valueFound = false;
        NlpResponseModel responseModel = new NlpResponseModel();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new NlpException("Sheet not found: " + sheetName);
            }
            
            int rowCount = sheet.getLastRowNum();
            for (int i = 0; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && cell.toString().trim().equalsIgnoreCase(searchValue)) {
                            Cell nextCell = row.getCell(j + 1);
                            if (nextCell == null) {
                                nextCell = row.createCell(j + 1);
                            }
                            nextCell.setCellValue(newValue);
                            valueFound = true;
                            break;
                        }
                    }
                }
                if (valueFound) break;
            }

            if (valueFound) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                }
                responseModel.setStatus(CommonConstants.pass);
                responseModel.setMessage("Dropdown value modified successfully.");
            } else {
                responseModel.setStatus(CommonConstants.fail);
                responseModel.setMessage("Search value not found in sheet.");
            }

        } catch (IOException e) {
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage("File error: " + e.getMessage());
        } catch (Exception e) {
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage("Unexpected error: " + e.getMessage());
        }
        
        return responseModel;
    }
    

}



