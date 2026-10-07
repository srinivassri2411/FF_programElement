package Custom_Map_Logic;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

public class CompareTwoMapsWithKeyValue implements Nlp {

    @InputParams({
        @InputParam(name = "Big Map", type = "Map"),
        @InputParam(name = "Small Map", type = "Map")
    })
    @ReturnType(name = "Result", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        Map<String, Object> bigMap = (Map<String, Object>) programElementsInput.get("Big Map");
        Map<String, Object> smallMap = (Map<String, Object>) programElementsInput.get("Small Map");
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            // 🔹 Call reusable method directly
            Map<String, String> result = findMismatches(bigMap, smallMap);

            // 🔹 Handle result
            if (result.isEmpty()) {
                returnValue = " All entries match!";
            } else {
                StringBuilder sb = new StringBuilder(" Mismatches found: ");
                for (Map.Entry<String, String> entry : result.entrySet()) {
                    sb.append("Key: ")
                      .append(entry.getKey())
                      .append(" -> ")
                      .append(entry.getValue())
                      .append("; ");
                }
                returnValue = sb.toString();
            }

            nlpResponseModel.setStatus(CommonConstants.pass);

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            returnValue = "Error: " + e.getMessage();
        }

        nlpResponseModel.getAttributes().put("Result", returnValue);
        return nlpResponseModel;
    }

    // ✅ Reusable method for comparison
    public static <K, V1, V2> Map<K, String> findMismatches(Map<K, V1> bigMap, Map<K, V2> smallMap) {
        Map<K, String> mismatches = new HashMap<>();

        for (Map.Entry<K, V2> entry : smallMap.entrySet()) {
            K key = entry.getKey();
            V2 expectedValue = entry.getValue();

            if (!bigMap.containsKey(key)) {
                mismatches.put(key, "Missing key. Expected value: " + expectedValue);
            } else {
                V1 actualValue = bigMap.get(key);

                String actualStr = (actualValue != null) ? actualValue.toString() : null;
                String expectedStr = (expectedValue != null) ? expectedValue.toString() : null;

                if (!Objects.equals(actualStr, expectedStr)) {
                    mismatches.put(key,
                        "Value mismatch. Expected: " + expectedStr + ", Found: " + actualStr);
                }
            }
        }
        return mismatches;
    }
}