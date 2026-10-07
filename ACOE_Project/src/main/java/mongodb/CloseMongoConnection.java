
package mongodb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.mongodb.client.MongoClient;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class CloseMongoConnection implements Nlp {
    @InputParams({@InputParam(name = "Mongo Connection", type = "com.mongodb.client.MongoClient")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          MongoClient mongoConnection = (MongoClient) programElementsInput.get("Mongo Connection");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  mongoConnection.close();
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Connection closed Successfully");
          }
          catch(Exception e) {
       	  StringWriter sw = new StringWriter();
          e.printStackTrace(new PrintWriter(sw));
          String exceptionAsString = sw.toString();
    	  nlpResponseModel.setMessage("Failed to Close Mongo Connection "+exceptionAsString);
         nlpResponseModel.setStatus(CommonConstants.fail);


          }

          return nlpResponseModel;
      }

  } 