package telusLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;



import org.springframework.stereotype.Component;

@Slf4j
@Component("LIC11870_PJT1012_PE_NLPf71dcc86-8edc-4d59-9636-f3abbdf3a9dd")
public class GetTheTimeOfTheElementWithoutExpectedTime implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "java.lang.String"),
			@InputParam(name = "Element Name", type = "java.lang.String"),
			@InputParam(name = "Max Time Out", type = "java.lang.String"),
			@InputParam(name = "Page Error Messages", type = "java.lang.String") })
	@ReturnType(name = "Return", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String xpath = (String) programElementsInput.get("Element");
		String name = (String) programElementsInput.get("Element Name");
		String maxTimeOut = (String) programElementsInput.get("Max Time Out");
		String errorMessages = (String) programElementsInput.get("Page Error Messages");
		Duration impli = null;
		WebDriver driver = null;
		String returnValue = null;
		Duration returnImpli = null;
		log.info("Given timeout" + maxTimeOut);
		try {
			driver = nlpRequestModel.getWebDriver();
			returnImpli = driver.manage().timeouts().getImplicitWaitTimeout();
			if (maxTimeOut.isEmpty() || maxTimeOut == null || maxTimeOut.equalsIgnoreCase("null")
					|| maxTimeOut.equals("0") || maxTimeOut.equalsIgnoreCase("zero")) {
				impli = driver.manage().timeouts().getImplicitWaitTimeout();
			} else {
				impli = Duration.ofSeconds(Integer.parseInt(maxTimeOut));
			}
			LinkedHashMap<String, Object> mapData = timeTaken(driver, xpath, name, errorMessages, impli);
			returnValue = mapData.get("Message").toString();
			if (Boolean.parseBoolean(mapData.get("Status").toString()) == true) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage(mapData.get("Message").toString());
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage(mapData.get("Message").toString());
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			log.info("Error is -->" + exceptionAsString);
			nlpResponseModel.setMessage("Failed to Check " + sw);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		driver.manage().timeouts().implicitlyWait(returnImpli);
		nlpResponseModel.getAttributes().put("Return", returnValue);
		return nlpResponseModel;
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

	public LinkedHashMap<String, Object> timeTaken(WebDriver driver, String xpath, String elementName,
			String errorMessages, Duration impli) throws InterruptedException {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		Boolean status = false;
		double endTime = 0.0;
		String returnValue = null;
		String diffTime = null;
		String errorMess = null;
		log.info("Xpath is " + xpath);
		log.info("fireflink implicit" + impli);
		long timeOut = impli.toMillis();
		driver.manage().timeouts().implicitlyWait(Duration.ofMillis(100));
		double countTime = 0.0;
		log.info("Loop End time " + timeOut);
		while (true) {
			countTime += 140.0;
			if (countTime <= timeOut) {
				try {
					log.info("Try block Entered");
					if (driver.findElement(By.xpath(xpath)).isDisplayed()) {
						endTime = countTime;
						diffTime = String.valueOf(Math.round(endTime / 1000.0));
						break;
					} else {
						Thread.sleep(100);
					}
				} catch (StaleElementReferenceException e) {
					log.info("Stale element block");
					endTime = countTime;
					diffTime = String.valueOf(Math.round(endTime / 1000.0));
					break;

				} catch (NoSuchElementException e) {
					log.info("No such element block");
				} catch (Throwable e) {
					log.info("Exception occured " + e);
					throw e;
				}
			} else {
				log.info("Element not found");
				break;
			}
		}

		String currentURL = driver.getCurrentUrl();
		log.info("Difference time is " + diffTime);
		if (diffTime != null && (timeOut >= Integer.parseInt(diffTime))) {
			log.info("Element found here");
			returnValue = "Element is displayed within :" + " " + diffTime + " second" + " : " + elementName;
			status = true;
			map.put("Status", status);
			map.put("Message", returnValue);
		} else {
			log.info("end time should be 0 but the time is " + endTime);
			log.info("Error messages list is " + errorMessages);
			String[] arrySplit = errorMessages.split(">");
			String title = driver.getTitle();
			String pageSource = driver.getPageSource();
			Document document = Jsoup.parse(pageSource);
			Elements script = document.getElementsByTag("script");
			for (Element element : script) {
				element.remove();
			}
			pageSource = document.html();
			log.info("Title of the page is " + title);
//			log.info("Page source is " + pageSource);
			for (int i = 0; i < arrySplit.length; i++) {
				if (pageSource.contains(arrySplit[i]) || title.contains(arrySplit[i])) {
					errorMess = arrySplit[i];
					break;
				}
			}
			log.info("Error messages list is " + errorMessages);
			log.info("Error message is " + errorMess);
			if (errorMess != null) {
				returnValue = "Load page error : " + Duration.ofMillis(timeOut).toSeconds() + " : " + impli.toSeconds()
						+ " : " + elementName + ": " + errorMess + " : " + currentURL;
				status = false;
				map.put("Status", status);
				map.put("Message", returnValue);
			} else {
				returnValue = "Load time exceed : " + Duration.ofMillis(timeOut).toSeconds() + " : " + impli.toSeconds()
						+ " : " + elementName + ": NA" + " : " + currentURL;
				status = false;
				map.put("Status", status);
				map.put("Message", returnValue);
			}
		}
		return map;
	}
}