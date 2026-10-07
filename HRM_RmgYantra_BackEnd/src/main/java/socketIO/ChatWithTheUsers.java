package socketIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class ChatWithTheUsers implements Nlp {
	@InputParams({ @InputParam(name = "Driver List", type = "java.util.List"),
			@InputParam(name = "Excel Data", type = "java.util.Map"),
			@InputParam(name = "Number of Message", type = "java.lang.Integer") })
	@ReturnType(name = "text", type = "java.lang.String")

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
		List<WebDriver> driverList = (List<WebDriver>) attributes.get("Driver List");
		Map<String, List<String>> excelData = (Map<String, List<String>>) attributes.get("Excel Data");
		Integer numberOfMessage = (Integer) attributes.get("Number of Message");

		// Your program element business logic goes here ...
		try {
			String text = chatWithTheUsers(driverList, excelData, numberOfMessage);
			nlpResponseModel.getAttributes().put("text", text);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Chatting is completed.");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Chat." + e);
		}
		return nlpResponseModel;
	}

	public String chatWithTheUsers(List<WebDriver> driverList, Map<String, List<String>> entireData,
			int numberOfMessages) throws InterruptedException {
		int numberOfThreads = driverList.size();
		int threadNumber = 1;
		String text = "";
		for (Entry<String, List<String>> singleUserData : entireData.entrySet()) {
			if (threadNumber <= numberOfThreads) {
				driverList.get(threadNumber - 1).findElement(By.id("input")).sendKeys(singleUserData.getKey());
				driverList.get(threadNumber - 1).findElement(By.xpath("//Button[text()='Send']")).click();
				for (int i = 0; i < numberOfMessages; i++) {
					driverList.get(threadNumber - 1).findElement(By.id("input"))
							.sendKeys(singleUserData.getValue().get(i));
					driverList.get(threadNumber - 1).findElement(By.xpath("//Button[text()='Send']")).click();
					Thread.sleep(1000);
				}
			} else {
				break;
			}
			Thread.sleep(1000);
			threadNumber++;
		}

		for (int i = 0; i < driverList.size(); i++) {
			driverList.get(i).findElement(By.xpath("//Button[text()='Disconnect']")).click();
			Thread.sleep(1000);
			if (i == driverList.size() - 1) {
				text = driverList.get(i).findElement(By.xpath("//body")).getText();
			}
			driverList.get(i).close();
		}
		return text;
	}
}