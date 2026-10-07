
package custom_logic;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class CheckWebAccessibility_GetReport2 implements Nlp {	
	public static void main(String[] args) throws MalformedURLException {

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName("chrome");
		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities);
		driver.manage().window().maximize();
		driver.get("http://106.51.90.215:8084");
		
		NlpRequestModel rq = new NlpRequestModel();
		rq.getAttributes().put("Standard (A,AA,AAA,Sec508)", "AA");
		rq.getAttributes().put("Output Report Directory", "C:\\reports");
		rq.setWebDriver(driver);
		try {
			NlpResponseModel res = new CheckWebAccessibility_GetReport2().execute(rq);
			System.out.println(res.getAttributes().get("Output"));
		} catch (NlpException e) {
			e.printStackTrace();
		}

	}

	private static List<Issue3> processedIssues;
	@InputParams({
		@InputParam(name = "Standard (A,AA,AAA,Sec508)", type = "String"),
		@InputParam(name = "Output Report Directory", type = "String")
	})
	@ReturnType(name = "Output", type = "String")
	
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String standard = (String) programElementsInput.get("Standard (A,AA,AAA,Sec508)");
		String outputDirectory = (String) programElementsInput.get("Output Report Directory");
		WebDriver driver= nlpRequestModel.getWebDriver();

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			processedIssues=null;
			outputDirectory=outputDirectory.replace("\\", "/");
			if(StringUtils.equalsAnyIgnoreCase(standard.trim(), "A","AA","AAA")) {
				standard="WCAG2"+standard.trim().toUpperCase();
			}
			else if(StringUtils.equalsIgnoreCase(standard.trim(), "Sec508")) {
				standard="Section508";
			}else {
				throw new RuntimeException("Please Specify Proper Standard");
			}
			String result = checkWebAccessibility_GetReport(driver,standard,outputDirectory);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Generated Web Accessiblity Report");
			nlpResponseModel.getAttributes().put("Output", result);
		}
		catch(Exception e) {
			e.printStackTrace();
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Generate Web Accessiblity Report, Reason for Failure ==> \n"+sw.toString());
		}
		return nlpResponseModel;
	}

	@SuppressWarnings("unchecked")
	public static String checkWebAccessibility_GetReport(WebDriver driver, String standard, String outputDirectory) throws Exception {
		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
		waitForLoad(driver);
		javascriptExecutor.executeScript(getData("https://res.cloudinary.com/dposietlk/raw/upload/v1729170504/Accessiblity_x6jjwl.js"));			
		waitForLoad(driver);
		for (int i = 0; i < 10; i++) {
			try {
				javascriptExecutor.executeScript(String.format(getData("https://res.cloudinary.com/dposietlk/raw/upload/v1729167940/Runner_ji9d54.js"),standard));
				break;
			} catch (JavascriptException exe) {
				waitForLoad(driver);
			}
		}
		processedIssues = issueList((ArrayList<Map<String, String>>) javascriptExecutor.executeScript(getData("https://res.cloudinary.com/dposietlk/raw/upload/v1729170533/GetData_bjmqx1.js")));
		Issues3 issues = getIssues("FF(ACOE)", driver,javascriptExecutor, standard);
		String reportPath = save(driver, issues, "FF(ACOE)", standard, outputDirectory);
		return reportPath;
	}
	
	static String getData(String url) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        String fileContents = ""; 

        try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                byteArrayOutputStream.write(dataBuffer, 0, bytesRead);
            }

            fileContents = byteArrayOutputStream.toString("UTF-8");  
        }

        conn.disconnect();	
        return fileContents;
	}

	static String save(WebDriver driver, Issues3 issues, String reportName, String standard, String outputDirectory) throws Exception {
		
		ObjectMapper mapper1 = new ObjectMapper();
		String outputJson="";
		try {
			String jsonInput = mapper1.writerWithDefaultPrettyPrinter().writeValueAsString(issues);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonInput);
			JsonNode issuesNode = rootNode.get("issues");
			ObjectNode outputNode = mapper.createObjectNode();
			int totalElementCount=driver.findElements(By.xpath("//*")).size();

			outputNode.put("url", driver.getCurrentUrl());
			outputNode.put("standard", standard);

			Map<Integer, ObjectNode> typeMap = new HashMap<>();
			for (JsonNode issueNode : issuesNode) {
				processIssue(driver, mapper, issueNode, typeMap);
			}
			for (Map.Entry<Integer, ObjectNode> entry : typeMap.entrySet()) {
				ObjectNode typeNode = entry.getValue();
				typeNode.put("issueType", getIssueTypeName(entry.getKey()));
				outputNode.set(typeNode.get("issueType").asText(), typeNode.get("issueTags"));
			}
			outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputNode);
			List<String> errorLocationList_mainLevel = getJsonListValue(outputJson, "$.Errors[*].issueCodes[*].issueDetails[*].issueElements[*].location");
			List<String> warnLocationList_mainLevel = getJsonListValue(outputJson, "$.Warnings[*].issueCodes[*].issueDetails[*].issueElements[*].location");
			List<String> noticeLocationList_mainLevel = getJsonListValue(outputJson, "$.Notices[*].issueCodes[*].issueDetails[*].issueElements[*].location");
			totalElementCount=totalElementCount-((errorCount()-errorLocationList_mainLevel.size())+(warningCount()-warnLocationList_mainLevel.size())+(noticeCount()-noticeLocationList_mainLevel.size()));
			outputNode.put("elementsCount", totalElementCount);
			long accessibiltyScore=Math.round(100.0-((((errorLocationList_mainLevel.size()*5.0)+(warnLocationList_mainLevel.size()*3.0)+(noticeLocationList_mainLevel.size()*1.0))/(totalElementCount*5.0))*100.0));
			outputNode.put("score", accessibiltyScore);
			outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			System.err.println("Failed to convert Object to Json " + e.getMessage());
		}
		return createHtmlReport(outputJson, outputDirectory);
	}


	private static String createHtmlReport(String outputJson, String outputDirectory) throws Exception {
		StringBuilder reportBuilder=new StringBuilder();
		reportBuilder.append("<!doctype html>\n");
		reportBuilder.append("<html lang='en'>\n");
		reportBuilder.append("\n");
		reportBuilder.append("<head>\n");
		reportBuilder.append("    <!-- Required meta tags -->\n");
		reportBuilder.append("    <meta charset='utf-8'>\n");
		reportBuilder.append("    <meta name='viewport' content='width=device-width, initial-scale=1, shrink-to-fit=no'>\n");
		reportBuilder.append("\n");
		reportBuilder.append("    <!-- <link rel='stylesheet' href='./asset/css/style.css'>\n");
		reportBuilder.append("    <link rel='stylesheet' href='./asset/css/bootstrap.css'> -->\n");
		reportBuilder.append("    <link rel='icon' href='https://res.cloudinary.com/dposietlk/image/upload/v1722593659/favicon_itzjrc.png'\n");
		reportBuilder.append("        type='image/png'>\n");
		reportBuilder.append("\n");
		reportBuilder.append("    <title>Fireflink Accessibility Report</title>\n");
		reportBuilder.append("    <style>\n");
		reportBuilder.append(""+getData("https://res.cloudinary.com/dposietlk/raw/upload/v1730700178/style_cjbv9k.css")+"\n");
		reportBuilder.append(".four .slice-colors .st.slice-item:nth-child(2) {\n");
		reportBuilder.append("  border-top: 50px #e67e22 solid;\n");
		reportBuilder.append("  border-right: 50px #e67e22 solid;\n");
		reportBuilder.append("  background-color: #E84C3D;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append(".four .slice-colors .st.slice-item:nth-child(4) {\n");
		reportBuilder.append("  left: 50%;\n");
		reportBuilder.append("  border-bottom: 50px #1eaa59 solid;\n");
		reportBuilder.append("  border-right: 50px #1eaa59 solid;\n");
		reportBuilder.append("  background-color: #f1c40f;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("    </style>\n");
		reportBuilder.append("<style>\n");
		reportBuilder.append("    #popupMessage {\n");
		reportBuilder.append("        visibility: hidden;  \n");
		reportBuilder.append("        background-color: #4CAF50;\n");
		reportBuilder.append("        color: white;\n");
		reportBuilder.append("        text-align: center;\n");
		reportBuilder.append("        border-radius: 5px;\n");
		reportBuilder.append("        padding: 10px;\n");
		reportBuilder.append("        position: fixed;\n");
		reportBuilder.append("        z-index: 1;\n");
		reportBuilder.append("        left: 50%;\n");
		reportBuilder.append("        top: 10px;\n");
		reportBuilder.append("        transform: translateX(-50%);\n");
		reportBuilder.append("        font-size: 17px;\n");
		reportBuilder.append("    }    \n");
		reportBuilder.append("</style>\n");
		reportBuilder.append("</head>\n");
		reportBuilder.append("\n");
		reportBuilder.append("<body>\n");
		reportBuilder.append("    <div id='navbar'>\n");
		reportBuilder.append("        <nav class='navbar navbar-expand-lg navbar-light' style='background: #f0f3f9 0 0 no-repeat padding-box;'>\n");
		reportBuilder.append("            <div class='container d-flex justify-content-between'>\n");
		reportBuilder.append("                <div class='p-3'> \n");
		reportBuilder.append("                    <img src='https://www.testyantraglobal.com/images/logo.svg' alt='Test Yantra Global Logo'\n");
		reportBuilder.append("                        height='40px'>\n");
		reportBuilder.append("                </div>\n");
		reportBuilder.append("                <div class='form-inline my-2 my-lg-0 text-ff-orange text-left'>\n");
		reportBuilder.append("                    <img src='https://www.fireflink.com/static/media/fireflink.0d427889.svg' alt='Fire Flink Logo'\n");
		reportBuilder.append("                        height='40px'>\n");
		reportBuilder.append("                    <h6 class='pt-2'> Fireflink Accessibility Report</h6>\n");
		reportBuilder.append("                </div>\n");
		reportBuilder.append("            </div>\n");
		reportBuilder.append("        </nav>\n");
		reportBuilder.append("\n");
		reportBuilder.append("    </div>\n");
		reportBuilder.append("\n");
		reportBuilder.append("    <div class='container mt-4'>\n");
		reportBuilder.append("\n");
		reportBuilder.append("        <div class='mt-3 mb-3'>\n");

		String url=getJsonStringValue(outputJson, "$.url");

		reportBuilder.append("            <h6>Audit results for: <span id='testing-site-url' class='text-ff-blue'>"+url+"</span></h6>\n");
		reportBuilder.append("        </div>\n");
		reportBuilder.append("\n");
		reportBuilder.append("        <div class='row mt-3'>\n");
		reportBuilder.append("            <div class='col-lg-4'>\n");
		reportBuilder.append("                <div class='card'>\n");
		reportBuilder.append("                    <div class='card-body'>\n");
		reportBuilder.append("                        <h5 class='card-title mb-5 fw-bold'>Audit Score:</h5>\n");
		reportBuilder.append("                        <div id='guage'>\n");
		reportBuilder.append("                            <div id='guage-needle' class='gauge four mx-auto'>\n");
		reportBuilder.append("                                <div class='slice-colors'>\n");
		reportBuilder.append("                                    <div class='st slice-item'></div>\n");
		reportBuilder.append("                                    <div class='st slice-item'></div>\n");
		reportBuilder.append("                                    <div class='st slice-item'></div>\n");
		reportBuilder.append("                                    <div class='st slice-item'></div>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div class='needle'></div>\n");
		reportBuilder.append("                                <div class='gauge-center'>\n");

		String score=getJsonStringValue(outputJson, "$.score");
		reportBuilder.append("                                    <div class='label'>Score</div>\n");
		reportBuilder.append("                                    <div class='number'>"+score+"</div>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                            </div>\n");
		reportBuilder.append("                        </div>\n");
		reportBuilder.append("                        <div class='text-center pt-3'>\n");
		reportBuilder.append("                            <small class='text-xs'>Accessibility audit score of 90 and higher greatly reduces the risk of WCAG non-compliance</small>\n");
		reportBuilder.append("                        </div>\n");
		reportBuilder.append("                    </div>\n");
		reportBuilder.append("                </div>\n");
		reportBuilder.append("            </div>\n");
		reportBuilder.append("            <div class='col-lg-8'>\n");
		reportBuilder.append("                <div class='card'>\n");
		reportBuilder.append("                    <div class='card-body'>\n");
		reportBuilder.append("                        <div class='d-flex justify-content-between'>\n");

		String standard=getJsonStringValue(outputJson, "$.standard");
		List<String> errorList = getJsonListValue(outputJson, "$.Errors[*].issueTag");
		List<String> warningList = getJsonListValue(outputJson, "$.Warnings[*].issueTag");
		List<String> noticeList = getJsonListValue(outputJson, "$.Notices[*].issueTag");

		List<String> errorLocationList_mainLevel = getJsonListValue(outputJson, "$.Errors[*].issueCodes[*].issueDetails[*].issueElements[*].location");
		List<String> warnLocationList_mainLevel = getJsonListValue(outputJson, "$.Warnings[*].issueCodes[*].issueDetails[*].issueElements[*].location");
		List<String> noticeLocationList_mainLevel = getJsonListValue(outputJson, "$.Notices[*].issueCodes[*].issueDetails[*].issueElements[*].location");

		reportBuilder.append("                            <p class='card-title fw-bold mb-4'>Standard: <span id='testing-standard'>"+standard+"</span></p>\n");
		reportBuilder.append("                        </div>\n");
		reportBuilder.append("                        <div class='card-body bg-light mb-2'>\n");
		reportBuilder.append("                            <div class='d-flex justify-content-between'>\n");
		reportBuilder.append("                                <div class='fw-bold' style='width:80px'>\n");
		reportBuilder.append("                                    <strong>Errors</strong>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div class='progress flex-fill flex-grow-1 ms-3' style='height: 25px;'>\n");
		reportBuilder.append("                                    <div id='error-bar' class='progress-bar bg-danger' role='progressbar'\n");
		reportBuilder.append("                                        aria-valuenow='25' aria-valuemin='0' aria-valuemax='100'>0%</div>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div id='progressbar-error-count'\n");
		reportBuilder.append("                                    class='bg-dark text-white ps-2 pe-2 rounded-end text-center' style='width: 50px;'>\n");
		reportBuilder.append("                                    "+errorLocationList_mainLevel.size()+"</div>\n");
		reportBuilder.append("                            </div>\n");
		reportBuilder.append("                        </div>\n");
		reportBuilder.append("                        <div class='card-body bg-light mb-2'>\n");
		reportBuilder.append("                            <div class='d-flex justify-content-between'>\n");
		reportBuilder.append("                                <div class='fw-bold' style='width:80px'>\n");
		reportBuilder.append("                                    <strong>Warnings</strong>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div class='progress flex-fill flex-grow-1 ms-3' style='height: 25px;'>\n");
		reportBuilder.append("                                    <div id='warning-bar' class='progress-bar bg-warning' role='progressbar'\n");
		reportBuilder.append("                                        aria-valuenow='50' aria-valuemin='0' aria-valuemax='100'>50%</div>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div id='progressbar-warning-count'\n");
		reportBuilder.append("                                    class='bg-dark text-white ps-2 pe-2 rounded-end text-center' style='width: 50px;'>\n");
		reportBuilder.append("                                "+warnLocationList_mainLevel.size()+"</div>\n");
		reportBuilder.append("                            </div>\n");
		reportBuilder.append("                        </div>\n");
		reportBuilder.append("                        <div class='card-body bg-light'>\n");
		reportBuilder.append("                            <div class='d-flex justify-content-between'>\n");
		reportBuilder.append("                                <div class='fw-bold' style='width:80px'>\n");
		reportBuilder.append("                                    <strong>Notices</strong>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div class='progress flex-fill flex-grow-1 ms-3' style='height: 25px;'>\n");
		reportBuilder.append("                                    <div id='notices-bar' class='progress-bar bg-info' role='progressbar'\n");
		reportBuilder.append("                                        aria-valuenow='80' aria-valuemin='0' aria-valuemax='100'>80%</div>\n");
		reportBuilder.append("                                </div>\n");
		reportBuilder.append("                                <div id='progressbar-notices-count'\n");
		reportBuilder.append("                                    class='bg-dark text-white ps-2 pe-2 rounded-end text-center' style='width: 50px;'>\n");
		reportBuilder.append("                                    "+noticeLocationList_mainLevel.size()+"</div>\n");
		reportBuilder.append("                            </div>\n");
		reportBuilder.append("                        </div>\n");
		reportBuilder.append("                    </div>\n");
		reportBuilder.append("                </div>\n");
		reportBuilder.append("            </div>\n");
		reportBuilder.append("        </div>\n");
		reportBuilder.append("\n");
		reportBuilder.append("\n");
		reportBuilder.append("        <div class='mt-5'>\n");
		reportBuilder.append("            <h4 class='leading-3 font-weight-bold my-3 text-ff-blue'>Testing Standards</h4>\n");
		reportBuilder.append("            <p>This report is complaint with <strong>WCAG 2.1</strong>, under the selected subsection (WCAG: A,AA,AAA, and Section 508).</p>\n");
		reportBuilder.append("        </div>\n");
		reportBuilder.append("\n");
		reportBuilder.append("        <div class='mt-5'>\n");
		reportBuilder.append("            <ul class='nav nav-tabs' id='myTab' role='tablist'>\n");

		if (errorList.size()>=1) {
			reportBuilder.append("                <li class='nav-item' role='presentation'>\n");
			reportBuilder.append("                    <button class='nav-link active text-danger' id='home-tab' data-bs-toggle='tab'\n");
			reportBuilder.append("                        data-bs-target='#home' type='button' role='tab' aria-controls='home'\n");
			reportBuilder.append("                        aria-selected='true'><strong>Errors\n");
			reportBuilder.append("                            <span id='error-tab-count'></span></strong></button>\n");
			reportBuilder.append("                </li>\n");
		}
		if (warningList.size()>=1) {
			reportBuilder.append("                <li class='nav-item' role='presentation'>\n");
			reportBuilder.append("                    <button class='nav-link text-warning' id='profile-tab' data-bs-toggle='tab'\n");
			reportBuilder.append("                        data-bs-target='#profile' type='button' role='tab' aria-controls='profile'\n");
			reportBuilder.append("                        aria-selected='false'><strong>Warnings <span id='warning-tab-count'></span></strong></button>\n");
			reportBuilder.append("                </li>\n");
		}
		if (noticeList.size()>=1) {
			reportBuilder.append("                <li class='nav-item' role='presentation'>\n");
			reportBuilder.append("                    <button class='nav-link text-info' id='contact-tab' data-bs-toggle='tab' data-bs-target='#contact'\n");
			reportBuilder.append("                        type='button' role='tab' aria-controls='contact' aria-selected='false'><strong>Notices\n");
			reportBuilder.append("                            <span id='notices-tab-count'></span></strong></button>\n");
			reportBuilder.append("                </li>\n");
		}
		reportBuilder.append("            </ul>\n");

		reportBuilder.append("            <div class='tab-content mt-3' id='myTabContent'>\n");

		if (errorList.size()>=1) {
			reportBuilder.append("                <!-- Errors Items Body-->\n");
			reportBuilder.append("                <div class='tab-pane fade show active' id='home' role='tabpanel' aria-labelledby='home-tab'>\n");
			reportBuilder.append("                    <!-- Errors Items Accordino -->\n");
			reportBuilder.append("                    <div class='accordion' id='errorTagAccordion'>\n");
			reportBuilder.append("                        <!-- Errors Tag Item -->\n");

			for (int i = 0; i < errorList.size(); i++) {
				String issueTag=errorList.get(i);
				List<String> errorIssueCodeList = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes[*].issueCode");
				List<String> errorLocationList_issueCodeLevel = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes[*].issueDetails[*].issueElements[*].location");
				reportBuilder.append("                        <div class='accordion-item'>\n");	
				reportBuilder.append("                            <h2 class='accordion-header' id='heading"+i+"'>\n");
				reportBuilder.append("                                <button class='accordion-button collapsed' type='button' data-bs-toggle='collapse'\n");
				reportBuilder.append("                                    data-bs-target='#errorTag-Collapse"+i+"' aria-expanded='true'\n");
				reportBuilder.append("                                    aria-controls='errorTag-Collapse"+i+"'>\n");
				reportBuilder.append("            						  Tag "+(i+1)+": "+issueTag+" (Elements: "+errorLocationList_issueCodeLevel.size()+", Guidelines: "+errorIssueCodeList.size()+")\n");
				reportBuilder.append("                                </button>\n");
				reportBuilder.append("                            </h2>\n");

				reportBuilder.append("                            <!-- Errors Tag Item Collaps Body -->\n");
				reportBuilder.append("                            <div id='errorTag-Collapse"+i+"' class='accordion-collapse collapse'\n");
				reportBuilder.append("                                aria-labelledby='heading"+i+"' data-bs-parent='#errorTagAccordion'>\n");
				reportBuilder.append("                                <div class='accordion-body'>\n");
				reportBuilder.append("                                    <!-- Error Tag Guidline Item -->\n");
				reportBuilder.append("                                    <div class='accordion' id='errorGuidlineAccordion'>\n");


				for (int j = 0; j < errorIssueCodeList.size(); j++) {
					String issueCode= errorIssueCodeList.get(j);
					List<String> errorIssueMsgList = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails[*].issueMsg");
					List<String> errorLocationList_msgLevel = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails[*].issueElements[*].location");
					reportBuilder.append("                                        <div class='accordion-item'>\n");
					reportBuilder.append("                                            <h2 class='accordion-header' id='heading"+i+""+j+"'>\n");
					reportBuilder.append("                                                <button class='accordion-button collapsed' type='button'\n");
					reportBuilder.append("                                                    data-bs-toggle='collapse'\n");
					reportBuilder.append("                                                    data-bs-target='#errorGuidline-Collapse"+i+""+j+"' aria-expanded='true'\n");
					reportBuilder.append("                                                    aria-controls='errorGuidline-Collapse"+i+""+j+"'>\n");
					reportBuilder.append("                Guidelines "+(j+1)+": "+issueCode+" (Elements: "+errorLocationList_msgLevel.size()+", Guideline-Message: "+errorIssueMsgList.size()+")\n");
					reportBuilder.append("                                                </button>\n");
					reportBuilder.append("                                            </h2>\n");

					reportBuilder.append("                                            <!-- Error Tag Guidline Item Collapse Body -->\n");
					reportBuilder.append("                                            <div id='errorGuidline-Collapse"+i+""+j+"' class='accordion-collapse collapse'\n");
					reportBuilder.append("                                                aria-labelledby='heading"+i+""+j+"' data-bs-parent='#errorGuidlineAccordion'>\n");
					reportBuilder.append("                                                <div class='accordion-body'>\n");
					reportBuilder.append("                                                    <!-- Error Tag Guidline Message Item -->\n");
					reportBuilder.append("                                                    <div class='accordion' id='errorGuidlineMessage-Collapse'>\n");

					for (int k = 0; k < errorIssueMsgList.size(); k++) {
						String issueMsg= errorIssueMsgList.get(k);
						List<String> errorLocationList = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements[*].location");
						reportBuilder.append("                                                        <div class='accordion-item'>\n");	
						reportBuilder.append("                                                            <h2 class='accordion-header' id='heading"+i+""+j+""+k+"'>\n");
						reportBuilder.append("                                                                <button class='accordion-button collapsed' type='button'\n");
						reportBuilder.append("                                                                    data-bs-toggle='collapse'\n");
						reportBuilder.append("                                                                    data-bs-target='#errorGuidlineMessage-Collapse"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                    aria-expanded='true'\n");
						reportBuilder.append("                                                                    aria-controls='errorGuidlineMessage-Collapse"+i+""+j+""+k+"'>\n");
						reportBuilder.append("                   Guideline Message "+(k+1)+": "+issueMsg+" (Elements: "+errorLocationList.size()+")\n");
						reportBuilder.append("                                                                </button>\n");
						reportBuilder.append("                                                            </h2>\n");

						reportBuilder.append("                                                            <!-- Error Tag Guidline Message Collapse Body -->\n");
						reportBuilder.append("                                                            <div id='errorGuidlineMessage-Collapse"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                class='accordion-collapse collapse'\n");
						reportBuilder.append("                                                                aria-labelledby='heading"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                data-bs-parent='#errorGuidlineMessage-Collapse'>\n");
						reportBuilder.append("                                                                <div class='accordion-body'>\n");
						reportBuilder.append("                                                                    <div class='list-group list-group-numbered'>\n");

						for (int l = 0; l < errorLocationList.size(); l++) {
							String location= errorLocationList.get(l);
							String screenshot=getJsonStringValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements["+l+"].screenShot");
							reportBuilder.append("                                                                        <div\n");
							reportBuilder.append("                                                                            class='list-group-item d-flex justify-content-between align-items-start'>\n");
							reportBuilder.append("                                                                            <div class='ms-2 me-auto'>\n");
							reportBuilder.append("                        <div id=\"ERROR"+i+""+j+""+k+""+l+"\" hidden>"+location+"</div>\n");
							reportBuilder.append("                        <div class=\"copyPath fw-bold\">Element "+(l+1)+": \n<img src=\"data:image/png;base64,"+screenshot+"\" alt=\"NON-UI Element\" class=\"hover-image\" onclick=\"copyXPath('ERROR"+i+""+j+""+k+""+l+"')\">\n</div>\n");
							reportBuilder.append("                                                                            </div>\n");
							reportBuilder.append("                                                                        </div>\n");
						}		

						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
					}
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
				}
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
			}
			reportBuilder.append("                    </div>\n");
			reportBuilder.append("                </div>\n");
			reportBuilder.append("\n");
		}

		if (warningList.size()>=1) {
			reportBuilder.append("                <!-- Warning Items Body-->\n");
			reportBuilder.append("                <div class='tab-pane fade' id='profile' role='tabpanel' aria-labelledby='profile-tab'>\n");
			reportBuilder.append("                    <!-- Warning Items Accordino -->\n");
			reportBuilder.append("                    <div class='accordion' id='warningTagAccordion'>\n");
			reportBuilder.append("                        <!-- Warning Tag Item -->\n");

			for (int i = 0; i < warningList.size(); i++) {
				String issueTag=warningList.get(i);
				List<String> warningIssueCodeList = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes[*].issueCode");
				List<String> warningLocationList_issueCodeLevel = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes[*].issueDetails[*].issueElements[*].location");
				reportBuilder.append("                        <div class='accordion-item'>\n");	
				reportBuilder.append("                            <h2 class='accordion-header' id='heading"+i+"'>\n");
				reportBuilder.append("                                <button class='accordion-button collapsed' type='button' data-bs-toggle='collapse'\n");
				reportBuilder.append("                                    data-bs-target='#warningTag-Collapse"+i+"' aria-expanded='true'\n");
				reportBuilder.append("                                    aria-controls='warningTag-Collapse"+i+"'>\n");
				reportBuilder.append("            						  Tag "+(i+1)+": "+issueTag+" (Elements: "+warningLocationList_issueCodeLevel.size()+", Guidelines: "+warningIssueCodeList.size()+")\n");
				reportBuilder.append("                                </button>\n");
				reportBuilder.append("                            </h2>\n");

				reportBuilder.append("                            <!-- Warning Tag Item Collaps Body -->\n");
				reportBuilder.append("                            <div id='warningTag-Collapse"+i+"' class='accordion-collapse collapse'\n");
				reportBuilder.append("                                aria-labelledby='heading"+i+"' data-bs-parent='#warningTagAccordion'>\n");
				reportBuilder.append("                                <div class='accordion-body'>\n");
				reportBuilder.append("                                    <!-- Warning Tag Guidline Item -->\n");
				reportBuilder.append("                                    <div class='accordion' id='warningGuidlineAccordion'>\n");


				for (int j = 0; j < warningIssueCodeList.size(); j++) {
					String issueCode= warningIssueCodeList.get(j);
					List<String> warningIssueMsgList = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails[*].issueMsg");
					List<String> warningLocationList_msgLevel = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails[*].issueElements[*].location");
					reportBuilder.append("                                        <div class='accordion-item'>\n");
					reportBuilder.append("                                            <h2 class='accordion-header' id='heading"+i+""+j+"'>\n");
					reportBuilder.append("                                                <button class='accordion-button collapsed' type='button'\n");
					reportBuilder.append("                                                    data-bs-toggle='collapse'\n");
					reportBuilder.append("                                                    data-bs-target='#warningGuidline-Collapse"+i+""+j+"' aria-expanded='true'\n");
					reportBuilder.append("                                                    aria-controls='warningGuidline-Collapse"+i+""+j+"'>\n");
					reportBuilder.append("                Guidelines "+(j+1)+": "+issueCode+" (Elements: "+warningLocationList_msgLevel.size()+", Guideline-Message: "+warningIssueMsgList.size()+")\n");
					reportBuilder.append("                                                </button>\n");
					reportBuilder.append("                                            </h2>\n");

					reportBuilder.append("                                            <!-- Warning Tag Guidline Item Collapse Body -->\n");
					reportBuilder.append("                                            <div id='warningGuidline-Collapse"+i+""+j+"' class='accordion-collapse collapse'\n");
					reportBuilder.append("                                                aria-labelledby='heading"+i+""+j+"' data-bs-parent='#warningGuidlineAccordion'>\n");
					reportBuilder.append("                                                <div class='accordion-body'>\n");
					reportBuilder.append("                                                    <!-- Warning Tag Guidline Message Item -->\n");
					reportBuilder.append("                                                    <div class='accordion' id='warningGuidlineMessage-Collapse'>\n");

					for (int k = 0; k < warningIssueMsgList.size(); k++) {
						String issueMsg= warningIssueMsgList.get(k);
						List<String> warningLocationList = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements[*].location");
						reportBuilder.append("                                                        <div class='accordion-item'>\n");	
						reportBuilder.append("                                                            <h2 class='accordion-header' id='heading"+i+""+j+""+k+"'>\n");
						reportBuilder.append("                                                                <button class='accordion-button collapsed' type='button'\n");
						reportBuilder.append("                                                                    data-bs-toggle='collapse'\n");
						reportBuilder.append("                                                                    data-bs-target='#warningGuidlineMessage-Collapse"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                    aria-expanded='true'\n");
						reportBuilder.append("                                                                    aria-controls='warningGuidlineMessage-Collapse"+i+""+j+""+k+"'>\n");
						reportBuilder.append("                  												 Guideline Message "+(k+1)+": "+issueMsg+" (Elements: "+warningLocationList.size()+")\n");
						reportBuilder.append("                                                                </button>\n");
						reportBuilder.append("                                                            </h2>\n");

						reportBuilder.append("                                                            <!-- Warning Tag Guidline Message Collapse Body -->\n");
						reportBuilder.append("                                                            <div id='warningGuidlineMessage-Collapse"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                class='accordion-collapse collapse'\n");
						reportBuilder.append("                                                                aria-labelledby='heading"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                data-bs-parent='#warningGuidlineMessage-Collapse'>\n");
						reportBuilder.append("                                                                <div class='accordion-body'>\n");
						reportBuilder.append("                                                                    <div class='list-group list-group-numbered'>\n");

						for (int l = 0; l < warningLocationList.size(); l++) {
							String location= warningLocationList.get(l);
							String screenshot=getJsonStringValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements["+l+"].screenShot");
							reportBuilder.append("                                                                        <div\n");
							reportBuilder.append("                                                                            class='list-group-item d-flex justify-content-between align-items-start'>\n");
							reportBuilder.append("                                                                            <div class='ms-2 me-auto'>\n");
							reportBuilder.append("                        <div id=\"WARN"+i+""+j+""+k+""+l+"\" hidden>"+location+"</div>\n");
							reportBuilder.append("                        <div class=\"copyPath fw-bold\">Element "+(l+1)+": \n<img src=\"data:image/png;base64,"+screenshot+"\" alt=\"NON-UI Element\" class=\"hover-image\" onclick=\"copyXPath('WARN"+i+""+j+""+k+""+l+"')\">\n</div>\n");
							reportBuilder.append("                                                                            </div>\n");
							reportBuilder.append("                                                                        </div>\n");
						}		

						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
					}
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
				}
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
			}
			reportBuilder.append("                    </div>\n");
			reportBuilder.append("                </div>\n");
			reportBuilder.append("\n");
		}


		if (noticeList.size()>=1) {
			reportBuilder.append("                <!-- Notice Items Body-->\n");
			reportBuilder.append("                <div class='tab-pane fade' id='contact' role='tabpanel' aria-labelledby='contact-tab'>\n");
			reportBuilder.append("                    <!-- Notice Items Accordino -->\n");
			reportBuilder.append("                    <div class='accordion' id='noticeTagAccordion'>\n");
			reportBuilder.append("                        <!-- Notice Tag Item -->\n");

			for (int i = 0; i < noticeList.size(); i++) {
				String issueTag=noticeList.get(i);
				List<String> noticeIssueCodeList = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes[*].issueCode");
				List<String> noticeLocationList_issueCodeLevel = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes[*].issueDetails[*].issueElements[*].location");
				reportBuilder.append("                        <div class='accordion-item'>\n");	
				reportBuilder.append("                            <h2 class='accordion-header' id='heading"+i+"'>\n");
				reportBuilder.append("                                <button class='accordion-button collapsed' type='button' data-bs-toggle='collapse'\n");
				reportBuilder.append("                                    data-bs-target='#noticeTag-Collapse"+i+"' aria-expanded='true'\n");
				reportBuilder.append("                                    aria-controls='noticeTag-Collapse"+i+"'>\n");
				reportBuilder.append("            						  Tag "+(i+1)+": "+issueTag+" (Elements: "+noticeLocationList_issueCodeLevel.size()+", Guidelines: "+noticeIssueCodeList.size()+")\n");
				reportBuilder.append("                                </button>\n");
				reportBuilder.append("                            </h2>\n");

				reportBuilder.append("                            <!-- Notice Tag Item Collaps Body -->\n");
				reportBuilder.append("                            <div id='noticeTag-Collapse"+i+"' class='accordion-collapse collapse'\n");
				reportBuilder.append("                                aria-labelledby='heading"+i+"' data-bs-parent='#noticeTagAccordion'>\n");
				reportBuilder.append("                                <div class='accordion-body'>\n");
				reportBuilder.append("                                    <!-- Notice Tag Guidline Item -->\n");
				reportBuilder.append("                                    <div class='accordion' id='noticeGuidlineAccordion'>\n");

				for (int j = 0; j < noticeIssueCodeList.size(); j++) {
					String issueCode= noticeIssueCodeList.get(j);
					List<String> noticeIssueMsgList = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails[*].issueMsg");
					List<String> noticeLocationList_msgLevel = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails[*].issueElements[*].location");
					reportBuilder.append("                                        <div class='accordion-item'>\n");
					reportBuilder.append("                                            <h2 class='accordion-header' id='heading"+i+""+j+"'>\n");
					reportBuilder.append("                                                <button class='accordion-button collapsed' type='button'\n");
					reportBuilder.append("                                                    data-bs-toggle='collapse'\n");
					reportBuilder.append("                                                    data-bs-target='#noticeGuidline-Collapse"+i+""+j+"' aria-expanded='true'\n");
					reportBuilder.append("                                                    aria-controls='noticeGuidline-Collapse"+i+""+j+"'>\n");
					reportBuilder.append("                Guidelines "+(j+1)+": "+issueCode+" (Elements: "+noticeLocationList_msgLevel.size()+", Guideline-Message: "+noticeIssueMsgList.size()+")\n");
					reportBuilder.append("                                                </button>\n");
					reportBuilder.append("                                            </h2>\n");

					reportBuilder.append("                                            <!-- Notice Tag Guidline Item Collapse Body -->\n");
					reportBuilder.append("                                            <div id='noticeGuidline-Collapse"+i+""+j+"' class='accordion-collapse collapse'\n");
					reportBuilder.append("                                                aria-labelledby='heading"+i+""+j+"' data-bs-parent='#noticeGuidlineAccordion'>\n");
					reportBuilder.append("                                                <div class='accordion-body'>\n");
					reportBuilder.append("                                                    <!-- Notice Tag Guidline Message Item -->\n");
					reportBuilder.append("                                                    <div class='accordion' id='noticeGuidlineMessage-Collapse'>\n");

					for (int k = 0; k < noticeIssueMsgList.size(); k++) {
						String issueMsg= noticeIssueMsgList.get(k);
						List<String> noticeLocationList = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements[*].location");
						reportBuilder.append("                                                        <div class='accordion-item'>\n");	
						reportBuilder.append("                                                            <h2 class='accordion-header' id='heading"+i+""+j+""+k+"'>\n");
						reportBuilder.append("                                                                <button class='accordion-button collapsed' type='button'\n");
						reportBuilder.append("                                                                    data-bs-toggle='collapse'\n");
						reportBuilder.append("                                                                    data-bs-target='#noticeGuidlineMessage-Collapse"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                    aria-expanded='true'\n");
						reportBuilder.append("                                                                    aria-controls='noticeGuidlineMessage-Collapse"+i+""+j+""+k+"'>\n");
						reportBuilder.append("                   Guideline Message "+(k+1)+": "+issueMsg+" (Elements: "+noticeLocationList.size()+")\n");
						reportBuilder.append("                                                                </button>\n");
						reportBuilder.append("                                                            </h2>\n");

						reportBuilder.append("                                                            <!-- Notice Tag Guidline Message Collapse Body -->\n");
						reportBuilder.append("                                                            <div id='noticeGuidlineMessage-Collapse"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                class='accordion-collapse collapse'\n");
						reportBuilder.append("                                                                aria-labelledby='heading"+i+""+j+""+k+"'\n");
						reportBuilder.append("                                                                data-bs-parent='#noticeGuidlineMessage-Collapse'>\n");
						reportBuilder.append("                                                                <div class='accordion-body'>\n");
						reportBuilder.append("                                                                    <div class='list-group list-group-numbered'>\n");

						for (int l = 0; l < noticeLocationList.size(); l++) {
							String location= noticeLocationList.get(l);
							String screenshot=getJsonStringValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements["+l+"].screenShot");
							reportBuilder.append("                                                                        <div\n");
							reportBuilder.append("                                                                            class='list-group-item d-flex justify-content-between align-items-start'>\n");
							reportBuilder.append("                                                                            <div class='ms-2 me-auto'>\n");
							reportBuilder.append("                        <div id=\"NOTICE"+i+""+j+""+k+""+l+"\" hidden>"+location+"</div>\n");
							reportBuilder.append("                        <div class=\"copyPath fw-bold\">Element "+(l+1)+": \n<img src=\"data:image/png;base64,"+screenshot+"\" alt=\"NON-UI Element\" class=\"hover-image\" onclick=\"copyXPath('NOTICE"+i+""+j+""+k+""+l+"')\">\n</div>\n");
							reportBuilder.append("                                                                            </div>\n");
							reportBuilder.append("                                                                        </div>\n");
						}		

						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
						reportBuilder.append("                                                                    </div>\n");
					}
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
					reportBuilder.append("                                                                </div>\n");
				}
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
				reportBuilder.append("                                                        </div>\n");
			}
			reportBuilder.append("                    </div>\n");
			reportBuilder.append("                </div>\n");
			reportBuilder.append("\n");
		}

		reportBuilder.append("    </div>\n");
		reportBuilder.append("    </div>\n");
		reportBuilder.append("    </div>\n");
		reportBuilder.append("    <div id=\"popupMessage\">Xpath copied to clipboard!</div>\n");
		reportBuilder.append("    <footer class='text-center text-lg-start text-muted border-top mt-5' style='background: #f0f3f9 0 0 no-repeat padding-box;'>\n");
		reportBuilder.append("\n");
		reportBuilder.append("        <div class='text-center p-4' style='background-color: rgba(0, 0, 0, 0.05);'>\n");
		reportBuilder.append("            © 2024 Copyright:\n");
		reportBuilder.append("            <a class='text-reset fw-bold' href='https://fireflink.com/'>www.fireflink.com</a>\n");
		reportBuilder.append("        </div>\n");
		reportBuilder.append("        <!-- Copyright -->\n");
		reportBuilder.append("    </footer>\n");
		reportBuilder.append("    <!-- Footer -->\n");
		String elementsCount=getJsonStringValue(outputJson, "$.elementsCount");


		reportBuilder.append("<script>\n");
		reportBuilder.append("\n");
		reportBuilder.append("\n");
		reportBuilder.append("function readTextFile(file, callback) {\n");
		reportBuilder.append("  var rawFile = new XMLHttpRequest();\n");
		reportBuilder.append("  rawFile.overrideMimeType('application/json');\n");
		reportBuilder.append("  rawFile.open('GET', file, true);\n");
		reportBuilder.append("  rawFile.onreadystatechange = function () {\n");
		reportBuilder.append("    if (rawFile.readyState === 4 && rawFile.status == '200') {\n");
		reportBuilder.append("      callback(rawFile.responseText);\n");
		reportBuilder.append("    }\n");
		reportBuilder.append("  };\n");
		reportBuilder.append("  rawFile.send(null);\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append("//usage:\n");
		reportBuilder.append("setData();\n");
		reportBuilder.append("\n");
		reportBuilder.append("function setData() {\n");
		reportBuilder.append("  setTabCount("+errorLocationList_mainLevel.size()+", "+warnLocationList_mainLevel.size()+", "+noticeLocationList_mainLevel.size()+");\n");
		reportBuilder.append("  setResultMeterProgressBar(\n");
		reportBuilder.append("    "+errorLocationList_mainLevel.size()+",\n");
		reportBuilder.append("    "+warnLocationList_mainLevel.size()+",\n");
		reportBuilder.append("    "+noticeLocationList_mainLevel.size()+"\n");
		reportBuilder.append("  );\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append("function setGuageMeter() {\n");
		reportBuilder.append("  clearGuageMeter();\n");
		reportBuilder.append("  let resultCount = "+score+";\n");
		reportBuilder.append("\n");
		reportBuilder.append("  var guageMeter = document.getElementById('guage-needle');\n");
		reportBuilder.append("  if (resultCount <= 25) {\n");
		reportBuilder.append("    guageMeter.classList.add('rischio1');\n");
		reportBuilder.append("  } else if (resultCount >= 25 && resultCount <= 50) {\n");
		reportBuilder.append("    guageMeter.classList.add('rischio2');\n");
		reportBuilder.append("  } else if (resultCount >= 50 && resultCount <= 75) {\n");
		reportBuilder.append("    guageMeter.classList.add('rischio3');\n");
		reportBuilder.append("  } else if (resultCount >= 75 && resultCount <= 100) {\n");
		reportBuilder.append("    guageMeter.classList.add('rischio4');\n");
		reportBuilder.append("  }\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append("function clearGuageMeter() {\n");
		reportBuilder.append("  var guageMeter = document.getElementById('guage-needle');\n");
		reportBuilder.append("  guageMeter.classList.remove('rischio1');\n");
		reportBuilder.append("  guageMeter.classList.remove('rischio2');\n");
		reportBuilder.append("  guageMeter.classList.remove('rischio3');\n");
		reportBuilder.append("  guageMeter.classList.remove('rischio4');\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append("function setResultMeterProgressBar(errorCount, warningCount, noticesCount) {\n");
		reportBuilder.append("  let errorPer =\n");
		reportBuilder.append("    (errorCount / ("+Integer.parseInt(elementsCount)+")) * 100;\n");
		reportBuilder.append("  let warniongPer =\n");
		reportBuilder.append("    (warningCount / ("+Integer.parseInt(elementsCount)+")) * 100;\n");
		reportBuilder.append("  let noticesPer =\n");
		reportBuilder.append("    (noticesCount / ("+Integer.parseInt(elementsCount)+")) * 100;\n");
		reportBuilder.append("\n");
		reportBuilder.append("  let errorBar = document.getElementById('error-bar');\n");
		reportBuilder.append("  errorBar.innerHTML = parseInt(errorPer) + '%';\n");
		reportBuilder.append("  errorBar.style.width = errorPer + '%';\n");
		reportBuilder.append("  document.getElementById('progressbar-error-count').innerHTML = errorCount;\n");
		reportBuilder.append("\n");
		reportBuilder.append("  let warningBar = document.getElementById('warning-bar');\n");
		reportBuilder.append("  warningBar.innerHTML = parseInt(warniongPer) + '%';\n");
		reportBuilder.append("  warningBar.style.width = warniongPer + '%';\n");
		reportBuilder.append("  document.getElementById('progressbar-warning-count').innerHTML = warningCount;\n");
		reportBuilder.append("\n");
		reportBuilder.append("  let noticesBar = document.getElementById('notices-bar');\n");
		reportBuilder.append("  noticesBar.innerHTML = parseInt(noticesPer) + '%';\n");
		reportBuilder.append("  noticesBar.style.width = noticesPer + '%';\n");
		reportBuilder.append("  document.getElementById('progressbar-notices-count').innerHTML = noticesCount;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append("function setTabCount(errorCount, warningCount, noticesCount) {\n");
		reportBuilder.append("  document.getElementById('error-tab-count').innerHTML = '(' + errorCount + ')';\n");
		reportBuilder.append("  document.getElementById('warning-tab-count').innerHTML =\n");
		reportBuilder.append("    '(' + warningCount + ')';\n");
		reportBuilder.append("  document.getElementById('notices-tab-count').innerHTML =\n");
		reportBuilder.append("    '(' + noticesCount + ')';\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append("</script>\n");
		reportBuilder.append("    <!-- <script src='./asset//js/script.js'></script> -->\n");
		reportBuilder.append("    <script src='https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js'\n");
		reportBuilder.append("        integrity='sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM'\n");
		reportBuilder.append("        crossorigin='anonymous'></script>\n");
		reportBuilder.append("    <script>\n");
		reportBuilder.append("        window.onload = function () {\n");
		reportBuilder.append("            setGuageMeter();\n");
		reportBuilder.append("        };\n");
		reportBuilder.append("\n");
		reportBuilder.append("        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle=\"tooltip\"]'))\n");
		reportBuilder.append("        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {\n");
		reportBuilder.append("            return new bootstrap.Tooltip(tooltipTriggerEl)\n");
		reportBuilder.append("        })\n");
		reportBuilder.append("\n");
		reportBuilder.append("        function copyXPath(elementID) {\n");
		reportBuilder.append("            const textElement = document.getElementById(elementID);\n");
		reportBuilder.append("            const tempInput = document.createElement('textarea');\n");
		reportBuilder.append("            tempInput.value = textElement.innerText;\n");
		reportBuilder.append("            document.body.appendChild(tempInput);\n");
		reportBuilder.append("            tempInput.select();\n");
		reportBuilder.append("            tempInput.setSelectionRange(0, 99999);\n");
		reportBuilder.append("            document.execCommand('copy');\n");
		reportBuilder.append("            document.body.removeChild(tempInput);\n");
		reportBuilder.append("            const popup = document.getElementById('popupMessage');\n");
		reportBuilder.append("            popup.style.visibility = 'visible';\n");
		reportBuilder.append("            setTimeout(function() {\n");
		reportBuilder.append("                popup.style.visibility = 'hidden';\n");
		reportBuilder.append("            }, 1200);  \n");
		reportBuilder.append("        }\n");
		reportBuilder.append("    </script>\n");
		reportBuilder.append("</body>\n");
		reportBuilder.append("\n");
		reportBuilder.append("</html>\n");

		Path htmlFolderPath= Path.of(outputDirectory + File.separator);
		SimpleDateFormat sdf=new SimpleDateFormat("_dd_MM_yyyy_HH_mm_sss");
		String htmlFileName= standard+"_report_"+sdf.format(new Date())+".html";
		String htmlFilePath = htmlFolderPath+File.separator+htmlFileName;
		try {
			if (!Files.exists(htmlFolderPath)) {
				Files.createDirectories(htmlFolderPath);
				System.out.println("Directory created successfully: " + htmlFolderPath.toAbsolutePath());
			} else {
				System.out.println("Directory already exists: " + htmlFolderPath.toAbsolutePath());
			}
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + e.getMessage());
		}
		try (FileWriter fileWriter = new FileWriter(htmlFilePath)) {
			fileWriter.write(reportBuilder.toString());
			System.out.println("HTML report generated successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}       
		return htmlFilePath;
	}

	public static String getJsonStringValue(String jsonBody, String jsonPath) {
		String value = "";
		try {
			value = JsonPath.read(jsonBody, jsonPath).toString().trim();
		} catch (Exception e) {
		}
		return value;
	}


	private static String getBeforeXpath(Element element) {
		String xpath="//*[local-name()=\""+element.tagName()+"\"";
		for (Attribute attribute : element.attributes()) {
			if(StringUtils.equalsAnyIgnoreCase(attribute.getKey(), "style")) {
				xpath=xpath+" and @"+attribute.getKey();
			}			
			else xpath=xpath+" and @"+attribute.getKey() + "=\"" + attribute.getValue()+"\"";
		}
		if(StringUtils.isNotBlank(element.text()) && !element.text().equalsIgnoreCase("...")) {
			xpath=xpath+" and text()=\""+StringEscapeUtils.escapeJava(element.text())+"\"";
		}
		xpath=xpath+"]";
		return xpath;
	}
	
	public static List<String> getJsonListValue(String jsonBody, String jsonPath) {
		List<String> value = new ArrayList<>();
		try {
			value = JsonPath.read(jsonBody, jsonPath);
		} catch (Exception e) {
		}
		return value;
	}

	private static void processIssue(WebDriver driver, ObjectMapper mapper, JsonNode issueNode, Map<Integer, ObjectNode> typeMap) {
		String issueTag = issueNode.get("issueTag").asText();
		int issueType = issueNode.get("issueType").asInt();
		String issueCode = issueNode.get("issueCode").asText();
		String issueMsg = issueNode.get("issueMsg").asText();
		String issueElement = issueNode.get("issueElement").asText();
		issueMsg=issueMsg.replace("'", "\\'");
		issueCode=issueCode.replace("'", "\\'");
		Actions act = new Actions(driver);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		String proc_issueCode="Principle ==> ";

		if(issueCode.startsWith("WCAG")) {
			String[] issueCodeArr = issueCode.split("\\.");		
			if(issueCodeArr[1].endsWith("1"))proc_issueCode=proc_issueCode+"Perceivable (P)";
			else if(issueCodeArr[1].endsWith("2"))proc_issueCode=proc_issueCode+"Operable (O)";
			else if(issueCodeArr[1].endsWith("3"))proc_issueCode=proc_issueCode+"Understandable (U)";
			else if(issueCodeArr[1].endsWith("4"))proc_issueCode=proc_issueCode+"Robust (R)";

			proc_issueCode=proc_issueCode+", "+(issueCodeArr[2].replace("Guideline", "Guideline ==> "));
			proc_issueCode=proc_issueCode+", Criterion ==> "+issueCodeArr[3];
			String techniques="";
			if(issueCodeArr.length>4)	{
				for (int i = 4; i < issueCodeArr.length; i++) techniques=techniques+issueCodeArr[i]+" ::: ";
				proc_issueCode=proc_issueCode+", Techniques ==> "+techniques.substring(0,(techniques.length()-" ::: ".length()));
			}
			issueCode=proc_issueCode;
		}
		
		
		

		ObjectNode typeNode = typeMap.computeIfAbsent(issueType, k -> {
			ObjectNode node = mapper.createObjectNode();
			node.set("issueTags", mapper.createArrayNode());
			return node;
		});
		ObjectNode tagNode = getOrCreateTagNode(mapper, typeNode, issueTag);
		ObjectNode issueCodeNode = getOrCreateIssueCodeNode(mapper, tagNode, issueCode, issueNode);
		ArrayNode issueDetailsArray = (ArrayNode) issueCodeNode.get("issueDetails");
		ObjectNode existingIssueDetailsNode = findExistingIssueDetails(issueDetailsArray, issueMsg);
		if (existingIssueDetailsNode == null) {
			ObjectNode issueDetailsNode = mapper.createObjectNode();
			issueDetailsNode.put("issueMsg", issueMsg);
			ArrayNode issueElementsArray = mapper.createArrayNode();
			ObjectNode elementNode = mapper.createObjectNode();
			elementNode.put("element", issueElement);
			Document doc = Jsoup.parse(issueElement, "", Parser.xmlParser());
			Elements elements = doc.getAllElements();
			for (Element element : elements) { 
				if(element.tagName().equalsIgnoreCase("#root"))continue;
				String xpath=getBeforeXpath(element);
				String screenShot="";
				try{
					WebElement ele=driver.findElement(By.xpath(xpath));
					act.moveToElement(ele).perform();
					((JavascriptExecutor) driver).executeScript("function _0x10bb(_0x523302,_0x391603){var _0x2a429d=_0x2a42();return _0x10bb=function(_0x10bb83,_0x244dfb){_0x10bb83=_0x10bb83-0x64;var _0x54dd38=_0x2a429d[_0x10bb83];return _0x54dd38;},_0x10bb(_0x523302,_0x391603);}function _0x2a42(){var _0x3e3a24=['24AvmFuP','446SrUtst','5235594PlYGSa','7963670BeQHFv','scrollIntoView','3812lVqXpv','center','2982483hDAXMQ','511sWCzBq','1284157odNNdB','345XYgHtJ','28192390EbfuwW'];_0x2a42=function(){return _0x3e3a24;};return _0x2a42();}var _0x4b62ea=_0x10bb;(function(_0x46c347,_0xa6d5ab){var _0x2edfd6=_0x10bb,_0xc2d993=_0x46c347();while(!![]){try{var _0x3eb49a=-parseInt(_0x2edfd6(0x64))/0x1*(-parseInt(_0x2edfd6(0x69))/0x2)+parseInt(_0x2edfd6(0x66))/0x3*(parseInt(_0x2edfd6(0x6d))/0x4)+-parseInt(_0x2edfd6(0x6b))/0x5+-parseInt(_0x2edfd6(0x6a))/0x6+-parseInt(_0x2edfd6(0x65))/0x7*(-parseInt(_0x2edfd6(0x68))/0x8)+-parseInt(_0x2edfd6(0x6f))/0x9+parseInt(_0x2edfd6(0x67))/0xa;if(_0x3eb49a===_0xa6d5ab)break;else _0xc2d993['push'](_0xc2d993['shift']());}catch(_0x540b3f){_0xc2d993['push'](_0xc2d993['shift']());}}}(_0x2a42,0xc2704),arguments[0x0][_0x4b62ea(0x6c)]({'behavior':'smooth','block':_0x4b62ea(0x6e),'inline':_0x4b62ea(0x6e)}));", ele);
					screenShot=ele.getScreenshotAs(OutputType.BASE64);
					Thread.sleep(300);
					screenShot=ele.getScreenshotAs(OutputType.BASE64);
				}catch (Exception e) {

				}
				String xxpath=getXpath(element);
				elementNode.put("location",xxpath);
				elementNode.put("screenShot",screenShot);
				break;
			}
			issueElementsArray.add(elementNode);
			issueDetailsNode.set("issueElements", issueElementsArray);


			issueDetailsArray.add(issueDetailsNode);
		} else {
			ArrayNode existingIssueElementsArray = (ArrayNode) existingIssueDetailsNode.get("issueElements");
			boolean elementExists = false;
			for (JsonNode elementNode : existingIssueElementsArray) {
				if (elementNode.get("element").asText().equals(issueElement)) {
					elementExists = true;
					break;
				}
			}
			if (!elementExists) {
				ObjectNode elementNode = mapper.createObjectNode(); 	           
				elementNode.put("element", issueElement);
				Document doc = Jsoup.parse(issueElement, "", Parser.xmlParser());
				Elements elements = doc.getAllElements();
				for (Element element : elements) { 
					if(element.tagName().equalsIgnoreCase("#root"))continue;
					String xpath=getBeforeXpath(element);
					String screenShot="";
					try{
						WebElement ele=driver.findElement(By.xpath(xpath));
						act.moveToElement(ele).perform();
						((JavascriptExecutor) driver).executeScript("function _0x10bb(_0x523302,_0x391603){var _0x2a429d=_0x2a42();return _0x10bb=function(_0x10bb83,_0x244dfb){_0x10bb83=_0x10bb83-0x64;var _0x54dd38=_0x2a429d[_0x10bb83];return _0x54dd38;},_0x10bb(_0x523302,_0x391603);}function _0x2a42(){var _0x3e3a24=['24AvmFuP','446SrUtst','5235594PlYGSa','7963670BeQHFv','scrollIntoView','3812lVqXpv','center','2982483hDAXMQ','511sWCzBq','1284157odNNdB','345XYgHtJ','28192390EbfuwW'];_0x2a42=function(){return _0x3e3a24;};return _0x2a42();}var _0x4b62ea=_0x10bb;(function(_0x46c347,_0xa6d5ab){var _0x2edfd6=_0x10bb,_0xc2d993=_0x46c347();while(!![]){try{var _0x3eb49a=-parseInt(_0x2edfd6(0x64))/0x1*(-parseInt(_0x2edfd6(0x69))/0x2)+parseInt(_0x2edfd6(0x66))/0x3*(parseInt(_0x2edfd6(0x6d))/0x4)+-parseInt(_0x2edfd6(0x6b))/0x5+-parseInt(_0x2edfd6(0x6a))/0x6+-parseInt(_0x2edfd6(0x65))/0x7*(-parseInt(_0x2edfd6(0x68))/0x8)+-parseInt(_0x2edfd6(0x6f))/0x9+parseInt(_0x2edfd6(0x67))/0xa;if(_0x3eb49a===_0xa6d5ab)break;else _0xc2d993['push'](_0xc2d993['shift']());}catch(_0x540b3f){_0xc2d993['push'](_0xc2d993['shift']());}}}(_0x2a42,0xc2704),arguments[0x0][_0x4b62ea(0x6c)]({'behavior':'smooth','block':_0x4b62ea(0x6e),'inline':_0x4b62ea(0x6e)}));", ele);
						screenShot=ele.getScreenshotAs(OutputType.BASE64);
						Thread.sleep(300);
						screenShot=ele.getScreenshotAs(OutputType.BASE64);
					}catch (Exception e) {

					}
					String xxpath=getXpath(element);
					elementNode.put("location",xxpath);
					elementNode.put("screenShot",screenShot);
					break;
				}
				existingIssueElementsArray.add(elementNode);
			}
		}

	}


	private static String getXpath(Element element) {
		String xpath="//*[local-name()=\""+element.tagName()+"\"";
		for (Attribute attribute : element.attributes()) {
			if(StringUtils.equalsAnyIgnoreCase(attribute.getKey(), "style","data-uuid","data-load-time","data-tagging-id")) {
				xpath=xpath+" and @"+attribute.getKey();
			}
			else if(StringUtils.equalsAnyIgnoreCase(attribute.getKey().trim(), "id","src","href")) {
				if(!attribute.getValue().matches(".*\\d.*"))
				xpath=xpath+" and @"+attribute.getKey() + "=\"" + attribute.getValue()+"\"";
				else xpath=xpath+" and @"+attribute.getKey();
			}
			else {
				xpath=xpath+" and @"+attribute.getKey() + "=\"" + attribute.getValue()+"\"";
			}
		}
		if(StringUtils.isNotBlank(element.text()) && !element.text().equalsIgnoreCase("...")) {
			xpath=xpath+" and text()=\""+StringEscapeUtils.escapeJava(element.text())+"\"";
		}
		xpath=xpath+"]";
		return xpath;
	}

	private static ObjectNode getOrCreateTagNode(ObjectMapper mapper, ObjectNode typeNode, String issueTag) {
		ArrayNode tagArray = (ArrayNode) typeNode.get("issueTags");
		ObjectNode tagNode = null;
		for (JsonNode node : tagArray) {
			if (node.get("issueTag").asText().equals(issueTag)) {
				tagNode = (ObjectNode) node;
				break;
			}
		}

		if (tagNode == null) {
			tagNode = mapper.createObjectNode();
			tagNode.put("issueTag", issueTag);
			tagNode.set("issueCodes", mapper.createArrayNode());
			tagArray.add(tagNode);
		}

		return tagNode;
	}

	private static ObjectNode getOrCreateIssueCodeNode(ObjectMapper mapper, ObjectNode tagNode, String issueCode, JsonNode issueNode) {
		ArrayNode issueCodeArray = (ArrayNode) tagNode.get("issueCodes");
		ObjectNode issueCodeNode = null;
		for (JsonNode node : issueCodeArray) {
			if (node.get("issueCode").asText().equals(issueCode)) {
				issueCodeNode = (ObjectNode) node;
				break;
			}
		}

		if (issueCodeNode == null) {
			issueCodeNode = mapper.createObjectNode();
			issueCodeNode.put("issueCode", issueCode);

			ArrayNode techniquesArray = mapper.createArrayNode();
			for (JsonNode techniqueNode : issueNode.get("issueTechniques")) {
				techniquesArray.add(techniqueNode.asText());
			}
			issueCodeNode.set("issueTechniques", techniquesArray);

			issueCodeNode.set("issueDetails", mapper.createArrayNode());
			issueCodeArray.add(issueCodeNode);
		}

		return issueCodeNode;
	}

	private static ObjectNode findExistingIssueDetails(ArrayNode issueDetailsArray, String issueMsg) {
		for (JsonNode node : issueDetailsArray) {
			if (node.get("issueMsg").asText().equals(issueMsg)) {
				return (ObjectNode) node;
			}
		}
		return null;
	}


	private static String getIssueTypeName(int issueType) {
		switch (issueType) {
		case 1:
			return "Errors";
		case 2:
			return "Warnings";
		case 3:
			return "Notices";
		default: 
			return "Unknown";
		}
	}

	private static Issues3 getIssues(String reportName, WebDriver driver, JavascriptExecutor javascriptExecutor, String standard) {
		Issues3 issues = new Issues3();
		issues.setNotices(noticeCount());
		issues.setWarnings(warningCount());
		issues.setErrors(errorCount());
		issues.setStandard(standard);
		issues.setUrl(driver.getCurrentUrl());
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		issues.setDate( dateFormat.format(new Date()));

		issues.setSize(viewPortWidth(javascriptExecutor) + " X " + viewPortHeight(javascriptExecutor));
		issues.setDevice(device(javascriptExecutor));
		Capabilities capabilities = ((RemoteWebDriver) driver).getCapabilities();
		String browserName = upperCaseFirst(capabilities.getBrowserName()) + " " + capabilities.getBrowserVersion();

		issues.setBrowser(browserName);
		issues.setName(reportName.isEmpty() ? driver.getTitle() : reportName);
		issues.setReportID(UUID.randomUUID().toString().replace("-", ""));
		issues.setIssues(processedIssues);
		return issues;
	}
	static long viewPortHeight(JavascriptExecutor javascriptExecutor) {
		return (long) javascriptExecutor.executeScript("var _0x450594=_0x3a0d;function _0x3388(){var _0x3cc7c1=['382779lTgMeT','1253424vvInBD','12wwwWkp','3808188NeQnTm','3186918omSfEc','2BPVGwz','5juMgtK','4zJAjwA','72fFnUUE','6476240jJNJLp','16315783LTyZXb','innerHeight','2024277ImPpdj'];_0x3388=function(){return _0x3cc7c1;};return _0x3388();}(function(_0x404c7e,_0x32a74d){var _0x5a1a54=_0x3a0d,_0x34f62e=_0x404c7e();while(!![]){try{var _0x56a7a1=parseInt(_0x5a1a54(0x1cd))/0x1*(parseInt(_0x5a1a54(0x1d2))/0x2)+-parseInt(_0x5a1a54(0x1d9))/0x3*(parseInt(_0x5a1a54(0x1d4))/0x4)+-parseInt(_0x5a1a54(0x1d3))/0x5*(-parseInt(_0x5a1a54(0x1d0))/0x6)+-parseInt(_0x5a1a54(0x1d1))/0x7+parseInt(_0x5a1a54(0x1ce))/0x8*(-parseInt(_0x5a1a54(0x1d5))/0x9)+parseInt(_0x5a1a54(0x1d6))/0xa+parseInt(_0x5a1a54(0x1d7))/0xb*(parseInt(_0x5a1a54(0x1cf))/0xc);if(_0x56a7a1===_0x32a74d)break;else _0x34f62e['push'](_0x34f62e['shift']());}catch(_0x278dba){_0x34f62e['push'](_0x34f62e['shift']());}}}(_0x3388,0xbabe1));function _0x3a0d(_0xa78bd1,_0x182ede){var _0x338802=_0x3388();return _0x3a0d=function(_0x3a0d84,_0x1dc6a4){_0x3a0d84=_0x3a0d84-0x1cd;var _0xa5cb73=_0x338802[_0x3a0d84];return _0xa5cb73;},_0x3a0d(_0xa78bd1,_0x182ede);}return window[_0x450594(0x1d8)];");
	}

	private static String upperCaseFirst(String name) {
		char[] array = name.toCharArray();
		array[0] = Character.toUpperCase(array[0]);
		return new String(array);
	}
	static String device(JavascriptExecutor javascriptExecutor)  {
		int width = (int) viewPortWidth(javascriptExecutor);
		if (width < 768) {
			return "Phone";
		} else if (width < 992) {
			return "Tablet";
		} else if (width < 1200) {
			return "Small Laptop";
		} else {
			return "Laptop/Desktop";
		}
	}

	static long viewPortWidth(JavascriptExecutor javascriptExecutor) {
		return (long) javascriptExecutor.executeScript("function _0x344a(){var _0x12c9cb=['194628QHzCdB','4CcjaOh','532680KWjCPs','406818NTNqsC','263082fXfppE','1334433vEycmm','96fBarfI','611810JsVeJd','innerWidth','413998KDOfnq'];_0x344a=function(){return _0x12c9cb;};return _0x344a();}function _0x5d6f(_0x65c7ba,_0x2bf40c){var _0x344a61=_0x344a();return _0x5d6f=function(_0x5d6f57,_0x10d701){_0x5d6f57=_0x5d6f57-0x1a6;var _0x1abdf3=_0x344a61[_0x5d6f57];return _0x1abdf3;},_0x5d6f(_0x65c7ba,_0x2bf40c);}var _0x5e9b91=_0x5d6f;(function(_0x318142,_0x3cecc2){var _0x570342=_0x5d6f,_0x59608e=_0x318142();while(!![]){try{var _0x47c045=-parseInt(_0x570342(0x1ae))/0x1+parseInt(_0x570342(0x1a9))/0x2+-parseInt(_0x570342(0x1af))/0x3+-parseInt(_0x570342(0x1ab))/0x4*(-parseInt(_0x570342(0x1a7))/0x5)+parseInt(_0x570342(0x1ac))/0x6+-parseInt(_0x570342(0x1aa))/0x7+-parseInt(_0x570342(0x1a6))/0x8*(-parseInt(_0x570342(0x1ad))/0x9);if(_0x47c045===_0x3cecc2)break;else _0x59608e['push'](_0x59608e['shift']());}catch(_0x1d5dec){_0x59608e['push'](_0x59608e['shift']());}}}(_0x344a,0x36e64));return window[_0x5e9b91(0x1a8)];");
	}
	static int errorCount() {
		return getCount(processedIssues, 1);
	}

	static int noticeCount() {
		return getCount(processedIssues, 3);
	}

	static int warningCount() {
		return getCount(processedIssues, 2);
	}


	private static 	int getCount(List<Issue3> issues, int issueType) {
		List<Issue3> filteredIssues = issues.stream()
				.filter(issue -> issue.getIssueType() == issueType)
				.collect(Collectors.toList());
		return filteredIssues.size();
	}
	private static List<Issue3> issueList(List<Map<String, String>> issuesList) {
		return issuesList.stream().map(entry -> {
			ObjectMapper objectMapper = new ObjectMapper();
			Issue3 issue = objectMapper.convertValue(entry, Issue3.class);
			issue.setIssueTechniques(getIssueTechniques(issue.getIssueCode()));
			return issue;
		}).collect(Collectors.toList());
	}

	private static void waitForLoad(WebDriver driver) {
		ExpectedCondition<Boolean> pageLoadCondition = webDriver -> ((JavascriptExecutor) webDriver).executeScript("var _0x335e7e=_0x39b3;function _0x39b3(_0x4c6160,_0x371dac){var _0x3e58fe=_0x3e58();return _0x39b3=function(_0x39b329,_0x15d951){_0x39b329=_0x39b329-0x141;var _0x497a92=_0x3e58fe[_0x39b329];return _0x497a92;},_0x39b3(_0x4c6160,_0x371dac);}(function(_0x34da1a,_0x1e82c1){var _0x570f4d=_0x39b3,_0x4b30b3=_0x34da1a();while(!![]){try{var _0x252d9a=-parseInt(_0x570f4d(0x141))/0x1*(parseInt(_0x570f4d(0x147))/0x2)+-parseInt(_0x570f4d(0x14a))/0x3+-parseInt(_0x570f4d(0x143))/0x4+parseInt(_0x570f4d(0x145))/0x5+-parseInt(_0x570f4d(0x14b))/0x6*(-parseInt(_0x570f4d(0x142))/0x7)+-parseInt(_0x570f4d(0x149))/0x8*(-parseInt(_0x570f4d(0x148))/0x9)+parseInt(_0x570f4d(0x146))/0xa;if(_0x252d9a===_0x1e82c1)break;else _0x4b30b3['push'](_0x4b30b3['shift']());}catch(_0x19252b){_0x4b30b3['push'](_0x4b30b3['shift']());}}}(_0x3e58,0xbd40d));function _0x3e58(){var _0xbb73ed=['3180654qGrQpY','4203EfIFpv','7pzxyOv','2595420xRBvre','readyState','502265HQjAeD','24562760wZnZTi','614CQWRIp','333513hBGOfU','40OAjxBG','1673298PZFvia'];_0x3e58=function(){return _0xbb73ed;};return _0x3e58();}return document[_0x335e7e(0x144)];").equals("complete");
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(pageLoadCondition);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static List<String> getIssueTechniques(String issueCode) {
		Pattern pattern = Pattern.compile("([A-Z]+[0-9]+(,[A-Z]+[0-9]+)*)");
		Matcher matcher = pattern.matcher(issueCode);
		LinkedList<String> codes = new LinkedList<>();
		while (matcher.find()) {
			String match = matcher.group();
			if (match.contains(",")) {
				String[] techniques = match.split(",");
				codes.addAll(Arrays.asList(techniques));
			} else {
				codes.add(match);
			}
		}
		if (codes.size() != 0) {
			codes.remove(0);
		}
		return codes.stream()
				.map(code -> "https://www.w3.org/TR/WCAG20-TECHS/" + code)
				.collect(Collectors.toList());
	}	  

}


class Issue3 {
	private Long issueType;
	private String issueCode;
	private List<String> issueTechniques;
	private String issueTag;
	private String issueId;
	private String issueMsg;
	private String issueElement;

	public List<String> getIssueTechniques() {
		return issueTechniques;
	}

	public void setIssueTechniques(List<String> issueTechniques) {
		this.issueTechniques = issueTechniques;
	}

	public Long getIssueType() {
		return issueType;
	}

	public void setIssueType(Long issueType) {
		this.issueType = issueType;
	}

	public String getIssueCode() {
		return issueCode;
	}

	public void setIssueCode(String issueCode) {
		this.issueCode = issueCode;
	}

	public String getIssueTag() {
		return issueTag;
	}

	public void setIssueTag(String issueTag) {
		this.issueTag = issueTag;
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	public String getIssueMsg() {
		return issueMsg;
	}

	public void setIssueMsg(String issueMsg) {
		this.issueMsg = issueMsg;
	}

	public String getIssueElement() {
		return issueElement;
	}

	public void setIssueElement(String issueElement) {
		this.issueElement = issueElement;
	}
}


class Issues3 {
	private int notices;
	private int warnings;
	private int errors;
	private String name;
	private String standard;
	private String url;
	private String date;
	private String size;
	private String device;
	private String browser;
	private String reportID;
	private List<Issue3> issues;

	public String getReportID() {
		return reportID;
	}

	public void setReportID(String reportID) {
		this.reportID = reportID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrowser() {
		return browser;
	}

	public  void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getNotices() {
		return notices;
	}

	public void setNotices(int notices) {
		this.notices = notices;
	}

	public int getWarnings() {
		return warnings;
	}

	public void setWarnings(int warnings) {
		this.warnings = warnings;
	}

	public  int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public String getStandard() {
		return standard;
	}

	public void setStandard(String standard) {
		this.standard = standard;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Issue3> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue3> issues) {
		this.issues = issues;
	}
}
