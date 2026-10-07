package logics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v142.network.Network;
import org.openqa.selenium.devtools.v142.network.model.Response;
import org.openqa.selenium.remote.Augmenter;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

public class ToWriteLogs implements Nlp {
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
		varun.s = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		Augmenter dri = new Augmenter();
		DevTools tools = ((HasDevTools) dri.augment(nlpRequestModel.getWebDriver())).getDevTools();
		tools.createSession();
		try {
			tools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty()));
			StringBuilder s = new StringBuilder();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Enabled DevTools");
			tools.addListener(Network.responseReceived(), responseReceived -> {
				try {
					Response res = responseReceived.getResponse();
					String responseBody = "";
					responseBody = tools.send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
					s.append(res.getUrl() + "::varunkumargowda::" + responseBody + "::varunkumargowda::"
							+ res.getStatus() + "::gowdakumarvarun::");
				} catch (Exception e) {

				}
				varun.s = s;
			});
		} catch (Exception e) {
		}
		return nlpResponseModel;
	}
}

class varun {
	static StringBuilder s;
}
