package unilog;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("LIC11870_PJT1012_PE_NLP61648eb8-3c88-419c-ac00-aded967e0aa4")
public class DefaultEmulatorDirectory implements Nlp {
	@ReturnType(name = "output", type = "java.lang.String")

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
//		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		String directoryPath = "";
		try {
			directoryPath = DefaultEmulatorDirectory.getDefaultEmulatorDirectory() + File.separator + "resources"
					+ File.separator + "custom.png";

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("The QR code image path is " + directoryPath);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed  " + exceptionAsString);

		}
		nlpResponseModel.getAttributes().put("output", directoryPath);

		return nlpResponseModel;
	}

	private static String getDefaultEmulatorDirectory() {
		String sdkPath = System.getenv("ANDROID_HOME");
		if (sdkPath == null || sdkPath.isEmpty()) {
			sdkPath = System.getenv("ANDROID_SDK_ROOT");
		}

		if (sdkPath == null || sdkPath.isEmpty()) {
			String os = System.getProperty("os.name").toLowerCase();
			String userHome = System.getProperty("user.home");

			if (os.contains("win")) {
				sdkPath = userHome + "\\AppData\\Local\\Android\\Sdk";
			} else if (os.contains("mac")) {
				sdkPath = userHome + "/Library/Android/sdk";
			} else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
				sdkPath = userHome + "/Android/Sdk";
			}
		}

		File emulatorDir = new File(sdkPath, "emulator");
		return emulatorDir.getAbsolutePath();
	}
}
