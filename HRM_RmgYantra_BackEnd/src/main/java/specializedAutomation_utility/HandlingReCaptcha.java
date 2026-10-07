
package specializedAutomation_utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandlingReCaptcha implements Nlp {
	@InputParams({ @InputParam(name = "Access Token", type = "java.lang.String") })

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
		Duration implicit = null;
		try {
			String access = (String) attributes.get("Access Token");
			WebDriver driver = nlpRequestModel.getWebDriver();
			implicit = driver.manage().timeouts().getImplicitWaitTimeout();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			if (driver.findElements(By.xpath("//iframe[@title='reCAPTCHA']")).size() >= 1) {
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//iframe[@title='reCAPTCHA']")));
				WebElement reCap = driver.findElement(By.xpath("//iframe[@title='reCAPTCHA']"));
				driver.switchTo().frame(reCap);
				driver.findElement(By.xpath("//div[@class='recaptcha-checkbox-border']")).click();
				log.info("------->>> click on checkbox");
				Thread.sleep(2000);
				int size = driver.findElements(By.xpath("//div[text()='You are verified']")).size();
				log.info("Size-->>" + size);
				if (size == 0) {
					driver.switchTo().parentFrame();
					log.info("Entered if block");
					try {
						wait.until(ExpectedConditions.elementToBeClickable(
								By.xpath("//iframe[@title=\"recaptcha challenge expires in two minutes\"]")));
					} catch (TimeoutException e) {
						// TODO: handle exception
					}
					WebElement reCapChalle = driver
							.findElement(By.xpath("//iframe[@title=\"recaptcha challenge expires in two minutes\"]"));
					driver.switchTo().frame(reCapChalle);
					if (driver.findElements(By.xpath(
							"//button[@title=\"Get an audio challenge\" and @style=\"display: none;\"]/../..//div[@class=\"button-holder audio-button-holder\"]"))
							.size() <= 0) {
						driver.findElement(By.xpath("//div[@class=\"button-holder audio-button-holder\"]")).click();
					}
					String href = driver.findElement(By.xpath("//div[@class=\"rc-audiochallenge-control\"]//audio"))
							.getAttribute("src");
					String path = fileDownload(href, "C:\\Users\\Public\\file.mp3");
					String ml = getCaptcha(path, access);
					log.info(ml + "---" + ml.length());
					if (ml.length() < 1) {
						driver.findElement(By.xpath("//div[@class=\"button-holder reload-button-holder\"]")).click();
						href = driver.findElement(By.xpath("//div[@class=\"rc-audiochallenge-control\"]//audio"))
								.getAttribute("src");
						path = fileDownload(href, "C:\\Users\\Public\\file.mp3");
						ml = getCaptcha(path, access);
					}
					driver.findElement(By.xpath("//input[@id=\"audio-response\"]")).click();
					driver.findElement(By.xpath("//input[@id=\"audio-response\"]")).sendKeys(ml);
					driver.findElement(By.xpath("//div[@class=\"verify-button-holder\"]")).click();
					driver.switchTo().parentFrame();
				} else {
					driver.switchTo().parentFrame();
				}
			}
			driver.manage().timeouts().implicitlyWait(implicit);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully handled captcha");

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Failed to handle captcha " + e);
		}

		return nlpResponseModel;
	}

	public static String fileDownload(String fileUrl, String destinationPath) {

		try {
			URL url = new URL(fileUrl);
			InputStream inputStream = url.openStream();

			Path destination = Path.of(destinationPath);
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);

			log.info("File downloaded successfully.");
		} catch (IOException e) {
			e.printStackTrace();
			log.info("Failed to download the file." + e);
		}
		return destinationPath;
	}

	static String getCaptcha(String mp3InputFile, String accessToken) throws ParseException, IOException {
		String apiUrl = "https://api.wit.ai/speech";
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(apiUrl);

		httpPost.setHeader("Authorization", "Bearer " + accessToken);

		File audioFile = new File(mp3InputFile);
		FileEntity fileEntity = new FileEntity(audioFile, ContentType.create("audio/mpeg"));

		httpPost.setEntity(fileEntity);

		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity responseEntity = response.getEntity();

		if (responseEntity != null) {
			String responseString = EntityUtils.toString(responseEntity);
			EntityUtils.consume(responseEntity);
			System.out.println(responseString);
			String finalValue = responseString.substring(responseString.lastIndexOf("text\": \"") + 8,
					(responseString.lastIndexOf("\"traits\":") - 5));
			return finalValue;
		} else {
			throw new RuntimeException("Failed to get response from the API");
		}
	}

	public static void main(String[] args) throws ParseException, IOException {
		int i=10;
		System.out.println(i);
		++i;
		++i;
		System.out.println(i);
//		System.out.println(getCaptcha("C:\\Users\\Public\\file.mp3", "IU3TKCTKEJYAE7BEA276VTFCFDMULYOL"));
	}
}