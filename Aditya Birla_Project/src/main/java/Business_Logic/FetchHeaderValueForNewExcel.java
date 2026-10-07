package Business_Logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Slf4j
public class FetchHeaderValueForNewExcel implements Nlp {
	private static final Logger log = LoggerFactory.getLogger(SuperCalculatorCheck.class);
    @InputParams({@InputParam(name = "Excel File Path", type = "java.lang.String"),
    	@InputParam(name = "Sheet Name", type = "java.lang.String"),
    	@InputParam(name = "Multi Table Reference", type = "java.lang.String"),
    	@InputParam(name = "Column Header", type = "java.lang.String"),
    	@InputParam(name = "Row Header", type = "java.lang.String")})
    @ReturnType(name = "string3", type = "java.lang.String")

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
        String multitableReference = (String) attributes.get("Multi Table Reference");
        String columnHeader = (String) attributes.get("Column Header");
        String rowHeader = (String) attributes.get("Row Header");

        String result = StringUtils.EMPTY;
//        boolean valueFound = false;
        
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            log.info("The Last row number is: " + sheet.getLastRowNum());
 log.info("");
            int coversRow = -1;
            int valueColumnIndex = -1;
            int rowIndex = -1;

            // Step 1: Locate 'Covers'
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equalsIgnoreCase(multitableReference)) {
                        coversRow = row.getRowNum();
                        log.info("Covers found at row: " + coversRow);
                        break;
                    }
                }
                if (coversRow != -1) break;
            }

            // Step 2: Locate 'columnToFind' in the same row as 'Covers'
            if (coversRow != -1) {
                Row coversRowObj = sheet.getRow(coversRow);
                if (coversRowObj != null) {
                    for (Cell cell : coversRowObj) {
                        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equalsIgnoreCase(columnHeader)) {
                            valueColumnIndex = cell.getColumnIndex();
                            log.info("Column '" + columnHeader + "' found at index: " + valueColumnIndex);
                            break;
                        }
                    }
                }	
            }
            log.info("Covers Row and  value column index are : "+coversRow+" "+valueColumnIndex);

            // Step 3: Locate 'rowToFind' under 'Covers'
            if (coversRow != -1 && valueColumnIndex != -1) {
            	log.info("Entered step 3 IF ");
                for (int i = coversRow + 1; i <= sheet.getLastRowNum(); i++) {
                	log.info("Entered for loop");

                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell firstCell = row.getCell(0); // Assuming row headers are in the first column
                    	log.info("The row value inside is : "+firstCell.getStringCellValue());
                        if (firstCell != null && firstCell.getCellType() == CellType.STRING &&firstCell.getStringCellValue().equalsIgnoreCase(rowHeader)
    ) {
                            rowIndex = i;
                            log.info("Row '" + rowHeader + "' found at index: " + rowIndex);
                            break;
                        }
                    }
                }
            }
log.info("Row and column index are : "+rowIndex+" "+valueColumnIndex);
            // Step 4: Extract the required value
            if (rowIndex != -1 && valueColumnIndex != -1) {
                log.info("Fetching value from Row: " + rowIndex + " and Column: " + valueColumnIndex);
                
                Row targetRow = sheet.getRow(rowIndex);
                if (targetRow == null) {
                    log.info("Row at index " + rowIndex + " is null.");
                } else {
                    Cell targetCell = targetRow.getCell(valueColumnIndex);
                    if (targetCell == null) {
                    	nlpResponseModel.setStatus(CommonConstants.fail);
                        nlpResponseModel.setMessage("Cell at row " + rowIndex + ", column " + valueColumnIndex + " is null.");
                    } else {
                    result=getCellValue(targetCell, formulaEvaluator);
                    System.out.println("The result is "+result);
                    nlpResponseModel.setStatus(CommonConstants.pass);
                    nlpResponseModel.setMessage("Extracted Value: " + result);
                    }
                }
            } else {
            	nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage("Could not locate the required value. Please check input headers.");
            }
   
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to fetch value: " + exceptionAsString);
        }
log.info("The Result is : "+result);
        nlpResponseModel.getAttributes().put("string3", result);
        return nlpResponseModel;
    }


    public static String getMergedCellValue(Sheet sheet, int row, int col) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(row, col)) {
                return sheet.getRow(range.getFirstRow()).getCell(range.getFirstColumn()).getStringCellValue();
            }
        }
        Cell cell = sheet.getRow(row).getCell(col);
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }

    public static void main(String[] args) throws NlpException {
		NlpRequestModel req=new NlpRequestModel();
		LinkedHashMap map = new LinkedHashMap();
		map.put("Excel File Path", "C:\\Users\\User\\Downloads\\Activ One - Calculator 2.xlsm");
		map.put("Sheet Name", "FF");
		map.put("Multi Table Reference", "Covers");
		map.put("Column Header", "Premium after Discount/Loading without GST");
		map.put("Row Header", "Total Base + Optional + Add Ons Premium");
req.setAttributes(map);
		new FetchHeaderValueForNewExcel().execute(req);

	}

    
    private static String getCellValue(Cell cell, FormulaEvaluator formulaEvaluator) {
        String cellValue = "";
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = cell.getDateCellValue().toString();
                } else {
                    cellValue = String.valueOf(Math.round(cell.getNumericCellValue()));
                }
                break;
            case STRING:
                cellValue = cell.getStringCellValue().trim();
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue = evaluateFormula(cell, formulaEvaluator);
                break;
            default:
                cellValue = "Unknown cell type";
                break;
        }
        return cellValue;
    }
    private static String evaluateFormula(Cell cell, FormulaEvaluator formulaEvaluator) {
        String result = "";

        try {
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case NUMERIC:
                    result = String.valueOf(cellValue.getNumberValue());
                    break;
                case STRING:
                    result = cellValue.getStringValue();
                    break;
                case BOOLEAN:
                    result = String.valueOf(cellValue.getBooleanValue());
                    break;
                case ERROR:
                    result = "Error in formula";
                    break;
                default:
                    result = "Unknown formula result type";
            }
        } catch (Exception e) {
            result = "Error evaluating formula: " + e.getMessage();
        }

        return result;
    }
}