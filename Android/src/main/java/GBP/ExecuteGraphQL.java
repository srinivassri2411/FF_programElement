
package GBP;

import java.util.HashMap;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ExecuteGraphQL implements Nlp {
	@InputParams({ @InputParam(name = "Url", type = "java.lang.String"),
			@InputParam(name = "Query", type = "java.lang.String"), @InputParam(name = "Variables", type = "Map"),
			@InputParam(name = "Headers", type = "Map") })
	@ReturnType(name = "returnData", type = "Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String url = (String) programElementsInput.get("Url");
		String query = (String) programElementsInput.get("Query");
		Map variables = (Map) programElementsInput.get("Variables");
		Map headers = (Map) programElementsInput.get("Headers");
		Map returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = executeGraphQL(url, query, variables, headers);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("GraphQL request is successfull");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform");
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("returnData", returnValue);
		return nlpResponseModel;
	}

	public static Map<String, Object> executeGraphQL(String url, String query, Map<String, Object> variables,
			Map<String, String> headers) {

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("query", query);

		if (variables != null) {
			requestBody.put("variables", variables);
		}

		Response res = RestAssured.given().headers(headers != null ? headers : new HashMap<>())
				.contentType("application/json").body(requestBody).post(url).then().extract().response();

		Map<String, Object> result = new HashMap<>();
		result.put("status", res.getStatusCode());

		Map<String, Object> responseBody = res.jsonPath().getMap("");
		result.put("response", responseBody);

		return result;
	}
}