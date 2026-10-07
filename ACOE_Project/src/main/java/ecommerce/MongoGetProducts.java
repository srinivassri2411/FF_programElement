
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
import com.mongodb.client.result.UpdateResult;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class MongoGetProducts implements Nlp {
	@InputParams({ @InputParam(name = "Connection String", type = "java.lang.String"),
		@InputParam(name = "Database Name", type = "java.lang.String"),
		@InputParam(name = "Collection Name", type = "java.lang.String"),
		@InputParam(name = "Filter Key1", type = "java.lang.String"),
		@InputParam(name = "Filter Key1 Value", type = "java.lang.String"),
		@InputParam(name = "Filter Key2", type = "java.lang.String"),
		@InputParam(name = "Filter Key2 Value", type = "java.lang.String")})
	@ReturnType(name = "list", type = "java.util.List")

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

List list=null;
      
        	
                try  {
               list= getProductNames(connectionString, databaseName, collectionName, filterKey1, filterKey1Value, filterKey2, filterKey2Value);
                    	 nlpResponseModel.setMessage("Product list is  "+list);
                       nlpResponseModel.setStatus(CommonConstants.pass);
                    

          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
           	 nlpResponseModel.setMessage("Failed to Update the Collection");

          }
    			nlpResponseModel.getAttributes().put("list", list);

          return nlpResponseModel;
      }
    public static List<String> getProductNames(String uri, String dbName, String collectionName, String filterKey1,String filterKey1Value, String filterKey2,String filterKey2Value) throws Exception {
        List<String> productNames = new ArrayList<>();

        ConnectionString connString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();

        try (MongoClient client = MongoClients.create(settings)) {
            MongoDatabase database = client.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Filter: mailId and role == merchant
            Bson filter = and(eq(filterKey1, filterKey1Value), eq(filterKey2, filterKey2Value));

            Document userDoc = collection.find(filter).first();

            if (userDoc == null) {
            	  throw new Exception("No matching document found.");
    
            }

            Object shopperOrdersObj = userDoc.get("shopperOrders");
            if (shopperOrdersObj instanceof List<?>) {
                List<?> shopperOrders = (List<?>) shopperOrdersObj;
                for (Object orderObj : shopperOrders) {
                    if (orderObj instanceof Document) {
                        Document orderDoc = (Document) orderObj;
                        Object orderedItemsObj = orderDoc.get("orderedItems");

                        if (orderedItemsObj instanceof List<?>) {
                            List<?> orderedItems = (List<?>) orderedItemsObj;
                            for (Object itemObj : orderedItems) {
                                if (itemObj instanceof Document) {
                                    Document itemDoc = (Document) itemObj;
                                    String productName = itemDoc.getString("productName");
                                    if (productName != null) {
                                        productNames.add(productName);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
              throw new Exception("'shopperOrders' is not a list.");
            }
        }

        return productNames;
    }
	}