
package tataPlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClients;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMongo implements Nlp {
	@InputParams({ @InputParam(name = "Number 1", type = "java.lang.Integer") })
	@ReturnType(name = "Return", type = "java.util.List")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Integer number1 = (Integer) programElementsInput.get("Number 1");
		List<String> returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getScriptDetails(nlpRequestModel.getExecutionIdentifiers().get("executionId"),
					nlpRequestModel.getExecutionIdentifiers().get("scriptId"), number1);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Return", returnValue);
		return nlpResponseModel;
	}

	public static ArrayList<String> getScriptDetails(String executionId, String scriptId, Integer ittCount) throws Exception {
		ArrayList<String> data = new ArrayList<>();
		MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"),
				"Optimize_Dev_client");
		Query query = new Query();
		query.addCriteria(Criteria.where("scriptRunDetails.executionId").is(executionId));
		query.addCriteria(Criteria.where("scriptRunDetails.scriptId").is(scriptId));
		List<Document> stepResultsList = mongoTemplate.find(query, Document.class, "StepResult");
		stepResultsList.stream().forEach(value -> data.add(value.toJson()));
//		List<Map> stepResults = mongoTemplate.find(query, Map.class, "StepResult");
//		log.info("stepResults" + stepResults);
//		for (Document doc : stepResults) {
//			try {
//				if (JsonPath.read(doc.toJson(), "$.iteration").toString().equals(ittCount + "")) {
//					data.add(doc.toJson());
//				}
//			} catch (Exception e) {
//				log.info(e + "");
//			}
//		}
		return data;
	}
}