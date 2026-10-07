
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




public class waitTime implements Nlp {
	@InputParams({ @InputParam(name = "timeInMilliseconds", type = "java.lang.Integer") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Integer timeInMilliseconds = (Integer) programElementsInput.get("timeInMilliseconds");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			Thread.sleep(timeInMilliseconds);
//	            System.out.println("Waited for " + timeInMilliseconds + " milliseconds (" + timeInMilliseconds / 1000.0 + " seconds).");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Waited for " + timeInMilliseconds + " milliseconds ("
					+ timeInMilliseconds / 1000.0 + " seconds).");

		} catch (InterruptedException e) {
			e.printStackTrace();
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to wait");
		}

		return nlpResponseModel;
	}

}
