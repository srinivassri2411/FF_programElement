
package oDex;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ChangeDataToXML implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "List of changes", type = "List") })
	@ReturnType(name = "File Path", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("File Path");
		List listofchanges = (List) programElementsInput.get("List of changes");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = replaceDataInEDI(filePath, listofchanges);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Changes are made in given");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to do changes in given");
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		nlpResponseModel.getAttributes().put("File Path", returnValue);
		return nlpResponseModel;
	}

	public static String replaceDataInEDI(String filePath, List listOfChanges) throws Exception {
		File inputFile = new File(filePath);
		String fileData = FileUtils.readFileToString(inputFile, "UTF-8");
		for (Object change : listOfChanges) {
			String[] changeArr = String.valueOf(change).split(">>");
			fileData = fileData.replaceAll(changeArr[0], changeArr[1]);
		}
		File tempFile = File.createTempFile("FF_Temp_INVOICEEDI", ".1");
		FileUtils.write(tempFile, fileData, "UTF-8");
		return tempFile.getAbsolutePath();
	}
}