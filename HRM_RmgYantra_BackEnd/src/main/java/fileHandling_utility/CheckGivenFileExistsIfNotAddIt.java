package fileHandling_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class CheckGivenFileExistsIfNotAddIt implements Nlp {
	@InputParams({ @InputParam(name = "File_Path", type = "java.lang.String"),
		@InputParam(name = "TestData_FileStream", type = "java.io.InputStream"),
		@InputParam(name = "isZipFile(true/false)", type = "java.lang.Boolean")})

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

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String filePath = (String) attributes.get("File_Path");
		InputStream testDataFile = (InputStream) attributes.get("TestData_FileStream");
		Boolean isZipFile = (Boolean) attributes.get("isZipFile(true/false)");

		// Your program element business logic goes here ...
		if (!checkFileExists(filePath)) {
			try {
				processFile(filePath, testDataFile, isZipFile);
				nlpResponseModel.setMessage("File is added from Test Data.");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} catch (IOException e) {
				nlpResponseModel.setMessage("Failed to add file,");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} else {
			nlpResponseModel.setMessage("File: "+filePath+" is already exists.");
			nlpResponseModel.setStatus(CommonConstants.pass);
		}
		return nlpResponseModel;
	}

	private static boolean checkFileExists(String directoryPath){
		Path path = Paths.get(directoryPath);
		// Check if the directory exists
		if (!Files.exists(path)) {
			return false;
		} else {
			return true;
		}
	}

	public static void processFile(String inputPath, InputStream inputStream, boolean isZipFile) throws IOException {
		// Define the base directory to split on
		String baseDirectory = "Fireflink_Home";

		// Split the path into parts
		Path originalPath = Paths.get(inputPath);
		String[] parts = originalPath.toString().split(Pattern.quote(File.separator));

		// Find the index of the base directory
		int baseIndex = -1;
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equals(baseDirectory)) {
				baseIndex = i;
				break;
			}
		}

		if (baseIndex == -1) {
			throw new IllegalArgumentException("Base directory 'Fireflink_Home' not found in the path.");
		}

		// Create the target directory path from parts after the base directory
		Path targetDir = originalPath.getRoot();
		for (int i = 1; i <= baseIndex; i++) {
			targetDir = targetDir.resolve(parts[i]);
		}

		for (int i = baseIndex + 1; i < parts.length - 1; i++) {
			targetDir = targetDir.resolve(parts[i]);
		}

		// Ensure the target directory exists
		Files.createDirectories(targetDir);

		// Determine file type by extension
		String fileName = originalPath.getFileName().toString();

		// Handle ZIP files by extracting them
		if (isZipFile) {
			try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
				ZipEntry entry;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					Path targetFile = targetDir.resolve(entry.getName());
					if (entry.isDirectory()) {
						Files.createDirectories(targetFile);
					} else {
						Files.createDirectories(targetFile.getParent());
						try (FileOutputStream outputStream = new FileOutputStream(targetFile.toFile())) {
							byte[] buffer = new byte[1024];
							int length;
							while ((length = zipInputStream.read(buffer)) > 0) {
								outputStream.write(buffer, 0, length);
							}
						}
					}
					zipInputStream.closeEntry();
				}
			}
		} else { // Handle normal files by copying them directly
			Path targetFilePath = targetDir.resolve(fileName);
			try (FileOutputStream fileOutputStream = new FileOutputStream(targetFilePath.toFile())) {
				byte[] buffer = new byte[1024];
				int length;
				while ((length = inputStream.read(buffer)) > 0) {
					fileOutputStream.write(buffer, 0, length);
				}
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException, NlpException {
		NlpRequestModel req = new NlpRequestModel();
		InputStream is = new FileInputStream("C:\\Users\\User\\Desktop\\AirPay.zip");
		req.getAttributes().put("File_Path", "C:\\Users\\User\\Fireflink_Home\\Channa\\myData\\pay");
		req.getAttributes().put("isZipFile(true/false)", true);
		req.getAttributes().put("TestData_FileStream", is);
		NlpResponseModel res = new CheckGivenFileExistsIfNotAddIt().execute(req);
		System.out.println("fghjk");
	}
}