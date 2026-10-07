package Flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ToSelectServiceTunnelDetails implements Nlp {
	static WebDriver driver = null;

	@InputParams({ @InputParam(name = "Connector Json", type = "java.lang.String"),
			@InputParam(name = "Access Tier Json", type = "java.lang.String"),
			@InputParam(name = "Xpath Map", type = "java.util.Map") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String ConnectorJson = (String) programElementsInput.get("Connector Json");
		String AccessTierJson = (String) programElementsInput.get("Access Tier Json");
		Map xpath = (Map) programElementsInput.get("Xpath Map");


		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			driver=nlpRequestModel.getWebDriver();
			if (ConnectorJson!=null) {
				Connector(driver, xpath, ConnectorJson);

			}else if (AccessTierJson!=null) {
				AccessTier(driver, xpath, AccessTierJson);

			}
			 nlpResponseModel.setMessage("Successfully Selected Service Tunnel");
             nlpResponseModel.setStatus(CommonConstants.pass);
		

		} catch (Exception e) {

			nlpResponseModel.setStatus(CommonConstants.fail);
			 nlpResponseModel.setMessage("Failed To Perform "+e);
			 throw new NlpException(e.getMessage());
		}

		return nlpResponseModel;
	}

	public static void main(String[] args) throws InterruptedException {

		Map<String, String> map = new HashMap<String, String>();
		map.put("RemoveNetworkButton",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//button[text()='Remove Network']");
		map.put("SelectIncludeAppButton",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Include\")]//button[text()='Select Applications']");
		map.put("SelectExcludeAppButton",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Exclude\")]//button[text()='Select Applications']");
		map.put("DomainIncludeTextField",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Include\")]//input[contains(@aria-label,\"Public Domains to Include\")]");
		map.put("DomainExcludeTextField",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Exclude\")]//input[contains(@aria-label,\"Domains to Exclude\")]");
		map.put("IpExcludeTextField",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Exclude\")]//input[contains(@aria-label,\"IP Ranges to Exclude\")] ");
		map.put("IpIncludeTextField",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Include\")]//input[contains(@aria-label,\"IP Ranges to Include\")] ");
		map.put("PublicIncludeDropdown",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//button[text()='Public Include Info']");
		map.put("PublicExcludeDropdown",
				"//button[text()='{{dynamicVal}}']//ancestor::fieldset//fieldset[contains(@id,\"Include\")]//input[contains(@aria-label,\"IP Ranges to Include\")] ");
		map.put("AddNetwork", "//button[text()='Add Network']");
		map.put("AddNetworkBox", "//div[@role=\"dialog\"]");
		map.put("AccessTiers", "//li[@aria-label='{{dynamicVal}}']");

		
		
		
//		
		Map<String, String> XPATH_MAP = Collections.unmodifiableMap(map);
		 ChromeOptions options = new ChromeOptions();
	     //  options.addArguments("--remote-debugging-port=2705");
		    options.setExperimentalOption("debuggerAddress", "localhost:2705");
			 driver = new ChromeDriver(options);
//		
//		
 AccessTier(driver, XPATH_MAP, "{   \"access\": [     {       \"name\": \"qa-auto-win-at-suite\",       \"include\": {         \"Apps\": [           \"Yahoo\"],         \"Domains\": [\"yahoo.com\"],         \"ips\": [\"98.137.11.164\"]       },       \"exclude\": {         \"Apps\": [\"Snowflake\"],         \"Domains\": [\"snowflake.com\"],         \"ips\": [\"172.66.1.187\"]       }     }   ] }");
//     Connector(driver, XPATH_MAP, "{\"connector\":{\"name\":[\"qa-auto-win-conn-suite15781\",\"qa-auto-win-conn-suite46663\",\"qa-auto-win-conn-suite51239\"],\"include\":{\"Apps\":[\"Tableau\",\"OpenAI ChatGPT\"],\"Domains\":[\"varun.com\",\"vasrun.com\"],\"ips\":[\"173.45.0.0/16\",\"173.46.0.0/16\"]},\"exclude\":{\"Apps\":[\"1Password\",\"cnn\"],\"Domains\":[\"varun.com\",\"varfun.com\"],\"ips\":[\"173.45.0.0/16\",\"173.35.0.0/16\"]}}}");
//		
	}

	public static void AccessTier(WebDriver driver, Map<String, String> XPATH_MAP,String json)
			throws InterruptedException {

		Object document = JsonPath.parse(json).json();
		List<String> value = JsonPath.read(document, "$.access[*].name");
		Actions actionObject = new Actions(driver);

		for (int i = 0; i < value.size(); i++) {
			
			click(XPATH_MAP.get("AddNetwork"));
			actionObject.moveToElement(driver.findElement(By.xpath(XPATH_MAP.get("AddNetworkBox")))).build().perform();
			click(replaceWithData(XPATH_MAP.get("AccessTiers"), value.get(i)));

			
			// All incude actions for access tier
			List<String> includeData = JsonPath.read(document, "$.access[" + i + "].include.Apps");
			click(replaceWithData(XPATH_MAP.get("SelectIncludeAppButton"), value.get(i)));
			for (int j = 0; j < includeData.size(); j++) {
				click(replaceWithData(XPATH_MAP.get("AccessTiers"), includeData.get(j)));
			}
			actionObject.sendKeys(Keys.ESCAPE).build().perform();

			List<String> includePublicDomains = JsonPath.read(document, "$.access[" + i + "].include.Domains");
			for (int j = 0; j < includePublicDomains.size(); j++) {
				sendkey(replaceWithData(XPATH_MAP.get("DomainIncludeTextField"), value.get(i)),includePublicDomains.get(j));
				actionObject.sendKeys(Keys.ENTER).build().perform();
			}

			List<String> includeIpRanges = JsonPath.read(document, "$.access[" + i + "].include.ips");
			for (int j = 0; j < includeIpRanges.size(); j++) {
				sendkey(replaceWithData(XPATH_MAP.get("IpIncludeTextField"), value.get(i)),includeIpRanges.get(j));
				actionObject.sendKeys(Keys.ENTER).build().perform();
			}
			
			
			
			// all exclude action for action tier
			List<String> excludeData = JsonPath.read(document, "$.access[" + i + "].exclude.Apps");
			click(replaceWithData(XPATH_MAP.get("SelectExcludeAppButton"), value.get(i)));

			for (int j = 0; j < excludeData.size(); j++) {
				click(replaceWithData(XPATH_MAP.get("AccessTiers"), excludeData.get(j)));

			}
			actionObject.sendKeys(Keys.ESCAPE).build().perform();

			List<String> excludePublicDomains = JsonPath.read(document, "$.access[" + i + "].exclude.Domains");
			for (int j = 0; j < excludePublicDomains.size(); j++) {
				sendkey(replaceWithData(XPATH_MAP.get("DomainExcludeTextField"),value.get(i)),excludePublicDomains.get(j));
				actionObject.sendKeys(Keys.ENTER).build().perform();
			}

			List<String> excludeIpRanges = JsonPath.read(document, "$.access[" + i + "].exclude.ips");
			for (int j = 0; j < excludeIpRanges.size(); j++) {
				sendkey(replaceWithData(XPATH_MAP.get("IpExcludeTextField"), value.get(i)),includeIpRanges.get(j));

				actionObject.sendKeys(Keys.ENTER).build().perform();
			}
			
		}
	}
		
	



	public static void Connector(WebDriver driver, Map<String, String> XPATH_MAP ,String json)
			throws InterruptedException {

		Object document = JsonPath.parse(json).json();
		
		List<String> value = JsonPath.read(document, "$.connector.name");
		Actions actionObject = new Actions(driver);
		

		  for (int i = 0; i < value.size(); i++) {
			  click(XPATH_MAP.get("AddNetwork"));
				actionObject.moveToElement(driver.findElement(By.xpath(XPATH_MAP.get("AddNetworkBox")))).build().perform();
			  click(replaceWithData(XPATH_MAP.get("AccessTiers"), value.get(i)));

		  }
		  
	     	 String data= XPATH_MAP.get("Connector");
			// All include actions for connector tier

				// All incude actions for access tier
				List<String> includeData = JsonPath.read(document, "$.connector.include.Apps");
				click(replaceWithData(XPATH_MAP.get("SelectIncludeAppButton"), data));
				for (int j = 0; j < includeData.size(); j++) {
					click(replaceWithData(XPATH_MAP.get("AccessTiers"), includeData.get(j)));
				}
				actionObject.sendKeys(Keys.ESCAPE).build().perform();

				List<String> includePublicDomains = JsonPath.read(document, "$.connector.include.Domains");
				for (int j = 0; j < includePublicDomains.size(); j++) {
					sendkey(replaceWithData(XPATH_MAP.get("DomainIncludeTextField"), data),includePublicDomains.get(j));
					actionObject.sendKeys(Keys.ENTER).build().perform();
				}

				List<String> includeIpRanges = JsonPath.read(document, "$.connector.include.ips");
				for (int j = 0; j < includeIpRanges.size(); j++) {
					sendkey(replaceWithData(XPATH_MAP.get("IpIncludeTextField"), data),includeIpRanges.get(j));
					actionObject.sendKeys(Keys.ENTER).build().perform();
				}
				
				
				
				// all exclude action for action tier
				List<String> excludeData = JsonPath.read(document, "$.connector.exclude.Apps");
				click(replaceWithData(XPATH_MAP.get("SelectExcludeAppButton"), data));

				for (int j = 0; j < excludeData.size(); j++) {
					click(replaceWithData(XPATH_MAP.get("AccessTiers"), excludeData.get(j)));

				}
				actionObject.sendKeys(Keys.ESCAPE).build().perform();

				List<String> excludePublicDomains = JsonPath.read(document, "$.connector.exclude.Domains");
				for (int j = 0; j < excludePublicDomains.size(); j++) {
					sendkey(replaceWithData(XPATH_MAP.get("DomainExcludeTextField"),data),excludePublicDomains.get(j));
					actionObject.sendKeys(Keys.ENTER).build().perform();
				}

				List<String> excludeIpRanges = JsonPath.read(document, "$.connector.exclude.ips");
				for (int j = 0; j < excludeIpRanges.size(); j++) {
					sendkey(replaceWithData(XPATH_MAP.get("IpExcludeTextField"), data),includeIpRanges.get(j));

					actionObject.sendKeys(Keys.ENTER).build().perform();
				}
				
		

}

	public static void sendkey(String Xpath, String Key) throws InterruptedException {
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'nearest'});",
					driver.findElement(By.xpath(Xpath)));
			driver.findElement(By.xpath(Xpath)).sendKeys(Key);
		} catch (org.openqa.selenium.ElementNotInteractableException e) {
			Thread.sleep(2000);
			driver.findElement(By.xpath(Xpath)).sendKeys(Key);
					
			
		}

	}

	public static void click(String Xpath) throws InterruptedException {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'nearest'});",
				driver.findElement(By.xpath(Xpath)));
		WebElement webElement = driver.findElement(By.xpath(Xpath));
			try {
					webElement.click();
					
				} catch (org.openqa.selenium.ElementClickInterceptedException e1) {
					Thread.sleep(2000);
					webElement.click();
				} catch (org.openqa.selenium.ElementNotInteractableException e1) {
					Thread.sleep(2000);
					webElement.click();
				}

			}
		

	

	public static String replaceWithData(String Xpath, String replacedata) {
		return Xpath.replaceAll("\\{\\{dynamicVal\\}\\}", replacedata);

	}

	
	
}