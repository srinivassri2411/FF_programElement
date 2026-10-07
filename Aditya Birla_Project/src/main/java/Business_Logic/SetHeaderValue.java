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

public class SetHeaderValue implements Nlp {

    @InputParams({
        @InputParam(name = "Excel File Path", type = "java.lang.String"),
        @InputParam(name = "Sheet Name", type = "java.lang.String"),
        @InputParam(name = "", type = "java.lang.String"),
        @InputParam(name = "", type = "java.lang.String"),
        @InputParam(name = "", type = "java.lang.String")
    })
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
        String valueForHeader1 = (String) attributes.get("Term (in years)");
        String valueForHeader2 = (String) attributes.get("Premium payment mode");
        String valueForHeader3 = (String) attributes.get("Number of Members");

        boolean valueFound = false;

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getLastRowNum();
            for (int i = 0; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = cell.toString().trim();
                            if (cellValue.equalsIgnoreCase("Term (in years)")) {
                                Cell nextCell = row.getCell(j + 1);
                                if (nextCell == null) {
                                    nextCell = row.createCell(j + 1);
                                }
                                nextCell.setCellValue(valueForHeader1);
                                valueFound = true;
                            } else if (cellValue.equalsIgnoreCase("Premium payment mode")) {
                                Cell nextCell = row.getCell(j + 1);
                                if (nextCell == null) {
                                    nextCell = row.createCell(j + 1);
                                }
                                nextCell.setCellValue(valueForHeader2);
                                valueFound = true;
                            } else if (cellValue.equalsIgnoreCase("Number of Members")) {
                                Cell nextCell = row.getCell(j + 1);
                                if (nextCell == null) {
                                    nextCell = row.createCell(j + 1);
                                }
                                nextCell.setCellValue(valueForHeader3);
                                valueFound = true;
                            }
                        }
                    }
                }

                if (valueFound) {
                    break;
                }
            }

            if (!valueFound) {
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage("Could not find any matching headers.");
            } else {
                try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                    workbook.write(fos);
                    nlpResponseModel.setStatus(CommonConstants.pass);
                    nlpResponseModel.setMessage("Values entered for headers successfully.");
                }
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to enter values: " + exceptionAsString);
        }

        return nlpResponseModel;
    }
}
