package intersoft;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.jayway.jsonpath.JsonPath;

public class Execute {
	public static void main(String[] args) throws Exception {
		System.out.println(
				getAccessToken("https://us-app.fireflink.com" + "/appmanagement/optimize/v1/public/user/plugins-signin",
						"srinivas.m@fireflink.com",
						"99AA0740EF6BA330C45F486CF03A5DC70936AE6FAFB33582A7D7C11E996F42D642BFD92BC709"));
	}

	public static Map<String, String> getAccessToken(String url, String userName, String passtoken) throws Exception {
		AesUtil aesUtil = new AesUtil((userName + "_fireflink.com").substring(0, 16));
		String convertedPassword = CryptoUtil.encryptPasswordWithRSA(aesUtil.decrypt(passtoken.trim()));
		String postData = "{\"emailId\": \"" + userName + "\", \"password\": \"" + convertedPassword + "\"}";
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity((HttpEntity) new StringEntity(postData));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		Map<String, String> result = new HashMap<>();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			CloseableHttpResponse response = httpClient.execute((HttpUriRequest) httpPost);
			try {
				String res = EntityUtils.toString(response.getEntity());
				System.out.println(res);
				String accessToken = (String) JsonPath.read(res, "$.responseObject.access_token",
						new com.jayway.jsonpath.Predicate[0]);
				String licenseType = (String) JsonPath.read(res, "$.responseObject.licenseType",
						new com.jayway.jsonpath.Predicate[0]);
				result.put("accessToken", accessToken);
				result.put("licenseType", licenseType);
				if (response != null)
					response.close();
			} catch (Throwable throwable) {
				if (response != null)
					try {
						response.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
			if (httpClient != null)
				httpClient.close();
		} catch (Throwable throwable) {
			if (httpClient != null)
				try {
					httpClient.close();
				} catch (Throwable throwable1) {
					throwable.addSuppressed(throwable1);
				}
			throw throwable;
		}
		return result;
	}
}
