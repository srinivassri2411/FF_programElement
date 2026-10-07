
package socketIO;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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




public class ReadDataFromExcelAsMap implements Nlp {
	@InputParams({ @InputParam(name = "Excel Stream", type = "java.io.InputStream"),
		@InputParam(name = "Sheet Name", type = "java.lang.String") })
	@ReturnType(name = "entireData", type = "java.util.Map")

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
		InputStream excelStream = (InputStream) attributes.get("Excel Stream");
		String sheetName = (String) attributes.get("Sheet Name");

		// Your program element business logic goes here ...
		try {
			Map<String, List<String>> entireData = getDataFromExcelAsMap(excelStream, sheetName);
			nlpResponseModel.getAttributes().put("entireData", entireData);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fetched data from excel.");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch data from excel."+e);
		}
		return nlpResponseModel;
	}

	public Map<String, List<String>> getDataFromExcelAsMap(InputStream excelStram, String sheetName)
			throws EncryptedDocumentException, IOException {
		Map<String, List<String>> entireData = new LinkedHashMap<String, List<String>>();
		Workbook workbook = WorkbookFactory.create(excelStram);
		DataFormatter df = new DataFormatter();
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

		Sheet sheet = workbook.getSheet(sheetName);
		int rowCount = sheet.getPhysicalNumberOfRows();
		for (int i = 1; i < rowCount; i++) {
			List<String> singleUserData = new LinkedList<String>();
			int columnCount = sheet.getRow(i).getPhysicalNumberOfCells();
			for (int j = 1; j < columnCount; j++) {
				singleUserData.add(df.formatCellValue(sheet.getRow(i).getCell(j), evaluator));
			}
			entireData.put(df.formatCellValue(sheet.getRow(i).getCell(0), evaluator), singleUserData);
		}
		workbook.close();
		excelStram.close();
		return entireData;
	}
}