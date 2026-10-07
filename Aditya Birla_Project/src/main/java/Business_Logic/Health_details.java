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


public class Health_details implements Nlp {
	static int delay=1000;
	@InputParams({ @InputParam(name = "Questions", type = "java.lang.String"),
	@InputParam(name = "healthDetails", type = "java.lang.String"),
	@InputParam(name = "alcoholInput", type = "java.lang.String"),
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
		String alcoholInput = (String) attributes.get("alcoholInput");
	
		List<String> namelist = (List<String>) attributes.get("memberDetails");
		WebDriver driver = nlpRequestModel.getWebDriver();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		LinkedHashMap<String, Object> orderedMap = new LinkedHashMap<>();

		try {
			
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
						Thread.sleep(delay);
						driver.findElement(By.xpath("(//h2[contains(text(),\""+name+"\")]/../..//p[contains(text(),\""+entry.getKey()+"\")]/following-sibling::div/descendant::label[text()=\"Yes\"])[last()]")).click();
						System.out.println("jsonarray");
						JSONArray arrayValue = (JSONArray) value;
						System.out.println("Key: " + key1);
						WebElement element=null;
						for (int j = 0; j < arrayValue.length(); j++) {
							try {
								System.out.println("jsonarray");
								System.out.println("Value " + (j + 1) + ": " + arrayValue.getString(j));
								Thread.sleep(delay);
								element = driver.findElement(By.xpath("(//h2[contains(text(),\""+name+"\")]/../..//p[contains(text(),\""+arrayValue.getString(j)+"\")]/following-sibling::div/descendant::label[text()=\"Yes\"])[last()]"));
								if (arrayValue.getString(j).contains("Do You want to consider this Health policy for Portability") || arrayValue.getString(j).contains("Claim in previous policy")) {
									System.out.println("only click");
									js.executeScript("window.scrollBy(0, arguments[0]);", 700);
									Thread.sleep(delay);
									element.click();
								}
								else {
									js.executeScript("window.scrollBy(0, arguments[0]);", 700);
									Thread.sleep(delay);
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
							Thread.sleep(delay);
							question = driver.findElement(By.xpath("//h2[contains(text(),\""+name+"\")]/../..//p[contains(text(),\""+entry.getKey()+"\")]/following-sibling::div/descendant::label[text()=\"Yes\"]"));
							System.out.println("scroll");
							Thread.sleep(delay);
							js.executeScript("window.scrollBy(0, arguments[0]);", 900);
							question.click();
							System.out.println("question clicked");

							if(entry.getKey().contains("please provide details in additional information")) {
								//if(driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'please provide details in additional information')]/../..//label[contains(text(),'"+entry.getKey()+"')]/preceding-sibling::input)[last()]")).isDisplayed()) {
								String sample="please provide details in additional information";
								Thread.sleep(delay);
								driver.findElement(By.xpath("//h2[contains(text(),\""+name+"\")]/../..//label[contains(text(),'Insurer Name')]/preceding-sibling::input")).sendKeys("Pradeep");
								inputTextFields(driver,sample,name,inputs);
							}
							else if(entry.getKey().contains("Do you consume alcohol or smoke or chew any tobacco products")) {
								System.out.println("Entered");
								inputAlcohol(driver, alcoholInput);
							}
						} catch (Exception e) {
							js.executeScript("window.scrollBy(0, arguments[0]);", 900);
						}
						System.out.println("Only json key and value");
						System.out.println("Key: " + key1 + ", Value: " + value);
					}
					Thread.sleep(delay);
					WebElement policy = driver.findElement(By.xpath("//H3[contains(text(),'Current policy details')]"));
					js.executeScript("arguments[0].scrollIntoView(true);", policy);
					js.executeScript("window.scrollBy(0, arguments[0]);", 900);	
				}
				if(namelist.size()>=1)
				{
					Thread.sleep(delay);
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
		return nlpResponseModel;
	}

	public static void inputTextFields(WebDriver driver, String sub, String name, String inputs) throws InterruptedException
	{
		JsonObject jsonObject = new Gson().fromJson(inputs, JsonObject.class);
		WebElement element=null;
		for (String key : jsonObject.keySet()) {       	 
			JsonElement value = jsonObject.get(key);
			String value1 = value.getAsString();
			System.out.println("Key: " + key + ", Value: " + value);	
			Thread.sleep(delay);
			element = driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+sub+"')]/../..//label[contains(text(),'"+key+"')]/preceding-sibling::input)[last()]"));
			try {
				if (element.isDisplayed()) {
					Thread.sleep(delay);
					driver.findElement(By.xpath("(//h2[contains(text(),'"+name+"')]/../..//p[contains(text(),'"+sub+"')]/../..//label[contains(text(),'"+key+"')]/preceding-sibling::input)[last()]")).sendKeys(value1);
				}
			}
			 catch (Exception e) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("arguments[0].scrollIntoView(true);", element);
				js.executeScript("window.scrollBy(0, arguments[0]);", 700);				
			}
		}

		}
		
	 public static void inputAlcohol(WebDriver driver, String alcoholInput) {
			JsonObject jsonObject = new Gson().fromJson(alcoholInput, JsonObject.class);
			WebElement element=null;
			for (String key : jsonObject.keySet()) {       	 
				JsonElement value = jsonObject.get(key);
				String value1 = value.getAsString();
			//	System.out.println("Key: " + key + ", Value: " + value);
				System.out.println("key is " +key);
				System.out.println("value is "+value1);
				element = driver.findElement(By.xpath("//p[text()='"+key+"']"));
				try {
					
					System.out.println("Entered in try");
					Thread.sleep(delay);
					//driver.findElement(By.xpath("//p[text()='"+key+"']")).click();
					
					WebElement alcEle = driver.findElement(By.xpath("//p[text()='"+key+"']"));
					
					JavascriptExecutor js = (JavascriptExecutor) driver;
					js.executeScript("arguments[0].click();", alcEle);
					
					System.out.println("Clicked on element");
					
					Thread.sleep(delay);
					 WebElement ele = driver.findElement(By.xpath("//p[text()='"+key+"']/../..//input"));
					 
					 js.executeScript("arguments[0].click();", ele);
					 System.out.println("Clicked");
					 js.executeScript("arguments[0].value=arguments[1];", ele, value1);
					 System.out.println("Entered value");
					 
					
						if(key.equalsIgnoreCase("Other")) {
							System.out.println("Entered in if block");
							Thread.sleep(delay);
							
							WebElement other = driver.findElement(By.xpath("//p[contains(text(),'Do you consume alcohol')]/../..//p[text()='Other']"));
							js.executeScript("arguments[0].click();", other);
							System.out.println("Clicked on others");
							
							WebElement otherName = driver.findElement(By.xpath("//p[text()='Other Name']/../..//input[@formcontrolname='S_anyothersubstance']"));
							js.executeScript("arguments[0].click();", other);
							js.executeScript("arguments[0].value=arguments[1];", otherName, value1);
							
							WebElement otherQuantity = driver.findElement(By.xpath("//p[text()='Other Quantity']/../..//input[@formcontrolname='S_otherName']"));
							js.executeScript("arguments[0].click();", otherQuantity);
							js.executeScript("arguments[0].value=arguments[1];", otherQuantity, 3);
							 System.out.println("Entered value");
							 break;
						}
					
				} catch (Exception e) {
					JavascriptExecutor js = (JavascriptExecutor) driver;
					js.executeScript("arguments[0].scrollIntoView(true);", element);
					js.executeScript("window.scrollBy(0, arguments[0]);", 700);				
				}

			
		}
	}	
}
