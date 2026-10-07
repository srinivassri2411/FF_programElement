
package integrationLogics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CheckStepResults implements Nlp {
	@InputParams({ @InputParam(name = "Script Id", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String scriptId = (String) programElementsInput.get("Script Id");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
			MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
					"Optimize_Dev_client");
			Query query = new Query();
			query.addCriteria(Criteria.where("scriptRunDetails.scriptId").is(scriptId));
			List<Document> stepResults = mongoTemplate.find(query, Document.class, "StepResult");
			ArrayList<String> allResults = new ArrayList<>();
			for (Document doc : stepResults) {
				System.out.println(doc.toJson());
				allResults.add(JsonPath.read(doc.toJson(), "$.status"));
			}
			mongoClient.close();
			System.out.println(allResults);
			if (allResults.contains("FAIL")) {
				returnValue = "FAIL";
			} else if (allResults.contains("PASS")) {
				returnValue = "PASS";
			} else if (allResults.contains("WARNING")) {
				returnValue = "WARNING";
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully checked step results");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to check step results " + e);
		}
		nlpResponseModel.getAttributes().put("result", returnValue);
		return nlpResponseModel;
	}
}