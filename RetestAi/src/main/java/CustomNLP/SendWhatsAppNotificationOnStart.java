package CustomNLP;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import io.appium.java_client.android.AndroidDriver;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class SendWhatsAppNotificationOnStart implements Nlp {

    private static final String BASE_URL = "https://fireflink.tataplay.com";

    @InputParams({
            @InputParam(name = "Group Name", type = "java.lang.String"),
            @InputParam(name = "Username", type = "java.lang.String"),
            @InputParam(name = "Password", type = "java.lang.String")
    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel responseModel = new NlpResponseModel();

        try {

            Map<String, Object> inputs = nlpRequestModel.getAttributes();

            String groupName = (String) inputs.get("Group Name");
            String username = (String) inputs.get("Username");
            String password = (String) inputs.get("Password");

            Map<String, String> identifiers = nlpRequestModel.getExecutionIdentifiers();

            String suiteId = identifiers.get("suiteId");
            String projectId = identifiers.get("projectId");
            String licId = identifiers.get("licenseId");

            System.out.println("suiteId = " + suiteId);
            System.out.println("projectId = " + projectId);
            System.out.println("licenseId = " + licId);

            if (suiteId == null || projectId == null || licId == null) {
                throw new RuntimeException("Required execution identifiers missing");
            }

            RestAssured.baseURI = BASE_URL;

            // Fetch token
            String token = fetchToken(licId, username, password);

            if (token == null) {
                throw new RuntimeException("Token generation failed");
            }

            // Call Suite API
            RequestSpecification request = given()
                    .header("Authorization", "Bearer " + token)
                    .header("projectId", projectId)
                    .header("licenseId", licId);

            Response suiteResponse = request.get(
                    "/alltrees/optimize/v3/tree/suite/section/suite"
                            + "?sourceId=" + suiteId
                            + "&action=start"
            );

            System.out.println("Suite API Response: " + suiteResponse.asString());

            String suiteName = suiteResponse
                    .jsonPath()
                    .getString("responseObject.treeDataList[0].name");

            if (suiteName == null) {
                throw new RuntimeException("Suite name not found");
            }

            System.out.println("Suite Name: " + suiteName);

            // -----------------------------
            // Mobile WhatsApp Interaction
            // -----------------------------

             AndroidDriver driver = nlpRequestModel.getAndroidDriver();
       //     responseModel.setWebDriver(driver);

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            String message = suiteName + " suite execution has started";

            // Tap search icon
            driver.findElement(By.id("com.whatsapp:id/search_bar_inner_layout")).click();

            // Enter group name
            driver.findElement(By.id("com.whatsapp:id/search_input"))
                    .sendKeys(groupName);

            Thread.sleep(2000);

            // Select group
            driver.findElement(By.xpath("//android.widget.RelativeLayout[@package='com.whatsapp']")).click();

            // Enter message
            driver.findElement(By.id("com.whatsapp:id/entry"))
                    .sendKeys(message);

            // Send message
            driver.findElement(By.id("com.whatsapp:id/send")).click();

            responseModel.setStatus(CommonConstants.pass);
            driver.terminateApp("com.whatsapp");

        }

        catch (Exception e) {

            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();

            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage(e.getMessage());

            throw new NlpException(e.getMessage());
        }
       
        return responseModel;
        
    }

    // -----------------------------
    // Sign-In API to Fetch Token
    // -----------------------------

    public static String fetchToken(String licId, String emailId, String password) throws Exception {

        String payload = "{"
                + "\"currentLicenseId\":\"" + licId + "\","
                + "\"emailId\":\"" + emailId + "\","
                + "\"password\":\"" + encryptPasswordV3(password) + "\","
                + "\"isSecureProtocol\":true"
                + "}";

        Response login = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/appmanagement/optimize/v1/public/user/signin");

        System.out.println("Login Response: " + login.asString());
        System.out.println("Payload = " + payload);
        System.out.println("Login Response = " + login.asString());

        if (!"200".equals(login.jsonPath().getString("responseCode"))) {
            throw new RuntimeException("Invalid credentials: " + login.asString());
        }

        return login.jsonPath().getString("responseObject.access_token");
    }

    // -----------------------------
    // Password Encryption Method
    // -----------------------------

    public static String encryptPasswordV3(String password) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB"
        );

        PublicKey key = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

        cipher.init(Cipher.ENCRYPT_MODE, key,
                new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA256,
                        PSource.PSpecified.DEFAULT
                ));

        return Base64.getEncoder().encodeToString(
                cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
    }
}