
package sonicWall;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class RemoveBanyanComplete implements Nlp {
	@InputParams(@InputParam(name = "Mac Password", type = "java.lang.String"))

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String password = (String) programElementsInput.get("Mac Password");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String output = null;
		try {

			String OsName = System.getProperty("os.name");

			if (OsName.contains("Windows")) {
				output = runForWindows("C:\\Program Files\\Banyan\\resources\\conf\\full-delete.cmd") + "";
			} else if (OsName.contains("Mac")) {
				output = runForMac("/Applications/Banyan.app/Contents/Resources/conf/full-delete.sh", password) + "";
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Os not supported");
			}

			if (output != null) {
				if (output.contains("true")) {
					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("successfully cleared banyan files");
				} else {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("Failed to clear banyan files");
				}
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to clear banyan files =>" + e);
		}

		return nlpResponseModel;
	}

	public boolean runForWindows(String cmdFilePath) throws IOException {
		try {
			// Step 1: Path to original CMD file
			Path originalScript = Paths.get(cmdFilePath);

			// Step 2: Read original CMD and remove prompts
			String cleanedScript = Files.lines(originalScript).filter(line -> !line.trim().startsWith("SET /P"))
					.filter(line -> !line.trim().startsWith("IF /I \"%AREYOUSURE%\""))
					.filter(line -> !line.trim().startsWith(":PROMPT"))
					.collect(Collectors.joining(System.lineSeparator()));

			// Step 3: Save to a temp .cmd file
			Path tempScript = Files.createTempFile("cleaned_script", ".cmd");
			Files.write(tempScript, cleanedScript.getBytes());
			System.out.println("Created cleaned script at: " + tempScript);

			// Step 4: PowerShell command to run as admin
			String command = "powershell -Command \"Start-Process cmd -ArgumentList '/c \""
					+ tempScript.toString().replace("\\", "\\\\") + "\"' -Verb RunAs\"";

			// Step 5: Run the PowerShell command
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
			builder.redirectErrorStream(true);
			Process process = builder.start();

			// Step 6: Output handling
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			}

			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);

			return exitCode == 0;

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean runForMac(String originalScriptPath, String SUDO_PASSWORD) {
		try {
			// 1. Read and filter out user prompts from original script
			Path originalScript = Paths.get(originalScriptPath);
			List<String> cleanedLines = Files.readAllLines(originalScript).stream()
					.filter(line -> !line.trim().startsWith("read -p")).filter(line -> !line.contains("case $yn in"))
					.filter(line -> !line.contains("[Yy]* ) fullDelete")).filter(line -> !line.contains("[Nn]* ) exit"))
					.filter(line -> !line.contains("* ) echo")).filter(line -> !line.contains("while true"))
					.filter(line -> !line.trim().equals("done")).filter(line -> !line.trim().equals("esac"))
					.collect(Collectors.toList());

			// 2. Append function call to actually execute uninstall
			cleanedLines.add("\nfullDelete");

			// 3. Write cleaned script to a temporary file
			Path tempScript = Files.createTempFile(Paths.get("/tmp"), "cleaned_uninstall", ".sh");
			Files.write(tempScript, cleanedLines);
			tempScript.toFile().setExecutable(true);
			System.out.println("Created cleaned script at: " + tempScript);

			// 4. Execute the cleaned script with sudo -S
			ProcessBuilder builder = new ProcessBuilder("sudo", "-S", tempScript.toAbsolutePath().toString());
			builder.redirectErrorStream(true);
			Process process = builder.start();

			// 5. Send the password to sudo
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
				writer.write(SUDO_PASSWORD + "\n");
				writer.flush();
			}

			// 6. Read and print the script's output
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			}

			// 7. Wait for the process to finish and return result
			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);
			return exitCode == 0;

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
}