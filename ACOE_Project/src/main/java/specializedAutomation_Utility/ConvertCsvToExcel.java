package specializedAutomation_Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class ConvertCsvToExcel implements Nlp {
	@InputParams({ @InputParam(name = "CSV File Path", type = "java.lang.String"),
		@InputParam(name = "Excel File Path", type = "java.lang.String")})

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

		String csvFilePath = (String) attributes.get("CSV File Path");
		String excelFilePath = (String) attributes.get("Excel File Path");
//		String sheetName = (String) attributes.get("Sheet name");

		// Your program element business logic goes here ...
		List<String> differnce = new ArrayList<>();
		  try {
	            // Read CSV file
	            List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
	            
	            // Create a new Excel workbook and sheet
	            Workbook workbook = new XSSFWorkbook();
	            Sheet sheet = workbook.createSheet("data");

	            // Iterate through CSV lines and add them to the Excel sheet
	            int rowNum = 0;
	            for (String line : lines) {
	                Row row = sheet.createRow(rowNum++);
	                String[] cells = line.split(",");
	                for (int i = 0; i < cells.length; i++) {
	                    Cell cell = row.createCell(i);
	                    cell.setCellValue(cells[i]);
	                }
	            }

	            // Write the Excel file
	            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
	                workbook.write(fileOut);
	            }

	            workbook.close();
	            nlpResponseModel.setMessage("CSV is successfully converted into excel");
				nlpResponseModel.setStatus(CommonConstants.pass);
	        } catch (IOException e) {
	        	nlpResponseModel.setMessage("Failed to convert CSV into Excel");
				nlpResponseModel.setStatus(CommonConstants.fail);
	            e.printStackTrace();
	        }

		return nlpResponseModel;
	}
	
}