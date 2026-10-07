
package businessLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;


public class KillProcessUsingPort implements Nlp {
	@InputParams({ @InputParam(name = "Port", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String port = (String) programElementsInput.get("Port");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			killProcess(Integer.parseInt(port));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Cleared the port number");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Clear the port number" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

	public void killProcess(Integer port) throws IOException {
		String line;
		String command;
		String operatingSystem = System.getProperty("os.name");
		if (operatingSystem.contains("Windows")) {
			command = "netstat -ano";
		} else {
			command = "netstat -nlp | grep :" + port;
		}
		Process p = Runtime.getRuntime().exec(command);
		try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			while ((line = input.readLine()) != null) {
				if (line.contains(Integer.toString(port))) {
					String pid = null;
					if (operatingSystem.contains("Windows")) {
						if (line.contains(":" + port)) {
							String[] parts = line.trim().split("\\s+");
							pid = Integer.parseInt(parts[parts.length - 1]) + "";
						}
					//	log.info("**************id is " + pid);
					} else {
						pid = line.substring(line.indexOf("LISTEN") + 6, line.lastIndexOf("/")).replaceAll(" ", "");
					//	log.info("**************id is " + pid);
					}
					try {
						if (operatingSystem.contains("Windows")) {
							Runtime.getRuntime().exec("taskkill /F /PID " + pid);
						} else {
							Runtime.getRuntime().exec("kill " + pid);
						}
					} catch (IOException e) {
						e.printStackTrace();
					//	log.info("Failed to kill process with PID " + pid);
					}
				}
			}
		}
	}
}