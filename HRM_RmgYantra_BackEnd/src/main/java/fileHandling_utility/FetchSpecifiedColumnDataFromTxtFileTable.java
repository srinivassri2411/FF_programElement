package fileHandling_utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class FetchSpecifiedColumnDataFromTxtFileTable implements Nlp {
	@InputParams({@InputParam(name = "Txt File Path", type = "java.lang.String"), @InputParam(name = "Column Header", type = "java.lang.String")})
	@ReturnType(name = "listOfData", type = "java.util.List")
	
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
		String txtFilePath = (String) attributes.get("Txt File Path");
		String columnHeader = (String) attributes.get("Column Header");

		// Your program element business logic goes here ...
		List<String> listOfData = null;
		try {
			listOfData = getDataFromTxtFile(txtFilePath, columnHeader);
			nlpResponseModel.setMessage("Fetching data from Txt file is Successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Fetch data from Txt file..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("listOfData", listOfData);
		return nlpResponseModel;
	}

	public static List<String> getDataFromTxtFile(String textFilePath, String columnHeader) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFilePath))) ;
		List<String> list = new LinkedList<>();
		int dataIndex = 0;
		boolean isFirstTime = true;
		String line;
		while ((line = reader.readLine()) != null) {
			if (!isFirstTime) {
				list.add(line.split("\\|")[dataIndex].trim());
			} else {
				isFirstTime = false;
				String[] parts = line.split("\\|"); // Splitting the line using the "|" delimiter
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].trim().equalsIgnoreCase(columnHeader)) {
						dataIndex = i;
						break;
					}
				}
			}
		}
		return list;
	}

	public static void main(String[] args) throws FileNotFoundException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("Txt File Path", "C:\\Users\\User\\Desktop\\Comparison\\Project Information.txt");
		nlpRequestModel.getAttributes().put("Column Header", "ProjectName");

		try {
			NlpResponseModel nlpResponseModel = new FetchSpecifiedColumnDataFromTxtFileTable().execute(nlpRequestModel);
			System.out.println(nlpResponseModel.getAttributes().get("listOfData"));
		} catch (NlpException e) {
			e.printStackTrace();
		}
	}
} 