package Mac;
 
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
 
import io.appium.java_client.mac.Mac2Driver;
 
public class excuteJarFile implements Nlp {
//	@InputParams({ @InputParam(name = "driver", type = "io.appium.java_client.mac.Mac2Driver"),
//		@InputParam(name = "Xpath", type = "java.lang.String") })
 
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
//		Mac2Driver driver = (Mac2Driver) attributes.get("driver");
//		String xpath = (String) attributes.get("Xpath");
		try {
			// Define the full command to run in Terminal
            String shellCommand = "java -jar /Users/sonicwall/Downloads/click.jar " +
                    "'/Users/sonicwall/Downloads/lock.png' " +
                    "'/Users/sonicwall/Downloads/hand.png'; exit";
 
            // Wrap the command in an AppleScript for Terminal
            String appleScript =
                "tell application \"Terminal\"\n" +
                "    activate\n" +
                "    do script \"" + shellCommand + "\"\n" +
                "end tell";
 
            // Execute the AppleScript using osascript
            String[] command = { "osascript", "-e", appleScript };
            Process process = new ProcessBuilder(command).start();
            process.waitFor();
 
            System.out.println("Terminal command executed successfully.");
 
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully performed Click Operation ");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			// Log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform action " + exceptionAsString);
		}
		return nlpResponseModel;
	}
}