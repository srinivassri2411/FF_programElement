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

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FetchHeaderValue implements Nlp {
    @InputParams({@InputParam(name = "Excel File Path", type = "java.lang.String"), @InputParam(name = "Sheet Name", type = "java.lang.String"), @InputParam(name = "Expected Header", type = "java.lang.String")})
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
        String searchValue = (String) attributes.get("Expected Header");

        String nextCellValue = StringUtils.EMPTY;
        boolean valueFound = false;
        
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);

            // Check if the sheet exists
            if (sheet == null) {
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage("Sheet '" + sheetName + "' not found.");
                return nlpResponseModel;
            }

            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int rowCount = sheet.getLastRowNum();

            for (int i = rowCount; i >= 0; i--) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        Cell cell = row.getCell(j);

                        if (cell != null && cell.toString().trim().contains(searchValue)) {
                            if (j + 1 < row.getPhysicalNumberOfCells()) {
                                Cell nextCell = row.getCell(j + 1);

                                if (nextCell != null) {
                                    switch (nextCell.getCellType()) {
                                        case NUMERIC:
                                            if (DateUtil.isCellDateFormatted(nextCell)) {
                                                nextCellValue = nextCell.getDateCellValue().toString();
                                            } else {
                                                nextCellValue = String.valueOf(Math.round(nextCell.getNumericCellValue()));
                                            }
                                            break;
                                        case STRING:
                                            nextCellValue = nextCell.getStringCellValue();
                                            break;
                                        case BOOLEAN:
                                            nextCellValue = String.valueOf(nextCell.getBooleanCellValue());
                                            break;
                                        case FORMULA:
                                            // If the next cell contains a formula, evaluate it
                                            nextCellValue = evaluateFormula(nextCell, formulaEvaluator, sheet);
                                            break;
                                        default:
                                            nextCellValue = "Unknown cell type";
                                            break;
                                    }
                                    System.out.println("Found '" + searchValue + "' in row " + i + ", value in next column: " + nextCellValue);
                                } else {
                                    System.out.println("Next cell in row " + i + " is empty.");
                                }
                            } else {
                                System.out.println("No next cell in row " + i + " for '" + searchValue + "'.");
                            }
                            valueFound = true;
                            break;
                        }
                    }
                }

                if (valueFound) {
                    break;
                }
            }

            if (valueFound) {
                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel.setMessage("The value for " + searchValue + " is: " + nextCellValue);
            } else {
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage("Value '" + searchValue + "' not found in any column.");
            }
            System.out.println("The value for " + searchValue + " is: " + nextCellValue);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to fetch value: " + exceptionAsString);
        }

        nlpResponseModel.getAttributes().put("string3", nextCellValue);
        return nlpResponseModel;
    }

    private String evaluateFormula(Cell cell, FormulaEvaluator formulaEvaluator, Sheet sheet) {
        String result = "";

        if (cell.getCellType() == CellType.FORMULA) {
            // Evaluate the formula and get the value of the result
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
        }
        return result;
    }

    private static String getCellValue(Cell cell) {
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
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue = cell.getCellFormula();
                break;
            default:
                cellValue = "Unknown cell type";
                break;
        }

        return cellValue;
    }

    public static void main(String[] args) throws NlpException {
		NlpRequestModel req=new NlpRequestModel();
		LinkedHashMap map = new LinkedHashMap();
		map.put("Excel File Path", "C:\\Users\\User\\Desktop\\Calculators\\Premium Calculators\\Super Health Plus Top-Up - Calculator.xlsx");
		map.put("Sheet Name", "Calculator");
		map.put("Expected Header", "Total Premium including GST");
req.setAttributes(map);
		new FetchHeaderValue().execute(req);
		
		
		
		
	}
}
