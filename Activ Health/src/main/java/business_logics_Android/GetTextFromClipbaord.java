package business_logics_Android;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.clipboard.ClipboardContentType;
import io.appium.java_client.ios.IOSDriver;

public class GetTextFromClipbaord implements Nlp {

    @ReturnType(name = "Text from clipboard is ", type = "java.lang.String")
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
        String text = null;

        AndroidDriver androidDriver = null;
        IOSDriver iosDriver = null;

        androidDriver = nlpRequestModel.getAndroidDriver();
        iosDriver = nlpRequestModel.getIosDriver();

        try {
            if (androidDriver != null) {
                androidDriver.getClipboard(ClipboardContentType.PLAINTEXT); 
                text = androidDriver.getClipboardText();
            }

            else {
                iosDriver.getClipboard(ClipboardContentType.PLAINTEXT); 
                text = iosDriver.getClipboardText();

            }
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully text fetched");

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to fetch text " + e);
        }
        nlpResponseModel.getAttributes().put("Text from clipboard is ", text);
        return nlpResponseModel;
    }
}