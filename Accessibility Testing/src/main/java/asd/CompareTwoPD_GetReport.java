package asd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class CompareTwoPD_GetReport implements Nlp {
	
	@InputParams({
		@InputParam(name = "Comparision Json Data", type = "String")
	})
	
	@ReturnType(name = "Output", type = "String")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String jsonData = (String) programElementsInput.get("Comparision Json Data");
		
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String result = createHTMLReport(jsonData);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Generated Report for Comparision of Actual Audio with Golden Audio");
			nlpResponseModel.getAttributes().put("Output", result);
		}
		catch(Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Generate Report for Comparision of Actual Audio with Golden Audio, Reason for Failure ==> \n"+sw.toString());
		}

		return nlpResponseModel;
	}
	
	


	private String createHTMLReport(String output) {
		StringBuilder htmlbuilder=new StringBuilder();
		htmlbuilder.append("<!DOCTYPE html>\n");
		htmlbuilder.append("<html lang='en'>\n");
		htmlbuilder.append("  <head>\n");
		htmlbuilder.append("    <meta charset='UTF-8' />\n");
		htmlbuilder.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0' />\n");
		htmlbuilder.append("    <title>Audio Comparision Report</title>\n");
		htmlbuilder.append("    <link\n");
		htmlbuilder.append("      rel='icon'\n");
		htmlbuilder.append("      href='https://res.cloudinary.com/dposietlk/image/upload/v1722593659/favicon_itzjrc.png'\n");
		htmlbuilder.append("      type='image/png'\n");
		htmlbuilder.append("    />\n");
		htmlbuilder.append("    <link href='https://fonts.googleapis.com/icon?family=Material+Icons' rel='stylesheet'>\n");
		htmlbuilder.append("    <style>\n");
		htmlbuilder.append("      button {\n");
		htmlbuilder.append("        text-decoration: none;\n");
		htmlbuilder.append("        border: none;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      body {\n");
		htmlbuilder.append("        font-family: 'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS',\n");
		htmlbuilder.append("          sans-serif;\n");
		htmlbuilder.append("        background-image: repeating-radial-gradient(\n");
		htmlbuilder.append("          circle,\n");
		htmlbuilder.append("          rgb(160, 196, 212) 0%,\n");
		htmlbuilder.append("          rgb(211, 211, 211) 70%,\n");
		htmlbuilder.append("          rgb(160, 196, 212) 100%\n");
		htmlbuilder.append("        );\n");
		htmlbuilder.append("        margin: 0;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .audio-container {\n");
		htmlbuilder.append("        display: flex;\n");
		htmlbuilder.append("        justify-content: space-around;\n");
		htmlbuilder.append("        font-size: 20px;\n");
		htmlbuilder.append("        padding-top: 10px;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .content {\n");
		htmlbuilder.append("        color: rgb(0, 4, 255);\n");
		htmlbuilder.append("        padding: 10px;\n");
		htmlbuilder.append("        font-weight: bolder;\n");
		htmlbuilder.append("        font-size: medium;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .contentPdf {\n");
		htmlbuilder.append("        width: 100%;\n");
		htmlbuilder.append("        border-radius: 5px;        \n");
		htmlbuilder.append("        display: flex;\n");
		htmlbuilder.append("        justify-content: center;\n");
		htmlbuilder.append("        align-items: center;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .listAudio.content::-webkit-scrollbar {\n");
		htmlbuilder.append("        display: none;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .listAudio {\n");
		htmlbuilder.append("        height: 200px;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .record {\n");
		htmlbuilder.append("        font-family: 'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS',\n");
		htmlbuilder.append("          sans-serif;\n");
		htmlbuilder.append("        padding: 5px 10px;\n");
		htmlbuilder.append("        font-size: 15px;\n");
		htmlbuilder.append("        border-radius: 5px;\n");
		htmlbuilder.append("        color: white;\n");
		htmlbuilder.append("        cursor: pointer;\n");
		htmlbuilder.append("        box-shadow: 1px 1px 20px white;\n");
		htmlbuilder.append("        font-size: 20px;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .record:hover {\n");
		htmlbuilder.append("        box-shadow: 1px 1px 20px white;\n");
		htmlbuilder.append("        font-size: 20px;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .topic {\n");
		htmlbuilder.append("        width: 300px;\n");
		htmlbuilder.append("        margin: auto;\n");
		htmlbuilder.append("        text-align: center;\n");
		htmlbuilder.append("        background-color: aliceblue;\n");
		htmlbuilder.append("        border-radius: 10px;\n");
		htmlbuilder.append("        border: 1px rgb(0, 24, 88) solid;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      ol {\n");
		htmlbuilder.append("        list-style-type: none;\n");
		htmlbuilder.append("        padding: 0;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .audio-controls {\n");
		htmlbuilder.append("        display: flex;\n");
		htmlbuilder.append("        justify-content: center;\n");
		htmlbuilder.append("        align-items: center;\n");
		htmlbuilder.append("        height: 25px;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .record-list {\n");
		htmlbuilder.append("        width: 32%;\n");
		htmlbuilder.append("        font-size: small;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .record-item {\n");
		htmlbuilder.append("        cursor: pointer;\n");
		htmlbuilder.append("        display: flex;\n");
		htmlbuilder.append("        align-items: center;\n");
		htmlbuilder.append("        justify-content: center;\n");
		htmlbuilder.append("        margin: 5px 0;\n");
		htmlbuilder.append("        padding: 5px;\n");
		htmlbuilder.append("        border-radius: 5px;\n");
		htmlbuilder.append("        background-color: rgba(255, 255, 255, 0.7);\n");
		htmlbuilder.append("        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .record-item button {\n");
		htmlbuilder.append("        margin-left: 10px;\n");
		htmlbuilder.append("        padding: 5px 10px;\n");
		htmlbuilder.append("        font-size: 14px;\n");
		htmlbuilder.append("        border-radius: 3px;\n");
		htmlbuilder.append("        background-color: #4caf50;\n");
		htmlbuilder.append("        color: white;\n");
		htmlbuilder.append("        cursor: pointer;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .record-item button:hover {\n");
		htmlbuilder.append("        background-color: #45a049;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .footer {\n");
		htmlbuilder.append("        width: 100%;\n");
		htmlbuilder.append("        bottom: 0px;\n");
		htmlbuilder.append("        left: 0px;\n");
		htmlbuilder.append("        padding-top: 5px;\n");
		htmlbuilder.append("        background-color: rgba(255, 255, 255, 0.8);\n");
		htmlbuilder.append("        display: flex;\n");
		htmlbuilder.append("        justify-content: center;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .footer img {\n");
		htmlbuilder.append("        margin: auto;\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      .expandedcontent {\n");
		htmlbuilder.append("            display: none;\n");
		htmlbuilder.append("            text-align: center;\n");
		htmlbuilder.append("            padding: 10px;\n");
		htmlbuilder.append("            background-color: #fafafa;\n");
		htmlbuilder.append("            border-top: 1px solid #ccc;\n");
		htmlbuilder.append("        }\n");
		htmlbuilder.append("    </style>\n");
		htmlbuilder.append("  </head>\n");
		htmlbuilder.append("  <body>\n");
		htmlbuilder.append("    <div\n");
		htmlbuilder.append("      style='\n");
		htmlbuilder.append("        text-align: center;\n");
		htmlbuilder.append("        background-image: linear-gradient(\n");
		htmlbuilder.append("          to right,\n");
		htmlbuilder.append("          rgb(39, 65, 180) 0%,\n");
		htmlbuilder.append("          rgb(136, 141, 190) 20%,\n");
		htmlbuilder.append("          rgb(26, 39, 153) 50%,\n");
		htmlbuilder.append("          rgb(93, 85, 185) 100%\n");
		htmlbuilder.append("        );\n");
		htmlbuilder.append("        color: white;\n");
		htmlbuilder.append("        padding: 5px;\n");
		htmlbuilder.append("        display: flex;\n");
		htmlbuilder.append("        font-size: 20px;\n");
		htmlbuilder.append("      '\n");
		htmlbuilder.append("    >\n");
		htmlbuilder.append("      <img\n");
		htmlbuilder.append("        src='https://res.cloudinary.com/dposietlk/image/upload/v1722593660/fireflink-white-logo_yi0ltk.png'\n");
		htmlbuilder.append("        alt='Fireflink Logo'\n");
		htmlbuilder.append("        height='25px'\n");
		htmlbuilder.append("      />\n");
		htmlbuilder.append("      <div style='width: 80%'>HTML Report For Audio Comparison</div>\n");
		htmlbuilder.append("    </div>\n");
		htmlbuilder.append("     \n");
		htmlbuilder.append("    <div class='audio-container'>\n");
		
		String goldenWav=((List<String>)JsonPath.read(output, "$[*].goldenWav")).get(0);
		String actualWav=((List<String>)JsonPath.read(output, "$[*].actualWav")).get(0);
		String goldenPDF=((List<String>)JsonPath.read(output, "$[*].goldenPDF")).get(0);
		String actualPDF=((List<String>)JsonPath.read(output, "$[*].actualPDF")).get(0);
		
		htmlbuilder.append("      <div class='topic'>Golden Audio<div class='content'>"+goldenWav.substring(goldenWav.lastIndexOf("/")+1,goldenWav.lastIndexOf(".")).toUpperCase()+"</div></div>\n");
		htmlbuilder.append("      <div class='topic'>Actual Audio<div class='content'>"+actualWav.substring(actualWav.lastIndexOf("/")+1,actualWav.lastIndexOf(".")).toUpperCase()+"</div></div>\n");
		htmlbuilder.append("    </div>\n");
		htmlbuilder.append("    \n");
		htmlbuilder.append("    <br />\n");
		htmlbuilder.append("    <div class='contentPdf'>\n");
		htmlbuilder.append("      <iframe\n");
		htmlbuilder.append("        src='"+goldenPDF.substring(goldenPDF.lastIndexOf("/")+1)+"'\n");
		htmlbuilder.append("        frameborder='0'\n");
		htmlbuilder.append("        width='100%'\n");
		htmlbuilder.append("        height='200px'\n");
		htmlbuilder.append("      ></iframe>\n");
		htmlbuilder.append("      <div style='padding: 5px;'></div>\n");
		htmlbuilder.append("      <iframe\n");
		htmlbuilder.append("        src='"+actualPDF.substring(actualPDF.lastIndexOf("/")+1)+"'\n");
		htmlbuilder.append("        frameborder='0'\n");
		htmlbuilder.append("        width='100%'\n");
		htmlbuilder.append("        height='200px'\n");
		htmlbuilder.append("      ></iframe>\n");
		htmlbuilder.append("    </div>\n");
		htmlbuilder.append("    <br />\n");

		if(!StringUtils.startsWithAny(output.trim(),"[","{")) {
			htmlbuilder.append("    <div class='audio-controls'>");
			htmlbuilder.append("      <button class='record' style='background-color: green'>");
			htmlbuilder.append("        There is No Issues between Audios");
			htmlbuilder.append("      </button>");
			htmlbuilder.append("    </div>");
		}
		else {
			List<String> result=(List<String>)JsonPath.read(output, "$[*]");
			htmlbuilder.append("    <div class='audio-controls'>\n");
			htmlbuilder.append("      <button class='record' style='background-color: red'>\n");
			htmlbuilder.append("        There are "+(result.size()-1)+" Issues between Audios\n");
			htmlbuilder.append("      </button>\n");
			htmlbuilder.append("    </div>\n");
		} 

		htmlbuilder.append("    <div class='audio-container'>\n");
		htmlbuilder.append("      <div>\n");
		htmlbuilder.append("        <audio id='expectedAudio' controls>\n");
		htmlbuilder.append("          <source src='"+goldenWav.substring(goldenWav.lastIndexOf("/")+1)+"' type='audio/wav' />\n");
		htmlbuilder.append("          Your browser does not support the audio element.\n");
		htmlbuilder.append("        </audio>\n");
		htmlbuilder.append("      </div>\n");
		htmlbuilder.append("      <div>\n");
		htmlbuilder.append("        <audio id='actualAudio' controls>\n");
		htmlbuilder.append("          <source src='"+actualWav.substring(actualWav.lastIndexOf("/")+1)+"' type='audio/wav' />\n");
		htmlbuilder.append("          Your browser does not support the audio element.\n");
		htmlbuilder.append("        </audio>\n");
		htmlbuilder.append("      </div>\n");
		htmlbuilder.append("    </div>\n");

		List<String> insertStr=JsonPath.read(output,"$..[?(@.operation=='insert')].actual_range");
		List<String> deleteStr=JsonPath.read(output,"$..[?(@.operation=='delete')].expected_range");
		List<String> modifyExpStr=JsonPath.read(output,"$..[?(@.operation=='replace')].expected_range");
		List<String> modifyActStr=JsonPath.read(output,"$..[?(@.operation=='replace')].actual_range");

		if(StringUtils.startsWithAny(output.trim(),"[","{")) {
			htmlbuilder.append("    <div class='audio-container'>\n");
			if(modifyActStr.size()>=1) {
				htmlbuilder.append("      <button class='record' style='background-color: rgb(17, 0, 255)'>\n");
				htmlbuilder.append("        Differences ("+modifyActStr.size()+")\n");
				htmlbuilder.append("      </button>\n");
				}
			if(deleteStr.size()>=1) {
			htmlbuilder.append("      <button class='record' style='background-color: rgb(17, 0, 255)'>\n");
			htmlbuilder.append("        Deleted ("+deleteStr.size()+") \n");
			htmlbuilder.append("      </button>\n");
			}
			if(insertStr.size()>=1) {
			htmlbuilder.append("      <button class='record' style='background-color: rgb(17, 0, 255)'>\n");
			htmlbuilder.append("        Added ("+insertStr.size()+")\n");
			htmlbuilder.append("      </button>\n");
			}
			htmlbuilder.append("    </div>\n");

			htmlbuilder.append("    <div class='audio-container listAudio'>\n");
			htmlbuilder.append("      \n");
			
			
			if(modifyActStr.size()>=1) {
				htmlbuilder.append("      <div class='record-list'>\n");
				htmlbuilder.append("        <ol id='modified'>\n");

				for (int i = 0; i < modifyActStr.size(); i++) {
					List<String> expDataList=JsonPath.read(modifyExpStr,"$["+i+"][*][0]");
					List<Double> expStart=JsonPath.read(modifyExpStr,"$["+i+"][*][1]");
					List<Double> expEnd=JsonPath.read(modifyExpStr,"$["+i+"][*][2]");

					List<String> actDataList=JsonPath.read(modifyActStr,"$["+i+"][*][0]");
					List<Double> actStart=JsonPath.read(modifyActStr,"$["+i+"][*][1]");
					List<Double> actEnd=JsonPath.read(modifyActStr,"$["+i+"][*][2]");

					String expData=String.join(" ", expDataList);
					double expStartTime=0.0;
					double expEndTime=0.0;
					if(expStart.size()>=1)  expStartTime=Math.floor(expStart.get(0));
					if(expEnd.size()>=1)   expEndTime=Math.ceil(expEnd.get(expEnd.size()-1));
					
					String actData=String.join(" ", actDataList);
					double actStartTime=0.0;
					double actEndTime=0.0;
					if(actStart.size()>=1)  actStartTime=Math.floor(actStart.get(0));
					if(actEnd.size()>=1)   actEndTime=Math.ceil(actEnd.get(actEnd.size()-1));				
					
					htmlbuilder.append("          <li class='record-item'>\n");
					htmlbuilder.append("            <div>"+(i+1)+")</div>\n");
					htmlbuilder.append("            <button onclick='{playExTime("+expStartTime+")}'>Exp at "+expStartTime+"s to "+expEndTime+"s</button>\n");
					htmlbuilder.append("            <button onclick='{playAcTime("+actStartTime+")}'>Act at "+actStartTime+"s to "+actEndTime+"s</button>\n");
					htmlbuilder.append("            <div onclick='{pause()}' style='margin-left: 15px;'><i class='material-icons'>pause</i></div>\n");
					htmlbuilder.append("          </li>\n");
					htmlbuilder.append("          <div class='expandedcontent'>\n");
					htmlbuilder.append("            Golden ==> "+expData+"<br>\n");
					htmlbuilder.append("            Actual ==> "+actData+"\n");
					
					htmlbuilder.append("        </div>  \n");
				}		
				htmlbuilder.append("        </ol>\n");
				htmlbuilder.append("      </div>\n");
				}
			
			if(deleteStr.size()>=1) {
			htmlbuilder.append("      <div class='record-list'>\n");
			htmlbuilder.append("        <ol id='deleted'>\n");
			
			for (int i = 0; i < deleteStr.size(); i++) {
				List<String> dataList=JsonPath.read(deleteStr,"$["+i+"][*][0]");
				List<Double> start=JsonPath.read(deleteStr,"$["+i+"][*][1]");
				List<Double> end=JsonPath.read(deleteStr,"$["+i+"][*][2]");

				String data=String.join(" ", dataList);
				double startTime=0.0;
				double endTime=0.0;
				if(start.size()>=1)  startTime=Math.floor(start.get(0));
				if(end.size()>=1)   endTime=Math.ceil(end.get(end.size()-1));
				
				htmlbuilder.append("          <li class='record-item'>\n");
				htmlbuilder.append("            <div>"+(i+1)+") (Start="+startTime+"s, End="+endTime+"s)</div>\n");
				htmlbuilder.append("            <button onclick='{playExTime("+startTime+")}'>Play</button>\n");
				htmlbuilder.append("            <div onclick='{pause()}' style='margin-left: 15px;'><i class='material-icons'>pause</i></div>\n");
				htmlbuilder.append("          </li>\n");
				htmlbuilder.append("          <div class='expandedcontent'>\n");
				htmlbuilder.append("            Golden ==> "+data+"\n");
				htmlbuilder.append("        </div>\n");
			}
			
			htmlbuilder.append("        </ol>\n");
			htmlbuilder.append("      </div>\n");
			}
			
			
			if(insertStr.size()>=1) {
				htmlbuilder.append("      <div class='record-list'>\n");
				htmlbuilder.append("        <ol id='added'>\n");

				for (int i = 0; i < insertStr.size(); i++) {
					List<String> dataList=JsonPath.read(insertStr,"$["+i+"][*][0]");
					List<Double> start=JsonPath.read(insertStr,"$["+i+"][*][1]");
					List<Double> end=JsonPath.read(insertStr,"$["+i+"][*][2]");

					String data=String.join(" ", dataList);
					double startTime=0.0;
					double endTime=0.0;
					if(start.size()>=1)  startTime=Math.floor(start.get(0));
					if(end.size()>=1)   endTime=Math.ceil(end.get(end.size()-1));	
					
					htmlbuilder.append("          <li class='record-item'>\n");
					htmlbuilder.append("            <div>"+(i+1)+") (Start="+startTime+"s, End="+endTime+"s)</div>\n");
					htmlbuilder.append("            <button onclick='{playAcTime("+startTime+")}'>Play</button>\n");
					htmlbuilder.append("            <div onclick='{pause()}' style='margin-left: 15px;'><i class='material-icons'>pause</i></div>\n");
					htmlbuilder.append("          </li>\n");
					htmlbuilder.append("          <div class='expandedcontent'>\n");
					htmlbuilder.append("            Actual ==> "+data+"\n");
					htmlbuilder.append("        </div>\n");
				}
				
				htmlbuilder.append("        </ol>\n");
				htmlbuilder.append("      </div>\n");
				}
			
			htmlbuilder.append("\n");
			htmlbuilder.append("    </div>\n");
		}

		htmlbuilder.append("    <!-- <div class='footer'>\n");
		htmlbuilder.append("      <img\n");
		htmlbuilder.append("        src='https://res.cloudinary.com/dposietlk/image/upload/v1722593660/Acoe_jlouhm.png'\n");
		htmlbuilder.append("        alt='Designed and Developed by ACOE Team'\n");
		htmlbuilder.append("        height='40px'\n");
		htmlbuilder.append("        width='350px'\n");
		htmlbuilder.append("      />\n");
		htmlbuilder.append("    </div> -->\n");
		htmlbuilder.append("    <script>\n");
		htmlbuilder.append("      function pause() {\n");
		htmlbuilder.append("        const expectedAudio = document.getElementById('expectedAudio');\n");
		htmlbuilder.append("        const actualAudio = document.getElementById('actualAudio');\n");
		htmlbuilder.append("        actualAudio.pause();\n");
		htmlbuilder.append("        expectedAudio.pause();\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      function playExTime(sec) {\n");
		htmlbuilder.append("        const expectedAudio = document.getElementById('expectedAudio');\n");
		htmlbuilder.append("        const actualAudio = document.getElementById('actualAudio');\n");
		htmlbuilder.append("        expectedAudio.currentTime = parseFloat(sec);\n");
		htmlbuilder.append("        actualAudio.pause();\n");
		htmlbuilder.append("        expectedAudio.play();\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("      function playAcTime(sec) {\n");
		htmlbuilder.append("        const actualAudio = document.getElementById('actualAudio');\n");
		htmlbuilder.append("        const expectedAudio = document.getElementById('expectedAudio');\n");
		htmlbuilder.append("        actualAudio.currentTime = parseFloat(sec);\n");
		htmlbuilder.append("        expectedAudio.pause();\n");
		htmlbuilder.append("        actualAudio.play();\n");
		htmlbuilder.append("      }\n");
		htmlbuilder.append("const expandables = document.querySelectorAll('.record-item');\n");
		htmlbuilder.append("\n");
		htmlbuilder.append("expandables.forEach(expandable => {\n");
		htmlbuilder.append("    expandable.addEventListener('click', function() {\n");
		htmlbuilder.append("        let nextElement = this.nextElementSibling;\n");
		htmlbuilder.append("\n");
		htmlbuilder.append("        if (nextElement && nextElement.classList.contains('expandedcontent')) {\n");
		htmlbuilder.append("            if (nextElement.style.display === 'block') {\n");
		htmlbuilder.append("                nextElement.style.display = 'none';\n");
		htmlbuilder.append("            } else {\n");
		htmlbuilder.append("                nextElement.style.display = 'block';\n");
		htmlbuilder.append("            }\n");
		htmlbuilder.append("        }\n");
		htmlbuilder.append("    });\n");
		htmlbuilder.append("});\n");
		htmlbuilder.append("\n");
		htmlbuilder.append("</script>\n");
		htmlbuilder.append("  </body>\n");
		htmlbuilder.append("</html>\n");
		htmlbuilder.append("\n");

		String folderPath=new File(actualPDF).getParent();
		Path htmlFolderPath= Path.of(folderPath + File.separator );
		SimpleDateFormat sdf=new SimpleDateFormat("_dd_MM_yyyy_HH_mm_sss");
		String htmlFileName="report"+sdf.format(new Date())+".html";
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
			fileWriter.write(htmlbuilder.toString());
			System.out.println("HTML report generated successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}       

		return htmlFilePath;
	}
	
}
