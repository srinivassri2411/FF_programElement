package specializedAutomation_Utility;

import java.io.File;
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




public class WaitAndVerifyFileIsDownloaded implements Nlp {
	@InputParams({@InputParam(name = "Downlaod Directory", type = "java.lang.String"), @InputParam(name = "Download File Name", type = "java.lang.String"), @InputParam(name = "Max Wait Time (in seconds)", type = "java.lang.Integer")})
	@ReturnType(name = "boolean", type = "java.lang.Boolean")
	
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

		String downlaodDirectory = (String) attributes.get("Downlaod Directory");
		String downloadFileName = (String) attributes.get("Download File Name");
		Integer maxWaitTime = (Integer) attributes.get("Max Wait Time (in seconds)");

		// Your program element business logic goes here ...
		boolean flag = false;
		int count = 0;
		while (true) {
			File latestFile = getLatestFile(downlaodDirectory);
			if (latestFile != null) {
				String oldFileName = latestFile.getName();
				if (!oldFileName.endsWith(".crdownload") && !oldFileName.endsWith(".tmp")) {
					String newName = renameFile(downlaodDirectory, oldFileName, downloadFileName);
					nlpResponseModel.setMessage("File " + newName + " downloaded successfully.");
					nlpResponseModel.setStatus(CommonConstants.pass);
					flag = true;
					break;
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count++;
				if (count >= maxWaitTime) {
					nlpResponseModel.setMessage("No files donloaded within the last " + maxWaitTime + " seconds.");
					nlpResponseModel.setStatus(CommonConstants.fail);
					break;
				}
			}
		}

		nlpResponseModel.getAttributes().put("boolean", flag);
		return nlpResponseModel;
	}

	public static File getLatestFile(String directoryPath) {
		File directory = new File(directoryPath);
		File[] files = directory.listFiles();

		if (files == null || files.length == 0) {
			return null; // No files in the directory
		}

		// Find the latest file updated within the last 1 second
		File latestFile = null;
		long currentTime = System.currentTimeMillis();
		long lastModifiedThreshold = currentTime - 1000; // 1 second ago
		for (File file : files) {
			if (file.isFile() && file.lastModified() > lastModifiedThreshold) {
				latestFile = file;
				break; // Found the latest file updated within the last 1 second
			}
		}

		return latestFile;
	}

	public static String renameFile(String directoryPath, String oldFileName, String newFileName) {
		File directory = new File(directoryPath);
		File oldFile = new File(directory, oldFileName);

		// Extract the extension from the old file name
		int lastDotIndex = oldFileName.lastIndexOf('.');
		String extension = "";
		if (lastDotIndex != -1) {
			extension = oldFileName.substring(lastDotIndex);
		}

		// Append the extension to the new file name
		String newFileNameWithExtension = newFileName + extension;
		File newFile = new File(directory, newFileNameWithExtension);

		// Attempt to rename the file
		if (oldFile.renameTo(newFile)) {
			return newFile.getName();
		} else {
			System.err.println("Failed to rename file: " + oldFile.getAbsolutePath());
			return "";
		}
	}
} 