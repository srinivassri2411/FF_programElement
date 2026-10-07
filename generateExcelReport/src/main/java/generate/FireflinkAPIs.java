package generate;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.jayway.jsonpath.JsonPath;

public class FireflinkAPIs {
	public static final String PASSWORD_ENCRYPTION_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB";

	public static String getSuiteExecutionDetails(String url, String token, String licenseType, String projectId)
			throws IOException {
		String result = "";
		HttpGet get = new HttpGet(url);
		get.setHeader("Authorization", "Bearer " + token);
		get.setHeader("Accept", "application/json");
		get.setHeader("Content-type", "application/json");
		get.setHeader("licenseType", licenseType);
		get.setHeader("projectId", projectId);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			CloseableHttpResponse response = httpClient.execute((HttpUriRequest) get);
			try {
				result = EntityUtils.toString(response.getEntity());
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

	public static String encryptPasswordWithRSA(String password) throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"),
				PSource.PSpecified.DEFAULT);
		cipher.init(1, publicKey, oaepParams);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public static Map<String, String> getAccessToken(String url, String userName, String passtoken, String licenseId)
			throws Exception {
		String encrpt = encryptPasswordWithRSA(passtoken);
//		System.out.println("encrpt" + encrpt);
		String postData = "{\"emailId\": \"" + userName + "\", \"password\": \"" + encrpt
				+ "\", \"currentLicenseId\": \"" + licenseId + "\"}";
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
//				System.out.println("Login Response is " + res);
				String accessToken = (String) JsonPath.read(res, "$.responseObject.access_token",
						new com.jayway.jsonpath.Predicate[0]);
				String licenseType = (String) JsonPath.read(res, "$.responseObject.licenseType",
						new com.jayway.jsonpath.Predicate[0]);
				String id = (String) JsonPath.read(res, "$.responseObject.id", new com.jayway.jsonpath.Predicate[0]);
				result.put("accessToken", accessToken);
				result.put("licenseType", licenseType);
				result.put("userId", id);
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
