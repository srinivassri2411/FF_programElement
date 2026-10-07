
package sonicWall;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

public class CreateFileFromGivenString implements Nlp {
	@InputParams({ @InputParam(name = "String Value", type = "java.lang.String"),
			@InputParam(name = "File name with Extension", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String stringValue = (String) programElementsInput.get("String Value");
		String filenamewithExtension = (String) programElementsInput.get("File name with Extension");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			FileUtils.write(new File(System.getProperty("user.home") + File.separator + filenamewithExtension),
					stringValue);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully written the file");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to write into file");
		}
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException, IOException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		nlpRequestModel.getAttributes().put("String Value",
				FileUtils.readFileToString(new File("C:\\Users\\maheshm\\enableBitLockerbat.txt")));
		nlpRequestModel.getAttributes().put("File name with Extension", "tempFolder\\enableBitlocker.bat");
		new CreateFileFromGivenString().execute(nlpRequestModel);
		NlpRequestModel nlpRequestModel1 = new NlpRequestModel();
		nlpRequestModel1.getAttributes().put("String Value",
				FileUtils.readFileToString(new File("C:\\Users\\maheshm\\enableBitLockervbs.txt")));
		nlpRequestModel1.getAttributes().put("File name with Extension", "tempFolder\\enableBitLockervbs.vbs");
		new CreateFileFromGivenString().execute(nlpRequestModel1);
	}
}