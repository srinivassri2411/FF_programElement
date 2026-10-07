package specializedAutomation_Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTable;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class FetchSpecifiedColumnDataFromPPTFileTable implements Nlp {
	@InputParams({@InputParam(name = "InputStream of PPT", type = "java.io.InputStream")})
	@ReturnType(name = "string", type = "java.lang.String")

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
		InputStream inputStream = (InputStream) attributes.get("InputStream of PPT");

		// Your program element business logic goes here ...
		String data = "";
		try {
			data = readPPTFile(inputStream);
			nlpResponseModel.setMessage("Fetching data from PPT file is Successful...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (IOException e) {
			nlpResponseModel.setMessage("Failed to Fetch data from PPT file..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("string", data);
		return nlpResponseModel;
	}

	private static String readPPTFile(InputStream inputstream) throws IOException {

		StringBuilder content = new StringBuilder();
		XMLSlideShow ppt = new XMLSlideShow(inputstream);

		for (XSLFShape shape : ppt.getSlides().get(0).getShapes()) {
			if (shape instanceof XSLFTable) {
				XSLFTable table = (XSLFTable) shape;
				for (int row = 0; row < table.getNumberOfRows(); row++) {
					for (int col = 0; col < table.getNumberOfColumns(); col++) {
						content.append(table.getCell(row, col).getText()+", ");
					}
					content.append("\n");
				}
			}
		}
		ppt.close();
		inputstream.close();
		return content.toString();
	}
} 