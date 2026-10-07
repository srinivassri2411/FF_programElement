package fileHandling_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FileToBase64_MultiInput implements Nlp {

    @InputParams({
        @InputParam(name = "FilePath", type = "java.lang.String"),
        @InputParam(name = "FileStream", type = "java.io.InputStream")  
    })
    @ReturnType(name = "base64Value", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String filePath = null;
        InputStream inputStream = null;
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            Object filePathObj = programElementsInput.get("FilePath");
            Object fileStreamObj = programElementsInput.get("FileStream");

            // Check if FilePath is provided and not empty
            if (filePathObj instanceof String && !((String) filePathObj).trim().isEmpty()) {
                filePath = (String) filePathObj;
                File file = new File(filePath);

                // Validate if the file exists
                if (!file.exists() || !file.isFile()) {
                    throw new IllegalArgumentException("Invalid file path: File does not exist.");
                }

                inputStream = new FileInputStream(file);  // Open FileInputStream
            } 
            // If FilePath is empty, check for FileStream
            else if (fileStreamObj instanceof InputStream) {
                inputStream = (InputStream) fileStreamObj;
            } 
            // If neither is provided, throw an error
            else {
                throw new IllegalArgumentException("Either a valid FilePath (String) or FileStream (InputStream) must be provided.");
            }

            // Convert to Base64
            returnValue = encodeImageToBase64(inputStream);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Converted successfully to base64");

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to convert: " + e.getMessage());
        }

        nlpResponseModel.getAttributes().put("base64Value", returnValue);
        return nlpResponseModel;
    }

    public static String encodeImageToBase64(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();  
        inputStream.close();
        return Base64.getEncoder().encodeToString(bytes);
    }
}