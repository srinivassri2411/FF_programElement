package sead_Utility;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public class CreateAndSaveObjects implements Nlp {
	@InputParams({ @InputParam(name = "BaseURL", type = "java.lang.String"),
		@InputParam(name = "FieldsToConsider(All,Mandatory,Optional)", type = "java.lang.String"),
		@InputParam(name = "ModuleName", type = "java.lang.String") })
	@ReturnType(name = "map", type = "java.util.Map")
	
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
		String baseURL = (String) attributes.get("BaseURL");
		String fieldsToConsider = (String) attributes.get("FieldsToConsider(All,Mandatory,Optional)");
		String moduleName = (String) attributes.get("ModuleName");
		
		try {
			Map<String, String> map = createAndSaveObject(nlpRequestModel.getWebDriver(), moduleName, fieldsToConsider, baseURL);
			nlpResponseModel.setMessage("New "+moduleName+" Created Successfully...");
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.getAttributes().put("map", map);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to create "+moduleName+"..."+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
			throw new NlpException(e.getMessage());
			
		}
		return nlpResponseModel;
	}
	
	public static String createAndSaveObject(String directoryPath, String oldFileName, String newFileName) {
	    File directory = new File(directoryPath);
	    File oldFile = new File(directory, oldFileName);

	    if (!oldFile.exists()) {
	        System.err.println("File not found: " + oldFile.getAbsolutePath());
	        return "";
	    }

	    int lastDotIndex = oldFileName.lastIndexOf('.');
	    String extension = "";
	    if (lastDotIndex != -1) {
	        extension = oldFileName.substring(lastDotIndex);
	    }

	    String newFileNameWithExtension = newFileName + extension;

	    File newFile = new File(directory, newFileNameWithExtension);

	    if (oldFile.renameTo(newFile)) {
	        return newFile.getName();
	    } else {
	        System.err.println("Failed to rename file: " + oldFile.getAbsolutePath());
	        return "";
	    }
	}
	
	public static Map<String, String> createAndSaveObject(WebDriver driver, String module, String fieldScope,
			String baseURL) throws Exception {
		baseURL=baseURL.replace("https://","").trim();
      if(baseURL.contains("/"))  baseURL="https://"+baseURL.substring(0,baseURL.indexOf("/"));
      else baseURL="https://"+baseURL;
        driver.get(baseURL + "/lightning/page/home");
        
		WebElement moduleEle = driver.findElement(By.xpath("//nav//span[text()='" + module + "']/parent::*"));
		((JavascriptExecutor) driver).executeScript("arguments[0].click()", moduleEle);
		Thread.sleep(2000);
		Map<String, String> map = getAuraTokenCookieSID(driver, baseURL);
		try {
			driver.findElement(By.xpath("//*[text()='New']")).click();
		} catch (Exception e) {
		}
		 String moduleName = driver.getCurrentUrl().replace(baseURL + "/lightning/o/", "").split("/")[0];
		Map<String, String> dataMap = newObject(driver, moduleName, fieldScope, baseURL, map, false);
		try {
			driver.findElement(By.xpath("//records-form-footer//button[text()='Save']")).click();
		} catch (Exception e) {
			try {
				driver.findElement(By.xpath("//button[@title='Save']")).click();
			} catch (Exception x) {
			}
		}
		return dataMap;
	}

	private static void subSectionDataFillForObject(WebDriver driver, String moduleName, Sheet sheet,
			List<Map<String, String>> personalDetailsMapList, String schemaResponseStr, String objectSchemaResponseStr)
			throws InterruptedException {
		for (int i = 0; i < personalDetailsMapList.size(); i++) {
			String mappingKey = personalDetailsMapList.get(i).get("Mapping Api Key").trim();
			String previousValue = getJsonStringValue(objectSchemaResponseStr, "$." + mappingKey);
			String label = personalDetailsMapList.get(i).get("Label Name").trim();
			String sectionFieldScope = personalDetailsMapList.get(i).get("Mandatory/Optional").trim();
			String fieldType = personalDetailsMapList.get(i).get("Field Type(only if Mapping Api Key Unkown)").trim();
			String value = personalDetailsMapList.get(i).get("Value").trim();
			String sectionName = personalDetailsMapList.get(i).get("Section Name").trim();

			if (sectionFieldScope.equalsIgnoreCase("M")) {
				if (value.equalsIgnoreCase("-") && !previousValue.equalsIgnoreCase(""))
					continue;

				if (fieldType.equalsIgnoreCase("-")) {
					fieldType = getJsonStringValue(schemaResponseStr, "$.fields[?(@.name=='" + mappingKey + "')].type")
							.replaceAll("[\\[\\]\"]", "");
				}

				String xpath = "";
				String forAttribute = "";
				if (StringUtils.equalsAnyIgnoreCase(fieldType, "section", "button", "link")) {
					try {
						if (fieldType.equalsIgnoreCase("section"))
							xpath = "//*[text()='" + label + "']/ancestor::div[@class='slds-section slds-is-close']";
						else
							xpath = "//*[text()='" + label + "']";
						WebElement ele = driver.findElement(By.xpath(xpath));
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", ele);
						((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");
						ele.click();
						Thread.sleep(1000);
						if (value.equalsIgnoreCase("true")) {
							personalDetailsMapList = getRelatedDataFromExcel(sheet, sectionName + "_" + label);
							subSectionDataFillForObject(driver, moduleName, sheet, personalDetailsMapList,
									schemaResponseStr, objectSchemaResponseStr);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				}
				if (StringUtils.equalsAnyIgnoreCase(fieldType, "string", "url", "textarea", "phone", "email", "int",
						"currency", "double", "reference")) {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

					try {
						try {
							forAttribute = driver
									.findElement(By.xpath("//*[text()='" + label + "' and starts-with(@for,'input')]"))
									.getAttribute("for");
						} catch (Exception e) {
							forAttribute = driver
									.findElement(By.xpath("//*[text()='" + label
											+ "']/ancestor::div[@class='slds-card']//label[starts-with(@for,'input')]"))
									.getAttribute("for");
						}
					} catch (Exception e) {
						try {
							forAttribute = driver.findElement(By.xpath("//*[text()='" + label
									+ "']/ancestor::div[contains(@class,'slds-grid')][1]//label[starts-with(@for,'input')]"))
									.getAttribute("for");
						} catch (Exception e1) {
							forAttribute = driver
									.findElement(By.xpath("//*[text()='" + label + "' and starts-with(@for,'input')]"))
									.getAttribute("for");
						}
					}
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
					xpath = String.format("//*[@id='%s']", forAttribute);
				}
				switch (fieldType.toLowerCase()) {
				case "string":
					if (StringUtils.equalsAnyIgnoreCase(label, "PostalCode", "Pincode")) {
						if (value.equalsIgnoreCase("-"))
							value = RandomStringUtils.randomNumeric(6);
						driver.findElement(By.xpath(xpath)).sendKeys(value);
					} else {
						if (value.equalsIgnoreCase("-"))
							value = moduleName.toLowerCase() + " " + RandomStringUtils.randomAlphabetic(10);
						driver.findElement(By.xpath(xpath)).sendKeys(value);
					}
					break;
				case "url":
					if (value.equalsIgnoreCase("-"))
						value = "www." + RandomStringUtils.randomAlphabetic(10) + ".com";
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "textarea":
					if (value.equalsIgnoreCase("-"))
						value = moduleName.toLowerCase() + " " + getRandomWords(20, 5);
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "phone":
					if (value.equalsIgnoreCase("-"))
						value = "9" + RandomStringUtils.randomNumeric(9, 10);
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "email":
					if (value.equalsIgnoreCase("-"))
						value = RandomStringUtils.randomAlphabetic(10) + "@fireflink.com";
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "int":
					if (value.equalsIgnoreCase("-"))
						value = RandomStringUtils.randomNumeric(2);
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "currency":
					if (value.equalsIgnoreCase("-"))
						value = RandomStringUtils.randomNumeric(5);
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "double":
					if (value.equalsIgnoreCase("-"))
						value = RandomStringUtils.randomNumeric(2) + "." + RandomStringUtils.randomNumeric(2);
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					break;
				case "picklist":
					try {
						forAttribute = driver.findElement(By.xpath("//*[text()='" + label
								+ "']/ancestor::div[contains(@class,'slds-card')][1]//label[starts-with(@for,'combobox-button')]"))
								.getAttribute("for");
					} catch (Exception e) {
						forAttribute = driver
								.findElement(
										By.xpath("//*[text()='" + label + "' and starts-with(@for,'combobox-button')]"))
								.getAttribute("for");
					}
					xpath = String.format("//*[@id='%s']", forAttribute);
					// ((JavascriptExecutor)
					// driver).executeScript("arguments[0].scrollIntoView(true);",
					// driver.findElement(By.xpath(xpath)));
					// ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-170);");
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);",
							driver.findElement(By.xpath(xpath)));
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");

					driver.findElement(By.xpath(xpath)).click();
					if (value.equalsIgnoreCase("-")) {
						int length = getJsonStringValue(schemaResponseStr, "$.fields[?(@.type=='picklist' && @.name=='"
								+ mappingKey + "')].picklistValues[*].label").split(",").length;
						value = getJsonStringValue(schemaResponseStr, "$.fields[?(@.type=='picklist' && @.name=='"
								+ mappingKey + "')].picklistValues[" + getRandomNumber(0, length - 1) + "].label")
								.replaceAll("[\\[\\]\"]", "");
					}

					xpath = "//lightning-base-combobox-item//*[@title='" + value + "']";
					driver.findElement(By.xpath(xpath)).click();
					break;
				case "multipicklist":
					int numberOfValues = 2;
					if (value.equalsIgnoreCase("-")) {
						numberOfValues = value.split(",").length;
					}
					String rightButtonXPath = "";
					for (int j = 0; j < numberOfValues; j++) {
						if (value.equalsIgnoreCase("-") && !mappingKey.equalsIgnoreCase("-")) {
							int length = getJsonStringValue(schemaResponseStr,
									"$.fields[?(@.type=='multipicklist' && @.name=='" + mappingKey
											+ "')].picklistValues[*].label")
									.split(",").length;
							value = getJsonStringValue(schemaResponseStr,
									"$.fields[?(@.type=='multipicklist' && @.name=='" + mappingKey
											+ "')].picklistValues[" + getRandomNumber(0, length - 1) + "].label")
									.replaceAll("[\\[\\]\"]", "");
						} else
							value = value.split(",")[j].trim();

						xpath = "//*[text()='" + label
								+ "']/ancestor::div[contains(@class,'slds-card')][1]//*[starts-with(@aria-labelledby,'group-label')]//*[text()='"
								+ value + "']";
						rightButtonXPath = "//*[text()='" + label
								+ "']/ancestor::div[contains(@class,'slds-card')][1]//*[starts-with(@aria-labelledby,'group-label')]//*[@data-key='right']";
						try {
							driver.findElement(By.xpath(xpath)).isDisplayed();
						} catch (Exception e) {
							xpath = "//*[text()='" + label + "']/following-sibling::div//li//*[text()='" + value + "']";
							rightButtonXPath = "//*[text()='" + label
									+ "']/following-sibling::div//*[@data-key='right']";
						}
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);",
								driver.findElement(By.xpath(xpath)));
						((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");

						driver.findElement(By.xpath(xpath)).click();
						driver.findElement(By.xpath(rightButtonXPath)).click();
					}
					break;
				case "search":
					forAttribute = driver
							.findElement(By.xpath("//*[text()='" + label
									+ "']/ancestor::div[@class='slds-card']//label[starts-with(@for,'input')]"))
							.getAttribute("for");
					xpath = String.format("//*[@id='%s']", forAttribute);
					// ((JavascriptExecutor)
					// driver).executeScript("arguments[0].scrollIntoView(true);",
					// driver.findElement(By.xpath(xpath)));
					// ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-170);");
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);",
							driver.findElement(By.xpath(xpath)));
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");
					driver.findElement(By.xpath(xpath)).sendKeys(value);
					driver.findElement(By.xpath("(//*[text()='" + label
							+ "']/ancestor::div[@class='slds-card']//*[text()='" + value + "'])[1]")).click();
					break;
				case "multi_search":
					forAttribute = driver
							.findElement(By.xpath("//*[text()='" + label
									+ "']/ancestor::div[@class='slds-card']//label[starts-with(@for,'input')]"))
							.getAttribute("for");
					xpath = String.format("//*[@id='%s']", forAttribute);
					// ((JavascriptExecutor)
					// driver).executeScript("arguments[0].scrollIntoView(true);",
					// driver.findElement(By.xpath(xpath)));
					// ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-170);");
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);",
							driver.findElement(By.xpath(xpath)));
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");
					String[] valueStr = value.split(",");
					for (int j = 0; j < valueStr.length; j++) {
						value = valueStr[j].trim();
						driver.findElement(By.xpath(xpath)).sendKeys(value);
						driver.findElement(
								By.xpath("//*[text()='" + label + "']/ancestor::div[@class='slds-card']//*[text()='"
										+ value + "']/preceding-sibling::span"))
								.click();
						driver.findElement(By.xpath(
								"//*[text()='" + label + "']/ancestor::div[@class='slds-card']//*[text()='Done']"))
								.click();
					}
					break;
				case "picklist_search":
					driver.findElement(
							By.xpath("//*[text()='" + label + "']/ancestor::div[@class='slds-card']//button")).click();
					// ((JavascriptExecutor)
					// driver).executeScript("arguments[0].scrollIntoView(true);",
					// driver.findElement(By.xpath("//*[text()='" + label +
					// "']/ancestor::div[@class='slds-card']//li")));
					// ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-170);");
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);",
							driver.findElement(
									By.xpath("//*[text()='" + label + "']/ancestor::div[@class='slds-card']//li")));
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");

					int count = driver
							.findElements(By.xpath("//*[text()='" + label + "']/ancestor::div[@class='slds-card']//li"))
							.size();
					driver.findElement(By.xpath("(//*[text()='" + label + "']/ancestor::div[@class='slds-card']//li)["
							+ getRandomNumber(1, count) + "]")).click();
					break;
				case "upload":
					// forAttribute = driver.findElement(By.xpath("//*[text()='" + label +
					// "']/ancestor::div[contains(@class,'slds-col')and
					// contains(@class,'slds-p-vertical')][1]//*[text()='Upload
					// Files']/parent::label")).getAttribute("for");
					// xpath = String.format("//*[@id='%s']", forAttribute);
					xpath = "(//*[text()='" + label + "']/following::input[@type='file'])[1]";
					WebElement ele = driver.findElement(By.xpath(xpath));
					((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest'});",
							ele);
					ele.sendKeys(value);
					Thread.sleep(1000);
					break;
				case "picklist_upload":
					ele = driver.findElement(By.xpath("(//*[text()='" + label + "']/following::button)[1]"));
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", ele);
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");

					ele.click();

					int pickListCount = driver
							.findElements(By
									.xpath("(//*[text()='" + label + "']/following::lightning-base-combobox-item)[1]"))
							.size();

					ele = driver.findElement(
							By.xpath("((//*[text()='" + label + "']/following::lightning-base-combobox-item)[1])["
									+ getRandomNumber(1, pickListCount) + "]"));
					((JavascriptExecutor) driver).executeScript(
							"arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest'});",
							ele);
					ele.click();
					ele = driver.findElement(By.xpath("(//*[text()='" + label + "']/following::input)[1]"));
					ele.sendKeys(value);

					Thread.sleep(1000);
					break;
				case "reference":
					ele = driver.findElement(By.xpath(xpath));
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", ele);
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150);");
					ele.click();
					ele.click();
					Thread.sleep(1000);
					String ariaControlsAttribute = driver.findElement(By.xpath(xpath)).getAttribute("aria-controls");
					if (StringUtils.equalsAnyIgnoreCase(value, "", "-"))
						value = "1";
					if (value.length() > 2) {
						driver.findElement(By.xpath(xpath)).sendKeys(value);
						value = "1";
					}
					xpath = "(//*[@id='" + ariaControlsAttribute + "']//li/lightning-base-combobox-item)[" + value
							+ "]";
					driver.findElement(By.xpath(xpath)).click();
					break;
				case "boolean":
					xpath = "//*[text()='" + label + "']/preceding-sibling::span";
					driver.findElement(By.xpath(xpath)).click();

					break;
				// case "date":
				// forAttribute =
				// driver.findElement(By.xpath("//*[text()='"+label+"']/ancestor::label[starts-with(@for,'checkbox')]")).getAttribute("for");
				// xpath = String.format("//*[@id='%s']", forAttribute);
				// if (value.equalsIgnoreCase("-")) value=RandomStringUtils.randomNumeric(2) +
				// "." + RandomStringUtils.randomNumeric(2);
				// SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				// driver.findElement(By.xpath(xpath)).sendKeys(sdf.format(new Date()));
				// break;
				// case "datetime":
				// if (value.equalsIgnoreCase("-")) value=RandomStringUtils.randomNumeric(2) +
				// "." + RandomStringUtils.randomNumeric(2);
				// System.err.println(dataType + " is defined in code base, Element Label ==> "
				// + label);
				// break;
				default:
					System.err.println(fieldType + " is not defined in code base, Element Label ==> " + label);
					break;
				}
			}
		}
	}

	private static List<Map<String, String>> getRelatedDataFromExcel(Sheet sheet, String sectionName) {
		DataFormatter df = new DataFormatter();
		boolean flag = false;
		List<Map<String, String>> list = new ArrayList<>();
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			if (df.formatCellValue(sheet.getRow(i).getCell(0)).equalsIgnoreCase(sectionName)) {
				flag = true;
				Map<String, String> map = new LinkedHashMap<>();
				for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
					String key = df.formatCellValue(sheet.getRow(0).getCell(j));
					String value = df.formatCellValue(sheet.getRow(i).getCell(j));
					map.put(key, value);
				}
				list.add(map);
			}
			if (flag && !df.formatCellValue(sheet.getRow(i).getCell(0)).equalsIgnoreCase(sectionName))
				break;
		}
		return list;
	}

	private static int getColumnIndex(Sheet sheet, int uniqueRowIndex, String columnName) throws Exception {
		DataFormatter df = new DataFormatter();
		Row row = sheet.getRow(uniqueRowIndex);
		int columnIndex = 0;
		for (int i = 0; i < row.getLastCellNum(); i++) {
			if (df.formatCellValue(row.getCell(i)).equalsIgnoreCase(columnName)) {
				columnIndex = i;
				break;
			}
		}
		return columnIndex;
	}

	private static Map<String, String> getAuraTokenCookieSID(WebDriver driver, String baseURL) throws Exception {
		List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
		System.out.print("*************");
		List<String> li = new ArrayList();
		for (LogEntry entry : entries) {
			li.add(entry.getMessage());
		}
		String cookieHeader = getRequestCookieHeader(baseURL, "origin", "cookie", entries);
		System.out.print("*************");
//		String requestBody = getRequestBody(
//				baseURL + "/aura?r=0&ui-flexipage-components-page.FlexipageControllerV2.getPage=1", entries);
		String requestBody = waitForAuraRequest(driver, baseURL);
		System.out.print("*************");
//		String sid = getSidFromCookie(baseURL, entries);
		String sid = cookieHeader.split("sid=")[1].split("; ")[0];
		System.out.print("*************");
		System.out.println("Request body: "+requestBody);
		System.out.println("*************");

		requestBody = URLDecoder.decode(requestBody);
		List<String> l = Arrays.asList(requestBody.split("&aura"));

		if (l.size() < 4) {
		    throw new Exception("Invalid requestBody format. Expected aura params but got: " + requestBody);
		}

		String auraContext = l.get(1).replace(".context=", "");
		String auraToken = l.get(3).replace(".token=", "");
		Map<String, String> map = new LinkedHashMap<>();
		map.put("auraToken", auraToken);
		map.put("auraContext", auraContext);
		map.put("cookieHeader", cookieHeader);
		map.put("sid", sid);
		return map;
	}
	
	private static String getRequestCookieHeader(String reqUrl, String origin, String headerName, List<LogEntry> entries) throws Exception {
        String message = "";
        for (LogEntry entry : entries) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonSessionPayload = objectMapper.readTree(entry.getMessage());
            String originURL = jsonSessionPayload.path("message").path("params").path("headers").path(origin).asText();
            if (originURL.equalsIgnoreCase(reqUrl)) {
                message = jsonSessionPayload.path("message").path("params").path("headers").path(headerName).asText();
                if (!message.equalsIgnoreCase("")) break;
            }
        }
        if (message.equals("") && headerName.equals("cookie"))message=getRequestCookieHeader(reqUrl, "Origin","Cookie",entries);
        return message;
    }
	
	private static void getAuraTokenCookieSID(String imagePath, int targetWidth, int targetHeight) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(imagePath));

		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = resizedImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		graphics2D.dispose();
		
		File inputFile = new File(imagePath);
		File parentDirectory = inputFile.getParentFile();
		String outputFileName = inputFile.getName(); // Keep the original file name
		String outputImagePath = parentDirectory.getAbsolutePath() + File.separator + outputFileName;

		File outputFile = new File(outputImagePath);
		ImageIO.write(resizedImage, "png", outputFile);
	}

	private static String getSidFromCookie(String reqUrl, List<LogEntry> entries) throws Exception {
		String message = "";
		for (LogEntry entry : entries) {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonSessionPayload = objectMapper.readTree(entry.getMessage());
			String originURL = jsonSessionPayload.path("message").path("params").path("headers").path("referer")
					.asText();
			if (originURL.endsWith("/"))
				originURL = originURL.substring(0, originURL.length() - 1);
			if (originURL.equalsIgnoreCase(reqUrl)) {
				message = getJsonStringValue(jsonSessionPayload.toString(),
						"$.message.params.associatedCookies..[?(@.name=='sid')].value").replaceAll("[\\[\\]\"]", "");
				System.out.println(message);
				if (!message.equalsIgnoreCase("") && !message.contains(",")) {
					break;
				}
			}
		}
		return message;
	}
	
	public static void newObject(InputStream inputStream) throws EncryptedDocumentException, IOException {
		FileInputStream fileInputStream = (FileInputStream) inputStream;
		
		Workbook wb = WorkbookFactory.create(fileInputStream);
		Sheet sheet = wb.getSheet("Table Data");
		sheet.getRow(0).getCell(0).setCellValue("Channaveer");

		FileDescriptor fd = fileInputStream.getFD();
		OutputStream fos = new FileOutputStream(fd);
		wb.write(fos);
		
	}

	private static Map<String, String> newObject(WebDriver driver, String moduleName, String fieldScope, String baseURL,
			Map<String, String> map, boolean isSubModule) throws Exception {
		System.out.print("*************************************************************************");

		String auraToken = map.get("auraToken");
		String auraContext = map.get("auraContext");
		String cookieHeader = map.get("cookieHeader");

		String getTemplateMessage = "{\"actions\":[{\"id\":\"1244;a\",\"descriptor\":\"aura://DynamicComponentController/ACTION$getTemplateDescriptorWithExpansionBundle\",\"callingDescriptor\":\"UNKNOWN\",\"params\":{\"type\":\"Bundle\",\"templateReferences\":[{\"type\":\"DetailPanel\",\"attributes\":{\"moduleName\":\"forceGenerated:detailPanel_"
				+ moduleName
				+ "___012000000000000AAA___Full___Create\",\"adgEnabled\":\"false\"}}]},\"storable\":true}]}";
		String getObjectInfoMessage = "{\"actions\":[{\"id\":\"1249;a\",\"descriptor\":\"aura://RecordUiController/ACTION$getObjectInfo\",\"callingDescriptor\":\"UNKNOWN\",\"params\":{\"objectApiName\":\""
				+ moduleName + "\"}}]}";
		String getPicklistValuesMessage = "{\"actions\":[{\"id\":\"1246;a\",\"descriptor\":\"aura://RecordUiController/ACTION$getPicklistValuesByRecordType\",\"callingDescriptor\":\"UNKNOWN\",\"params\":{\"objectApiName\":\""
				+ moduleName + "\",\"recordTypeId\":\"012000000000000AAA\"}}]}";
		System.out.print("*************");
		String requestBody = "message=" + URLEncoder.encode(getTemplateMessage) + "&aura.context="
				+ URLEncoder.encode(auraContext) + "&aura.token=" + URLEncoder.encode(auraToken);
		Response getTemplateResponse = postRequestWithoutHeader(baseURL + "/aura",
				"?aura.DynamicComponent.getTemplateDescriptorWithExpansionBundle=1", requestBody,
				"Content-Type###application/x-www-form-urlencoded; charset=UTF-8", "Cookie###" + cookieHeader);
		String getTemplateResponseStr = getTemplateResponse.getBody().asPrettyString();
		System.out.print("*************");
		requestBody = "message=" + URLEncoder.encode(getPicklistValuesMessage) + "&aura.context="
				+ URLEncoder.encode(auraContext) + "&aura.token=" + URLEncoder.encode(auraToken);
		Response getPickListValueResponse = postRequestWithoutHeader(baseURL + "/aura",
				"?aura.RecordUi.getPicklistValuesByRecordType=1", requestBody,
				"Content-Type###application/x-www-form-urlencoded; charset=UTF-8", "Cookie###" + cookieHeader);
		String getPickListValueResponseStr = getPickListValueResponse.getBody().asPrettyString();
		System.out.print("*************");
		requestBody = "message=" + URLEncoder.encode(getObjectInfoMessage) + "&aura.context="
				+ URLEncoder.encode(auraContext) + "&aura.token=" + URLEncoder.encode(auraToken);
		Response getObjectInfoResponse = postRequestWithoutHeader(baseURL + "/aura", "?aura.RecordUi.getObjectInfo=1",
				requestBody, "Content-Type###application/x-www-form-urlencoded; charset=UTF-8",
				"Cookie###" + cookieHeader);
		String getObjectInfoResponseStr = getObjectInfoResponse.getBody().asPrettyString();
//		FileWriter fw=new FileWriter(new File("./testMohan.txt"));
//		fw.append(getTemplateResponseStr);
//		fw.flush();
		List<String> listModuleDefs = getJsonListValue(getTemplateResponseStr, "$.context.moduleDefs[*]");
		String moduleDefCo = "";
		String moduleDefCo_4 = "";
		for (int i = 0; i < listModuleDefs.size(); i++) {
			String moduleDefCe = getJsonStringValue(getTemplateResponseStr, "$.context.moduleDefs[" + i + "].ce");
			if (moduleDefCe.endsWith("Full___Create___RecordLayout2"))
				moduleDefCo = getJsonStringValue(getTemplateResponseStr, "$.context.moduleDefs[" + i + "].co");
			else if (moduleDefCe.endsWith("Full___Create___RecordLayoutTemplate"))
				moduleDefCo_4 = getJsonStringValue(getTemplateResponseStr, "$.context.moduleDefs[" + i + "].co");
		}
		System.out.print("****************************************");
		String mastetPropModuleDefCo = moduleDefCo_4;
		String layoutFieldMetadata = StringUtils.substringBetween(moduleDefCo, "this.layoutFieldMetadata = {", "};")
				.trim();
		
		List<String> readOnlyFields = new ArrayList<String>();
		try{ readOnlyFields = Arrays.asList(StringUtils
				.substringBetween(layoutFieldMetadata, "readOnlyFields: [", "]").trim().replace("\"", "").split(", "));
		}catch (Exception e) {System.out.print("\nRead Only Fields are not there in this modules");}
		
		List<String> fieldApiNames = new ArrayList<>();
		fieldApiNames.addAll(Arrays.asList(StringUtils.substringBetween(layoutFieldMetadata, "fieldApiNames: [", "]")
				.trim().replace("\"", "").split(", ")));
		fieldApiNames.removeAll(readOnlyFields);
		List<String> requiredFields = new ArrayList<>();
		requiredFields.addAll(Arrays.asList(StringUtils.substringBetween(layoutFieldMetadata, "requiredFields: [", "]")
				.trim().replace("\"", "").split(", ")));

		List<String> executionFields = new ArrayList<>();
		if (fieldScope.equalsIgnoreCase("Mandatory"))
			executionFields = requiredFields;
		else if (fieldScope.equalsIgnoreCase("Optional")) {
			fieldApiNames.removeAll(requiredFields);
			executionFields = fieldApiNames;
		} else if (fieldScope.equalsIgnoreCase("all"))
			executionFields = fieldApiNames;
		System.out.print("*************");

		Map<String, String> dataMap = new HashMap<>();
		for (int i = 0; i < executionFields.size(); i++) {
			String fieldName = executionFields.get(i);
			String dataType = getJsonStringValue(getObjectInfoResponseStr,
					"$.actions[0].returnValue.fields." + fieldName + ".dataType");
			String label = getJsonStringValue(getObjectInfoResponseStr,
					"$.actions[0].returnValue.fields." + fieldName + ".label");
			String relationshipName = getJsonStringValue(getObjectInfoResponseStr,
					"$.actions[0].returnValue.fields." + fieldName + ".referenceToInfos[0].apiName");

			String xpath = "";
			String forAttribute = "";
			String data = "";
			try {
				try {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
					if (isSubModule) {
						if (dataType.equalsIgnoreCase("Picklist"))
							forAttribute = driver.findElement(By.xpath("(//span[text()='" + label + "'])[last()]"))
									.getAttribute("id");
						else
							forAttribute = driver
									.findElement(By.xpath("(//span[text()='" + label + "']/parent::label)[last()]"))
									.getAttribute("for");
					} else {
						if (dataType.equalsIgnoreCase("Boolean"))
							forAttribute = driver.findElement(By.xpath("//span[text()='" + label + "']/parent::label"))
									.getAttribute("for");
						else
							try {
								forAttribute = driver.findElement(By.xpath("//label[text()='" + label + "']"))
										.getAttribute("for");
							} catch (Exception x) {
								forAttribute = driver
										.findElement(By.xpath("//span[text()='" + label + "']/parent::label"))
										.getAttribute("for");
							}
					}
				} catch (Exception e) {
					moduleDefCo_4 = mastetPropModuleDefCo.substring(0,
							mastetPropModuleDefCo.indexOf("\"fieldApiName\": \"" + fieldName + "\""));
					label = moduleDefCo_4
							.substring(moduleDefCo_4.lastIndexOf("\"fieldLabel\": "), moduleDefCo_4.lastIndexOf("\","))
							.trim().replace("\"fieldLabel\": \"", "");
					if (isSubModule) {
						if (dataType.equalsIgnoreCase("Picklist"))
							forAttribute = driver.findElement(By.xpath("(//span[text()='" + label + "'])[last()]"))
									.getAttribute("id");
						else
							forAttribute = driver
									.findElement(By.xpath("(//span[text()='" + label + "']/parent::label)[last()]"))
									.getAttribute("for");
					} else {
						if (dataType.equalsIgnoreCase("Boolean"))
							forAttribute = driver.findElement(By.xpath("//span[text()='" + label + "']/parent::label"))
									.getAttribute("for");
						else
							try {
								forAttribute = driver.findElement(By.xpath("//label[text()='" + label + "']"))
										.getAttribute("for");
							} catch (Exception x) {
								forAttribute = driver
										.findElement(By.xpath("//span[text()='" + label + "']/parent::label"))
										.getAttribute("for");
							}

					}
				}
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

				// xpath
				xpath = String.format("//input[@id='%s']", forAttribute);
				if (dataType.equalsIgnoreCase("TextArea"))
					xpath = String.format("//textarea[@id='%s']", forAttribute);
				else if (dataType.equalsIgnoreCase("Picklist")) {
					if (isSubModule)
						xpath = String.format("(//*[@*='%s']/ancestor::div[@*='uiPopupTrigger'])[last()]",
								forAttribute);
					else
						xpath = String.format("//button[@id='%s']", forAttribute);
				}
				// datatype
				if (dataType.equalsIgnoreCase("String")) {
					if (StringUtils.equalsAnyIgnoreCase(label, "PostalCode", "Pincode")) {
						data = RandomStringUtils.randomNumeric(6);
						driver.findElement(By.xpath(xpath)).sendKeys(data);
					} else {
						data = moduleName.toLowerCase() + " " + RandomStringUtils.randomAlphabetic(10);
						driver.findElement(By.xpath(xpath)).sendKeys(data);
					}
				} else if (dataType.equalsIgnoreCase("Url")) {
					data = "www." + RandomStringUtils.randomAlphabetic(10) + ".com";
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("TextArea")) {
					data = moduleName.toLowerCase() + " " + getRandomWords(20, 5);
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Picklist")) {
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
							driver.findElement(By.xpath(xpath)));
					driver.findElement(By.xpath(xpath)).click();
					List<String> list = getJsonListValue(getPickListValueResponseStr,
							"$.actions[0].returnValue.picklistFieldValues." + fieldName + ".values[*]");
					String pickListValue = getJsonStringValue(getPickListValueResponseStr,
							"$.actions[0].returnValue.picklistFieldValues." + fieldName + ".values["
									+ getRandomNumber(0, list.size() - 1) + "].value");
					if (isSubModule) {
						String id = driver.findElement(By.xpath(xpath)).getAttribute("id");
						xpath = "(//*[@*='" + id + "']//*[text()='" + pickListValue + "'])[last()]";
					} else
						xpath = "//lightning-base-combobox-item//*[@title='" + pickListValue + "']";
					try {
						driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								driver.findElement(By.xpath(xpath)));
						driver.findElement(By.xpath(xpath)).click();
					} catch (Exception e) {
						pickListValue = getJsonStringValue(getPickListValueResponseStr,
								"$.actions[0].returnValue.picklistFieldValues." + fieldName + ".values[1].value");
						xpath = "//lightning-base-combobox-item//*[@title='" + pickListValue + "']";
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								driver.findElement(By.xpath(xpath)));
						driver.findElement(By.xpath(xpath)).click();
					}
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
					data = pickListValue;
				} else if (dataType.equalsIgnoreCase("Phone")) {
					data = "9" + RandomStringUtils.randomNumeric(9, 10);
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Email")) {
					data = RandomStringUtils.randomAlphabetic(10) + "@fireflink.com";
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Int")) {
					data = RandomStringUtils.randomNumeric(2);
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Currency")) {
					data = RandomStringUtils.randomNumeric(5);
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Date")) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					data = sdf.format(new Date());
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Double") || dataType.equalsIgnoreCase("Percent")) {
					driver.findElement(By.xpath(xpath)).clear();
					data = RandomStringUtils.randomNumeric(2) + "." + RandomStringUtils.randomNumeric(2);
					driver.findElement(By.xpath(xpath)).sendKeys(data);
				} else if (dataType.equalsIgnoreCase("Boolean")) {
					data = "true";
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
							driver.findElement(By.xpath(xpath)));
					driver.findElement(By.xpath(xpath)).click();
				} else if (dataType.equalsIgnoreCase("DateTime")) {
					System.err.println(dataType + " is defined in code base, Element Label ==> " + label);
				} else if (dataType.equalsIgnoreCase("Reference")) {
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
							driver.findElement(By.xpath(xpath)));
					driver.findElement(By.xpath(xpath)).click();
					driver.findElement(By.xpath(xpath)).click();
					Thread.sleep(1000);
					String idAttribute = driver.findElement(By.xpath(xpath)).getAttribute("id");
					boolean flag = false;
					if (isSubModule) {
						xpath = "(//input[@id='" + idAttribute
								+ "']/following-sibling::div//div[contains(@class,'createNew')])[last()]";
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								driver.findElement(By.xpath(xpath)));
						driver.findElement(By.xpath(xpath)).click();
						flag = true;
					} else {
						// xpath = "//lightning-base-combobox-item[contains(@data-item-id,'" +
						// idAttribute + "')]";
						// int size = driver.findElements(By.xpath(xpath)).size();
						// if (size != 0) {
						xpath = " //*[@*='" + idAttribute
								+ "']/following::*[@icon-name='utility:add']/ancestor::*[@role='option']";
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								driver.findElement(By.xpath(xpath)));
						driver.findElement(By.xpath(xpath)).click();
						flag = true;
						// }
					}
					if (flag) {
						Map<String, String> referenceMap = newObject(driver, relationshipName, "Mandatory", baseURL,
								map, true);
						driver.findElement(
								By.xpath("(//div[contains(@class,'footerAction')]//button[@title='Save'])[last()]"))
								.click();
						String text = driver
								.findElement(By.xpath("(//div[@*='Success']//*[@*='forceActionsText'])[last()]"))
								.getText();
						data = referenceMap.toString();
						driver.findElement(By.xpath(" (//div[@*='Success']//*[@title='Close'])[last()]")).click();
						Thread.sleep(2000);
					}
				} else {
					System.err.println(dataType + " is not defined in code base, Element Label ==> " + label);
				}
			} catch (Exception ee) {
				System.err.println(label + " having issue to interact");
				ee.printStackTrace();
				data = "---NA---";
			}
			dataMap.put(label, data);
		}
		return dataMap;
	}

	public static String getJsonStringValue(String jsonBody, String jsonPath) {
		String value = "";
		try {
			value = JsonPath.read(jsonBody, jsonPath).toString().trim();
		} catch (Exception e) {
		}
		return value;
	}

	public static List<String> getJsonListValue(String jsonBody, String jsonPath) {
		List<String> value = new ArrayList<>();
		try {
			value = JsonPath.read(jsonBody, jsonPath);
		} catch (Exception e) {
		}
		return value;
	}

	private static void waitAndClick(WebDriver driver, int duration, String xpath) {
		int count = 0;
		while (count < duration) {
			try {
				driver.findElement(By.xpath(xpath)).click();
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
					count++;
				} catch (InterruptedException ex) {
				}
			}

		}
	}

	private static int getRandomNumber(int min, int max) {
		double a = Math.random() * (max - min + 1) + min;
		int b = (int) (Math.random() * (max - min + 1) + min);
		return b;
	}

	private static String getRandomWords(int wordCount, int charCountInWord) {
		String randomWord = "";
		for (int i = 0; i < wordCount; i++) {
			randomWord = randomWord + RandomStringUtils.randomAlphabetic(charCountInWord) + " ";
		}
		return randomWord.trim();
	}

	private static String getRequestHeader(String reqUrl, String headerName, List<LogEntry> entries) throws Exception {

		String message = "";
		for (LogEntry entry : entries) {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonSessionPayload = objectMapper.readTree(entry.getMessage());
			String originURL = jsonSessionPayload.path("message").path("params").path("headers").path("origin")
					.asText();
			if (originURL.equalsIgnoreCase(reqUrl)) {
				message = jsonSessionPayload.path("message").path("params").path("headers").path(headerName).asText();
				if (!message.equalsIgnoreCase(""))
					break;
			}
		}
		return message;
	}

	private static String getRequestBody(String reqUrl, List<LogEntry> entries) throws Exception {
		String message = "";
		for (LogEntry entry : entries) {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonSessionPayload = objectMapper.readTree(entry.getMessage());
			String url = jsonSessionPayload.path("message").path("params").path("request").path("url").asText();
			if (url.equalsIgnoreCase(reqUrl)) {
				message = jsonSessionPayload.path("message").path("params").path("request").path("postData").asText();
				if (!message.equalsIgnoreCase(""))
					break;
			}
		}
		return message;
	}

	public static Response postRequestWithoutHeader(String baseUrl, String endPoint, String requestBody,
			String... headersKey_Value) {
		RequestSpecification requestSpecification = RestAssured.given();
		if (headersKey_Value.length > 0) {
			for (String keyValue : headersKey_Value) {
				String[] keyValueArr = keyValue.split("###");
				requestSpecification.header(keyValueArr[0], keyValueArr[1]);
			}
		}

		Response response = requestSpecification.body(requestBody).when().post(baseUrl + endPoint).then().extract()
				.response();
		return response;
	}

	public static Response getRequest(String baseUrl, String endPoint, String... headersKey_Value) {
		RequestSpecification requestSpecification = RestAssured.given();
		if (headersKey_Value.length > 0) {
			for (String keyValue : headersKey_Value) {
				String[] keyValueArr = keyValue.split("###");
				requestSpecification.header(keyValueArr[0], keyValueArr[1]);
			}
		}
		Response response = requestSpecification.contentType(ContentType.JSON).when().get(baseUrl + endPoint).then()
				.extract().response();

		return response;
	}
	
	/**
	 * Author: Karthick M
	 * Reason: Retry until Aura Data is found
	 * @param driver
	 * @param baseURL
	 * @return
	 * @throws Exception
	 */
	private static String waitForAuraRequest(WebDriver driver, String baseURL) throws Exception {

	    int maxAttempts = 5;
	    int attempt = 0;

	    while (attempt < maxAttempts) {
	        System.out.println("Attempt " + (attempt + 1) + " to capture aura request...");

	        // Wait a bit for network calls
	        Thread.sleep(4000);

	        List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();

	        String requestBody = extractAuraRequest(entries);

	        if (requestBody != null && !requestBody.isEmpty()) {
	            System.out.println("✅ Aura request captured!");
	            return requestBody;
	        }

	        // If not found → refresh
	        System.out.println("⚠️ Aura request not found, refreshing page...");
	        driver.navigate().refresh();

	        attempt++;
	    }

	    throw new Exception("Failed to capture aura request after " + maxAttempts + " attempts");
	}
	
	
	/**
	 * Author: Karthick M
	 * Reason: Checks for the Aura Data 
	 * @param entries
	 * @return
	 * @throws Exception
	 */
	private static String extractAuraRequest(List<LogEntry> entries) throws Exception {

	    for (LogEntry entry : entries) {
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode json = objectMapper.readTree(entry.getMessage());

	        String url = json.path("message")
	                         .path("params")
	                         .path("request")
	                         .path("url")
	                         .asText();

	        if (url.contains("/aura")) {

	            String postData = json.path("message")
	                                  .path("params")
	                                  .path("request")
	                                  .path("postData")
	                                  .asText();

	            if (postData != null && !postData.isEmpty()) {
	                System.out.println("Captured AURA URL: " + url);
	                return postData;
	            }
	        }
	    }

	    return null;
	}
	
	public static void main(String[] args) throws Exception {
	    NlpRequestModel nlpRequestModel = new NlpRequestModel();
	    
	    // Set attributes
	    Map<String, Object> attributes = nlpRequestModel.getAttributes();
	    attributes.put("BaseURL", "https://ruby-site-5132.lightning.force.com/");
	    attributes.put("FieldsToConsider(All,Mandatory,Optional)", "All");
	    attributes.put("ModuleName", "Leads");

	    // Configure ChromeOptions
	    ChromeOptions options = new ChromeOptions();
	    LoggingPreferences logPrefs = new LoggingPreferences();
	    logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
	    logPrefs.enable(LogType.BROWSER, Level.ALL);  // Capture browser console logs
	    options.setCapability("goog:loggingPrefs", logPrefs);
	    options.addArguments("--disable-notifications");
	    options.addArguments("--user-data-dir=C:\\Users\\User\\Desktop\\test");
	    
	    // Initialize RemoteWebDriver
	    WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
	    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

	    try {
	        // Maximize and navigate
	        driver.manage().window().maximize();
//	        driver.get("https://business-ruby-3289.lightning.force.com/lightning/o/Lead/list?filterName=AllOpenLeads");
	        driver.get("https://computing-nosoftware-61762.my.salesforce.com/");

	        // Wait for the login page to load
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	        wait.until(ExpectedConditions.elementToBeClickable(By.id("Login")));  // Refined wait logic
	        
	        // Enter username
	        WebElement usernameField = driver.findElement(By.id("username"));
//	        usernameField.sendKeys("mahesh.m-kxzh@force.com");
	        usernameField.clear();
	        usernameField.sendKeys("johnsmithfftrainer_sta185pqyfzzr95ycg@gmail.com");

	        // Enter password
	        WebElement passwordField = driver.findElement(By.id("password"));
	        passwordField.clear();
	        passwordField.sendKeys("Password@123");

	        // Click the login button
	        WebElement loginButton = driver.findElement(By.id("Login"));
	        loginButton.click();

	        // Wait for the Salesforce home page or specific element to load
	        wait.until(ExpectedConditions.urlContains("/lightning"));
	        
	        // Execute NLP operation
//	        nlpRequestModel.setWebDriver(driver);
//	        CreateAndSaveObject createAndSaveObject = new CreateAndSaveObject();
//	        NlpResponseModel response = createAndSaveObject.execute(nlpRequestModel);
	        createAndSaveObject(driver, "Leads", "All", "https://computing-nosoftware-61762.lightning.force.com/");

	        System.out.println("NLP execution completed successfully.");

	    } catch (Exception e) {
	        System.err.println("Error during execution: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
//	        driver.quit();
	    }
	}
	
}