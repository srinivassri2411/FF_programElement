
package reusable_utility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class NetworkPatcher implements Nlp {
//	public static WebDriver driver;

	@InputParams({ @InputParam(name = "Header Map", type = "Map"),
			@InputParam(name = "Patcher File Path", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Map headerMap = (Map) programElementsInput.get("Header Map");
		String patcherFilePath = (String) programElementsInput.get("Patcher File Path");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			patcher(nlpRequestModel.getWebDriver(), patcherFilePath, headerMap);
//			patcher(driver, patcherFilePath, headerMap);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Network patcher added");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to add " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	private void patcher(WebDriver driver, String filePath, Map headers) throws IOException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		ObjectMapper objectMapper = new ObjectMapper();
		String headersJson = objectMapper.writeValueAsString(headers);
		String script = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		String patcherScript = script.replace("__HEADERS_PLACEHOLDER__", headersJson);
		js.executeScript(patcherScript);
	}

	public static void main(String[] args) throws JsonProcessingException {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		driver.get("http://49.249.29.4:8091/");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> headers = new HashMap<>();
		headers.put("MyName", "Srinivas");
		headers.put("MySecond", "Swaraj");
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Header Map", headers);
		model.getAttributes().put("Patcher File Path", "C:\\Users\\User\\Downloads\\patcher.min.js");
		new Scanner(System.in).nextLine();
		driver.findElement(By.id("username")).sendKeys("rmgyantra");
		driver.findElement(By.id("inputPassword")).sendKeys("rmgy@9999");
		driver.findElement(By.xpath("//button[text()='Sign in']")).click();
	}
}