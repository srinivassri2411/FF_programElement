package locobuzz;

import java.io.PrintWriter;
import java.io.StringWriter;
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

import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;


import org.springframework.stereotype.Component;

@Slf4j
@Component("LIC11870_PJT1012_PE_NLPf738673d-3f31-4e50-aa88-dff7d65f9cbc")
public class GetRequestPayload implements Nlp {
	@InputParams({ @InputParam(name = "Proxy Object", type = "net.lightbody.bmp.BrowserMobProxy"),
			@InputParam(name = "URL", type = "java.lang.String") })
	@ReturnType(name = "proxyData", type = "java.util.List")

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
		List<String> allResponseBody = new ArrayList<String>();
		try {
			BrowserMobProxy proxy = (BrowserMobProxy) attributes.get("Proxy Object");
			String url = (String) attributes.get("URL");
			Har har1 = proxy.getHar();
			for (HarEntry entry : har1.getLog().getEntries()) {
				if (entry.getRequest().getUrl().contains(url)) {
					String requestPayLoad = entry.getRequest().getPostData().getText();
					allResponseBody.add(requestPayLoad);
				}
			}
			if (allResponseBody.size() != 0) {
				nlpResponseModel.setMessage("Successfully fetched the request payload from given url");
				nlpResponseModel.setStatus(CommonConstants.pass);
			} else {
				nlpResponseModel.setMessage("url is not present in the request payload");
				nlpResponseModel.setStatus(CommonConstants.fail);
			}
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			log.error(exception.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed " + exception.toString());
		}
		nlpResponseModel.getAttributes().put("proxyData", allResponseBody);
		return nlpResponseModel;
	}
}