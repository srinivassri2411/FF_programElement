package datadriven_utility;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class ReadDataFromCsv implements Nlp {
	@InputParams({@InputParam(name = "Stream", type = "java.io.InputStream"), @InputParam(name = "UniqueID", type = "java.lang.String"),})
	@ReturnType(name = "headersAndValues", type = "java.util.Map")

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
		InputStream csvfileStream = (InputStream) attributes.get("Stream");
		String UniqueID = (String) attributes.get("UniqueID");
		Map<String, String> headersAndValues=null;
		try {
			headersAndValues = readCsvHeadersAndValuesForTestCase(csvfileStream, UniqueID);
			if (!headersAndValues.isEmpty()) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Headers and Values for TestCaseId " + UniqueID + ":"+headersAndValues);

			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("No data found for TestCaseId " + UniqueID);
			}
		}catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("No data found for TestCaseId due to ecception");
		}
		nlpResponseModel.getAttributes().put("headersAndValues", headersAndValues);
		return nlpResponseModel;
	}
	public static Map<String, String> readCsvHeadersAndValuesForTestCase(InputStream csvFileInputStream, String targetTestCaseId) throws IOException, CsvValidationException {
		Map<String, String> headersAndValues = new LinkedHashMap<>();
		InputStreamReader inputStreamReader = new InputStreamReader(csvFileInputStream);
		CSVReader reader = new CSVReader(inputStreamReader);

		String[] headers = reader.readNext(); // Assuming the first row contains headers
		String[] values;

		while ((values = reader.readNext()) != null) {
			if (values.length > 0 && values[0].equals(targetTestCaseId)) {
				for (int i = 0; i < headers.length && i < values.length; i++) {
					headersAndValues.put(headers[i], values[i]);
				}
				break; 
			}
		}


		return headersAndValues;
	}
}





