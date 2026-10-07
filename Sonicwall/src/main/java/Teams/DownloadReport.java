
package Teams;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DownloadReport implements Nlp {
    @InputParams({@InputParam(name = "Access Token", type = "java.lang.String"), @InputParam(name = "Execution ID", type = "java.lang.String"), @InputParam(name = "Folder Path To Download", type = "java.lang.String")})
    @ReturnType(name = "File Path", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String accessToken = (String) programElementsInput.get("Access Token");
          String executionID = (String) programElementsInput.get("Execution ID");
          String folderPathToDownload = (String) programElementsInput.get("Folder Path To Download");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  returnValue = downloadExecutionReport(accessToken,executionID,folderPathToDownload);
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
        	  nlpResponseModel.setMessage("Failed To Download\n\n" +
                      "Error Message: " + e.getMessage() + "\n\n" +
                      "Stack Trace:\n");
             nlpResponseModel.setStatus(CommonConstants.fail);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("File Path", returnValue);
          System.out.println(returnValue);
          return nlpResponseModel;
      }
    public static String downloadExecutionReport(
            String accessToken,
            String executionId,
            String saveDirectory) throws Exception {

        // Build Download URL dynamically
        String downloadUrl = "https://csod.fireflink.com/executionresult/optimize/v1/executionResponse/download/"
                + executionId + "?archiveFlag=false";

        // Create folder if not exists
        Path directoryPath = Paths.get(saveDirectory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // File name auto from execution ID
        String fullFilePath = saveDirectory + File.separator + executionId + ".pdf";

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Required Headers
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("projectid", "PJT1001");
        connection.setRequestProperty("projectname", "Sonicwall");
        connection.setRequestProperty("projecttype", "Web & Mobile");
        connection.setRequestProperty("licensetype", "ONPREM");
        connection.setRequestProperty("timezone", "Asia/Kolkata");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(fullFilePath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            connection.disconnect();
            return fullFilePath;

        } else {
            throw new RuntimeException("Download failed. HTTP Code: " + responseCode);
        }
    }
    
    /*
    public static void main(String[] args) throws NlpException {
        String accessToken = "Access Token";
       


    	NlpRequestModel nlpRequestModel = new NlpRequestModel();
  		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
  		programElementsInput.put("Access Token",accessToken );
  		programElementsInput.put("Execution ID", "EXEC17112");
  		programElementsInput.put("Folder Path To Download", "C:\\Suit_Report_FLS");
  		
  		
  		
    	
    	
  		DownloadReport PSV = new DownloadReport();
  		PSV.execute(nlpRequestModel);

  		System.out.println("NLP execution completed successfully.");
  		 
  	}*/


  } 