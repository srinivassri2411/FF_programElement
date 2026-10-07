package fileHandling_utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FetchTextFromPdfByURL implements Nlp {
	@InputParams({ @InputParam(name = "Pdf_Url", type = "java.lang.String") })
	@ReturnType(name = "Text", type = "java.lang.String")

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
		String pdfUrl = (String) attributes.get("Pdf_Url");

		// Your program element business logic goes here ...
		try {
			String text = extractTextFromPDF(pdfUrl);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fetched text from pdf using URL: " + pdfUrl);
			nlpResponseModel.getAttributes().put("Text", text);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch text from PDF.."+e);
		}
		return nlpResponseModel;
	}

	public static String extractTextFromPDF(String pdfUrl) throws IOException {
		InputStream input = new URL(pdfUrl).openStream();
		PDDocument document = PDDocument.load(input);
		String text = new PDFTextStripper().getText(document);
		document.close();
		return text;
	}
}