package specializedAutomation_Utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
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
		String pdfUrl = (String) attributes.get("Pdf_Url");

		try {
			String text = extractTextFromPDF(pdfUrl);
			response.setStatus(CommonConstants.pass);
			response.setMessage("Fetched text successfully from PDF URL.");
			response.getAttributes().put("Text", text);
		} catch (Exception e) {
			response.setStatus(CommonConstants.fail);
			response.setMessage("Failed to fetch text from PDF: " + e.getMessage());
		}
		return response;
	}

	public static String extractTextFromPDF(String pdfUrl) throws IOException {

		URL url = new URL(pdfUrl);

		try (InputStream inputStream = url.openStream()) {

			byte[] pdfBytes = inputStream.readAllBytes(); // Java 9+
			try (PDDocument document = Loader.loadPDF(pdfBytes)) {

				PDFTextStripper stripper = new PDFTextStripper();
				return stripper.getText(document);
			}
		}
	}

	// Quick sanity test
	public static void main(String[] args) throws NlpException {
		NlpRequestModel mod = new NlpRequestModel();
		mod.getAttributes().put("Pdf_Url", "https://ontheline.trincoll.edu/images/bookdown/sample-local-pdf.pdf");

		NlpResponseModel response = new FetchTextFromPdfByURL().execute(mod);
		System.out.println(response.getAttributes().get("Text"));
	}
}
