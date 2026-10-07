
package ecommerce;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.bson.Document;

public class MongoUpdateZone implements Nlp {
	@InputParams({ @InputParam(name = "Connection String", type = "java.lang.String"),
		@InputParam(name = "Database Name", type = "java.lang.String"),
		@InputParam(name = "Collection Name", type = "java.lang.String"),
		@InputParam(name = "Filter Key1", type = "java.lang.String"),
		@InputParam(name = "Filter Key1 Value", type = "java.lang.String"),
		@InputParam(name = "Filter Key2", type = "java.lang.String"),
		@InputParam(name = "Filter Key2 Value", type = "java.lang.String"),
		@InputParam(name = "Key to Update", type = "java.lang.String"),
		@InputParam(name = "Value to Update", type = "java.lang.String")})
//	@ReturnType(name = "boolean", type = "java.lang.Boolean")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
			String connectionString = (String) programElementsInput.get("Connection String");
			String databaseName = (String) programElementsInput.get("Database Name");
			String collectionName = (String) programElementsInput.get("Collection Name");
			String filterKey1 = (String) programElementsInput.get("Filter Key1");
			String filterKey1Value = (String) programElementsInput.get("Filter Key1 Value");
			String filterKey2 = (String) programElementsInput.get("Filter Key2");
			String filterKey2Value = (String) programElementsInput.get("Filter Key2 Value");
			String keyToUpdate = (String) programElementsInput.get("Key to Update");
			String valueToUpdate = (String) programElementsInput.get("Value to Update");

      
        	    ConnectionString connString = new ConnectionString(connectionString);
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connString)
                        .build();

                try (MongoClient mongoClient = MongoClients.create(settings)) {
                    MongoDatabase database = mongoClient.getDatabase(databaseName);
                    MongoCollection<Document> collection = database.getCollection(collectionName);

                    Document filter = new Document(filterKey1, filterKey1Value)
                                            .append(filterKey2, filterKey2Value);

                    Document update = new Document("$set", new Document(keyToUpdate, valueToUpdate));

                    UpdateResult result = collection.updateOne(filter, update);

                    if (result.getMatchedCount() > 0) {
                    	 nlpResponseModel.setMessage("Document updated successfully.");
                       nlpResponseModel.setStatus(CommonConstants.pass);
                    } else {
                   	 nlpResponseModel.setMessage("No matching document found or Filter is not Proper");
                     nlpResponseModel.setStatus(CommonConstants.pass);
                    }

          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
           	 nlpResponseModel.setMessage("Failed to Update the Collection");

          }

          return nlpResponseModel;
      }

	}