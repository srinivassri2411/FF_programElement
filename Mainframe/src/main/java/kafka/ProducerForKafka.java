package kafka;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

public class ProducerForKafka implements Nlp {

    @InputParams({
        @InputParam(name = "bootstrap path", type = "java.lang.String"),
        @InputParam(name = "topic", type = "java.lang.String"),
        @InputParam(name = "message", type = "java.lang.String")
    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

        String bootstrapPath = (String) programElementsInput.get("bootstrap path");
        String topic = (String) programElementsInput.get("topic");
        String message = (String) programElementsInput.get("message");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        KafkaProducer<String, String> producer = null;

        try {
            // Kafka Properties
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapPath);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("acks", "all");

            // Create Producer
            producer = new KafkaProducer<>(props);

            // Create Record
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, message);

            // Send Message
            producer.send(record);
            producer.flush();

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Message sent successfully to Kafka topic: " + topic);

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Error: " + e.getMessage());

        } finally {
            if (producer != null) {
                producer.close();
            }
        }

        return nlpResponseModel;
    }
    public static void main(String[] args) throws NlpException {
    	
    	 Map<String, Object> Input= new HashMap<String, Object>();
    	 Input.put("bootstrap path", "localhost:9092");
    	 Input.put("message", "Hii kafka");
    	 Input.put("topic", "orde");
    	 
    	 NlpRequestModel model= new NlpRequestModel();
    	 model.setAttributes(Input);
    	 
    	
    	 ProducerForKafka pfk= new ProducerForKafka();
    	 NlpResponseModel response = pfk.execute(model);
    	 
    	 System.out.println("Status: " + response.getStatus());
    	    System.out.println("Message: " + response.getMessage());
    	 
		
    	
	}
}