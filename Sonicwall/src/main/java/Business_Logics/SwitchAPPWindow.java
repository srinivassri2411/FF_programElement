
package Business_Logics;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

public class SwitchAPPWindow implements Nlp {
	@InputParams({ @InputParam(name = "App", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String app = (String) programElementsInput.get("App");
		String fileURL = "https://github.com/Channu12/test/raw/refs/heads/main/acoe.exe";
		String saveDir = System.getProperty("java.io.tmpdir");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String filepath = downloadFile(fileURL, saveDir);
			System.out.println(filepath);
			executeCommand(" \"" + filepath + "\" win activate process \"" + app + "\"");
			deleteFile(filepath);
			System.out.println("Execution Completed");
			nlpResponseModel.setMessage("Switched to: " + app + " app.");
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (IOException e) {
			nlpResponseModel.setMessage("Failed to Switch.");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static String downloadFile(String fileURL, String saveDir) throws IOException {
		URL url = new URL(fileURL);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		int responseCode = httpConnection.getResponseCode();
		String fileName = "";

		// Check if response code is HTTP_OK
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String disposition = httpConnection.getHeaderField("Content-Disposition");

			// Extracts file name from header field
			if (disposition != null) {
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 10, disposition.length() - 1);
				}
			} else {
				// Extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
			}

			// log.info("Downloading: " + fileName);

			// Opens input stream from the HTTP connection
			BufferedInputStream inputStream = new BufferedInputStream(httpConnection.getInputStream());

			// Opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveDir + fileName);

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			// log.info("File downloaded to " + saveDir + fileName);
		} else {
			// log.info("No file to download. Server replied HTTP code: " + responseCode);
		}
		httpConnection.disconnect();
		return saveDir + fileName;

	}

	public static String executeCommand(String command) {
		StringBuilder output = new StringBuilder();

		try {
			// Create a process to execute the command
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
			processBuilder.redirectErrorStream(true);

			// Start the process
			Process process = processBuilder.start();

			// Read the output of the command
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append(System.lineSeparator());
				System.out.println(line);
			}

			// Wait for the process to finish and get the exit value
			int exitCode = process.waitFor();
			output.append("Exited with code: ").append(exitCode);
			// log.info(output.toString());
		} catch (IOException | InterruptedException e) {
			output.append("Exception: ").append(e.getMessage());
			// log.info(output.toString());
		}

		return output.toString();
	}

	public static boolean deleteFile(String filePath) {
		// Create a File object with the specified file path
		File file = new File(filePath);

		// Check if the file exists and is a file (not a directory)
		if (file.exists() && file.isFile()) {
			// Attempt to delete the file
			return file.delete();
		} else {
			// log.info("File not found or is not a file: " + filePath);
			return false;
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("App", "notepad.exe");
		new SwitchAPPWindow().execute(req);
	}
}