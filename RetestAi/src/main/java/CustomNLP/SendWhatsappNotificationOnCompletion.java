package CustomNLP;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.openqa.selenium.By;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import io.appium.java_client.android.AndroidDriver;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class SendWhatsappNotificationOnCompletion implements Nlp {

    private static String token;
    private static String projectId;
    private static String projectType;
    private static String suiteID;
    private static String executionId;

    public static String baseUrl = "https://fireflink.tataplay.com";
    public static String username = "";
    public static String password = "";
    public static String licenseName = "fire-flink-LIC4900";
    public static String projectName = "TataPlay";

    private static String WHATSAPP_CONTACT = "";

    // Maps to store failure details
    private static Map<String, String> variableMap = new LinkedHashMap<String, String>();
    private static List<String> sourceList = new ArrayList<String>();
    private static String module = "";
    private static String scriptName = "";
    private static String executedDate = "";
    private static String executedBy = "";
    private static String variableData = "";
    
    // Maps to store failed step details
    private static Map<String, Map<String, String>> failedStepDetailsMap = new LinkedHashMap<>();
    private static boolean exitFlagForFailedStep = false;
    private static String failedStepId = "";
    
    // Lists to collect behaviour data
    private static List<String> expectedBehaviourList = new ArrayList<String>();
    private static List<String> actualBehaviourList = new ArrayList<String>();

    @InputParams({
            @InputParam(name = "Suite ID", type = "java.lang.String"),
            @InputParam(name = "FF UN", type = "java.lang.String"),
            @InputParam(name = "FF PWD", type = "java.lang.String"),
            @InputParam(name = "WhatsApp Group Name", type = "java.lang.String"),
            @InputParam(name = "Variables Data", type = "java.lang.String")
    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel responseModel = new NlpResponseModel();

        try {
            Map<String, Object> inputs = nlpRequestModel.getAttributes();

            suiteID = (String) inputs.get("Suite ID");
            username = (String) inputs.get("FF UN");
            password = (String) inputs.get("FF PWD");
            WHATSAPP_CONTACT = (String) inputs.get("WhatsApp Group Name");
            variableData = (String) inputs.get("Variables Data");

            Map<String, String> ids = nlpRequestModel.getExecutionIdentifiers();
            projectId = ids.get("projectId");

            String whatsappMessage = mainMethod();

            // Send WhatsApp message
            AndroidDriver driver = nlpRequestModel.getAndroidDriver();
            if (driver != null && WHATSAPP_CONTACT != null && !WHATSAPP_CONTACT.isEmpty()) {
                sendWhatsAppMessage(driver, whatsappMessage);
            }

            responseModel.setStatus(CommonConstants.pass);
            responseModel.setMessage(whatsappMessage);

        } catch (Exception e) {
            e.printStackTrace();
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage(e.getMessage());
            throw new NlpException(e.getMessage());
        }

        return responseModel;
    }

    public static String mainMethod() {
        RestAssured.baseURI = baseUrl;

        try {
            System.out.println("Authenticating...");
            String encryptedPassword = encryptPasswordV3(password);
            String signInPayload = "{ \"emailId\": \"" + username + "\", \"password\": \"" + encryptedPassword + "\"}";
            String signInResponse = postRequest("appmanagement/optimize/v1/public/user/signin", signInPayload);

            token = convertToString(getJsonValue(signInResponse, "responseObject.access_token"));
            String userId = convertToString(getJsonValue(signInResponse, "responseObject.id"));

            // Select license and project
            selectLicenseAndProject(userId);

            // Get execution ID
            executionId = fetchExecutionKey(suiteID, projectId, token);
            System.out.println("Execution ID: " + executionId);

            // Check suite status
            String status = getSuiteExecutionStatus(executionId);
            System.out.println("Suite execution status: " + status);

            // Store variables if provided
            if (variableData != null && !variableData.isEmpty()) {
                storeVariableDataToList(token);
            }

            String whatsappMessage;

            if ("PASSED".equals(status)) {
                whatsappMessage = "✅ *Suite Execution PASSED*\n\n" +
                                 "Suite ID: " + suiteID + "\n" +
                                 "Execution ID: " + executionId + "\n" +
                                 "Timestamp: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            } 
            else if ("FAILED".equals(status)) {
                whatsappMessage = getDetailedFailureReport(executionId);
            } 
            else if ("TERMINATED".equals(status)) {
                whatsappMessage = "⚠️ *Suite Execution TERMINATED*\n\n" +
                                 "Suite ID: " + suiteID + "\n" +
                                 "Execution ID: " + executionId + "\n" +
                                 "Timestamp: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            } 
            else {
                whatsappMessage = "❓ *Suite Execution Status: " + status + "*\n\n" +
                                 "Suite ID: " + suiteID + "\n" +
                                 "Execution ID: " + executionId;
            }

            return whatsappMessage;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error fetching suite execution status: " + e.getMessage();
        }
    }

    // ==================== fetchExecutionKey Method ====================

    public static String fetchExecutionKey(String suiteId, String projectId, String token1) {
        RestAssured.baseURI = baseUrl;
        
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token1)
                .header("projectId", projectId)
                .queryParam("sourceId", suiteId)
                .queryParam("action", "expand")
                .get("/alltrees/optimize/v3/tree/execution/suite");
        
        List<Map<String, Object>> treeDataList = response.jsonPath().getList("responseObject.treeDataList");
        
        if (treeDataList == null || treeDataList.isEmpty()) {
            throw new RuntimeException("No tree data found for suite: " + suiteId);
        }
        
        // First try to find Execution node with EXEC prefix
        for (Map<String, Object> node : treeDataList) {
            String entityType = (String) node.get("entityType");
            String key = (String) node.get("key");
            
            if ("Execution".equals(entityType) && key != null && key.startsWith("EXEC")) {
                System.out.println("Found Execution node with key: " + key);
                return key;
            }
        }
        
        // If not found, try any Execution node
        for (Map<String, Object> node : treeDataList) {
            String entityType = (String) node.get("entityType");
            String key = (String) node.get("key");
            
            if ("Execution".equals(entityType) && key != null) {
                System.out.println("Found Execution node with key (without EXEC prefix): " + key);
                return key;
            }
        }
        
        throw new RuntimeException("Execution ID not found for suite: " + suiteId);
    }

    // ==================== WhatsApp Sending Method ====================

    private static void sendWhatsAppMessage(AndroidDriver driver, String message) throws Exception {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        driver.findElement(By.id("com.whatsapp:id/search_bar_inner_layout")).click();
        driver.findElement(By.id("com.whatsapp:id/search_input")).sendKeys(WHATSAPP_CONTACT);
        Thread.sleep(2000);

        driver.findElement(By.xpath("//android.widget.TextView[@text='" + WHATSAPP_CONTACT + "']")).click();

        driver.findElement(By.id("com.whatsapp:id/entry")).sendKeys(message);
        driver.findElement(By.id("com.whatsapp:id/send")).click();

        driver.terminateApp("com.whatsapp");
    }

    // ==================== Suite Status Check Method ====================

    private static String getSuiteExecutionStatus(String executionId) {
        try {
            String treeResponse = getRequest("alltrees/optimize/v3/tree/execution/suite?sourceId=" + executionId + "&action=expandAll");
            
            int treeResponseCode = convertToInteger(getJsonValue(treeResponse, "responseCode"));
            if (treeResponseCode != 200) {
                System.err.println("Failed to fetch execution tree. Response code: " + treeResponseCode);
                return "UNKNOWN";
            }

            List<Map<String, Object>> treeDataList = (List<Map<String, Object>>) getJsonValue(treeResponse, "responseObject.treeDataList");
            
            if (treeDataList == null || treeDataList.isEmpty()) {
                return "UNKNOWN";
            }

            boolean hasFailed = false;
            boolean hasTerminated = false;

            for (Map<String, Object> node : treeDataList) {
                if ("Script".equals(node.get("entityType"))) {
                    String scriptStatus = (String) node.get("status");
                    
                    if ("TERMINATED".equals(scriptStatus)) {
                        hasTerminated = true;
                    } else if ("FAIL".equals(scriptStatus)) {
                        hasFailed = true;
                    }
                }
            }

            if (hasTerminated) {
                return "TERMINATED";
            } else if (hasFailed) {
                return "FAILED";
            } else {
                boolean hasScripts = false;
                for (Map<String, Object> node : treeDataList) {
                    if ("Script".equals(node.get("entityType"))) {
                        hasScripts = true;
                        break;
                    }
                }
                
                if (!hasScripts) {
                    return "UNKNOWN";
                }
                return "PASSED";
            }

        } catch (Exception e) {
            System.err.println("Error in getSuiteExecutionStatus: " + e.getMessage());
            e.printStackTrace();
            return "UNKNOWN";
        }
    }

    // ==================== FIXED: Process steps to collect source like working code ====================
    
    private static void processSteps(List<Map<String, Object>> stepResults) {
        for (Map<String, Object> step : stepResults) {
            String stepType = (String) step.get("type");
            
            if ("Group".equals(stepType) || "ForLoop".equals(stepType) || "StepGroup".equals(stepType)) {
                List<Map<String, Object>> childSteps = (List<Map<String, Object>>) step.get("stepResults");
                if (childSteps != null) {
                    processSteps(childSteps);
                }
            } else if ("step".equals(stepType) || stepType == null) {
                String stepStatus = (String) step.get("status");
                if (stepStatus != null) {
                    // Add to status list if needed
                }
                
                // --- FIXED: Collect sources from stepInputs (this is the key part from working code) ---
                List<Map<String, Object>> stepInputs = (List<Map<String, Object>>) step.get("stepInputs");
                if (stepInputs != null) {
                    for (Map<String, Object> input : stepInputs) {
                        String reference = (String) input.get("reference");
                        if (reference != null && (reference.equals("LOCAL") || reference.equals("GLOBAL") || reference.equals("PROJECT_ENVIRONMENT"))) {
                            String value = (String) input.get("value");
                            if (value != null && (value.startsWith("URL_") || value.startsWith("APP_"))) {
                                // Extract source by splitting at underscore and taking second part
                                String source = value.split("_")[1];
                                if (!sourceList.contains(source)) {
                                    sourceList.add(source);
                                    System.out.println("✅ Collected source: " + source + " from value: " + value);
                                }
                            }
                        }
                    }
                }
                
                // Check stepReturn for expected/actual behaviour
                Map<String, Object> stepReturn = (Map<String, Object>) step.get("stepReturn");
                if (stepReturn != null) {
                    String stepName = (String) step.get("name");
                    if ("Generate_Expected_Behaviour".equals(stepName)) {
                        String value = (String) stepReturn.get("value");
                        if (value != null) {
                            expectedBehaviourList.add(value);
                        }
                    } else if ("Generate_Actual_Behaviour".equals(stepName)) {
                        String value = (String) stepReturn.get("value");
                        if (value != null) {
                            actualBehaviourList.add(value);
                            // Associate this actual behaviour with the failed step
                            String previousFailedStepPath = (String) step.get("previousFailedStep");
                            if (previousFailedStepPath != null && !previousFailedStepPath.isEmpty()) {
                                String prevFailedId = previousFailedStepPath.substring(previousFailedStepPath.lastIndexOf('/') + 1);
                                Map<String, String> failedStepDetails = failedStepDetailsMap.get(prevFailedId);
                                if (failedStepDetails != null) {
                                    failedStepDetails.put("actualBehaviour", value);
                                }
                            }
                        }
                    }
                }
                
                // Capture failed step
                if ("FAIL".equals(stepStatus) && !exitFlagForFailedStep) {
                    failedStepId = (String) step.get("uniqueId");
                    exitFlagForFailedStep = true;
                    
                    // Get step name
                    String failedStepName = (String) step.get("name");
                    if (failedStepName == null) {
                        failedStepName = (String) step.get("title");
                    }
                    if (failedStepName == null) {
                        failedStepName = "Unknown Step";
                    }
                    
                    // Store step details
                    Map<String, String> details = new LinkedHashMap<>();
                    details.put("name", failedStepName);
                    details.put("screenshotPath", (String) step.get("screenshotPath"));
                    
                    failedStepDetailsMap.put(failedStepId, details);
                    System.out.println("📝 Captured failed step: " + failedStepName);
                }
            }
        }
    }

    // ==================== FIXED: Get source from sourceList like working code ====================

    private static String getSourceFromList() {
        if (!sourceList.isEmpty()) {
            return sourceList.get(0);
        }
        return "NA";
    }

    // ==================== FIXED: Detailed Failure Report Method ====================

    private static String getDetailedFailureReport(String executionId) {
        try {
            resetCollections();

            String treeResponse = getRequest("alltrees/optimize/v3/tree/execution/suite?sourceId=" + executionId + "&action=expandAll");
            List<Map<String, Object>> treeDataList = (List<Map<String, Object>>) getJsonValue(treeResponse, "responseObject.treeDataList");

            Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
            for (Map<String, Object> node : treeDataList) {
                nodeMap.put((String) node.get("key"), node);
            }

            for (Map<String, Object> node : treeDataList) {
                if ("Execution".equals(node.get("entityType"))) {
                    executedBy = (String) node.get("runInitiatedBy");
                    break;
                }
            }

            List<Map<String, Object>> failedScripts = new ArrayList<>();
            for (Map<String, Object> node : treeDataList) {
                if ("Script".equals(node.get("entityType")) && "FAIL".equals(node.get("status"))) {
                    failedScripts.add(node);
                }
            }

            if (failedScripts.isEmpty()) {
                return "❌ *Suite Execution FAILED*\n\nNo failed script details found.";
            }

            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder.append("❌ *Suite Execution FAILED*\n\n");
            reportBuilder.append("📋 *Failure Report*\n");
            reportBuilder.append("═══════════════════════════\n\n");

            int scriptCounter = 0;
            for (Map<String, Object> scriptNode : failedScripts) {
                scriptCounter++; 
                
                // Clear collections for this script
                sourceList.clear();
                expectedBehaviourList.clear();
                actualBehaviourList.clear();
                failedStepDetailsMap.clear();
                exitFlagForFailedStep = false;
                failedStepId = "";
                
                String scriptId = (String) scriptNode.get("originalId");
                String scriptDisplayName = (String) scriptNode.get("name");
                String runId = findRunId(scriptNode, nodeMap);

                if (runId == null) continue;

                String scriptResult = getRequest("executionresult/optimize/v3/execution-result/run/" + executionId + "/" + runId + "/" + scriptId);
                Map<String, Object> result = (Map<String, Object>) getJsonValue(scriptResult, "responseObject");

                scriptName = (String) result.get("title");
                if (scriptName == null) scriptName = scriptDisplayName;

                String path = (String) result.get("path");
                if (path != null) {
                    String[] parts = path.split("/");
                    module = parts.length > 1 ? parts[parts.length - 2] : "";
                }

                executedDate = (String) result.get("executedOn");
                if (executedDate == null) executedDate = "";

                String formattedDate = formatExecutedOn(executedDate);

                // Process steps to collect sources and find failures
                Map<String, Object> steps = (Map<String, Object>) result.get("steps");
                String failedStepMessage = "";
                String failedStepName = "";

                if (steps != null) {
                    List<Map<String, Object>> stepResults = (List<Map<String, Object>>) steps.get("stepResults");
                    if (stepResults != null) {
                        // Process all steps to collect sources and find failed step
                        processSteps(stepResults);
                        
                        // Get source from sourceList (like working code)
                        String source = getSourceFromList();
                        
                        // Get failed step details
                        if (!failedStepDetailsMap.isEmpty()) {
                            Map.Entry<String, Map<String, String>> firstEntry = failedStepDetailsMap.entrySet().iterator().next();
                            if (firstEntry.getValue() != null) {
                                failedStepName = firstEntry.getValue().get("name") != null ? 
                                                firstEntry.getValue().get("name") : "Unknown Step";
                                
                                // Get actual behaviour if available
                                if (!actualBehaviourList.isEmpty()) {
                                    failedStepMessage = actualBehaviourList.get(actualBehaviourList.size() - 1);
                                }
                            }
                        }
                        
                        System.out.println("For script " + scriptId + " - Source: " + source + 
                                           ", Failed step: " + failedStepName + 
                                           ", Message: " + failedStepMessage);

                        // Build report
                        reportBuilder.append("*Failure ").append(scriptCounter).append("*\n");
                        reportBuilder.append("─────────────────\n");
                        reportBuilder.append("*Source:* ").append(source).append("\n");
                        reportBuilder.append("*Module:* ").append(module).append("\n");
                        reportBuilder.append("*Scenario:* ").append(scriptId).append(" - ").append(scriptName).append("\n");
                        reportBuilder.append("*Exec ID:* ").append(executionId).append("\n");
                        reportBuilder.append("*Date/Time:* ").append(formattedDate).append("\n");
                        reportBuilder.append("*Executed By:* ").append(executedBy).append("\n");

                        // Add variables if available
                        if (!variableMap.isEmpty()) {
                            for (Map.Entry<String, String> var : variableMap.entrySet()) {
                                reportBuilder.append("*").append(var.getKey()).append(":* ").append(var.getValue()).append("\n");
                            }
                        }

                        if (!failedStepName.isEmpty()) {
                            reportBuilder.append("*Failed Step:* ").append(failedStepName).append("\n");
                        }

                        if (!failedStepMessage.isEmpty()) {
                            reportBuilder.append("*Issue:* ").append(failedStepMessage).append("\n\n");
                        } else {
                            reportBuilder.append("\n");
                        }
                    }
                }
            }

            return reportBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ *Suite Execution FAILED*\n\nError fetching details: " + e.getMessage();
        }
    }

    // ==================== Helper Methods ====================

    private static void selectLicenseAndProject(String userId) throws Exception {
        String licensesResponse = getRequest("appmanagement/optimize/v1/license/licenses");
        int licenseCount = convertToInteger(getJsonValue(licensesResponse, "responseObject.length()"));
        String licenseId = null;

        for (int i = 0; i < licenseCount; i++) {
            String name = convertToString(getJsonValue(licensesResponse, "responseObject[" + i + "].licenseName"));
            if (licenseName.equals(name)) {
                licenseId = convertToString(getJsonValue(licensesResponse, "responseObject[" + i + "].id"));
                break;
            }
        }

        if (licenseId == null) {
            throw new Exception("License not found: " + licenseName);
        }

        String selectLicensePayload = "{\"defaultLicenseId\":\"" + licenseId + "\"}";
        putRequest("appmanagement/optimize/v1/user/updateUserData/" + userId, selectLicensePayload);

        String projectsResponse = getRequest("project/optimize/v1/projects/user/" + userId);
        int projectCount = convertToInteger(getJsonValue(projectsResponse, "responseObject.length()"));

        for (int i = 0; i < projectCount; i++) {
            String name = convertToString(getJsonValue(projectsResponse, "responseObject[" + i + "].project.name"));
            if (projectName.equals(name)) {
                projectId = convertToString(getJsonValue(projectsResponse, "responseObject[" + i + "].project.id"));
                projectType = convertToString(getJsonValue(projectsResponse, "responseObject[" + i + "].project.type"));
                break;
            }
        }

        if (projectId == null) {
            throw new Exception("Project not found: " + projectName);
        }
    }

    private static void storeVariableDataToList(String token) {
        if (variableData == null || variableData.isEmpty()) return;

        String[] variables = variableData.split(",");
        for (String variable : variables) {
            String[] parts = variable.split("##");
            if (parts.length == 2) {
                String variableType = parts[0].trim();
                String variableName = parts[1].trim();

                try {
                    if (variableType.equals("GV")) {
                        String response = getRequest("project/optimize/v1/variable/type/GLOBAL");
                        List<Map<String, Object>> variablesList = (List<Map<String, Object>>) getJsonValue(response, "responseObject");

                        for (Map<String, Object> varMap : variablesList) {
                            if (variableName.equals(varMap.get("name"))) {
                                variableMap.put(variableName, (String) varMap.get("value"));
                                break;
                            }
                        }
                    } else if (variableType.equals("PEV")) {
                        String response = getRequest("project/optimize/v1/variable/type/PROJECT_ENVIRONMENT");
                        List<Map<String, Object>> variablesList = (List<Map<String, Object>>) getJsonValue(response, "responseObject");

                        for (Map<String, Object> varMap : variablesList) {
                            if (variableName.equals(varMap.get("name"))) {
                                variableMap.put(variableName, (String) varMap.get("value"));
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching variable: " + variableName);
                }
            }
        }
    }

    private static void resetCollections() {
        sourceList.clear();
        expectedBehaviourList.clear();
        actualBehaviourList.clear();
        failedStepDetailsMap.clear();
        module = "";
        scriptName = "";
        exitFlagForFailedStep = false;
        failedStepId = "";
    }

    private static String encryptPasswordV3(String password) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyf80Hbn+1zWq5VzKynlEQy6EzsrbsEcB3EuppQzpQZGnmonioeTcpwb4A93f8pNADXkX2EA62D4EoqXH5ypmq4QOMj2qJb19MO44Sl3V5akwZH1fL8PCq05jHD34a8bcsxeUyk9WlJNla79cUFOombhdBN18hg/F4iDsJFbRjHIqac4nDAWtxpyBnf2kBEEbwUV4SJ9eOtz/BGGzKnowveLHvYQXsyd1iaeFhfYIITIrSOibvNqEWs/J9Bz+EepJGaneTP7cK1HciitGYjb6pv4ZJvDhK4RARM03AjRtURBRwJHPl4yOuORCQ85G5eH4sgpvty+KGSEKY7HhK0NB3wIDAQAB");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);

        byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String getRequest(String endpoint, String... headers) {
        RequestSpecification request = RestAssured.given();

        if (token != null && !endpoint.contains("signin")) {
            request.header("Authorization", "Bearer " + token);
        }
        if (projectId != null) {
            request.header("projectId", projectId);
        }
        if (projectType != null && !projectType.isEmpty()) {
            request.header("projectType", projectType);
        }

        return request.get(endpoint).asString();
    }

    private static String postRequest(String endpoint, String body, String... headers) {
        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body);

        if (token != null && !endpoint.contains("signin")) {
            request.header("Authorization", "Bearer " + token);
        }
        if (projectId != null) {
            request.header("projectId", projectId);
        }

        return request.post(endpoint).asString();
    }

    private static String putRequest(String endpoint, String body, String... headers) {
        RequestSpecification request = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body);

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (projectId != null) {
            request.header("projectId", projectId);
        }

        return request.put(endpoint).asString();
    }

    private static Object getJsonValue(String json, String path) {
        return JsonPath.read(json, "$." + path, new Predicate[0]);
    }

    private static String convertToString(Object obj) {
        return (String) obj;
    }

    private static int convertToInteger(Object obj) {
        return (Integer) obj;
    }

    private static int convertToInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return (int) Double.parseDouble((String) value);
            }
        }
        return 0;
    }

    private static String findRunId(Map<String, Object> node, Map<String, Map<String, Object>> nodeMap) {
        String currentKey = (String) node.get("parentId");
        while (currentKey != null) {
            Map<String, Object> parent = nodeMap.get(currentKey);
            if (parent == null) break;
            if ("Run".equals(parent.get("entityType"))) {
                return (String) parent.get("runId");
            }
            currentKey = (String) parent.get("parentId");
        }
        return null;
    }

    private static String formatExecutedOn(String executedOn) {
        if (executedOn == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            Date date = inputFormat.parse(executedOn);
            return outputFormat.format(date);
        } catch (Exception e) {
            return executedOn;
        }
    }
}