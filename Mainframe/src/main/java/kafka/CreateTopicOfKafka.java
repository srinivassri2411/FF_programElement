package kafka;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class CreateTopicOfKafka implements Nlp {

    @InputParams({
            @InputParam(name = "KafkaPath", type = "java.lang.String"),
            @InputParam(name = "topic", type = "java.lang.String"),
            @InputParam(name = "bootstrap path", type = "java.lang.String")
    })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> input = nlpRequestModel.getAttributes();

        String kafkaPath = (String) input.get("KafkaPath");
        String topic = (String) input.get("topic");
        String bootstrap = (String) input.get("bootstrap path");

        NlpResponseModel response = new NlpResponseModel();

        StringBuilder output = new StringBuilder();

        try {

            String scriptPath = kafkaPath + "\\bin\\windows\\kafka-topics.bat";
            System.out.println("Executing: " + scriptPath);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "cmd.exe", "/c", "call",
                    scriptPath,
                    "--create",
                    "--topic", topic,
                    "--bootstrap-server", bootstrap,
                    "--partitions", "1",
                    "--replication-factor", "1"
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // READ OUTPUT (VERY IMPORTANT)
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("KAFKA >> " + line);
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            System.out.println("EXIT CODE: " + exitCode);

            String result = output.toString().toLowerCase();

            // ---------------- LOGIC FIX ----------------

            if (result.contains("already exists")) {

                response.setStatus(CommonConstants.pass);
                response.setMessage("Topic already exists: " + topic + "\n\nOutput:\n" + output);

            } else if (result.contains("created")) {

                response.setStatus(CommonConstants.pass);
                response.setMessage("Topic created successfully: " + topic + "\n\nOutput:\n" + output);

            } else if (exitCode != 0) {

                response.setStatus(CommonConstants.fail);
                response.setMessage("Topic creation failed:\n" + output);

            } else {

                response.setStatus(CommonConstants.pass);
                response.setMessage("Kafka executed successfully:\n" + output);
            }

        } catch (Exception e) {

            response.setStatus(CommonConstants.fail);
            response.setMessage("Exception while creating topic: " + e.getMessage());
        }

        return response;
    }

    public static void main(String[] args) throws NlpException {

        Map<String, Object> input = new HashMap<>();
        input.put("bootstrap path", "localhost:9092");
        input.put("KafkaPath", "C:\\kafka_2.13-4.1.0");
        input.put("topic", "orde");

        NlpRequestModel model = new NlpRequestModel();
        model.setAttributes(input);

        CreateTopicOfKafka obj = new CreateTopicOfKafka();
        NlpResponseModel response = obj.execute(model);

        System.out.println("Status: " + response.getStatus());
        System.out.println("Message: " + response.getMessage());
    }
}