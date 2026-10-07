package Business_Logic;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class VideoMock implements Nlp {

    @InputParams({ @InputParam(name = "Desired Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities"),
            @InputParam(name = "File Stream", type = "java.io.InputStream") })
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
        DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Desired Capabilities");
        InputStream inputStream = (InputStream) attributes.get("File Stream");

        try {
            DesiredCapabilities dc = new DesiredCapabilities();
            String outputFile = System.getProperty("user.dir") + "\\" + "outputout.y4m";
            Path filePath = Paths.get(outputFile);

            // Check if the output file exists. If not, write the input stream to the file.
            if (!Files.exists(filePath)) {
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                inputStream.close();
            }

            // Configure browser options for Chrome
            if (cap.getBrowserName().contains("chrome")) {
                HashMap<String, Object> chromePrefs = new HashMap<>();
                chromePrefs.put("autofill.profile_enabled", false);

                ChromeOptions options = new ChromeOptions();
                LoggingPreferences logPrefs = new LoggingPreferences();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--allow-file-access-from-files", "--use-fake-ui-for-media-stream",
                        "--allow-file-access");
                options.addArguments("use-fake-ui-for-media-stream");
                options.addArguments("--use-fake-device-for-media-stream");
                options.addArguments("--use-file-for-fake-video-capture=" + outputFile);
                options.setCapability("goog:loggingPrefs", logPrefs);
                options.addArguments("--disable-autofill-addresses");
                options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                options.setExperimentalOption("prefs", chromePrefs);

                dc.setCapability(ChromeOptions.CAPABILITY, options);
                cap.merge(dc);

            } else if (cap.getBrowserName().contains("edge")) { // Handling Edge browser
                EdgeOptions options = new EdgeOptions();
                LoggingPreferences logPrefs = new LoggingPreferences();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--allow-file-access-from-files", "--allow-file-access");
                options.addArguments("use-fake-ui-for-media-stream");
                options.addArguments("--use-fake-device-for-media-stream");
                options.addArguments("--use-file-for-fake-video-capture=" + outputFile);
                options.setCapability("goog:loggingPrefs", logPrefs);
                options.addArguments("--disable-autofill-addresses");
                options.addArguments("--disable-autofill-profile");
                options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

                dc.setCapability(EdgeOptions.CAPABILITY, options);
                cap.merge(dc);
            }

            // If the capabilities are successfully set, return a successful response
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully Mocked the Camera");

        } catch (Exception e) {
            // In case of any failure, set the response status as failed
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed To Add Arguments: " + e.getMessage());
        }

        return nlpResponseModel;
    }
}
