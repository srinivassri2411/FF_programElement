
package logics;

import java.util.Map;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class EnterUsingActions implements Nlp {
	@InputParams({ @InputParam(name = "Input", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String input = (String) programElementsInput.get("Input");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			enterInputUsingActions(nlpRequestModel.getWebDriver(), input);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Entered given input " + input);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to enter " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		return nlpResponseModel;
	}

	public static void enterInputUsingActions(WebDriver driver, String input) {
		char[] c = input.toCharArray();
		Actions actions = new Actions(driver);
		for (char value : c) {
			switch (value) {
			case '@':
				actions.sendKeys("@");
				break;
			case '/':
				actions.sendKeys("/");
				break;
			case '-':
				actions.sendKeys("-");
				break;
			case '.':
				actions.sendKeys(".");
				break;
			case '#':
				actions.sendKeys("#");
				break;
			case ':':
				actions.sendKeys(":");
				break;
			case 'A':
				actions.sendKeys("A");
				break;
			case 'B':
				actions.sendKeys("B");
				break;
			case 'C':
				actions.sendKeys("C");
				break;
			case 'D':
				actions.sendKeys("D");
				break;
			case 'E':
				actions.sendKeys("E");
				break;
			case 'F':
				actions.sendKeys("F");
				break;
			case 'G':
				actions.sendKeys("G");
				break;
			case 'H':
				actions.sendKeys("H");
				break;
			case 'I':
				actions.sendKeys("I");
				break;
			case 'J':
				actions.sendKeys("J");
				break;
			case 'K':
				actions.sendKeys("K");
				break;
			case 'L':
				actions.sendKeys("L");
				break;
			case 'M':
				actions.sendKeys("M");
				break;
			case 'N':
				actions.sendKeys("N");
				break;
			case 'O':
				actions.sendKeys("O");
				break;
			case 'P':
				actions.sendKeys("P");
				break;
			case 'Q':
				actions.sendKeys("Q");
				break;
			case 'R':
				actions.sendKeys("R");
				break;
			case 'S':
				actions.sendKeys("S");
				break;
			case 'T':
				actions.sendKeys("T");
				break;
			case 'U':
				actions.sendKeys("U");
				break;
			case 'V':
				actions.sendKeys("V");
				break;
			case 'W':
				actions.sendKeys("W");
				break;
			case 'X':
				actions.sendKeys("X");
				break;
			case 'Y':
				actions.sendKeys("Y");
				break;
			case 'Z':
				actions.sendKeys("Z");
				break;
			case '0':
				actions.sendKeys(Keys.NUMPAD0);
				break;
			case '1':
				actions.sendKeys(Keys.NUMPAD1);
				break;
			case '2':
				actions.sendKeys(Keys.NUMPAD2);
				break;
			case '3':
				actions.sendKeys(Keys.NUMPAD3);
				break;
			case '4':
				actions.sendKeys(Keys.NUMPAD4);
				break;
			case '5':
				actions.sendKeys(Keys.NUMPAD5);
				break;
			case '6':
				actions.sendKeys(Keys.NUMPAD6);
				break;
			case '7':
				actions.sendKeys(Keys.NUMPAD7);
				break;
			case '8':
				actions.sendKeys(Keys.NUMPAD8);
				break;
			case '9':
				actions.sendKeys(Keys.NUMPAD9);
				break;
			case 'a':
				actions.sendKeys("a");
				break;
			case 'b':
				actions.sendKeys("b");
				break;
			case 'c':
				actions.sendKeys("c");
				break;
			case 'd':
				actions.sendKeys("d");
				break;
			case 'e':
				actions.sendKeys("e");
				break;
			case 'f':
				actions.sendKeys("f");
				break;
			case 'g':
				actions.sendKeys("g");
				break;
			case 'h':
				actions.sendKeys("h");
				break;
			case 'i':
				actions.sendKeys("i");
				break;
			case 'j':
				actions.sendKeys("j");
				break;
			case 'k':
				actions.sendKeys("k");
				break;
			case 'l':
				actions.sendKeys("l");
				break;
			case 'm':
				actions.sendKeys("m");
				break;
			case 'n':
				actions.sendKeys("n");
				break;
			case 'o':
				actions.sendKeys("o");
				break;
			case 'p':
				actions.sendKeys("p");
				break;
			case 'q':
				actions.sendKeys("q");
				break;
			case 'r':
				actions.sendKeys("r");
				break;
			case 's':
				actions.sendKeys("s");
				break;
			case 't':
				actions.sendKeys("t");
				break;
			case 'u':
				actions.sendKeys("u");
				break;
			case 'v':
				actions.sendKeys("v");
				break;
			case 'w':
				actions.sendKeys("w");
				break;
			case 'x':
				actions.sendKeys("x");
				break;
			case 'y':
				actions.sendKeys("y");
				break;
			case 'z':
				actions.sendKeys("z");
				break;
			default:
				System.out.println("Not match to the Dial Keypad => " + value);
				break;
			}
		}
		actions.perform();
	}
}