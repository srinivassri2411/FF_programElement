
package others;

import java.io.File;
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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class GetVirusTotalURL implements Nlp {
	private static String apikey = "7a7e9d5c3d04571094b59b54b42baa896faa29c87e8408afdca73efdd1434cd5";
	private static String baseUri = "https://www.virustotal.com/api/v3/";

	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String") })
	@ReturnType(name = "finalUrl", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("File Path");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String uploadURL = getUploadUrl();
//			System.out.println("Uploaded url" + uploadURL);
			String uploadedId = uploadFile(uploadURL, filePath);
//			System.out.println("Uploaded ID" + uploadedId);
			returnValue = "https://www.virustotal.com/gui/file/" + getDataId(uploadedId) + "/detection";
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("finalUrl", returnValue);
		return nlpResponseModel;
	}

	public static String getUploadUrl() {
		Response response = RestAssured.given().header("accept", "application/json").header("x-apikey", apikey).when()
				.get(baseUri + "files/upload_url").then().statusCode(200).extract().response();
		System.out.println(response.asPrettyString());
		return JsonPath.from(response.asPrettyString()).getString("data");
	}

	public static String uploadFile(String uploadUrl, String filePath) {
		File file = new File(filePath);
		Response response = RestAssured.given().header("x-apikey", apikey).multiPart("file", file).when()
				.post(uploadUrl).then().extract().response();
		System.out.println(response.asPrettyString());
		return JsonPath.from(response.asPrettyString()).getString("data.id");
	}

	public static String getDataId(String analysisId) throws InterruptedException {

		int maxRetries = 30;

		for (int i = 1; i <= maxRetries; i++) {

			Response response = RestAssured.given().header("accept", "application/json").header("x-apikey", apikey)
					.when().get(baseUri + "analyses/" + analysisId).then().extract().response();

			String status = response.jsonPath().getString("data.attributes.status");

			String sha256 = response.jsonPath().getString("meta.file_info.sha256");

			System.out.println("Attempt " + i + " | Status: " + status + " | SHA256: " + sha256);

			if (sha256 != null && !sha256.isBlank()) {
				System.out.println(response.asPrettyString());
				return sha256;
			}

			if ("completed".equalsIgnoreCase(status)) {
				throw new RuntimeException("Analysis completed but meta.file_info.sha256 was not returned.");
			}

			Thread.sleep(5000);
		}

		throw new RuntimeException("Timed out waiting for meta.file_info.sha256");
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("File Path",
				"C:\\Users\\User\\Downloads\\AmazonWorkSpacesApplicationsClientSetup.exe");
		System.out.println(new GetVirusTotalURL().execute(model));
	}
}