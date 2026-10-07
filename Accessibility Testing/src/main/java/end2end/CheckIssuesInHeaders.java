package end2end;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class CheckIssuesInHeaders implements Nlp {
	@InputParams({ @InputParam(name = "Extracted Headers File Path", type = "java.lang.String") })
	// @ReturnType(name = "Sum of Two numbers", type = "java.lang.Integer")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		String FilePath = (String) programElementsInput.get("Extracted Headers File Path");
		String inputFilePath = FilePath;
		String outputFilePath = FilePath;

		List<String> processedLines = new ArrayList<>();
		Stack<Integer> levelStack = new Stack<>();
		boolean firstHeading = true; // Flag to h
		try {
			removeExtraline(FilePath);
			try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
				String line;

				// Read all lines from the input file
				while ((line = reader.readLine()) != null) {
					int currentLevel = extractHeadingLevel(line);
					// System.out.println(line);

					if (currentLevel > 0) {
						if (firstHeading) {
							processedLines.add(line);
							levelStack.push(currentLevel);
							firstHeading = false;
						} else {
							String errorMessage = validateHeadingOrder(levelStack, currentLevel);
							if (!errorMessage.isEmpty()) {
								processedLines.add(line + " " + errorMessage);
							} else {
								processedLines.add(line);
							}
							levelStack.push(currentLevel);
						}
					} else {
						// If not a heading line, keep it unchanged
						processedLines.add(line);
					}
				}

			}

			// Write back the processed lines to the output file
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
				for (String line : processedLines) {
					writer.write(line);
					writer.newLine(); // Add a new line after each line
				}
			}

			// System.out.println("File processed successfully. Check the output file for
			// results.");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("File processed successfully. Check the output file " + FilePath);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed verify header in file");
		}

//		nlpResponseModel.getAttributes().put("", );
		return nlpResponseModel;
	}

	public static void removeExtraline(String filePath) {
		// Define the phrases to be removed
		String[] phrasesToRemove = { "Tools  button  collapsed", "no next heading", "no previous heading" };

		try {
			// Read the entire file content
			String content = new String(Files.readAllBytes(Paths.get(filePath)));

			// Remove the specified phrases
			for (String phrase : phrasesToRemove) {
				content = content.replace(phrase, "");
			}

			// Write the cleaned content back to the file
			Files.write(Paths.get(filePath), content.getBytes());

			System.out.println("Phrases removed and file updated successfully.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int extractHeadingLevel(String line) {
		if (line.contains("heading") && line.contains("level")) {
			try {
				// Extract the number after "heading level" (e.g., "heading level 2" -> 2)
				// String levelString = line.substring(line.indexOf("heading level") + 14,
				// line.indexOf("heading level") + 15);
				Pattern pattern = Pattern.compile("heading\\s+level\\s+(\\d+)");
				Matcher matcher = pattern.matcher(line);
				String levelString = "";
				if (matcher.find()) {
					// Extract the level number
					levelString = matcher.group(1);
					// System.out.println(levelString);
				} else {
					System.out.println("No heading level found in the line.");
				}
				return Integer.parseInt(levelString);
			} catch (Exception e) {
				// Handle any parsing errors
				return 0;
			}
		}
		return 0; // Return 0 if it's not a heading line
	}

	public static String validateHeadingOrder(Stack<Integer> levelStack, int currentLevel) {
		StringBuilder errors = new StringBuilder();

		if (levelStack.isEmpty()) {
			if (currentLevel != 1) {
				errors.append("[ERROR: Heading order error - No previous heading]");
			}
			return errors.toString();
		}

		if (currentLevel == 1) {
			if (!levelStack.isEmpty()) {
				errors.append("[ERROR: Level 1 heading should be the first heading; No previous headings expected]");
			}
		} else {
			while (!levelStack.isEmpty() && levelStack.peek() >= currentLevel) {
				levelStack.pop();
			}

			if (levelStack.isEmpty() && currentLevel != 1) {
				errors.append("[ERROR: Heading order error - Missing Level 1 heading]");
			} else if (!levelStack.isEmpty() && levelStack.peek() != currentLevel - 1) {
				if (currentLevel == 2 && levelStack.peek() != 1) {
					errors.append("[ERROR: Level 2 heading should follow a Level 1 heading; Level 1 is missing]");
				} else if (currentLevel == 3 && levelStack.peek() != 2) {
					errors.append("[ERROR: Level 3 heading should follow a Level 2 heading; Level 2 is missing]");
				} else if (currentLevel == 4 && levelStack.peek() != 3) {
					errors.append("[ERROR: Level 4 heading should follow a Level 3 heading; Level 3 is missing]");
				} else {
					errors.append("[ERROR: Invalid heading order]");
				}
			}
		}

		return errors.toString();
	}

}