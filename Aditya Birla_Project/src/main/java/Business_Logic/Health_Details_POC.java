package Business_Logic;

import java.util.ArrayList;
import java.util.LinkedHashMap; // Changed import
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


public class Health_Details_POC implements Nlp {
	@InputParams({ @InputParam(name = "Questions", type = "java.lang.String"),
	@InputParam(name = "healthDetails", type = "java.lang.String"),
	@InputParam(name = "memberDetails", type = "java.util.List") })
	//@ReturnType(name = "map", type = "java.util.Map")

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
		String jsonStr = (String) attributes.get("Questions");
		String inputs = (String) attributes.get("healthDetails");
		@SuppressWarnings("unchecked")
		List<String> namelist = (List<String>) attributes.get("memberDetails");
		WebDriver driver = nlpRequestModel.getWebDriver();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		try {
			LinkedHashMap<String, Object> orderedMap = new LinkedHashMap<>();
			Pattern pattern = Pattern.compile("\\\"([^\\\"]+)\\\":(\\[.*?\\]|\\\".*?\\\"),?");
			Matcher matcher = pattern.matcher(jsonStr);
			while (matcher.find()) {
				String key1 = matcher.group(1);
				String value = matcher.group(2);
				if (value.startsWith("[")) {
					JSONArray jsonArray = new JSONArray(value);
					orderedMap.put(key1, jsonArray);
				} else {
					orderedMap.put(key1, value);
				}
			}
			for (int i = 0; i <namelist.size(); i++) {
				for (Map.Entry<String, Object> entry : orderedMap.entrySet()) {
					String name = namelist.get(i);
					System.out.println(name);
					String key1 = entry.getKey();
					Object value = entry.getValue();
					if (value instanceof JSONArray) {
						driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+entry.getKey()+"')]/following-sibling::div/descendant::label[text()='Yes'])[last()]")).click();
						System.out.println("jsonarray");
						JSONArray arrayValue = (JSONArray) value;
						System.out.println("Key: " + key1);
						WebElement element=null;
						for (int j = 0; j < arrayValue.length(); j++) {
							try {
								System.out.println("jsonarray");
								System.out.println("Value " + (j + 1) + ": " + arrayValue.getString(j));
								element = driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+arrayValue.getString(j)+"')]/following-sibling::div/descendant::label[text()='Yes'])[last()]"));
								if (arrayValue.getString(j).contains("Do You want to consider this Health policy for Portability") || arrayValue.getString(j).contains("Claim in previous policy")) {
									System.out.println("only click");
									js.executeScript("window.scrollBy(0, arguments[0]);", 700);
									element.click();
								}
								else {
									js.executeScript("window.scrollBy(0, arguments[0]);", 700);
									element.click();
									inputTextFields(driver,arrayValue.getString(j),name,inputs);
								}

							} catch (Exception e) {
								js.executeScript("window.scrollBy(0, arguments[0]);", 700);
							}

						}
					} else {					
						System.out.println("click iteration else block");
						WebElement question=null;
						try {
							System.out.println(entry.getKey());
							System.out.println(entry.getValue());				
							question = driver.findElement(By.xpath("//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+entry.getKey()+"')]/following-sibling::div/descendant::label[text()='Yes']"));
							System.out.println("scroll");
							
							js.executeScript("window.scrollBy(0, arguments[0]);", 900);
							question.click();
							System.out.println("question clicked");

							if(entry.getKey().contains("please provide details in additional information")) {
								//if(driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'please provide details in additional information')]/../..//label[contains(text(),'"+entry.getKey()+"')]/preceding-sibling::input)[last()]")).isDisplayed()) {
								String sample="please provide details in additional information";
								driver.findElement(By.xpath("//h2[contains(text(),'"+name+"')]/../..//label[contains(text(),'Insurer Name')]/preceding-sibling::input")).sendKeys("Pradeep");
								inputTextFields(driver,sample,name,inputs);
							}
						} catch (Exception e) {
							js.executeScript("window.scrollBy(0, arguments[0]);", 900);
						}
						System.out.println("Only json key and value");
						System.out.println("Key: " + key1 + ", Value: " + value);
					}
					WebElement policy = driver.findElement(By.xpath("//H3[contains(text(),'Current policy details')]"));
					js.executeScript("arguments[0].scrollIntoView(true);", policy);
					js.executeScript("window.scrollBy(0, arguments[0]);", 900);	
				}
				if(namelist.size()>=1)
				{
					driver.findElement(By.xpath("//span[contains(text(),'Proceed')]")).click();
					Thread.sleep(15000);
					System.out.println("proceed button clicked");

				}
			}
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("successfully entered all member health details");
		} catch (Exception e) {

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("failed to enter members health details"+e);
		}
		//  nlpResponseModel.getAttributes().put("map", salDataMap);
		return nlpResponseModel;
	}

	public static void inputTextFields(WebDriver driver, String sub, String name, String inputs)
	{
		//		Map<String, String> input = new LinkedHashMap<>();
		//		input.put("Disease name", "disease");
		//		input.put("Date of Diagnosis", "16052024");
		//		input.put("Last Consultation Date", "18052024");
		//		input.put("Name of Surgery", "Surgery");
		//		input.put("Details of Treatment", "Nothing");
		//		input.put("Disability %", "30");
		//		input.put("Period of hospitalization", "2");
		//		input.put("Any Other information", "Nothing");
		JsonObject jsonObject = new Gson().fromJson(inputs, JsonObject.class);
		WebElement element=null;
		for (String key : jsonObject.keySet()) {       	 
			JsonElement value = jsonObject.get(key);
			String value1 = value.getAsString();
			System.out.println("Key: " + key + ", Value: " + value);		
			element = driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+sub+"')]/../..//label[contains(text(),'"+key+"')]/preceding-sibling::input)[last()]"));
			try {
				if (element.isDisplayed()) {
					//					JavascriptExecutor js = (JavascriptExecutor) driver;
					//					js.executeScript("window.scrollBy(0, arguments[0]);", 300);
					//driver.findElement(By.xpath("(//p[contains(text(),'"+sub+"')]/../..//label[contains(text(),'"+entry.getKey()+"')]/preceding-sibling::input)[last()]")).sendKeys(entry.getValue());
					driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+sub+"')]/../..//label[contains(text(),'"+key+"')]/preceding-sibling::input)[last()]")).sendKeys(value1);
				}
			} catch (Exception e) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("arguments[0].scrollIntoView(true);", element);
				js.executeScript("window.scrollBy(0, arguments[0]);", 700);				
			}

		}
	} 
}
