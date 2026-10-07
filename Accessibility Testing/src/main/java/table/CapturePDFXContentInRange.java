package table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class CapturePDFXContentInRange implements Nlp {
	@InputParams({ @InputParam(name = "InputFilePath", type = "java.lang.String"),
			@InputParam(name = "extractedDataFilePath", type = "java.lang.String"),
			@InputParam(name = "RangeofPage", type = "java.lang.String") })
	// @ReturnType(name = "tableContent", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		String inputFilePath = (String) programElementsInput.get("InputFilePath");
		String outputFilePath = (String) programElementsInput.get("extractedDataFilePath");
		String RangeofPage = (String) programElementsInput.get("RangeofPage");

		String pageSummary ="";

		try {
			// Read the input file
			String content = readFile(inputFilePath);

			// Split content based on "Page X of Y" format
			List<String> pages = splitContentByPage(content);

			// Extract content for the specified range
			 pageSummary = extractContentForRange(pages, RangeofPage);

			// Write the extracted content to the output file
			writeFile(outputFilePath, pageSummary);

			System.out.println("Content extracted and written to file.");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully captured table content");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to capture table content " + e);
		}

//		nlpResponseModel.getAttributes().put("tableContent", pageSummary);
		return nlpResponseModel;

	}

	public static List<String> splitContentByPage(String content) {
		List<String> pages = new ArrayList<>();
		Pattern pagePattern = Pattern.compile("Page \\d+ of \\d+", Pattern.DOTALL);
		Matcher matcher = pagePattern.matcher(content);

		int lastIndex = 0;
		while (matcher.find()) {
			if (lastIndex != 0) {
				pages.add(content.substring(lastIndex, matcher.start()).trim());
			}
			lastIndex = matcher.start();
		}
		if (lastIndex != 0) {
			pages.add(content.substring(lastIndex).trim());
		}

		return pages;
	}

	// Method to extract content based on a page range (e.g., "1-2")
	public static String extractContentForRange(List<String> pages, String pageRange) {
		StringBuilder extractedContent = new StringBuilder();

		int[] range = parsePageRange(pageRange);
		int startPage = range[0];
		int endPage = range[1];

		for (int i = startPage - 1; i < endPage && i < pages.size(); i++) {
			extractedContent.append(pages.get(i)).append("\n");
		}

		return extractedContent.toString();
	}

	// Helper method to parse the page range (e.g., "1-2")
	public static int[] parsePageRange(String pageRange) {
		String[] rangeParts = pageRange.split("-");
		int startPage = Integer.parseInt(rangeParts[0].trim());
		int endPage = rangeParts.length > 1 ? Integer.parseInt(rangeParts[1].trim()) : startPage;

		return new int[] { startPage, endPage };
	}

	public static String readFile(String filePath) throws IOException {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				contentBuilder.append(currentLine).append("\n");
			}
		}
		return contentBuilder.toString();
	}

	public static void writeFile(String filePath, String content) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
			writer.write(content);
		}
	}

	public static void main(String[] args) throws NlpException {

		CapturePDFXContentInRange ua = new CapturePDFXContentInRange();
		NlpRequestModel nl = new NlpRequestModel();
		Map<String, Object> attributes = nl.getAttributes();
		attributes.put("InputFilePath", "C:\\CIBC\\extractedPDFText.txt");
		attributes.put("extractedDataFilePath", "C:\\CIBC\\TranscribePDF.txt");
		attributes.put("RangeofPage", "1");
		ua.execute(nl);
	}

}