
package integrationLogics;

import static io.restassured.RestAssured.baseURI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class DownloadFireFlinkResults_Auto implements Nlp {

//	private static final Logger log = LoggerFactory.getLogger(DownloadFireFlinkResults_Auto.class);
	public static String executionId = "";
	public static String variableData = "";
	public static String baseUri = "";
	public static String projectId = "";
	private String runId;
	private String token;
	private String scriptId;
	private List<String> scriptStatusList = new ArrayList<String>();
	private List<String> scriptIdList = new ArrayList<String>();
	private List<String> scriptNameList = new ArrayList<String>();;
	private String currentLicenseId;

	@InputParams({ @InputParam(name = "Base URL", type = "java.lang.String"),
			@InputParam(name = "Fireflink_Username", type = "java.lang.String"),
			@InputParam(name = "Fireflink_Password", type = "java.lang.String"), })
	@ReturnType(name = "result", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String baseURl = (String) programElementsInput.get("Base URL");
		String fireflinkUsername = (String) programElementsInput.get("Fireflink_Username");
		String fireflinkPassword = (String) programElementsInput.get("Fireflink_Password");
		String filePath = null;
		baseURI = baseURl;
		try {
			// Get Execution Identifiers
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
			log.info("exeIdentifiers" + exeIdentifiers);
			if (exeIdentifiers.containsKey("suiteId")) {
				executionId = exeIdentifiers.get("executionId");
				runId = exeIdentifiers.get("runId");
				projectId = exeIdentifiers.get("projectId");
				currentLicenseId = exeIdentifiers.get("licenseId");

//			executionId = "EXEC31379";
//			runId = "Run5df24f38-3f4b-4934-93cd-3fae212936f5";
//			projectId = "PJT1142";
//			currentLicenseId = "LIC16627";

//			executionId = "EXEC66519";
//			runId = "Run323430f7-5fa0-45e4-9789-494d709c21dd";
//			projectId = "PJT1001";
//			currentLicenseId = "LIC27000";

				// Sign in API Request
				fireflinkPassword = encryptPassword(fireflinkPassword);
				String signInRequestPayload = "{ \"emailId\": \"" + fireflinkUsername + "\", \"password\": \""
						+ fireflinkPassword + "\",\"currentLicenseId\":\"" + currentLicenseId + "\"}";
				String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/signin",
						signInRequestPayload);

				String message = convertToString(getJsonValue(signInResponsePayload, "message"));

				// Provide message if EmailID/Password is Invalid
				if (message != null && message.contains("Email or Password is invalid")) {
					System.err.println(message);
					throw new RuntimeException(message);
				}

				// Access Token
				token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));

				log.info("Token" + token);
				// Get Executed By and Scripts
				String executionDetailsResponsePayload = getRequest(
						baseURl + "dashboardexecution/optimize/v3/dashboard/execution/" + executionId + "",
						"Authorization", "Bearer " + token, "projectId", projectId);

				log.info("executionDetailsResponsePayload" + executionDetailsResponsePayload);

				scriptIdList = (List<String>) getJsonValue(executionDetailsResponsePayload,
						"responseObject.preExecution.executionTree..children[?(@.type=='Script')].key");
				System.out.println("scriptIdList" + scriptIdList);
				scriptIdList.remove(scriptIdList.size() - 1);
				for (int i = 0; i < scriptIdList.size(); i++) { // Ignore first and last script

					scriptId = scriptIdList.get(i);

					// Script level Api Request
					String scriptLevelResponsePayload = getRequest("executionresult/optimize/v3/execution-result/run/"
							+ executionId + "/" + runId + "/" + scriptId, "Authorization", "Bearer " + token,
							"projectId", projectId);
					// Add Script status to Status List
					String scriptStatus = convertToString(
							getJsonValue(scriptLevelResponsePayload, "responseObject.status"));
					scriptStatusList.add(scriptStatus);
					// Fetch details from automation response payload
					scriptNameList
							.add(convertToString(getJsonValue(scriptLevelResponsePayload, "responseObject.title")));
				}

				Workbook workbook = new XSSFWorkbook();
				Sheet sheet = workbook.createSheet("Failed Scripts");

				Row header = sheet.createRow(0);
				header.createCell(0).setCellValue("Sl.No");
				header.createCell(1).setCellValue("Script Name");
				header.createCell(2).setCellValue("Script ID");
				header.createCell(3).setCellValue("Status");
				header.createCell(4).setCellValue("Jira ID");

				int rowNum = 1;
				int serialNo = 1;
				log.info("scriptIdList" + scriptIdList);
				log.info("scriptStatusList" + scriptStatusList);
				log.info("scriptNameList" + scriptNameList);
				for (int i = 0; i < scriptIdList.size(); i++) {
					String status = scriptStatusList.get(i);

					if (status.toLowerCase().contains("fail")) {
						String name = scriptNameList.get(i);
						String key = scriptIdList.get(i);
						Row row = sheet.createRow(rowNum++);
						String jiraId = "";
						row.createCell(0).setCellValue(serialNo++);
						row.createCell(1).setCellValue(name);
						row.createCell(2).setCellValue(key);
						row.createCell(3).setCellValue(status);
						row.createCell(4).setCellValue(jiraId);
					}
				}

				for (int i = 0; i < 5; i++) {
					sheet.autoSizeColumn(i);
				}

				String userHome = System.getProperty("user.home");

				// Create directory if it doesn't exist
				String folderPath = userHome + File.separator + "FireFlinkResults";
				File folder = new File(folderPath);
				if (!folder.exists()) {
					folder.mkdirs();
				}

				// Build file path with execId
				filePath = folderPath + File.separator + executionId + "_Excel_File.xlsx";

				FileOutputStream fileOut = new FileOutputStream(filePath);
				workbook.write(fileOut);

				fileOut.close();
				workbook.close();

				System.out.println("Excel file created successfully at: " + filePath);
			}
			nlpResponseModel.setMessage("Excel is generated for given execution id");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to generate => " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		ArrayList<String> listFiles = new ArrayList<>();
		listFiles.add(filePath);
		nlpResponseModel.setOutputLocalFilePaths(listFiles);
		nlpResponseModel.getAttributes().put("result", filePath);
		return nlpResponseModel;
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]);
		return ((Response) request.when().get(endpoint, new Object[0])).asString();
	}

	private static String postRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = RestAssured.given();
		if (headers.length % 2 != 0)
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		for (int i = 0; i < headers.length; i += 2)
			request.header(headers[i], headers[i + 1], new Object[0]);
		return ((Response) request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint, new Object[0]))
				.asString();
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath, new com.jayway.jsonpath.Predicate[0]);
	}

	public static boolean convertToBoolean(Object obj) {
		return ((Boolean) obj).booleanValue();
	}

	public static String convertToString(Object obj) {
		return (String) obj;
	}

	public static double convertToDouble(Object obj) {
		return ((Double) obj).doubleValue();
	}

	public static String encryptPassword(String password) throws Exception {
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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
//		model.getAttributes().put("Base URL", "https://app.v3.fireflink.com/");
//		model.getAttributes().put("Fireflink_Username", "srinivas.m@fireflink.com");
//		model.getAttributes().put("Fireflink_Password", "Qwerty@123");

		model.getAttributes().put("Base URL", "https://sonicwall.fireflink.com/");
		model.getAttributes().put("Fireflink_Username", "varun.kn@fireflink.com");
		model.getAttributes().put("Fireflink_Password", "Varun@9902098003");
		log.info(new DownloadFireFlinkResults_Auto().execute(model) + "");
	}
}