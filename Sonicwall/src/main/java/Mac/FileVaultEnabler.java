package Mac;

import java.io.*;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FileVaultEnabler implements Nlp {

    @InputParams({ @InputParam(name = "username", type = "java.lang.String"),
                   @InputParam(name = "password", type = "java.lang.String") })
    @ReturnType(name = "result", type = "java.lang.String")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        NlpResponseModel responseModel = new NlpResponseModel();
        Map<String, Object> inputs = nlpRequestModel.getAttributes();

        String username = (String) inputs.get("username");
        String password = (String) inputs.get("password");
        String result;
        String logFilePath = System.getProperty("user.home") + "/EnableFileVault_Log.txt";

        try {
            // Step 1: Check if FileVault is already enabled
            Process check = Runtime.getRuntime().exec("fdesetup status");
            BufferedReader reader = new BufferedReader(new InputStreamReader(check.getInputStream()));
            String line;
            StringBuilder statusOutput = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                statusOutput.append(line).append("\n");
            }

            if (statusOutput.toString().contains("FileVault is On")) {
                responseModel.setStatus(CommonConstants.pass);
                responseModel.setMessage("FileVault is already enabled.");
                responseModel.getAttributes().put("result", "Already Enabled");
                return responseModel;
            }

            // Step 2: Enable FileVault using fdesetup
            File scriptFile = new File(System.getProperty("user.home") + "/enableFileVault.sh");
            writeShellScript(scriptFile, username, password);

            ProcessBuilder pb = new ProcessBuilder("sh", scriptFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String outLine;
            while ((outLine = br.readLine()) != null) {
                output.append(outLine).append("\n");
            }
            int exitCode = process.waitFor();

            // Log result
            try (FileWriter fw = new FileWriter(logFilePath)) {
                fw.write(output.toString());
            }

            if (exitCode == 0 && output.toString().contains("FileVault has been enabled")) {
                result = "FileVault enabled successfully.";
                responseModel.setStatus(CommonConstants.pass);
            } else {
                result = "FileVault enabling may have failed. Review logs.";
                responseModel.setStatus(CommonConstants.fail);
            }
            responseModel.setMessage(result);
            responseModel.getAttributes().put("result", output.toString());

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            responseModel.setMessage("Exception occurred: " + sw.toString());
            responseModel.setStatus(CommonConstants.fail);
        }

        return responseModel;
    }

    private void writeShellScript(File script, String username, String password) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(script))) {
            writer.write("#!/bin/bash\n");
            writer.write("expect <<EOF\n");
            writer.write("spawn sudo fdesetup enable -user \"" + username + "\"\n");
            writer.write("expect \"Password:\"\n");
            writer.write("send \"" + password + "\\r\"\n");
            writer.write("expect eof\n");
            writer.write("EOF\n");
        }
        script.setExecutable(true);
    }

    public static void main(String[] args) throws NlpException {
        NlpRequestModel model = new NlpRequestModel();
        model.getAttributes().put("username", "your_mac_username");
        model.getAttributes().put("password", "your_mac_password");
        System.out.println(new FileVaultEnabler().execute(model));
    }
}