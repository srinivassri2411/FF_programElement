package reusable_utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mysql.cj.jdbc.Driver;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class MySql_ConnectToDB implements Nlp {
	@InputParams({ @InputParam(name = "DB URL", type = "java.lang.String"),
		@InputParam(name = "DB Username", type = "java.lang.String"),
		@InputParam(name = "DB Password", type = "java.lang.String") })
	@ReturnType(name = "Connection", type = "java.sql.Connection")

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

	private Connection connectToDB(String url, String username, String password) throws SQLException {
		Driver driver = new Driver();
		DriverManager.registerDriver(driver);
		Connection connection = DriverManager.getConnection(url, username, password);
		return connection;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();

		try {
			String dbURL = (String) attributes.get("DB URL");
			String dbUsername = (String) attributes.get("DB Username");
			String dbPassword = (String) attributes.get("DB Password");
			
			Connection connection = connectToDB(dbURL, dbUsername, dbPassword);
			nlpResponseModel.getAttributes().put("Connection", connection);

			nlpResponseModel.setMessage("Connected to DB");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (SQLException e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("DB Connection Failed... " + writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}
}