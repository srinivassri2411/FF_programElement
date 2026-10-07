package parsing_utility;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import lombok.extern.slf4j.Slf4j;

@Slf4j


public class ExcelToJSON implements Nlp {
    @InputParams({@InputParam(name = "Excel File Stream", type = "java.io.InputStream"),@InputParam(name = "Sheet Name", type = "java.lang.String")})


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
	InputStream	stream=(InputStream)attributes.get("Excel File Stream");
	String	sheetName=(String)attributes.get("Sheet Name");

    System.setProperty("java.awt.headless", "false");
	 try {
		  
				 Workbook workbook = new XSSFWorkbook(stream);

	            Sheet sheet = workbook.getSheet(sheetName);

	            JsonObject jsonObject = new JsonObject();

	            Iterator<Row> rowIterator = sheet.iterator();

	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();

	                Cell keyCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                String key = keyCell.toString();

	                JsonArray valuesArray = new JsonArray();

	                for (int i = 1; i < row.getLastCellNum(); i++) {
	                    Cell dataCell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    if (dataCell.getCellType() == CellType.NUMERIC) {
	                        valuesArray.add(new DataFormatter().formatCellValue(dataCell));
	                    } else {
	                        valuesArray.add(dataCell.toString());
	                    }
	                }

	                if (valuesArray.size() > 1) {
	                    jsonObject.add(key, valuesArray);
	                } else {
	                    jsonObject.addProperty(key, valuesArray.get(0).getAsString());
	                }
	            }

	            Gson gson = new Gson();
	            String json = gson.toJson(jsonObject);
	            System.out.println(json);

	            workbook.close();
	           stream.close();
	            
	            
	            Instant now = Instant.now();
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss").withZone(ZoneOffset.systemDefault());
	            String timestamp = formatter.format(now);
	            String fileName = "Parsed_json_" + timestamp + ".json";
	            Path filePath = Path.of(System.getProperty("user.dir").replaceAll("\\\\", "/")+"/"+fileName);
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
		
}
