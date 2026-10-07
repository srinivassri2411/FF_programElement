package businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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


public class GetRowIndexFromExcelForGivenUniqueData implements Nlp {
	@InputParams({ @InputParam(name = "InputStream", type = "java.io.InputStream"),
			@InputParam(name = "Sheet Name", type = "java.lang.String"),
			@InputParam(name = "Unique Data", type = "java.lang.String") })
	@ReturnType(name = "index", type = "java.lang.Integer")

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
		InputStream inputStream = (InputStream) attributes.get("InputStream");
		String sheetName = (String) attributes.get("Sheet Name");
		String uniqueData = (String) attributes.get("Unique Data");
		
		// Your program element business logic goes here ...
		int index = 0;
		try {
			index = getIndex(inputStream, sheetName, uniqueData);
			nlpResponseModel.setMessage("Index for given Unique Data " + uniqueData + " is " + index);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to fetch index..." + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("index", index);
		return nlpResponseModel;
	}

	private static int getIndex(InputStream inputStream, String sheetName, String uniqueData)
			throws EncryptedDocumentException, IOException {
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheet(sheetName);
		DataFormatter df = new DataFormatter();
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		
		int rowCount = sheet.getLastRowNum();
		int index = 0;
		for (int i = 0; i <= rowCount; i++) {
			if (sheet.getRow(i) == null) {
				i = i++;
			} else {
				if (df.formatCellValue(sheet.getRow(i).getCell(0), evaluator).equalsIgnoreCase(uniqueData)) {
					index = i;
					break;
				}
			}
		}
		workbook.close();
		inputStream.close();
		return index;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		NlpRequestModel rq = new NlpRequestModel();
		InputStream inputStream = new FileInputStream("C:\\Users\\fireflink1\\Downloads\\BLCompanyapi.xlsx");
		
		rq.getAttributes().put("InputStream", inputStream);
		rq.getAttributes().put("Sheet Name", "Test");
		rq.getAttributes().put("Unique Data", "BLCMAPI008");
		
		try {
			NlpResponseModel res = new GetRowIndexFromExcelForGivenUniqueData().execute(rq);
			System.out.println(res.getAttributes().get("index"));
			System.out.println("Execution Completed");
		} catch (NlpException e) {
			e.printStackTrace();
		}
	}
}
