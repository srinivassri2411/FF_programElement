package specializedAutomation_Utility;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import io.restassured.http.ContentType;
import java.net.URL;
import java.util.HashMap;
import static io.restassured.RestAssured.*;



public class ConvertTextToSpeech implements Nlp {
	@InputParams({ @InputParam(name = "Text", type = "java.lang.String"),
			@InputParam(name = "Accent", type = "java.lang.String"),
			@InputParam(name = "File path with extension", type = "java.lang.String") })

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
		String text = (String) attributes.get("Text");
		String accent = (String) attributes.get("Accent");
		String filePath = (String) attributes.get("File path with extension");
		try {
			String fileid = getFileId(text, accent);
			downloadFile(fileid, filePath);
			nlpResponseModel.setMessage("Text got Converted");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to convert text to Speech Because:  " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		// Your program element business logic goes here ...

		return nlpResponseModel;
	}

	public static String getFileId(String text, String accent) {
		RestAssured.baseURI = "https://ttsmp3.com";
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Content-Type", "application/x-www-form-urlencoded");
		header.put("Connection", "keep-alive");
		header.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
		header.put("Accept", "*/*");
		String response = given().headers(header).contentType(ContentType.URLENC).formParam("msg", text)
				.formParam("lang", accent).formParam("source", "ttsmp3").when().post("/makemp3_new.php").then().log()
				.all().extract().response().asString();
		JsonPath js = new JsonPath(response);
		String fileId = js.get("MP3");
		System.out.println(fileId);
		return fileId;
	}

	private static void downloadFile(String fileID, String path) {
		String mp3Url = "https://ttsmp3.com/dlmp3.php?mp3=" + fileID + "&location=direct";
		downloadMp3File(mp3Url, path);
	}

	private static void downloadMp3File(String mp3Url, String fileName) {
		try (InputStream in = new URL(mp3Url).openStream(); FileOutputStream out = new FileOutputStream(fileName)) {

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}