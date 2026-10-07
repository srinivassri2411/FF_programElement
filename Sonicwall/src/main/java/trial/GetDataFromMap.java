package trial;

import java.io.PrintWriter;
import java.io.StringWriter;
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

public class GetDataFromMap implements Nlp {

	@InputParams({ @InputParam(name = "Map", type = "java.lang.Object"),
			@InputParam(name = "Map key", type = "java.lang.String") })
	@ReturnType(name = "Sum of Two numbers", type = "java.lang.Object")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Object Mapdata = (Object) programElementsInput.get("Map");
		String key = (String) programElementsInput.get("Map key");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Object decide = null;

		try {
			if (Mapdata instanceof Map<?, ?>) {
				key = (String) ((Map) Mapdata).get(key);
				if ( key == null) {
					decide = false;
					nlpResponseModel.setMessage("The Map Provided the Key Is Not Present");
					nlpResponseModel.setStatus(CommonConstants.pass);
				} else {
					
					
					if (key.contains("::")) {
						String data[] = key.split("::");
						List dataa=new ArrayList<String>();
                         dataa.add(data[0]);
                         dataa.add(data[1]);
 						decide=dataa;

					
					} else {
						decide=true;
						decide=Boolean.parseBoolean(key);
					}
					nlpResponseModel.setMessage("Successfully fetched the Value from Given Map");
					nlpResponseModel.setStatus(CommonConstants.pass);

				}

			} else {
				decide=false;
				nlpResponseModel.setMessage("The Map Provided Is Empty");
				nlpResponseModel.setStatus(CommonConstants.pass);
			}

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Fetch the Value from Map");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			throw new NlpException(exceptionAsString);
		}
		nlpResponseModel.getAttributes().put("Sum of Two numbers", decide);

		return nlpResponseModel;
	}

}