package fileHandling_utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class FetchEntireDataFromWordFileInString implements Nlp {
	@InputParams({@InputParam(name = "Word File Path", type = "java.lang.String")})
	@ReturnType(name = "string", type = "java.lang.String")

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
		String wordFilePath = (String) attributes.get("Word File Path");

		// Your program element business logic goes here ...
		String data = "";
		try {
			data = readWordFile(wordFilePath);
			nlpResponseModel.setMessage("Fetching data from word file is Successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (IOException e) {
			nlpResponseModel.setMessage("Failed to Fetch data from Word file..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("string", data);
		return nlpResponseModel;
	}

	public static String readWordFile(String filePath) throws IOException {
		FileInputStream fis = new FileInputStream(filePath);
		XWPFDocument document = new XWPFDocument(fis);
		StringBuilder content = new StringBuilder();

		for (XWPFParagraph paragraph : document.getParagraphs()) {
			content.append(paragraph.getText()).append("\n");
		}
		document.close();
		fis.close();	
		return content.toString();
	}
} 