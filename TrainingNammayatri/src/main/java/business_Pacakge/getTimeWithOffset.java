package business_Pacakge;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class getTimeWithOffset implements Nlp {

    @InputParams({
        @InputParam(name = "hourOffset", type = "java.lang.Integer"),
        @InputParam(name = "timeFormat", type = "java.lang.String")
    })
    @ReturnType(name = "formattedTime", type = "java.lang.String")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> attributes = nlpRequestModel.getAttributes();
        NlpResponseModel responseModel = new NlpResponseModel();

        try {
            int hourOffset = (int) attributes.get("hourOffset");
            String timeFormat = (String) attributes.getOrDefault("timeFormat", "HH:mm:ss");

            LocalTime currentTime = LocalTime.now();
            LocalTime adjustedTime = currentTime.plusHours(hourOffset);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
            String formattedTime = adjustedTime.format(formatter);

            responseModel.setStatus(CommonConstants.pass);
            responseModel.getAttributes().put("formattedTime", formattedTime);
        } catch (Exception e) {
            responseModel.setStatus(CommonConstants.fail);
        }

        return responseModel;
    }
}
