package NewPDFTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class PDFExtractRangeOrSinglePage implements Nlp {
	@InputParams({ @InputParam(name = "pages", type = "java.lang.String"),
			@InputParam(name = "pdfFile", type = "java.lang.String") })
	@ReturnType(name = "outputFilePath", type = "java.lang.String")

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
		String pages = (String) attributes.get("pages");
		String pdf = (String) attributes.get("pdfFile");
		// String pages = "1,5";
//		File pdfFile = new File("C:\\CIBC\\6249A.pdf");
		String outputFilePath = "";
		try {
			String outputDir = "C:\\CIBC\\";
			verifyFilePathAndCreate(outputDir);
			// Call the method to extract and save the specified pages
			File pdfFile = new File(pdf);
			outputFilePath = extractAndSavePages(pdfFile, pages, outputDir);

			// Print the path of the extracted PDF file
			if (outputFilePath != null) {
				System.out.println("PDF extracted and saved to: " + outputFilePath);
				nlpResponseModel.setMessage("PDF extracted and saved to: " + outputFilePath);
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("PDF extracted and saved to: " + outputFilePath);
				nlpResponseModel.setStatus(CommonConstants.fail);
			}

		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to extract PDF " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

//		String string3 = "Return Value";
		nlpResponseModel.getAttributes().put("outputFilePath", outputFilePath);
		return nlpResponseModel;
	}

	public static String extractAndSavePages(File pdfFile, String pages, String outputDir) {
		try (PDDocument document = PDDocument.load(pdfFile)) {
			int startPage;
			int endPage;

			if (pages.contains(",")) {
				// Handle the range case (e.g., "2,5")
				String[] pageRange = pages.split(",");
				startPage = Integer.parseInt(pageRange[0].trim());
				endPage = Integer.parseInt(pageRange[1].trim());
			} else {
				// Handle the single page case (e.g., "4")
				startPage = endPage = Integer.parseInt(pages.trim());
			}

			// Validate page numbers
			if (startPage <= 0 || endPage > document.getNumberOfPages() || startPage > endPage) {
				System.out.println("Invalid page range.");
				return null;
			}

			// Create a new document for the extracted pages
			PDDocument extractedDoc = new PDDocument();

			// Loop through the specified range and add pages to the new document
			for (int i = startPage - 1; i < endPage; i++) {
				PDPage page = document.getPage(i);
				extractedDoc.addPage(page);
			}

			// Define the output file path
			String outputFilePath;
			if (startPage == endPage) {
				outputFilePath = outputDir + "extracted-page-" + startPage + ".pdf";
			} else {
				outputFilePath = outputDir + "extracted-pages-" + startPage + "-to-" + endPage + ".pdf";
			}

			// Save the extracted pages to the output file
			extractedDoc.save(outputFilePath);

			// Close the extracted document
			extractedDoc.close();

			// Return the path of the saved PDF file
			return outputFilePath;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void verifyFilePathAndCreate(String folderPath) throws IOException {
		File file = new File(folderPath);

		if (folderPath.endsWith(File.separator) || file.isDirectory()) {
			if (!file.exists()) {
				if (file.mkdirs()) {
					System.out.println("Directory created: " + file.getAbsolutePath());
				} else {
					throw new IOException("Failed to create directory: " + file.getAbsolutePath());
				}
			}
		}
		// If the path represents a file, ensure the parent directory exists
		else if (file.isFile() || !folderPath.endsWith(File.separator)) {
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				if (parentDir.mkdirs()) {
					System.out.println("Parent directories created: " + parentDir.getAbsolutePath());
				} else {
					throw new IOException("Failed to create parent directories: " + parentDir.getAbsolutePath());
				}
			}

			// Optionally create an empty file if it doesn't exist
			if (!file.exists()) {
				if (file.createNewFile()) {
					System.out.println("File created: " + file.getAbsolutePath());
				} else {
					throw new IOException("Failed to create file: " + file.getAbsolutePath());
				}
			}
		} else {
			throw new IllegalArgumentException("The specified path is not valid: " + folderPath);
		}
	}

	public static void main(String[] args) throws NlpException {

		PDFExtractRangeOrSinglePage ua = new PDFExtractRangeOrSinglePage();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("pages", "1");
		attributes.put("pdfFile", "C:\\CIBC\\6249A.pdf");

		ua.execute(nl);
	}
}
