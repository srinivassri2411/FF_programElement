package integrationLogics;

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
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
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
	private String scriptId;
	private List<String> scriptStatusList = new ArrayList<String>();
	private List<String> scriptIdList = new ArrayList<String>();
	private List<String> scriptNameList = new ArrayList<String>();;
	private String currentLicenseId;

	@ReturnType(name = "result", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = null;
		try {
			// Get Execution Identifiers
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
//			Map<String, String> exeIdentifiers = Map.of("suiteId", "SUITE_e301786e-b97a-447a-80f2-2fce994d6d75",
//					"executionId", "EXEC69063", "runId", "Run0356d8c3-0ee0-4d48-86d1-bc4e4474678b", "projectId",
//					"PJT1001", "licenseId", "LIC27000");
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

				// Get Executed By and Scripts
				Map<String, String> queryMapSuiteResults = Map.of("executionId", executionId, "runId", runId);
				String suiteLevelMongoData = connectAndFetchMongoDocumentsAsJsonString(queryMapSuiteResults, null, null,
						currentLicenseId + "_" + executionId + "_entity_results");

				log.info("queryMapSuiteResults" + queryMapSuiteResults);

//				FileWriter fw = new FileWriter(new File("C:\\Users\\User\\Desktop\\JJJJJ.json"));
//				fw.write(executionDetailsResponsePayload.toString());
//				fw.close();

				scriptIdList = (List<String>) getJsonValue(suiteLevelMongoData,
						"[?(@.entityType=='Script')].originalId");
				System.out.println("scriptIdList" + scriptIdList);
				scriptIdList.remove(scriptIdList.size() - 1);
				for (int i = 0; i < scriptIdList.size(); i++) { // Ignore first and last script

					scriptId = scriptIdList.get(i);

					// Script level Api Request
					String scriptStatus = ((List<String>) (getJsonValue(suiteLevelMongoData,
							"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].status"))).get(0);
					scriptStatusList.add(scriptStatus);

					// Fetch details from automation response payload
					scriptNameList.add(((List<String>) (getJsonValue(suiteLevelMongoData,
							"[?(@.entityType=='Script' && @.originalId=='" + scriptId + "')].name"))).get(0));
				}

				if (scriptStatusList.contains("FAIL")) {

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
				}

				System.out.println("Excel file created successfully at: " + filePath);
				nlpResponseModel.setMessage("Excel is generated for given execution id");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("Excel is not generated for given execution id cause its script");
				nlpResponseModel.setStatus(CommonConstants.pass);
			}
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

	public static String connectAndFetchMongoDocumentsAsJsonString(Map<String, String> queryMap,
			Map<String, String> excludeMap, Map<String, List<Object>> notInMap, String collectionName) {

		MongoClient mongoClient = null;
		try {
			mongoClient = MongoClients.create("mongodb://localhost:27017");
			MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "Optimize_Dev_client");
			Query query = new Query();

			// Normal equality conditions
			if (queryMap != null) {
				for (Map.Entry<String, String> entry : queryMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if ("NOT_NULL".equals(value)) {
						query.addCriteria(Criteria.where(key).exists(true).ne(null));
					} else {
						query.addCriteria(Criteria.where(key).is(value));
					}
				}
			}

			// Exclude conditions
			if (excludeMap != null) {
				for (Map.Entry<String, String> entry : excludeMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (value == null) {
						query.addCriteria(Criteria.where(key).exists(true).ne(null));
					} else {
						query.addCriteria(Criteria.where(key).ne(value));
					}
				}
			}

			// NOT IN conditions
			if (notInMap != null) {
				for (Map.Entry<String, List<Object>> entry : notInMap.entrySet()) {
					String key = entry.getKey();
					List<Object> values = entry.getValue();

					if (values != null && !values.isEmpty()) {
						query.addCriteria(Criteria.where(key).nin(values));
					}
				}
			}

			// System.out.println("Generated Query : " + query);
			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);
			return documents.stream().map(Document::toJson).collect(java.util.stream.Collectors.joining(",", "[", "]"));
		} finally {
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

	public void clearAll() {

		// static/global variables
		executionId = null;
		variableData = null;
		baseUri = null;
		projectId = null;

		// instance variables
		runId = null;
		scriptId = null;
		currentLicenseId = null;

		// lists
		scriptStatusList.clear();
		scriptIdList.clear();
		scriptNameList.clear();
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