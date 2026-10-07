
package table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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




public class ExtractTableData implements Nlp {
	@InputParams({ @InputParam(name = "InputFilePath", type = "java.lang.String"),
			@InputParam(name = "extractedDataFilePath", type = "java.lang.String") })
	@ReturnType(name = "tableSummary", type = "java.lang.Object")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String inputFilePath = (String) programElementsInput.get("InputFilePath");
		String outputFilePath = (String) programElementsInput.get("extractedDataFilePath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		StringBuilder tableSummary = new StringBuilder();
		try {
			String content = readFile(inputFilePath);

			// Create a pattern to match tables (from "table" to "out of table")
			Pattern tablePattern = Pattern.compile("table(.*?)out of table", Pattern.DOTALL);
			Matcher tableMatcher = tablePattern.matcher(content);

			StringBuilder output = new StringBuilder();
			int tableCount = 1;

			// Loop through each found table
			while (tableMatcher.find()) {
				String tableData = tableMatcher.group(1); // Extract table data between "table" and "out of table"

				// Pattern to match rows
				Pattern rowPattern = Pattern.compile("row\\s*(\\d+)(.*?)(?=(row|out of table|$))", Pattern.DOTALL);
				Matcher rowMatcher = rowPattern.matcher(tableData);

				int rowCount = 0;
				int maxColumnCount = 0;

				// First pass to count rows and columns
				while (rowMatcher.find()) {
					rowCount++; // Count each row

					// Pattern to match columns within the row
					String rowData = rowMatcher.group(2).trim();

					if (!rowData.startsWith("column")) {
						rowData = "column 1  " + rowData.trim();
					}
					Pattern colPattern = Pattern.compile("column\\s*(\\d+)\\s*(.*?)(?=(column|row|out of table|$))",
							Pattern.DOTALL);
					Matcher colMatcher = colPattern.matcher(rowData);

					int columnCount = 0;
					while (colMatcher.find()) {
						columnCount++; // Count each column
					}

					// Track the maximum number of columns in any row
					if (columnCount > maxColumnCount) {
						maxColumnCount = columnCount;
					}
	//-->
//					if (rowData.contains("table")) {
//						rowCount--;
//					}
					if (rowData.contains("table")) {
						String[] words = rowData.split("\\s+"); // Split the row data into words

						for (String word : words) {
							if (word.equals("table")) {
								rowCount--;
								break; // Exit the loop after finding "table"
							}
						}
					}
				}

				// Output table summary with row and column counts
				output.append("Table ").append(tableCount).append(" with ").append(rowCount).append(" rows and ")
						.append(maxColumnCount).append(" columns\n");

				tableSummary.append("Table ").append(tableCount).append(" with ").append(rowCount).append(" rows and ")
						.append(maxColumnCount).append(" columns\n");

				// Reset the row matcher to process rows again
				rowMatcher.reset();
				int currentRowCount = 1;

				// Process each row again to extract and display the actual data
				while (rowMatcher.find()) {
					String rowData = rowMatcher.group(2).trim();
					if (rowData.isEmpty())
						continue; // Skip empty rows
//-->
//					if (rowData.contains("table")) {
//						output.append("  Row ").append(currentRowCount).append("\n");
//						currentRowCount--;
//					}
					boolean isTableStandalone = false;
					if (rowData.contains("table")) {
						String[] words = rowData.split("\\s+"); // Split the row data into words

						for (String word : words) {
							if (word.equals("table")) {
								output.append("  Row ").append(currentRowCount).append("\n");
								currentRowCount--;
								  isTableStandalone = true;
								break; // Exit the loop after finding "table"
							}
						}
						if (!isTableStandalone) {
					        output.append("  Row ").append(currentRowCount).append("\n");
					    }
					}

					else {
						output.append("  Row ").append(currentRowCount).append("\n");
					}
					
					//output.append("  Row ").append(currentRowCount).append("\n");
					// Add "Column 1" for rows not starting with "column"
					if (!rowData.startsWith("column")) {
						rowData = "column 1  " + rowData.trim();
					}
					// Pattern to match columns within the row
					Pattern colPattern = Pattern.compile("column\\s*(\\d+)\\s*(.*?)(?=(column|row|out of table|$))",
							Pattern.DOTALL);
					Matcher colMatcher = colPattern.matcher(rowData);

					int columnCount = 1; // Start column counting from 1
					while (colMatcher.find()) {
						String columnData = colMatcher.group(2).trim();
						if (!columnData.isEmpty()) {
							// Output the current column data
							output.append("    Column ").append(columnCount).append(" ").append(columnData)
									.append("\n");
							columnCount++;
						}
					}

					currentRowCount++;
				}

				output.append("\n");
				tableCount++;
			}
			tableSummary.append("total table count " + (tableCount - 1));

			// Write the extracted table data to the output file
			writeFile(outputFilePath, output.toString());

			System.out.println("Table data has been successfully extracted!");

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully captured table data from file");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to capture table data from file" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		nlpResponseModel.getAttributes().put("tableSummary", tableSummary);
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

	// Helper method to write string content into a file
	public static void writeFile(String filePath, String content) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(content);
		}
	}

}