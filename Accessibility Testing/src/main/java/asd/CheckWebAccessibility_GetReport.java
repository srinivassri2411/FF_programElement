
package asd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;






public class CheckWebAccessibility_GetReport implements Nlp {	
	
	@InputParams({
		@InputParam(name = "Accessibility Json Data", type = "String"),
		@InputParam(name = "Output Report Directory", type = "String")
	})
	@ReturnType(name = "Output", type = "String")
	
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String jsonData = (String) programElementsInput.get("Accessibility Json Data");
		String outputDirectory = (String) programElementsInput.get("Output Report Directory");

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			outputDirectory=outputDirectory.replace("\\", "/");
			String result = createHtmlReport(jsonData,outputDirectory);
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

	
	
	private static String createHtmlReport(String outputJson, String outputDirectory) {
		
		StringBuilder reportBuilder=new StringBuilder();
		reportBuilder.append("<!DOCTYPE html>\n");
		reportBuilder.append("<html lang=\"en\">\n");

		reportBuilder.append("<head>\n");
		reportBuilder.append("    <meta charset=\"UTF-8\">\n");
		reportBuilder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
		reportBuilder.append("    <title>Accessibility Issue Report</title>\n");
		reportBuilder.append("    <link rel='icon' href='https://res.cloudinary.com/dposietlk/image/upload/v1722593659/favicon_itzjrc.png' type='image/png'>\n");

		reportBuilder.append("<style>   \n");
		reportBuilder.append("body {\n");
		reportBuilder.append("    background: #e8efff; \n");
		reportBuilder.append("    margin: 0;\n");
		reportBuilder.append("    font-family: 'Roboto', sans-serif; \n");
		reportBuilder.append("    padding-top: 50px; \n");
		reportBuilder.append("    padding-bottom: 60px; \n");
		reportBuilder.append("    color: #3c4a8b; \n");
		reportBuilder.append("	  overflow: hidden;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("#popupMessage {\n");
		reportBuilder.append("    visibility: hidden;  \n");
		reportBuilder.append("    background-color: #4CAF50;\n");
		reportBuilder.append("    color: white;\n");
		reportBuilder.append("    text-align: center;\n");
		reportBuilder.append("    border-radius: 5px;\n");
		reportBuilder.append("    padding: 10px;\n");
		reportBuilder.append("    position: fixed;\n");
		reportBuilder.append("    z-index: 1;\n");
		reportBuilder.append("    left: 50%;\n");
		reportBuilder.append("    top: 70px;\n");
		reportBuilder.append("    transform: translateX(-50%);\n");
		reportBuilder.append("    font-size: 17px;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".header {\n");
		reportBuilder.append("    position: fixed;\n");
		reportBuilder.append("    top: 0;\n");
		reportBuilder.append("    left: 0;\n");
		reportBuilder.append("    width: 100%;\n");
		reportBuilder.append("    background-color: #00274d; \n");
		reportBuilder.append("    color: white;\n");
		reportBuilder.append("    text-align: center;\n");
		reportBuilder.append("    padding: 10px 0;\n");
		reportBuilder.append("    z-index: 1000;\n");
		reportBuilder.append("    display: flex;\n");
		reportBuilder.append("    justify-content: space-between;\n");
		reportBuilder.append("    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);\n");
		reportBuilder.append("    transition: background-color 0.3s ease, box-shadow 0.3s ease;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".header:hover {\n");
		reportBuilder.append("    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4);\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".footer {\n");
		reportBuilder.append("    position: fixed;\n");
		reportBuilder.append("    bottom: 0;\n");
		reportBuilder.append("    left: 0;\n");
		reportBuilder.append("    width: 100%;\n");
		reportBuilder.append("    background-color: #e1e4e8; \n");
		reportBuilder.append("    color: white;\n");
		reportBuilder.append("    text-align: center;\n");
		reportBuilder.append("    padding-top: 5px;\n");
		reportBuilder.append("    box-shadow: 0 -6px 12px rgba(0, 0, 0, 0.3);\n");
		reportBuilder.append("    transition: background-color 0.3s ease, box-shadow 0.3s ease;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".footer:hover {\n");
		reportBuilder.append("    box-shadow: 0 -8px 16px rgba(0, 0, 0, 0.4);\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".info-container {\n");
		reportBuilder.append("    display: grid;\n");
		reportBuilder.append("    grid-template-columns: repeat(2, 1fr);\n");
		reportBuilder.append("    gap: 20px;\n");
		reportBuilder.append("	  margin-top:15px;\n");
		reportBuilder.append("	  margin-right:20px;\n");
		reportBuilder.append("	  margin-left:20px;\n");
		reportBuilder.append("	  margin-bottom:15px; \n");
		reportBuilder.append("}\n");
		reportBuilder.append("a:hover {\n");
		reportBuilder.append("    opacity: 0.9;\n");
		reportBuilder.append("    transform: translateY(-4px);\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".info-container div {\n");
		reportBuilder.append("	  text-align: center;\n");
		reportBuilder.append("    background-color: #ffffff;\n");
		reportBuilder.append("    color: #3c4a8b; \n");
		reportBuilder.append("    padding: 10px;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    border: 1px solid #ddd;\n");
		reportBuilder.append("    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);\n");
		reportBuilder.append("    transition: box-shadow 0.3s ease, transform 0.3s ease;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab-container {\n");
		reportBuilder.append("    display: flex;\n");
		reportBuilder.append("    justify-content: space-around;\n");
		reportBuilder.append("    margin-right: 20px;\n");
		reportBuilder.append("    margin-left: 20px ;\n");
		reportBuilder.append("    margin-bottom: 5px;\n");
		reportBuilder.append("    border-bottom: 3px solid #3c4a8b; \n");
		reportBuilder.append("    background-color: #ffffff;\n");
		reportBuilder.append("    border-radius: 12px 12px 0 0;\n");
		reportBuilder.append("    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab {\n");
		reportBuilder.append("    padding: 8px 25px;\n");
		reportBuilder.append("    cursor: pointer;\n");
		reportBuilder.append("    font-weight: 550;\n");
		reportBuilder.append("    color: #000000;\n");
		reportBuilder.append("    border-radius: 12px 12px 0 0;\n");
		reportBuilder.append("    text-align: center;\n");
		reportBuilder.append("    transition: background-color 0.3s ease, transform 0.3s ease;\n");
		reportBuilder.append("    position: relative;\n");
		reportBuilder.append("    overflow: hidden;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab:hover {\n");
		reportBuilder.append("    background-color: #0056b3;\n");
		reportBuilder.append("    opacity: 0.9;\n");
		reportBuilder.append("    transform: translateY(-4px); \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab.active {\n");
		reportBuilder.append("	  box-shadow: 5px 5px 5px rgba(150, 150, 150, 1);\n");
		reportBuilder.append("    background: linear-gradient(135deg, #ffffff, #e0e0e0); \n");
		reportBuilder.append("    color: #00274d; \n");
		reportBuilder.append("    text-transform: uppercase;\n");
		reportBuilder.append("    font-size: 16px;\n");
		reportBuilder.append("    transform: translateY(-4px); \n");
		reportBuilder.append("    font-weight: bold;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("\n");
		reportBuilder.append(".tab.active::after {\n");
		reportBuilder.append("    content: \"\";\n");
		reportBuilder.append("    position: absolute;\n");
		reportBuilder.append("    left: 0;\n");
		reportBuilder.append("    top: 0;\n");
		reportBuilder.append("    height: 100%;\n");
		reportBuilder.append("    width: 8px; \n");
		reportBuilder.append("    background-color: #00274d;\n");
		reportBuilder.append("    border-radius: 0 12px 12px 0; \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab.errors {\n");
		reportBuilder.append("    background: linear-gradient(135deg, #ff6f61, #ff3d39); \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab.warnings {\n");
		reportBuilder.append("    background: linear-gradient(135deg, #ffeb3b, #fdbe35); \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab.notices {\n");
		reportBuilder.append("    background: linear-gradient(135deg, #64b5f6, #2196f3); \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".copyPath {\n");
		reportBuilder.append("    text-align: left;\n");
		reportBuilder.append("    font-weight: 600;\n");
		reportBuilder.append("    display: flex;\n");
		reportBuilder.append("    align-items: center;\n");
		reportBuilder.append("	  padding: 20px;\n");
		reportBuilder.append("	  border-bottom: 3px dotted #bbb;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab-content {\n");
		reportBuilder.append("    display: none;\n");
		reportBuilder.append("    background-color: #ffffff;\n");
		reportBuilder.append("    color: #3c4a8b; \n");
		reportBuilder.append("    border-radius: 0 0 12px 12px;\n");
		reportBuilder.append("    padding: 20px;\n");
		reportBuilder.append("    margin: 0 20px;\n");
		reportBuilder.append("    border: 1px solid #ddd;\n");
		reportBuilder.append("    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);\n");
		reportBuilder.append("    transition: opacity 0.3s ease, max-height 0.3s ease;\n");
		reportBuilder.append("    opacity: 0;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab-content.active {\n");
		reportBuilder.append("    display: block;\n");
		reportBuilder.append("    opacity: 1;	\n");
		reportBuilder.append("	  overflow-y: auto;	\n");
		reportBuilder.append("	  height:60vh;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-tag {\n");
		reportBuilder.append("    margin-bottom: 20px;\n");
		reportBuilder.append("    border: 1px solid #ddd;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    padding: 10px;\n");
		reportBuilder.append("    background-color: #f9f9f9;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-code {\n");
		reportBuilder.append("    cursor: pointer;\n");
		reportBuilder.append("    color: #3c4a8b; \n");
		reportBuilder.append("    font-size: 16px; \n");
		reportBuilder.append("    background-color: #ffffff;\n");
		reportBuilder.append("    padding: 12px 16px;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\n");
		reportBuilder.append("    margin-bottom: 12px;\n");
		reportBuilder.append("    transition: background-color 0.3s ease, transform 0.3s ease;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-code:hover, .issue-code.active {\n");
		reportBuilder.append("    background-color: #f5f7fa; \n");
		reportBuilder.append("    transform: translateY(-4px); \n");
		reportBuilder.append("    font-weight: bold; \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-elements {\n");
		reportBuilder.append("    display: none;\n");
		reportBuilder.append("    margin: 20px 0;\n");
		reportBuilder.append("    padding: 20px;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    background-color: #ffffff;\n");
		reportBuilder.append("    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);\n");
		reportBuilder.append("    transition: max-height 0.3s ease, opacity 0.3s ease;\n");
		reportBuilder.append("    opacity: 0;\n");
		reportBuilder.append("    max-height: 0;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-elements.show {\n");
		reportBuilder.append("    display: block;\n");
		reportBuilder.append("    opacity: 1;\n");
		reportBuilder.append("    max-height: 100%; \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-element {\n");
		reportBuilder.append("    margin-top: 10px;\n");
		reportBuilder.append("    padding: 12px 24px;\n");
		reportBuilder.append("    background-color: #007bff; \n");
		reportBuilder.append("    color: white;\n");
		reportBuilder.append("    border: none;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    cursor: pointer;\n");
		reportBuilder.append("    transition: background-color 0.3s ease, transform 0.3s ease;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-element:hover {\n");
		reportBuilder.append("    background-color: #0056b3;\n");
		reportBuilder.append("    transform: translateY(-4px); \n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-message {\n");
		reportBuilder.append("    cursor: pointer;\n");
		reportBuilder.append("    background-color: #f0f4f8;\n");
		reportBuilder.append("    border: 1px solid #ddd;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    padding: 15px;\n");
		reportBuilder.append("    margin-bottom: 10px;\n");
		reportBuilder.append("    transition: background-color 0.3s ease, box-shadow 0.3s ease;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".issue-message:hover, .issue-message.active {\n");
		reportBuilder.append("    background-color: #e1e8f0;\n");
		reportBuilder.append("    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);\n");
		reportBuilder.append("	  font-weight: bold;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("@media (max-width: 768px) {\n");
		reportBuilder.append("    .info-container {\n");
		reportBuilder.append("        grid-template-columns: 1fr;\n");
		reportBuilder.append("        margin: 20px;\n");
		reportBuilder.append("    }\n");
		reportBuilder.append("    .tab-container {\n");
		reportBuilder.append("        flex-direction: column;\n");
		reportBuilder.append("    }\n");
		reportBuilder.append("    .tab {\n");
		reportBuilder.append("        margin-bottom: 10px;\n");
		reportBuilder.append("    }\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".hover-image:hover {\n");
		reportBuilder.append("    transform: scale(2); \n");
		reportBuilder.append("	  z-index: 10;\n");
		reportBuilder.append("	  position: relative;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".hover-image {\n");
		reportBuilder.append("    width: 150px; \n");
		reportBuilder.append("    height: auto; \n");
		reportBuilder.append("    transition: transform 0.3s ease-in-out; \n");
		reportBuilder.append("	  margin: 0 auto; \n");
		reportBuilder.append("	  max-width: 220px;\n");
		reportBuilder.append("    height: auto;\n");
		reportBuilder.append("    display: block;\n");
		reportBuilder.append("    margin-top: 10px;\n");
		reportBuilder.append("    border: 1px solid #ddd;\n");
		reportBuilder.append("    border-radius: 12px;\n");
		reportBuilder.append("    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".tab .count { \n");
		reportBuilder.append("	  color: #000000;\n");
		reportBuilder.append("    display: block;\n");
		reportBuilder.append("	  margin-top: 5px;\n");
		reportBuilder.append("}\n");
		reportBuilder.append(".count {\n");
		reportBuilder.append("	  font-size: 12px;\n");
		reportBuilder.append("}\n");
		reportBuilder.append("</style>\n");

		reportBuilder.append("    <script>\n");
		reportBuilder.append("        function openTab(event, tabId) {\n");
		reportBuilder.append("		 const issueCode = document.getElementsByClassName('issue-code');\n");
		reportBuilder.append("		for (let i = 0; i < issueCode.length; i++) {\n");
		reportBuilder.append("			issueCode[i].classList.remove('active');\n");
		reportBuilder.append("		} \n");
		reportBuilder.append("		const issueMessage = document.getElementsByClassName('issue-message');\n");
		reportBuilder.append("		for (let i = 0; i < issueMessage.length; i++) {\n");
		reportBuilder.append("			issueMessage[i].classList.remove('active');\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		const issueEle = document.getElementsByClassName('issue-elements');\n");
		reportBuilder.append("		for (let i = 0; i < issueEle.length; i++) {\n");
		reportBuilder.append("			issueEle[i].classList.remove('show');\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		const tabContents = document.getElementsByClassName('tab-content');\n");
		reportBuilder.append("		const tabs = document.getElementsByClassName('tab');\n");
		reportBuilder.append("		for (let i = 0; i < tabContents.length; i++) {\n");
		reportBuilder.append("			tabContents[i].classList.remove('active');\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		for (let i = 0; i < tabs.length; i++) {\n");
		reportBuilder.append("			tabs[i].classList.remove('active');\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		document.getElementById(tabId).classList.add('active');\n");
		reportBuilder.append("		event.currentTarget.classList.add('active');\n");
		reportBuilder.append("		const details = document.getElementsByClassName('details');\n");
		reportBuilder.append("		for (let i = 0; i < details.length; i++) {\n");
		reportBuilder.append("			details[i].classList.remove('show');\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		const subsectionHeader = document.getElementsByClassName('subsection-header');\n");
		reportBuilder.append("		for (let i = 0; i < subsectionHeader.length; i++) {\n");
		reportBuilder.append("			subsectionHeader[i].classList.remove('active');\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		}\n");
		reportBuilder.append("		\n");
		reportBuilder.append("        function toggleIssueElements(event, id) {\n");
		reportBuilder.append("            const element = document.getElementById(id);\n");
		reportBuilder.append("            element.classList.toggle('show');\n");
		reportBuilder.append("            event.currentTarget.classList.toggle('active');\n");
		reportBuilder.append("        }\n");
		reportBuilder.append("		\n");
		reportBuilder.append("        function copyXPath(elementID) {\n");
		reportBuilder.append("            const textElement = document.getElementById(elementID);\n");
		reportBuilder.append("            const tempInput = document.createElement(\"textarea\");\n");
		reportBuilder.append("            tempInput.value = textElement.innerText;\n");
		reportBuilder.append("            document.body.appendChild(tempInput);\n");
		reportBuilder.append("            tempInput.select();\n");
		reportBuilder.append("            tempInput.setSelectionRange(0, 99999);\n");
		reportBuilder.append("            document.execCommand(\"copy\");\n");
		reportBuilder.append("            document.body.removeChild(tempInput);\n");
		reportBuilder.append("            const popup = document.getElementById(\"popupMessage\");\n");
		reportBuilder.append("            popup.style.visibility = \"visible\";\n");
		reportBuilder.append("            setTimeout(function() {\n");
		reportBuilder.append("                popup.style.visibility = \"hidden\";\n");
		reportBuilder.append("            }, 1200);  \n");
		reportBuilder.append("        }\n");
		reportBuilder.append("    </script>\n");

		reportBuilder.append("    <link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.6.3/css/all.css\">\n");
		reportBuilder.append("    <link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">\n");
		reportBuilder.append("</head>\n");

		reportBuilder.append("<body>\n");
		reportBuilder.append("    <div class=\"header\">\n");
		reportBuilder.append("        <img style=\"margin-left: 20px;\" src='https://res.cloudinary.com/dposietlk/image/upload/v1722593660/fireflink-white-logo_yi0ltk.png' alt='Fireflink Logo' height='30px'>\n");
		reportBuilder.append("        <h1 style=\"font-size: 24px; margin: 0; margin-right: 20px;\"><i class=\"fas fa-universal-access\" style=\"font-size: 30px;\"></i> Fireflink Accessibility Report</h1>\n");
		reportBuilder.append("    </div>\n");
		
		String url=getJsonStringValue(outputJson, "$.url");
		String standard=getJsonStringValue(outputJson, "$.standard");
		
		reportBuilder.append("    <div class=\"info-container\">\n");
		reportBuilder.append("        <a href=\""+url+"\" target=\"_blank\" style=\"text-decoration: none;\"><div><i class=\"fas fa-external-link-alt text-warning\" style=\"color:orange;\"></i><strong style=\"color:orange;\"> URL:</strong> "+url+"</div></a>\n");
		reportBuilder.append("        <div><i class=\"fas fa-list-ul text-warning\" style=\"color:orange;\"></i><strong style=\"color:orange;\"> Standard:</strong> "+(standard.replace("2", " - "))+"</div>\n");
		reportBuilder.append("    </div>\n");

		List<String> errorList = getJsonListValue(outputJson, "$.Errors[*].issueTag");
		List<String> warningList = getJsonListValue(outputJson, "$.Warnings[*].issueTag");
		List<String> noticeList = getJsonListValue(outputJson, "$.Notices[*].issueTag");

		reportBuilder.append("    <div class=\"tab-container\">\n");
		List<String> errorGuidelines = getJsonListValue(outputJson, "$.Errors[*].issueCodes[*].issueCode");
		List<String> errorLocationList_mainLevel = getJsonListValue(outputJson, "$.Errors[*].issueCodes[*].issueDetails[*].issueElements[*].location");
		
		List<String> warningGuidelines = getJsonListValue(outputJson, "$.Warnings[*].issueCodes[*].issueCode");
		List<String> warnLocationList_mainLevel = getJsonListValue(outputJson, "$.Warnings[*].issueCodes[*].issueDetails[*].issueElements[*].location");
		
		List<String> noticeGuidelines = getJsonListValue(outputJson, "$.Notices[*].issueCodes[*].issueCode");
		List<String> noticeLocationList_mainLevel = getJsonListValue(outputJson, "$.Notices[*].issueCodes[*].issueDetails[*].issueElements[*].location");
		if (errorList.size()>=1) reportBuilder.append("        <div class=\"tab errors active\" onclick=\"openTab(event, 'errorTab')\">Errors ("+errorLocationList_mainLevel.size()+") \n<span class=\"count\">Tags: "+errorList.size()+", Elements: "+errorLocationList_mainLevel.size()+"</span></div>\n");
		if (warningList.size()>=1) reportBuilder.append("      <div class=\"tab warnings\" onclick=\"openTab(event, 'warningTab')\">Warnings ("+warnLocationList_mainLevel.size()+") \n<span class=\"count\">Tags: "+warningList.size()+", Elements: "+warnLocationList_mainLevel.size()+"</span></div>\n");
		if (noticeList.size()>=1) reportBuilder.append("       <div class=\"tab notices\" onclick=\"openTab(event, 'noticeTab')\">Notices ("+noticeLocationList_mainLevel.size()+") \n<span class=\"count\">Tags: "+noticeList.size()+", Elements: "+noticeLocationList_mainLevel.size()+"</span></div>\n");

		reportBuilder.append("    </div>\n");

		if (errorList.size()>=1) reportBuilder.append("    <div id=\"errorTab\" class=\"tab-content active\">\n");

		for (int i = 0; i < errorList.size(); i++) {
			String issueTag=errorList.get(i);
			List<String> errorIssueCodeList = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes[*].issueCode");
			List<String> errorLocationList_issueCodeLevel = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes[*].issueDetails[*].issueElements[*].location");
			reportBuilder.append("        <div class=\"issue-tag\">\n");
			reportBuilder.append("            <div class=\"issue-code\" onclick=\"toggleIssueElements(event, 'errorTag"+i+"')\">Tag "+(i+1)+": "+issueTag+" \n<span class=\"count\">(Elements: "+errorLocationList_issueCodeLevel.size()+", Guidelines: "+errorIssueCodeList.size()+")</span></div>\n");
			reportBuilder.append("            <div id=\"errorTag"+i+"\" class=\"issue-elements\">\n");

			for (int j = 0; j < errorIssueCodeList.size(); j++) {
				String issueCode=errorIssueCodeList.get(j);
				List<String> errorIssueMsgList = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails[*].issueMsg");
				List<String> errorLocationList_msgLevel = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails[*].issueElements[*].location");
				reportBuilder.append("                <div class=\"issue-code\" onclick=\"toggleIssueElements(event, 'errorCode"+i+""+j+"')\">Guidelines "+(j+1)+": "+issueCode+" \n<span class=\"count\">(Elements: "+errorLocationList_msgLevel.size()+", Guideline-Message: "+errorIssueMsgList.size()+")</span></div>\n");
				reportBuilder.append("                <div id=\"errorCode"+i+""+j+"\" class=\"issue-elements\">\n");

				for (int k = 0; k < errorIssueMsgList.size(); k++) {
					String issueMsg=errorIssueMsgList.get(k);
					List<String> errorLocationList = getJsonListValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements[*].location");
					reportBuilder.append("                    <div class=\"issue-message\" onclick=\"toggleIssueElements(event, 'errorMsg"+i+""+j+""+k+"')\">Guideline Message "+(k+1)+": "+issueMsg+" \n<span class=\"count\">(Elements: "+errorLocationList.size()+")</span></div>\n");
					reportBuilder.append("                    <div id=\"errorMsg"+i+""+j+""+k+"\" class=\"issue-elements\">\n");

					for (int l = 0; l < errorLocationList.size(); l++) {
						String location=errorLocationList.get(l);
						String screenshot=getJsonStringValue(outputJson, "$.Errors["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements["+l+"].screenShot");
						reportBuilder.append("                        <div id=\"ERR"+i+""+j+""+k+""+l+"\" hidden>"+location+"</div>\n");
						reportBuilder.append("                        <div class=\"copyPath\">Element "+(l+1)+": \n<img src=\"data:image/png;base64,"+screenshot+"\" alt=\"NON-UI Element\" class=\"hover-image\" onclick=\"copyXPath('ERR"+i+""+j+""+k+""+l+"')\">\n</div>\n");
					}
					reportBuilder.append("                    </div>\n");
				}
				reportBuilder.append("                </div>\n");
			}
			reportBuilder.append("            </div>\n");
			reportBuilder.append("        </div>\n");
		}

		if (errorList.size()>=1) {
			reportBuilder.append("    </div>\n");
		}


		if (warningList.size()>=1) reportBuilder.append("    <div id=\"warningTab\" class=\"tab-content\">\n");

		for (int i = 0; i < warningList.size(); i++) {
			String issueTag=warningList.get(i);
			List<String> warningIssueCodeList = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes[*].issueCode");
			List<String> warningLocationList_issueCodeLevel = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes[*].issueDetails[*].issueElements[*].location");
			reportBuilder.append("        <div class=\"issue-tag\">\n");
			reportBuilder.append("            <div class=\"issue-code\" onclick=\"toggleIssueElements(event, 'warningTag"+i+"')\">Tag "+(i+1)+": "+issueTag+" \n<span class=\"count\">(Elements: "+warningLocationList_issueCodeLevel.size()+", Guidelines: "+warningIssueCodeList.size()+")</span></div>\n");
			reportBuilder.append("            <div id=\"warningTag"+i+"\" class=\"issue-elements\">\n");

			for (int j = 0; j < warningIssueCodeList.size(); j++) {
				String issueCode= warningIssueCodeList.get(j);
				List<String> warningIssueMsgList = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails[*].issueMsg");
				List<String> warningLocationList_msgLevel = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails[*].issueElements[*].location");
				reportBuilder.append("                <div class=\"issue-code\" onclick=\"toggleIssueElements(event, 'warningCode"+i+""+j+"')\">Guidelines "+(j+1)+": "+issueCode+" \n<span class=\"count\">(Elements: "+warningLocationList_msgLevel.size()+", Guideline-Message: "+warningIssueMsgList.size()+")</span></div>\n");
				reportBuilder.append("                <div id=\"warningCode"+i+""+j+"\" class=\"issue-elements\">\n");

				for (int k = 0; k < warningIssueMsgList.size(); k++) {
					String issueMsg= warningIssueMsgList.get(k);
					List<String> warningLocationList = getJsonListValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements[*].location");
					reportBuilder.append("                    <div class=\"issue-message\" onclick=\"toggleIssueElements(event, 'warningMsg"+i+""+j+""+k+"')\">Guideline Message "+(k+1)+": "+issueMsg+" \n<span class=\"count\">(Elements: "+warningLocationList.size()+")</span></div>\n");
					reportBuilder.append("                    <div id=\"warningMsg"+i+""+j+""+k+"\" class=\"issue-elements\">\n");

					for (int l = 0; l < warningLocationList.size(); l++) {
						String location= warningLocationList.get(l);
						String screenshot=getJsonStringValue(outputJson, "$.Warnings["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements["+l+"].screenShot");
						reportBuilder.append("                        <div id=\"WARN"+i+""+j+""+k+""+l+"\" hidden>"+location+"</div>\n");
						reportBuilder.append("                        <div class=\"copyPath\">Element "+(l+1)+": \n<img src=\"data:image/png;base64,"+screenshot+"\" alt=\"NON-UI Element\" class=\"hover-image\" onclick=\"copyXPath('WARN"+i+""+j+""+k+""+l+"')\">\n</div>\n");
					}
					reportBuilder.append("                    </div>\n");
				}
				reportBuilder.append("                </div>\n");
			}
			reportBuilder.append("            </div>\n");
			reportBuilder.append("        </div>\n");
		}

		if (warningList.size()>=1) {
			reportBuilder.append("    </div>\n");
		}

		if (noticeList.size()>=1) reportBuilder.append("    <div id=\"noticeTab\" class=\"tab-content\">\n");

		for (int i = 0; i < noticeList.size(); i++) {
			String issueTag=noticeList.get(i);
			List<String> noticeIssueCodeList = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes[*].issueCode");
			List<String> noticeLocationList_issueCodeLevel = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes[*].issueDetails[*].issueElements[*].location");
			reportBuilder.append("        <div class=\"issue-tag\">\n");
			reportBuilder.append("            <div class=\"issue-code\" onclick=\"toggleIssueElements(event, 'noticeTag"+i+"')\">Tag "+(i+1)+": "+issueTag+" \n<span class=\"count\">(Elements: "+noticeLocationList_issueCodeLevel.size()+", Guidelines: "+noticeIssueCodeList.size()+")</span></div>\n");
			reportBuilder.append("            <div id=\"noticeTag"+i+"\" class=\"issue-elements\">\n");

			for (int j = 0; j < noticeIssueCodeList.size(); j++) {
				String issueCode= noticeIssueCodeList.get(j);
				List<String> noticeIssueMsgList = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails[*].issueMsg");
				List<String> noticeLocationList_msgLevel = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails[*].issueElements[*].location");
				reportBuilder.append("                <div class=\"issue-code\" onclick=\"toggleIssueElements(event, 'noticeCode"+i+""+j+"')\">Guidelines "+(j+1)+": "+issueCode+" \n<span class=\"count\">(Elements: "+noticeLocationList_msgLevel.size()+", Guideline-Message: "+noticeIssueMsgList.size()+")</span></div>\n");
				reportBuilder.append("                <div id=\"noticeCode"+i+""+j+"\" class=\"issue-elements\">\n");

				for (int k = 0; k < noticeIssueMsgList.size(); k++) {
					String issueMsg=noticeIssueMsgList.get(k);
					List<String> noticeLocationList = getJsonListValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements[*].location");
					reportBuilder.append("                    <div class=\"issue-message\" onclick=\"toggleIssueElements(event, 'noticeMsg"+i+""+j+""+k+"')\">Guideline Message "+(k+1)+": "+issueMsg+" \n<span class=\"count\">(Elements: "+noticeLocationList.size()+")</span></div>\n");
					reportBuilder.append("                    <div id=\"noticeMsg"+i+""+j+""+k+"\" class=\"issue-elements\">\n");

					for (int l = 0; l < noticeLocationList.size(); l++) {
						String location=noticeLocationList.get(l);
						String screenshot=getJsonStringValue(outputJson, "$.Notices["+i+"].issueCodes["+j+"].issueDetails["+k+"].issueElements["+l+"].screenShot");
						reportBuilder.append("                        <div id=\"NOTICE"+i+""+j+""+k+""+l+"\" hidden>"+location+"</div>\n");
						reportBuilder.append("                        <div class=\"copyPath\">Element "+(l+1)+": \n<img src=\"data:image/png;base64,"+screenshot+"\" alt=\"NON-UI Element\" class=\"hover-image\" onclick=\"copyXPath('NOTICE"+i+""+j+""+k+""+l+"')\">\n</div>\n");
					}
					reportBuilder.append("                    </div>\n");
				}
				reportBuilder.append("                </div>\n");
			}
			reportBuilder.append("            </div>\n");
			reportBuilder.append("        </div>\n");
		}

		if (noticeList.size()>=1) {
			reportBuilder.append("    </div>\n");
		}

		reportBuilder.append("    <div id=\"popupMessage\">Xpath copied to clipboard!</div>\n");
		reportBuilder.append("    <div class=\"footer\">\n");
		reportBuilder.append("        <img src='https://res.cloudinary.com/dposietlk/image/upload/v1722593660/Acoe_jlouhm.png' alt='Designed and Developed by ACOE Team' height='38px' width='350px' />\n");
		reportBuilder.append("    </div>\n");
		reportBuilder.append("</body>\n");
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
	
	
	public static List<String> getJsonListValue(String jsonBody, String jsonPath) {
		List<String> value = new ArrayList<>();
		try {
			value = JsonPath.read(jsonBody, jsonPath);
		} catch (Exception e) {
		}
		return value;
	}

}
