
package rabbitmq_utility;

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

import lombok.extern.slf4j.Slf4j;
@Slf4j

public class ProducingMessage implements Nlp {
    @InputParams({@InputParam(name = "Connection", type = "com.rabbitmq.client.Connection"), @InputParam(name = "Queue Name", type = "java.lang.String"), @InputParam(name = "Message to be Added", type = "java.lang.String")})
  //  @ReturnType(name = "Channel", type = "com.rabbitmq.client.Channel")

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
          Connection connection = (Connection) attributes.get("Connection");
          String queue = (String) attributes.get("Queue Name");
          String msg = (String) attributes.get("Message to be Added");
          
      try {
    	 
    	  Channel  channel = connection.createChannel();
    	  channel.queueDeclare(queue, true, false, false, null);
          
          // Publish a message to the queue
         // String message = "Welcome to, RabbitMQ  !";
          channel.basicPublish("", queue, null, msg.getBytes());
          nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage(msg+" Message Published");
  channel.close();
  connection.close();
      }
      catch(Exception e) {
    		StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed  " + exceptionAsString);
 
      }

        
          return nlpResponseModel;
      }
  } 