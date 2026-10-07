package table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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




public class CapturePageContentinRange implements Nlp {
	@InputParams({ @InputParam(name = "InputFilePath", type = "java.lang.String"),
			@InputParam(name = "extractedDataFilePath", type = "java.lang.String"),
			@InputParam(name = "RangeofPage", type = "java.lang.String") })
	// @ReturnType(name = "tableContent", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		String inputFilePath = (String) programElementsInput.get("InputFilePath");
		String extractedDataFilePath = (String) programElementsInput.get("extractedDataFilePath");
		String RangeofPage = (String) programElementsInput.get("RangeofPage");

		// String returnValue = null;

		int Range[] = null;
		StringBuilder pageSummary = new StringBuilder();

		try {
			String content = readFile(inputFilePath);
		    Pattern tablePattern = Pattern.compile("(Page.*?out of region)", Pattern.DOTALL);


			Matcher tableMatcher = tablePattern.matcher(content);

			int pageCount = 1;

			// Loop through each found table
			while (tableMatcher.find()) {
				String pageData = tableMatcher.group(1); // Extract table data between "table" and "out of table"
				// tableSummary.append(tableData).append("\n");
				if (RangeofPage.contains("-")) {
					String[] rangeParts = RangeofPage.split("-");
					Range = new int[] { Integer.parseInt(rangeParts[0].trim()),
							Integer.parseInt(rangeParts[1].trim()) };

					if (pageCount >= Range[0] && pageCount <= Range[1]) {

						pageSummary.append(pageData).append("\n");
					}
				} else {
					// Handle the single page case
					int singlePage = Integer.parseInt(RangeofPage.trim());
					if (pageCount == singlePage) {
						pageSummary.append(pageData).append("\n"); // Append the data for the single page
					}
				}
				pageCount++;
			}
			System.out.println(pageSummary);
			writeFile(extractedDataFilePath, pageSummary.toString());
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully captured table content");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to capture table content " + e);
		}

//		nlpResponseModel.getAttributes().put("tableContent", pageSummary);
		return nlpResponseModel;

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

}