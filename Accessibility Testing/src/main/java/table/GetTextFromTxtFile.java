package table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetTextFromTxtFile implements Nlp {
	@InputParams({ @InputParam(name = "FilePath", type = "java.lang.String") })
	@ReturnType(name = "TextFromFile", type = "java.lang.String")

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

		String Content = "";

		try {
			Content = Files.readString(Paths.get(FilePath), StandardCharsets.UTF_8);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Copied content from text file is : " + Content);
		}

		catch (java.nio.charset.MalformedInputException e) {
			try {
				Content = Files.readString(Paths.get(FilePath), StandardCharsets.ISO_8859_1);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Copied content from text file is : " + Content);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Not able to copy content from text file" + e);
			}

		} catch (IOException e) {
			e.printStackTrace();

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Not able to copy content from text file" + e);
		}

		nlpResponseModel.getAttributes().put("TextFromFile", Content);
		return nlpResponseModel;
	}
}