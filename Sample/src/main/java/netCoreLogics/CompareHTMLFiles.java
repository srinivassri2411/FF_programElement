package netCoreLogics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLP304c3b92-245e-4e59-a066-62de5ef7c3e8")
public class CompareHTMLFiles implements Nlp {
	@InputParams({ @InputParam(name = "HTML 1", type = "java.lang.String"),
			@InputParam(name = "HTML 2", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.util.List")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String html1Path = (String) programElementsInput.get("HTML 1");
		String html2Path = (String) programElementsInput.get("HTML 2");
		List<String> resultData = null;
		try {
			Document doc1 = Jsoup.parse(new File(html1Path), "UTF-8");
			Document doc2 = Jsoup.parse(new File(html2Path), "UTF-8");
			String normalizedHtml1 = doc1.toString();
			String normalizedHtml2 = doc2.toString();

			resultData = compareHtml(normalizedHtml1, normalizedHtml2);
			if (resultData.size() >= 1) {
				nlpResponseModel.setMessage("HTML data is not same");
				nlpResponseModel.setStatus(CommonConstants.fail);
			} else {
				nlpResponseModel.setMessage("HTML data is same");
				nlpResponseModel.setStatus(CommonConstants.pass);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Fail to fetch" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", resultData);
		return nlpResponseModel;
	}

	private static List<String> compareHtml(String html1, String html2) {
		List<String> resultData = new ArrayList<String>();
		List<String> original = List.of(html1.split("\n"));
		List<String> revised = List.of(html2.split("\n"));
		Patch<String> patch = DiffUtils.diff(original, revised);
		for (Delta<String> delta : patch.getDeltas()) {
//			System.out.println(delta.getType().name());
			String data = delta.toString();
			resultData.add(
					delta.getType().name() + "-->" + data.substring(data.indexOf(" lines:") + 7, data.length()).trim());
//			System.out.println(data.substring(data.indexOf(" lines:") + 7, data.length()));
		}
		return resultData;
	}

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
}