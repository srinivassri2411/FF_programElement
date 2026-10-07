package unilog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.xssf.streaming.SXSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Snap {
	public static void main(String[] args) throws Exception {
		FileInputStream fis = new FileInputStream(
				"C:\\Users\\Srinivas m\\Downloads\\iPru - Save N Grow calculator (GIFT V11+EzyGrow V05) (2).xlsx");
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		XSSFSheet sheet = workbook.getSheet("Input");
		sheet.getRow(4).getCell(2).setCellValue(40);
		workbook.setForceFormulaRecalculation(true);
	SXSSFFormulaEvaluator	.evaluateAllFormulaCells(workbook);
		Thread.sleep(2000);
		FileOutputStream fos = new FileOutputStream(new File(
				"C:\\Users\\Srinivas m\\Downloads\\iPru - Save N Grow calculator (GIFT V11+EzyGrow V05) (2).xlsx"));
		workbook.write(fos);
		workbook.close();
		fos.close();
	}
}