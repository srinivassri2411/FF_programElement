package specializedAutomation_Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class CompareExcelFileWithPdfFile implements Nlp {
	@InputParams({ @InputParam(name = "PdfFilePath", type = "java.lang.String"),
			@InputParam(name = "ExcelFilePath", type = "java.lang.String"),
			@InputParam(name = "SheetName", type = "java.lang.String"),
			@InputParam(name = "DifferencesFolderDirectory", type = "java.lang.String") })

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

		String pdfFilePath = (String) attributes.get("PdfFilePath");
		String excelFilePath = (String) attributes.get("ExcelFilePath");
		String sheetName = (String) attributes.get("SheetName");
		String differencesFolderDirectory = (String) attributes.get("DifferencesFolderDirectory");

		// Your program element business logic goes here ...
		List<String> differnce = new ArrayList<>();
		try {
			String absolutePath = convertpdfToWord(pdfFilePath);
			differnce = getDataFromExcelAndCompare(excelFilePath, sheetName, absolutePath, differencesFolderDirectory);

			deleteFile(absolutePath);
			if (differnce.isEmpty()) {
				nlpResponseModel.setMessage("Both Tables are Same");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("There are Some differnces Between the Tables");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Compare => " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		
		return nlpResponseModel;
	}

	private static String convertpdfToWord(String pdfFilePath) throws Exception, InterruptedException {

		// Set download directory
		String downloadPath = System.getProperty("java.io.tmpdir");
		HashMap<String, Object> chromePrefs = new HashMap<>();
		chromePrefs.put("download.default_directory", downloadPath);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		options.addArguments("--headless");
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("chrome");
		cap.setCapability(ChromeOptions.CAPABILITY, options);

		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		driver.get("https://www.ilovepdf.com/pdf_to_word");

		String fullPath = pdfFilePath;
		driver.findElement(By.xpath("//input[@type='file']")).sendKeys(pdfFilePath);
		Thread.sleep(2000);
		WebElement element = driver.findElement(By.id("processTask"));

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.visibilityOf(element));
		element.click();
		String fileFullPath = downloadPath + new File(fullPath).getName().replaceAll(".pdf", ".docx");
		System.out.println(fileFullPath);
		waitForDownload(fileFullPath);
		driver.close();
		return fileFullPath;
	}

	private static List<List<String>> getDataFromWord(String wordPath, int tableNumber) throws IOException {
		List<List<String>> wordData = new LinkedList<>();
		InputStream fis = new FileInputStream(wordPath);
		XWPFDocument document = new XWPFDocument(fis);
		List<XWPFTableRow> tableRows = document.getTables().get(tableNumber).getRows();
		for (int i = 0; i < tableRows.size(); i++) {
			List<XWPFTableCell> tableCells = tableRows.get(i).getTableCells();
			List<String> word = new LinkedList<>();
			for (int j = 0; j < tableCells.size(); j++) {
				word.add(tableCells.get(j).getText());
			}
			wordData.add(word);
		}
		document.close();
		return wordData;
	}

	private static List<String> getDataFromExcelAndCompare(String excelPath, String sheetName, String wordPath,
			String destinationFolderPath) throws Exception {

		FileInputStream fis = new FileInputStream(excelPath);
		Workbook workbook = WorkbookFactory.create(fis);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

		DataFormatter df = new DataFormatter();
		Sheet sheet = workbook.getSheet(sheetName);

		FileInputStream newFis = null;
		Workbook newWorkbook = null;
		String absolutePath = "";

		List<List<String>> wordData = getDataFromWord(wordPath, 0);
		List<String> difference = new LinkedList<String>();
		boolean isFirstTime = true;

		for (int i = 0, k = 0; i <= sheet.getLastRowNum(); i++) {
			Row currentRow = sheet.getRow(i);
			if (currentRow == null)
				continue;

			int cellNumber = currentRow.getLastCellNum();
			String ref = "";
			for (int j = 0, x = 0; j < cellNumber; j++) {
				int UnMergeCellNum = j;
				Cell currentCell = currentRow.getCell(j);
				String data;

				if (currentCell == null) {
					data = ""; // Empty cell
				} else {
					data = df.formatCellValue(currentCell, evaluator);
				}

				int first = 0;
				// Check if the current cell is part of a merged region
				CellRangeAddress mergedRegion = getMergedRegion(sheet, i, j);
				if (mergedRegion != null) {
					// Get the value from the top-left cell of the merged region
					data = df.formatCellValue(
							sheet.getRow(mergedRegion.getFirstRow()).getCell(mergedRegion.getFirstColumn()), evaluator);
					j = mergedRegion.getLastColumn(); // Skip to the last column of the merged region
					first = mergedRegion.getFirstColumn();
				}
				if (UnMergeCellNum == j) {
					first = UnMergeCellNum;
				} else {
					first = mergedRegion.getFirstColumn();
				}
				if (x == 0) {
					ref = wordData.get(k).get(x).replaceAll("\\n", "");
				}

				if (isFirstTime && !data.replaceAll("\\n", "").trim()
						.equalsIgnoreCase(wordData.get(k).get(x).replaceAll("\\n", "").trim())) {

					absolutePath = copyExcelFile(excelPath, destinationFolderPath);
					newFis = new FileInputStream(excelPath);
					newWorkbook = WorkbookFactory.create(newFis);
					isFirstTime = false;
				}

				if (!data.replaceAll("\\n", "").trim()
						.equalsIgnoreCase(wordData.get(k).get(x).replaceAll("\\n", "").trim())) {

					highlightCell(newWorkbook, absolutePath, sheetName, i, first, IndexedColors.RED);
					difference
							.add("Ref: " + ref + " ==> PdfData = " + wordData.get(k).get(x).replaceAll("\\n", "").trim()
									+ " but ExcelData = " + data.replaceAll("\\n", "").trim());
				}
				x++;
			}
			k++;
		}
		if (!difference.isEmpty()) {
			newFis.close();
			newWorkbook.close();
		}
		fis.close();
		workbook.close();
		return difference;
	}

	private static CellRangeAddress getMergedRegion(Sheet sheet, int rowIdx, int colIdx) {
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
			if (mergedRegion.isInRange(rowIdx, colIdx)) {
				return mergedRegion;
			}
		}
		return null;
	}

	private static void highlightCell(Workbook workbook, String filePath, String sheetName, int rowNum, int cellNum,
			IndexedColors color) throws IOException {

		DataFormatter df = new DataFormatter();

		Sheet sheet = workbook.getSheet(sheetName);
		Row row = sheet.getRow(rowNum);
		Cell cell = row.getCell(cellNum);
		if (cell == null) {
			cell = row.createCell(cellNum);
		}

		// Get the existing cell value
		String oldValue = df.formatCellValue(cell);

		// Get the existing cell style
		CellStyle oldStyle = cell.getCellStyle();

		// Create the cell style with light red background
		CellStyle style = workbook.createCellStyle();
		style.cloneStyleFrom(oldStyle); // Preserve the original style
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// Apply the new style to the cell
		cell.setCellStyle(style);

		// Set the cell value back to its original content
		cell.setCellValue(oldValue);

		FileOutputStream fos = new FileOutputStream(filePath);
		workbook.write(fos);
		fos.close();
	}

	private static String copyExcelFile(String sourceFilePath, String destinationFolderPath) throws IOException {
		// Create file objects for source and destination
		File sourceFile = new File(sourceFilePath);
		File destinationFolder = new File(destinationFolderPath);

		// Create destination folder if it doesn't exist
		if (!destinationFolder.exists()) {
			destinationFolder.mkdirs();
		}

		// Get the timestamp
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String timestamp = dateFormat.format(new Date());

		// Generate new file name with timestamp
		String sourceFileName = sourceFile.getName();
		int dotIndex = sourceFileName.lastIndexOf('.');
		String extension = (dotIndex == -1) ? "" : sourceFileName.substring(dotIndex);
		String newFileName = sourceFileName.substring(0, dotIndex) + "_" + timestamp + extension;

		// Create FileInputStream to read from the source file
		FileInputStream fis = new FileInputStream(sourceFile);

		// Create FileOutputStream to write to the destination file
		String absolutePath = destinationFolder.getAbsolutePath() + "\\" + newFileName;
		FileOutputStream fos = new FileOutputStream(absolutePath);

		// Buffer for reading/writing data
		byte[] buffer = new byte[1024];
		int length;

		// Copy data from source to destination
		while ((length = fis.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}

		// Close streams
		fis.close();
		fos.close();

		// Return absolute path of the copied file
		return absolutePath;
	}

	private static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
//			System.out.println("File Deleted Successfully");
		} else {
//			System.out.println("File Not Found to Delete");
		}
	}

	private static void waitForDownload(String fullFilePath) throws IOException, InterruptedException {
		File file = new File(fullFilePath);
		while (true) {
			try {
				if (file.exists())
					break;
				else
					throw new Exception("Need Some time to download");
			} catch (Exception e) {
				Thread.sleep(1000);
			}
		}
	}
}