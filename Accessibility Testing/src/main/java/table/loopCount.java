
package table;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class loopCount implements Nlp {
	@InputParams({ @InputParam(name = "pdfFilePath", type = "java.lang.Integer") })
	@ReturnType(name = "loop", type = "java.lang.Integer")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String pdfFilePath = (String) programElementsInput.get("pdfFilePath");
		Integer returnValue = 0;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
			int pageCount = document.getNumberOfPages();
			returnValue = getRangeValue(pageCount);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("No of pages in document is " + pageCount);
		} catch (IOException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to find pages in document");
//			e.printStackTrace();
		}

		nlpResponseModel.getAttributes().put("loop", returnValue);
		return nlpResponseModel;
	}

	public static int getRangeValue(int pageCount) {
		if (pageCount <= 10) {
			return 1;
		} else {
			return (int) Math.ceil((pageCount - 10) / 5.0) + 1;
		}
	}
}
