
package ssh_utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ReadLogs implements Nlp {
	@InputParams({ @InputParam(name = "Ssh_Session_Object", type = "com.jcraft.jsch.Session"),
		@InputParam(name = "File_Path", type = "java.lang.String"),
		@InputParam(name = "Lines", type = "java.lang.String") })
	@ReturnType(name = "logs", type = "java.lang.String")

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
		try {
			Session session = (Session) attributes.get("Ssh_Session_Object");
			String filePath = (String) attributes.get("File_Path");
			String lines = (String) attributes.get("Lines");
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("tail -n "+lines+" "+filePath+"");

			InputStream in = channel.getInputStream();
			channel.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String logs = "";
			String line;
			while ((line = reader.readLine()) != null) {
				logs = logs + line;
			}

			channel.disconnect();
			nlpResponseModel.getAttributes().put("logs", logs);
			nlpResponseModel.setMessage("Fetched logs successfully.");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to Fetched logs."+writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		
		return nlpResponseModel;
	}
}