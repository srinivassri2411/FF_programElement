package pbPartner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class PDFExtract implements Nlp {

	@InputParams({ @InputParam(name = "PDFFilePath", type = "java.lang.String"),
			@InputParam(name = "SearchKey", type = "java.lang.String"),
			@InputParam(name = "TessData Zip", type = "java.lang.String") })
	@ReturnType(name = "ExtractedValue", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		// Retrieve inputs
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		if (attributes == null) {
			attributes = new HashMap<>();
			nlpRequestModel.setAttributes(attributes);
		}

		String pdfFilePath = (String) attributes.get("PDFFilePath");
		String searchKey = (String) attributes.get("SearchKey");
		String tessData = (String) attributes.get("TessData Zip");

		try {
			tessData = unzipToTempFolder(tessData);
			// Extract text from PDF
			String extractedText = extractTextFromPDF(pdfFilePath, tessData);
			String extractedValue = extractValueForKey(extractedText, searchKey);

			if (extractedValue != null) {
				nlpResponseModel.getAttributes().put("ExtractedValue", extractedValue);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Value extracted for key '" + searchKey + "': " + extractedValue);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Key '" + searchKey + "' not found in PDF.");
			}

		} catch (IOException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Error processing the PDF: " + e.getMessage());
		}

		return nlpResponseModel;
	}

	public static String unzipToTempFolder(String zipFilePath) throws IOException {
		File tempDir = Files.createTempDirectory("FF_unzippedFiles_").toFile();
		try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				File outFile = new File(tempDir, entry.getName());

				if (entry.isDirectory()) {
					outFile.mkdirs();
				} else {
					File parent = outFile.getParentFile();
					if (!parent.exists())
						parent.mkdirs();

					try (InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(outFile)) {
						in.transferTo(out);
					}
				}
			}
		}
		return tempDir.getAbsolutePath() + File.separator;
	}

	// Extract text from PDF using PDFBox
	public static String extractTextFromPDF(String pdfFilePath, String tessData) throws IOException {
		File file = new File(pdfFilePath);
		if (!file.exists()) {
			throw new IOException("PDF file not found at: " + pdfFilePath);
		}

		PDDocument document = PDDocument.load(file);
		PDFTextStripper pdfStripper = new PDFTextStripper();
		String text = pdfStripper.getText(document);
		document.close();

		// If no text is found, use OCR as fallback
		if (text.trim().isEmpty()) {
			System.out.println("No text found. Trying OCR...");
			return extractTextUsingOCR(pdfFilePath, tessData);
		}

		return text;
	}

	// Extract value based on a key using regex (supports multi-line values)
	public static String extractValueForKey(String text, String key) {
		// Remove CASE_INSENSITIVE to make it case-sensitive
		Pattern pattern = Pattern.compile(key + "\\s*[:\\-]?\\s*(.+?)(\\r?\\n|$)");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}

	// OCR extraction using Tesseract (if text is in image format)
	public static String extractTextUsingOCR(String imagePath, String tessData) {
		ITesseract tesseract = new Tesseract();
		tesseract.setDatapath(tessData); // Update path if needed
		try {
			return tesseract.doOCR(new File(imagePath));
		} catch (TesseractException e) {
			System.err.println("Error extracting text using OCR: " + e.getMessage());
			return null;
		}
	}

	// Main method for testing
	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();

		// Initialize attributes before use
		req.setAttributes(new HashMap<>());
		req.getAttributes().put("PDFFilePath", "C:\\Users\\User\\Desktop\\PBPDFS\\FG.pdf");
		req.getAttributes().put("SearchKey", "Policy Number");
		req.getAttributes().put("TessData Zip", "C:\\Users\\User\\Downloads\\eng.zip");

		NlpResponseModel response = new PDFExtract().execute(req);

		// Print response
		System.out.println(response.getMessage());
		System.out.println("Extracted Value: " + response.getAttributes().get("ExtractedValue"));
	}
}