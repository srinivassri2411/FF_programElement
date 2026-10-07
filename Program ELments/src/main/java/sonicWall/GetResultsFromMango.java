
package sonicWall;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class GetResultsFromMango implements Nlp {
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
			MongoDatabase database = mongoClient.getDatabase("Optimize_Dev_client");
			MongoCollection<Document> collection = database.getCollection("testCollection");
			//
			MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"), // Mongo
																												// URI
					"Optimize_Dev_client" // Replace with actual DB name
			);
			Query query = new Query();
//              query.addCriteria(Criteria.where("key").is(scriptId));

			List<Document> result = mongoTemplate.find(query, Document.class, "ExecutionEntityResponse");
			StringBuilder builder = new StringBuilder();
			for (Document doc : result) {
				builder.append(doc.toJson()).append("\n\n");
			}
			mongoClient.close();
			returnValue = builder.toString();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("pass");
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Faill");
		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("result", returnValue);
		return nlpResponseModel;
	}

}