package specializedAutomation_Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
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

public class FetchSpecifiedColumnDataFromWordFileTable implements Nlp {

	@InputParams({
		@InputParam(name = "Word File Path", type = "java.lang.String"),
		@InputParam(name = "Column Index", type = "java.lang.Integer")
	})
	@ReturnType(name = "list", type = "java.util.List")
	@Override
	public List<String> getTestParameters() throws NlpException {
		return new ArrayList<>();
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		return new StringBuilder();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel response = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		String wordFilePath = (String) attributes.get("Word File Path");
		Integer columnIndex = (Integer) attributes.get("Column Index");

		try {
			List<String> dataList = readSpecificColumn(wordFilePath, columnIndex);
			response.setMessage("Column data fetched successfully from Word table.");
			response.setStatus(CommonConstants.pass);
			response.getAttributes().put("list", dataList);
		} catch (Exception e) {
			response.setMessage("Failed to fetch column data from Word table: " + e.getMessage());
			response.setStatus(CommonConstants.fail);
		}

		return response;
	}

	public static List<String> readSpecificColumn(String filePath, int columnIndex) throws IOException {

		FileInputStream fis = new FileInputStream(filePath);
		XWPFDocument document = new XWPFDocument(fis);

		List<String> columnDataList = new ArrayList<>();

		for (XWPFTable table : document.getTables()) {
			List<XWPFTableRow> rows = table.getRows();

			// Skip header row (index 0)
			for (int i = 1; i < rows.size(); i++) {
				XWPFTableRow row = rows.get(i);
				List<XWPFTableCell> cells = row.getTableCells();

				if (columnIndex < cells.size()) {
					String cellValue = cells.get(columnIndex).getText().trim();
					if (!cellValue.isEmpty()) {
						columnDataList.add(cellValue);
					}
				}
			}
		}

		document.close();
		fis.close();

		return columnDataList;
	}
	public static void main(String[] args) throws NlpException {
		NlpRequestModel mod=new NlpRequestModel();
		mod.getAttributes().put("Word File Path", "C:\\Users\\User\\Downloads\\Project Information (2).docx");
		mod.getAttributes().put("Column Index", 0);
		System.out.println(new FetchSpecifiedColumnDataFromWordFileTable().execute(mod));
	}
}
