package reusable_utility;

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




public class MySql_ExecuteQuery implements Nlp {
	@InputParams({@InputParam(name = "Connection", type = "java.sql.Connection"), @InputParam(name = "Query", type = "java.lang.String")})
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

	private boolean executeQuery(Connection connection, String query) throws SQLException {
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.TYPE_SCROLL_INSENSITIVE);
		boolean flag = statement.execute(query);
		return flag;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Connection connection = (Connection) attributes.get("Connection");
		String query = (String) attributes.get("Query");

		boolean flag = false;
		// Your program element business logic goes here ...
		try {
			flag = executeQuery(connection,query);
			nlpResponseModel.setMessage("Query Executed Successfully...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (SQLException e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to Execute Query... "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("boolean", flag);
		return nlpResponseModel;
	}
} 