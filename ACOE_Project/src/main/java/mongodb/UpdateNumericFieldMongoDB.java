
package mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import org.bson.Document;

public class UpdateNumericFieldMongoDB implements Nlp {
    @InputParams({@InputParam(name = "Connection String", type = "java.lang.String"),
    	@InputParam(name = "Database Name", type = "java.lang.String"),
    	@InputParam(name = "Collection Name", type = "java.lang.String"),
    	@InputParam(name = "Unique Key", type = "java.lang.String"),
    	@InputParam(name = "Unique Value", type = "java.lang.String"),
    	@InputParam(name = "Key To Update", type = "java.lang.String"),
    	@InputParam(name = "Value To Set", type = "java.lang.Number")

    	
    })
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String connectionString = (String) programElementsInput.get("Connection String");
          String databaseName = (String) programElementsInput.get("Database Name");
          String collectionName = (String) programElementsInput.get("Collection Name");
          String uniqueKey = (String) programElementsInput.get("Unique Key");
          String uniqueValue = (String) programElementsInput.get("Unique Value");
          String keyToUpdate = (String) programElementsInput.get("Key To Update");
          Number valueToSet = (Number) programElementsInput.get("Value To Set");

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try (MongoClient mongoClient = MongoClients.create(connectionString)) {

              MongoDatabase database = mongoClient.getDatabase(databaseName);
              MongoCollection<Document> collection = database.getCollection(collectionName);

              collection.updateOne(
                      eq(uniqueKey, uniqueValue),  
                      set(keyToUpdate, valueToSet) 
              );
              nlpResponseModel.setStatus(CommonConstants.pass);

              nlpResponseModel.setMessage(keyToUpdate+" updated successfully with "+valueToSet);

          } catch (Exception e) {
          	  StringWriter sw = new StringWriter();
        			e.printStackTrace(new PrintWriter(sw));
        			String exceptionAsString = sw.toString();
        			////log.info(exceptionAsString);
        			nlpResponseModel.setStatus(CommonConstants.fail);
              	  nlpResponseModel.setMessage("Failed to update value: "+exceptionAsString);          }
      

          return nlpResponseModel;
      }
    public static void updateNumericField(
            String connectionString,
            String dbName,
            String collectionName,
            String uniqueKey,
            String uniqueValue,
            String keyToUpdate,
            Number valueToSet) {

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {

            MongoDatabase database = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            collection.updateOne(
                    eq(uniqueKey, uniqueValue),   // find document
                    set(keyToUpdate, valueToSet)  // update numeric value
            );

            System.out.println("Field updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  } 