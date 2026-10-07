package parsing_utility;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import lombok.extern.slf4j.Slf4j;


@Slf4j

public class CSVToJSON implements Nlp {
    @InputParams({@InputParam(name = "CSV File Stream", type = "java.io.InputStream")})


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
	InputStream	stream=(InputStream)attributes.get("CSV File Stream");
    System.setProperty("java.awt.headless", "false");
	 try (
             BOMInputStream bomInputStream = new BOMInputStream(stream);
             InputStreamReader inputStreamReader = new InputStreamReader(bomInputStream);
             CSVReader csvReader = new CSVReader(inputStreamReader)) {
            List<String[]> csvRows = csvReader.readAll();
            Map<String, Object> jsonObject = convertCsvRowsToJsonObject(csvRows);
            Gson gson = new Gson();
            String json = gson.toJson(jsonObject);
            System.out.println(json);
            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss").withZone(ZoneOffset.systemDefault());
            String timestamp = formatter.format(now);
            String fileName = "Parsed_json_" + timestamp + ".json";
            Path filePath = Path.of(System.getProperty("user.dir").replaceAll("\\\\", "/") + "/" + fileName);
            System.out.println(filePath);
            Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
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
		 private static Map<String, Object> convertCsvRowsToJsonObject(List<String[]> csvRows) {
		        Map<String, Object> jsonObject = new HashMap<>();
    for (String[] row : csvRows) {
		            if (row.length > 0) {
		                String key = row[0];

		                if (row.length == 2) {
		                    if (jsonObject.containsKey(key)) {
		                        Object existingValue = jsonObject.get(key)
		;
		                        if (existingValue instanceof String) {
		                            List<String> valueList = new ArrayList<>();
		                            valueList.add((String) existingValue);
		                            valueList.add(row[1]);
		                            jsonObject.put(key, valueList);
		                        } else if (existingValue instanceof List) {
		                            ((List<String>) existingValue).add(row[1]);
		                       }
		                    } else {
		                        jsonObject.put(key, row[1]);
		                    }
		                } else if (row.length > 2) {
		                    List<String> values = new ArrayList<>();
		                    for (int i = 1; i < row.length; i++) {
		                        values.add(row[i]);
		                    }

		                    if (jsonObject.containsKey(key)) {
		                        Object existingValue = jsonObject.get(key)
		;
		                        if (existingValue instanceof String) {
		                            List<String> valueList = new ArrayList<>();
		                            valueList.add((String) existingValue);
		                            valueList.addAll(values);
		                            jsonObject.put(key, valueList);
		                        } else if (existingValue instanceof List) {
		                            ((List<String>) existingValue).addAll(values);
		                        }
		                    } else {
		                        jsonObject.put(key, values);
		                    }
		                }
		            }
		        }

		        return jsonObject;
	}
}
