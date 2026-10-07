package mysql_utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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




public class MySql_VerifyColumnContainsData implements Nlp {
	@InputParams({ @InputParam(name = "Connection", type = "java.sql.Connection"),
			@InputParam(name = "Query", type = "java.lang.String"),
			@InputParam(name = "Column Name", type = "java.lang.String"),
			@InputParam(name = "Expected Data", type = "java.lang.String") })
	@ReturnType(name = "boolean", type = "java.lang.Boolean")

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

	private boolean verifyColumnContainsData(Connection connection, String query, String columnName, String data)
			throws SQLException {
		boolean flag = false;

		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.TYPE_SCROLL_INSENSITIVE);
		ResultSet result = statement.executeQuery(query);

		while (result.next()) {
			String actData = result.getString(columnName);
			if (actData.equalsIgnoreCase(data)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Connection connection = (Connection) attributes.get("Connection");
		String query = (String) attributes.get("Query");
		String columnName = (String) attributes.get("Column Name");
		String data = (String) attributes.get("Expected Data");

		// Your program element business logic goes here ...
		boolean flag = false;
		try {
			flag = verifyColumnContainsData(connection, query, columnName, data);
			if (flag) {
				nlpResponseModel.setMessage("Column Name " + columnName + " Contains Data " + data);
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("Column Name " + columnName + " Does not Contains Data " + data);
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to verify column contains data... " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("boolean", flag);
		return nlpResponseModel;
	}
}