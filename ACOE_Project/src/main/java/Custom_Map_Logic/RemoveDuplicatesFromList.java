package Custom_Map_Logic;


import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoveDuplicatesFromList implements Nlp {

    @SuppressWarnings("unchecked")
	@InputParams({ @InputParam(name = "inputList", type = "java.util.List") })
    @ReturnType(name = "processedResult", type = "java.util.Map")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        List<Object> inputList = (List<Object>)
            nlpRequestModel.getAttributes().get("inputList");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
//            log.info("=== RemoveDuplicatesFromList START ===");
//            log.info("Total items before dedup: {}", inputList.size());

            Set<String> seen = new LinkedHashSet<>();
            List<Object> uniqueList = new ArrayList<>();
            List<Object> duplicateList = new ArrayList<>();

            for (Object item : inputList) {
                String cleanItem = item.toString().trim().toLowerCase();
                if (!seen.add(cleanItem)) {
                    if (!duplicateList.contains(item)) {
                        duplicateList.add(item);
                    }
                } else {
                    uniqueList.add(item);
                }
            }

//            log.info("Unique items: {}", uniqueList.size());
//            log.info("Duplicate items: {}", duplicateList);
//            log.info("=== RemoveDuplicatesFromList END ===");

            Map<String, Object> processedResult = new LinkedHashMap<>();
            processedResult.put("uniqueList", uniqueList);
            processedResult.put("duplicateList", duplicateList);
            processedResult.put("uniqueCount", uniqueList.size());
            processedResult.put("duplicateCount", duplicateList.size());
            processedResult.put("totalCount", inputList.size());

            nlpResponseModel.getAttributes()
                .put("processedResult", processedResult);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Duplicates removed. Total: "
                + inputList.size() + ", Unique: " + uniqueList.size()
                + ", Duplicates: " + duplicateList.size());

        } catch (Exception e) {
           // log.error("Exception in RemoveDuplicatesFromList: {}", e.getMessage());
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed: " + e.getMessage());
        }

        return nlpResponseModel;
    }
}
