package Business_Logic;


import java.io.File;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class CheckFileExistence implements Nlp {
	@InputParams({ @InputParam(name = "File Prefix", type = "java.lang.String"),
@InputParam(name = "Directory to Check", type = "java.lang.String"),
			@InputParam(name = "Time Out Seconds", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePrefix = (String) programElementsInput.get("File Prefix");
		String timeOut = (String) programElementsInput.get("Time Out Seconds");
	String directoryToCheck= (String) programElementsInput.get("Directory to Check");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			// Your program element Main Logic here ...
			if (isFileDownloaded(directoryToCheck, filePrefix,
					Integer.parseInt(timeOut))) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("File has been downloaded in given duration");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("No File has been downloaded in given duration");
			}

		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed" + e);
		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public static boolean isFileDownloaded(String downloadDir, String filePrefix, int timeoutInSeconds) {

		File folder = new File(downloadDir);

		// Capture method start time
		long startTime = System.currentTimeMillis();

		long endTime = startTime + (timeoutInSeconds * 1000L);

		while (System.currentTimeMillis() < endTime) {

			File[] files = folder.listFiles();

			if (files != null) {

				for (File file : files) {

					String fileName = file.getName();

					// Ignore incomplete downloads
					boolean isTempFile = fileName.endsWith(".crdownload") || fileName.endsWith(".part");

					if (isTempFile) {
						continue;
					}

					// Check file created/modified AFTER method start
					boolean isNewFile = file.lastModified() >= startTime;

					if (!isNewFile) {
						continue;
					}

					// If prefix is empty/null -> accept any file
					boolean prefixMatches = filePrefix == null || filePrefix.trim().isEmpty()
							|| fileName.startsWith(filePrefix);

					if (prefixMatches) {
						return true;
					}
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}

		return false;
	}
}
