package Windows;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.windows.WindowsDriver;

public class clickOnImageNew implements Nlp {

    @InputParams({
        @InputParam(name = "driver", type = "io.appium.java_client.windows.WindowsDriver"),
        @InputParam(name = "Xpath", type = "java.lang.String")
    })

    @Override
    public List<String> getTestParameters() throws NlpException {
        return new ArrayList<>();
    }

    @Override
    public StringBuilder getTestCode() throws NlpException {
        return new StringBuilder();
    }

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        Map<String, Object> attributes = nlpRequestModel.getAttributes();

        WindowsDriver driver = (WindowsDriver) attributes.get("driver");
        String xpath = (String) attributes.get("Xpath");

        try {
            Thread.sleep(1000);
            driver.findElement(By.xpath(xpath)).click();
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully performed Click Operation using XPath.");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to perform click using XPath. Error: " + exceptionAsString);
        }

        return nlpResponseModel;
    }

    // Standalone Sikuli click demo - if needed
    public static void main(String[] args) throws FindFailed, InterruptedException {
        Screen screen = new Screen();
        Thread.sleep(4000);
        Pattern imagePattern = new Pattern("C:\\Path\\To\\Your\\Image.png"); // Use absolute path for Windows
        screen.find(imagePattern).click();
    }
}

