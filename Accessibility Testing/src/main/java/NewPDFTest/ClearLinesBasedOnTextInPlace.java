package NewPDFTest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class ClearLinesBasedOnTextInPlace implements Nlp {
	@InputParams({ @InputParam(name = "FilePath", type = "java.lang.String"),
			@InputParam(name = "SpecificText", type = "java.lang.String") })

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

		String FilePath = (String) attributes.get("FilePath");

		String SpecificText = (String) attributes.get("SpecificText");

		try {
			List<String> lines = Files.readAllLines(Paths.get(FilePath));

			boolean shouldRemoveLines = false;
			for (int i = 0; i < Math.min(2, lines.size()); i++) {
				if (lines.get(i).contains(SpecificText)) {
					shouldRemoveLines = true;
					break;
				}
			}
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FilePath))) {
				int startLine = shouldRemoveLines ? 2 : 0;
				for (int i = startLine; i < lines.size(); i++) {
					writer.write(lines.get(i));
					writer.newLine();
				}
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Two lines are removed since found specific tecxt: ");
		} catch (IOException e) {
			e.printStackTrace();

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Specific text not found in two lines" + e);
		}

		return nlpResponseModel;
	}
}