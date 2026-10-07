package fileHandling_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class ValidateTwoExcelData implements Nlp {
	static XSSFSheet sheet=null;
	static XSSFSheet sheet1=null;
	@InputParams({ @InputParam(name = "DB Excel File Path", type = "java.lang.String"),
		@InputParam(name = "Exported Excel File Path", type = "java.lang.String")})

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

		String csvFilePath = (String) attributes.get("DB Excel File Path");
		String excelFilePath = (String) attributes.get("Exported Excel File Path");
//		String sheetName = (String) attributes.get("Sheet name");

		// Your program element business logic goes here ...
		List<String> differnce = new ArrayList<>();
		  try {
			  File file = new File(csvFilePath);
				FileInputStream fis = new FileInputStream(file);
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				sheet = wb.getSheet("data");

//				String path="C:\\Users\\User\\Downloads\\empl.xlsx";
//				String name=path.substring(path.lastIndexOf('\\')+1,path.indexOf('.'));

				File file1 = new File(excelFilePath);
				FileInputStream fis1 = new FileInputStream(file1);
				XSSFWorkbook wb1 = new XSSFWorkbook(fis1);
				sheet1 = wb1.getSheet("data");
				XSSFCell cell1 = sheet.getRow(0).createCell(sheet.getRow(0).getLastCellNum());
				cell1.setCellValue("Status");
				for (int i = 1; i <=sheet.getLastRowNum(); i++) {
					for (int j = 0; j < sheet.getRow(0).getLastCellNum()-1; j++) {
						String header=sheet.getRow(0).getCell(j).toString();
						int index=getHeaderIndex(header);
						if (index!=0) {
							XSSFCell cell = sheet.getRow(i).createCell(sheet.getRow(0).getLastCellNum()-1);
							if (sheet.getRow(i).getCell(j).toString().trim().equals(sheet1.getRow(i).getCell(index).toString().trim())) {
								cell.setCellValue("Verified");
							}
							else{
								cell.setCellValue("Not Verified");

							}
						}
					
//						System.out.println(sheet.getRow(i).getCell(j).toString()); 

					}
				}

				try (FileOutputStream fileOut = new FileOutputStream(csvFilePath)) {
					wb.write(fileOut);
				}

				wb.close();
	            nlpResponseModel.setMessage("CSV is successfully converted into excel");
				nlpResponseModel.setStatus(CommonConstants.pass);
	        } catch (IOException e) {
	        	StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
	        	nlpResponseModel.setMessage("Failed to convert CSV into Excel"+exceptionAsString);
				nlpResponseModel.setStatus(CommonConstants.fail);
	            e.printStackTrace();
	        }

		return nlpResponseModel;
	}
	public static int getHeaderIndex(String Header) {
		int a=0;
		for (int i = 0; i <sheet1.getRow(0).getLastCellNum()-1; i++) {
			System.out.println(sheet1.getRow(0).getCell(i).toString()+""+Header+"-");
			if ((sheet1.getRow(0).getCell(i).toString().trim()).contains(Header.trim())) {
				a=i;
				System.out.println(a);
				break;
			} 
		}
		return a;
	}
	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpResponseModel = new NlpRequestModel();
		Map<String, Object> attributes = nlpResponseModel.getAttributes();
		attributes.put("DB Excel File Path", "C:\\Users\\User\\Downloads\\Book6.xlsx");
		attributes.put("Exported Excel File Path", "C:\\Users\\User\\Downloads\\Book6.xlsx");
		attributes.put("Sheet name", "project");
		ValidateTwoExcelData v=new  ValidateTwoExcelData();
		v.execute(nlpResponseModel);
				
	}
	
}