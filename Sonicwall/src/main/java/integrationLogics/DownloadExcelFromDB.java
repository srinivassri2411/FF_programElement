
package integrationLogics;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DownloadExcelFromDB implements Nlp {
//	private static final String URL = "jdbc:mysql://localhost:3306/AutomationDB";
//	private static final String USER = "root";
//	private static final String PASSWORD = "root";
	private static String URL;
	private static String USER;
	private static String PASSWORD;

	@InputParams({ @InputParam(name = "DB URL", type = "java.lang.String"),
			@InputParam(name = "DB User Name", type = "java.lang.String"),
			@InputParam(name = "DB Password", type = "java.lang.String"),
			@InputParam(name = "Query", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String query = (String) programElementsInput.get("Query");
		URL = (String) programElementsInput.get("DB URL");
		USER = (String) programElementsInput.get("DB User Name");
		PASSWORD = (String) programElementsInput.get("DB Password");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String excelFilePath = null;
		try {
			String folderPath = System.getProperty("user.home") + File.separator + "Fireflink_Reports";
			File folderPathObj = new File(folderPath);

			if (!folderPathObj.exists()) {
				folderPathObj.mkdirs();
			}

			excelFilePath = Files.createTempFile(folderPathObj.toPath(), "FF_Result_Table", ".xlsx").toAbsolutePath()
					.toString();

			System.out.println(excelFilePath);
			exportToExcel(query, excelFilePath);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fetched the excel report from Database");
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to fetch the excel report from Database " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		ArrayList<String> outputFile = new ArrayList<>();
		outputFile.add(excelFilePath);
		nlpResponseModel.setOutputLocalFilePaths(outputFile);
		return nlpResponseModel;
	}

	public static void exportToExcel(String query, String excelFilePath) throws Exception {

		try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				Workbook workbook = new XSSFWorkbook()) {

			Sheet sheet = workbook.createSheet("Report");

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			Row headerRow = sheet.createRow(0);

			for (int i = 1; i <= columnCount; i++) {
				headerRow.createCell(i - 1).setCellValue(metaData.getColumnLabel(i));
			}
			int rowNum = 1;

			while (rs.next()) {

				Row row = sheet.createRow(rowNum++);

				for (int i = 1; i <= columnCount; i++) {

					Object value = rs.getObject(i);

					if (value != null) {
						row.createCell(i - 1).setCellValue(value.toString());
					} else {
						row.createCell(i - 1).setBlank();
					}
				}
			}
			for (int i = 0; i < columnCount; i++) {
				sheet.autoSizeColumn(i);
			}

			try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
				workbook.write(fos);
			}

			System.out.println("Excel exported successfully : " + excelFilePath);
		}
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Query", "SELECT * FROM sanity_2");
		System.out.println(new DownloadExcelFromDB().execute(model) + "");
	}
}