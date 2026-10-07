package spensersGift;

import static io.restassured.RestAssured.given;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class GetDataFromMultiRowData implements Nlp {
	@InputParams({ @InputParam(name = "Sheet Url", type = "java.lang.String"),
			@InputParam(name = "Sheet Name", type = "java.lang.String"),
			@InputParam(name = "unique Data", type = "java.lang.String"),
			@InputParam(name = "unique Data Index", type = "java.lang.String"),
			@InputParam(name = "isTableVertical", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.util.Map")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String sheetUrl = (String) programElementsInput.get("Sheet Url");
		String sheetName = (String) programElementsInput.get("Sheet Name");
		String uniqueData = (String) programElementsInput.get("unique Data");
		String uniqueDataIndex = (String) programElementsInput.get("unique Data Index");
		String isTableVertical = (String) programElementsInput.get("isTableVertical");
		Map<String, String> resultData = new LinkedHashMap<>();
		try {
			Map<String, String> params = new LinkedHashMap<>();
			params.put("sheetUrl", sheetUrl);
			params.put("sheetName", sheetName);
			params.put("isTableVertical", isTableVertical);
			params.put("uniqueData", uniqueData);
			params.put("uniqueDataIndex", uniqueDataIndex);
			ExtractableResponse<Response> response = given().contentType(ContentType.ANY).queryParams(params).when()
					.get("https://script.google.com/macros/s/AKfycbwlf9cY9SNjyE3I_Vg-5MQjYtbhGQB0kqSQ-RS76D1r4GjNNeYY9fAuk3ArdBOVT73p/exec")
					.then().extract();
			String responseBody = response.response().body().asPrettyString();
			System.out.println(responseBody);
			if (responseBody.contains("You do not have permission to access the requested document")) {
				nlpResponseModel
						.setMessage("Failed to fetch You do not have permission to access the requested document");
				nlpResponseModel.setStatus(CommonConstants.fail);
			} else {
				JsonPath jsonObject = JsonPath.from(responseBody);
				long status = jsonObject.getLong("status");
				String message = jsonObject.getString("message");
				resultData = jsonObject.getMap("data");
				System.out.println(status);
				System.out.println(message);
				System.out.println(resultData);
				if (status > 200) {
					nlpResponseModel.setMessage("Failed to fetch " + message);
					nlpResponseModel.setStatus(CommonConstants.fail);
				} else if (status == 200) {
					nlpResponseModel.setMessage("Successfully fetched the data into map");
					nlpResponseModel.setStatus(CommonConstants.pass);
				} else {
					nlpResponseModel.setMessage("Failed to fetch " + message);
					nlpResponseModel.setStatus(CommonConstants.fail);
				}
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Fail to fetch" + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", resultData);
		return nlpResponseModel;
	}

	public static int getRandomInteger(int min, int max) {
		Random random = new Random();
		return random.nextInt((max - min) + 1) + min;
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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("Sheet Url",
				"https://docs.google.com/spreadsheets/d/1wCPp0FzjKO5eYCvPVw3-PBg72QI153rs0hX8bDU81LU/edit");
		req.getAttributes().put("Sheet Name", "MR_Horizontal");
		req.getAttributes().put("unique Data", "TC03");
		req.getAttributes().put("unique Data Index", "0");
		req.getAttributes().put("isTableVertical", "false");
		System.out.println(new GetDataFromMultiRowData().execute(req));
	}
}