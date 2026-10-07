package browser_capability;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import java.util.logging.Level;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetPerformanceLoggingCapability implements Nlp {

    @InputParams({
        @InputParam(name = "capabilities", type = "java.lang.Object")
    })
    @ReturnType(name = "updatedCapabilities", type = "java.lang.Object")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        Object capObject = programElementsInput.get("capabilities");
        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
//            log.info("=== SetPerformanceLoggingCapability START ===");
//            log.info("Capabilities object type: {}", capObject.getClass().getName());

            // Cast to DesiredCapabilities
            DesiredCapabilities cap = (DesiredCapabilities) capObject;

            // Create LoggingPreferences and enable performance logging
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);

            // Set it on the capabilities object
            cap.setCapability("goog:loggingPrefs", logPrefs);

//            log.info("Performance logging capability set successfully!");
//            log.info("=== SetPerformanceLoggingCapability END ===");

            nlpResponseModel.getAttributes().put("updatedCapabilities", cap);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Performance logging capability set successfully.");

        } catch (Exception e) {
           // log.error("Exception: {}", e.getMessage());
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed: " + e.getMessage());
        }
        return nlpResponseModel;
    }
}
