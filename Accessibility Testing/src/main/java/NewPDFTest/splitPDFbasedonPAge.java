package NewPDFTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;


import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class splitPDFbasedonPAge implements Nlp {
	@InputParams({ @InputParam(name = "pages", type = "java.lang.String"),
			@InputParam(name = "pdfFile", type = "java.lang.String") })
	@ReturnType(name = "extractedPaths", type = "java.util.List")

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
		String pdfFile1 = (String) attributes.get("pdfFile");
		// String pages = "5,7";
		File pdfFile = new File(pdfFile1);
		List<String> extractedPaths = null;
		try {
			String outputDir = "C:\\CIBC\\Split\\";
			verifyFilePathAndCreate(outputDir);
			extractedPaths = extractAndSavePages(pdfFile, pages, outputDir);
			System.out.println(extractedPaths);
		} catch (Exception e) {
			// TODO: handle exception
		}

		// String string3 = "Return Value";
		nlpResponseModel.getAttributes().put("extractedPaths", extractedPaths);
		return nlpResponseModel;
	}

	public static List<String> extractAndSavePages(File pdfFile, String pages, String outputDir) {

		List<String> savedPaths = new ArrayList<>();
		try (PDDocument document = PDDocument.load(pdfFile)) {
			int startPage = 1;
			int endPage = 0;
			if (pages.equalsIgnoreCase("all")) {
				endPage = document.getNumberOfPages();
			} else if (pages.contains("-")) {
				// Handle the range case (e.g., "2,5")
				String[] pageRange = pages.split("-");
				startPage = Integer.parseInt(pageRange[0].trim());
				endPage = Integer.parseInt(pageRange[1].trim());
			} else {
				// Handle the single page case (e.g., "4")
				startPage = endPage = Integer.parseInt(pages.trim());
			}
			// Split the PDF document, saving each page as a separate file
			for (int i = startPage - 1; i < endPage; i++) {
				int pageNumber = i + 1;
				if (pageNumber > 0 && pageNumber <= document.getNumberOfPages()) {
					// Create a new document for the current page
					PDDocument singlePageDoc = new PDDocument();
					PDPage page = document.getPage(i);
					singlePageDoc.addPage(page);
					String outputFilePath = outputDir + "extracted-page-" + pageNumber + ".pdf";
					singlePageDoc.save(outputFilePath);
					singlePageDoc.close();
					savedPaths.add(outputFilePath);
					System.out.println("Page " + pageNumber + " extracted and saved to: " + outputFilePath);

				} else {
					System.out.println("Page " + pageNumber + " is out of bounds.");
				}

			}
			System.out.println("PDF split successfully. Files saved to: " + outputDir);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return savedPaths;
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
}
