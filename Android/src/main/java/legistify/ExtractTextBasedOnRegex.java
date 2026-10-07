
package legistify;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ExtractTextBasedOnRegex implements Nlp {
	@InputParams({ @InputParam(name = "Text File Path", type = "java.lang.String"),
			@InputParam(name = "Regex Pattern", type = "java.lang.String") })
	@ReturnType(name = "returnData", type = "List")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String textFilePath = (String) programElementsInput.get("Text File Path");
		String regexPattern = (String) programElementsInput.get("Regex Pattern");
		List<String> returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getText(textFilePath, regexPattern);
			nlpResponseModel.setMessage("Successfully extracted text from given regex");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to extract " + writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("returnData", returnValue);
		return nlpResponseModel;
	}

	private List<String> getText(String filePath, String regex) throws IOException {
		String content = new String(Files.readAllBytes(Paths.get(filePath)));

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		return matches;
	}
}