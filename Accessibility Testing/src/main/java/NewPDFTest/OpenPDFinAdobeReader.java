package NewPDFTest;

import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class OpenPDFinAdobeReader implements Nlp {
	@InputParams({ @InputParam(name = "pdfPath", type = "java.lang.String") })

//	@ReturnType(name = "Sum of Two numbers", type = "java.lang.Integer")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String pdfPath = (String) programElementsInput.get("pdfPath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String adobePath = "C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe";
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(adobePath, pdfPath);
			processBuilder.start();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Adobe Acrobat has been launched. Continuing with pdf");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to open Adobe Acrobat, Continuing with pdf");
		}

		// nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
		return nlpResponseModel;
	}

}
