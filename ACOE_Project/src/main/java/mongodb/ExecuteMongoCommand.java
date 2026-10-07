
package mongodb;

import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ExecuteMongoCommand implements Nlp {
    @InputParams({@InputParam(name = "Mongo Connection", type = "com.mongodb.client.MongoClient"),
    	@InputParam(name = "DB Name", type = "java.lang.String"),
    	@InputParam(name = "JS Command", type = "java.lang.String"),
    	@InputParam(name = "Balance Variable To Set", type = "java.lang.Double")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          MongoClient mongoConnection = (MongoClient) programElementsInput.get("Mongo Connection");
          String dBName = (String) programElementsInput.get("DB Name");
          String jSCommand = (String) programElementsInput.get("JS Command");
          Double balanceVariableToSet = (Double) programElementsInput.get("Balance Variable To Set");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	String s=  executeCommand(mongoConnection, dBName, jSCommand, balanceVariableToSet);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Command executed successfully: " +s);
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          return nlpResponseModel;
      }
    public static String executeCommand(MongoClient client,
            String databaseName,
            String jsonCommand,
            double balanceToSet) {

    try {
    // Step 1: Replace placeholder
    String updatedJson = jsonCommand.replace("balanceVar", String.valueOf(balanceToSet));

    // Step 2: Normalize escaped JSON → proper JSON
    updatedJson = normalizeJson(updatedJson);

    // Step 3: Parse to Document
    Document command = Document.parse(updatedJson);

    // Step 4: Execute command
    MongoDatabase db = client.getDatabase(databaseName);
    Document result = db.runCommand(command);

    System.out.println("Command executed successfully: " + result.toJson());
    return result.toJson().toString();

    } catch (Exception e) {
    throw new RuntimeException("Failed to execute Mongo command", e);
    }
    }

    // 🔥 Handles \\n, \\" etc.
    private static String normalizeJson(String json) {

    // Remove surrounding quotes if present
    if (json.startsWith("\"") && json.endsWith("\"")) {
    json = json.substring(1, json.length() - 1);
    }

    // Replace escaped characters
    json = json.replace("\\n", "")        // remove new lines
    .replace("\\\"", "\"")    // unescape quotes
    .replace("\\\\", "\\");   // unescape backslashes

    return json.trim();
    }
  } 