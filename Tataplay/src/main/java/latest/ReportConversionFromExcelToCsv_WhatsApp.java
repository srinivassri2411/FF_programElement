package latest;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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

public class ReportConversionFromExcelToCsv_WhatsApp implements Nlp {
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_TIME_PATTERN);

	@InputParams({ @InputParam(name = "Excel_FilePath", type = "java.lang.String") })
	@ReturnType(name = "CSV_FilePath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String excelFilePath = (String) programElementsInput.get("Excel_FilePath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		String csvFilePath = excelFilePath.replace(".xlsx", ".csv");
		try {
			DataFormatter formatter = new DataFormatter();
			final boolean WRITE_UTF8_BOM = true;

			FileInputStream fis = new FileInputStream(excelFilePath);
			Workbook workbook = new XSSFWorkbook(fis);
			OutputStream os = Files.newOutputStream(Paths.get(csvFilePath));
			OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
			BufferedWriter writer = new BufferedWriter(osw);

			if (WRITE_UTF8_BOM) {
				writer.write('\uFEFF');
			}

			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) {

				if (row == null) {
					writer.newLine();
					continue;
				}

				int firstCell = row.getFirstCellNum();
				int lastCell = row.getLastCellNum();

				if (firstCell == -1 || lastCell == -1) {
					writer.newLine();
					continue;
				}

				int lastNonEmpty = firstCell - 1;

				for (int c = lastCell - 1; c >= firstCell; c--) {

					Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					String txt = getCellTextWithDateFormat(cell, formatter, evaluator);
					txt = normalize(txt);
					txt = sanitize(txt);

					if (!txt.isEmpty()) {
						lastNonEmpty = c;
						break;
					}
				}

				if (lastNonEmpty < firstCell) {
					writer.newLine();
					continue;
				}

				StringBuilder sb = new StringBuilder();

				for (int c = firstCell; c <= lastNonEmpty; c++) {

					Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

					String cellText = getCellTextWithDateFormat(cell, formatter, evaluator);
					cellText = normalize(cellText);
					cellText = sanitize(cellText);

					sb.append(cellText);

					if (c < lastNonEmpty) {
						sb.append("|");
					}
				}

				writer.write(sb.toString());
				writer.newLine();
			}

			writer.flush();
			os.close();
			osw.close();
			writer.close();
			workbook.close();
			fis.close();

			System.out.println("Conversion completed successfully!");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Converted from Excel to CSV. ");
			nlpResponseModel.getAttributes().put("CSV_FilePath", csvFilePath);
			ArrayList<String> fileList = new ArrayList<String>();
			nlpResponseModel.setOutputLocalFilePaths(fileList);

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Convert from Excel to CSV. " + writer.toString());
		}

		return nlpResponseModel;
	}

	private static String getCellTextWithDateFormat(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {

		if (cell == null)
			return "";

		try {

			if (cell.getCellType() == CellType.FORMULA) {

				CellType evaluatedType;

				try {
					evaluatedType = evaluator.evaluateFormulaCell(cell);
				} catch (Exception ex) {
					return formatter.formatCellValue(cell);
				}

				switch (evaluatedType) {

				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						Date d = cell.getDateCellValue();
						return (d == null) ? "" : SDF.format(d);
					} else {
						return formatter.formatCellValue(cell);
					}

				case STRING:
					return cell.getStringCellValue();

				case BOOLEAN:
					return String.valueOf(cell.getBooleanCellValue());

				case ERROR:
					return ""; // ignore excel errors

				case BLANK:
					return "";

				default:
					return formatter.formatCellValue(cell);
				}
			}

			if (cell.getCellType() == CellType.NUMERIC) {

				if (DateUtil.isCellDateFormatted(cell)) {
					Date d = cell.getDateCellValue();
					return (d == null) ? "" : SDF.format(d);
				}
			}

		} catch (Exception e) {
			return formatter.formatCellValue(cell);
		}

		return formatter.formatCellValue(cell);
	}

	private static String normalize(String s) {

		if (s == null)
			return "";

		return s.replaceAll("\\r?\\n", " ").replace("\r", " ").trim();
	}

	private static String sanitize(String s) {

		if (s == null)
			return "";

		String t = s;

		t = t.replace("\"\"", "");
		t = t.replace("\"", "");
		t = t.replace(",", "");

		return t.trim();
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("Excel_FilePath",
				"C:\\Users\\praveenj889\\Fireflink\\EXEC35272_20260318_012955\\EXEC35272.xlsx");

		NlpResponseModel res = new ReportConversionFromExcelToCsv_WhatsApp().execute(req);

		System.out.println(res.getAttributes().get("CSV_FilePath"));
	}
}