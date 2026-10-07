package telusLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




import org.springframework.stereotype.Component;

@Component("LIC11870_PJT1012_PE_NLPd58a66af-46e5-4274-8426-a2007ac3bc45")
public class GetRandomNumberBetweenRange implements Nlp {
	@InputParams({ @InputParam(name = "Minimum", type = "java.lang.Integer"),
			@InputParam(name = "Maximum", type = "java.lang.Integer") })
	@ReturnType(name = "Return", type = "java.lang.Integer")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Integer min = (Integer) programElementsInput.get("Minimum");
		Integer max = (Integer) programElementsInput.get("Maximum");
		int randomInteger = 0;
		try {
			randomInteger = getRandomInteger(min, max);
			System.out.println("Random Integer: " + randomInteger);
			nlpResponseModel.setMessage("Successfully fetched the random number " + randomInteger);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Fail to fetch" + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("Return", randomInteger);
		return nlpResponseModel;
	}

	public static int getRandomInteger(int min, int max) {
		Random random = new Random();
		return random.nextInt((max - min) + 1) + min;
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

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("Minimum", 0);
		req.getAttributes().put("Maximum", 5);
		System.out.println(new GetRandomNumberBetweenRange().execute(req));
	}
}