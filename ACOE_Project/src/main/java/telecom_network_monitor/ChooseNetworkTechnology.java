
package telecom_network_monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ChooseNetworkTechnology implements Nlp {
    
	@InputParams({@InputParam(name = "Adb Command", type = "java.lang.String")})
    

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String adbCommand = (String) programElementsInput.get("Adb Command");
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            // Your program element business logic starts here ...
      	  Runtime.getRuntime().exec(adbCommand);
      	  
            nlpResponseModel.setStatus(CommonConstants.pass);
        }
        catch(Exception e) {
        	 // Your program element Exception handling goes here ...
           nlpResponseModel.setStatus(CommonConstants.fail);

        }

        // Your program element business logic ends here ...
        return nlpResponseModel;
    }
	
	// Toggle Airplane Mode (ON -> OFF)
    public static void toggleAirplaneMode() throws InterruptedException {
    	execAdb("adb shell cmd connectivity airplane-mode enable");
        Thread.sleep(2000); // small gap
        execAdb("adb shell cmd connectivity airplane-mode disable");
    }
	
	// Executes adb command and returns output
    public static String execAdb(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    

    // Wait until LTE or NR is registered
    public static boolean waitForNetwork(int maxWaitSeconds) throws InterruptedException {
        int attempts = maxWaitSeconds / 2;

        for (int i = 0; i < attempts; i++) {
            String output = execAdb("adb shell dumpsys telephony.registry");

            if (output.contains("LTE") || output.contains("NR")) {
                return true;
            }
            Thread.sleep(2000);
        }

        System.err.println("Network not registered within timeout");
        return false;
    }

  } 