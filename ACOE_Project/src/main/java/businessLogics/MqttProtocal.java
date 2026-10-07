package businessLogics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.json.JSONObject;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class MqttProtocal implements Nlp {
//	private static final String ACCESS_TOKEN = "mZyRAG9in1pI0qwTu25k";
	
    @InputParams({@InputParam(name = "SchemaFilePath", type = "java.lang.String"),
    	          @InputParam(name = "ACCESS_TOKEN", type = "java.lang.String"),
                  @InputParam(name = "ConfigPropertites", type = "java.lang.String"),
                  @InputParam(name = "logFile", type = "java.lang.String"),
                  @InputParam(name = "Iteration", type = "java.lang.Integer"),
                  @InputParam(name = "Frequency(ms)", type = "java.lang.Integer"),})

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String SchemaFilePath = (String) programElementsInput.get("SchemaFilePath");
          String ACCESS_TOKEN = (String) programElementsInput.get("ACCESS_TOKEN");
          String ConfigPropertites = (String) programElementsInput.get("ConfigPropertites");
          String logFile = (String) programElementsInput.get("logFile");
          Integer Iteration = (Integer) programElementsInput.get("Iteration");
          Integer Frequency = (Integer) programElementsInput.get("Frequency(ms)");
      	  String API_URL = "http://thingsboard.cloud/api/v1/" + ACCESS_TOKEN + "/telemetry";

          try {
        	  
      			String schemaJson = new String(Files.readAllBytes(new File(SchemaFilePath).toPath()));
      			JSONObject schema = new JSONObject(schemaJson);

      			Properties config = new Properties();
      			config.load(new FileInputStream(ConfigPropertites));

      			Random random = new Random();

      			int count = 0;
    			FileWriter fw=new FileWriter(new File(logFile));
      			while (count <Iteration ) {
      				JSONObject payload = new JSONObject();

      				for (String key : schema.keySet()) {
      					String varName = schema.getString(key);
      					String range = config.getProperty(varName);

      					if (range == null || !range.contains("-")) {
      						System.err.println("Invalid or missing range for " + varName);
      						continue;
      					}
      					String[] minMax = range.split("-");
      					double min = Double.parseDouble(minMax[0]);
      					double max = Double.parseDouble(minMax[1]);
      					double value = min + (max - min) * random.nextDouble();
      					payload.put(key, Math.round(value * 100.0) / 100.0); // round to 2 decimal places
      					sendPostRequest(API_URL, payload.toString());
      				}
      				System.out.println("Sent: " + payload);
                    fw.write(payload.toString() + System.lineSeparator());
      				Thread.sleep(Frequency);
      				count++;
      			} 		
				fw.close();

              nlpResponseModel.setMessage("Successfully pushed data to thingsCloud");
              nlpResponseModel.setStatus(CommonConstants.pass);
              }
            catch(Exception e) {
              nlpResponseModel.setMessage("Failed to push data to thingsCloud" +e);
              nlpResponseModel.setStatus(CommonConstants.fail);
              throw new NlpException(e.getMessage().toString());
              }
          return nlpResponseModel;
      }
    private static void sendPostRequest(String apiUrl, String jsonPayload) throws Exception {
		URL url = new URL(apiUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);

		try (OutputStream os = conn.getOutputStream()) {
			os.write(jsonPayload.getBytes());
			os.flush();
		}

		int responseCode = conn.getResponseCode();
		if (responseCode != 200) {
			System.out.println("Failed to send data. HTTP response code: " + responseCode);
		}

		conn.disconnect();
	}
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
      public static void main(String[] args) throws NlpException {
		MqttProtocal mq=new MqttProtocal();
		NlpRequestModel nlp=new NlpRequestModel();
        nlp.getAttributes().put("SchemaFilePath", "C:\\Users\\Chandan\\Documents\\schema.json");
        nlp.getAttributes().put("ConfigPropertites", "C:\\Users\\Chandan\\Documents\\config.properties");
        nlp.getAttributes().put("Iteration", 5);
        nlp.getAttributes().put("Frequency(ms)", 300);
        mq.execute(nlp);  
	}
  } 