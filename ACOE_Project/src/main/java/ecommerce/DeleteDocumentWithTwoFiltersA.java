
package ecommerce;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class DeleteDocumentWithTwoFiltersA implements Nlp {
	@InputParams({ @InputParam(name = "Connection String", type = "java.lang.String"),
		@InputParam(name = "Database Name", type = "java.lang.String"),
		@InputParam(name = "Collection Name", type = "java.lang.String"),
		@InputParam(name = "Filter Key1", type = "java.lang.String"),
		@InputParam(name = "Filter Key1 Value", type = "java.lang.String"),
		@InputParam(name = "Filter Key2", type = "java.lang.String"),
		@InputParam(name = "Filter Key2 Value", type = "java.lang.String")})

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


			   try (MongoClient mongoClient = MongoClients.create(connectionString)) {
		            MongoDatabase database = mongoClient.getDatabase(databaseName);
		            MongoCollection<org.bson.Document> collection = database.getCollection(collectionName);

		            Bson filter = Filters.and(
		                    Filters.eq(filterKey1, filterKey1Value),
		                    Filters.eq(filterKey2, filterKey2Value)
		            );

		            var result = collection.deleteOne(filter);

	            	 nlpResponseModel.setMessage("Successfully Deleted the document with "+filterKey1+" : "+filterKey1Value+" and "+filterKey2+" : "+filterKey2Value);
                     nlpResponseModel.setStatus(CommonConstants.pass);
                 
			   }     catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
           	 nlpResponseModel.setMessage("Failed to Delete the Collection");

          }

          return nlpResponseModel;
      }
 
	}