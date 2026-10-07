package payment_gateway;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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

public class GenerateSwiftTxnFiles implements Nlp {
	@InputParams({ @InputParam(name = "SwiftSchema_FilePath", type = "java.lang.String"),
		@InputParam(name = "Excel_FilePath", type = "java.lang.String"),
		@InputParam(name = "Sheet_Name", type = "java.lang.String")
	})
	@ReturnType(name = "Generated_Swift_FilePaths", type = "java.util.List")
	
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		try {
			String swiftSchemaFilePath = (String) attributes.get("SwiftSchema_FilePath");
			String excelFilePath = (String) attributes.get("Excel_FilePath");
			String sheetName = (String) attributes.get("Sheet_Name");
			
			List<String> swiftFilePaths = createSwiftTxnFiles(excelFilePath, sheetName, swiftSchemaFilePath);
			
			nlpResponseModel.getAttributes().put("Generated_Swift_FilePaths", swiftFilePaths);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Swift files generation is successful.");
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to generate Swift files."+writer.toString());
		}
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("SwiftSchema_FilePath", "C:\\Workspaces\\OpenSource\\RapiPay\\Swift_Simuation_Files\\Swift_Message_SchemaFile.txt");
		req.getAttributes().put("Excel_FilePath", "C:\\Workspaces\\OpenSource\\RapiPay\\Swift_Simuation_Files\\Swift_Message_DataFile.xlsx");
		req.getAttributes().put("Sheet_Name", "Txn_Info");
		req.getAttributes().put("Generated_SwiftFiles_Directory", "C:\\Workspaces\\OpenSource\\RapiPay\\Swift_Simuation_Files");

		System.out.println(new GenerateSwiftTxnFiles().execute(req).getAttributes().get("Generated_Swift_FilePaths"));
	}


	public static List<String> createSwiftTxnFiles(String excelFilePath, String sheetName, String swiftFormatFilePath) {
		List<String> pathsList = new LinkedList<String>();
		
		generateAndWriteDataToExcelForSwift(excelFilePath, sheetName);
		List<Map<String, String>> entireData = getDataFromExcel(excelFilePath, sheetName);
		String xmlStaticData = readDataFromTextFile(swiftFormatFilePath);
		String copyStaticData = xmlStaticData;

		File folder = new File(new File(excelFilePath).getParent() + File.separator + getDateAndTimeInSpecifiedFormat("yyyy_MM_dd_hh_mm_ss"));
		folder.mkdir();
		
		String finalString = "";
		
		for (Map<String, String> map : entireData) {
			for (Entry<String, String> singleRowData : map.entrySet()) {
				xmlStaticData = xmlStaticData.replaceAll(singleRowData.getKey().trim(), singleRowData.getValue().trim());
			}
			finalString ="	<Transaction>\n"+xmlStaticData+"	</Transaction>\n";
			xmlStaticData = copyStaticData;
			String txtFilePath = writeDataToTxtFile(folder.getAbsolutePath(), map.get("TXN_REF_ID_VAR"),"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Transactions>\n"+finalString+"</Transactions>");
			pathsList.add(txtFilePath);
		}
		return pathsList;
	}

	public static String writeDataToTxtFile(String directoryPath, String fileName, String data) {
		BufferedWriter writer = null;
		String path = "";

		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		File file = new File(directory, fileName+".txt");

		try {
			path =file.getCanonicalPath();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	public static String getDateAndTimeInSpecifiedFormat(String format) {
		return new SimpleDateFormat(format).format(new Date()).toString();
	}

	public static String readDataFromTextFile(String txtFilePath) {
		StringBuilder content = new StringBuilder();
		try {
			InputStream inputStream = new FileInputStream(txtFilePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			if (reader != null) {
				reader.close();
			}
		}catch (Exception e) {
		}
		return content.toString();
	}

	public static List<Map<String, String>> getDataFromExcel(String excelPath, String sheetName) {
		List<Map<String, String>> entireData = new LinkedList<>();

		try (InputStream inputStream = new FileInputStream(excelPath);
				Workbook workbook = WorkbookFactory.create(inputStream)) {

			Sheet sheet = workbook.getSheet(sheetName);

			Row headerRow = sheet.getRow(0);
			if (headerRow == null) {
				throw new IllegalArgumentException("The sheet is empty.");
			}

			int rowCount = sheet.getLastRowNum();
			int columnCount = headerRow.getLastCellNum();

			for (int i = 1; i <= rowCount; i++) {
				Row currentRow = sheet.getRow(i);
				if (currentRow == null) {
					continue;
				}

				Map<String, String> singleRowData = new LinkedHashMap<>();
				for (int j = 0; j < columnCount; j++) {
					String header = getCellValue(headerRow.getCell(j));

					Cell cell = currentRow.getCell(j);
					String cellValue = cell == null ? "" : getCellValue(cell);

					singleRowData.put(header, cellValue);
				}
				entireData.add(singleRowData);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return entireData;
	}

	private static String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				// Use BigDecimal to prevent scientific notation for large numbers
				return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
			}
		case BOOLEAN:
			return Boolean.toString(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		case BLANK:
			return "";
		default:
			return "";
		}
	}

	public static void generateAndWriteDataToExcelForSwift(String filePath, String sheetName) {
		try {
			InputStream inputStream = new FileInputStream(filePath);
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheet(sheetName);
			DataFormatter df = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			int rowCount = sheet.getPhysicalNumberOfRows();
			int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();

			int tatDateColumn = 0 ;
			for (int i = 0; i < columnCount; i++) {
				if (df.formatCellValue(sheet.getRow(0).getCell(i), evaluator).trim().equalsIgnoreCase("TAT_DATE")) {
					tatDateColumn = i;
					break;
				}
			}

			for (int i = 1; i < rowCount; i++) {
				String tatDate = df.formatCellValue(sheet.getRow(i).getCell(tatDateColumn), evaluator).trim();
				String txnRefId = "TR"+generateDateWithOffset(tatDate,"yyyyMMddhhmmssSSS");
				String txnDateTime = generateDateWithOffset(tatDate,"yyyy-MM-dd'T'HH:mm:ss") ;
				String paymentSettlementDate = generateDateWithOffset(tatDate,"yyyy-MM-dd");

				for (int j = 0; j < columnCount; j++) {
					String cellData = df.formatCellValue(sheet.getRow(0).getCell(j), evaluator);

					if (cellData.equalsIgnoreCase("TXN_REF_ID_VAR")) {
						sheet.getRow(i).getCell(j).setCellValue(txnRefId);
					} else if(cellData.equalsIgnoreCase("TXN_DATE_TIME_VAR")) {
						sheet.getRow(i).getCell(j).setCellValue(txnDateTime);
					} else if(cellData.equalsIgnoreCase("PAYMENT_SETTLEMENT_DATE_VAR")) {
						sheet.getRow(i).getCell(j).setCellValue(paymentSettlementDate);
					}
				}
				Thread.sleep(100);
			}
			OutputStream outputStream = new FileOutputStream(filePath);
			workbook.write(outputStream);
			workbook.close();
			inputStream.close();
			outputStream.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String generateDateWithOffset(String offset, String format) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(offset));
		return new SimpleDateFormat(format).format(calendar.getTime());
	}

}
