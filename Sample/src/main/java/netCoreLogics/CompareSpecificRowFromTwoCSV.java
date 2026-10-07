package netCoreLogics;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP0ed09450-2111-4c26-9e8a-d986972e954a")
public class CompareSpecificRowFromTwoCSV implements Nlp {
	@InputParams({ @InputParam(name = "CSV1 Path", type = "java.lang.String"),
			@InputParam(name = "CSV2 Path", type = "java.lang.String"),
			@InputParam(name = "Unique Data", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.util.List")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath1 = (String) programElementsInput.get("CSV1 Path");
		String filePath2 = (String) programElementsInput.get("CSV2 Path");
		String uniqueKey = (String) programElementsInput.get("Unique Data");
		List<String> data = null;
		try {
			List<String[]> allData1 = readCSV(filePath1);
			List<String[]> allData2 = readCSV(filePath2);
			System.out.println(allData1);
			System.out.println();
			System.out.println(allData2);
			String[] row1 = findRowByUniqueKey(allData1, uniqueKey);
			String[] row2 = findRowByUniqueKey(allData2, uniqueKey);
			List<String> list1 = Arrays.asList(row1);
			List<String> list2 = Arrays.asList(row2);
			if (list1.equals(list2)) {
				nlpResponseModel.setMessage("Both Row data's are matching in the CSV files");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				data = compareRows(row1, row2);
				nlpResponseModel.setMessage("Data is not matching");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Fail to fetch" + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", data);
		return nlpResponseModel;
	}

	private static List<String[]> readCSV(String filePath) throws IOException, CsvException {
		try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
			return reader.readAll();
		}
	}

	private static String[] findRowByUniqueKey(List<String[]> allData, String uniqueKey) {
		for (String[] row : allData) {
			if (row.length > 0 && row[0].equals(uniqueKey)) {
				return row;
			}
		}
		return null;
	}

	private static List<String> compareRows(String[] row1, String[] row2) {
		int maxCols = Math.max(row1.length, row2.length);
		List<String> list = new ArrayList<>();
		for (int j = 0; j < maxCols; j++) {
			String cell1 = j < row1.length ? row1[j] : "";
			String cell2 = j < row2.length ? row2[j] : "";
			if (!cell1.equals(cell2)) {
				list.add("column " + (j + 1) + ": \"" + cell1 + "\" != \"" + cell2 + "\"");
			}
		}
		return list;
	}

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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("CSV1 Path", "C:\\Users\\Srinivas m\\Downloads\\cust_data_sms_658.csv");
		req.getAttributes().put("CSV2 Path", "C:\\Users\\Srinivas m\\Downloads\\cust_data_sms_658_1.csv");
		req.getAttributes().put("Unique Data", "zaki.ansari@netcorecloud.com");
		System.out.println(new CompareSpecificRowFromTwoCSV().execute(req));
	}
}