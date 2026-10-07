package Business_Logics;

import java.util.HashMap;
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
import io.restassured.response.Response;

public class GenerateOAuth2Token implements Nlp {

    @InputParams({
            @InputParam(name = "Token URL", type = "java.lang.String"),
            @InputParam(name = "Client ID", type = "java.lang.String"),
            @InputParam(name = "Client Secret", type = "java.lang.String"),
            @InputParam(name = "Grant Type", type = "java.lang.String")
    })

    @ReturnType(name = "Access Token", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

        String tokenUrl = (String) programElementsInput.get("Token URL");
        String clientId = (String) programElementsInput.get("Client ID");
        String clientSecret = (String) programElementsInput.get("Client Secret");
        String grantType = (String) programElementsInput.get("Grant Type");

        String accessToken = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {

            Response response = RestAssured
                    .given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("grant_type", grantType)
                    .formParam("client_id", clientId)
                    .formParam("client_secret", clientSecret)
                    .post(tokenUrl);

            if (response.getStatusCode() == 200) {

                accessToken = response.jsonPath().getString("access_token");

                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel.setMessage(
                        "Successfully Fetched the Access Code"
                                + response.getStatusCode());

            } else {

                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage(
                        "Failed to generate token. Status Code: "
                                + response.getStatusCode());
            }

        } catch (Exception e) {

            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Exception occurred: " + e.getMessage());
        }

        nlpResponseModel.getAttributes().put("Access Token", accessToken);

        return nlpResponseModel;
    }
    public static void main(String[] args) {

        try {
            NlpRequestModel requestModel = new NlpRequestModel();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("Token URL", "https://your-auth-server.com/oauth/token");
            attributes.put("Client ID", "your-client-id");
            attributes.put("Client Secret", "your-client-secret");
            attributes.put("Grant Type", "client_credentials");

            requestModel.setAttributes(attributes);

            GenerateOAuth2Token obj = new GenerateOAuth2Token();
            NlpResponseModel response = obj.execute(requestModel);

            System.out.println("Status: " + response.getStatus());
            System.out.println("Message: " + response.getMessage());
            System.out.println("Access Token: "
                    + response.getAttributes().get("Access Token"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}