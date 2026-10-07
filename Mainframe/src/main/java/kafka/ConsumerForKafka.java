package kafka;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.*;

public class ConsumerForKafka implements Nlp {

    @InputParams({
        @InputParam(name = "bootstrap path", type = "java.lang.String"),
        @InputParam(name = "topic", type = "java.lang.String"),
        @InputParam(name = "group id", type = "java.lang.String")
    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();

        String bootstrapPath = (String) programElementsInput.get("bootstrap path");
        String topic = (String) programElementsInput.get("topic");
        String groupId = (String) programElementsInput.get("group id");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        KafkaConsumer<String, String> consumer = null;

        try {
            // Kafka Consumer Properties
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapPath);
            props.put("group.id", groupId);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("auto.offset.reset", "earliest");

            // Create Consumer
            consumer = new KafkaConsumer<>(props);

            // Subscribe to topic
            consumer.subscribe(Arrays.asList(topic));

            StringBuilder messages = new StringBuilder();

            // Poll messages
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            for (ConsumerRecord<String, String> record : records) {
                messages.append(record.value()).append(" | ");
            }

            if (messages.length() == 0) {
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage("No messages received from topic: " + topic);
            } else {
                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel.setMessage("Messages: " + messages.toString());
            }

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Error: " + e.getMessage());

        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }

        return nlpResponseModel;
    }

    // Optional main method for testing
    public static void main(String[] args) throws NlpException {

        Map<String, Object> Input = new HashMap<String, Object>();
        Input.put("bootstrap path", "localhost:9092");
        Input.put("topic", "orde");
        Input.put("group id", "test-group");

        NlpRequestModel model = new NlpRequestModel();
        model.setAttributes(Input);

        ConsumerForKafka cfk = new ConsumerForKafka();
        NlpResponseModel response = cfk.execute(model);

        System.out.println("Status: " + response.getStatus());
        System.out.println("Message: " + response.getMessage());
    }
}
