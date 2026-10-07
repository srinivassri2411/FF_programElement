import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fireflink.utils.FireflinkApis;
import com.jayway.jsonpath.JsonPath;

public class Snippet {
	static String v3Base = "https://app.v3.fireflink.com/";
	static String baseURI = "https://app.fireflink.com/";

	public static void main(String[] args) throws IOException, InterruptedException {
		String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJNODJhX1U2VHRFSnFvWV9jdzNfWi1KaWVoZ2hQcjJ4QjVMbnlNZ3RtRGs0In0.eyJleHAiOjE3Njg4MjUxMDcsImlhdCI6MTc2ODgyNDUwNywianRpIjoiYzdlMWRlMDktY2NmYi00YzZjLTk3MjEtZWRkYWVhM2Q0ZDk3IiwiaXNzIjoiaHR0cHM6Ly9hcHAuZmlyZWZsaW5rLmNvbTozMTAwMS9yZWFsbXMvRmlyZUZsaW5rIiwic3ViIjoiZjphZjhmZmUyYS1jMTIyLTQ3ZjAtOTIxYS01ODgyODcwMjVjZmM6c3Jpbml2YXMubUBmaXJlZmxpbmsuY29tIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmxpbmstc2VydmljZSIsInNlc3Npb25fc3RhdGUiOiIxZjFjYTgxYy1iMzc0LTRhMzMtYmM2YS04ZTY3NjhkZmMwNTAiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6IjFmMWNhODFjLWIzNzQtNGEzMy1iYzZhLThlNjc2OGRmYzA1MCIsImN1cnJlbnRMaWNlbnNlSWQiOiJMSUMxMTg3MCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY3VycmVudFByaXZpbGVnZSI6IlN1cGVyIEFkbWluIiwiZnVsbE5hbWUiOiJTcmluaXZhcyBNIiwiYWN0aXZhdGlvblN0YXR1cyI6IkFDVElWRSIsInByaXZpbGVnZSI6IlN1cGVyIEFkbWluIiwibGljZW5zZU5hbWUiOiJQcm9ncmFtRWxlbWVudHNUZXN0aW5nLUxJQzExODcwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiU3Jpbml2YXMgTSIsInVzZXJOYW1lIjoic3Jpbml2YXMubUBmaXJlZmxpbmsuY29tIiwiYmlsbGluZ0N5Y2xlIjoiWWVhcmx5IiwiaWQiOiJVU1I1NTkyMyIsImxpY2Vuc2VJZCI6IkxJQzExODcwIiwiZW1haWwiOiJzcmluaXZhcy5tQGZpcmVmbGluay5jb20ifQ.sqS6By_noEHYWZK43Y7n-znquUOsy9SgFDnQDOf1a5LIcbDaA9PVK5f7S_lkzAmQeqbW59g3CR9d2GIwKwHGNxE9Ac-7ymLy0MasNhn4urWMIBC5RJ5N2ArA3sm5iteh6SHMdXQs3RcoFT4rjrG22VNcetPhD3j-emt0jiv1AXj-zjV_MRAAMiEyK2lkxKdqCW-5B68SvLW8SVTLeoTVAKhaiEfUubG7VuPdz52jmNq6MOEc63iEZLeHaiTn7fARXHGT7r98q9MHTMK1UcXDUyQZqku7GicY2kma9bryv05aeXusdw1PdqUEUnl8P9Ukx0x_4k-CP4bvemxI6IiVvg";
		String projId = "PJT1020";
		String suiteId = "SUITE1049";
		String machineName = "FFE-Srinivas-M";
		FireflinkApis fireflinkApis = new FireflinkApis();
		JsonNode suiteDetails = fireflinkApis.getSuiteDetails(accessToken, projId, suiteId);
		System.out.println("suiteDetails" + suiteDetails);
		JsonNode waitTime = suiteDetails.get("responseObject").get("waitTime");
		JsonNode resultSetting = suiteDetails.get("responseObject").get("resultSetting");
		JsonNode executionTermination = suiteDetails.get("responseObject").get("executionTermination");
		JsonNode autoRunSuite = suiteDetails.get("responseObject").get("autoRunSuite");
		JsonNode clientMachines = fireflinkApis.getMachineDetails(accessToken, projId);
		Map<String, Map<String, Object>> machineDetails = new LinkedHashMap<String, Map<String, Object>>();
		machineDetails.put(machineName, new LinkedHashMap<String, Object>());
		List<JsonNode> machineObject = fireflinkApis.buildMachineObject(clientMachines, machineDetails);
		System.out.println("machineObject" + machineObject);
		JsonNode selectedModules = fireflinkApis.selectedModulesAndScripts(suiteDetails);
		System.out.println("selectedModules" + selectedModules);
		JsonNode payload = fireflinkApis.generateSuiteBody(selectedModules, machineObject, waitTime, resultSetting,
				executionTermination, autoRunSuite);
		System.out.println(payload);
		String execId = fireflinkApis.triggerSuite(accessToken, payload, projId, suiteId);
		System.out.println(execId);
	}

//	public static void main(String[] args) throws Exception {
//		System.out.println(getBearerTokenV3("LIC16627", "srinivas.m@testyantra.com", "Srini@123"));
//		System.out.println(getBearerTokenV2("LIC16627", "srinivas.m@fireflink.com", "Qwerty@123"));
//	}
//
//	public static String getBearerTokenV2(String licId, String email, String password) throws Exception {
//		System.out.println("V2 url is " + baseURI);
//		password = encryptPasswordV2(password);
//		HttpClient client = HttpClient.newHttpClient();
//		String body = "{ \"currentLicenseId\": \"" + licId + "\", \"emailId\": \"" + email + "\", \"password\": \""
//				+ password + "\"}";
//		HttpRequest request = HttpRequest.newBuilder()
//				.uri(URI.create(baseURI + "appmanagement/optimize/v1/public/user/client-signin"))
//				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
//		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//		System.out.println("Login status is {}" + JsonPath.from(response.body()).get("responseCode") + "");
//		return JsonPath.from(response.body()).get("responseObject.access_token");
//	}
//
//	public static String getBearerTokenV3(String licId, String email, String password) throws Exception {
//		System.out.println("V3 url is " + v3Base);
//		password = encryptPasswordV3(password);
//		HttpClient client = HttpClient.newHttpClient();
//		String body = "{ \"currentLicenseId\": \"" + licId + "\", \"emailId\": \"" + email + "\", \"password\": \""
//				+ password + "\"}";
//		HttpRequest request = HttpRequest.newBuilder()
//				.uri(URI.create(v3Base + "appmanagement/optimize/v1/public/user/client-signin"))
//				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
//		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//		System.out.println("Login status is {}" + JsonPath.from(response.body()).get("responseCode") + "");
//		return JsonPath.from(response.body()).get("responseObject.access_token");
//	}
//
//	public static String encryptPasswordV2(String plainText) throws Exception {
//		byte[] publicKeyBytes = Base64.getDecoder().decode(
//				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
//		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
//		return Base64.getEncoder().encodeToString(encryptedBytes);
//	}
//
//	public static String encryptPasswordV3(String password) throws Exception {
//		byte[] publicKeyBytes = Base64.getDecoder().decode(
//				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//		PublicKey publicKey = keyFactory.generatePublic(keySpec);
//
//		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
//		OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
//				PSource.PSpecified.DEFAULT);
//		cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
//
//		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
//		return Base64.getEncoder().encodeToString(encryptedBytes);
//	}
}