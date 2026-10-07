package ssh_utility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class SSH_DisConnect implements Nlp {
	@InputParams({@InputParam(name = "session", type = "com.jcraft.jsch.Session")})

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

		Session session = (Session) attributes.get("session");
		try {
			session.disconnect();
			nlpResponseModel.setMessage("Disconnected from Unix box");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to Disconnect from Unix box");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}
} 