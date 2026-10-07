
package custom_logic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class ReplaceTextBasedOnMap implements Nlp {
	
	public static void main(String[] args) {	
		 Map<String, String> mapper = new HashMap<>();
	        mapper.put("Mohan", "C I B C");
	        mapper.put("canada", "kannada");
		NlpRequestModel rq = new NlpRequestModel();
		rq.getAttributes().put("Text", "Hi This is Mohan");
		rq.getAttributes().put("Is Mapping Required(true, false)", "false");
		rq.getAttributes().put("Mapping Data(if true)", mapper);
		
		try {
			NlpResponseModel res = new ReplaceTextBasedOnMap().execute(rq);
			System.out.println(res.getAttributes().get("Output Path"));
		} catch (NlpException e) {
			e.printStackTrace();
		}

	}
	
	@InputParams({
		@InputParam(name = "Text", type = "String"),
		@InputParam(name = "Is Mapping Required(true, false)", type = "String"),
		@InputParam(name = "Mapping Data(if true)", type = "Map")
	})
	@ReturnType(name = "Output", type = "String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String text = (String) programElementsInput.get("Text");
		String isMappingReq = (String) programElementsInput.get("Is Mapping Required(true, false)");
		Map<String,String> mappingData=new LinkedHashMap<String, String>();
		if(StringUtils.equalsIgnoreCase(isMappingReq,"true")) {
		 mappingData = (Map<String,String>) programElementsInput.get("Mapping Data(if true)");
		}
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String returnValue = replaceCaseInsensitive(text, mappingData);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Replace Text Based On Map");
			nlpResponseModel.getAttributes().put("Output", returnValue);
		}
		catch(Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Replace Text Based On Map, Reason for Failure ==> \n"+sw.toString());
		}

		return nlpResponseModel;
	}


	 public static String replaceCaseInsensitive(String text, Map<String, String> mapper) {
	        String[] words = text.split(" ");
	        StringBuilder result = new StringBuilder();

	        for (String word : words) {
	            String lowerCaseWord = word.toLowerCase();
	            boolean replaced = false;

	            for (Map.Entry<String, String> entry : mapper.entrySet()) {
	                if (lowerCaseWord.equals(entry.getKey().toLowerCase())) {
	                    result.append(entry.getValue()).append(" ");
	                    replaced = true;
	                    break;
	                }
	            }

	            if (!replaced) {
	                result.append(word).append(" ");
	            }
	        }
	        return result.toString().trim();
	    }
}
