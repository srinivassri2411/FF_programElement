package specializedAutomation2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class PullLatestRecordingFromTheMobile implements Nlp {
	@InputParams({@InputParam(name = "MobileFolderPath", type = "java.lang.String"), @InputParam(name = "SystemFolderPath", type = "java.lang.String")})
	@ReturnType(name = "SystemRecordingPath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String MobileFolderPath = (String) programElementsInput.get("MobileFolderPath");
		String SystemFolderPath = (String) programElementsInput.get("SystemFolderPath");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String systemRecordingPath = pullLatestRecording(MobileFolderPath, SystemFolderPath);
			nlpResponseModel.getAttributes().put("SystemRecordingPath", systemRecordingPath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Pulled latest recording.");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to pull the recording."+e);
		}
		return nlpResponseModel;
	}

	public static String pullLatestRecording(String mobileFolder, String systemFolder) {
		try {
			String listCommand = "adb shell ls -t \"" + mobileFolder + "\"";
			Process process = Runtime.getRuntime().exec(listCommand);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String latestFile = reader.readLine();
			
			if (latestFile == null || latestFile.isEmpty()) {
				System.out.println("No files found in the specified mobile folder.");
				return null;
			}

			// Construct full file paths
			String mobileFilePath = mobileFolder.endsWith("/") ? mobileFolder + latestFile : mobileFolder + "/" + latestFile;
			String systemFilePath = systemFolder.endsWith("/") ? systemFolder + latestFile : systemFolder + "/" + latestFile;

			// Pull the latest file from mobile to system
			String pullCommand = "adb pull \"" + mobileFilePath + "\" \"" + systemFolder + "\"";
			Process pullProcess = Runtime.getRuntime().exec(pullCommand);
			pullProcess.waitFor(); // Wait for the pull process to complete

			System.out.println("File pulled successfully: " + systemFilePath);
			return systemFilePath;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put(null, req);
		req.getAttributes().put(null, req);
	}
} 