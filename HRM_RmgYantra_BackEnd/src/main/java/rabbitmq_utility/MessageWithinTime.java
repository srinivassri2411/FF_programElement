
package rabbitmq_utility;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j

public class MessageWithinTime implements Nlp {
    @InputParams({@InputParam(name = "Message to Check", type = "java.lang.String"),@InputParam(name = "Connection", type = "com.rabbitmq.client.Connection"), @InputParam(name = "time", type = "java.lang.Integer"),@InputParam(name = "Queue", type = "java.lang.String")})

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
      static Connection connection ;
   static    Channel channel;
      @Override
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> attributes = nlpRequestModel.getAttributes();
          String MESSAGE_TO_FIND = (String) attributes.get("Message to Check");
          int TIMEOUT_SECONDS = (Integer) attributes.get("time");
          String QUEUE_NAME = (String) attributes.get("Queue");
         connection = (Connection) attributes.get("Connection");
AtomicBoolean messageFound = new AtomicBoolean(false);

          ConnectionFactory factory = new ConnectionFactory();
       
           factory.setHost("49.249.29.5");
           factory.setPort(8104); // Default RabbitMQ port
           factory.setUsername("admin");
           factory.setPassword("admin");
      
          try { connection = factory.newConnection();
                   channel = connection.createChannel();

              DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                  String message = new String(delivery.getBody(), "UTF-8");
                  System.out.println("Consumed: " + message);

                  if (message.equals(MESSAGE_TO_FIND)) {
                      System.out.println("Message found: " + message);
                      messageFound.set(true);
                      // Requeue the message
                      channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                      // Stop consuming further messages
                      channel.basicCancel(consumerTag);
                  } else {
                      // Requeue the message
                      channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                  }
              };

              CancelCallback cancelCallback = consumerTag -> System.out.println("Consumer canceled");

              String consumerTag = channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);

              ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
              scheduler.schedule(() -> {
                  if (!messageFound.get()) {
                      System.out.println("Message not found within the timeout period.");
                      try {
                          channel.basicCancel(consumerTag);
                      } catch (IOException e) {
                          e.printStackTrace();
                      } finally {
                          try {
                              if (channel.isOpen()) {
                                  channel.close();
                              }
                              if (connection.isOpen()) {
                                  connection.close();
                              }
                          } catch (IOException | TimeoutException e) {
                              e.printStackTrace();
                          }
                          scheduler.shutdown();
                      }
                  }
              }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

              while (!messageFound.get() && !scheduler.isTerminated()) {
                  Thread.sleep(100);
              }

              if (!scheduler.isShutdown()) {
                  scheduler.shutdown();
              }
          } catch (Exception e) {
        	
		try {
        	  
			  if (channel.isOpen()) {
                  channel.close();
                  Thread.sleep(200);
              }
              if (connection.isOpen()) {
                  connection.close();
                  Thread.sleep(200);
              }
              
              factory.setHost("49.249.29.5");
              factory.setPort(8104); // Default RabbitMQ port
              factory.setUsername("admin");
              factory.setPassword("admin");
        connection = factory.newConnection();
                     channel = connection.createChannel();
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
              String message = new String(delivery.getBody(), "UTF-8");
              System.out.println("Consumed: " + message);

              if (message.equals(MESSAGE_TO_FIND)) {
                  System.out.println("Message found: " + message);
                  messageFound.set(true);
                  // Requeue the message
                  channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                  // Stop consuming further messages
                  channel.basicCancel(consumerTag);
              } else {
                  // Requeue the message
                  channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
              }
          };

          CancelCallback cancelCallback = consumerTag -> System.out.println("Consumer canceled");

          String consumerTag = channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);

          ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
          scheduler.schedule(() -> {
              if (!messageFound.get()) {
                  System.out.println("Message not found within the timeout period.");
                  try {
                      channel.basicCancel(consumerTag);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  } finally {
                      try {
                          if (channel.isOpen()) {
                              channel.close();
                          }
                          if (connection.isOpen()) {
                              connection.close();
                          }
                      } catch (IOException | TimeoutException e2) {
                          e2.printStackTrace();
                      }
                      scheduler.shutdown();
                  }
              }
          }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

          while (!messageFound.get() && !scheduler.isTerminated()) {
              try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
          }

          if (!scheduler.isShutdown()) {
              scheduler.shutdown();
          }
		}
		catch(Exception e5){
			   if(messageFound.get()==true) {  
	   			   nlpResponseModel.setStatus(CommonConstants.pass);
	   			nlpResponseModel.setMessage(MESSAGE_TO_FIND+" Found in the Queue" );}
	   	      else {
	   	    	  nlpResponseModel.setStatus(CommonConstants.fail);
	   				nlpResponseModel.setMessage(MESSAGE_TO_FIND+" is not found in the Queue" );
	   	      } 
		}// Place your additional logic here
          } finally {
        	  log.info("boolean value is "+messageFound.get());
       	   if(messageFound.get()==true) {  
   			   nlpResponseModel.setStatus(CommonConstants.pass);
   			nlpResponseModel.setMessage(MESSAGE_TO_FIND+" Found in the Queue" );}
   	      else {
   	    	  nlpResponseModel.setStatus(CommonConstants.fail);
   				nlpResponseModel.setMessage(MESSAGE_TO_FIND+" is not found in the Queue" );
   	      }  
          }
          return nlpResponseModel;
      }
  } 