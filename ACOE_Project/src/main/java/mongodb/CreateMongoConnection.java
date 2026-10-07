
package mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class CreateMongoConnection implements Nlp {
    @InputParams({@InputParam(name = "Connection String", type = "java.lang.String")})
    @ReturnType(name = "Mongo Client", type = "com.mongodb.client.MongoClient")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String connectionString = (String) programElementsInput.get("Connection String");
          MongoClient mongoClient=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	   mongoClient = MongoClients.create(connectionString);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Connection created successfully");
          }
          catch(Exception e) {
        	  StringWriter sw = new StringWriter();
              e.printStackTrace(new PrintWriter(sw));
              String exceptionAsString = sw.toString();
        	  nlpResponseModel.setMessage("Failed to Create Mongo Connection "+exceptionAsString);
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Mongo Client", mongoClient);
          return nlpResponseModel;
      }

  } 