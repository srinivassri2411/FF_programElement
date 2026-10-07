package NewPDFTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class multipleFileCopyToSingleFile implements Nlp {
	@InputParams({ @InputParam(name = "SourceFilePaths", type = "java.util.List"),
			@InputParam(name = "DestinationFilePath", type = "java.lang.String") })
	@ReturnType(name = "returnType", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		List sourceFiles = (List) programElementsInput.get("SourceFilePaths");
		String DestinationFilePath = (String) programElementsInput.get("DestinationFilePath");
		String copiedFilePath = "";

		try {
			// copiedFilePath = copyMultipleFilesToSingleFile(sourceFiles,
			// DestinationFilePath);
			copiedFilePath = combineTextFiles(sourceFiles, DestinationFilePath);
			nlpResponseModel.setMessage("Successfully copied content from the files");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception goes here ...
			nlpResponseModel.setMessage("Failed to copy the context from the files");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("returnType", copiedFilePath);
		return nlpResponseModel;
	}

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	public static String copyMultipleFilesToSingleFile(List<String> sourceFilePaths, String destinationFilePath)
			throws IOException {
		// Open the destination file for writing
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFilePath))) {
			for (String sourceFilePath : sourceFilePaths) {
				// Open each source file for reading
				try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath))) {
					String line;
					// Read each line from the source file and write it to the destination file
					while ((line = reader.readLine()) != null) {
						writer.write(line);
						writer.newLine(); // Add a new line after each line copied
					}
				} catch (IOException e) {
					System.err.println("Error reading file " + sourceFilePath + ": " + e.getMessage());
				}
			}
		}

		// Return the path of the destination file
		return destinationFilePath;
	}

	public static String combineTextFiles(List<String> sourceFiles, String destFilePath) {
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
		return destFilePath;
	}

}