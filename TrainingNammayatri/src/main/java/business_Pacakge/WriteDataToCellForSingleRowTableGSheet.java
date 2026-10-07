package business_Pacakge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class WriteDataToCellForSingleRowTableGSheet implements Nlp {

	@InputParams({ @InputParam(name = "sheetUrl", type = "java.lang.String"),
			@InputParam(name = "sheetName", type = "java.lang.String"),
			@InputParam(name = "uniqueData", type = "java.lang.String"),
			@InputParam(name = "uniqueDataIndex", type = "java.lang.String"),
			@InputParam(name = "header", type = "java.lang.String"),
			@InputParam(name = "data", type = "java.lang.String"),
			@InputParam(name = "isTableVertical", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String sheetUrl = (String) programElementsInput.get("sheetUrl");
		String sheetName = (String) programElementsInput.get("sheetName");
		String uniqueData = (String) programElementsInput.get("uniqueData");
		String uniqueDataIndex = (String) programElementsInput.get("uniqueDataIndex");
		String header = (String) programElementsInput.get("header");
		String data = (String) programElementsInput.get("data");
		String isTableVertical = (String) programElementsInput.get("isTableVertical");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {
			String url = "https://script.google.com/macros/s/AKfycbwQzEJAWgqoAHTTUAp-foQEeu057RIkiBJboKUvoGMP5MRS0yK3hh-7K6R2SKOH_Jk-/exec";
			String jsonBody = buildJson(sheetUrl, sheetName, Integer.parseInt(uniqueDataIndex), uniqueData, header,
					data, Boolean.parseBoolean(isTableVertical));
			String response = sendPostRequest(url, jsonBody);

			if (response.contains("Data written successfully")) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Success: Data written successfully");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failure: Unexpected response=> " + response);
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed => " + writer);
		}

		return nlpResponseModel;
	}

	// Helper method to send POST request using HttpURLConnection
	private static String sendPostRequest(String targetUrl, String jsonBody) throws IOException {
		URL url = new URL(targetUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Setting up the connection properties
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);

		// Writing the JSON body to the request output stream
		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		}
		StringBuilder response = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		}
		connection.disconnect();
		return response.toString();
	}

	private static String buildJson(String sheetUrl, String sheetName, int uniqueDataIndex, String uniqueData,
			String header, String data, boolean isTableVertical) {
		return String.format("""
				{
				"sheetUrl": "%s",
				"sheetName": "%s",
				"uniqueDataIndex": %d,
				"uniqueData": "%s",
				"header": "%s",
				"data": "%s",
				"isTableVertical": %s
				}
				""", sheetUrl, sheetName, uniqueDataIndex, uniqueData, header, data, isTableVertical);
	}

	public static void main(String[] args) throws FileNotFoundException, NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("sheetUrl",
				"https://docs.google.com/spreadsheets/d/1wCPp0FzjKO5eYCvPVw3-PBg72QI153rs0hX8bDU81LU/edit?gid=1220564949#gid=1220564949");
		model.getAttributes().put("sheetName", "Verbal");
		model.getAttributes().put("uniqueData", "SC003");
		model.getAttributes().put("uniqueDataIndex", "0");
		model.getAttributes().put("header", "Title");
		model.getAttributes().put("data", "bvvgjvhjjhv");
		model.getAttributes().put("isTableVertical", "false");
		System.out.println(new WriteDataToCellForSingleRowTableGSheet().execute(model));
	}
}
