package Mac;
 
import java.io.File;

import java.io.IOException;

import java.util.Map;
 
import org.apache.commons.io.FileUtils;

import org.openqa.selenium.NotFoundException;
 
import com.sun.jna.platform.win32.User32;

import com.sun.jna.platform.win32.WinDef;

import com.tyss.optimize.common.util.CommonConstants;

import com.tyss.optimize.nlp.util.Nlp;

import com.tyss.optimize.nlp.util.NlpException;

import com.tyss.optimize.nlp.util.NlpRequestModel;

import com.tyss.optimize.nlp.util.NlpResponseModel;

import com.tyss.optimize.nlp.util.annotation.InputParam;

import com.tyss.optimize.nlp.util.annotation.InputParams;
 
public class ZeroTouchMac implements Nlp {

	@InputParams({ @InputParam(name = "Command", type = "java.lang.String") })
 
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

		String cm= (String) programElementsInput.get("Command");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {

//			String shellCommand = "cd /Users/sonicwall/Downloads && sudo sh banyan-macos.sh qa-automation-testorg1 BHYTpR/sLLaLRe4K2rfuCiyyDmwT/i9nErZDdTDn5U4= 3.28.0-rc.2; exit";

			String shellCommand = cm+"; exit";
 
	        String[] cmd = {

	            "osascript",

	            "-e", "tell application \"Terminal\"",

	            "-e", "activate",

	            "-e", "do script \"" + shellCommand.replace("\"", "\\\"") + "\"",

	            "-e", "end tell"

	        };
 
	            Process process = new ProcessBuilder(cmd).start();

			nlpResponseModel.setStatus(CommonConstants.pass);

			nlpResponseModel.setMessage("Successfully installed the application");

		} catch (Exception e) {

			nlpResponseModel.setMessage("Failed =>" + e.getMessage());

			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		return nlpResponseModel;

	}

}
 