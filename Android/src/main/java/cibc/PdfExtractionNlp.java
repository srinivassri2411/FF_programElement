package cibc;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.response.Response;

public class PdfExtractionNlp implements Nlp {

	private static final String BASE_URL = "https://pdf-services.adobe.io";

	@InputParams({ @InputParam(name = "Client ID", type = "java.lang.String"),
			@InputParam(name = "Client Secret", type = "java.lang.String"),
			@InputParam(name = "PDF File Path", type = "java.lang.String") })
	@ReturnType(name = "Extracted Text File Path", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> inputs = nlpRequestModel.getAttributes();
		String clientId = (String) inputs.get("Client ID");
		String clientSecret = (String) inputs.get("Client Secret");
		String pdfFilePath = (String) inputs.get("PDF File Path");
		String extractedFilePath = null;
		String apiKey = clientId;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {
			// Step 1: Get Access Token
			String accessToken = getAccessToken(clientId, clientSecret);

			// Step 2: Get Upload URL
			DocumentContext uploadResp = getUploadUrl(accessToken, apiKey);
			String uploadUrl = uploadResp.read("$.uploadUri");
			String assetId = uploadResp.read("$.assetID");

			// Step 3: Upload PDF
			uploadPdf(uploadUrl, pdfFilePath);

			// Step 4: Trigger Extraction
			String statusUrl = triggerExtraction(accessToken, assetId, apiKey);

			// Step 5: Poll until extraction completes
			String downloadUri = pollUntilComplete(accessToken, statusUrl, apiKey);

			// Step 6: Download result and save extracted text to temp file
			extractedFilePath = downloadAndSaveTextContentToTempFile(downloadUri);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("PDF extraction successful");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("PDF extraction failed: " + e.getMessage());
			e.printStackTrace();
		}

		nlpResponseModel.getAttributes().put("Extracted Text File Path", extractedFilePath);
		return nlpResponseModel;
	}

	private String getAccessToken(String clientId, String clientSecret) {
		Response response = given().baseUri(BASE_URL).formParam("client_id", clientId)
				.formParam("client_secret", clientSecret).post("/token");

		return "Bearer " + response.jsonPath().getString("access_token");
	}

	private DocumentContext getUploadUrl(String accessToken, String apiKey) {
		Response response = given().baseUri(BASE_URL).header("Authorization", accessToken).header("X-API-Key", apiKey)
				.contentType("application/json").body("{\"mediaType\":\"application/pdf\"}").post("/assets");

		return JsonPath.parse(response.getBody().asPrettyString());
	}

	private void uploadPdf(String uploadUrl, String filePath) throws Exception {
		File file = new File(filePath);

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPut httpPut = new HttpPut(uploadUrl);
			httpPut.setHeader("Content-Type", "application/pdf");
			FileEntity fileEntity = new FileEntity(file, ContentType.APPLICATION_PDF);
			httpPut.setEntity(fileEntity);

			ClassicHttpResponse response = httpClient.execute(httpPut);
			int statusCode = response.getCode();
			if (statusCode < 200 || statusCode >= 300) {
				throw new RuntimeException("Upload failed with status code: " + statusCode);
			}
		}
	}

	private String triggerExtraction(String accessToken, String assetId, String apiKey) throws JSONException {
		JSONObject requestJson = new JSONObject();
		requestJson.put("assetID", assetId);
		requestJson.put("getCharBounds", "false");
		requestJson.put("includeStyling", "false");
		requestJson.put("elementsToExtract", new String[] { "text", "tables" });
		requestJson.put("tableOutputFormat", "xlsx");
		requestJson.put("renditionsToExtract", new String[] { "tables", "figures" });

		Response response = given().baseUri(BASE_URL).header("Authorization", accessToken).header("x-api-key", apiKey)
				.contentType("application/json").body(requestJson.toString()).post("/operation/extractpdf/");

		return response.getHeader("location");
	}

	private String pollUntilComplete(String accessToken, String statusUrl, String apiKey) throws InterruptedException {
		while (true) {
			Response response = given().header("Authorization", accessToken).header("x-api-key", apiKey)
					.contentType("application/json").get(statusUrl);

			String status = response.jsonPath().getString("status");
			if ("done".equalsIgnoreCase(status)) {
				return response.jsonPath().getString("content.downloadUri");
			} else if ("failed".equalsIgnoreCase(status)) {
				throw new RuntimeException("PDF Extraction failed");
			}

			Thread.sleep(3000);
		}
	}

	private String downloadAndSaveTextContentToTempFile(String downloadUri) throws Exception {
		String jsonString;

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(downloadUri);

			try (ClassicHttpResponse response = httpClient.executeOpen(null, request, null)) {
				int statusCode = response.getCode();
				if (statusCode != 200) {
					throw new RuntimeException("Failed to download file. HTTP status: " + statusCode);
				}

				jsonString = EntityUtils.toString(response.getEntity());
			}
		}

		JSONObject json = new JSONObject(jsonString);
		JSONArray elements = json.getJSONArray("elements");

		File tempFile = File.createTempFile("pdf-extract-", ".txt");

		try (FileWriter writer = new FileWriter(tempFile)) {
			for (int i = 0; i < elements.length(); i++) {
				JSONObject obj = elements.getJSONObject(i);
				if (obj.has("Text")) {
					writer.write(obj.getString("Text"));
					writer.write(System.lineSeparator());
				}
			}
		}

		return tempFile.getAbsolutePath();
	}
}