package BusinessLogics;

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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ReplaceCompleteData implements Nlp {

    @InputParams({
        @InputParam(name = "Header Map", type = "java.util.Map"),
        @InputParam(name = "Excel Data", type = "java.util.Map"),
        @InputParam(name = "Value", type = "java.lang.String")
    })
    
    @ReturnType(name = "Updated Value", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        Map<String, Object> headerName = (Map<String, Object>) programElementsInput.get("Header Map");
        Map<String, Object> excelData = (Map<String, Object>) programElementsInput.get("Excel Data");
        String value = (String) programElementsInput.get("Value");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
         List Data=new ArrayList();
        try {
            for (Map.Entry<String, Object> entry : headerName.entrySet()) {

            	  Object actualValue =null;
                String header = entry.getKey();
                String placeholder = entry.getValue().toString();

                if (header.contains("value::")) {
                    value = value.replace(placeholder, header.split("value::")[1]);
                    continue;

				}else {
                 actualValue = excelData.get(header);
				}

                if (actualValue != null) {
                    value = value.replace(placeholder, actualValue.toString());
                } else {
                	Data.add(header);
                    System.out.println("Missing value for: " + header);
                }
            }

            System.out.println("Final Value: " + value);

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Succesfully replaced data with missing data " +Data.toString());
           
        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to replace data");
        }

        // Set output
        nlpResponseModel.getAttributes().put("Updated Value", value);

        return nlpResponseModel;
    }
}