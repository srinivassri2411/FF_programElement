
package specializedAutomation2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ConvertVoiceToText implements Nlp {
	@InputParams({ @InputParam(name = "Python_Script_Path", type = "java.lang.String"),
		@InputParam(name = "Python_Exe_Path", type = "java.lang.String"),
		@InputParam(name = "Recording_File_Path", type = "java.lang.String") })
	@ReturnType(name = "output", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String pythonScriptPath = (String) programElementsInput.get("Python_Script_Path");
		String pythonExecutable = (String) programElementsInput.get("Python_Exe_Path");
		String recordingFilePath = (String) programElementsInput.get("Recording_File_Path");
		String output = "";

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			// Read and store the original content of the Python file
			String originalContent = new String(Files.readAllBytes(Paths.get(pythonScriptPath)), StandardCharsets.UTF_8);

			// Replace File Path
			modifyFile(pythonScriptPath, "FILEPATH", recordingFilePath);

			// Build the command to execute the Python script
			String command = pythonExecutable + " " + pythonScriptPath;

			// Run the command using Runtime
			Process process = Runtime.getRuntime().exec(command);

			// Capture the output from the Python script
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// Read and print standard output
			String line;
			while ((line = inputReader.readLine()) != null) {
				output = output+line; 
			}

			// Read and print error output, if any
			while ((line = errorReader.readLine()) != null) {
				output = output+line;
			}

			// Wait for the process to finish and print the exit code
			process.waitFor();

			// Restore the original content of the Python file
			Files.write(Paths.get(pythonScriptPath), originalContent.getBytes(StandardCharsets.UTF_8));
			
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Executed python script and the output is: "+output);

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to executed python script. "+e);		
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("output", output);
		return nlpResponseModel;
	}

	public static void modifyFile(String filePath, String targetText, String replacementText) {
		try {
			// Read the file content
			Path path = Paths.get(filePath);
			String content = new String(Files.readAllBytes(path));

			// Replace the target text
			content = content.replace(targetText, replacementText);

			// Write back the modified content to the same file
			Files.write(path, content.getBytes());

			System.out.println("File updated successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}