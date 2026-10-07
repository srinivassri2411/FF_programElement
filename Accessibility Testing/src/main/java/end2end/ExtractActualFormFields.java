package end2end;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

//@Slf4j
public class ExtractActualFormFields implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "Form Fields Count", type = "java.lang.Integer") })
//			@InputParam(name = "Adobe Path", type = "java.lang.String") })
	@ReturnType(name = "ActualFields", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePathString = (String) programElementsInput.get("File Path");
		Integer headerCount = (Integer) programElementsInput.get("Form Fields Count");
//		String adobePath = (String) programElementsInput.get("Adobe Path");

//		String ActualFields = "C:\\CIBC\\ActualFields.txt";
		String ActualFields = "";
		try {
			ActualFields = "C:\\CIBC\\" + getFileNameWithoutExtension(filePathString) + "_ActualFields.txt";
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
//			pressTwoKeys(KeyEvent.VK_SHIFT, KeyEvent.VK_H);

			for (int i = 1; i <= headerCount + 5; i++) {
				pressKey(KeyEvent.VK_F);
				Thread.sleep(100);
			}

			StartStopRecordering();
			Thread.sleep(5000);
			stopRecorder();

			String adobename = Paths.get(adobePath).getFileName().toString();
			killAcrobatProcess(adobename);

			presskeyNthTime(KeyEvent.VK_ALT, 5);

//			String extractedTxtFile = "C:\\CIBC\\extractedPDFText.txt";
			String extractedTxtFile = extractText();
			FilebeforeLineRemover(extractedTxtFile, "document");
			removeUnwantedLine(extractedTxtFile,
					"Tools  button  collapsed,graphic    page 1  CIBC logo,no next heading,no previous heading,graphic    CIBC Logo,Loading document...,button    CLEAR FORM CLEAR FORM,graphic    CIBC logo,no next form field,no previous form field");

			RemoveSequentialDuplicates(extractedTxtFile);
			verifyFilePathAndCreate(ActualFields);
			copyFile(extractedTxtFile, ActualFields);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully extracted fields from file " + ActualFields);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			ActualFields = "";
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
//			log.error(exception.toString());
			nlpResponseModel.setMessage("Failed to extract fields from file  " + exception.toString());
		}

		nlpResponseModel.getAttributes().put("ActualFields", ActualFields);
		return nlpResponseModel;
	}

	public static String getFileNameWithoutExtension(String filePath) {
		Path path = Paths.get(filePath);
		String fileNameWithExtension = path.getFileName().toString();
		int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
		if (lastDotIndex == -1) {
			return fileNameWithExtension;
		}
		return fileNameWithExtension.substring(0, lastDotIndex);
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

	private static void StartRecorder() throws InterruptedException {
		pressThreeKeys(KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_N);
		System.out.println("Recorder started");
	}

	private static void startApplication(String appPath) {
		String command = "powershell -Command \"Start-Process -FilePath '" + appPath + "'\"";
		executeCommand(command);
	}

	static void stopRecorder() {
//		String command = "powershell.exe -ExecutionPolicy Bypass -File \"C:\\CIBC\\stopRecorder.ps1\"";
		String command = "powershell -Command \"Get-WmiObject -Query 'SELECT * FROM Win32_Process WHERE Name = \\\"nvda.exe\\\"' | ForEach-Object { $_.Terminate() }\"";
		executeCommand(command);
		System.out.println("Stoped the recorder");
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

	private static void StartStopRecordering() throws InterruptedException {
		pressTwoKeys(KeyEvent.VK_ALT, KeyEvent.VK_S);
		System.out.println("Recording");
	}

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

	static void FileLineRewriter(String filePath, String specificText) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath));
			int startLine = findStartLine(lines, specificText);

			if (startLine >= 0) {
				rewriteFile(filePath, lines, startLine);
				System.out.println("Successfully re write file based on specified text line");
			} else {
				System.out.println("Specific text not found in the file.");
			}
			// Write the lines back to the file starting from the determined start line

		} catch (Exception e) {
			System.err.println("Error reading the file: " + e.getMessage());

		}
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

	static void removeUnwantedLine(String filePath, String inputPhrases) {

//		String[] phrasesToRemove = { "Tools  button  collapsed", "no next heading", "no previous heading",
//				"graphic    CIBC Logo", "Loading document...","button    CLEAR FORM CLEAR FORM" };

		String[] phrasesToRemove = inputPhrases.split(",");
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			// Read the entire file content
			content = content.replaceAll("(?i)^page\\s+.*[\\r\\n]", "");
			content = content.replaceAll("(?m)^\\d+/\\d+$", "");
			content = content.replaceAll("(?m)^f\\s*$", "");
			// Remove the specified phrases
			for (String phrase : phrasesToRemove) {
				content = content.replace(phrase, "");
			}
			content = content.replaceAll("(?m)^(\\s*\\n)+", ""); // removes all extra blank lines, including those with
																	// spaces

			Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));

			System.out.println("Phrases removed and file updated successfully.");

		} catch (IOException e) {
			System.out.println("Failed to remove " + e);
		}

	}

	private static void RemoveSequentialDuplicates(String filePath) throws IOException {
		// Read all lines from the file
//		List<String> lines = Files.readAllLines(Paths.get(filePath));
		List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

		// StringBuilder to hold the final output
		StringBuilder outputLines = new StringBuilder();

		// Set to track unique lines for the first four lines
		Set<String> firstFourUniqueLines = new HashSet<>();

		// Variable to keep track of the last added line for subsequent lines
		String lastAddedLine = null;

		// Iterate through each line
		for (int i = 0; i < lines.size(); i++) {
			String currentLine = lines.get(i);

			// For the first four lines, disallow duplicates
			if (i < 4) {
				// Check if the current line is already added
				if (firstFourUniqueLines.add(currentLine)) {
					outputLines.append(currentLine).append(System.lineSeparator());
				}
			} else {
				// For lines after the first four, allow all lines including duplicates
				outputLines.append(currentLine).append(System.lineSeparator());
			}
		}

		// Write the merged output back to the same file
//		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
//			writer.write(outputLines.toString());
//		}
		Files.write(Paths.get(filePath), outputLines.toString().getBytes(StandardCharsets.UTF_8));

	}

	private static void FilebeforeLineRemover(String filePath, String specificText) throws Exception {
		try {
			List<String> lines = Files.readAllLines(Paths.get(filePath));
			int startLine = findStartLine1(lines, specificText);
			writeRemainingLines(lines, startLine, filePath);
			System.out.println("Successfully removed line based on text");
		} catch (Exception e) {
			System.out.println("Error while removing before lines based on text");
			throw e;
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

	public static void main1(String[] args) throws NlpException {
		ExtractActualFormFields eah = new ExtractActualFormFields();
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		programElementsInput.put("filePath", "C:\\CIBC\\pdfs\\80.pdfx.pdf");
		programElementsInput.put("headerCount", 32);
		programElementsInput.put("adobePath", "C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe");
		eah.execute(nlpRequestModel);
	}

	public static void main(String[] args) {
		removeUnwantedLine("C:\\CIBC\\rrif-iiroc-beneficiary-form-en_ActualFields.txt",
				"Tools  button  collapsed,graphic    page 1  CIBC logo,no next heading,no previous heading,graphic    CIBC Logo,Loading document...,button    CLEAR FORM CLEAR FORM,graphic    CIBC logo,no next form field,no previous form field");

	}
}
