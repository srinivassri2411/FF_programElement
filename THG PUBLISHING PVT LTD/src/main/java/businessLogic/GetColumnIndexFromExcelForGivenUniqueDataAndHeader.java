package businessLogic;

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





public class GetColumnIndexFromExcelForGivenUniqueDataAndHeader implements Nlp {
	@InputParams({ @InputParam(name = "InputStream", type = "java.io.InputStream"),
			@InputParam(name = "Sheet Name", type = "java.lang.String"),
			@InputParam(name = "Unique Data", type = "java.lang.String"),
			@InputParam(name = "Header/Value", type = "java.lang.String") })
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
		InputStream fileInputStream = (InputStream) attributes.get("InputStream");
		String sheetName = (String) attributes.get("Sheet Name");
		String uniqueData = (String) attributes.get("Unique Data");
		String header = (String) attributes.get("Header/Value");

		// Your program element business logic goes here ...
		int index = 0;
		try {
			index = getIndex(fileInputStream, sheetName, uniqueData, header);
			nlpResponseModel.setMessage("Index for given header/value " + header + " is " + index);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to fetch index..." + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("index", index);
		return nlpResponseModel;
	}

	private static int getIndex(InputStream fis, String sheetName, String uniqueData, String header)
			throws EncryptedDocumentException, IOException {
		Workbook workbook = WorkbookFactory.create(fis);
		Sheet sheet = workbook.getSheet(sheetName);
		DataFormatter df = new DataFormatter();
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

		int rowCount = sheet.getLastRowNum();
		int index = 0;
		boolean oturForLoop = false;
		for (int i = 0; i <= rowCount; i++) {
			if (sheet.getRow(i) == null) {
				i = i++;
			} else {
				if (df.formatCellValue(sheet.getRow(i).getCell(0), evaluator).equalsIgnoreCase(uniqueData)) {
					int cellCount = sheet.getRow(i - 1).getLastCellNum();
					for (int j = 0; j <= cellCount; j++) {
						if (df.formatCellValue(sheet.getRow(i - 1).getCell(j), evaluator).equalsIgnoreCase(header)) {
							index = j;
							oturForLoop = true;
							break;
						} else {
							cellCount = sheet.getRow(i).getLastCellNum();
							if (df.formatCellValue(sheet.getRow(i).getCell(j), evaluator).equalsIgnoreCase(header)) {
								index = j;
								oturForLoop = true;
								break;
							}
						}
					}
					if (oturForLoop) {
						break;
					}
				}
			}
		}
		workbook.close();
		fis.close();
		return index;
	}

//	public static void main(String[] args) {
//		NlpRequestModel rq = new NlpRequestModel();
//		rq.getAttributes().put("File Path", "C:\\Users\\User\\Desktop\\output.xlsx");
//		rq.getAttributes().put("Sheet Name", "Sheet1");
//		rq.getAttributes().put("Unique Data", "tc03");
//		rq.getAttributes().put("Header/Value", "mass");
//
//		try {
//			NlpResponseModel res = new GetColumnIndexFromExcelForGivenUniqueDataAndHeader().execute(rq);
//			System.out.println(res.getAttributes().get("index"));
//			System.out.println("Execution Completed");
//		} catch (NlpException e) {
//			e.printStackTrace();
//		}
//	}
}