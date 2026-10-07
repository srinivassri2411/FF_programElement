
package canvas;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class CanvasExtractData implements Nlp {
	@ReturnType(name = "Extracted Data", type = "java.util.Map")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		WebDriver driver = nlpRequestModel.getWebDriver();
		Map<String, Number> chartMap = new LinkedHashMap<>();
		try {
			JavascriptExecutor jse=(JavascriptExecutor)driver;
	        List<String> labels = (List<String>) jse.executeScript("return Chart.instances[0].data.labels;");
	        List<Object> rawData = (List<Object>) jse.executeScript("return Chart.instances[0].data.datasets[0].data;");
	        for (int i = 0; i < labels.size(); i++) {
	            Number value = (Number) rawData.get(i);
	            chartMap.put(labels.get(i), value);
	        }
	        Map<String, Number> extractedData = chartMap;
	        nlpResponseModel.getAttributes().put("Extracted Data", extractedData);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Extracted data from canvas Element");
		} catch (Exception e) {
			StringWriter writer=new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to extract data from canvas Element "+writer.toString());
		}
		return nlpResponseModel;
	}

}