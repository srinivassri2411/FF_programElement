package pdf_Accessibility;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import lombok.extern.slf4j.Slf4j;


@Slf4j

public class CheckAccessibilityOfPDFFile implements Nlp {
	@InputParams({ @InputParam(name = "File Input Stream", type = "java.io.InputStream"),
			@InputParam(name = "File Name with Extension", type = "java.lang.String"),
			@InputParam(name = "ProfileName", type = "java.lang.String")})
	@ReturnType(name = "HTML File Path", type = "java.lang.String")
	
	
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
	
	
	private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY = "-----Boundary" + System.currentTimeMillis();
	
	

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String pdfFileName="";
		try {
			InputStream inputStream = (InputStream) attributes.get("File Input Stream");
			String fileName = (String) attributes.get("File Name with Extension");
			String profileName=(String) attributes.get("ProfileName");
            String requestURL = "http://49.249.29.5:8187/"+profileName;
            File file = convertInputStreamToFile(inputStream, fileName);
            String response=sendMultipartPostRequest(requestURL, file);
            String folderPath=System.getProperty("user.home");
           log.info(folderPath);
            Path htmlFolderPath= Path.of(folderPath + File.separator + "HTML Reports");
            SimpleDateFormat sdf=new SimpleDateFormat("_dd_MM_yyyy_HH_mm_sss");
            String htmlFileName="report"+sdf.format(new Date())+".html";
            pdfFileName=htmlFolderPath+File.separator+htmlFileName;
            try {
                if (!Files.exists(htmlFolderPath)) {
                    Files.createDirectories(htmlFolderPath);
                   log.info("Directory created successfully: " + htmlFolderPath.toAbsolutePath());
                } else {
                   log.info("Directory already exists: " + htmlFolderPath.toAbsolutePath());
                }
            } catch (IOException e) {
               log.info("Failed to create directory: " + e.getMessage());
            }
            
            String html=generateHtml(response);
            String htmlContent = generateHtml(response);
            try (FileWriter fileWriter = new FileWriter(pdfFileName)) {
                fileWriter.write(htmlContent);
               log.info("HTML report generated successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }

          	nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Report generated successfully in: "+pdfFileName);
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to check PDF Accessibility because " + e);

		}
		
		nlpResponseModel.getAttributes().put("HTML File Path", pdfFileName);
		return nlpResponseModel;
	}
	
    private static String generateHtml(String response) throws ParseException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        StringBuilder htmlContent = new StringBuilder();

            JSONObject reportJson = (JSONObject) parser.parse(response);
            JSONObject report = (JSONObject) reportJson.get("report");
            JSONArray reports = (JSONArray) report.get("reports");
            JSONObject array= (JSONObject) reports.get(0);
            JSONObject itemDetails=(JSONObject) array.get("itemDetails");
            String fileName= itemDetails.get("name").toString();
            String fileSize= itemDetails.get("size").toString();
            JSONObject validationResult=(JSONObject) array.get("validationResult");
            String profileName=validationResult.get("profileName").toString();
            String statement=validationResult.get("statement").toString();
            String compliant=validationResult.get("compliant").toString();
            JSONObject details=(JSONObject) validationResult.get("details");
            String passedRules=details.get("passedRules").toString();
            String failedRules=details.get("failedRules").toString();
            String passedChecks=details.get("passedChecks").toString();
            String failedChecks=details.get("failedChecks").toString();
            JSONArray tags=(JSONArray) details.get("tags");

            JSONArray ruleSummaries=(JSONArray) details.get("ruleSummaries");
            if(ruleSummaries!=null){

                for (Object ruleSummarie: ruleSummaries){
                    JSONObject ruleSum= (JSONObject) ruleSummarie;
                    String ruleStatus=ruleSum.get("ruleStatus").toString();
                    String specification=ruleSum.get("specification").toString();
                    String clause=ruleSum.get("clause").toString();
                    String testNumber=ruleSum.get("testNumber").toString();
                    String status=ruleSum.get("status").toString();
                    String description=ruleSum.get("description").toString();
                    String object=ruleSum.get("object").toString();
                    JSONArray tag=(JSONArray) ruleSum.get("tags");
                }
            }
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html lang=\"en\">\n");
        htmlContent.append("<head>\n");
        htmlContent.append("    <meta charset=\"UTF-8\">\n");
        htmlContent.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        htmlContent.append("    <title>PDF Validation Report</title>\n");
        htmlContent.append("    <style>\n");
        htmlContent.append("        body { font-family: Arial, sans-serif; }\n");
        htmlContent.append("        .container { margin: 20px; }\n");
        htmlContent.append("        h1, h2, h3 { color: #2c3e50; }\n");
        htmlContent.append("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        htmlContent.append("        table, th, td { border: 1px solid #ddd; }\n");
        htmlContent.append("        th, td { padding: 8px; text-align: left; }\n");
        htmlContent.append("        th { background-color: #f2f2f2; }\n");
        htmlContent.append("        .tag { background-color: #e7f3fe; border: 1px solid #d6e9c6; border-radius: 4px; display: inline-block; padding: 2px 4px; margin-right: 4px; }\n");
        htmlContent.append("        .failed { color: red; }\n");
        htmlContent.append("        .passed { color: green; }\n");
        htmlContent.append("    </style>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");
        htmlContent.append("<div class=\"container\">\n" +
                "<svg id=\"Layer_1\" data-name=\"Layer 1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 8000 500\">\n" +
                "  <defs>\n" +
                "    <style>\n" +
                "      .cls-1 {\n" +
                "        fill: #db7221;\n" +
                "      }\n" +
                "\n" +
                "      .cls-1, .cls-2 {\n" +
                "        stroke-width: 0px;\n" +
                "      }\n" +
                "\n" +
                "      .cls-2 {\n" +
                "        fill: #434db7;\n" +
                "      }\n" +
                "    </style>\n" +
                "  </defs>\n" +
                "  <g>\n" +
                "    <g id=\"Group_7277\" data-name=\"Group 7277\">\n" +
                "      <g id=\"Path_529\" data-name=\"Path 529\">\n" +
                "        <path class=\"cls-2\" d=\"m509.59,167.44v38.86h77.74v38.86h-77.74v60.67h-50.24v-177.25h138.41v38.86h-88.19.03Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_530\" data-name=\"Path 530\">\n" +
                "        <path class=\"cls-2\" d=\"m624.3,128.58h50.24v176.39h-50.24v-176.39Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_531\" data-name=\"Path 531\">\n" +
                "        <path class=\"cls-2\" d=\"m788.29,258.44h-27.49v46.52h-50.24v-176.39h80.58c48.35,0,78.67,24.65,78.67,65.4,1.02,23.86-12.46,45.97-34.13,56l37.92,54.99h-53.08l-32.23-46.52Zm.95-90.99h-27.49v51.19h27.48c20.86,0,31.29-9.48,31.29-25.59-.95-15.19-10.43-25.62-31.29-25.62h.01Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_532\" data-name=\"Path 532\">\n" +
                "        <path class=\"cls-2\" d=\"m1041.39,266.03v38.93h-141.25v-176.39h138.41v38.86h-89.12v29.4h78.67v36.97h-78.67v32.2l91.96.03Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_533\" data-name=\"Path 533\">\n" +
                "        <path class=\"cls-2\" d=\"m1121.02,167.44v38.86h77.74v38.86h-77.74v60.67h-50.24v-177.25h138.41v38.86h-88.19.03Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_534\" data-name=\"Path 534\">\n" +
                "        <path class=\"cls-2\" d=\"m1234.77,128.58h50.24v136.5h84.37v39.88h-133.66v-176.39h-.95Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_535\" data-name=\"Path 535\">\n" +
                "        <path class=\"cls-2\" d=\"m1391.18,128.58h50.24v176.39h-50.24v-176.39Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_536\" data-name=\"Path 536\">\n" +
                "        <path class=\"cls-2\" d=\"m1646.18,128.58v176.39h-40.76l-77.74-93.79v93.79h-49.29v-176.39h40.76l77.74,93.79v-93.79h49.29Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_537\" data-name=\"Path 537\">\n" +
                "        <path class=\"cls-2\" d=\"m1751.41,241.38l-18.95,19.91v44.56h-49.3v-177.27h49.29v72.99l68.3-72.99h54.99l-72.04,77.74,75.85,98.65h-57.83l-50.3-63.58Z\"/>\n" +
                "      </g>\n" +
                "    </g>\n" +
                "    <g id=\"Logo\">\n" +
                "      <g id=\"Path_12\" data-name=\"Path 12\">\n" +
                "        <path class=\"cls-2\" d=\"m167.77,67.44c-4.75,2.8-8.53,4.75-12.32,6.64-14.22,7.59-27.49,14.22-41.7,21.8-1.99,1.16-3.09,3.4-2.8,5.68v287.23c0,3.79-.95,6.64-4.73,7.59-14.29,5.91-30.55,4.49-43.61-3.79-9.54-6.71-16.02-16.94-18-28.43-2.35-15.37-3.28-30.91-2.8-46.45-.95-75.83-1.89-152.59-1.89-228.46-.74-12.06,1.54-24.12,6.64-35.08,8.87-18.91,30.18-28.56,50.24-22.75,13.48,4.06,26.26,10.13,37.92,18.02,10.43,5.68,20.86,11.38,30.34,17.06.83-.94,1.78.01,2.72.95Z\"/>\n" +
                "      </g>\n" +
                "      <g id=\"Path_13\" data-name=\"Path 13\">\n" +
                "        <path class=\"cls-2\" d=\"m169.67,359.42v-82.52c-.07-2.24.98-4.37,2.8-5.68,32.2-18.02,64.39-36.02,95.74-54.04,8.53-4.75,17.06-9.48,26.6-15.16-5.48-3.52-11.18-6.68-17.06-9.48-14.22-8.53-28.43-16.11-42.65-24.65-1.43-.98-3.32-.98-4.75,0-18.02,11.38-35.07,21.8-53.08,33.18-.95,0-.95.95-1.89.95v-60.67c-.08-2.51.94-4.94,2.8-6.64,16.11-11.38,32.2-21.8,47.4-33.18,1.78-.95,3.91-.95,5.68,0,21.8,12.32,44.56,25.59,66.35,37.92,15.16,8.53,30.34,17.06,46.45,25.59,11.54,6.02,21.1,15.26,27.49,26.6,8.28,15.1,5.99,33.8-5.68,46.45-7.51,8.53-16.52,15.6-26.6,20.86-21.8,12.32-42.65,25.59-64.39,37.92-27.49,16.11-54.04,31.29-81.52,47.4-8.51,5.66-16.1,10.4-23.68,15.15Z\"/>\n" +
                "      </g>\n" +
                "    </g>\n" +
                "  </g>\n" +
                "</svg>\n" +
                "  </div>");
        htmlContent.append("<div class=\"container\">\n");
        htmlContent.append("    <h1>PDF Accessibility Report</h1>\n");
        htmlContent.append("<h2> Profile Details</h2>");
        htmlContent.append("<table>\n");
        htmlContent.append("        <tr><th>Profile name</th><td>").append(profileName).append("</td></tr>\n");
        htmlContent.append("        <tr><th>Statement</th><td>").append(statement).append("</td></tr>\n");
        htmlContent.append("        <tr><th>Compliant</th><td>").append(compliant.toUpperCase()).append("</td></tr>\n");
        htmlContent.append("    </table>\n");
        htmlContent.append("    <h2>Item Details</h2>\n");
        htmlContent.append("    <table>\n");
        htmlContent.append("        <tr><th>File name</th><td>").append(fileName).append("</td></tr>\n");
        htmlContent.append("        <tr><th>File size</th><td>").append(fileSize).append(" bytes</td></tr>\n");
        htmlContent.append("    </table>\n");
        htmlContent.append("    <h2>Validation Result</h2>\n");
        htmlContent.append("    <table>\n");
        htmlContent.append("        <tr><th>Passed Rules</th><td>").append(passedRules).append("</td></tr>\n");
        htmlContent.append("        <tr><th>Failed Rules</th><td>").append(failedRules).append("</td></tr>\n");
        htmlContent.append("        <tr><th>Passed Checks</th><td>").append(passedChecks).append("</td></tr>\n");
        htmlContent.append("        <tr><th>Failed Checks</th><td>").append(failedChecks).append("</td></tr>\n");
        if(tags!=null) {
        htmlContent.append("        <tr><th>Tags</th><td>");
            for (Object tag : tags) {
                htmlContent.append("<span class=\"tag\">").append(tag.toString().toUpperCase()).append("</span>");
            }
        }
        htmlContent.append("        </td></tr>\n");
        htmlContent.append("    </table>\n");
        htmlContent.append("    <h3>Rule Summaries</h3>\n");
        htmlContent.append("    <table>\n");
        htmlContent.append("        <thead>\n");
        htmlContent.append("            <tr>\n");
        htmlContent.append("                <th>Rule Status</th>\n");
        htmlContent.append("                <th>Specification</th>\n");
        htmlContent.append("                <th>Clause</th>\n");
        htmlContent.append("                <th>Test Number</th>\n");
        htmlContent.append("                <th>Failed Checks</th>\n");
        htmlContent.append("                <th>Tags</th>\n");
        htmlContent.append("                <th>Description</th>\n");
        htmlContent.append("            </tr>\n");
        htmlContent.append("        </thead>\n");
        htmlContent.append("        <tbody>\n");


        for (Object obj : ruleSummaries) {
            JSONObject ruleSummary = (JSONObject) obj;
            String ruleStatus = (ruleSummary.get("ruleStatus") != null) ? (String) ruleSummary.get("ruleStatus") : "";
            String specification = (ruleSummary.get("specification") != null) ? (String) ruleSummary.get("specification") : "";
            String clause = (ruleSummary.get("clause") != null) ? (String) ruleSummary.get("clause") : "";
            int testNumber = (ruleSummary.get("testNumber") != null) ? ((Long) ruleSummary.get("testNumber")).intValue() : 0;
            int failedChecksCount = (ruleSummary.get("failedChecks") != null) ? ((Long) ruleSummary.get("failedChecks")).intValue() : 0;
            JSONArray ruleTagsArray = (ruleSummary.get("tags") != null) ? (JSONArray) ruleSummary.get("tags") : new JSONArray();
            String[] ruleTags = new String[ruleTagsArray.size()];
            for (int i = 0; i < ruleTagsArray.size(); i++) {
                ruleTags[i] = (String) ruleTagsArray.get(i);
            }
            String description = (ruleSummary.get("description") != null) ? (String) ruleSummary.get("description") : "";

            htmlContent.append("            <tr>\n");
            htmlContent.append("                <td class=\"").append(ruleStatus.equals("passed") ? "passed" : "failed").append("\">").append(ruleStatus).append("</td>\n");
            htmlContent.append("                <td>").append(specification).append("</td>\n");
            htmlContent.append("                <td>").append(clause).append("</td>\n");
            htmlContent.append("                <td>").append(testNumber).append("</td>\n");
            htmlContent.append("                <td>").append(failedChecksCount).append("</td>\n");
            htmlContent.append("                <td>");
            for (String tag : ruleTags) {
                htmlContent.append("<span class=\"tag\">").append(tag).append("</span>");
            }
            htmlContent.append("</td>\n");
            htmlContent.append("                <td>").append(description).append("</td>\n");
            htmlContent.append("            </tr>\n");
        }

        htmlContent.append("        </tbody>\n");
        htmlContent.append("    </table>\n");



        return htmlContent.toString();
    }


    private static File convertInputStreamToFile(InputStream inputStream, String fileName) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
       log.info("File Path"+tempFile.getAbsolutePath());
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private static String sendMultipartPostRequest(String requestURL, File file) {
        String response = "";
        try {
            URL url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            try (OutputStream outputStream = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

                writer.append("--").append(BOUNDARY).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(LINE_FEED);
                writer.append("Content-Type: application/pdf").append(LINE_FEED);
                writer.append(LINE_FEED).flush();
                Files.copy(file.toPath(), outputStream);
                outputStream.flush();
                writer.append(LINE_FEED).flush();

                writer.append("--").append(BOUNDARY).append("--").append(LINE_FEED);
                writer.close();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder responseStringBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseStringBuilder.append(line).append("\n");
                    }
                }
                response = responseStringBuilder.toString();
            } else {
                response = "Error: " + responseCode;
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }	

}