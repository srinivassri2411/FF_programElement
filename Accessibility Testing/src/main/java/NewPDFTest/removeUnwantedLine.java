package NewPDFTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;




public class removeUnwantedLine implements Nlp {
	@InputParams({ @InputParam(name = "pdfPath", type = "java.lang.String"),
	@InputParam(name = "inputPhrases", type = "java.lang.String"), })

//	@ReturnType(name = "Sum of Two numbers", type = "java.lang.Integer")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("pdfPath");
		String inputPhrases = (String) programElementsInput.get("inputPhrases");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

//		String[] phrasesToRemove = { "Tools  button  collapsed", "no next heading", "no previous heading",
//				"graphic    CIBC Logo", "Loading document...","button    CLEAR FORM CLEAR FORM" };
	
		String[] phrasesToRemove = inputPhrases.split(",");
		try {
			// Read the entire file content
			String content = new String(Files.readAllBytes(Paths.get(filePath)));

			// Remove the specified phrases
			for (String phrase : phrasesToRemove) {
				content = content.replace(phrase, "");
			}

			// Write the cleaned content back to the file
			Files.write(Paths.get(filePath), content.getBytes());

			System.out.println("Phrases removed and file updated successfully.");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully removed and file updated");

		} catch (IOException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to remove " + e);
		}

		// nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
		return nlpResponseModel;
	}

}
