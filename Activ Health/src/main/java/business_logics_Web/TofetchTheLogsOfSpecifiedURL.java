package business_logics_Web;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class TofetchTheLogsOfSpecifiedURL extends varun implements Nlp {
	@InputParams({ @InputParam(name = "ExpectedURL", type = "java.lang.String") })
	@ReturnType(name = "urlee", type = "java.util.List")

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
		String ExpectedURL = (String) attributes.get("ExpectedURL");

		List<String> urlee = new ArrayList<String>();

		String fileContent = "";
		try {

			try {

				fileContent = varun.s.toString();
				Path logFilePath = Paths.get(System.getProperty("user.home"), "network_log.txt");

				PrintWriter logWriter = new PrintWriter(new FileWriter(logFilePath.toFile()));
				logWriter.println(fileContent);
				logWriter.flush();
				logWriter.close();

				String arr1[] = fileContent.split("::gowdakumarvarun::");

				for (int i = 0; i < arr1.length; i++) {
                       String arr2[]=      arr1[i].split("::varunkumargowda::");
                       if (arr2[0].toLowerCase().contains(ExpectedURL.toLowerCase())) {
                    	   urlee.add(arr2[1]);
					}
                       
				}

				if (urlee.size() == 0) {
					nlpResponseModel.setStatus(CommonConstants.fail);
					nlpResponseModel.setMessage("No data found for Particular Instance for the Given URL");
				} else {

					nlpResponseModel.setStatus(CommonConstants.pass);
					nlpResponseModel.setMessage("Successfully fetched logs for particular URL "+ExpectedURL);
				}

			} catch (Exception e) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("NO data found" + e.getMessage());

			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Exception");
		}

		nlpResponseModel.getAttributes().put("urlee", urlee);
		return nlpResponseModel;
	}
}

