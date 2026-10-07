package parsing_utility;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import lombok.extern.slf4j.Slf4j;


@Slf4j

public class JSONToCSV implements Nlp {
    @InputParams({@InputParam(name = "JSON File Stream", type = "java.io.InputStream")})


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
	InputStream	stream=(InputStream)attributes.get("JSON File Stream");
	System.setProperty("java.awt.headless", "false");
	 try {
		 String jsonString = getJsonStringFromInputStream(stream);
	        ObjectMapper objectMapper = new ObjectMapper();
	        String jsonData= objectMapper.readTree(jsonString).toString();
	        
	        Instant now = Instant.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss").withZone(ZoneOffset.systemDefault());
	        String timestamp = formatter.format(now);
	        String fileName = "Parsed_csv_" + timestamp + ".csv";
	        Path filePath = Path.of(System.getProperty("user.dir").replaceAll("\\\\", "/")+"/"+fileName);
	System.out.println(filePath);
	     
	        convertJsonToCsv(jsonData, filePath.toString());
	            
	        
	        	Desktop.getDesktop().open(new File(filePath.toString()));
	            nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Parsed file stored at "+filePath);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			//log.info(exceptionAsString);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed  " + exceptionAsString);
		}
	    System.setProperty("java.awt.headless", "true");
		return nlpResponseModel;
		}
    private static void convertJsonToCsv(String jsonData, String outputCsvFilePath) {
        Gson gson = new Gson();

        try (FileWriter fileWriter = new FileWriter(outputCsvFilePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {        	
            Map<String, Object> jsonMap = gson.fromJson(jsonData, Map.class);

            List<String[]> csvRows = convertJsonMapToCsvRows(jsonMap);

            csvWriter.writeAll(csvRows);

            System.out.println("JSON converted to CSV successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String[]> convertJsonMapToCsvRows(Map<String, Object> jsonMap) {
        List<String[]> csvRows = new ArrayList<>();

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
              
                csvRows.add(new String[]{key, (String) value});
            } else if (value instanceof List) {
              
                List<String> values = (List<String>) value;
                String[] row = new String[values.size() + 1];
                row[0] = key;
                for (int i = 0; i < values.size(); i++) {
                    row[i + 1] = values.get(i);
                }
                csvRows.add(row);
            }
        
        }

        return csvRows;
    }
    public static String getJsonStringFromInputStream(InputStream inputStream) {
        StringBuilder jsonString = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
           
        }

        return jsonString.toString();
    }		
}
