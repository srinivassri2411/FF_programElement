package Business_Logics;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class SchemaValidatorForJSON implements Nlp {
	@InputParams({ @InputParam(name = "Schema File InputStream", type = "java.io.InputStream"),
			@InputParam(name = "Json Body As String", type = "java.lang.String") })
	@ReturnType(name = "Boolean", type = "java.lang.Boolean")

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
		InputStream schemaFileInputStream = (InputStream) attributes.get("Schema File InputStream");
		String jsonBodyAsString = (String) attributes.get("Json Body As String");
		boolean flag = false;
		try {
			String schemaString = JsonPath.parse(schemaFileInputStream).jsonString();
			JSONObject rawSchema = new JSONObject(new JSONTokener(schemaString));
//			System.out.println(rawSchema);
			Schema schema = SchemaLoader.load(rawSchema);
			JSONObject json = new JSONObject(new JSONTokener(jsonBodyAsString));
//			System.out.println(json);
			schema.validate(json);
			flag = true;
			nlpResponseModel.setMessage("JSON is valid");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (ValidationException e) {
			nlpResponseModel.setMessage("JSON is invalid");
			nlpResponseModel.setStatus(CommonConstants.fail);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		nlpResponseModel.getAttributes().put("boolean", flag);
		return nlpResponseModel;
	}

	// Sample Body : {\"monthlyPayment\":5942.78,\"rate\":60.64}
	// Supported data types in Schema are object, array, string, number, integer,
	// boolean, or null
	public static void main(String[] args) throws FileNotFoundException, NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Schema File InputStream",
				new FileInputStream("C:\\Users\\User\\Desktop\\schema.json"));
		model.getAttributes().put("Json Body As String", "{\r\n" + "    \"data\": {\r\n" + "        \"id\": 2,\r\n"
				+ "        \"email\": \"janet.weaver@reqres.in\",\r\n" + "        \"first_name\": \"Janet\",\r\n"
				+ "        \"last_name\": \"Weaver\",\r\n"
				+ "        \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\r\n" + "    },\r\n"
				+ "    \"support\": {\r\n"
				+ "        \"url\": \"https://contentcaddy.io?utm_source=reqres&utm_medium=json&utm_campaign=referral\",\r\n"
				+ "        \"text\": \"Tired of writing endless social media content? Let Content Caddy generate it for you.\"\r\n"
				+ "    }\r\n" + "}");
		System.out.println(new SchemaValidatorForJSON().execute(model));
	}
}