package specializedAutomation2;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import java.io.File;

public class CreateDirectory implements Nlp {

    @InputParams({
        @InputParam(name = "Directory Path", type = "java.lang.String")
    })
    @ReturnType(name = "Directory Created", type = "java.lang.Boolean")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String directoryPath = (String) programElementsInput.get("Directory Path");

        Boolean returnValue = false;
        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            // Your program element business logic starts here ...
            File dir = new File(directoryPath);

            // Create directory (including missing parent dirs)
            boolean created = dir.mkdirs();

            // If directory already exists, treat as success
            if (dir.exists()) {
                returnValue = true;
            } else {
                returnValue = created;
            }

            nlpResponseModel.setStatus(CommonConstants.pass);

            // Optionally override default pass message
            // nlpResponseModel.setMessage("Directory created successfully");

        } catch (Exception e) {
            // Your program element Exception handling goes here ...
            nlpResponseModel.setStatus(CommonConstants.fail);

            // Optionally override default fail message
            // nlpResponseModel.setMessage("Failed to create directory");
        }

        // Your program element business logic ends here ...
        nlpResponseModel.getAttributes().put("Directory Created", returnValue);
        return nlpResponseModel;
    }
}
