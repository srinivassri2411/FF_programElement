package specializedAutomation_utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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




public class Simulate_ExcelData_To_Json implements Nlp {
	@InputParams({@InputParam(name = "Txt_InputStream", type = "java.io.InputStream"), @InputParam(name = "Excel_InputStream", type = "java.io.InputStream"), @InputParam(name = "Sheet_Name", type = "java.lang.String")})
	@ReturnType(name = "Absolute_Path", type = "java.lang.String")

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
		InputStream txt_InputStream = (InputStream) attributes.get("Txt_InputStream");
		InputStream excel_InputStream = (InputStream) attributes.get("Excel_InputStream");
		String sheet_Name = (String) attributes.get("Sheet_Name");

		// Your program element business logic goes here ...

		String finalData = "";
		String absolutePath = "";
		try {
			List<Map<String, String>> entireData = getdataFromExcel(excel_InputStream, sheet_Name);
			String jsonStaticData = readTextFile(txt_InputStream);
			String copyStaticData = jsonStaticData;

			for (Map<String, String> map : entireData) {
				for (Entry<String, String> singleRowData : map.entrySet()) {
					jsonStaticData = jsonStaticData.replaceAll(singleRowData.getKey().trim(), singleRowData.getValue().trim());
				}
				finalData = finalData+jsonStaticData;
				jsonStaticData = copyStaticData;
			}
			
			absolutePath = writeTextToFile(finalData);

			nlpResponseModel.setMessage("Data Simulation is successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Simulate Data..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Absolute_Path", absolutePath);
		return nlpResponseModel;
	}

	private static List<Map<String, String>> getdataFromExcel(InputStream inputStream, String sheetName)
			throws EncryptedDocumentException, IOException {

		List<Map<String, String>> entireData = new LinkedList<Map<String, String>>();
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheet(sheetName);
		DataFormatter df = new DataFormatter();
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

		int rowCount = sheet.getLastRowNum();
		for (int i = 0; i < rowCount; i++) {
			Map<String, String> singleRowData = new LinkedHashMap<String, String>();
			int columnCount = sheet.getRow(i).getLastCellNum();
			for (int j = 0; j < columnCount; j++) {
				singleRowData.put(df.formatCellValue(sheet.getRow(0).getCell(j), evaluator), df.formatCellValue(sheet.getRow(i+1).getCell(j), evaluator));
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
		String directoryPath = System.getProperty("user.home")+File.separator+"Updated Json Files";

		// Create the directory if it doesn't exist
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs(); // creates parent directories if necessary
		}

		// Generate fileName with time stamp
		String fileName = "Product_Details_JsonData"+new SimpleDateFormat("yyyy_MM_dd_hh_mm_sss").format(new Date())+".txt";

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
} 