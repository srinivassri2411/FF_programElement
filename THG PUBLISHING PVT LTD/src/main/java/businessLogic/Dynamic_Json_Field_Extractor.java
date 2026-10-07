package businessLogic;

import com.google.gson.*;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Dynamic_Json_Field_Extractor implements Nlp {

    @InputParams({
        @InputParam(name = "JSON URL", type = "java.lang.String"),
        @InputParam(name = "Field Path", type = "java.lang.String")
    })
    @ReturnType(name = "Unique Values", type = "java.lang.String")
    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> input = nlpRequestModel.getAttributes();

        NlpResponseModel responseModel = new NlpResponseModel();
        String rawJsonResponse = null;

        // Safely extract inputs and validate
        String jsonUrl = input.get("JSON URL") != null ? ((String) input.get("JSON URL")).trim() : "";
        String fieldPath = input.get("Field Path") != null ? ((String) input.get("Field Path")).trim().replaceAll("^\\.*", "") : "";

        try {
            if (jsonUrl.isEmpty()) {
                responseModel.setStatus(CommonConstants.fail);
                responseModel.setMessage("JSON URL is required.");
                return responseModel;
            }
            if (fieldPath.isEmpty()) {
                responseModel.setStatus(CommonConstants.fail);
                responseModel.setMessage("Field Path is required.");
                return responseModel;
            }

            HttpURLConnection connection = (HttpURLConnection) new URL(jsonUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                responseModel.setStatus(CommonConstants.fail);
                responseModel.setMessage("HTTP Error Code: " + responseCode);
                return responseModel;
            }

            StringBuilder rawResponseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    rawResponseBuilder.append(line);
                }
            }

            rawJsonResponse = rawResponseBuilder.toString();
            if (rawJsonResponse.trim().isEmpty()) {
                responseModel.setStatus(CommonConstants.fail);
                responseModel.setMessage("Empty JSON response.");
                responseModel.getAttributes().put("Unique Values", "");
                return responseModel;
            }

            JsonElement root = JsonParser.parseString(rawJsonResponse);
            Set<String> results = extractUniqueValues(root, fieldPath.split("\\."));

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultJson = gson.toJson(results);

            responseModel.setStatus(CommonConstants.pass);
            responseModel.getAttributes().put("Unique Values", resultJson);

        } catch (Exception e) {
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage("Exception: " + e.getMessage());
            responseModel.getAttributes().put("Unique Values", rawJsonResponse != null ? rawJsonResponse : "");
        }

        return responseModel;
    }

    private Set<String> extractUniqueValues(JsonElement element, String[] pathParts) {
        Set<String> resultSet = new LinkedHashSet<>();
        extractRecursive(element, pathParts, 0, resultSet);
        return resultSet;
    }

    private void extractRecursive(JsonElement element, String[] pathParts, int depth, Set<String> resultSet) {
        if (element == null || element.isJsonNull()) return;

        if (depth == pathParts.length) {
            if (element.isJsonPrimitive()) resultSet.add(element.getAsString());
            else if (element.isJsonArray()) {
                for (JsonElement e : element.getAsJsonArray())
                    if (e.isJsonPrimitive()) resultSet.add(e.getAsString());
            } else resultSet.add(element.toString());
            return;
        }

        String key = pathParts[depth];
        boolean isArrayWildcard = key.endsWith("[*]");
        boolean isIndexedArray = key.matches(".+\\[\\d+]");

        String cleanKey = key.replaceAll("\\[.*]", "");

        if (element.isJsonObject() && element.getAsJsonObject().has(cleanKey)) {
            JsonElement child = element.getAsJsonObject().get(cleanKey);
            if (isArrayWildcard && child.isJsonArray()) {
                for (JsonElement item : child.getAsJsonArray())
                    extractRecursive(item, pathParts, depth + 1, resultSet);
            } else if (isIndexedArray && child.isJsonArray()) {
                int index = Integer.parseInt(key.replaceAll(".*\\[(\\d+)]", "$1"));
                if (index < child.getAsJsonArray().size())
                    extractRecursive(child.getAsJsonArray().get(index), pathParts, depth + 1, resultSet);
            } else {
                extractRecursive(child, pathParts, depth + 1, resultSet);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray())
                extractRecursive(item, pathParts, depth, resultSet);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter JSON URL: ");
        String url = scanner.nextLine();
        System.out.print("Enter Field Path: ");
        String path = scanner.nextLine();

        Dynamic_Json_Field_Extractor fetcher = new Dynamic_Json_Field_Extractor();
        NlpRequestModel model = new NlpRequestModel();
        Map<String, Object> input = new HashMap<>();
        input.put("JSON URL", url);
        input.put("Field Path", path);
        model.setAttributes(input);

        try {
            NlpResponseModel response = fetcher.execute(model);
            System.out.println("\nStatus: " + response.getStatus());
            System.out.println("\nUnique Values:\n" + response.getAttributes().get("Unique Values"));
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}
