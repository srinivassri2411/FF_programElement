package Business_Logics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

public class UpdatePS1File implements Nlp {
    @InputParams({ @InputParam(name = "PS1 path", type = "java.lang.String"),
            @InputParam(name = "Key", type = "java.lang.String"),
            @InputParam(name = "New Value", type = "java.lang.String") })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String pS1path = (String) programElementsInput.get("PS1 path");
        String key = (String) programElementsInput.get("Key");
        String newValue = (String) programElementsInput.get("New Value");
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            boolean changed = modifyKey(pS1path, key, newValue, false);
            if (changed) {
                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel.setMessage("Changes done in given ps1 file");
            } else {
                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel.setMessage("Changes not done in given ps1 file cause values are same");
            }
        } catch (Exception e) {
            nlpResponseModel.setMessage("Failed to change " + e);
            nlpResponseModel.setStatus(CommonConstants.fail);
        }
        return nlpResponseModel;
    }

    public static boolean modifyKey(String filePath, String key, String newValue, boolean appendIfMissing)
            throws IOException {

        String os = System.getProperty("os.name").toLowerCase();

        // Existing Windows logic - unchanged
        if (os.contains("win")) {

            List<String> lines = Files.readAllLines(Paths.get(filePath));
            List<String> updatedLines = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith(key + " ")) {
                    updatedLines.add(key + " = " + newValue);
                    found = true;
                } else {
                    updatedLines.add(line);
                }
            }

            if (!found && appendIfMissing) {
                updatedLines.add(key + " = " + newValue);
                found = true;
            }

            if (found) {
                Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return found;
        }

        // macOS / Linux logic
        else {

            List<String> lines = Files.readAllLines(Paths.get(filePath));
            List<String> updatedLines = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {

                String trimmed = line.trim();

                // Ignore comments and empty lines
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    updatedLines.add(line);
                    continue;
                }

                int equalsIndex = trimmed.indexOf('=');

                if (equalsIndex > 0) {

                    String existingKey = trimmed.substring(0, equalsIndex).trim();

                    if (existingKey.equalsIgnoreCase(key)) {
                        updatedLines.add(existingKey + "=" + newValue);
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }

                } else {
                    updatedLines.add(line);
                }
            }

            if (!found && appendIfMissing) {
                updatedLines.add(key + "=" + newValue);
                found = true;
            }

            if (found) {
                Files.write(Paths.get(filePath), updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return found;
        }
    }
    
    public static void main(String[] args) throws Exception {

        boolean result = modifyKey(
                "/Users/apple/Downloads/sonicwall-cse-macos (2).sh",
                "AUTO_LOGIN",
                "false",
                false);

        System.out.println("Updated: " + result);
    }

}