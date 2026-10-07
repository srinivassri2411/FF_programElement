package Custom_Map_Logic;


import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FetchApiDetailsForGivenURL implements Nlp {

    @InputParams({ @InputParam(name = "Api_Url", type = "java.lang.String") })
    @ReturnType(name = "listOfMap", type = "java.lang.List")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String filterUrl = (String) programElementsInput.get("Api_Url");
        WebDriver driver = nlpRequestModel.getWebDriver();
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
//            log.info("=== FetchApiDetailsForGivenURL START ===");
//            log.info("Filter URL received: {}", filterUrl);
//            log.info("WebDriver type: {}", driver.getClass().getName());

            List<Map<String, Object>> listOfMap = getApiDetails(driver, filterUrl);

//            log.info("Total maps returned: {}", listOfMap.size());
//            log.info("=== FetchApiDetailsForGivenURL END ===");

            nlpResponseModel.getAttributes().put("listOfMap", listOfMap);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Fetched Api Details for given URL. Total records: " + listOfMap.size());
        } catch (Exception e) {
        //    log.error("Exception in FetchApiDetailsForGivenURL: {}", e.getMessage());
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to fetch API details. Error: " + e.getMessage());
        }
        return nlpResponseModel;
    }

    public List<Map<String, Object>> getApiDetails(WebDriver driver, String filterUrl) {
        List<Map<String, Object>> listOfMap = new LinkedList<Map<String, Object>>();
        try {
//            log.info("=== getApiDetails START ===");
//            log.info("Available log types: {}", 
 //               driver.manage().logs().getAvailableLogTypes());

            List<LogEntry> entries = driver.manage().logs()
                .get(LogType.PERFORMANCE).getAll();
          //  log.info("Total performance log entries: {}", entries.size());

            if (entries.isEmpty()) {
              //  log.warn("WARNING: Performance logs are EMPTY!");
                return listOfMap;
            }

            for (LogEntry entry : entries) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                try {
                    String type = JsonPath.read(entry.getMessage(),
                        "$.message.method").toString();

                    if (type.equalsIgnoreCase("Network.responseReceived")) {
                        String url = JsonPath.read(entry.getMessage(),
                            "$.message.params.response.url");
                       // log.info("Response URL: {}", url);
                        if (url.contains(filterUrl)) {
                           // log.info("URL MATCHED for response: {}", url);
                            int statusCode = JsonPath.read(entry.getMessage(),
                                "$.message.params.response.status");
                            Object headers = JsonPath.read(entry.getMessage(),
                                "$.message.params.response.headers");
                            map.put("type", type);
                            map.put("url", url);
                            map.put("headers", headers);
                            map.put("statusCode", statusCode);
                        }
                    } else if (type.equalsIgnoreCase("Network.requestWillBeSent")) {
                        String url = JsonPath.read(entry.getMessage(),
                            "$.message.params.request.url");
                    //    log.info("Request URL: {}", url);
                        if (url.contains(filterUrl)) {
                      //      log.info("URL MATCHED for request: {}", url);
                            Object headers = JsonPath.read(entry.getMessage(),
                                "$.message.params.request.headers");
                            String httpMethod = JsonPath.read(entry.getMessage(),
                                "$.message.params.request.method");
                            map.put("type", type);
                            map.put("url", url);
                            map.put("method", httpMethod);
                            map.put("headers", headers);
                            if (httpMethod.equalsIgnoreCase("POST") ||
                                httpMethod.equalsIgnoreCase("PUT") ||
                                httpMethod.equalsIgnoreCase("PATCH")) {
                                Object requestPayload = JsonPath.read(
                                    entry.getMessage(),
                                    "$.message.params.request.postData");
                                map.put("requestPayload", requestPayload);
                            }
                        }
                    }
                } catch (Exception e) {
                    // skip
                }
                if (!map.isEmpty()) {
                    listOfMap.add(map);
                }
            }
          //  log.info("=== getApiDetails END. Maps found: {} ===", 
            //    listOfMap.size());
        } catch (Exception e) {
          //  log.error("Exception in getApiDetails: {}", e.getMessage());
        }
        return listOfMap;
    }
}