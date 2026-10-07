package businessLogic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class XML_Validation implements Nlp {
	@InputParams({ @InputParam(name = "XML Response", type = "java.lang.String") })
	@ReturnType(name = "Return Value", type = "java.util.Map")
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

		String xmlString = (String) attributes.get("XML Response");

		Map<String, Object> map = null;

		try {
			xmlString = xmlString.replaceAll("\\\\n", "").replaceAll("\\\\t", "").replaceAll("\\\\", "");

			map = getResponse(xmlString);
			String format = map.get("Format").toString();
			if (format.equalsIgnoreCase("Valid")) {

				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Data is in correct XML format " + xmlString);
			} else if (format.equalsIgnoreCase("Invalid")) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Data is not in correct XML format " + xmlString);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Data is not in correct XML format " + xmlString);
			}

		} catch (Exception e) {

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Data is not in correct XML format :" + e.getLocalizedMessage());

		}
		nlpResponseModel.getAttributes().put("Return Value", map);
		return nlpResponseModel;
	}

	public static Map<String, Object> getResponse(String xmlString) throws Exception {
		// xmlString = xmlString.replaceAll("\\\\", "");
		Map<String, Object> map = new LinkedHashMap<>();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlString)));

			map.put("Format", "Valid");
			map.put("Errors", "No Errors");

		} catch (Exception e) {
			// TODO: handle exception
			map.put("Format", "Invalid");
			map.put("Errors", e.getLocalizedMessage());
		}

		return map;
	}
}
