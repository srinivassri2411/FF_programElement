
package NewPDFTest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class FileLineRewriter implements Nlp {
	@InputParams({ @InputParam(name = "filePath", type = "java.lang.String"),
			@InputParam(name = "specificText", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("filePath");
		String specificText = (String) programElementsInput.get("specificText");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {

			List<String> lines = Files.readAllLines(Paths.get(filePath));
			int startLine = findStartLine(lines, specificText);

			if (startLine >= 0) {
				rewriteFile(filePath, lines, startLine);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully re write file based on specified text line");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Specific text not found in the file.");
			}
			// Write the lines back to the file starting from the determined start line

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			System.err.println("Error reading the file: " + e.getMessage());

		}

		return nlpResponseModel;
	}

	private static int findStartLine(List<String> lines, String searchTerm) {
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).contains(searchTerm)) {
				return i; // Return the line where the specific text is found
			}
		}
		return -1; // Return -1 if the text is not found
	}

	private static void rewriteFile(String filePath, List<String> lines, int startLine) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
			for (int i = startLine; i < lines.size(); i++) {
				writer.write(lines.get(i));
				writer.newLine();
			}
			System.out.println("File rewritten, starting from line: " + startLine);
		} catch (IOException e) {
			System.err.println("Error writing to the file: " + e.getMessage());
		}
	}

}