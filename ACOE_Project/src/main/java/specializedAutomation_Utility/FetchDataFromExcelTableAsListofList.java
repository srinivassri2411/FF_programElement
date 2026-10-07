package specializedAutomation_Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class FetchDataFromExcelTableAsListofList implements Nlp {
	@InputParams({@InputParam(name = "FileInputStream", type = "java.io.InputStream"), @InputParam(name = "TableNumber", type = "java.lang.Integer")})
	@ReturnType(name = "list", type = "java.util.List")

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
		FileInputStream fileInputStream = (FileInputStream) attributes.get("FileInputStream");
		Integer tableNumber = (Integer) attributes.get("TableNumber");

		// Your program element business logic goes here ...
		List<List<String>> list = null;
		try {
			list = getDataFromWord(fileInputStream, tableNumber);
			nlpResponseModel.setMessage("Fetching data from word file is Successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (IOException e) {
			nlpResponseModel.setMessage("Failed to Fetch data from word file..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("list", list);
		return nlpResponseModel;
	}

	private static List<List<String>> getDataFromWord(FileInputStream fis, int tableNumber) throws IOException {
		List<List<String>> wordData = new LinkedList<>();
		XWPFDocument document = new XWPFDocument(fis);
		List<XWPFTableRow> tableRows = document.getTables().get(tableNumber-1).getRows();
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
} 