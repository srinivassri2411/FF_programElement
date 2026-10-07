
package businessLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;





public class Remove_Escape_Character implements Nlp {
	@InputParams({ @InputParam(name = "String", type = "java.lang.String") })
	@ReturnType(name = "Return Value", type = "java.util.List")

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
		String str = (String) attributes.get("String");
		List<String> list = new ArrayList<String>();
		try {
			list = getList(str, ",");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Escaped characters removed successfully");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to remove Escaped characters");
		}

		nlpResponseModel.getAttributes().put("Return Value", list);
		return nlpResponseModel;
	}

	public static List<String> getList(String str, String delimiter) {
		str = StringEscapeUtils.unescapeJava(str).replace("[", "").replace("]", "").replace("\"", "").replace(" ", "");
		String[] strArr = str.split(delimiter);
		List<String> list = Arrays.asList(strArr);
		return list;
	}
}