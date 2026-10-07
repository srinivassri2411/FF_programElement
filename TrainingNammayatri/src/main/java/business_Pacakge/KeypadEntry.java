package business_Pacakge;

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

public class KeypadEntry implements Nlp {
	@InputParams({ @InputParam(name = "String", type = "java.lang.String") })

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
		String string = (String) attributes.get("String");

		try {
			AndroidDriver driver = nlpRequestModel.getAndroidDriver();
			pressKey(string, driver);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Clicked on Dial Keypad " + string);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to click on Dial Keypad " + e);
		}
		return nlpResponseModel;
	}

	void pressKey(String value, AndroidDriver driver) throws InterruptedException {
		char[] c = value.toCharArray();
		for (char item : c) {
			switch (item) {
			case '@':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.AT));
				break;
			case '.':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.PERIOD));
				break;
			case 'A':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.A));
				break;
			case 'B':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.B));
				break;
			case 'C':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.C));
				break;
			case 'D':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.D));
				break;
			case 'E':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.E));
				break;
			case 'F':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.F));
				break;
			case 'G':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.G));
				break;
			case 'H':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.H));
				break;
			case 'I':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.I));
				break;
			case 'J':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.J));
				break;
			case 'K':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.K));
				break;
			case 'L':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.L));
				break;
			case 'M':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.M));
				break;
			case 'N':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.N));
				break;
			case 'O':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.O));
				break;
			case 'P':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.P));
				break;
			case 'Q':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.Q));
				break;
			case 'R':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.R));
				break;
			case 'S':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.S));
				break;
			case 'T':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.T));
				break;
			case 'U':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.U));
				break;
			case 'V':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.V));
				break;
			case 'W':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.W));
				break;
			case 'X':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.X));
				break;
			case 'Y':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.Y));
				break;
			case 'Z':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.Z));
				break;
			case '0':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_0));
				break;
			case '1':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_1));
				break;
			case '2':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_2));
				break;
			case '3':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_3));
				break;
			case '4':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_4));
				break;
			case '5':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_5));
				break;
			case '6':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_6));
				break;
			case '7':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_7));
				break;
			case '8':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_8));
				break;
			case '9':
				driver.pressKey(new KeyEvent().withKey(AndroidKey.DIGIT_9));
				break;
			default:
				throw new RuntimeException("Not match to the Dial Keypad");
			}
			Thread.sleep(500);
		}
	}
}
