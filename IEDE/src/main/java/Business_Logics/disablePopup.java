package Business_Logics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class disablePopup implements Nlp {
    @InputParams({ @InputParam(name = "Downloads path", type = "java.lang.String") })
    @ReturnType(name = "MapVal", type = "java.util.Map")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String downloadPath = (String) programElementsInput.get("Downloads path");
        Map<String, Object> prefs = new HashMap<String, Object>();

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            
            prefs.put("download.default_directory", downloadPath);
            prefs.put("download.prompt_for_download", false);
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("plugins.always_open_pdf_externally", true);

            nlpResponseModel.setStatus(CommonConstants.pass);
             nlpResponseModel.setMessage("Fetched the map");
        } catch (Exception e) {
            // Your program element Exception handling goes here ...
            nlpResponseModel.setStatus(CommonConstants.fail);

             nlpResponseModel.setMessage("Failed to get the map");
        }

        // Your program element business logic ends here ...
        nlpResponseModel.getAttributes().put("MapVal", prefs);
        return nlpResponseModel;
    }

}