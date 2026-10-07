package sonicwallAIO;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.apache.http.client.ClientProtocolException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

//@Slf4j

public class UpdateScriptResult_DB implements Nlp {

	private static final Logger log = LoggerFactory.getLogger(UpdateScriptResult_DB.class);
	private static final String URL = "jdbc:mysql://localhost:3306/AutomationDB";
	private static final String USER = "root";
	private static final String PASSWORD = "root";

	@InputParams({ @InputParam(name = "FireflinkMailId", type = "java.lang.String"),
			@InputParam(name = "FireflinkPassword", type = "java.lang.String"),
			@InputParam(name = "Table Name", type = "java.lang.String") })
	@ReturnType(name = "cycleID", type = "java.lang.String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String tableName = (String) programElementsInput.get("Table Name");
		String fireflinkMailId = (String) programElementsInput.get("FireflinkMailId");
		String fireflinkPassword = (String) programElementsInput.get("FireflinkPassword");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
//			List<String> scriptDetails = new ArrayList<String>();
//			scriptDetails.add("FAIL");
//			scriptDetails.add("EXEC78001");
//			scriptDetails.add("SUITE1095");

			Map<String, String> identifier = nlpRequestModel.getExecutionIdentifiers();
			String scriptId = identifier.get("scriptId");
			String projectId = identifier.get("projectId");
			String executionId = identifier.get("executionId");
			String runId = identifier.get("runId");
			String licenseId = identifier.get("licenseId");

//			String scriptId = "TC001";
//			String projectId = "PJT1001";
			List<String> scriptDetails = getScriptDetails(scriptId, executionId, projectId, runId, licenseId);
			pushResults(scriptId, scriptDetails.get(0), scriptDetails.get(1), scriptDetails.get(2), tableName,
					fireflinkMailId, fireflinkPassword, projectId);

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Given execution details has been updated to aio");
		} catch (ClientProtocolException e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Update failed since its script level");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to update => " + writer);
			nlpResponseModel.setStatus(CommonConstants.pass);
		}
		nlpResponseModel.getAttributes().put("cycleID", "");
		return nlpResponseModel;
	}

	public static List<String> getScriptDetails(String scriptId, String execId, String projectID, String runId,
			String licId) throws Exception {
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
				"Optimize_Dev_client");
		Query query = new Query();
		query.addCriteria(Criteria.where("scriptId").is(scriptId));
		query.addCriteria(Criteria.where("executionId").is(execId));
		query.addCriteria(Criteria.where("runId").is(runId));
//		LIC27000_EXEC81310_step_results
		List<Document> stepResults = mongoTemplate.find(query, Document.class, licId + "_" + execId + "_step_results");
		ArrayList<String> allResults = new ArrayList<>();
		Set<String> execIDs = new LinkedHashSet<>();
		Set<String> suiteIDs = new LinkedHashSet<>();
		for (Document doc : stepResults) {
			log.info(doc.toJson());
			allResults.add(JsonPath.read(doc.toJson(), "$.status"));
			execIDs.add(JsonPath.read(doc.toJson(), "$.executionId"));
			try {
				suiteIDs.add(JsonPath.read(doc.toJson(), "$.suiteId"));
			} catch (Exception e) {
				throw new ClientProtocolException("Its a script");
			}
		}
		mongoClient.close();
		log.info("allResults==> " + allResults);
		String scriptResult = null;
		if (allResults.contains("FAIL")) {
			scriptResult = "FAIL";
		} else if (allResults.contains("PASS") || allResults.contains("WARNING")) {
			scriptResult = "PASS";
		}
		List<String> returnList = new ArrayList<String>();
		returnList.add(scriptResult);
		returnList.add(execId);
		returnList.add(suiteIDs.toArray()[0].toString());
		log.info("getScriptDetails==> " + returnList);
		return returnList;
	}

	public static void pushResults(String scriptId, String scriptStatus, String execID, String suiteID,
			String tableName, String fireflinkEmailId, String password, String projectId) throws Exception {
		StringBuilder missingParams = new StringBuilder();
		if (scriptId == null || scriptId.isEmpty())
			missingParams.append("scriptId, ");
		if (scriptStatus == null || scriptStatus.isEmpty())
			missingParams.append("scriptStatus, ");
		if (execID == null || execID.isEmpty())
			missingParams.append("execID, ");
		if (suiteID == null || suiteID.isEmpty())
			missingParams.append("suiteID, ");
		if (tableName == null || tableName.isEmpty())
			missingParams.append("tableName, ");

		if (missingParams.length() > 0) {
			missingParams.setLength(missingParams.length() - 2);
			throw new InvalidParameterException("Missing or empty parameters: " + missingParams.toString());
		}
		baseURI = "https://sonicwall.fireflink.com/";
		String token = "";
		String suiteName = "";
		password = encryptPassword(password);
		String signInRequestPayload = "{ \"emailId\": \"" + fireflinkEmailId + "\", \"password\": \"" + password
				+ "\"}";
		String signInResponsePayload = postRequest("appmanagement/optimize/v1/public/user/client-signin",
				signInRequestPayload);

		String message = convertToString(getJsonValue(signInResponsePayload, "status"));

		// Provide message if EmailID/Password is Invalid
		if (message != null && !message.contains("SUCCESS")) {
			throw new InvalidParameterException(convertToString(getJsonValue(signInResponsePayload, "message")));
		}
		token = convertToString(getJsonValue(signInResponsePayload, "responseObject.access_token"));
		log.info("token" + token);

		String allSuiteDetails = getRequest(
				"alltrees/optimize/v3/tree/execution/suite?sourceId=" + suiteID + "&action=refresh", "Authorization",
				"Bearer " + token, "projectId", projectId);
		System.out.println(allSuiteDetails);
		suiteName = getJsonListStringValue(allSuiteDetails,
				"responseObject.treeDataList[?(@.key=='" + suiteID + "')].name");
		log.info(suiteName + "");
		createTableIfNotExistsAndUpdate(tableName, scriptId,
				Date.valueOf(java.time.LocalDate.now()
						.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()),
				scriptStatus, suiteName);
	}

	private static String getRequest(String endpoint, String... headers) {
		RequestSpecification request = given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.when().get(endpoint).then().assertThat().statusCode(200).extract().asString();
	}

	public static String getJsonListStringValue(String json, String jsonPath) {
		List<Integer> ids = JsonPath.read(json, "$." + jsonPath);
		return ids.isEmpty() ? null : String.valueOf(ids.get(0));
	}

	public static Object getJsonValue(String jsonBody, String jsonPath) {
		return JsonPath.read(jsonBody, "$." + jsonPath);
	}

	public static String convertToString(Object obj) {
		return (String) obj;
	}

	private static String postRequest(String endpoint, String requestBody, String... headers) {
		RequestSpecification request = given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		return request.body(requestBody).contentType(ContentType.JSON).when().post(endpoint).then().assertThat()
				.statusCode(200).extract().asString();
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

	public static void createTableIfNotExistsAndUpdate(String tableName, String scriptId, Date executedDate,
			String scriptStatus, String suiteName) throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Statement stmt = null;

		try {

			con = DriverManager.getConnection(URL, USER, PASSWORD);

			// -------------------------------------------------------
			// Check if table exists
			// -------------------------------------------------------

			String checkTable = "SELECT COUNT(*) FROM information_schema.tables " + "WHERE table_schema = DATABASE() "
					+ "AND table_name = ?";

			ps = con.prepareStatement(checkTable);
			ps.setString(1, tableName);

			rs = ps.executeQuery();

			boolean tableExists = false;

			if (rs.next()) {
				tableExists = rs.getInt(1) > 0;
			}

			rs.close();
			ps.close();

			// -------------------------------------------------------
			// Create table if it doesn't exist
			// -------------------------------------------------------

			if (!tableExists) {

				stmt = con.createStatement();

				// Copy structure
				stmt.executeUpdate("CREATE TABLE `" + tableName + "` LIKE MasterExecution");

				// Copy data
				stmt.executeUpdate("INSERT INTO `" + tableName + "` SELECT * FROM MasterExecution");

				stmt.close();

				System.out.println("Table created : " + tableName);

			} else {

				System.out.println("Table already exists : " + tableName);
			}

			// -------------------------------------------------------
			// Update data
			// -------------------------------------------------------

			String update = "UPDATE `" + tableName + "` " + "SET ExecutedDate=?, SuiteName=?, ScriptStatus=? "
					+ "WHERE ScriptID=?";

			ps = con.prepareStatement(update);

			ps.setDate(1, executedDate);
			ps.setString(2, suiteName);
			ps.setString(3, scriptStatus);
			ps.setString(4, scriptId);

			int rows = ps.executeUpdate();
			if (rows == 0) {
				throw new IllegalArgumentException(
						"No record exists with ScriptID '" + scriptId + "' in table '" + tableName + "'.");
			}
			System.out.println(rows + " row(s) updated.");

			ps.close();
			con.close();

		} catch (Exception e) {

			if (rs != null)
				rs.close();

			if (ps != null)
				ps.close();

			if (stmt != null)
				stmt.close();

			if (con != null)
				con.close();

			throw e;
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Table Name", "Sanity_2");
		model.getAttributes().put("FireflinkMailId", "asdsd");
		model.getAttributes().put("FireflinkPassword", "asddsa");
		log.info(new UpdateScriptResult_DB().execute(model) + "");
	}
}