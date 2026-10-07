
package Windows;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.commons.io.FileUtils;

public class GetStringFromLocalFilePath implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String") })
	@ReturnType(name = "returnString", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("File Path");
		String returnValue = "";

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String osName = System.getProperty("os.name");
			if (osName.toLowerCase().contains("windows")) {
				returnValue = FileUtils.readFileToString(new File(filePath), "UTF-8");
			} else {
				ProcessBuilder pb = new ProcessBuilder("cat", "/private/etc/sonicwallcse/csewgs/bwgsconfig");

				Process process = pb.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//				String string ="";

				String line;
				while ((line = reader.readLine()) != null) {
					// System.out.println(line);
					returnValue = returnValue + "" + line;
				}
//				System.out.println(string);
				int exitCode = process.waitFor();
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched the string from local file");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch the string from local file " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		nlpResponseModel.getAttributes().put("returnString", returnValue);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("File Path", "C:\\Users\\User\\AppData\\Roaming\\sonicwallcse\\logs\\main.log");
		System.out.println(new GetStringFromLocalFilePath().execute(model));
	}
}