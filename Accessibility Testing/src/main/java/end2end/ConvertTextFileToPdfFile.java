package end2end;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ConvertTextFileToPdfFile implements Nlp {

	@InputParams({ @InputParam(name = "Text File Path", type = "java.lang.String") })
	@ReturnType(name = "pdfFilePath", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String textFilePath = (String) programElementsInput.get("Text File Path");

		String pdfFilePath = "";
		try {

			pdfFilePath = ConvertTXTToPDF(textFilePath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully converted Text file to PDF " + pdfFilePath);

		} catch (Exception e) {

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to convert Text file to PDF" + e);

		}
		nlpResponseModel.getAttributes().put(pdfFilePath, pdfFilePath);

		return nlpResponseModel;
	}

	public static String ConvertTXTToPDF(String textFilePath) {

		Path textFile = Paths.get(textFilePath);
		System.out.println(textFile);
		System.out.println(textFile.getParent());
		String pdfFilePath = textFile.getParent()
				.resolve(textFile.getFileName().toString().replaceFirst("\\.txt$", ".pdf")).toString();
		System.out.println(pdfFilePath);
		try {
			File pdfFile = new File(pdfFilePath);
			if (!pdfFile.getParentFile().exists()) {
				pdfFile.getParentFile().mkdirs(); // Create directories if they do not exist
			}
			PdfWriter writer = new PdfWriter(pdfFile);
			PdfDocument pdfDocument = new PdfDocument(writer);
			Document document = new Document(pdfDocument);
			StringBuilder content = new StringBuilder();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(textFilePath), StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			document.add(new Paragraph(content.toString()));
			document.close();
			reader.close();
			System.out.println("PDF created successfully.");
			System.out.println("Saving PDF to: " + pdfFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pdfFilePath;
	}

	public static void main(String[] args) throws NlpException {

		ConvertTextFileToPdfFile ua = new ConvertTextFileToPdfFile();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("TextFilePath", "C:\\\\CIBC\\\\pdfs\\\\after.txt");
		ua.execute(nl);
	}

	public static void main1(String[] args) {
		String textFilePath = "C:\\CIBC\\pdfs\\after.txt";
		String pdfFilePath = ConvertTXTToPDF(textFilePath);
		System.out.println(pdfFilePath);
	}

}