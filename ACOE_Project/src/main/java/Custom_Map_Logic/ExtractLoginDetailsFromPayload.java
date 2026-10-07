package Custom_Map_Logic;


import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtractLoginDetailsFromPayload implements Nlp {

    @InputParams({
        @InputParam(name = "requestPayload", type = "java.lang.String")
    })
    @ReturnType(name = "loginDetailsMap", type = "java.util.Map")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String payload = (String) programElementsInput.get("requestPayload");
        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
//            log.info("=== ExtractLoginDetailsFromPayload START ===");
//            log.info("Payload received: {}", payload);

            Map<String, Object> loginDetailsMap = new LinkedHashMap<>();

            // Split payload by &
            String[] parts = payload.split("&");
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = URLDecoder.decode(
                        keyValue[1].trim(), 
                        StandardCharsets.UTF_8.toString()
                    );
                 //   log.info("Key: {}, Value: {}", key, value);
                    if (key.equalsIgnoreCase("login")) {
                        loginDetailsMap.put("login", value);
                    } else if (key.equalsIgnoreCase("password")) {
                        loginDetailsMap.put("password", value);
                    }
                }
            }

//            log.info("Login details extracted: {}", loginDetailsMap);
//            log.info("=== ExtractLoginDetailsFromPayload END ===");

            nlpResponseModel.getAttributes().put("loginDetailsMap", loginDetailsMap);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Login details extracted successfully.");

        } catch (Exception e) {
        //    log.error("Exception: {}", e.getMessage());
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed: " + e.getMessage());
        }
        return nlpResponseModel;
    }
}
