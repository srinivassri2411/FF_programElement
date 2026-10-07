package bfsi_trading;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CheckIfDocumentExistsOneFilter implements Nlp {
	@InputParams({@InputParam(name = "Connection String", type = "java.lang.String"),
		@InputParam(name = "Database Name", type = "java.lang.String"),
		@InputParam(name = "Collection Name", type = "java.lang.String"),
		@InputParam(name = "Filter Key1", type = "java.lang.String"),
		@InputParam(name = "Filter Key1 Value", type = "java.lang.String")

	})
	@ReturnType(name = "IsDocumentExists", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String connectionString = (String) programElementsInput.get("Connection String");
		String databaseName = (String) programElementsInput.get("Database Name");
		String collectionName = (String) programElementsInput.get("Collection Name");
		String filterKey1 = (String) programElementsInput.get("Filter Key1");
		String filterKey1Value = (String) programElementsInput.get("Filter Key1 Value");

		Boolean returnValue=null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue=checkIfDocumentExists(connectionString, databaseName, collectionName, filterKey1, filterKey1Value);
		
			if(returnValue) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Document is present");	
			}
			else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Document is not present");
			}
		}
		catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			nlpResponseModel.setStatus(CommonConstants.fail);
   	 nlpResponseModel.setMessage("Failed to check the document in Collection : "+exceptionAsString);


		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("IsDocumentExists", returnValue);
		return nlpResponseModel;
	}
	public static boolean checkIfDocumentExists(String connectionString,
			String databaseName,
			String collectionName,
			String filterKey1, Object filterKey1Value) {
		try (MongoClient mongoClient = MongoClients.create(connectionString)) {

			MongoDatabase database = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> collection = database.getCollection(collectionName);

			Bson filter = Filters.and(
					Filters.eq(filterKey1, filterKey1Value)
					);

			// Efficient existence check (no need to fetch document)
			return collection.find(filter).limit(1).first() != null;
		}
	}

} 