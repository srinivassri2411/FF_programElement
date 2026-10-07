package generic;

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

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP1450c4a0-4f5a-4f74-abba-0a579d9bb32f")
public class PressAnyKeyAndroid implements Nlp {
	@InputParams({ @InputParam(name = "Key", type = "java.lang.String") })

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
		// Srinivas M
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String key = (String) attributes.get("Key");

		try {
			AndroidDriver driver = nlpRequestModel.getAndroidDriver();
			pressAnyKey(key, driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Pressed given " + key + " key");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to press " + e);
		}
		return nlpResponseModel;
	}

	void pressAnyKey(String value, AndroidDriver driver) {
		switch (value.toLowerCase()) {
		case "tab":
			driver.pressKey(new KeyEvent().withKey(AndroidKey.TAB));
			break;
		case "enter":
			driver.pressKey(new KeyEvent().withKey(AndroidKey.ENTER));
			break;
		case "space":
			driver.pressKey(new KeyEvent().withKey(AndroidKey.SPACE));
			break;
		default:
			System.out.println("No Such Key");
			break;
		}
	}
}