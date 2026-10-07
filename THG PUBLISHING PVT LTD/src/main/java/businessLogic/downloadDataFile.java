
package businessLogic;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class downloadDataFile implements Nlp {
	
	@InputParams({ @InputParam(name = "Folder Name", type = "java.lang.String"),@InputParam(name = "Data FileName", type = "java.lang.String"),
		@InputParam(name = "Download DataFilePath", type = "java.lang.String"),@InputParam(name = "To Email Address", type = "java.lang.String"),
			@InputParam(name = "Email Subject", type = "java.lang.String") })
	@ReturnType(name = "String", type = "java.lang.String")

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
		String pid=null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String folderName = (String) attributes.get("Folder Name");
		String DataFileName = (String) attributes.get("Data FileName");
		String DownloadDataFilePath = (String) attributes.get("Download DataFilePath");
		String toEmail = (String) attributes.get("To Email Address");
		String subject = (String) attributes.get("Email Subject");

		// Your program element business logic goes here ...
		try {

			Utility u = new Utility();
			WebDriver driver = nlpRequestModel.getWebDriver();
//			u.execute(nlpRequestModel);
			pid=u.downloadData(folderName, DataFileName, DownloadDataFilePath, toEmail, subject, driver);
			System.out.println("pass");
			nlpResponseModel.setMessage("pass");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			System.out.println("fail" + e);
			nlpResponseModel.setMessage("failed" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

          String string3 = "Return Value";
          nlpResponseModel.getAttributes().put("String", pid);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlp = new NlpRequestModel();
		Map<String, Object> attributes = nlp.getAttributes();
		attributes.put("Folder Name", "THG_Publishing_Pvt_Ltd");
		attributes.put("Data FileName", "THG_Publishing_Pvt_Ltd_TestData_Epaper_API.xlsx");
		attributes.put("Download DataFilePath", "C:\\Users\\User\\Downloads\\");
		downloadDataFile d = new downloadDataFile();
		d.execute(nlp);

	}
}