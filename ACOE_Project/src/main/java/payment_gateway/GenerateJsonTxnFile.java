package payment_gateway;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

public class GenerateJsonTxnFile implements Nlp {
	@InputParams({@InputParam(name = "JsonSchema_FilePath", type = "java.lang.String"), 
		@InputParam(name = "Excel_FilePath", type = "java.lang.String"),
		@InputParam(name = "Sheet_Name", type = "java.lang.String")
	})
	@ReturnType(name = "Generated_Json_FilePath", type = "java.lang.String")

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

		try {
			String jsonSchemaFilePath = (String) attributes.get("JsonSchema_FilePath");
			String excelFilePath = (String) attributes.get("Excel_FilePath");
			String sheetName = (String) attributes.get("Sheet_Name");

			generateAndWriteDataToExcel(excelFilePath, sheetName);
			List<Map<String, String>> entireData = getdataFromExcel(excelFilePath, sheetName);
			String jsonStaticData = readTextFile(jsonSchemaFilePath);
			String copyStaticData = jsonStaticData;
			
			List<String> listOfJson = new ArrayList<String>();
			
			for (Map<String, String> map : entireData) {
				for (Entry<String, String> singleRowData : map.entrySet()) {
					jsonStaticData = jsonStaticData.replaceAll(singleRowData.getKey().trim(), singleRowData.getValue().trim());
				}
				listOfJson.add(jsonStaticData);
				jsonStaticData = copyStaticData;
			}
			
			// Add necessary new line characters
			String jsonString = "[";
			for (int i = 0; i < listOfJson.size(); i++) {
				if (i==listOfJson.size()-1) {
					jsonString = jsonString + listOfJson.get(i) +"]";
				} else {
					jsonString = jsonString + listOfJson.get(i) +",\n";
				}
			}
			
			// Get Generated JSON file path
			String absolutePath = writeTextToFile(jsonSchemaFilePath, jsonString);
			nlpResponseModel.getAttributes().put("Generated_Json_FilePath", absolutePath);
			
			nlpResponseModel.setMessage("Json Transaction file generation is successful.");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to generate Json Transaction file.."+writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		return nlpResponseModel;
	}

	private static void generateAndWriteDataToExcel(String filePath, String sheetName) throws IOException, InterruptedException{
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
			String transactionId = "TR"+generateDateWithOffset(tatDate,"yyyyMMddhhmmssSSS");
			String transctionDate = generateDateWithOffset(tatDate,"dd-MM-yyyy") ;
			String transactionTime = generateDateWithOffset(tatDate,"hh:mm:ss:SSS");

			for (int j = 0; j < columnCount; j++) {
				String cellData = df.formatCellValue(sheet.getRow(0).getCell(j), evaluator);

				if (cellData.equalsIgnoreCase("TXN_ID_VAR")) {
					sheet.getRow(i).getCell(j).setCellValue(transactionId);
				} else if(cellData.equalsIgnoreCase("TXN_DATE_VAR")) {
					sheet.getRow(i).getCell(j).setCellValue(transctionDate);
				} else if(cellData.equalsIgnoreCase("TXN_TIME_VAR")) {
					sheet.getRow(i).getCell(j).setCellValue(transactionTime);
				}
			}
			Thread.sleep(100);
		}
		OutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		workbook.close();
		inputStream.close();
		outputStream.close();
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

	private static String readTextFile(String txtFilePath) throws IOException {
		StringBuilder content = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(txtFilePath));
		String line;
		while ((line = reader.readLine()) != null) {
			content.append(line);
		}
		if (reader != null) {
			reader.close();
		}
		return content.toString();
	}

	private static String writeTextToFile(String jsonSchemaFilePath, String data) throws IOException {
		BufferedWriter writer = null;

		// Create the directory if it doesn't exist
		String directoy = new File(jsonSchemaFilePath).getParent();

		// Generate fileName with time stamp
		String fileName = "Json_Txn_file_" + new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date())
				+ ".txt";

		// Create the file in the specified directory
		File file = new File(directoy+ File.separator + fileName);

		// Write data to the file
		writer = new BufferedWriter(new FileWriter(file));
		writer.write(data);
		if (writer != null) {
			writer.close();
		}
		return file.getAbsolutePath();
	}

	private static String generateDateWithOffset(String offset, String format) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(offset));
		return new SimpleDateFormat(format).format(calendar.getTime());
	}
	
	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("JsonSchema_FilePath", "C:\\Users\\User\\Fireflink Home\\Json_Txn_SchemaFile.txt");
		req.getAttributes().put("Excel_FilePath", "C:\\Users\\User\\Fireflink Home\\Json_DataFile.xlsx");
		req.getAttributes().put("Sheet_Name", "Txn_Info");

		System.out.println(new GenerateJsonTxnFile().execute(req).getAttributes().get("Generated_Json_FilePath"));
	}

}