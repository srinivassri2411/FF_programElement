package mysql_utility;

import java.sql.Connection;
import java.sql.SQLException;
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




public class MySql_DisconnectFromDB implements Nlp {
	@InputParams({@InputParam(name = "Connection", type = "java.sql.Connection")})

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

	private void disconnectFromDB(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Connection connection = (Connection) attributes.get("Connection");

		// Your program element business logic goes here ...
		try {
			disconnectFromDB(connection);
			nlpResponseModel.setMessage("Disconnected from db");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (SQLException e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to Disconnect from db... "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}
}