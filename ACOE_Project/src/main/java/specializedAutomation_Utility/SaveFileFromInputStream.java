package specializedAutomation_Utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SaveFileFromInputStream implements Nlp {

    @InputParams({
        @InputParam(name = "File InputStream", type = "java.io.InputStream"),
        @InputParam(name = "File Name", type = "java.lang.String")
    })
    @ReturnType(name = "Absolute File Path", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        InputStream inputStream = (InputStream) programElementsInput.get("File InputStream");
        String fileName = (String) programElementsInput.get("File Name");
        String absolutePath = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            // Documents folder of the current user
            String documentsPath = Paths.get(System.getProperty("user.home"), "Documents").toString();
            File localFile = new File(documentsPath, fileName);

            // Save stream to file
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            absolutePath = localFile.getAbsolutePath();

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("File saved successfully at: " + absolutePath);
        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to save file: " + e.getMessage());
        }

        // Return absolute path in attributes
        nlpResponseModel.getAttributes().put("Absolute File Path", absolutePath);
        return nlpResponseModel;
    }
}
