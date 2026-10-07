package netCoreLogics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;




import org.springframework.stereotype.Component;

@Slf4j
@Component("LIC11870_PJT1012_PE_NLP26998f20-e778-4337-b2f2-1172397d8bc0")
public class CompareJSONonSchema implements Nlp {
	@InputParams({ @InputParam(name = "Schema Json", type = "java.lang.String"),
			@InputParam(name = "Json", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String schemajson = (String) programElementsInput.get("Schema Json");
		String json = (String) programElementsInput.get("Json");
		String result = null;
		try {
			System.out.println(json);
			try (InputStream schemaStream = new FileInputStream(schemajson)) {
				JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
				Schema schema = SchemaLoader.load(rawSchema);
				try (InputStream dataStream = new FileInputStream(json)) {
					JSONObject jsonData = new JSONObject(new JSONTokener(dataStream));
					schema.validate(jsonData);
					result = "JSON is valid against the schema";
					nlpResponseModel.setMessage(result);
					nlpResponseModel.setStatus(CommonConstants.pass);
				} catch (Exception e) {
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					log.info("JSON data is invalid: " + writer);
					result = "JSON data is invalid: " + e.getMessage();
					nlpResponseModel.setMessage(result);
					nlpResponseModel.setStatus(CommonConstants.fail);
				}
			} catch (Exception e) {
				StringWriter writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				log.info("Schema is invalid: " + writer);
				result = "Schema is invalid: " + e.getMessage();
				nlpResponseModel.setMessage(result);
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Fail to fetch" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", result);
		return nlpResponseModel;
	}

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

//	public static void main(String[] args) throws NlpException {
//		NlpRequestModel req = new NlpRequestModel();
//		req.getAttributes().put("Schema Json", "C:\\Users\\Srinivas m\\Documents\\ACOE\\NetCore\\Schema.json");
//		req.getAttributes().put("Json", "C:\\Users\\Srinivas m\\Documents\\ACOE\\NetCore\\John Personal Data.json");
//		System.out.println(new CompareJSONonSchema().execute(req));
//	}
}