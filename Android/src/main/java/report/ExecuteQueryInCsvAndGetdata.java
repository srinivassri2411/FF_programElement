package report;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ExecuteQueryInCsvAndGetdata implements Nlp {
	@InputParams({ @InputParam(name = "CSV filepath", type = "java.lang.String"),
			@InputParam(name = "Query", type = "java.lang.String") })
	@ReturnType(name = "Sum of Two numbers", type = "java.lang.String")

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
		String path = (String) attributes.get("CSV filepath");
		String query = (String) attributes.get("Query");
		// String jdbcURL = "jdbc:mysql://49.249.29.6:3333/projects";
		// String username = "root@%";
		// String password = "root";
		// String csvFilePath = "output.csv";
		String returnValue = null;

		try {

			// Load H2 driver
			Class.forName("org.h2.Driver");

			// Connect to H2 in-memory DB
			Connection conn = DriverManager.getConnection("jdbc:h2:mem:");

			Statement stmt = conn.createStatement();

			// Change the file path here
			String filePath = path;

			// Execute query to create a CSV-readable table
			String createTableSQL = "CREATE TABLE my_table AS SELECT * FROM CSVREAD('" + filePath + "');";
			stmt.execute(createTableSQL);

			// Example query: get count
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
//				System.out.println("Row count: " + rs.getInt(1));
				returnValue = rs.getInt(1) + "";
			}

			conn.close();

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully executed the query");
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to execute" + e);
		}

		nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
		return nlpResponseModel;
	}
}