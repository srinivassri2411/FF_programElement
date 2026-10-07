
package specializedAutomation_Utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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

public class CreatingChannel implements Nlp {
    @InputParams({@InputParam(name = "Host", type = "java.lang.String"), @InputParam(name = "UserName", type = "java.lang.String"), @InputParam(name = "Password", type = "java.lang.String"), @InputParam(name = "Port", type = "java.lang.Integer")})
    @ReturnType(name = "connection", type = "com.rabbitmq.client.Connection")

      @Override
      public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();
        return params;
      }

      @Override
      public StringBuilder getTestCode() throws NlpException {
        StringBuilder sb = new StringBuilder();
        return sb;
      }
      @Override
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> attributes = nlpRequestModel.getAttributes();
          String host = (String) attributes.get("Host");
          String userName = (String) attributes.get("UserName");
          String password = (String) attributes.get("Password");
          int port = (Integer) attributes.get("Port");
         // Channel channel =null;
          Connection connection =null;
      try {
    	  ConnectionFactory    factory = new ConnectionFactory();
    	        
    	        // Provide your RabbitMQ server details
    	        factory.setHost(host);
    	        factory.setPort(port); // Default RabbitMQ port
    	        factory.setUsername(userName);
    	        factory.setPassword(password);

    	        connection=    factory.newConnection();
    	         nlpResponseModel.setStatus(CommonConstants.pass);
       			nlpResponseModel.setMessage("Created a connection" );
      }
      catch(Exception e) {
    		StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
		//	log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed  " + exceptionAsString);

      }

        
          nlpResponseModel.getAttributes().put("connection", connection);
          return nlpResponseModel;
      }
  } 