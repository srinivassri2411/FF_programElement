package specializedAutomation_Utility;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;
 



@Slf4j

public class DBToExcel implements Nlp {
    @InputParams({@InputParam(name = "Connection", type = "java.sql.Connection"),@InputParam(name = "Query", type = "java.lang.String")})


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
		Connection	connection=(Connection)attributes.get("Connection");
		String	query=(String)attributes.get("Query");
	System.setProperty("java.awt.headless", "false");
	 try {
	       Instant now = Instant.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss").withZone(ZoneOffset.systemDefault());
	        String timestamp = formatter.format(now);
	        String fileName = "DB_Data_" + timestamp + ".xlsx";
	        Path filePath = Path.of(System.getProperty("user.dir").replaceAll("\\\\", "/")+"/"+fileName);
	 Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.TYPE_SCROLL_INSENSITIVE);
			ResultSet result = statement.executeQuery(query);

			 Workbook workbook = new XSSFWorkbook();
			    Sheet sheet = workbook.createSheet("Sheet1");
			    ResultSetMetaData metaData = result.getMetaData();
			    int columnCount = metaData.getColumnCount();
			    Row headerRow = sheet.createRow(0);

			    for (int i = 1; i <= columnCount; i++) {
			        headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
			    }

			    int rowCount = 1;
			    while (result.next()) {
			        Row row = sheet.createRow(rowCount++);

			        for (int i = 1; i <= columnCount; i++) {
			            row.createCell(i - 1).setCellValue(result.getString(i));
			        }
			    }
			    try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
			        workbook.write(outputStream);
			    }
	        	Desktop.getDesktop().open(new File(filePath.toString()));
	            nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("DB Data stored at "+filePath);

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
