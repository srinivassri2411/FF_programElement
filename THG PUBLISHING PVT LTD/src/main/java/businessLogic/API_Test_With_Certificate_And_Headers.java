
package businessLogic;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class API_Test_With_Certificate_And_Headers implements Nlp {
	@InputParams({ @InputParam(name = "baseURL", type = "java.lang.String"),
			@InputParam(name = "endPoint", type = "java.lang.String"),
			@InputParam(name = "pfxFilePath", type = "java.lang.String"),
			@InputParam(name = "headerName", type = "java.lang.String"),
			@InputParam(name = "headerValue", type = "java.lang.String")})
	@ReturnType(name = "Return Value", type = "java.util.Map")

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

		String baseURL = (String) attributes.get("baseURL");
		String endPoint = (String) attributes.get("endPoint");
		String pfxFilePath = (String) attributes.get("pfxFilePath");
		String headername = (String) attributes.get("headerName");
		String headerValue = (String) attributes.get("headerValue");
		Map<String, Object> map = null;
		try {
			map = getResponse(baseURL, endPoint, pfxFilePath,headername,headerValue);
			//String responseBody = map.get("ResponseBody").toString();
			String statusMsg = map.get("StatusMessage").toString();

			if (!statusMsg.equalsIgnoreCase("Error")) {
				if (statusMsg.equalsIgnoreCase("OK")) {
					nlpResponseModel.setMessage("API Response for given Request is Successfull");
					nlpResponseModel.setStatus(CommonConstants.pass);
				} else {
					nlpResponseModel.setMessage("API Response for given Request is Unsuccessfull");
					nlpResponseModel.setStatus(CommonConstants.fail);
				}

			} else if (statusMsg.equalsIgnoreCase("Error")) {
				//nlpResponseModel.setMessage("Failed to get API Response for given Request  [" + responseBody+"]");
				nlpResponseModel.setMessage("Failed to get API Response for given Request");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {

			nlpResponseModel.setMessage("Failed to get API Response for given Request  " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("Return Value", map);
		return nlpResponseModel;
	}

	public static Map<String, Object> getResponse(String baseURL, String endPoint, String pfxFilePath,String headerName,String headerValue)
			throws Exception {
		Map<String, Object> map = new LinkedHashMap<>();
		HttpResponse response = null;

		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(new FileInputStream(pfxFilePath), "".toCharArray());

			SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, null).build();

			HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
			long startTime = System.currentTimeMillis();
			HttpGet httpGet = new HttpGet(baseURL + endPoint);
			httpGet.setHeader(headerName, headerValue);
			response = httpClient.execute(httpGet);
			long endTime = System.currentTimeMillis();

			map.put("StatusCode", response.getStatusLine().getStatusCode());
			map.put("StatusMessage", response.getStatusLine().getReasonPhrase());
			map.put("ResponseTime", endTime - startTime);
			map.put("RequestHeaders", Arrays.asList(httpGet.getAllHeaders()));
			map.put("ResponseHeaders", Arrays.asList(response.getAllHeaders()));
			//map.put("ResponseBody", EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
			map.put("Errors", "N/A");
		} catch (Exception e) {
			// TODO: handle exception
			map.put("StatusCode", "N/A");
			map.put("StatusMessage", "Error");
			map.put("ResponseTime", "N/A");
			map.put("RequestHeaders", "N/A");
			map.put("ResponseHeaders", "N/A");
			//map.put("ResponseBody", "N/A");
			map.put("Errors", e.getLocalizedMessage());
		}

		return map;
	}
}