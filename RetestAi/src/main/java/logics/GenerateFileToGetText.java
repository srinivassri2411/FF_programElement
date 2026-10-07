package logics;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GenerateFileToGetText implements Nlp {
	@InputParams({ @InputParam(name = "Captcha Image Path", type = "java.lang.String") })
	@ReturnType(name = "Extracted Python FilePath", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		String captchaImagePath = (String) programElementsInput.get("Captcha Image Path");

		String pythonFilePath = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {

			File tempFile = File.createTempFile("captcha_script_", ".py");

			tempFile.deleteOnExit(); // delete when JVM exits

			// Build Python code content

			String pythonCode = String.format("""
					import easyocr
					reader = easyocr.Reader(['en'])
					results = reader.readtext(r'%s')
					for (bbox, text, prob) in results:
					    print(text)
					""", captchaImagePath.replace("\\", "\\\\"));

			// Write Python code into file
			try (FileWriter writer = new FileWriter(tempFile)) {

				writer.write(pythonCode);

			}

			pythonFilePath = tempFile.getAbsolutePath();

			nlpResponseModel.setMessage("Python file created at: " + tempFile.getAbsolutePath());

			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {

			StringWriter sw = new StringWriter();

			e.printStackTrace(new PrintWriter(sw));

			String exceptionAsString = sw.toString();

			nlpResponseModel.setMessage("Failed to generate python file : " + exceptionAsString);

			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		nlpResponseModel.getAttributes().put("Extracted Python FilePath", pythonFilePath);
		return nlpResponseModel;

	}

}
