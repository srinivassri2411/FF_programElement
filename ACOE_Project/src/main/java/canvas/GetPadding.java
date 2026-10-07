package canvas;

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




public class GetPadding implements Nlp {
	@InputParams({ @InputParam(name = "Width", type = "java.lang.String"),
		@InputParam(name = "Height", type = "java.lang.String"),
		@InputParam(name = "Location", type = "java.lang.String") })
	@ReturnType(name = "padding", type = "java.lang.String")

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
		String width = (String) attributes.get("Width");
		String height = (String) attributes.get("Height");
		String location = (String) attributes.get("Location");

		// Your program element business logic goes here ...
		String padding = getPadding(width, height, location);
		nlpResponseModel.getAttributes().put("padding", padding);
		
		nlpResponseModel.setMessage("Padding for the given input: "+padding);
		nlpResponseModel.setStatus(CommonConstants.pass);
		return nlpResponseModel;
	}

	private static String getPadding(String widthStr, String heightStr, String location) {
		int width = (int) Double.parseDouble(widthStr);
		int height = (int) Double.parseDouble(heightStr);
		String padding = "";
		switch (location.toLowerCase()) {
		case "t_left":
			padding = (-width) + "," + (-height);
			break;
		case "t_center":
			padding = (0) + "," + (-height);
			break;
		case "t_right":
			padding = (width) + "," + (-height);
			break;
		case "m_left":
			padding = (-width) + "," + (0);
			break;
		case "m_center":
			padding = (0) + "," + (0);
			break;
		case "m_right":
			padding = (width) + "," + (0);
			break;
		case "b_left":
			padding = (-width) + "," + (height);
			break;
		case "b_center":
			padding = (0) + "," + (height);
			break;
		case "b_right":
			padding = (width) + "," + (height);
			break;
		}
		return padding;
	}
}