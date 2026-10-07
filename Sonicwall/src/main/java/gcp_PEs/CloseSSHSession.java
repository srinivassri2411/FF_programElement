
package gcp_PEs;

import java.util.Map;
import net.schmizz.sshj.connection.channel.direct.Session;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class CloseSSHSession implements Nlp {
	@InputParams({ @InputParam(name = "CMD Session", type = "net.schmizz.sshj.connection.channel.direct.Session") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Session sSHConnection = (Session) programElementsInput.get("CMD Session");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			// Your program element business logic starts here ...
			sSHConnection.close();
			nlpResponseModel.setMessage("Session closed");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			// Your program element Exception handling goes here ...
			nlpResponseModel.setMessage("Failed to close - Reason:" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		// Your program element business logic ends here ...
		return nlpResponseModel;
	}

}