
package table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class CaptureTableContent implements Nlp {
	@InputParams({ @InputParam(name = "InputFilePath", type = "java.lang.String"),
			@InputParam(name = "extractedDataFilePath", type = "java.lang.String") })
	@ReturnType(name = "tableContent", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String inputFilePath = (String) programElementsInput.get("InputFilePath");
		String outputFilePath = (String) programElementsInput.get("extractedDataFilePath");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		StringBuilder tableSummary = new StringBuilder();
		try {

			String content = readFile(inputFilePath);
			// Pattern tablePattern = Pattern.compile("table(.*?)out of table",
			// Pattern.DOTALL);
			Pattern tablePattern = Pattern
					.compile("\\btable\\b.*?(row\\s+\\d+\\s+column\\s+\\d+.*?)(?=\\bout of table\\b)", Pattern.DOTALL);

			Matcher tableMatcher = tablePattern.matcher(content);
			int tableCount = 1;

			while (tableMatcher.find()) {
				String tableData = tableMatcher.group(1); // Extract table data between "table" and "out of table"
				tableSummary.append(tableCount + ".\n").append("table ").append(tableData).append("\n");
				tableCount++;
			}
			writeFile(outputFilePath, tableSummary.toString());
			// System.out.println(tableSummary);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully captured table content");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to capture table content " + e);

		}

		nlpResponseModel.getAttributes().put("tableContent", tableSummary);
		return nlpResponseModel;
	}

	public static String readFile(String filePath) throws IOException {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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