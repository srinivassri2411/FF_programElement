package generic;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP670a3a25-490b-4c33-bb32-c3e63827fd19")
public class GetTheTextFromImageAPI implements Nlp {
	@InputParams({ @InputParam(name = "Image Path", type = "java.lang.String") })
	@ReturnType(name = "Result", type = "java.lang.String")

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
		String path = (String) attributes.get("Image Path");
		String Return = null;
		try {
			RestAssured.baseURI = "https://api8.ocr.space/parse/image";
			File imageFile = new File(path);
			Response response = given().header("apikey", "donotstealthiskey_ip1").multiPart("file", imageFile)
					.formParam("language", "eng").formParam("isOverlayRequired", "true").formParam("FileType", ".Auto")
					.formParam("IsCreateSearchablePDF", "false").formParam("isSearchablePdfHideTextLayer", "true")
					.formParam("detectOrientation", "false").formParam("isTable", "false").formParam("scale", "true")
					.formParam("OCREngine", "5").formParam("detectCheckbox", "false").formParam("checkboxTemplate", "0")
					.when().post().then().extract().response();
			System.out.println("Response Status Code: " + response.getStatusCode());
			if (response.getStatusCode() <= 203) {
				try {
					Return = JsonPath.from(response.getBody().asString()).get("ParsedResults[0].ParsedText");
					nlpResponseModel.setMessage("Successfully fetched the text from image");
					nlpResponseModel.setStatus(CommonConstants.pass);
				} catch (IllegalArgumentException | JsonPathException e) {
					nlpResponseModel.setMessage("Failed to fetch " + e.getMessage());
					nlpResponseModel.setStatus(CommonConstants.fail);
				}
			} else {
				nlpResponseModel.setMessage("Failed to fetch " + response.getBody().asString());
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setMessage("Failed to set ==> " + sw.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Result", Return);
		return nlpResponseModel;
	}

}