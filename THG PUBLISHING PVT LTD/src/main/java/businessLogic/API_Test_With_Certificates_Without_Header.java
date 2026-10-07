package businessLogic;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class API_Test_With_Certificates_Without_Header implements Nlp {
	@InputParams({ @InputParam(name = "baseURL", type = "java.lang.String"),
			@InputParam(name = "endPoint", type = "java.lang.String"),
			@InputParam(name = "pfxFilePath", type = "java.lang.String"), })
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
		Map<String, Object> map = null;
		try {
			map = getResponse(baseURL, endPoint, pfxFilePath);
			// String responseBody = map.get("ResponseBody").toString();
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
				// nlpResponseModel.setMessage("Failed to get API Response for given Request ["
				// + responseBody+"]");
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

	public static Map<String, Object> getResponse(String baseURL, String endPoint, String pfxFilePath)
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
			response = httpClient.execute(httpGet);
			long endTime = System.currentTimeMillis();

			map.put("StatusCode", response.getStatusLine().getStatusCode());
			map.put("StatusMessage", response.getStatusLine().getReasonPhrase());
			map.put("ResponseTime", endTime - startTime);
			map.put("RequestHeaders", Arrays.asList(httpGet.getAllHeaders()));
			map.put("ResponseHeaders", Arrays.asList(response.getAllHeaders()));
			map.put("ResponseBody", EntityUtils.toString(response.getEntity(),
			 StandardCharsets.UTF_8));
			map.put("Errors", "N/A");
			System.out.println(map);
			
		} catch (Exception e) {
			// TODO: handle exception
			map.put("StatusCode", "N/A");
			map.put("StatusMessage", "Error");
			map.put("ResponseTime", "N/A");
			map.put("RequestHeaders", "N/A");
			map.put("ResponseHeaders", "N/A");
			 map.put("ResponseBody", "N/A");
			map.put("Errors", e.getLocalizedMessage());
			System.out.println(map);
		}

		return map;
	}
	
	// ==== NEW ADDED METHODS FOR GENERIC JSON FIELD EXTRACTION ====
	
	/**
	 * Extract multiple fields from JSON string by given dot notation paths.
	 * @param jsonString full JSON response string
	 * @param fieldsToExtract list of JSON field paths (e.g. "publishedDate", "articles[0].publishedDate")
	 * @return Map with field path as key and extracted value as Object (String or JSON string)
	 * @throws Exception
	 */
	public static Map<String, Object> extractFieldsFromJson(String jsonString, List<String> fieldsToExtract) throws Exception {
	    Map<String, Object> extractedValues = new LinkedHashMap<>();
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree(jsonString);

	    for (String path : fieldsToExtract) {
	        Object value = getValueByPath(rootNode, path);
	        extractedValues.put(path, value);
	    }
	    return extractedValues;
	}

	/**
	 * Helper method to extract value from JsonNode by path with dot notation and array indexing.
	 * Supports paths like "articles[0].publishedDate"
	 * @param node JsonNode root or current node
	 * @param path dot notation path with optional array indexes
	 * @return extracted value as String or JSON string; null if not found
	 */
	private static Object getValueByPath(JsonNode node, String path) {
	    String[] parts = path.split("\\.");
	    JsonNode currentNode = node;

	    for (String part : parts) {
	        if (currentNode == null) {
	            return null;
	        }
	        // Check for array index like articles[0]
	        Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)]");
	        Matcher matcher = pattern.matcher(part);
	        if (matcher.matches()) {
	            String arrayName = matcher.group(1);
	            int index = Integer.parseInt(matcher.group(2));
	            currentNode = currentNode.path(arrayName);
	            if (currentNode.isArray() && currentNode.size() > index) {
	                currentNode = currentNode.get(index);
	            } else {
	                return null;
	            }
	        } else {
	            currentNode = currentNode.path(part);
	        }
	    }

	    if (currentNode == null || currentNode.isMissingNode() || currentNode.isNull()) {
	        return null;
	    } else if (currentNode.isValueNode()) {
	        return currentNode.asText();
	    } else {
	        return currentNode.toString(); // for objects or arrays, return JSON string
	    }
	}

	// ==== END NEW METHODS ====

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpRequestModel = new NlpRequestModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		attributes.put("baseURL", "https://www.thehindu.com/");
		attributes.put("endPoint", "/news/");
		attributes.put("pfxFilePath", "C:\\Users\\User\\Downloads\\theHindu.pfx");
		API_Test_With_Certificates_Without_Header api=new API_Test_With_Certificates_Without_Header();
		NlpResponseModel response = api.execute(nlpRequestModel);
		
		// Example usage of new methods to extract publishedDate dynamically
		try {
			@SuppressWarnings("unchecked")
			Map<String,Object> returnMap = (Map<String,Object>) response.getAttributes().get("Return Value");
			String responseBody = (String) returnMap.get("ResponseBody");
			
			List<String> fieldsToExtract = Arrays.asList("publishedDate");
			Map<String,Object> extracted = extractFieldsFromJson(responseBody, fieldsToExtract);
			System.out.println("Extracted Fields: " + extracted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
