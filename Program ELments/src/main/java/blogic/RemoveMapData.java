package blogic;

import java.util.LinkedHashMap;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class RemoveMapData implements Nlp {
	@SuppressWarnings("unchecked")
	@InputParams({ @InputParam(name = "map", type = "java.util.Map"),
			@InputParam(name = "Key", type = "java.lang.String") })
	@ReturnType(name = "returnData", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Map<String, String> map = (Map<String, String>) programElementsInput.get("map");
		String key = (String) programElementsInput.get("Key");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, String> returnValue = null;
		try {
			returnValue = removeMap(map, key);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Removed the " + key + " from the given map");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to remove the " + key + " from the given map +>" + e.getMessage());
		}
		nlpResponseModel.getAttributes().put("returnData", returnValue);
		return nlpResponseModel;
	}

	public Map<String, String> removeMap(Map<String, String> map, String key) {
		map.remove(key);
		return map;
	}

	public static void main(String[] args) throws NlpException {
		Map<String, String> linked = new LinkedHashMap<String, String>();
		linked.put("abc", "12412");
		linked.put("def", "12412");
		linked.put("rty", "12412");
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("map", linked);
		nlpreq.getAttributes().put("Key", "def");
		System.out.println(new RemoveMapData().execute(nlpreq));
	}
}