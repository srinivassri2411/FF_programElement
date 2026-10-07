
package BusinessLogics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CreateFile implements Nlp {
	@InputParams({ @InputParam(name = "Status Code", type = "java.lang.String"),
			@InputParam(name = "Request Body", type = "java.lang.String"),
			@InputParam(name = "Response Body", type = "java.lang.String"),
			@InputParam(name = "Response Body", type = "java.lang.String"),
			@InputParam(name = "Map Inputs", type = "java.util.Map") })
	@ReturnType(name = "returnMap", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String statusCode = (String) programElementsInput.get("Status Code");
		String requestBody = (String) programElementsInput.get("Request Body");
		String responseBody = (String) programElementsInput.get("Response Body");
		Map mapInput = (Map) programElementsInput.get("Map Inputs");
		LinkedHashMap returnMap = new LinkedHashMap<>();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (statusCode.equals("200")) {
				returnMap = storelabelPDf(requestBody, responseBody, mapInput);
			} else {
				returnMap = createTestNameAndJsonFailed(requestBody, responseBody, mapInput);
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		nlpResponseModel.getAttributes().put("returnMap", returnMap);

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	private static LinkedHashMap storelabelPDf(String requestBody, String responseBody, Map mapInputs)
			throws IOException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
		LocalDateTime now = LocalDateTime.now();
		String formattedDateTime = now.format(formatter);
		String testName = mapInputs.get("testName").toString();
		String testNameWithDate = testName + "_" + formattedDateTime;

		DocumentContext responseBodyObject = JsonPath.parse(responseBody);

		String userHome = System.getProperty("user.home");

		String testRequest = testName + "Request" + "_" + formattedDateTime + ".json";
		String testRequestFile = userHome + File.separator + testRequest;
		File testRequestFilePath = new File(testRequestFile);
		testRequestFilePath.createNewFile();
		FileWriter fw = new FileWriter(testRequestFilePath);
		fw.write(requestBody);
		fw.close();

		String testResponse = testName + "Response" + "_" + formattedDateTime + ".json";
		String testResponseFile = userHome + File.separator + testResponse;
		File testResponseFilePath = new File(testResponseFile);
		testResponseFilePath.createNewFile();
		fw = new FileWriter(testResponseFilePath);
		fw.write(responseBody);
		fw.close();

		String testNameTxt = testName + "Label" + formattedDateTime;
		String testNameTxtFile = userHome + File.separator + testNameTxt + ".txt";
		File testNameTxtFilePath = new File(testNameTxtFile);
		testNameTxtFilePath.createNewFile();
		fw = new FileWriter(testNameTxtFilePath);
		String Labels = mapInputs.get("Labels").toString();
		fw.write(Labels);
		fw.close();

		String commandtoDecode = "certutil -decode \"" + testNameTxtFilePath.getAbsolutePath() + "\" ";
		String labelsDirectory = mapInputs.get("labelsDirectory").toString();
		String concatpdf = "^ \"" + labelsDirectory + "\\" + testNameTxt + ".pdf";
		commandtoDecode = commandtoDecode + concatpdf + "\"";
		Runtime.getRuntime().exec(commandtoDecode);

//		String testNamePdf = testName + "Label" + formattedDateTime + ".pdf";
		String expShipmentAction = mapInputs.get("expShipmentAction").toString();
		LinkedHashMap returnMap = new LinkedHashMap<>();
		if (expShipmentAction.equalsIgnoreCase("Create")) {
			returnMap.put("actShipmentId",
					((List) responseBodyObject.read("$.Packages[0].ShipmentId")).get(0).toString());
		} else if (expShipmentAction.equalsIgnoreCase("Allocate")) {
			returnMap.put("actTrackingNumber",
					((List) responseBodyObject.read("$.Packages[0].TrackingNumber")).get(0).toString());
		}
		return returnMap;
	}

	private static LinkedHashMap createTestNameAndJsonFailed(String requestBody, String responseBody, Map mapInputs)
			throws IOException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
		LocalDateTime now = LocalDateTime.now();
		String formattedDateTime = now.format(formatter);
		String testName = mapInputs.get("testName").toString();
		String testNameWithDate = testName + "_" + formattedDateTime;
		String userHome = System.getProperty("user.home");
		String testCaseNameDirectory = userHome + File.separator + testNameWithDate;
		new File(testCaseNameDirectory).mkdir();
		String testRequest = testName + "Request" + "_" + formattedDateTime + ".json";
		String testRequestFile = userHome + File.separator + testRequest;
		File testRequestFilePath = new File(testRequestFile);
		testRequestFilePath.createNewFile();
		FileWriter fw = new FileWriter(testRequestFilePath);
		fw.write(requestBody);
		fw.close();
		String testResponse = testName + "Response" + "_" + formattedDateTime + ".json";
		String testResponseFile = userHome + File.separator + testResponse;
		File testResponseFilePath = new File(testResponseFile);
		testResponseFilePath.createNewFile();
		fw = new FileWriter(testResponseFilePath);
		fw.write(responseBody);
		fw.close();
		String expErrorCode = mapInputs.get("expErrorCode").toString();
		String expErrorMessage = mapInputs.get("expErrorMessage").toString();
		LinkedHashMap returnMap = new LinkedHashMap<>();
		if (expErrorCode != "") {
			returnMap.put("expErrorCode", responseBody.contains(expErrorCode));
			returnMap.put("expErrorMessage", responseBody.contains(expErrorMessage));
		}
		return returnMap;
	}

	public static void main(String[] args) {
//		createTestNameAndJsonFailed("");
	}
}