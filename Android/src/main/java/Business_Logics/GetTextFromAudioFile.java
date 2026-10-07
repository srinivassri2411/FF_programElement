
package Business_Logics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.RequestFailedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetTextFromAudioFile implements Nlp {
	@InputParams({ @InputParam(name = "Audio File Path", type = "java.lang.String") })
	@ReturnType(name = "returnVar", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String audioFilePath = (String) programElementsInput.get("Audio File Path");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getText(audioFilePath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched text from given audio file");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetched text from given audio file " + writer);
		}
		nlpResponseModel.getAttributes().put("returnVar", returnValue);
		return nlpResponseModel;
	}

	public static String getText(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
		}

		String contentType = detectContentType(file.getName());
		ContentType ct = ContentType.create(contentType);
		String URL = "http://49.249.28.218:8108/acoe/transcribe";
		HttpPost post = new HttpPost(URL);
		HttpEntity multipart = MultipartEntityBuilder.create().addBinaryBody("file", file, ct, file.getName()).build();

		post.setEntity(multipart);

		try (CloseableHttpClient client = HttpClients.createDefault();
				CloseableHttpResponse response = client.execute(post)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode > 200) {
				throw new RequestFailedException(
						"Request failed " + new String(response.getEntity().getContent().readAllBytes()));
			}
			if (response.getEntity() != null) {
				URL = JsonPath.parse(new String(response.getEntity().getContent().readAllBytes())).read("$.text");
			}
		}
		return URL;
	}

	private static String detectContentType(String filename) {
		String lower = filename.toLowerCase();
		if (lower.endsWith(".mp3"))
			return "audio/mpeg";
		if (lower.endsWith(".wav"))
			return "audio/wav";
		return "application/octet-stream";
	}

	public static void main(String[] args) throws IOException {

//		String FILE_PATH = "C:/Users/User/Downloads/hjjhj.wav";
		String FILE_PATH = "C:\\Users\\User\\Downloads\\harvard.wav";
//		String FILE_PATH = "C:/Users/User/Downloads/download.png";
		System.out.println(getText(FILE_PATH));
	}
}