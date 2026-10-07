package mysql_utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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




public class MySql_GetDataFromDbAsListOfMap implements Nlp {
	@InputParams({@InputParam(name = "connection", type = "java.sql.Connection"), @InputParam(name = "Query", type = "java.lang.String")})
	@ReturnType(name = "list", type = "java.util.List")

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

	private List<Map<String, Object>> getDataFromDbAsListOfMap(Connection connection, String query) throws SQLException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.TYPE_SCROLL_INSENSITIVE);
		ResultSet result = statement.executeQuery(query);

		while (result.next()) {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			int numColumns = result.getMetaData().getColumnCount();

			for (int i = 1; i <= numColumns; i++) {
				map.put(result.getMetaData().getColumnName(i),result.getObject(i));
			}
			list.add(map);
		}
		return list;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Connection connection = (Connection) attributes.get("connection");
		String query = (String) attributes.get("Query");

		// Your program element business logic goes here ...
		List<Map<String, Object>> list = null;
		try {
			list = getDataFromDbAsListOfMap(connection,query);
			nlpResponseModel.setMessage("Data Fetched from DB Successfully...");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (SQLException e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to Fetch data from DB... "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("list", list);
		return nlpResponseModel;
	}
} 