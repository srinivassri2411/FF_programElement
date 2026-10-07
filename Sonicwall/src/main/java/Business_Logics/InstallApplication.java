package Business_Logics;

import java.io.BufferedReader;
import java.io.File;
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

public class InstallApplication implements Nlp {

    @InputParams({
        @InputParam(name = "ExeFilePath", type = "java.lang.String")
    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> inputParams = nlpRequestModel.getAttributes();
        String exeFilePath = (String) inputParams.get("ExeFilePath");
        NlpResponseModel responseModel = new NlpResponseModel();

        try {
//            System.out.println("Starting installation: " + exeFilePath);
//            String output = executeCommand(exeFilePath);
//            System.out.println("Installer output:\n" + output);
//
//            responseModel.setStatus(CommonConstants.pass);
//            responseModel.setMessage("Application installer launched successfully. "+output);
        	 System.out.println("Starting installation: " + exeFilePath);

             boolean isInstalled = false;
             int maxRetries = 3;
             for (int i = 1; i <= maxRetries; i++) {

            	    System.out.println("Attempt " + i + " to install...");

            	    String output = executeCommand("\"" + exeFilePath + "\"");
            	    System.out.println("Installer output:\n" + output);

            	    String installedPath = "C:\\Program Files\\SonicWall Cloud Secure Edge\\SonicWall Cloud Secure Edge.exe";

            	    boolean found = waitForFile(installedPath, 60000); 

            	    if (found) {
            	        isInstalled = true;
            	        System.out.println("Installation detected at: " + installedPath);
            	        break;
            	    } else {
            	        System.out.println("Installation not found within timeout. Retrying...");
            	    }
            	}

             if (isInstalled) {
                 responseModel.setStatus(CommonConstants.pass);
                 responseModel.setMessage("Application installed successfully.");
             } else {
                 responseModel.setStatus(CommonConstants.fail);
                 responseModel.setMessage("Installation failed after retries.");
             }


        } catch (Exception e) {
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage("Failed to install application: " + e.getMessage());
        }

        return responseModel;
    }

    public static String executeCommand(String command) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
        }

        int exitCode = process.waitFor();
        output.append("Exited with code: ").append(exitCode);
        return output.toString();
    }

    public static boolean waitForFile(String path, long timeoutMillis) throws InterruptedException {
        File file = new File(path);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (file.exists()) {
                return true;
            }
            Thread.sleep(2000); // check every 2 seconds
        }
        return false;
    }
    public static void main(String[] args) throws NlpException {
        NlpRequestModel request = new NlpRequestModel();
        request.getAttributes().put("ExeFilePath", "C:\\Path\\To\\YourInstaller.exe");

        NlpResponseModel response = new InstallApplication().execute(request);
        System.out.println("Status: " + response.getStatus());
        System.out.println("Message: " + response.getMessage());
    }
}
