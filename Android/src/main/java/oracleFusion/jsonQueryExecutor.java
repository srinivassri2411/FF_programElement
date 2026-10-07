package oracleFusion;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

public class jsonQueryExecutor implements Nlp {

	public static String text = "";
	public static String action = "";
	public static String input = "";
	public static WebDriver driver = null;
	public static WebDriverWait wait = null;
	public static Map<String, Object> map = null;
	public static int count = 0;
	public static int waitTime = 20;

	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "Map", type = "java.util.Map"),
			@InputParam(name = "Script Name", type = "java.lang.String") })
	@ReturnType(name = "Map", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("File Path");
		String scriptName = (String) programElementsInput.get("Script Name");
		map = (Map<String, Object>) programElementsInput.get("Map");
		// driver= nlpRequestModel.getWebDriver();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		String htmlFilePath = System.getProperty("java.io.tmpdir") + File.separator + "Oracle Fusion HTMLReport.html";

		// Initialize HTML
		HtmlReport report = new HtmlReport();

		try {
			Files.deleteIfExists(new File(htmlFilePath).toPath());
			String json = new String(Files.readAllBytes(Paths.get(filePath)));
			validateJson(json);
			List<Map<String, Object>> result = JsonPath.parse(json).read("$");

			// Print the result
			System.out.println(result);

			count = 1;

			iterate(result, report, scriptName);

		} catch (Exception e) {

			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
					"Failed", "", "", "Exception ==>> " + e.toString(), scriptName);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to execute the query \n Exception ==>> " + sw.toString());
		}

		// Save Html Report
		try {
			report.generateReport(htmlFilePath, scriptName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> list = new ArrayList<>();
		list.add(htmlFilePath);
		nlpResponseModel.setOutputLocalFilePaths(list);

		// Your program element business logic ends here ...
		nlpResponseModel.getAttributes().put("Map", map);
		return nlpResponseModel;
	}

	public static void iterate(List<Map<String, Object>> jsonArray, HtmlReport report, String scriptName)
			throws Exception {

		for (Map<String, Object> map : jsonArray) {
			action = map.get("action").toString();
			System.out.println(action);

			if (map.containsKey("text")) {
				text = map.get("text").toString();
				System.out.println(text);
			}

			if (map.containsKey("input")) {
				input = map.get("input").toString();
				System.out.println(input);
			}

			// call function
			try {
				executeAction(report, scriptName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}

			for (String key : map.keySet()) {
				if (key.startsWith("child_")) {
					List<Map<String, Object>> subJsonArray = (List<Map<String, Object>>) map.get(key);
					iterate(subJsonArray, report, scriptName); // Recursively process child elements
				}
			}
		}
	}

	// private static void enterDataUsingJs(String xpath, String input) {
	//
	// String script = """
	//
	// (async () => {
	//
	// const el = document.querySelector("%s");
	//
	// if (!el) return;
	//
	// el.click();
	//
	// el.focus();
	//
	// el.value = "";
	//
	// for (const char of "%s") {
	//
	// const e = { key: char, code: `Key${char.toUpperCase()}`, charCode:
	// char.charCodeAt(0), keyCode: char.charCodeAt(0), which: char.charCodeAt(0),
	// bubbles: true };
	//
	// el.dispatchEvent(new KeyboardEvent("keydown", e));
	//
	// el.value += char;
	//
	// el.dispatchEvent(new KeyboardEvent("keypress", e));
	//
	// el.dispatchEvent(new InputEvent("input", { bubbles: true }));
	//
	// el.dispatchEvent(new KeyboardEvent("keyup", e));
	//
	// await new Promise(r => setTimeout(r, 100));
	//
	// }
	//
	// el.dispatchEvent(new Event("change", { bubbles: true }));
	//
	// el.blur();
	//
	// })();
	//
	// """
	// .formatted(driver.findElement(By.xpath(xpath)), input);
	//
	// ((JavascriptExecutor) driver).executeScript(script);
	//
	// }
	//

	private static void clickUsingJs(String xpath) {
		String jsCode = """
				    const element = arguments[0];
				    ['mousedown', 'mouseup', 'click'].forEach(eventType => {
				        element.dispatchEvent(new MouseEvent(eventType, {
				            bubbles: true,
				            cancelable: true,
				            view: window
				        }));
				    });
				""";
		((JavascriptExecutor) driver).executeScript(jsCode, driver.findElement(By.xpath(xpath)));
	}

	private static void clickUsingJs2(String xpath) {
		String jsCode = """
				    const element = arguments[0];

				    if (!element) {
				        console.error("Element not found");
				        return;
				    }

				    // Scroll to the element
				    element.scrollIntoView({ behavior: "auto", block: "center" });

				    // Hover and focus
				    element.dispatchEvent(new MouseEvent("mouseover", { bubbles: true, cancelable: true }));
				    element.dispatchEvent(new MouseEvent("mousemove", { bubbles: true, cancelable: true }));
				    element.dispatchEvent(new FocusEvent("focus", { bubbles: true, cancelable: true }));

				    // Dispatch full click sequence
				    ['mousedown', 'mouseup', 'click'].forEach(type => {
				        element.dispatchEvent(new MouseEvent(type, {
				            bubbles: true,
				            cancelable: true,
				            view: window,
				            buttons: 1,
				            composed: true
				        }));
				    });

				    // Native click fallback
				    try {
				        Object.getPrototypeOf(element).click.call(element);
				    } catch (e) {
				        console.error("Native click fallback failed:", e);
				    }
				""";

		WebElement element = driver.findElement(By.xpath(xpath));
		((JavascriptExecutor) driver).executeScript(jsCode, element);
	}

	private static void clickUsingJsWithDelays(String xpath) {
		driver.navigate().refresh();
	}

	private static void waitUntilElementIsStable(WebElement element, int durationInMillis) {
		try {
			Point lastLocation = element.getLocation();
			Dimension lastSize = element.getSize();
			long start = System.currentTimeMillis();

			while (System.currentTimeMillis() - start < durationInMillis) {
				Thread.sleep(50);
				Point newLocation = element.getLocation();
				Dimension newSize = element.getSize();

				if (!newLocation.equals(lastLocation) || !newSize.equals(lastSize)) {
					lastLocation = newLocation;
					lastSize = newSize;
					start = System.currentTimeMillis(); // Reset timer
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void enterDataUsingJs(WebElement element, String input) {
		String script = """
				    const el = arguments[0];
				    const inputValue = arguments[1];

				    el.focus();
				    el.value = '';

				    for (const char of inputValue) {
				        const eventOptions = {
				            key: char,
				            code: `Key${char.toUpperCase()}`,
				            charCode: char.charCodeAt(0),
				            keyCode: char.charCodeAt(0),
				            which: char.charCodeAt(0),
				            bubbles: true
				        };

				        el.dispatchEvent(new KeyboardEvent('keydown', eventOptions));
				        el.value += char;
				        el.dispatchEvent(new KeyboardEvent('keypress', eventOptions));
				        el.dispatchEvent(new InputEvent('input', { bubbles: true }));
				        el.dispatchEvent(new KeyboardEvent('keyup', eventOptions));
				    }

				    el.dispatchEvent(new Event('change', { bubbles: true }));
				    el.blur();
				""";

		((JavascriptExecutor) driver).executeScript(script, element, input);
	}

	private static String generateDynamicXPath() throws Exception {
		List<String> xpathStrategies = new ArrayList<>();
		switch (action.toLowerCase().trim()) {

		case "click_on_button":
			xpathStrategies.add("//div[not(@style)]/*[normalize-space()='" + text + "']");
			break;

		case "click_on_terminated":
			xpathStrategies.add("(//a[contains(text(),'Recently Terminated')])[1]");
			break;

		case "click_on_okbutton":
			xpathStrategies.add(
					"(//BUTTON[normalize-space(@_afrpdo)='cancel']/preceding-sibling::BUTTON[normalize-space(@class)='xuz p_AFTextOnly'])[3]");
			break;

		case "click_on_branch":
			xpathStrategies.add("//a[@title='" + text + "']");
			break;

		case "click_on_banksave":
			xpathStrategies.add("//button[text()='ave and Close']");
			break;

		case "click_on_button2":
			// xpathStrategies.add("//div[not(@style)]/span[normalize-space()='"+text+"']");
			xpathStrategies.add("//div[not(@style)]/span[normalize-space()='" + text + "']/ancestor::oj-button");
			break;

		case "click_on_popup_button":
			xpathStrategies.add(
					"//div[contains(@id, 'TaskPopup::popup-container')]//button[normalize-space()='" + text + "']");
			break;

		case "click_on_popup_button2":
			xpathStrategies.add("//*[contains(@id,'msgDlg::cancel')] ");
			break;

		case "click_on_text_button":
			xpathStrategies.add("//button[text()='" + text + "']");
			break;

		case "click_on_error_button":
			xpathStrategies.add("//span[text()='" + text + "']/following::td/button[contains(@id,\"msgDlg::cancel\")]");
			break;

		case "click_on_menu":
			xpathStrategies.add("//a[@title='" + text + "']");
			break;

		case "click_on_refresh":
			xpathStrategies.add("//img[@title='" + text + "']");
			break;

		case "click_on_paymentsrefresh":
			// xpathStrategies.add("//DIV[DIV[TABLE[TBODY[TR[TD[DIV[H2[text()='" + text
			// + "']]]]]]]]/descendant::IMG[@title='Refresh']/parent::a");
			xpathStrategies.add("//div[@title='" + text + "']/../../../../../..//a[@title='Refresh']");
			break;

		case "click_on_side_icon":
			xpathStrategies.add("//div[@title='" + text + "']//img");
			break;

		case "click_on_icon":
			xpathStrategies.add("//img[@title='" + text + "']");
			break;

		case "click_on_search":
			xpathStrategies.add("//a[text()='" + text + "']");
			break;

		case "click_on_icon1":
			xpathStrategies.add("//a[text()='" + text
					+ "']/parent::span/../../../..//img[@title='Assign payment process attributes']");
			break;

		case "verify_img_displayed":
			xpathStrategies.add("//img[@title='" + text + "']");
			break;

		case "click_on_textfield":
			xpathStrategies.add("//label[text()='" + text + "']/preceding-sibling::input");
			break;

		case "click_on_dropdown":
			xpathStrategies
					.add("//label[text()='" + text + "']/ancestor::td/following-sibling::td[@valign='top']//select");
			break;

		case "click_on_requisitions_dropdown":
			xpathStrategies.add("//SELECT[.='" + text + "']  ");
			break;

		case "click_on_dropdown2":
			xpathStrategies.add("//a[text()='" + text + "']");
			break;

		case "click_on_dropdown3":
			xpathStrategies.add("//a[@title='" + text + "']");
			break;

		case "click_on_side_links1":
			xpathStrategies.add("//span[text()='System Holds']/parent::span/following::a[text()='" + text + "']");
			break;

		case "click_on_invoicecheckbox1":
			xpathStrategies.add("//span[text()='Quantity']/preceding::th//input[@type='checkbox']");
			break;

		case "click_on_invoicecheckbox2":
			xpathStrategies.add("//span[text()='" + text + "']/../../../../../../..//input[@type='checkbox']");
			break;

		case "click_on_text_button2":
			xpathStrategies.add("//a[@accesskey='" + text + "']");
			break;

		case "get_text1":
			xpathStrategies.add("//label[text()='" + text + "']");
			break;

		case "click_on_editable_dropdown":
			xpathStrategies.add("//label[text()='" + text + "' ]/ancestor::td/following-sibling::td[@valign='top']");
			break;

		case "click_on_editrequistion_dropdown":
			xpathStrategies.add("//input[@id=string(//label[span[contains(text(),'" + text + "')]]/@for)]");
			// xpathStrategies.add("(//label[span[contains(text(),'"+text+"')]]/../../following-sibling::input)[1]");
			break;

		case "click_on_editable_dropdown1":
			xpathStrategies.add(" //table//label[text()='" + text
					+ "']/ancestor::td/following-sibling::td[@valign='top']//input[contains(@id,'content')]");
			break;

		case "click_on_editable_dropdown2":
			xpathStrategies.add("//span[contains(text(),'" + text
					+ "')]/following::input[contains(@class,\"searchselect-input\")]");
			break;

		case "click_on_category_dropdown":
			xpathStrategies.add("//span[text()='" + text
					+ "']/ancestor::div[@class='oj-label-group']/parent::oj-label/following-sibling::input[@id='oj-selectsingle-2|input']");
			break;

		case "enter_on_requistion_dropdown":
			xpathStrategies.add("//input[@id = string(//label[span[normalize-space(text())='" + text + "']]/@for)]");
			break;

		case "enter_PPR":
			xpathStrategies.add("//input[@aria-label='" + text + "']");
			break;

		case "enter_category_dropdown":
			xpathStrategies.add("//span[text()='" + text
					+ "']/ancestor::div[@class='oj-label-group']/parent::oj-label/following-sibling::input[@id='oj-selectsingle-2|input']");
			break;

		case "click_on_dropdown_option":
			xpathStrategies.add(" //li[contains(text(),'" + text + "')]");
			xpathStrategies.add(" //td[text()='" + text + "']");

			break;

		case "click_on_dropdown_option1":
			xpathStrategies.add("//span[text()='" + text + "']");
			break;

		case "click_on_branchoption":
			xpathStrategies.add("(//td/span[text()='" + text + "'])[1]");
			break;

		case "click_on_dropdown_option2":
			xpathStrategies.add("//option[text()='" + text + "']");
			break;

		case "click_on_dropdown_option3":
			xpathStrategies.add("//span[contains(text(),'" + text + "')]");
			break;

		case "click_on_po_option":
			xpathStrategies.add("//li[@class='AFAutoSuggestItem']");
			break;

		case "click_on_selectarea":
			xpathStrategies.add("//td[@class='xwp']");
			break;

		case "click_on_paymentsselectarea":
			xpathStrategies.add("//td[@class='p_AFFocused xwp']");
			break;

		case "click_on_sublinks":
			xpathStrategies.add("//div[@_afrptkey]//a[text()='" + text + "']");
			break;

		case "click_on_menu_links":
			xpathStrategies.add("//span[text()='" + text + "']");
			xpathStrategies.add("//*[text()='" + text + "']");
			break;

		case "click_on_side_links":
			xpathStrategies.add("//a[text()='" + text + "']");
			break;

		case "click_on_invoicecheckbox":
			xpathStrategies.add("//th[contains(@class,'Selected')]//input[@type='checkbox']");
			break;

		case "click_on_supplierscheckbox":
			xpathStrategies.add("//label[text()='" + text + "']//div");
			break;

		case "enter_textfield":

			xpathStrategies.add("//input[@id=string(//label[text()='" + text + "']/@for)]");
			xpathStrategies.add("//input[@placeholder='" + text + "']");
			break;

		case "enter_textfield1":
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");
			break;

		case "enter_accountnumber":
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");

			// xpathStrategies.add("//input[@id=string(//label[text()='" + text +
			// "']/@for)]");
			break;

		case "enter_branch":
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");
			break;

		case "enter_3random_data":
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");
			break;

		case "enter_total":
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");
			break;

		case "enter_random_itemdescription":

			xpathStrategies.add("//textarea[@id=//label[span[text()='" + text + "']]/@for]");
			break;

		case "enter_textarea":
			xpathStrategies.add("//textarea[@id=string(//label[text()='" + text + "']/@for)]");
			break;

		case "enter_editable_dropdown2":
			// xpathStrategies.add("//TD[.='"+text+"']/preceding::SPAN[.='Open'] ");
			xpathStrategies.add(
					"//span[contains(text(),'" + text + "')]/following::input[contains(@class,'searchselect-input')]");
			break;

		case "clear_and_enter":
			xpathStrategies.add("//*[text()='" + text + "']/ancestor::oj-input-text");
			break;

		case "clear_text_if_exists":
			xpathStrategies.add(" //table//label[text()='" + text
					+ "']/ancestor::td/following-sibling::td[@valign='top']//input[contains(@id,'content')]");
			break;

		case "clear_and_enter_inrange":
			xpathStrategies.add("//input[@id=string(//label[text()='" + text + "']/@for)]");
			break;

		case "enter_quantity":
			xpathStrategies.add("//span[text()='" + text + "']/ancestor::oj-label/following-sibling::input");
			break;

		case "enter_random_data":
			xpathStrategies.add("//input[@id=string(//label[text()='" + text + "']/@for)]");
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");

			break;

		case "enter_random_data1":
			xpathStrategies.add("//input[@id=string(//label[text()='" + text + "']/@for)]");
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");

			break;

		case "enter_data_js":
			xpathStrategies.add("//input[@id=string(//label[text()='" + text + "']/@for)]");
			xpathStrategies.add("//label[text()='" + text + "']/parent::td/following-sibling::td//input");

			break;

		case "get_requisitionid_text":
			xpathStrategies.add("//h1[contains(@id,'pageTitle')]");
			break;

		case "verify_text":// suppliers popup text verification
			xpathStrategies.add("//div[text()='" + text + "']");
			xpathStrategies.add("//a[text()='" + text + "']");
			break;

		case "verify_pagetext":// suppliers module page text verification
			xpathStrategies.add("//h1[text()='" + text + "']");
			break;

		case "verify_postatus":
			xpathStrategies.add("//label[text()='" + text + "']/../following-sibling::td/descendant::a");
			xpathStrategies.add("//a//span[text()='" + text + "']");
			break;

		case "verify_approved":
			xpathStrategies.add("//span[text()='" + text.split(",")[0].trim() + "']/ancestor::div//span[text()='"
					+ text.split(",")[1].trim() + "']");
			break;

		/*
		 * case "press_enter_key": xpathStrategies.add(""); break;
		 */

		case "get_text":
			xpathStrategies.add("//div[@title='" + text + "']/preceding-sibling::div");
			break;

		case "get_number_from_text":
			xpathStrategies.add("//div[contains(text(),'" + text + "')]");
			break;

		case "wait_until_displayed":
			System.out.println("Obtained Text::" + text);
			String wait_till_xpath = "//span[contains(text(),'" + text
					+ "')]/parent::div/preceding-sibling::div//span[text()='Approved']";
			return wait_till_xpath;

		default:
			xpathStrategies.add("//*[text()='" + text + "']");
		}

		System.out.println("xpathStrategies : " + xpathStrategies);
		if (!action.toLowerCase().equals("click_on_error_button")) {
			customWait(xpathStrategies);
		}
		// Find the first valid XPath
		for (String xpath : xpathStrategies) {

			List<WebElement> elements = driver.findElements(By.xpath(xpath));
			if (!elements.isEmpty()) {
				return xpath; // Return the first valid XPath
			}
		}
		return null; // No valid XPath found
	}

	private static void executeAction(HtmlReport report, String scriptName) throws Exception {

		String variableName = "";
		WebElement element = null;

		if (action.isEmpty()) {
			System.out.println("Skipping invalid action: ");
			report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
					"Failed", "", "", "Skipping invalid action", scriptName);
		} else {
			try {
				String xpath = null;
				if (!text.isEmpty() && !text.equals("NA")) {
					if (text.startsWith("VAR")) {
						variableName = text.split("#")[1];
						System.out.println("Map data:: " + map);
						System.out.println("Variable data:: " + variableName);
						System.out.println("Output:" + map.get(variableName));
						text = map.get(variableName).toString();
						// text = variableName;
					}
					xpath = generateDynamicXPath();
					// System.out.println("xpath: "+xpath);

					if (xpath == null) {

						System.out.println("No matching element found for action: " + action + " with text: " + text);
						// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
						// Text:"+text, "Failed" , "","", "No matching element found for action: " +
						// action + " with text: " + text, scriptName);

					}
				} else if (text.isEmpty()) {
					xpath = generateDynamicXPath();
					if (xpath == null) {
						System.out.println("No matching element found for action: " + action + " with text: " + text);
					}
				}

				switch (action.toLowerCase().trim()) {

				case "static_wait":
					long num = Long.parseLong(input);
					Thread.sleep(num);
					report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input, "Passed",
							xpath, "", "Successfully Waited", scriptName);
					break;

				case "click_on_button":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_terminated":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_okbutton":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_search":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_branch":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_error_button":
					System.out.println(xpath);
					try {
						driver.findElement(By.xpath(xpath));
						if (driver.findElement(By.xpath(xpath)).isDisplayed()) {
							try {
								Thread.sleep(1000); // Optional wait
								try {
									driver.findElement(By.xpath(xpath)).click();
								} catch (Exception e) {
									JavascriptExecutor js = (JavascriptExecutor) driver;
									js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
								}
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Element Clicked Successfully", scriptName);
								System.out.println("Clicked on: " + text);
							} catch (Exception e) {
								StringWriter sw = new StringWriter();
								PrintWriter pw = new PrintWriter(sw);
								e.printStackTrace(pw);
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Failed", xpath, "",
										"Failed to Click on Element \n Exception ==>> " + sw.toString(), scriptName);
							}
						} else {
							System.out.println("Element not displayed for: " + text + " --> Skipping click");
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Skipped", xpath, "", "Element not displayed, skipping click", scriptName);
						}
					} catch (NoSuchElementException ne) {
						System.out.println("Element not found for: " + text + " --> Skipping step");
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Skipped", xpath, "", "Element not found, skipping click", scriptName);
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Unexpected failure during click \n Exception ==>> " + sw.toString(), scriptName);
					}
					break;

				case "click_on_button2":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_banksave":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_textfield":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_popup_button":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_popup_button2":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_text_button":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_text_button2":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							System.out.println("JS CLICKING");
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_menu":
					System.out.println(xpath);
					try {
						try {
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

//				case "click_on_refresh":
//					System.out.println(xpath);
//					try {
//						try {
//							clickUsingJs(xpath);
//							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
//									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
//									scriptName);
//						} catch (Exception e) {
//							JavascriptExecutor js = (JavascriptExecutor) driver;
//							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
//							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
//									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
//									scriptName);
//						}
//					} catch (Exception ee) {
//						StringWriter sw = new StringWriter();
//						PrintWriter pw = new PrintWriter(sw);
//						ee.printStackTrace(pw);
//						report.addStep(
//								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
//								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
//								scriptName);
//
//						throw ee;
//					}
//					System.out.println("Clicked on: " + text);
//					break;

				case "click_on_paymentsrefresh":
					System.out.println(xpath);
					try {
						String PaymentName = (String) map.get("PaymentName");
						System.out.println(PaymentName);
						try {
							while (true) {
								try {
									driver.findElement(By.xpath("//a[text()='" + PaymentName + "']")).isDisplayed();
									System.out.println("Stopped");
									break;
								} catch (Exception e) {
									driver.findElement(By.xpath(xpath)).click();
								}
								Thread.sleep(500);
							}

							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							System.out.println(e);
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_side_icon":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(2000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_icon":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_icon1":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_dropdown":
					System.out.println(xpath);

					try {
						try {
							System.out.println(element);
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									driver.findElement(By.xpath(xpath)));
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					break;

				case "click_on_dropdown3":
					System.out.println(xpath);

					try {
						try {
							System.out.println(element);
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									driver.findElement(By.xpath(xpath)));
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					break;

				case "click_on_requisitions_dropdown":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_editrequistion_dropdown":
					System.out.println(xpath);
					try {
						try {

							String jsCode = """
									    const element = arguments[0];
									    ['mousedown', 'mouseup', 'click'].forEach(eventType => {
									        element.dispatchEvent(new MouseEvent(eventType, {
									            bubbles: true,
									            cancelable: true,
									            view: window
									        }));
									    });
									""";
							((JavascriptExecutor) driver).executeScript(jsCode, driver.findElement(By.xpath(xpath)));

							// new
							// Actions(driver).moveToElement(driver.findElement(By.xpath(xpath))).click().perform();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_dropdown2":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_editable_dropdown":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_editable_dropdown1":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_editable_dropdown2":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_category_dropdown":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_dropdown_option":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_po_option":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_dropdown_option1":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_branchoption":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_dropdown_option2":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_dropdown_option3":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_selectarea":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_paymentsselectarea":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_sublinks":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_side_links":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Element Clicked Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on Element \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_menu_links":
					System.out.println(xpath);
					try {
						try {
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).click();
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Clicked on icon Successfully",
									scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Clicked on icon Successfully",
									scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Click on icon \n Exception ==>> " + sw.toString(),
								scriptName);

						throw ee;
					}
					System.out.println("Clicked on: " + text);
					break;

				case "click_on_supplierscheckbox":
					try {

						if (!driver.findElement(By.xpath(xpath)).isSelected()) {
							try {
								driver.findElement(By.xpath(xpath)).click();
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);

							} catch (Exception e) {
								JavascriptExecutor js = (JavascriptExecutor) driver;
								js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);

							}
							System.out.println("Checked checkbox: " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to click on Checkbox", scriptName);
						throw e;
					}
					break;

				case "click_on_invoicecheckbox":
					try {
						if (!driver.findElement(By.xpath(xpath)).isSelected()) {
							try {
								driver.findElement(By.xpath(xpath)).click();
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);
							} catch (Exception e) {
								JavascriptExecutor js = (JavascriptExecutor) driver;
								js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);
							}
							System.out.println("Checked checkbox: " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to click on Checkbox", scriptName);
						throw e;
					}
					break;

				case "click_on_invoicecheckbox1":
					try {
						if (!driver.findElement(By.xpath(xpath)).isSelected()) {
							try {
								driver.findElement(By.xpath(xpath)).click();
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);
							} catch (Exception e) {
								JavascriptExecutor js = (JavascriptExecutor) driver;
								js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);
							}
							System.out.println("Checked checkbox: " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to click on Checkbox", scriptName);
						throw e;
					}
					break;

				case "click_on_invoicecheckbox2":
					try {
						if (!driver.findElement(By.xpath(xpath)).isSelected()) {
							try {
								driver.findElement(By.xpath(xpath)).click();
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);
							} catch (Exception e) {
								JavascriptExecutor js = (JavascriptExecutor) driver;
								js.executeScript("arguments[0].click();", driver.findElement(By.xpath(xpath)));
								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Clicked on checkbox Successfully", scriptName);
							}
							System.out.println("Checked checkbox: " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to click on Checkbox", scriptName);
						throw e;
					}
					break;

				case "enter_textfield":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);

							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);

							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_PPR":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);

							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);

							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_branch":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_textfield1":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_editable_dropdown2":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_on_requistion_dropdown":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_textarea":
					try {
						System.out.println(xpath);

						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and entered the input successfully", scriptName);

							System.out.println("Cleared and entered text: " + input + " into " + text);
						} else if (input != null) {
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and entered the input successfully", scriptName);

							System.out.println("Cleared and entered text: " + input + " into " + text);
						}
					} catch (StaleElementReferenceException se) {
						System.out.println("StaleElementReferenceException: Retrying with re-located element...");
						driver.findElement(By.xpath(xpath)).clear();
						Thread.sleep(300);
						driver.findElement(By.xpath(xpath)).sendKeys(input);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_category_dropdown":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				/*
				 * case "enter_textarea": try { if (input.startsWith("VAR")) { variableName =
				 * input.split("#")[1].trim(); System.out.println(xpath);
				 * wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
				 * element .clear(); Thread.sleep(300); element
				 * .sendKeys(map.get(variableName).toString());
				 * report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "
				 * +input+", Text:"+text, "Passed" , xpath,"",
				 * "Cleared and Enter the inpur Successfully", scriptName);
				 * 
				 * System.out.println("Cleared and Entered text: " + input + " into " + text); }
				 * else if (input != null) { System.out.println(xpath);
				 * wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
				 * element .clear(); Thread.sleep(300); element .sendKeys(input);
				 * report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "
				 * +input+", Text:"+text, "Passed" , xpath,"",
				 * "Cleared and Enter the input Successfully", scriptName);
				 * 
				 * System.out.println("Cleared and Entered text: " + input + " into " + text); }
				 * } catch(Exception e) { StringWriter sw=new StringWriter(); PrintWriter pw=new
				 * PrintWriter(sw); e.printStackTrace(pw);
				 * report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "
				 * +input+", Text:"+text, "Failed" , xpath,"",
				 * "Failed to clear and Enter the input \n Exception ==>> "+sw.toString(),
				 * scriptName); throw e; } break;
				 */

				/*
				 * case "enter_random_data": try { variableName = input.split("#")[0].trim();
				 * String prefix = input.split("#")[1].trim(); if (input != null) {
				 * System.out.println(xpath);
				 * wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
				 * Thread.sleep(500); element .click(); Thread.sleep(1000); element .clear();
				 * Thread.sleep(300); int length = 4; long min = (long) Math.pow(10, length -
				 * 1); long max = (long) Math.pow(10, length) - 1;
				 * 
				 * Random random = new Random(); long randomNumber = min + (long)
				 * (random.nextDouble() * (max - min + 1)); Thread.sleep(1000); element
				 * .sendKeys(prefix + randomNumber);
				 * report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "
				 * +input+", Text:"+text, "Passed" , xpath,"", "Enter the input Successfully",
				 * scriptName); map.put(variableName, prefix + randomNumber);
				 * System.out.println("Cleared and Entered text: " + input + " into " + text); }
				 * } catch(Exception e) { StringWriter sw=new StringWriter(); PrintWriter pw=new
				 * PrintWriter(sw); e.printStackTrace(pw); System.out.println("failed");
				 * report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "
				 * +input+", Text:"+text, "Failed" , xpath,"",
				 * "Failed to Enter the input \n Exception ==>> "+sw.toString(), scriptName);
				 * throw e; }
				 * 
				 * break;
				 */

				case "enter_random_data":
					try {
						variableName = input.split("#")[0].trim();
						String prefix = input.split("#")[1].trim();

						if (input != null && xpath != null) {
							System.out.println(xpath);

							// Generate random 4-digit number
							int length = 5;
							long min = (long) Math.pow(10, length - 1);
							long max = (long) Math.pow(10, length) - 1;
							long randomNumber = min + (long) (new Random().nextDouble() * (max - min + 1));

							Thread.sleep(2000);
							driver.findElement(By.xpath(xpath)).sendKeys(prefix + randomNumber);
							Thread.sleep(2000);

							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Enter the input Successfully",
									scriptName);

							map.put(variableName, prefix + randomNumber);
							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						System.out.println("failed");

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Enter the input \n Exception ==>> " + sw.toString(),
								scriptName);

						throw e;
					}
					break;

				case "enter_random_data1":
					try {
						String prefix = "";
						variableName = input;

						// Check if input contains '#', else treat the entire input as variableName and
						// use default prefix
						if (input.contains("#")) {
							String[] parts = input.split("#");
							variableName = parts[0].trim();
							prefix = parts[1].trim();
						} else {
							prefix = "AUTO"; // Default prefix if not provided
						}

						if (input != null && xpath != null) {
							System.out.println(xpath);

							// Generate random 5-digit number
							int length = 5;
							long min = (long) Math.pow(10, length - 1);
							long max = (long) Math.pow(10, length) - 1;
							long randomNumber = min + (long) (new Random().nextDouble() * (max - min + 1));

							String finalValue = prefix + randomNumber;

							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(finalValue);
							Thread.sleep(300);

							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Entered the input Successfully", scriptName);

							map.put(variableName, finalValue);
							System.out.println("Cleared and Entered text: " + finalValue + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				// case "enter_accountnumber":
				// try {
				// String prefix = "";
				// variableName = input;
				//
				// if (input.contains("#")) {
				// String[] parts = input.split("#");
				// variableName = parts[0].trim();
				// prefix = parts[1].trim();
				// } else {
				// prefix = "AUTO";
				// }
				//
				// if (input != null && xpath != null) {
				// System.out.println("Using XPath: " + xpath);
				//
				// // Generate 8-digit number
				// int length = 8;
				// long min = (long) Math.pow(10, length - 1);
				// long max = (long) Math.pow(10, length) - 1;
				// long randomNumber = min + (long) (new Random().nextDouble() * (max - min +
				// 1));
				// String finalValue = prefix + randomNumber;
				//
				// WebElement element1 = driver.findElement(By.xpath(xpath));
				//
				// ((JavascriptExecutor)
				// driver).executeScript("arguments[0].scrollIntoView(true);", element1);
				// Thread.sleep(300);
				//
				// if (element1.isDisplayed() && element1.isEnabled()) {
				// try {
				// element1.clear(); // standard clear
				// } catch (org.openqa.selenium.ElementNotInteractableException ex) {
				// System.out.println("Standard clear() failed, trying JS clear()");
				// ((JavascriptExecutor) driver).executeScript("arguments[0].value='';",
				// element1);
				// }
				//
				// element1.sendKeys(finalValue);
				// Thread.sleep(300);
				//
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// "Passed", xpath, "", "Cleared and Entered the input Successfully",
				// scriptName);
				//
				// map.put(variableName, finalValue);
				// System.out.println("Cleared and Entered text: " + finalValue + " into " +
				// text);
				// } else {
				// throw new ElementNotInteractableException("Element not interactable: either
				// not displayed or not enabled.");
				// }
				// }
				//
				// } catch (Exception e) {
				// StringWriter sw = new StringWriter();
				// PrintWriter pw = new PrintWriter(sw);
				// e.printStackTrace(pw);
				//
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// "Failed", xpath, "", "Failed to clear and Enter the input \n Exception ==>> "
				// + sw.toString(),
				// scriptName);
				// throw e;
				// }
				// break;

				case "enter_accountnumber":
					try {
						String prefix = "";
						variableName = input;

						// Split input into variable and prefix
						if (input.contains("#")) {
							String[] parts = input.split("#");
							variableName = parts[0].trim();
							prefix = parts[1].trim();
						}

						if (input != null && xpath != null) {
							System.out.println("Xpath for input: " + xpath);

							// Generate random 8-digit number
							int length = 8;
							long min = (long) Math.pow(10, length - 1);
							long max = (long) Math.pow(10, length) - 1;
							long randomNumber = min + (long) (new Random().nextDouble() * (max - min + 1));

							String finalValue = prefix + randomNumber;

							WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
							WebElement element1 = wait
									.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
							wait.until(ExpectedConditions.elementToBeClickable(element1));

							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element1);
							Thread.sleep(300); // Allow scroll

							if (element1.isDisplayed() && element1.isEnabled()) {
								element1.click(); // Focus
								Thread.sleep(200);
								element1.clear();
								Thread.sleep(200);
								// element1.sendKeys(finalValue);
								enterDataUsingJs(element1, finalValue);
								Thread.sleep(200);

								report.addStep(
										"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
												+ text,
										"Passed", xpath, "", "Cleared and Entered the input Successfully", scriptName);

								map.put(variableName, finalValue);
								System.out.println("Cleared and Entered text: " + finalValue + " into " + text);
							} else {
								throw new ElementNotInteractableException(
										"Element found but not interactable: " + xpath);
							}
						}

					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "enter_data_js":
					try {
						variableName = input.split("#")[0].trim();
						String prefix = input.split("#")[1].trim();

						if (input != null && xpath != null) {
							System.out.println(xpath);

							// Generate random 4-digit number
							int length = 5;
							long min = (long) Math.pow(10, length - 1);
							long max = (long) Math.pow(10, length) - 1;
							long randomNumber = min + (long) (new Random().nextDouble() * (max - min + 1));

							Thread.sleep(1000);
							WebElement element1 = driver.findElement(By.xpath(xpath));
							enterDataUsingJs(element1, prefix + randomNumber);

							Thread.sleep(1000);

							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Enter the input Successfully",
									scriptName);

							map.put(variableName, prefix + randomNumber);
							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						System.out.println("failed");

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Enter the input \n Exception ==>> " + sw.toString(),
								scriptName);

						throw e;
					}
					break;

				case "enter_3random_data":
					try {
						variableName = input.split("#")[0].trim();
						String prefix = input.split("#")[1].trim();

						if (input != null && xpath != null) {
							System.out.println(xpath);

							int length = 3;
							long min = (long) Math.pow(10, length - 1);
							long max = (long) Math.pow(10, length) - 1;
							long randomNumber = min + (long) (new Random().nextDouble() * (max - min + 1));

							Thread.sleep(2000);
							driver.findElement(By.xpath(xpath)).sendKeys(prefix + randomNumber);
							Thread.sleep(2000);

							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Enter the input Successfully",
									scriptName);

							map.put(variableName, prefix + randomNumber);
							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						System.out.println("failed");

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Enter the input \n Exception ==>> " + sw.toString(),
								scriptName);

						throw e;
					}
					break;

				case "enter_randomalphabet_data":
					try {
						variableName = input.split("#")[0].trim();
						String prefix = input.split("#")[1].trim();
						if (input != null) {
							System.out.println(xpath);
							Thread.sleep(500);
							driver.findElement(By.xpath(xpath)).click();
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);

							// Generate random alphabet string of length 6
							int length = 6;
							String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
							StringBuilder randomString = new StringBuilder();
							Random random = new Random();

							for (int i = 0; i < length; i++) {
								int index = random.nextInt(alphabet.length());
								randomString.append(alphabet.charAt(index));
							}

							String finalValue = prefix + randomString.toString();
							driver.findElement(By.xpath(xpath)).sendKeys(finalValue);
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Entered the input Successfully",
									scriptName);
							map.put(variableName, prefix + finalValue);
							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Enter the input \n Exception ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				case "enter_random_itemdescription":
					try {
						variableName = input.split("#")[0].trim();
						String prefix = input.split("#")[1].trim();
						if (input != null) {
							System.out.println(xpath);
							Thread.sleep(500);
							driver.findElement(By.xpath(xpath)).click();
							Thread.sleep(1000);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							int length = 4;
							long min = (long) Math.pow(10, length - 1);
							long max = (long) Math.pow(10, length) - 1;

							Random random = new Random();
							long randomNumber = min + (long) (random.nextDouble() * (max - min + 1));
							driver.findElement(By.xpath(xpath)).sendKeys(prefix + randomNumber);
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Enter the input Successfully",
									scriptName);
							map.put(variableName, prefix + randomNumber);
							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Enter the input \n Exception ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				case "clear_and_enter":
					try {
						if (input.startsWith("VAR")) {
							variableName = input.split("#")[1].trim();
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(map.get(variableName).toString());
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the inpur Successfully", scriptName);

							System.out.println("Cleared and Entered text: " + input + " into " + text);
						} else if (input != null) {
							System.out.println(xpath);
							driver.findElement(By.xpath(xpath)).clear();
							Thread.sleep(300);
							driver.findElement(By.xpath(xpath)).sendKeys(input);
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared and Enter the input Successfully", scriptName);
							System.out.println("Cleared and Entered text: " + input + " into " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and Enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "clear_text_if_exists":
					try {
						System.out.println(xpath);
						String existingValue = driver.findElement(By.xpath(xpath)).getDomAttribute("value");
						if (existingValue != null && !existingValue.isEmpty()) {
							driver.findElement(By.xpath(xpath)).clear();
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "Cleared existing text successfully", scriptName);
							System.out.println("Cleared text from: " + text);
						} else {
							report.addStep(
									"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:"
											+ text,
									"Passed", xpath, "", "No text to clear, field already empty", scriptName);
							System.out.println("No existing text in: " + text);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to clear text\nException ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				// case "wait_until_displayed":
				// try {
				//// driver.findElement(By.xpath(xpath)).isDisplayed();
				// boolean isDisplayed = false;
				// System.out.println("Start Action and xpath:: "+xpath);
				//
				// for (int i = 0; i < 100; i++) {
				// Thread.sleep(5000);
				//
				// try {
				// System.out.println("Action Try");
				// if (driver.findElement(By.xpath(xpath)).isDisplayed()) {
				// isDisplayed = true;
				// break;
				// }
				// } catch (Exception e) {
				//
				// }
				// System.out.println("Stop Action");
				//
				// driver.navigate().refresh(); // Refresh page if element is not visible
				// //System.out.println("Refreshed the page - Attempt " + (i + 1));
				// }
				//
				// if (isDisplayed) {
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// "Passed", xpath, "", "Element is now visible after refresh", scriptName);
				// System.out.println("Element displayed successfully: " + text);
				// } else {
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// "Failed", xpath, "", "Element not displayed after retries", scriptName);
				// System.out.println("Element was not displayed after multiple refresh
				// attempts.");
				// }
				//
				// } catch (Exception e) {
				// StringWriter sw = new StringWriter();
				// PrintWriter pw = new PrintWriter(sw);
				// e.printStackTrace(pw);
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// "Failed", xpath, "", "Exception while waiting for element\nException ==>> " +
				// sw.toString(), scriptName);
				// throw e;
				// }
				// break;
				// case "wait_until_displayed":
				// try {
				// boolean isDisplayed = false;
				// System.out.println("Start Action and xpath:: " + xpath);
				//
				// // Optional: total retries and sleep duration can be adjusted
				// final int maxRetries = 30;
				// final int sleepMillis = 3000;
				//
				// for (int i = 0; i < maxRetries; i++) {
				// driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
				// try {
				// System.out.println("Attempt " + (i + 1));
				// if (driver.findElement(By.xpath(xpath)).isDisplayed()) {
				// isDisplayed = true;
				// break;
				// }
				// } catch (Exception e) {
				// System.out.println("Element not found or not displayed yet: " +
				// e.getMessage());
				// }
				//
				//// Thread.sleep(sleepMillis);
				// driver.navigate().refresh();
				// waitForPageToLoad(driver); // Ensure full page load after refresh
				//// Thread.sleep(sleepMillis);
				// }
				// driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
				//
				// System.out.println("Stop Action");
				//
				// String stepResult = isDisplayed ? "Passed" : "Failed";
				// String message = isDisplayed ?
				// "Element is now visible after refresh" :
				// "Element not displayed after " + maxRetries + " retries";
				//
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// stepResult, xpath, "", message, scriptName);
				//
				// System.out.println(message);
				//
				// } catch (Exception e) {
				// StringWriter sw = new StringWriter();
				// e.printStackTrace(new PrintWriter(sw));
				// report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				// input + ", Text:" + text,
				// "Failed", xpath, "", "Exception while waiting for element\nException ==>> " +
				// sw.toString(), scriptName);
				// throw e;
				// }
				// break;

				case "wait_until_displayed":
					try {
						boolean isDisplayed = false;
						System.out.println("Start Action and xpath:: " + xpath);

						final int maxRetries = 30;
						final int sleepMillis = 3000;

						// Helper function: Wait for page to be fully loaded (inline function)
						Supplier<Boolean> waitForPageToLoad = () -> {
							try {
								new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> ((JavascriptExecutor) d)
										.executeScript("return document.readyState").equals("complete"));
								return true;
							} catch (Exception ex) {
								System.out.println("Page did not load completely: " + ex.getMessage());
								return false;
							}
						};

						for (int i = 0; i < maxRetries; i++) {
							try {
								// Set implicit wait before trying
								driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
								System.out.println("Attempt " + (i + 1));

								if (driver.findElement(By.xpath(xpath)).isDisplayed()) {
									isDisplayed = true;
									break;
								}
							} catch (Exception e) {
								System.out.println("Element not found or not yet visible: " + e.getMessage());
							}

							driver.navigate().refresh();
							waitForPageToLoad.get(); // Call inline helper
							Thread.sleep(sleepMillis);
						}

						// Reset implicit wait to zero (optional)
						driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

						System.out.println("Stop Action");

						String stepResult = isDisplayed ? "Passed" : "Failed";
						String message = isDisplayed ? "Element is now visible after refresh"
								: "Element not displayed after " + maxRetries + " retries";

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								stepResult, xpath, "", message, scriptName);

						System.out.println(message);

					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Exception while waiting for element\nException ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				/*
				 * case "press_enter_key": try {
				 * wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
				 * element = driver.findElement(By.xpath(xpath)); element .sendKeys(Keys.ENTER);
				 * // Press Enter key
				 * 
				 * report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " +
				 * input + ", Text:" + text, "Passed", xpath, "",
				 * "Pressed Enter key successfully", scriptName);
				 * 
				 * System.out.println("Pressed Enter on: " + text); } catch (Exception e) {
				 * StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);
				 * e.printStackTrace(pw); report.addStep("step " + (count++) + " ==> Action: " +
				 * action + ", Input: " + input + ", Text:" + text, "Failed", xpath, "",
				 * "Failed to press Enter key\nException ==>> " + sw.toString(), scriptName);
				 * throw e; } break;
				 */

				case "press_enter_key":
					try {
						Actions actions = new Actions(driver);
						actions.sendKeys(Keys.ENTER).perform(); // Press Enter globally

						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Passed", "", "", "Pressed Enter key successfully", scriptName);

						System.out.println("Pressed Enter key globally");
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", "", "", "Failed to press Enter key\nException ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				case "verify_text":
					try {
						String message = driver.findElement(By.xpath(xpath)).getText();
						boolean result = message.contains(text);
						if (result == true) {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Resource Deleted Successfully",
									scriptName);

							System.out.println("Verified successfully: " + input + " into " + text);
						} else {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Failed", xpath, "", "Failed to Delete a resource ",
									scriptName);

						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
						// Text:"+text, "Failed" , xpath,"", "Failed to Delete a resource \n Exception
						// ==>> "+sw.toString(), scriptName);
						throw e;
					}
					break;

				case "verify_pagetext":
					try {
						String message = driver.findElement(By.xpath(xpath)).getText();
						System.out.println(message);
						boolean result = text.contains(message);
						System.out.println(result);
						if (result == true) {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Resource Deleted Successfully",
									scriptName);

							System.out.println("Created successfully: " + input + " into " + text);
						} else {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Failed", xpath, "", "Failed to Delete a resource ",
									scriptName);

						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
						// Text:"+text, "Failed" , xpath,"", "Failed to Delete a resource \n Exception
						// ==>> "+sw.toString(), scriptName);
						throw e;
					}
					break;

				// case "verify_postatus":
				// boolean resultopen = false;
				// int times = 0;
				// try {
				// while (times< Integer.parseInt(input)) {
				// String message = driver.findElement(By.xpath(xpath)).getText();
				// if (message.equals("Open")) {
				// resultopen = true;
				// break;
				// } else {
				// Thread.sleep(1000);
				// driver.findElement(By.xpath("//button[text()='Refresh']")).click();
				// times++;
				// }
				// }
				//
				// if (resultopen) {
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text, "Passed" , xpath,"", "Resource Updated Successfully",
				// scriptName);
				// System.out.println("Created successfully: " + input + " into " + text);
				// } else {
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text, "Failed" , xpath,"", "Failed to update a resource ",
				// scriptName);
				// }
				// }catch(Exception e) {
				// StringWriter sw=new StringWriter();
				// PrintWriter pw=new PrintWriter(sw);
				// e.printStackTrace(pw);
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text, "Failed" , xpath,"", "Failed to Update a resource \n Exception
				// ==>> "+sw.toString(), scriptName);
				// throw e;
				// }
				// break;

				case "verify_postatus":
					boolean resultopen = false;
					int times = 0;
					try {
						int maximumRetries = Integer.parseInt(input);
						while (times < maximumRetries) {
							try {
								// Re-locate every time to avoid stale references
								String message = driver.findElement(By.xpath(xpath)).getText();
								System.out.println("Status check #" + (times + 1) + ": " + message);

								if (message.equalsIgnoreCase("Open")) {
									resultopen = true;
									break;
								} else {
									Thread.sleep(1000);
									driver.findElement(By.xpath("//button[text()='Refresh']")).click();
									Thread.sleep(1000); // wait for page to refresh
									times++;
								}
							} catch (StaleElementReferenceException sere) {
								System.out.println("Caught stale element, retrying...");
								Thread.sleep(500); // wait a bit before retrying
								// retry the same loop iteration
							}
						}

						if (resultopen) {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Resource Updated Successfully",
									scriptName);
							System.out.println("Created successfully: " + input + " into " + text);
						} else {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Failed", xpath, "", "Failed to update a resource", scriptName);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Update a resource \n Exception ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				case "verify_approved":
					try {
						String message = driver.findElement(By.xpath(xpath)).getText();
						boolean result = message.contains(text);
						if (result == true) {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "Resource Updated Successfully",
									scriptName);

							System.out.println("Created successfully: " + input + " into " + text);
						} else {
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Failed", xpath, "", "Failed to update a resource ",
									scriptName);

						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to Update a resource \n Exception ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				case "verify_img_displayed":
					try {
						if (xpath != null) {
							// Wait until the image is visible
							WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
							WebElement imageElement = wait
									.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));

							boolean isDisplayed = imageElement.isDisplayed();

							if (isDisplayed) {
								report.addStep("step " + (count++) + " ==> Action: " + action + ", Text: " + text,
										"Passed", xpath, "", "Image is displayed", scriptName);
								System.out.println("✅ Image is displayed for: " + text);
							} else {
								report.addStep("step " + (count++) + " ==> Action: " + action + ", Text: " + text,
										"Failed", xpath, "", "Image is not displayed", scriptName);
								System.out.println("❌ Image is NOT displayed for: " + text);
							}
						} else {
							throw new IllegalArgumentException("XPath is null.");
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);

						report.addStep("step " + (count++) + " ==> Action: " + action + ", Text: " + text, "Failed",
								xpath, "", "Failed to verify image visibility\n Exception ==>> " + sw.toString(),
								scriptName);
						throw e;
					}
					break;

				case "get_text":
					System.out.println(xpath);
					try {
						try {
							String fetchedText = driver.findElement(By.xpath(xpath)).getText();
							map.put(input, fetchedText);
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "text fetched Successfully", scriptName);
						} catch (Exception e) {
							JavascriptExecutor js = (JavascriptExecutor) driver;
							js.executeScript("return arguments[0].textContent;", driver.findElement(By.xpath(xpath)));
							report.addStep("step " + (count++) + " ==> Action: " + action + ", Input: " + input
									+ ", Text:" + text, "Passed", xpath, "", "text fetched Successfully", scriptName);
						}
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						ee.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "", "Failed to fetched Element \n Exception ==>> " + sw.toString(),
								scriptName);
						throw ee;
					}
					System.out.println("fetched text: " + text);
					break;

				case "get_number_from_text":
					try {
						System.out.println(xpath);
						String fullText = driver.findElement(By.xpath(xpath)).getText();
						;

						variableName = input.split("#")[0];
						String prefix = input.split("#")[1];
						System.out.println("Full text from element: " + fullText);

						// Extract numbers only
						String numberOnly = fullText.replaceAll("[^0-9]", "");
						System.out.println("Extracted number: " + numberOnly);

						// Store in map if variableName is defined
						if (variableName != null && !variableName.isEmpty()) {
							map.put(variableName, prefix + numberOnly);
						}

						report.addStep("step " + (count++) + " ==> Action: " + action + ", Text: " + text, "Passed",
								xpath, "", "Extracted number: " + numberOnly, scriptName);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						report.addStep("step " + (count++) + " ==> Action: " + action + ", Text: " + text, "Failed",
								xpath, "", "Failed to extract number\nException ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				case "clear_and_enter_inrange":
					try {
						System.out.println(xpath);
						driver.findElement(By.xpath(xpath)).clear();

						Thread.sleep(300);

						int min = 4;
						int max = 13;
						int randomNumber = new Random().nextInt((max - min) + 1) + min;

						driver.findElement(By.xpath(xpath)).sendKeys(String.valueOf(randomNumber));
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Passed", xpath, "", "Entered the input successfully", scriptName);

						if (input != null) {
							variableName = input.trim();
							map.put(variableName, randomNumber);
						}

						System.out.println("Cleared and Entered quantity: " + randomNumber + " into " + text);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;
				// try {
				// if (input.startsWith("VAR")) {
				// variableName = input.split("#")[1].trim();
				// System.out.println(xpath);
				// driver.findElement(By.xpath(xpath)).clear();
				// Thread.sleep(300);
				// int min = 4;
				// int max = 13;
				//
				// Random random = new Random();
				// long randomNumber = random.nextInt((max - min) + 1) + min;
				// driver.findElement(By.xpath(xpath)).sendKeys(String.valueOf(randomNumber));
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text, "Passed" , xpath,"", "Enter the input Successfully",
				// scriptName);
				// map.put(variableName,randomNumber);
				//
				// //element.sendKeys(map.get(variableName).toString());
				// //report.addStep("step "+(count++)+" ==> Action: "+action+", Input:
				// "+input+", Text:"+text, "Passed" , xpath,"", "Cleared and Enter the inpur
				// Successfully", scriptName);
				//
				// System.out.println("Cleared and Entered text: " + input + " into " + text);
				// } else if (input != null) {
				// System.out.println(xpath);
				// driver.findElement(By.xpath(xpath)).clear();
				// Thread.sleep(300);
				// int min = 4;
				// int max = 13;
				//
				// Random random = new Random();
				// long randomNumber = random.nextInt((max - min) + 1) + min;
				// driver.findElement(By.xpath(xpath)).sendKeys(String.valueOf(randomNumber));
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text, "Passed" , xpath,"", "Enter the input Successfully",
				// scriptName);
				// map.put(variableName,randomNumber);
				// System.out.println("Cleared and Entered text: " + input + " into " + text);
				// }
				// }
				// catch(Exception e) {
				// StringWriter sw=new StringWriter();
				// PrintWriter pw=new PrintWriter(sw);
				// e.printStackTrace(pw);
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text, "Failed" , xpath,"", "Failed to clear and Enter the input \n
				// Exception ==>> "+sw.toString(), scriptName);
				// throw e;
				// }
				// break;

				case "enter_quantity":
					try {
						System.out.println(xpath);
						driver.findElement(By.xpath(xpath)).clear();

						Thread.sleep(300);

						int min = 4;
						int max = 13;
						int randomNumber = new Random().nextInt((max - min) + 1) + min;

						driver.findElement(By.xpath(xpath)).sendKeys(String.valueOf(randomNumber));
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Passed", xpath, "", "Entered the input successfully", scriptName);

						if (input != null) {
							variableName = input.trim();
							map.put(variableName, randomNumber);
						}

						System.out.println("Cleared and Entered quantity: " + randomNumber + " into " + text);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to clear and enter the input \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				// case "enter_total":
				// try {
				// // Fetch Quantity and price from the map
				// double quantity = Double.parseDouble(map.get("Quantity").toString());
				// double price = Double.parseDouble(map.get("price").toString());
				//
				// // Calculate base total and 20% tax
				// double baseTotal = quantity * price;
				// double tax = baseTotal * 0.20;
				// double total = baseTotal + tax;
				//
				// // Format total to 2 decimal places (if needed)
				// String totalString = String.format("%.2f", total);
				//
				// // Wait and enter total in the field
				// System.out.println(xpath);
				// driver.findElement(By.xpath(xpath)).clear();
				// Thread.sleep(300);
				// driver.findElement(By.xpath(xpath)).sendKeys(totalString);
				//
				// // Log and store in map if needed
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text,
				// "Passed", xpath, "", "Entered calculated total successfully", scriptName);
				// map.put("total", totalString); // Optional: store total in map
				//
				// System.out.println("Entered total: " + totalString + " into " + text);
				// } catch (Exception e) {
				// StringWriter sw = new StringWriter();
				// PrintWriter pw = new PrintWriter(sw);
				// e.printStackTrace(pw);
				// report.addStep("step "+(count++)+" ==> Action: "+action+", Input: "+input+",
				// Text:"+text,
				// "Failed", xpath, "", "Failed to enter calculated total \n Exception ==>> " +
				// sw.toString(), scriptName);
				// throw e;
				// }
				// break;

				case "enter_total":
					try {
						// Fetch Quantity and price from the map
						double quantity = Double.parseDouble(map.get("Quantity").toString());
						double price = Double.parseDouble(map.get("price").toString());

						// Calculate base total and 20% tax
						double baseTotal = quantity * price;
						double tax = baseTotal * 0.20;
						double total = baseTotal + tax;

						// Format total to 2 decimal places
						String totalString = String.format("%.2f", total);

						// Locate the input field freshly
						driver.findElement(By.xpath(xpath));

						// Wait for the field to be visible
						new WebDriverWait(driver, Duration.ofSeconds(10))
								.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(xpath))));

						// Clear and set the value using JavaScript
						JavascriptExecutor js = (JavascriptExecutor) driver;
						js.executeScript("arguments[0].value='';", driver.findElement(By.xpath(xpath)));
						Thread.sleep(300); // small pause to allow any DOM events to finish
						js.executeScript("arguments[0].value='" + totalString + "';",
								driver.findElement(By.xpath(xpath)));

						// Optional: Trigger blur/focusout to retain value
						driver.findElement(By.xpath(xpath)).sendKeys(Keys.TAB);

						// Log and store in map
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Passed", xpath, "", "Entered calculated total successfully", scriptName);
						map.put("total", totalString);

						System.out.println("Entered total: " + totalString + " into " + text);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						report.addStep(
								"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
								"Failed", xpath, "",
								"Failed to enter calculated total \n Exception ==>> " + sw.toString(), scriptName);
						throw e;
					}
					break;

				default:
					report.addStep(
							"step " + (count++) + " ==> Action: " + action + ", Input: " + input + ", Text:" + text,
							"Failed", xpath, "", "Unknown action: " + action, scriptName);
					System.out.println("Unknown action: " + action);

					System.exit(1);
				}

				action = "";
				text = "";
				input = "";

			} catch (Exception e) {
				System.out.println("Error executing action: " + action);
				e.printStackTrace();
				throw e;
			}

		}
	}

	public static void validateJson(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.readTree(json);
	}

	// public static void customWait(List<String> xpathStrategies) throws
	// InterruptedException {
	// int count = 0;
	// boolean flag=false;
	// while (count<waitTime) {
	// try {
	// for(String xpath:xpathStrategies) {
	// try {
	// driver.findElement(By.xpath(xpath));
	// flag = true;
	// break;
	// }catch (Exception e) {
	// flag=false;
	// }
	// }
	// if(flag) { break;}
	// else {throw new Exception("Retry");}
	// } catch (Exception e) {
	// Thread.sleep(1000);
	// count++;
	// }
	// }
	// }
	public static void customWait(List<String> xpathStrategies) throws InterruptedException {
		boolean isElementFound = false;

		for (int i = 0; i < waitTime; i++) {
			for (String xpath : xpathStrategies) {
				try {
					driver.findElement(By.xpath(xpath));
					isElementFound = true;
					break;
				} catch (Exception e) {
					continue;
				}
			}

			if (isElementFound) {
				break;
			}

			Thread.sleep(1000);
		}

		if (!isElementFound) {
			throw new RuntimeException("None of the provided XPaths matched any element within the wait time.");
		}
	}

	public static void main(String[] args) throws Exception {
		// HtmlReport report=new HtmlReport();
		// report.addStep("step 1","pass","element 1","variable 1", "Logs", "page 1");
		// report.generateReport("D://mohan.html","Mohan");
		ChromeOptions options = new ChromeOptions();
		options.setAcceptInsecureCerts(true);
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		// driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		driver.get("https://login-iagzme-dev6.fa.ocs.oraclecloud.com/");

		// Login to Application
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userid")));
		// driver.findElement(By.xpath("//span[text()='Save']")).click();
		driver.findElement(By.id("userid")).sendKeys("shreyas.k@fireflink.com");
		driver.findElement(By.id("password")).sendKeys("Oracle@1234");
		driver.findElement(By.id("btnActive")).click();
		// driver.findElement(By.xpath("//span[text()='Ok']")).click();
		// Navigate to Device Module
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Welcome, Shreyas Fireflink']")));
		// driver.findElement(By.xpath("//P[normalize-space(.)='System']//ancestor::span//mat-icon")).click();
		// driver.findElement(By.xpath("//P[normalize-space(.)='" + category +
		// "']")).click();
		// driver.findElement(By.xpath("//div[contains(@id,'superHeadAdd')]")).click();
		Thread.sleep(2000);
		// Click on search icon in Create Device Pop Up
		// wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
		// ---
		// "//*[contains(@id,'mat-expansion-panel-header')]/span/mat-panel-title[contains(text(),'Simulator')]")));
		// driver.findElement(By.xpath("//DIV[contains(@class,'titleContainer')]/DIV/DIV")).click();

		LinkedHashMap<String, Object> mp = new LinkedHashMap<String, Object>();
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("File Path", "C:\\Users\\User\\Documents\\ACOE\\ProgramEle\\Android\\PaymentsTest.txt");
		req.getAttributes().put("Map", mp);
		req.getAttributes().put("Script Name", "Procurement to pay flow");
		try {
			NlpResponseModel response = new jsonQueryExecutor().execute(req);
			System.out.println(response.getAttributes().get("Map"));
		} catch (NlpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class Test {
	static List<String[]> steps = new ArrayList<String[]>();

}

class HtmlReport {

	private List<String[]> steps = new ArrayList<String[]>();

	public void addStep(String step, String status, String elementsDetails, String variablesDetails, String logs,
			String pageName) {

		List<String[]> step21 = new ArrayList<String[]>();
		step21.add(new String[] { step, status, elementsDetails, variablesDetails, logs, pageName });
		Test.steps.addAll(step21);
	}

	public List<String[]> GetList() {
		return steps;
	}

	public void generateReport(String filePath, String scriptName) {
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html>\n");
		htmlContent.append("<head>\n");
		htmlContent.append("<title>Report for ").append(scriptName).append("</title>\n");

		// Enhanced CSS styling
		htmlContent.append("<style>\n")
				.append("body { font-family: Arial, sans-serif; margin: 0; background-color: white; color: #333; }\n")
				.append("h1 { color: #434db8; text-align: center; margin: 20px 0; }\n")
				.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n")
				.append("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }\n")
				.append("th { background-color: #434db8; color: white; }\n")
				.append("tr:nth-child(even) { background-color: #f9f9f9; }\n")
				.append("tr:hover { background-color: #f1f1f1; cursor: pointer; }\n")
				.append(".status-passed { color: green; font-weight: bold; }\n")
				.append(".status-failed { color: red; font-weight: bold; }\n")
				.append(".overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.4); display: none; z-index: 998; }\n")
				.append(".overlay.open { display: block; }\n")
				.append(".side-panel { position: fixed; right: -75%; top: 0; height: 100%; width: 75%; background: white; border-left: 2px solid #ddd; box-shadow: -2px 0 5px rgba(0,0,0,0.1); transition: right 0.3s ease; z-index: 999; }\n")
				.append(".side-panel.open { right: 0; }\n")
				.append(".panel-header { padding: 10px; background: #434db8; color: white; text-align: center; font-size: 18px; }\n")
				.append(".tab-buttons { display: flex; border-bottom: 1px solid #ddd; }\n")
				.append(".tab-button { flex: 1; text-align: center; padding: 10px; cursor: pointer; background: #f9f9f9; border-right: 1px solid #ddd; }\n")
				.append(".tab-button.active { background: #434db8; color: white; font-weight: bold; }\n")
				.append(".tab-content { display: none; padding: 20px; }\n")
				.append(".tab-content.active { display: block; }\n")
				.append(".close-panel { position: absolute; top: 10px; right: 10px; font-size: 20px; cursor: pointer; color: #aaa; }\n")
				.append(".collapsible {\r\n" + "  background-color: #434db8;\r\n" + "  color: white;\r\n"
						+ "  cursor: pointer;\r\n" + "  padding: 10px;\r\n" + "  width: 100%;\r\n"
						+ "  text-align: left;\r\n" + "  border: none;\r\n" + "  outline: none;\r\n"
						+ "  font-size: 15px;\r\n" + "}")
				.append("\r\n" + ".collapsible:after {\r\n" + "  content: '\\002B'; /* Plus sign */\r\n"
						+ "  color: white;\r\n" + "  font-weight: bold;\r\n" + "  float: right;\r\n" + "}")
				.append("\r\n" + ".collapsible.active:after {\r\n" + "  content: '\\2212'; /* Minus sign */\r\n"
						+ "}\r\n" + "\r\n" + ".content {\r\n" + "  padding: 0 18px;\r\n"
						// + " display: none;\r\n"
						+ "  overflow: hidden;\r\n" + "  background-color: #f1f1f1;\r\n" + "}")
				.append("\r\n.footer { bottom: 0; left: 0; width: 100%; color: white; text-align: center; padding: 5px 0; transition: background-color 0.3s ease, box-shadow 0.3s ease; }\r\n")

				.append("</style>\n");

		// JavaScript for interactivity
		htmlContent.append("<script>\n").append("function openPanel(step, elements, variables, logs, status) {\n")
				.append("  document.getElementById('panel-header').innerText = step.innerText;\n")
				.append("  document.getElementById('elements-content').innerHTML = elements.innerHTML;\n")
				.append("  document.getElementById('variables-content').innerHTML = variables.innerHTML;\n")
				.append("  document.getElementById('logs-content').innerHTML = logs.innerHTML;\n")
				.append("  document.querySelector('.side-panel').classList.add('open');\n")
				.append("  document.querySelector('.overlay').classList.add('open');\n")
				.append("  let tabButtons = document.querySelectorAll('.tab-button');\n")
				.append(" tabButtons.forEach(button => button.classList.remove('active'));\n")
				.append("  switchTab('elements-tab');\n") // Default to Elements tab
				.append("}\n").append("function closePanel() {\n")
				.append("  document.querySelector('.side-panel').classList.remove('open');\n")
				.append("  document.querySelector('.overlay').classList.remove('open');\n").append("}\n")
				.append("function switchTab(tabId) {\n")
				.append("  document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));\n")
				.append("  document.querySelectorAll('.tab-button').forEach(button => button.classList.remove('active'));\n")
				.append("  document.getElementById(tabId).classList.add('active');\n")
				.append("  document.querySelector(`[data-tab='${tabId}']`).classList.add('active');\n").append("}\n")
				.append("function toggleCollapsible() {\r\n" + "  var content = this.nextElementSibling;\r\n"
						+ "  this.classList.toggle('active');\r\n" + "  if (content.style.display === 'block') {\r\n"
						+ "    content.style.display = 'none';\r\n" + "  } else {\r\n"
						+ "    content.style.display = 'block';\r\n" + "  }\r\n" + "}")
				.append("document.addEventListener('DOMContentLoaded', function() {\r\n"
						+ "  var collapsibles = document.querySelectorAll('.collapsible');\r\n"
						+ "  collapsibles.forEach(function(collapsible) {\r\n"
						+ "    collapsible.addEventListener('click', toggleCollapsible);\r\n" + "  });\r\n" + "});")

				.append("</script>\n");
		htmlContent.append("</head>\n");
		htmlContent.append("<body>\n");
		htmlContent.append("<h1>Test Report for ").append(scriptName).append("</h1>\n");

		// Table with steps

		Map<String, StringBuilder> data = new LinkedHashMap<String, StringBuilder>();

		for (String[] step : Test.steps) {

			StringBuilder s = new StringBuilder();

			if (data.get(step[5]) != null) {
				s.append(data.get(step[5]));
			}

			String header = "headerAcoe" + new Random().nextDouble();
			String value = "acoe" + new Random().nextDouble();
			String variables = "variablesAcoe" + new Random().nextDouble();
			String logs = "logsAcoe" + new Random().nextDouble();
			header = header.replace(".", "");
			value = value.replace(".", "");
			variables = variables.replace(".", "");
			logs = logs.replace(".", "");
			s.append("<div id=" + header + " hidden>" + step[0] + "</div>");
			s.append("<div id=" + value + " hidden>" + step[2] + "</div>");
			s.append("<div id=" + variables + " hidden>" + step[3] + "</div>");
			s.append("<div id=" + logs + " hidden>" + step[4] + "</div>");

			String statusClass = step[1].equalsIgnoreCase("Passed") ? "status-passed" : "status-failed";
			s.append("<tr onclick=\"openPanel(").append("document.getElementById('" + header + "')").append(", ")
					.append("document.getElementById('" + value + "')").append(", ")
					.append("document.getElementById('" + variables + "')").append(", ")
					.append("document.getElementById('" + logs + "')").append(", '").append(step[1]).append("')\">")
					.append("<td>").append(step[0]).append("</td>").append("<td class='").append(statusClass)
					.append("'>").append(step[1]).append("</td>").append("</tr>\n");

			data.put(step[5], s);

		}

		for (Map.Entry<String, StringBuilder> entry : data.entrySet()) {

			StringBuilder val = entry.getValue();
			htmlContent.append("<button class=\"collapsible\">" + entry.getKey() + "</button>\n");
			htmlContent.append("<div class=\"content\">\n");
			htmlContent.append("<table>\n");
			htmlContent.append("<tr><th>Step</th><th>Status</th></tr>\n");
			htmlContent.append(val);

			htmlContent.append("</table>\n");
			htmlContent.append("</div>\n");

		}
		htmlContent.append("<div class='overlay' onclick='closePanel()'></div>\n").append("<div class='side-panel'>\n")
				.append("<div class='panel-header' id='panel-header'></div>\n")
				.append("<span class='close-panel' onclick='closePanel()'>&times;</span>\n")
				.append("<div class='tab-buttons'>\n")
				.append("<div class='tab-button' data-tab='elements-tab' onclick=\"switchTab('elements-tab')\">Elements</div>\n")
				.append("<div class='tab-button' data-tab='variables-tab' onclick=\"switchTab('variables-tab')\">Variables</div>\n")
				.append("<div class='tab-button' data-tab='logs-tab' onclick=\"switchTab('logs-tab')\">Logs</div>\n")
				.append("</div>\n").append("<div id='elements-tab' class='tab-content'>\n")
				.append("<p id='elements-content'></p>\n").append("</div>\n")
				.append("<div id='variables-tab' class='tab-content'>\n").append("<p id='variables-content'></p>\n")
				.append("</div>\n").append("<div id='logs-tab' class='tab-content'>\n")
				.append("<p id='logs-content'></p>\n").append("</div>\n").append("</div>\n");
		htmlContent.append(
				"<div class=\"footer\"><img src=\"https://res.cloudinary.com/dposietlk/image/upload/v1722593660/Acoe_jlouhm.png\" alt=\"Designed and Developed by ACOE Team\" height=\"40px\" width=\"360px\" /><!-- This Webpage Designed by MohanKumar - ACOE Team --></div>\r\n");
		htmlContent.append("</body>\n");
		htmlContent.append("</html>\n");

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(htmlContent.toString());
			writer.flush();
			System.out.println("HTML Report has been Successfully Generated in the Path \"" + filePath + "\"");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to Generate the HTML Report");
		}

		Test.steps.clear();
	}
}