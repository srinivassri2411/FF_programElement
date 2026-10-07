package end2end;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

//@Slf4j
public class ExtractTabledatafromPDFx implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "Table number or range(example:- 1,(1,4),1-5)", type = "java.lang.String") })
//			@InputParam(name = "Adobe Path", type = "java.lang.String") })
	@ReturnType(name = "returnData", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String filePathString = (String) programElementsInput.get("File Path");
//		String tableStructure = (String) programElementsInput.get("tableStructure");
		String tableStructure = (String) programElementsInput
				.get("Table number or range(example:- 1,(1,4),1-5)");
//		String adobePath = (String) programElementsInput.get("Adobe Path");

		String extractedTableText = "C:\\CIBC\\extractedTableDataFormatted.txt";
		String tableContentFilePath = "C:\\CIBC\\extractedRawTableContent.txt";

		StringBuilder returnData = new StringBuilder();

		try {

			Robot robot = new Robot();
//			StartRecorder();
			String pathhh = getApplicationPath(new String[] { "nvda.exe" });
			startApplication(pathhh);
			Thread.sleep(10000);
			pressKey(KeyEvent.VK_CONTROL);
			Thread.sleep(2000);
			String adobePath = getApplicationPath(new String[] { "Acrobat.exe", "AcroRd32.exe" });
			openPDFinAdobe(adobePath, filePathString);
			Thread.sleep(10000);
			maxAdobe();
			pressKey(KeyEvent.VK_CONTROL);
			Thread.sleep(5000);
			StartStopRecordering();
			Thread.sleep(2000);
			moveMouse(600, 200);
			Thread.sleep(1000);
			MouseClickOnPointer();
			Thread.sleep(5000);
			pressKey(KeyEvent.VK_UP);
//			pressTwoKeys(KeyEvent.VK_SHIFT, KeyEvent.VK_T);

//			String list[] = tableStructure.split(",");
//			List<String> tableStructureList = new ArrayList(Arrays.asList(list));

			List<Integer> tableStructureList = generateListFromInput(tableStructure);
			System.out.println(tableStructureList);

			for (int i = 0; i < tableStructureList.size(); i++) {
				naviagateToTable1(tableStructureList.get(i));
			}

			Thread.sleep(5000);
			StartStopRecordering();
			Thread.sleep(2000);
			stopRecorder();
			String adobename = Paths.get(adobePath).getFileName().toString();
			killAcrobatProcess(adobename);
			presskeyNthTime(KeyEvent.VK_ALT, 5);

//			String extractedTxtFile = "C:\\CIBC\\extractedPDFText.txt";
			String extractedTxtFile = extractText();

			FilebeforeLineRemover(extractedTxtFile, "document");
			removeUnwantedLine(extractedTxtFile,
					"Tools  button  collapsed,no next heading,no previous heading,graphic    CIBC Logo,Loading document...,button,no next form field,no previous table,no next table");

			removeRepeatedLines(extractedTxtFile);

			String tableSummary = ExtractTableInFormat(extractedTxtFile, extractedTableText);

			CaptureTableContent(extractedTxtFile, tableContentFilePath);

			nlpResponseModel.setStatus(CommonConstants.pass);
			returnData.append(">> Extracted table raw content file path: " + tableContentFilePath + "\n   ");
			returnData.append(">> Extracted table data in format file path: " + extractedTableText + "\n   ");
			returnData.append(">> Table Summary: " + tableSummary + "\n    ");

//			System.out.println("Successfully extracted headers from PDFx " + returnData);
			nlpResponseModel.setMessage("Successfully extracted headers from PDFx " + returnData);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			returnData.setLength(0);
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
//			log.error(exception.toString());
			nlpResponseModel.setMessage("Failed to extract table data from pdfx" + exception.toString());
		}
		nlpResponseModel.getAttributes().put("returnData", returnData);
		return nlpResponseModel;
	}

	public static void removeRepeatedLines(String filePath) throws IOException {
		String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		String modifiedContent = content.replaceAll("(?m)^(\\s*\\n)+", ""); // remove extra lines
		Files.write(Paths.get(filePath), modifiedContent.getBytes(StandardCharsets.UTF_8));

		List<String> lines = new ArrayList<>();

		// Read all lines from the file
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(lines);
		List<String> line2 = new ArrayList<>();
		line2.addAll(lines);

		System.out.println(line2);

		String last = lines.get(lines.size() - 1);
		for (int i = lines.size() - 1; i >= 1; i--) {
//		String	dupString=lines.get(i);
			String org = lines.get(i - 1);
			if (!org.contains(last)) {
				break;
			} else {
				line2.remove(i);

			}
		}
		System.out.println(line2);
		// Write the modified lines back to the file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (String line : line2) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static void removeUnwantedLine(String filePath, String inputPhrases) {

//		String[] phrasesToRemove = { "Tools  button  collapsed", "no next heading", "no previous heading",
//				"graphic    CIBC Logo", "Loading document...","button    CLEAR FORM CLEAR FORM" };

		String[] phrasesToRemove = inputPhrases.split(",");
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			// Read the entire file content

			// Remove the specified phrases
			for (String phrase : phrasesToRemove) {
				content = content.replace(phrase, "");
			}
			 content = content.replaceAll("(?m)^(\\s*\\n)+", ""); // removes all extra blank lines, including those with spaces

			Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));

			System.out.println("Phrases removed and file updated successfully.");

		} catch (IOException e) {
			System.out.println("Failed to remove " + e);
		}

	}

	private static void FilebeforeLineRemover(String filePath, String specificText) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath));
			int startLine = findStartLine1(lines, specificText);
			writeRemainingLines(lines, startLine, filePath);
			System.out.println("Successfully removed line based on text");
		} catch (Exception e) {
			System.out.println("Error while removing before lines based on text");
		}

	}

	private static int findStartLine1(List<String> lines, String specificText) {
		// Check the first four lines for the specific text
		for (int i = 0; i < Math.min(4, lines.size()); i++) {
			if (lines.get(i).contains(specificText)) {
				// Return the next line index to start writing
				return i + 1;
			}
		}
		// If not found, start from the beginning
		return 0;
	}

	private static void writeRemainingLines(List<String> lines, int startLine, String filePath) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
			for (int i = startLine; i < lines.size(); i++) {
				writer.write(lines.get(i));
				writer.newLine();
			}
		} catch (Exception e) {
			System.err.println("Error writing to the file: " + e.getMessage());
		}
	}

	public static String getFileNameWithoutExtension(String filePath) {
		Path path = Paths.get(filePath);

		// Extract the file name with extension
		String fileNameWithExtension = path.getFileName().toString();

		// Find the last dot to remove the extension
		int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
		if (lastDotIndex == -1) { // No extension found
			return fileNameWithExtension; // Return the full name if no extension
		}

		// Return the substring without the extension
		return fileNameWithExtension.substring(0, lastDotIndex);
	}

	public static List<Integer> generateListFromInput(String input) {
		List<Integer> resultList = new ArrayList<>();

		// Step 1: Split the input by commas (or directly use it if there are no commas)
		String[] elements = input.split(",\\s*");

		for (String element : elements) {
			element = element.replaceAll("[a-zA-Z]", "");
			// Step 2: Check if the element contains a hyphen (indicating a range)
			if (element.contains("-")) {
				// Split by hyphen to get the start and end of the range
				String[] rangeParts = element.split("-");
				int start = Integer.parseInt(rangeParts[0].trim());
				int end = Integer.parseInt(rangeParts[1].trim());

				// Add all numbers from start to end (inclusive) to the result list
				for (int i = start; i <= end; i++) {
					resultList.add(i);
				}
			} else {
				// If it's a single number, add it directly to the list
				resultList.add(Integer.parseInt(element.trim()));
			}
		}
		return resultList;
	}

	public static void naviagateToTable(String tableStructure) throws InterruptedException {
		Thread.sleep(2000);
		String str[] = tableStructure.split("->");
		int tableCount = Integer.parseInt(str[0].replaceAll("[^0-9]+", ""));

		presskeyNthTime(KeyEvent.VK_T, tableCount);

		int rowCount = Integer.parseInt(str[1].replaceAll("[^0-9]+", ""));
		int columnCount = Integer.parseInt(str[2].replaceAll("[^0-9]+", ""));
		int cellcount = (rowCount * columnCount) + 10;

		presskeyNthTime(KeyEvent.VK_DOWN, cellcount);

		Thread.sleep(1000);

		pressTwokeyNthTime(KeyEvent.VK_SHIFT, KeyEvent.VK_T, tableCount + 2);

	}

	public static void naviagateToTable1(Integer tableCount) throws InterruptedException {
		Thread.sleep(1000);
		presskeyNthTime(KeyEvent.VK_T, tableCount);
		int cellcount = 100;
		Thread.sleep(2000);
		presskeyNthTime(KeyEvent.VK_DOWN, cellcount);
		Thread.sleep(1000);
		pressTwokeyNthTime(KeyEvent.VK_SHIFT, KeyEvent.VK_T, tableCount + 2);

	}

	public static void presskeyNthTime(int keycode, int count) throws InterruptedException {
		for (int i = 1; i <= count; i++) {
			pressKey(keycode);
		}
	}

	public static void pressTwokeyNthTime(int keycode1, int keycode2, int count) throws InterruptedException {
		for (int i = 1; i <= count; i++) {
			pressTwoKeys(keycode1, keycode2);
		}
	}

	public static void pressKey(int keyCode) throws InterruptedException {
		try {
			Robot robot = new Robot();
			robot.keyPress(keyCode);
			Thread.sleep(100);
			robot.keyRelease(keyCode);
			System.out.println("Keys pressed: " + KeyEvent.getKeyText(keyCode));
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static void pressTwoKeys(int keyCode1, int keyCode2) throws InterruptedException {
		try {
			Robot robot = new Robot();
			robot.keyPress(keyCode1);
			robot.keyPress(keyCode2);
			Thread.sleep(100);
			robot.keyRelease(keyCode2);
			robot.keyRelease(keyCode1);
			System.out
					.println("Keys pressed: " + KeyEvent.getKeyText(keyCode1) + " + " + KeyEvent.getKeyText(keyCode2));
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static void pressThreeKeys(int keyCode1, int keyCode2, int keyCode3) throws InterruptedException {
		try {
			Robot robot = new Robot();
			robot.keyPress(keyCode1);
			robot.keyPress(keyCode2);
			robot.keyPress(keyCode3);
			Thread.sleep(100);
			robot.keyRelease(keyCode3);
			robot.keyRelease(keyCode2);
			robot.keyRelease(keyCode1);
			System.out.println("Keys pressed: " + KeyEvent.getKeyText(keyCode1) + " + " + KeyEvent.getKeyText(keyCode2)
					+ " + " + KeyEvent.getKeyText(keyCode3));
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private static void StartRecorder() throws InterruptedException {
		pressThreeKeys(KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_N);
		System.out.println("Recorder started");
	}

	private static void startApplication(String appPath) {
		String command = "powershell -Command \"Start-Process -FilePath '" + appPath + "'\"";
		executeCommand(command);
	}

	private static void openPDFinAdobe(String adobePath, String filePathString) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(adobePath, filePathString);
			processBuilder.start();
			System.out.println("Adobe Acrobat has been launched. Continuing with pdf");
		} catch (Exception e) {
			System.out.println("Failed to open Adobe Acrobat, Continuing with pdf");
		}
	}

	private static void maxAdobe() throws InterruptedException {
		pressTwoKeys(KeyEvent.VK_CONTROL, KeyEvent.VK_H);
		System.out.println("Maximized Adobe");
	}

	private static void StartStopRecordering() throws InterruptedException {
		pressTwoKeys(KeyEvent.VK_ALT, KeyEvent.VK_S);
		System.out.println("int recording");
	}

	static void moveMouse(int targetX, int targetY) throws InterruptedException {
		try {
			System.setProperty("java.awt.headless", "false");
			Robot robot = new Robot();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int screenWidth = (int) screenSize.getWidth();
			int screenHeight = (int) screenSize.getHeight();

			if (targetX >= 0 && targetX <= screenWidth && targetY >= 0 && targetY <= screenHeight) {
				robot.mouseMove(targetX, targetY);
				System.out.println("Mouse moved to coordinates successfully.");
			} else {
				System.out.println("The specified coordinates are out of screen bounds.");
			}

		} catch (AWTException e) {
			System.out.println("Failed - Error creating Robot instance: " + e.getMessage());
		}
	}

	static void MouseClickOnPointer() {
		try {
			System.setProperty("java.awt.headless", "false");
			Robot robot = new Robot();
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			Thread.sleep(500);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

			System.out.println("Successfully clicked at the current mouse position.");

		} catch (Throwable e) {
			System.out.println("Failed to click on the current mouse position. " + e);
		}

	}

	static void stopRecorder() {
//		String command = "powershell.exe -ExecutionPolicy Bypass -File \"C:\\CIBC\\stopRecorder.ps1\"";
		String command = "powershell -Command \"Get-WmiObject -Query 'SELECT * FROM Win32_Process WHERE Name = \\\"nvda.exe\\\"' | ForEach-Object { $_.Terminate() }\"";

		executeCommand(command);
		System.out.println("Stoped the recorder");
	}

	public static void killAcrobatProcess(String adobename) {
		String taskKillCommand = "taskkill /im " + adobename + " /f";
		executeCommand(taskKillCommand);
	}

	/*
	 * static void executeCommandexe(String command) { try { Process process =
	 * Runtime.getRuntime().exec(command); process.waitFor();
	 * 
	 * System.out.println("Command executed successfully: " + command); } catch
	 * (IOException | InterruptedException e) { e.printStackTrace(); } }
	 */

	public static void executeCommand(String command) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
			Process process = processBuilder.start();
			process.waitFor();

			System.out.println("Command executed successfully: " + command);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
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

	static String extractText() {
		String destinationPath = "C:\\CIBC\\extractedPDFText.txt";
		try {
			String userHome = System.getProperty("user.home");
			String nvdaPath = userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator
					+ "nvda" + File.separator + "NVDARecord.txt";
			String sourcePath = nvdaPath;

			verifyFilePathAndCreate(destinationPath);
			System.out.println("Folder or file path verified and ready.");

			copyFile(sourcePath, destinationPath);
			System.out.println("File copied to: " + destinationPath);

		} catch (Exception e) {
			System.err.println("Error during file copy: " + e.getMessage());
		}
		return destinationPath;

	}

	public static void copyFile(String sourceFilePath, String destinationFilePath) throws IOException {
		File sourceFile = new File(sourceFilePath);
		File destinationFile = new File(destinationFilePath);

		// Create input and output streams
		try (FileInputStream fis = new FileInputStream(sourceFile);
				FileOutputStream fos = new FileOutputStream(destinationFile)) {

			// Buffer for data transfer
			byte[] buffer = new byte[1024];
			int length;

			// Copy file content from source to destination
			while ((length = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
			System.out.println("File copied to: " + destinationFile);
		}

		catch (Exception e) {
			System.out.println("Failed to copy to: " + destinationFile);
		}

	}

	static String GetTextFromTextFile(String txtFilePath) {
		String FilePath = txtFilePath;
		String Content = "";

		try {
			Content = Files.readString(Paths.get(FilePath), StandardCharsets.UTF_8);
//			System.out.println("Copied content from text file is : " + Content);
		}

		catch (java.nio.charset.MalformedInputException e) {
			try {
				Content = Files.readString(Paths.get(FilePath), StandardCharsets.ISO_8859_1);
//				System.out.println("Copied content from text file is : " + Content);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Not able to copy content from text file" + e);
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Not able to copy content from text file" + e);
		}
		return Content;

	}

	static String AdobeAcrobatFinder() {
		String adobeAcrobatPath = findAdobeAcrobatInstallationPath();
		if (adobeAcrobatPath != null && new File(adobeAcrobatPath).exists()) {
			System.out.println("Adobe Acrobat installation path: " + adobeAcrobatPath);
		} else {
			System.out.println("Adobe Acrobat is not installed.");
		}
		return adobeAcrobatPath;
	}

	private static String findAdobeAcrobatInstallationPath() {
		String command = "reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall /s | findstr /i \"Adobe Acrobat\"";
		StringBuilder result = new StringBuilder();

		try {
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				// Extract the installation path from the registry output
				if (line.contains("InstallLocation")) {
					// Get the path after "InstallLocation" and trim any whitespace
					String path = line.substring(line.indexOf("REG_SZ") + 7).trim();
					// Append the executable name
					return path + File.separator + "Acrobat" + File.separator + "Acrobat.exe";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null; // Return null if Adobe Acrobat is not found
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

	public static String CaptureTableContent(String inputFilePath, String outputFilePath) {

		StringBuilder tableSummary = new StringBuilder();
		try {

			String content = readFile(inputFilePath);
			// Pattern tablePattern = Pattern.compile("table(.*?)out of table",
			// Pattern.DOTALL);
			Pattern tablePattern = Pattern.compile(
					"\\btable\\b.*?(row\\s+\\d+\\s+column\\s+\\d+.*?)(?=\\bout of table\\b|$)", Pattern.DOTALL);

			Matcher tableMatcher = tablePattern.matcher(content);
			int tableCount = 1;

			while (tableMatcher.find()) {
				String tableData = tableMatcher.group(1); // Extract table data between "table" and "out of table"
				tableSummary.append(tableCount + ".\n").append("table ").append(tableData).append("\n");
				tableCount++;
			}
			writeFile(outputFilePath, tableSummary.toString());
			// System.out.println(tableSummary);
			System.out.println("Successfully captured table content");
		} catch (Exception e) {
			System.out.println("Failed to capture table content " + e);

		}
		return tableSummary.toString();
	}

	public static String ExtractTableInFormat(String inputFilePath, String outputFilePath) {
		StringBuilder tableSummary = new StringBuilder();
		try {
			String content = readFile(inputFilePath);

			// Create a pattern to match tables (from "table" to "out of table")
//			Pattern tablePattern = Pattern.compile("table(.*?)out of table", Pattern.DOTALL);
			Pattern tablePattern = Pattern.compile("(table.*?)(out of table|$)", Pattern.DOTALL);
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

//					if (!rowData.startsWith("column")) {
					if (!rowData.contains("column 1")) {
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
					// -->

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

				List<String> removeRow = new ArrayList<>();

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

					// output.append(" Row ").append(currentRowCount).append("\n");
					// Add "Column 1" for rows not starting with "column"
//					if (!rowData.startsWith("column")) {
					if (!rowData.contains("column 1")) {
						rowData = "column 1  " + rowData.trim();
					}

					if (rowData.contains("page")) {
						rowData = rowData.replaceAll("page \\d+", "").trim();
					}

					if (currentRowCount > 1) {
						for (String string : removeRow) {
							System.out.println(string);
							if (rowData.contains(string)) {
								rowData = rowData.replaceAll(string, "");
							}
						}

					}

					// Pattern to match columns within the row
					Pattern colPattern = Pattern.compile("column\\s*(\\d+)\\s*(.*?)(?=(column|row|out of table|$))",
							Pattern.DOTALL);
					Matcher colMatcher = colPattern.matcher(rowData);

					int columnCount = 1; // Start column counting from 1
					while (colMatcher.find()) {
						String columnData = colMatcher.group(2).trim().split("\n")[0];
//						if (currentRowCount > 1) {
//							if (columnData.contains("Maximum Investment in Voting Shares Property")) {
//								columnData=columnData.replaceAll("Maximum Investment in Voting Shares Property", "");
//							}
//						}

						if (currentRowCount == 1) {
							removeRow.add(columnData);
							System.out.println(columnData);
						}

						if (!columnData.isEmpty()) {
							// Output the current column data
							output.append("    Column ").append(columnCount).append(" ").append(columnData)
									.append("\n");
							columnCount++;
						} else {
							output.append("    Column ").append(columnCount).append(" ").append("NULL").append("\n");
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

			System.out.println("Table data has been successfully extracted");
		} catch (Exception e) {
			System.out.println("Failed to capture table data from file" + e);

		}

		return tableSummary.toString();
	}

	public static String getApplicationPath(String[] strings) {
		// List of possible executable names (correctly split by commas)
//		List<String> executableNames = new ArrayList(Arrays.asList("Acrobat.exe", "AcroRd32.exe"));
		List<String> executableNames = new ArrayList(Arrays.asList(strings));

		// List of drives to search in (C: and D:)
		List<String> drives = new ArrayList(Arrays.asList("C:\\", "D:\\", "E:\\", "F:\\"));
		List<String> mainDirectories = null;
		if (executableNames.contains("nvda.exe")) {
			System.out.println("yes");
			// List of common installation directories (to optimize search speed)
			mainDirectories = new ArrayList(
					Arrays.asList("C:\\Program Files (x86)\\NVDA", "C:\\Program Files (x86)", "C:\\Program Files",
							"C:\\ProgramData", "C:\\Users\\%USERNAME%\\AppData\\Local", "D:\\Program Files",
							"D:\\Program Files (x86)", "D:\\ProgramData", "D:\\Users\\%USERNAME%\\AppData\\Local"));
		} else {
			mainDirectories = new ArrayList(Arrays.asList("C:\\Program Files", "C:\\Program Files (x86)",
					"C:\\ProgramData", "C:\\Users\\%USERNAME%\\AppData\\Local", "D:\\Program Files",
					"D:\\Program Files (x86)", "D:\\ProgramData", "D:\\Users\\%USERNAME%\\AppData\\Local"));
		}

		// Searching in main directories first
		String foundPath = searchInMainDirectories(mainDirectories, executableNames);

		if (foundPath != null) {
			System.out.println("Executable found at: " + foundPath);
		} else {
			// If not found in main directories, search the drives
			foundPath = searchForExecutable(drives, executableNames);
			if (foundPath != null) {
				System.out.println("Executable found at: " + foundPath);
			} else {
				System.out.println("Executable not found.");
			}
		}
		return foundPath;
	}

	private static String searchInMainDirectories(List<String> mainDirectories, List<String> executableNames) {
		for (String directory : mainDirectories) {
			String userDirectory = directory.replace("%USERNAME%", System.getProperty("user.name"));
			File dir = new File(userDirectory);
			if (dir.exists() && dir.isDirectory()) {
				String result = searchInDirectory(dir, executableNames);
				if (result != null) {
					return result;
				}
			}
		}
		return null; // Not found in main directories
	}

	private static String searchForExecutable(List<String> drives, List<String> executableNames) {
		for (String drive : drives) {
			File rootDir = new File(drive);
			String result = searchInDirectory(rootDir, executableNames);
			if (result != null) {
				return result;
			}
		}
		return null; // Not found in any drives
	}

	private static String searchInDirectory(File directory, List<String> executableNames) {
		if (!directory.exists() || !directory.isDirectory()) {
			return null;
		}

		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					// Recursive search in subdirectories
					String result = searchInDirectory(file, executableNames);
					if (result != null) {
						return result;
					}
				} else if (file.isFile()) {
					for (String executableName : executableNames) {
						if (file.getName().equalsIgnoreCase(executableName)) {
							return file.getAbsolutePath(); // Return path if executable matches
						}
					}
				}
			}
		}
		return null; // Not found in this directory or subdirectories
	}
	public static void main(String[] args) {
		ExtractTableInFormat("C:\\CIBC\\extractedpageTextFinal.txt", "C:\\CIBC\\extractedpageTextFinal11.txt");
	}

}
