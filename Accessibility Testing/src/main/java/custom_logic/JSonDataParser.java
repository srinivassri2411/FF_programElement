
package custom_logic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class JSonDataParser implements Nlp {

	@InputParams({
		@InputParam(name = "Json Data", type = "String"),
		@InputParam(name = "JSON Path", type = "String"),
		@InputParam(name = "Required (data or size)", type = "String")
	})
	@ReturnType(name = "Output", type = "String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String jsonData = (String) programElementsInput.get("Json Data");
		String jsonPath = (String) programElementsInput.get("JSON Path");
		String dataOrSize = (String) programElementsInput.get("Required (data or size)");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		
		try {
			if(StringUtils.startsWithAny(jsonData.trim(),"[","{")) {
			String result=(String)JsonPath.read(jsonData, jsonPath).toString();
		
			if (dataOrSize.equalsIgnoreCase("data")) {
				nlpResponseModel.getAttributes().put("Output", result);
			}else if (dataOrSize.equalsIgnoreCase("size")) {
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(result);
				if(rootNode.size()==0)result="1";
				else result= String.valueOf(rootNode.size());
				nlpResponseModel.getAttributes().put("Output", result);
			}
			}else {
				nlpResponseModel.getAttributes().put("Output", "0");
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Parsed Json Data and returned required output");
		}
		catch(Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Parse Json Data, Reason for Failure ==> \n"+sw.toString());
		}

		return nlpResponseModel;
	}

}
