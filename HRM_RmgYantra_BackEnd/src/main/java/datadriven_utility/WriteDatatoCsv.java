package datadriven_utility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class WriteDatatoCsv implements Nlp {
	@InputParams({@InputParam(name = "Stream", type = "java.io.InputStream"), @InputParam(name = "UniqueID", type = "java.lang.String"),@InputParam(name = "Header", type = "java.lang.String"),@InputParam(name = "Data", type = "java.lang.String")})
	//@ReturnType(name = "headersAndValues", type = "java.util.Map")

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
		InputStream csvFileInputStream = (InputStream) attributes.get("Stream");
		String UniqueID = (String) attributes.get("UniqueID");
		String Header = (String) attributes.get("Header");
		String Data = (String) attributes.get("Data");
		try {
			updateCsvColumn(csvFileInputStream, UniqueID, Header, Data);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Succesfully written back the data in csv file based on header "+Header);
		} catch (CsvValidationException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("failed to write back the data in csv file based on header "+Header);			
			e.printStackTrace();
		} catch (IOException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("failed to write back the data in csv file based on header "+Header);
			e.printStackTrace();
		}
		return nlpResponseModel;

	}
	public static void updateCsvColumn(InputStream csvFileInputStream, String testCaseId, String columnName, String newValue) throws IOException, CsvValidationException {
		List<String[]> lines = new ArrayList<>();
		InputStreamReader inputStreamReader=new InputStreamReader(csvFileInputStream);
		CSVReader reader = new CSVReader(inputStreamReader);
		String[] headers = reader.readNext(); 
		lines.add(headers); 
		int testCaseIdIndex = -1;
		int columnIndex = -1;
		// Find the index of the specified column
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].equals(columnName)) {
				columnIndex = i;
				break;
			}
		}
		if (columnIndex != -1) {
			String[] values;
			while ((values = reader.readNext()) != null) {
				// Check if the TestCaseId matches in the first column
				if (values.length > 0 && values[0].equals(testCaseId)) {
					// Update the value for the specified column
					values[columnIndex] = newValue;
				}
				lines.add(values); // Add the current line to the list
			}
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = csvFileInputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, bytesRead);
		}
		Writer writer = new OutputStreamWriter(byteArrayOutputStream);
		writeCsvFile(writer, lines);
	}
	private static void writeCsvFile(Writer writer, List<String[]> lines) throws IOException {

		CSVWriter csvWrite = new CSVWriter(writer);
		csvWrite.writeAll(lines);
		
	}


}





