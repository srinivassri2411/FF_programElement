package specializedAutomation_utility;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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




public class SimulateInvoiceData implements Nlp {
	@InputParams({ @InputParam(name = "ExcelFilePath", type = "java.lang.String"),
			@InputParam(name = "SheetName", type = "java.lang.String"),
			@InputParam(name = "TxtFilePath", type = "java.io.InputStream") })
	@ReturnType(name = "absolutePathOfJsonsFile", type = "java.lang.String")

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
		String excelFilePath = (String) attributes.get("ExcelFilePath");
		String sheetName = (String) attributes.get("SheetName");
		InputStream txtFilePath = (InputStream) attributes.get("TxtFilePath");

		// Your program element business logic goes here ...
		String absolutePath = "";
		try {
			List<List<List<String>>> AllInvoiceData = generateAndWriteDataToExcel(excelFilePath, sheetName);
			List<Map<String, String>> entireData = getdataFromExcel(excelFilePath, sheetName);
			String jsonStaticData = readTextFile(txtFilePath);

			List<String> listOfJson = new ArrayList<String>();
			List<String> listOfSingleInoiceProductsStaticData = new ArrayList<String>();

			String staticDataWithProductDetails = "";

			for (int i = 0; i < AllInvoiceData.size(); i++) {
				staticDataWithProductDetails = "";
				for (int j = 0; j < AllInvoiceData.get(i).get(0).size(); j++) {
					String productObject = "{\"Product_Id\":\"PRODUCT_ID_VAR\",\"Product_Name\":\"PRODUCT_NAME_VAR\",\"Quantity\":\"QUANTITY_VAR\",\"Rate\":\"RATE_VAR\",\"Amount\":\"AMOUNT_VAR\"}";
					String productArray = productObject
							.replaceAll("PRODUCT_ID_VAR", AllInvoiceData.get(i).get(0).get(j))
							.replaceAll("PRODUCT_NAME_VAR", AllInvoiceData.get(i).get(1).get(j))
							.replaceAll("RATE_VAR", AllInvoiceData.get(i).get(2).get(j))
							.replaceAll("QUANTITY_VAR", AllInvoiceData.get(i).get(3).get(j))
							.replaceAll("AMOUNT_VAR", AllInvoiceData.get(i).get(4).get(j));
					staticDataWithProductDetails = staticDataWithProductDetails + productArray + ",";
				}
				staticDataWithProductDetails = staticDataWithProductDetails.substring(0,
						staticDataWithProductDetails.length() - 1);
				String replaceObj = "{\"Product_Id\":\"PRODUCT_ID_VAR\",\"Product_Name\":\"PRODUCT_NAME_VAR\",\"Quantity\":\"QUANTITY_VAR\",\"Rate\":\"RATE_VAR\",\"Amount\":\"AMOUNT_VAR\"}"
						.replace("\\", "");
				staticDataWithProductDetails = jsonStaticData.replace(replaceObj, staticDataWithProductDetails);
				listOfSingleInoiceProductsStaticData.add(staticDataWithProductDetails);
			}

			int count = 0;
			for (Map<String, String> map : entireData) {
				String correspondingJson = "";
				correspondingJson = listOfSingleInoiceProductsStaticData.get(count);
				for (Entry<String, String> singleRowData : map.entrySet()) {
					if (!singleRowData.getKey().trim().equalsIgnoreCase("RATE_VAR")) {
						correspondingJson = correspondingJson.replace(singleRowData.getKey().trim(),
								singleRowData.getValue().trim());
					}
				}
				listOfJson.add(correspondingJson);
				count++;
			}

			// Get jsons file path
			absolutePath = writeTextToFile(listOfJson.toString());

			// Post request
			int statusCode = postRequest(absolutePath);
			if (statusCode == 200) {
				nlpResponseModel.setMessage("Data Simulated successfully...");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("Failed to Simulate data..." + statusCode + " Status code");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to Simulate data..." + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("absolutePathOfJsonsFile", absolutePath);
		return nlpResponseModel;
	}

	private static List<List<List<String>>> generateAndWriteDataToExcel(String filePath, String sheetName)
			throws IOException, InterruptedException {
		InputStream inputStream = new FileInputStream(filePath);
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheet(sheetName);
		DataFormatter df = new DataFormatter();
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

		int rowCount = sheet.getPhysicalNumberOfRows();
		int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();
		List<List<List<String>>> AllInvoiceDetails = new LinkedList<List<List<String>>>();

		int tatDateColumn = 0;
		for (int i = 0; i < columnCount; i++) {
			if (df.formatCellValue(sheet.getRow(0).getCell(i), evaluator).trim().equalsIgnoreCase("TAT_DATE")) {
				tatDateColumn = i;
				break;
			}
		}

		for (int i = 1; i < rowCount; i++) {
			List<List<String>> invoiceInfo = new LinkedList<List<String>>();
			String tatDate = df.formatCellValue(sheet.getRow(i).getCell(tatDateColumn), evaluator).trim();
			String invoiceId = "INV_" + generateDateWithOffset(tatDate, "yyyyMMddhhmmssSSS");
			String date = generateDateWithOffset(tatDate, "dd MMMM yyyy");
			int noOfProducts = 0;
			String amount = "";
			int subTotal = 0;
			int tax = 0;
			String taxRate = "";
			List<String> field = null;

			for (int j = 0; j < columnCount; j++) {
				String cellDataHeader = df.formatCellValue(sheet.getRow(0).getCell(j), evaluator);
				String cellDataValue = df.formatCellValue(sheet.getRow(i).getCell(j), evaluator);
				if (cellDataHeader.equalsIgnoreCase("INVOICE_ID_VAR")) {
					sheet.getRow(i).getCell(j).setCellValue(invoiceId);
				} else if (cellDataHeader.equalsIgnoreCase("INVOICE_DATE_VAR")) {
					sheet.getRow(i).getCell(j).setCellValue(date);
				} else if (cellDataHeader.equalsIgnoreCase("PRODUCT_ID_VAR")
						|| cellDataHeader.equalsIgnoreCase("PRODUCT_NAME_VAR")
						|| cellDataHeader.equalsIgnoreCase("QUANTITY_VAR")
						|| cellDataHeader.equalsIgnoreCase("RATE_VAR")) {
					field = new LinkedList<String>();
					noOfProducts = cellDataValue.split("\\|").length;
					for (int k = 0; k < noOfProducts; k++) {
						field.add(cellDataValue.split("\\|")[k].trim());
					}
					invoiceInfo.add(field);
				} else if (cellDataHeader.equalsIgnoreCase("AMOUNT_VAR")) {
					field = new LinkedList<String>();
					for (int k = 0; k < noOfProducts; k++) {
						int AmountOfSingleProduct = (Integer.parseInt(invoiceInfo.get(2).get(k)))
								* (Integer.parseInt(invoiceInfo.get(3).get(k)));
						field.add(Integer.toString(AmountOfSingleProduct));
						amount = amount + AmountOfSingleProduct + "|";
						subTotal = subTotal + AmountOfSingleProduct;
					}
					invoiceInfo.add(field);
					sheet.getRow(i).getCell(j).setCellValue(amount.substring(0, amount.length() - 1));
				} else if (cellDataHeader.equalsIgnoreCase("SUBTOTAL_VAR")) {
					sheet.getRow(i).getCell(j).setCellValue(subTotal);
				} else if (cellDataHeader.equalsIgnoreCase("TAX_RATE_VAR")) {
					taxRate = df.formatCellValue(sheet.getRow(i).getCell(j), evaluator);
				} else if (cellDataHeader.equalsIgnoreCase("TAX_VAR")) {
					tax = (subTotal * Integer.parseInt(taxRate)) / 100;
					sheet.getRow(i).getCell(j).setCellValue(tax);
				} else if (cellDataHeader.equalsIgnoreCase("TOTAL_VAR")) {
					int total = subTotal + tax;
					sheet.getRow(i).getCell(j).setCellValue(total);
				}
			}
			AllInvoiceDetails.add(invoiceInfo);
			Thread.sleep(100);
		}
		OutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		workbook.close();
		inputStream.close();
		outputStream.close();
		return AllInvoiceDetails;
	}

	private static List<Map<String, String>> getdataFromExcel(String excelPath, String sheetName)
			throws EncryptedDocumentException, IOException {

		List<Map<String, String>> entireData = new LinkedList<Map<String, String>>();
		InputStream inputStream = new FileInputStream(excelPath);
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheet(sheetName);
		DataFormatter df = new DataFormatter();
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

		int rowCount = sheet.getLastRowNum();
		for (int i = 0; i < rowCount; i++) {
			Map<String, String> singleRowData = new LinkedHashMap<String, String>();
			int columnCount = sheet.getRow(i).getLastCellNum();
			for (int j = 0; j < columnCount; j++) {
				singleRowData.put(df.formatCellValue(sheet.getRow(0).getCell(j), evaluator),
						df.formatCellValue(sheet.getRow(i + 1).getCell(j), evaluator));
			}
			entireData.add(singleRowData);
		}
		workbook.close();
		inputStream.close();
		return entireData;
	}

	private static String readTextFile(InputStream inputStream) throws IOException {
		StringBuilder content = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = reader.readLine()) != null) {
			content.append(line).append("\n");
		}
		if (reader != null) {
			reader.close();
		}
		return content.toString();
	}

	private static String writeTextToFile(String data) throws IOException {
		BufferedWriter writer = null;
		String directoryPath = System.getProperty("user.home") + File.separator + "Updated Json Files";

		// Create the directory if it doesn't exist
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs(); // creates parent directories if necessary
		}

		// Generate fileName with time stamp
		String fileName = "Invoice_Details_JsonData_" + new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date())
				+ ".txt";

		// Create the file in the specified directory
		File file = new File(directory, fileName);

		// Write data to the file
		writer = new BufferedWriter(new FileWriter(file));
		writer.write(data);
		if (writer != null) {
			writer.close();
		}
		return file.getAbsolutePath();
	}

	private static int postRequest(String filePath) {
		baseURI = "http://49.249.29.5:8090";
		return given().multiPart(new File(filePath)).post("/invoices").getStatusCode();
	}

	private static String generateDateWithOffset(String offset, String format) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(offset));
		return new SimpleDateFormat(format).format(calendar.getTime());
	}

	public static void main(String[] args) throws FileNotFoundException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("ExcelFilePath", "C:\\Users\\User\\Desktop\\Invoice_Simulation\\Dymaic_Data.xlsx");
		req.getAttributes().put("SheetName", "DynamicData");
		InputStream in = new FileInputStream("C:\\Users\\User\\Desktop\\Invoice_Simulation\\Static_JsonFile.txt");
		req.getAttributes().put("TxtFilePath", in);

		try {
			NlpResponseModel res = new SimulateInvoiceData().execute(req);
			System.out.println(res.getAttributes().get("absolutePathOfJsonsFile"));
		} catch (NlpException e) {
			e.printStackTrace();
		}
	}
}