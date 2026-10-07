package mainFrameUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GenerateTxnFiles implements Nlp {

	@InputParams({ @InputParam(name = "Excel_FilePath", type = "java.lang.String"),
			@InputParam(name = "Sheet_Name", type = "java.lang.String") })
	@ReturnType(name = "Generated_Swift_FilePaths", type = "java.util.List")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		List<String> files;

		try {

			String excelFilePath = (String) attributes.get("Excel_FilePath");
			String sheetName = (String) attributes.get("Sheet_Name");

			String tempDir = System.getProperty("java.io.tmpdir");
			String outputDir = tempDir + File.separator + "swift_mt103_output";

			new File(outputDir).mkdirs();

			files = processExcel(excelFilePath, outputDir, sheetName);

			nlpResponseModel.getAttributes().put("Generated_Swift_FilePaths", files);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Swift files generation is successful.");

		} catch (Exception e) {

			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to generate Swift files: " + writer.toString());
			//throw new NlpException(e.getMessage());
		}

		return nlpResponseModel;
	}

	/**
	 * Converts Excel date cell to SWIFT format yyMMdd
	 */
	public static String convertDate(Cell cell) throws Exception {

		SimpleDateFormat output = new SimpleDateFormat("yyMMdd");

		if (cell == null)
			return "";

		if (DateUtil.isCellDateFormatted(cell)) {
			return output.format(cell.getDateCellValue());
		}

		DataFormatter formatter = new DataFormatter();
		String value = formatter.formatCellValue(cell);

		String[] patterns = { "dd-MM-yyyy", "d/M/yy", "d/M/yyyy", "dd/MM/yyyy" };

		for (String pattern : patterns) {
			try {
				return output.format(new SimpleDateFormat(pattern).parse(value));
			} catch (Exception ignored) {
			}
		}

		throw new RuntimeException("Unsupported date format: " + value);
	}

	/**
	 * Generates MT103 SWIFT message
	 */
	public static String generateMT103(String senderReference, String bankOperationCode, String swiftDate,
			String currency, String amount, String instructedAmount, String orderingAccount, String orderingName,
			String orderingAddress, String beneficiaryName, String beneficiaryAddress, String charges) {

		return "{1:F01BANKBEBBAXXX0000000000}" + "{2:O1031001050106BANKDEFFXXXX22221234560501061001N}" + "{4:\n"
				+ ":20:" + senderReference + "\n" + ":23B:" + bankOperationCode + "\n" + ":32A:" + swiftDate + currency
				+ amount + "\n" + ":33B:" + instructedAmount + "\n" + ":50K:/" + orderingAccount + "\n" + orderingName
				+ "\n" + orderingAddress + "\n" + ":59:/" + senderReference + orderingAccount + "\n" + beneficiaryName
				+ "\n" + beneficiaryAddress + "\n" + ":71A:" + charges + "\n" + "-}";
	}

	/**
	 * Reads Excel and generates MT103 files
	 */
	public static List<String> processExcel(String excelPath, String outputDir, String sheetName) throws Exception {

		List<String> fileNames = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(excelPath); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(sheetName);

			DataFormatter formatter = new DataFormatter(true);

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				String senderReference = formatter.formatCellValue(row.getCell(0));
				String bankOperationCode = formatter.formatCellValue(row.getCell(1));
				String swiftDate = convertDate(row.getCell(2));
				String currency = formatter.formatCellValue(row.getCell(3));
				String amount = formatter.formatCellValue(row.getCell(4));
				String currencyInstructedAmount = formatter.formatCellValue(row.getCell(5));
				String orderingCustomerAccount = formatter.formatCellValue(row.getCell(6));
				String orderingCustomerName = formatter.formatCellValue(row.getCell(7));
				String orderingCustomerAddress = formatter.formatCellValue(row.getCell(8));
//				String beneficiaryCustomerAccount = formatter.formatCellValue(row.getCell(9));
				String beneficiaryCustomerName = formatter.formatCellValue(row.getCell(9));
				String beneficiaryCustomerAddress = formatter.formatCellValue(row.getCell(10));
				String detailsOfCharges = formatter.formatCellValue(row.getCell(11));

				String mt103 = generateMT103(senderReference, bankOperationCode, swiftDate, currency, amount,
						currencyInstructedAmount, orderingCustomerAccount, orderingCustomerName,
						orderingCustomerAddress, beneficiaryCustomerName, beneficiaryCustomerAddress, detailsOfCharges);

				String fileName = "MT103_" + senderReference + ".txt";

				File file = new File(outputDir + File.separator + fileName);

				try (FileWriter writer = new FileWriter(file)) {
					writer.write(mt103);
				}

				fileNames.add(file.getAbsolutePath());
			}
		}

		return fileNames;
	}

	/**
	 * Local testing
	 */
	public static void main(String[] args) throws NlpException {

		NlpRequestModel req = new NlpRequestModel();

		req.getAttributes().put("Excel_FilePath", "C:\\Users\\User\\Downloads\\Swift_Message_DataFile.xlsx");

		req.getAttributes().put("Sheet_Name", "Txn_Info");

		System.out.println(new GenerateTxnFiles().execute(req).getAttributes().get("Generated_Swift_FilePaths"));
	}
}