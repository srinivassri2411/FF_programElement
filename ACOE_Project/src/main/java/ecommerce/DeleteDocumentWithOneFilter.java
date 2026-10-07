
package ecommerce;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class DeleteDocumentWithOneFilter implements Nlp {
	@InputParams({ @InputParam(name = "Connection String", type = "java.lang.String"),
		@InputParam(name = "Database Name", type = "java.lang.String"),
		@InputParam(name = "Collection Name", type = "java.lang.String"),
		@InputParam(name = "Filter Key1", type = "java.lang.String"),
		@InputParam(name = "Filter Key1 Value", type = "java.lang.String")})

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
			String connectionString = (String) programElementsInput.get("Connection String");
			String databaseName = (String) programElementsInput.get("Database Name");
			String collectionName = (String) programElementsInput.get("Collection Name");
			String filterKey1 = (String) programElementsInput.get("Filter Key1");
			String filterKey1Value = (String) programElementsInput.get("Filter Key1 Value");



			   try (MongoClient mongoClient = MongoClients.create(connectionString)) {
		            MongoDatabase database = mongoClient.getDatabase(databaseName);
		            MongoCollection<org.bson.Document> collection = database.getCollection(collectionName);

		            Bson filter = Filters.and(
		                    Filters.eq(filterKey1, filterKey1Value)
		            );
		            Document latestDoc = collection.find(filter)
		                    .sort(Sorts.descending("_id"))  // sort descending to get the highest ID first
		                    .limit(1)
		                    .first();
		            if (latestDoc != null) {
		           
		                Object idValue = latestDoc.get("_id");               // don't assume ObjectId
		                collection.deleteOne(Filters.eq("_id", idValue));  
		              	 nlpResponseModel.setMessage("Successfully Deleted the document with "+filterKey1+" : "+filterKey1Value );
	                     nlpResponseModel.setStatus(CommonConstants.pass);
		            } else {
		           	 nlpResponseModel.setMessage("No document found matching filters.");
                     nlpResponseModel.setStatus(CommonConstants.fail); }} 
			   catch(Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					nlpResponseModel.setStatus(CommonConstants.fail);
           	 nlpResponseModel.setMessage("Failed to Delete the Collection : "+exceptionAsString);

          }

          return nlpResponseModel;
      }
 
	}