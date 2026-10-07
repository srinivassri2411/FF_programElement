
package cmdAutomation;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RunCMDAsAdmin implements Nlp {
	@InputParams({ @InputParam(name = "CMD", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String cMD = (String) programElementsInput.get("CMD");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = executeCmd(cMD);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully executed the given cmd");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to execute the given cmd " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("result", returnValue);
		return nlpResponseModel;
	}

	private static String executeCmd(String cmd) throws IOException, InterruptedException {
		String result = null;
		String resultFile = Files.createTempFile("ResultFile", ".txt").toAbsolutePath().toString();
		System.out.println(resultFile);
		if (cmd.equalsIgnoreCase("wg")) {
			// handling bat logic
			String batFile = Files.createTempFile("WgTempFile", ".bat").toAbsolutePath().toString();
			System.out.println(batFile);
			String batContent = """
					cd "C:\\Program Files\\Banyan\\resources\\bin\\"
					wg > OutPutFile
					echo ===== End of batch ===== >> OutPutFile
					""";
			batContent = batContent.replace("OutPutFile", resultFile);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(batFile))) {
				writer.write(batContent);
				System.out.println("Batch file created successfully at: " + batFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// handling vbs file
			String vbsFile = Files.createTempFile("WgTempFile", ".vbs").toAbsolutePath().toString();
			System.out.println(vbsFile);
			String vbsContent = """
					Set UAC = CreateObject("Shell.Application")\s
					UAC.ShellExecute "cmd.exe", "/c BATPATH", "", "runas", 1
					""";
			vbsContent = vbsContent.replace("BATPATH", batFile);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(vbsFile))) {
				writer.write(vbsContent);
				System.out.println("VB file created successfully at: " + vbsFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			List<String> command = new ArrayList<>();
			command.add("cscript");
			command.add("//B"); // Runs script in batch mode (without any popup)
			command.add(vbsFile);

			// Start the process and wait for it to complete
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true); // Combine stdout and stderr

			Process process = processBuilder.start();
			int exitCode = process.waitFor(); // Wait for the process to complete
			System.out.println("Batch file execution completed with exit code: " + exitCode);
			long timeoutMillis = 30000; // 30 seconds
			long startTime = System.currentTimeMillis();
			File outputFile = new File(resultFile);

			while (true) {
				if (System.currentTimeMillis() - startTime > timeoutMillis) {
					throw new InterruptedException("Timeout reached. Breaking the loop.");
				}

				if (outputFile.exists()) {
					result = FileUtils.readFileToString(outputFile, "UTF-8");
					if (result.contains("===== End of batch =====")) {
						System.out.println("Batch finished successfully.");
						break;
					}
				}

				try {
					Thread.sleep(200); // Delay to avoid busy waiting
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // Restore interrupt status
					break;
				}
			}
		} else {
			// handling bat logic
			String batFile = Files.createTempFile("WgTempFile", ".bat").toAbsolutePath().toString();
			System.out.println(batFile);
			String batContent = """
					CMD > OutPutFile
					echo ===== End of batch ===== >> OutPutFile
					""";
			batContent = batContent.replace("OutPutFile", resultFile);
			batContent = batContent.replace("CMD", cmd);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(batFile))) {
				writer.write(batContent);
				System.out.println("Batch file created successfully at: " + batFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// handling vbs file
			String vbsFile = Files.createTempFile("WgTempFile", ".vbs").toAbsolutePath().toString();
			System.out.println(vbsFile);
			String vbsContent = """
					Set UAC = CreateObject("Shell.Application")\s
					UAC.ShellExecute "cmd.exe", "/c BATPATH", "", "runas", 1
					""";
			vbsContent = vbsContent.replace("BATPATH", batFile);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(vbsFile))) {
				writer.write(vbsContent);
				System.out.println("VB file created successfully at: " + vbsFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			List<String> command = new ArrayList<>();
			command.add("cscript");
			command.add("//B"); // Runs script in batch mode (without any popup)
			command.add(vbsFile);

			// Start the process and wait for it to complete
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true); // Combine stdout and stderr

			Process process = processBuilder.start();
			int exitCode = process.waitFor(); // Wait for the process to complete
			System.out.println("Batch file execution completed with exit code: " + exitCode);
			long timeoutMillis = 30000; // 30 seconds
			long startTime = System.currentTimeMillis();
			File outputFile = new File(resultFile);

			while (true) {
				if (System.currentTimeMillis() - startTime > timeoutMillis) {
					throw new InterruptedException("Timeout reached. Breaking the loop.");
				}

				if (outputFile.exists()) {
					result = FileUtils.readFileToString(outputFile, "UTF-8");
					if (result.contains("===== End of batch =====")) {
						System.out.println("Batch finished successfully.");
						break;
					}
				}

				try {
					Thread.sleep(200); // Delay to avoid busy waiting
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // Restore interrupt status
					break;
				}
			}
		}
		return result;
	}
}