package custom_logics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetTextFromImageusingFreeOCR implements Nlp {

	@InputParams({ @InputParam(name = "FilePath", type = "java.lang.String") })
	@ReturnType(name = "text", type = "java.lang.String")
	@Override
	public List<String> getTestParameters() throws NlpException {
		return new ArrayList<>();
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		return new StringBuilder();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String filePath = (String) attributes.get("FilePath");

		String extractedText = "";

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			File file = new File(filePath);
			if (!file.exists()) {
				throw new RuntimeException("File does not exist at: " + filePath);
			}

			HttpPost post = new HttpPost("https://api8.ocr.space/parse/image");

			HttpEntity entity = MultipartEntityBuilder.create()
					.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName()).addTextBody("url", "")
					.addTextBody("language", "eng").addTextBody("isOverlayRequired", "true")
					.addTextBody("FileType", ".Auto").addTextBody("IsCreateSearchablePDF", "false")
					.addTextBody("isSearchablePdfHideTextLayer", "true").addTextBody("detectOrientation", "false")
					.addTextBody("isTable", "false").addTextBody("scale", "true").addTextBody("OCREngine", "5")
					.addTextBody("detectCheckbox", "false").addTextBody("checkboxTemplate", "0").build();

			post.setEntity(entity);
			post.addHeader("apikey", "donotstealthiskey_ip1");
			post.addHeader("accept", "application/json, text/javascript, /; q=0.01");
			post.addHeader("origin", "https://ocr.space");
			post.addHeader("referer", "https://ocr.space/");
			post.addHeader("user-agent", "Mozilla/5.0");

			String responseBody = client.execute(post, httpResponse -> EntityUtils.toString(httpResponse.getEntity()));

			// Parse JSON response
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(responseBody);
			JsonNode lineTextNode = root.path("ParsedResults").get(0).path("ParsedText");

			extractedText = lineTextNode.asText();

			nlpResponseModel.setMessage("Fetched text from image");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to fetch text from image: " + e.getMessage());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("text", extractedText);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("FilePath", "C:\\Users\\User\\Downloads\\sample.pdf");

		NlpResponseModel res = new GetTextFromImageusingFreeOCR().execute(req);
		System.out.println("Extracted Text: " + res.getAttributes().get("text"));
	}
}