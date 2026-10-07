package specializedAutomation_Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class VoiceToText implements Nlp {
	@InputParams({ @InputParam(name = "AccessToken", type = "java.lang.String"),
			@InputParam(name = "FilePath", type = "java.lang.String") })
	@ReturnType(name = "text", type = "java.lang.String")

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
		String AccessToken = (String) attributes.get("AccessToken");
		String FilePath = (String) attributes.get("FilePath");
		String text = "";
		try {
			text = VoiceTotext(FilePath, AccessToken, nlpResponseModel);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nlpResponseModel.getAttributes().put("text", text);
		return nlpResponseModel;

	}

	public static String VoiceTotext(String mp3InputFile, String accessToken, NlpResponseModel nlpresponse)
			throws ParseException, IOException {
		String apiUrl = "https://api.wit.ai/speech";
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(apiUrl);

		httpPost.setHeader("Authorization", "Bearer " + accessToken);

		File audioFile = new File(mp3InputFile);
		FileEntity fileEntity = new FileEntity(audioFile, ContentType.create("audio/mpeg"));

		httpPost.setEntity(fileEntity);

		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity responseEntity = response.getEntity();

		if (responseEntity != null) {
			String responseString = EntityUtils.toString(responseEntity);
			EntityUtils.consume(responseEntity);
			int index = responseString.lastIndexOf("text");
			int index1 = responseString.lastIndexOf(",");
			String text = responseString.substring(index + 7, index1);
			nlpresponse.setStatus(CommonConstants.pass);
			nlpresponse.setMessage("Succesfully transcripted text from voice " + responseString + " " + text);
			return text;
		} else {
			throw new RuntimeException("Failed to get response from the API");
		}
	}
}