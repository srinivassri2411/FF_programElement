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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

//@Slf4j
public class TranscribePDFxContent implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "Total Pages in pdfx", type = "java.lang.Integer"),
			@InputParam(name = "Range(example:- all, 1, 1-5)", type = "java.lang.String") })
//			@InputParam(name = "Adobe Path", type = "java.lang.String") })
	@ReturnType(name = "TranscribePDFPath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String filePathString = (String) programElementsInput.get("File Path");
		Integer pageCount = (Integer) programElementsInput.get("Total Pages in pdfx");
		String Range = (String) programElementsInput.get("Range(example:- all, 1, 1-5)");
//		String adobePath = (String) programElementsInput.get("Adobe Path");

		String TranscribetxtPath = "";
		String TranscribePDFPath = "";

		try {

			String finaltxtPath = "C:\\CIBC\\extractedpageTextFinal.txt";
			TranscribetxtPath = "C:\\CIBC\\" + getFileNameWithoutExtension(filePathString) + "_Transcribe.txt";

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

			for (int i = 1; i <= pageCount * 110; i++) {
				pressKey(KeyEvent.VK_DOWN);
				Thread.sleep(100);
			}
			StartStopRecordering();
			Thread.sleep(5000);
			pressKey(KeyEvent.VK_ALT);
			stopRecorder();
			String adobename = Paths.get(adobePath).getFileName().toString();
			killAcrobatProcess(adobename);
			pressKey(KeyEvent.VK_ALT);

//			String extractedTxtFile = "C:\\CIBC\\extractedPDFText.txt";
			String extractedTxtFile = extractText();
			FilebeforeLineRemover(extractedTxtFile, "document");
			removeUnwantedLine(extractedTxtFile,
					"Tools  button  collapsed,no next heading,no previous heading,Loading document...,button,no next form field,no previous table,edit  read only");

//			String content = new String(Files.readAllBytes(Paths.get(extractedTxtFile)), StandardCharsets.UTF_8);
//			String modifiedContent = removeLastEditLines(content);
//			Files.write(Paths.get(extractedTxtFile), modifiedContent.getBytes(StandardCharsets.UTF_8));
//			System.out.println("Successfully modified the text file.");

			removeRepeatedLines(extractedTxtFile);

			if (Range.toLowerCase().contains("all")) {
				copyFile(extractedTxtFile, TranscribetxtPath);

				// all ,1 ,3 , 1-4
			} else {
				CapturePDFXContentInRange(extractedTxtFile, TranscribetxtPath, Range);
			}

			TranscribePDFPath = ConvertTXTToPDF(TranscribetxtPath);
			System.out.println("Successfully extracted content from pdfx " + TranscribePDFPath);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully extracted content from pdf " + TranscribePDFPath);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			TranscribePDFPath = "";
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
//			log.error(exception.toString());
			nlpResponseModel.setMessage("Failed to extract fields from file  " + exception.toString());
		}

		nlpResponseModel.getAttributes().put("TranscribePDFPath", TranscribePDFPath);
		return nlpResponseModel;
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

	public static void killAcrobatProcess(String adobename) {
		String taskKillCommand = "taskkill /im " + adobename + " /f";
		executeCommand(taskKillCommand);
	}

	public static void CapturePDFXContentInRange(String inputFilePath, String outputFilePath, String RangeofPage) {

		String pageSummary = "";

		try {
			// Read the input file
			String content = readFile(inputFilePath);

			// Split content based on "Page X of Y" format
			List<String> pages = splitContentByPage(content);

			// Extract content for the specified range
			pageSummary = extractContentForRange(pages, RangeofPage);

			// Write the extracted content to the output file
			writeFile(outputFilePath, pageSummary);

			System.out.println("Content extracted and written to file." + outputFilePath);

		} catch (Exception e) {
			System.out.println("Failed to extract and written to file.");
		}

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

	public static void combineTextFiles(List<String> sourceFiles, String destFilePath) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(destFilePath), StandardCharsets.UTF_8)) {
			for (String sourceFile : sourceFiles) {
				List<String> lines = Files.readAllLines(Paths.get(sourceFile), StandardCharsets.UTF_8);
				for (String line : lines) {
					writer.write(line);
					writer.newLine();
				}
			}
			System.out.println("Text combined and saved successfully.");
		} catch (IOException e) {
			System.err.println("Error during file processing: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void pressKey(int keyCode) throws InterruptedException {
		try {
			Robot robot = new Robot();
			robot.keyPress(keyCode);
			Thread.sleep(100);
			robot.keyRelease(keyCode);
			System.out.println("Key pressed and released successfully.");
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
			System.out.println("Two keys pressed and released successfully.");
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
			System.out.println("Three keys pressed and released successfully.");
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

	private static String removeLastEditLines(String text) {
		Pattern pattern = Pattern.compile("(\\n\\s*edit\\s*){3,}$", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(text);
		return matcher.replaceAll(""); // Replace matched pattern with an empty string
	}

	public static String ConvertTXTToPDF(String textFilePath) {

		Path textFile = Paths.get(textFilePath);
		System.out.println(textFile);
		System.out.println(textFile.getParent());
		String pdfFilePath = textFile.getParent()
				.resolve(textFile.getFileName().toString().replaceFirst("\\.txt$", ".pdf")).toString();
		System.out.println(pdfFilePath);
		try {
			File pdfFile = new File(pdfFilePath);
			if (!pdfFile.getParentFile().exists()) {
				pdfFile.getParentFile().mkdirs(); // Create directories if they do not exist
			}
			PdfWriter writer = new PdfWriter(pdfFile);
			PdfDocument pdfDocument = new PdfDocument(writer);
			Document document = new Document(pdfDocument);
			StringBuilder content = new StringBuilder();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(textFilePath), StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			document.add(new Paragraph(content.toString()));
			document.close();
			reader.close();
			System.out.println("PDF created successfully.");
			System.out.println("Saving PDF to: " + pdfFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pdfFilePath;
	}

	public static String getApplicationPath(String[] strings) {
		// List of possible executable names (correctly split by commas)
//		List<String> executableNames = new ArrayList(Arrays.asList("Acrobat.exe", "AcroRd32.exe"));
		List<String> executableNames = new ArrayList(Arrays.asList(strings));

		// List of drives to search in (C: and D:)
		List<String> drives = new ArrayList(Arrays.asList("C:\\", "D:\\"));
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

	public static void main(String[] args) throws InterruptedException, IOException, NlpException {
		TranscribePDFxContent tx = new TranscribePDFxContent();
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		programElementsInput.put("filePath", "C:\\CIBC\\pdfs\\80.pdfx.pdf");
		programElementsInput.put("pageCount", 2);
		programElementsInput.put("Range", "1");
		programElementsInput.put("adobePath", "C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe");
		tx.execute(nlpRequestModel);
	}
}