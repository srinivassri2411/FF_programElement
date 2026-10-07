package netCoreLogics;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

@Component("LIC11870_PJT1012_PE_NLP3a54a31d-a68f-4539-a704-a8343e99ed86")
public class CompareTwoCSV implements Nlp {
	@InputParams({ @InputParam(name = "CSV1 Path", type = "java.lang.String"),
			@InputParam(name = "CSV2 Path", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.util.Map")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath1 = (String) programElementsInput.get("CSV1 Path");
		String filePath2 = (String) programElementsInput.get("CSV2 Path");
		Map resultMap = null;
		try {
			List<String[]> csv1 = readCSV(filePath1);
			List<String[]> csv2 = readCSV(filePath2);
			resultMap = compareCSV(csv1, csv2);
//			System.out.println(map);
			if (resultMap.size() >= 1) {
				nlpResponseModel.setMessage("Data is not matching");
				nlpResponseModel.setStatus(CommonConstants.fail);
			} else {
				nlpResponseModel.setMessage("Both the CSV files are matching");
				nlpResponseModel.setStatus(CommonConstants.pass);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Fail to fetch" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", resultMap);
		return nlpResponseModel;
	}

	private static Map compareCSV(List<String[]> csv1, List<String[]> csv2) throws IOException {
		int maxRows = Math.max(csv1.size(), csv2.size());
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < maxRows; i++) {
			String[] row1 = i < csv1.size() ? csv1.get(i) : null;
			String[] row2 = i < csv2.size() ? csv2.get(i) : null;
			List list = compareRows(row1, row2, i + 1);
			if (list.size() >= 1) {
				map.put("Difference found at row " + (i + 1), list);
			}
		}
		return map;
	}

	private static List<String[]> readCSV(String filePath) throws IOException, CsvException {
		try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
			// Explicitly convert List<String[]> from List<String[]>
			return reader.readAll().stream().map(row -> row.clone()).collect(Collectors.toList());
		}
	}

	private static List compareRows(String[] row1, String[] row2, int rowNum) {
		int maxCols = Math.max(row1.length, row2.length);
		List list = new ArrayList<>();
		for (int j = 0; j < maxCols; j++) {
			String cell1 = j < row1.length ? row1[j] : "";
			String cell2 = j < row2.length ? row2[j] : "";

			if (!cell1.equals(cell2)) {
				list.add("column " + (j + 1) + ": \"" + cell1 + "\" != \"" + cell2 + "\"");
//				System.out.println("Difference found at row " + rowNum + ", column " + (j + 1) + ": \"" + cell1
//						+ "\" vs \"" + cell2 + "\"");
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
		System.out.println(new CompareTwoCSV().execute(req));
	}
}