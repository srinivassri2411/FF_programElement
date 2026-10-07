
package businessLogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Utility implements Nlp {

	String token = null;
	String pID = null;
	String json = null;
	String folderName = null;

	String masterFolderName = null;

	String masterfileName = null;
	String datafileName = null;
	String downloadDatafilePath = null;
	String downloadMasterfilePath = null;
	String uploadfilePath = null;
	WebDriver driver = null;
//    @InputParams({@InputParam(name = "string1", type = "java.lang.String"), @InputParam(name = "string2", type = "java.lang.String")})
//    @ReturnType(name = "string3", type = "java.lang.String")

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
		driver = nlpRequestModel.getWebDriver();
//          String string1 = (String) attributes.get("string1");
//          String string2 = (String) attributes.get("string2");

		// Your program element business logic goes here ...

//          String string3 = "Return Value";
//          nlpResponseModel.getAttributes().put("string3", string3);
		return nlpResponseModel;
	}

	public String Signin(String licId, String email, String password, String url) throws Exception {
		System.out.println(url);
		password = encryptPasswordV3(password);
		HttpClient client = HttpClient.newHttpClient();
		String body = "{ \"currentLicenseId\": \"" + licId + "\", \"emailId\": \"" + email + "\", \"password\": \""
				+ password + "\"}";
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url + "/appmanagement/optimize/v1/public/user/client-signin"))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(
				"Login status is" + io.restassured.path.json.JsonPath.from(response.body()).get("responseCode") + "");
		return "Bearer " + io.restassured.path.json.JsonPath.from(response.body()).get("responseObject.access_token");
	}

	public static String encryptPasswordV3(String password) throws Exception {
		byte[] publicKeyBytes = Base64.getDecoder().decode(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
				PSource.PSpecified.DEFAULT);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);

		byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

//	public String downloadData(String FolderName, String dataFileName, String downloadDataFilePath, String toEmail,
//			String subject, WebDriver driver) throws Exception {
//		downloadDatafilePath = downloadDataFilePath;
//		datafileName = dataFileName;
//		folderName = FolderName;
//		token = Signin("LIC16281", "sysadmin@thehindu.co.in", "FlinkFire@859", "https://sg-app.fireflink.com");
//		downloadFile(datafileName, downloadDatafilePath, FolderName);
//		writeEmail(downloadDatafilePath + "" + datafileName, toEmail, subject, driver);
//		return pID;
//	}

	public void uploadMaster(String dataFileName, String masterFileName, String downloadMasterFilePath)
			throws Exception {
		System.out.println("files----files---" + masterFileName + " " + downloadMasterFilePath);
		masterfileName = masterFileName;
		downloadMasterfilePath = downloadMasterFilePath;
//		String pid = PID;

//		folderName = FolderName;
		token = Signin("LIC16281", "sysadmin@thehindu.co.in", "FlinkFire@859", "https://sg-app.fireflink.com");
		downloadFile(masterfileName, downloadMasterfilePath);
		uploadFile(downloadMasterFilePath, dataFileName);

	}

	public static Response getRequest(String endpoint, String token, String header) {
		return RestAssured.given().header("Authorization", token).header("projectId", header).get(endpoint);
	}

	public void downloadFile(String pID, String path) throws IOException {
		json = getJson(token);
		System.out.println("PID+--------" + pID);
		RestAssured.baseURI = "https://sg-app.fireflink.com/";
		Response responseFileIds = getRequest("project/optimize/v1/file/" + pID, token, "PJT1004");
		System.out.println(responseFileIds.body().asString());
		datafileName = JsonPath.parse(responseFileIds.body().asString()).read("$.responseObject.name");
		Response response = getRequest("project/optimize/v1/file/download/cloud/" + pID, token, "PJT1004");
		byte[] byteArray = response.getBody().asByteArray();
		FileOutputStream fileOutputStream = new FileOutputStream(path + "" + datafileName);
		fileOutputStream.write(byteArray);
		fileOutputStream.close();
		System.out.println("File downloaded successfully.");
	}

	public void uploadFile(String filepath, String dataFileName) throws IOException {
//		json = getJson(token);
		System.out.println("PID+--------" + dataFileName);
		RestAssured.baseURI = "https://sg-app.fireflink.com/";
		filepath = filepath + datafileName;
		System.out.println(filepath);
//		System.out.println(RestAssured.given().header("Authorization", token).header("projectId", "PJT1004")
//				.multiPart("file", filepath).put("project/optimize/v1/file/replace/cloud/" + dataFileName).then()
//				.extract().body().asString());

		String url = "https://sg-app.fireflink.com/project/optimize/v1/file/replace/cloud/" + dataFileName;

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPut httpPut = new HttpPut(url);
		httpPut.setHeader("projectid", "PJT1004");
		httpPut.setHeader("authorization", token);
		httpPut.setHeader("sectionname", "Test Data");
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", new File(filepath));

		HttpEntity multipart = builder.build();
		httpPut.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(httpPut);

		try {
			System.out.println("Status Code: " + response.getStatusLine().getStatusCode());
			System.out.println("Response Body:");
			System.out.println(EntityUtils.toString(response.getEntity()));
		} finally {
			response.close();
			httpClient.close();
		}
		System.out.println("File uploaded successfully.");
	}

	public String projectIDAndFileID(String foldername, String Json, String fileName) throws IOException {
		String key = null;
//  		String data = new String(Files.readAllBytes(Paths.get("C:\\Users\\User\\Desktop\\InputsValue.Json")));
		String json = getJson(token);
		System.out.println("Varun " + json);
//  	      JsonObject jsonObject = jsonElement.getAsJsonObject();
		String path = "responseObject.folderTree[0].fileCount";
		System.out.println("Filecount--------" + path);
		int iValue = 0;
		Object res = JsonPath.read(json, path);
		int resu = (int) res;
		System.out.println(resu);
//  			String jsonString = jsonObject.toString();

//  			String path = "responseObject.folderTree[0].fileCount";
		for (int i = 0; i < resu; i++) {

			System.out.println("---for-----");
			String folderName = JsonPath.read(json, "responseObject.folderTree[0].children[" + i + "].name");
			System.out.println("---folderName-----" + folderName);

			if (folderName.equals(foldername)) {
				key = JsonPath.read(json, "responseObject.folderTree[0].children[" + i + "].key");
				iValue = i;
				System.out.println("if " + key);
				break;
			}
			System.out.println("-----key---" + key);
		}
		key = fileID(key, fileName);
		// Only returning the child object
//  			System.out.println(res);

		return key;

	}

	public String fileID(String iValue, String fileName) throws IOException {
		System.out.println("File-----------");
		String key = null;
		String url = "https://sg-app.fireflink.com/alltrees/optimize/v3/trees/folder?sourceId=" + iValue
				+ "&action=expand";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// Request method
		con.setRequestMethod("GET");

		// Request headers
		con.setRequestProperty("Authorization", token);

		// Send the GET request
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		// Read the response
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Print the response
		System.out.println(response.toString());
		String json = response.toString();

//		log.info(json);
		String path = "responseObject.folderTree[0].fileCount";
		Object res = JsonPath.read(json, path);
		int resu = (int) res;

//  			String path = "responseObject.folderTree[0].fileCount";

		for (int i = 0; i < resu; i++) {

			System.out.println();
			String searchKey = JsonPath.read(json, "responseObject.folderTree[0].children[" + i + "].title");

			if (searchKey.contains(fileName)) {
				System.out.println("----json---------" + json);
				key = JsonPath.read(json, "responseObject.folderTree[0].children[" + i + "].key");
				System.out.println("if " + key);
			}
		}

		return key;

	}

	public String getJson(String key) throws IOException {
		StringBuilder response = null;
		try {
			String url = "https://sg-app.fireflink.com/alltrees/optimize/v1/trees/folders";
			URL urlObject = new URL(url);

			// Open connection
			HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

			// Set request method to GET
			connection.setRequestMethod("GET");

			// Add headers
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", key); // Replace with your actual bearer token
//  	            connection.setRequestProperty("projectType", "Web & Mobile");
			connection.setRequestProperty("projectId", "PJT1004");

			// Get response code
			int responseCode = connection.getResponseCode();
			System.out.println("Response Code: " + responseCode);

			// Read response content
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				in.close();
				System.out.println("Response Body: " + response.toString());
			} else {
				System.out.println("Error in GET request");
			}

			// Close the connection
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();

	}

}