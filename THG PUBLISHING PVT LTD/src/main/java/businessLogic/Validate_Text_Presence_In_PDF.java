
package businessLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class Validate_Text_Presence_In_PDF implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "Expected Text", type = "java.lang.String") })
	@ReturnType(name = "Return value", type = "java.lang.Boolean")

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
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String filePath = (String) attributes.get("File Path");
		String expText = (String) attributes.get("Expected Text");
		Map<String, Object> map = new HashMap();

		boolean boolea = false;
		try {
			PdfDocument doc = new PdfDocument();
			doc.loadFromFile(filePath);
			int count = doc.getPages().getCount();
			for (int i = 0; i < count; i++) {
				PdfPageBase page = doc.getPages().get(i);
				String b = page.extractText(false);
				String c = b.replaceAll("\u00AD", "").replaceAll("\u00A0", "").replaceAll("\\s+", "");
				boolea = c.contains(expText.replaceAll("\\s+", ""));
				System.out.println(c);
				System.out.println(boolea);
				if (boolea) {
					nlpResponseModel.setMessage("Expected text is Present within PDF ");
					nlpResponseModel.setStatus(CommonConstants.pass);
					break;
				} else if (i == count - 1) {
					nlpResponseModel.setMessage("Expected text is not Present within PDF");
					nlpResponseModel.setStatus(CommonConstants.fail);
				}
			}

		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to execute NLP " + e.getLocalizedMessage());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Return value", boolea);
		return nlpResponseModel;
	}
}