
package Business_Logic;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractZipFolder implements Nlp {
	@InputParams({@InputParam(name = "systemDirectory", type = "java.lang.String"),
	@InputParam(name = "fileTobeExtracted", type = "java.lang.String"),
	@InputParam(name = "requiredFolderName", type = "java.lang.String"), 
	@InputParam(name = "fileName", type = "java.lang.String")})
    @ReturnType(name = "filePath", type = "java.lang.String")
	public static void extractZipFile(String fileTobeExtracted, String baseFolderPath) throws FileNotFoundException, IOException {
        File destFolder = new File(baseFolderPath);
        if (!destFolder.exists()) {
            destFolder.mkdirs();  
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileTobeExtracted))) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outputFile = new File(baseFolderPath, entry.getName());

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipInputStream.closeEntry(); 
            }
        }
    }
	
	public static String getFilePath(String requiredFolderName, String fileName, String baseFolderPath) {
        File folder = new File(baseFolderPath, requiredFolderName);
        File targetFile = new File(folder, fileName);

        if (targetFile.exists() && targetFile.isFile()) {
            return targetFile.getAbsolutePath();
        } else {
            return null;
        }
    }
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String systemDirectory = (String) programElementsInput.get("systemDirectory");
          String fileTobeExtracted = (String) programElementsInput.get("fileTobeExtracted");
          String requiredFolderName=(String)programElementsInput.get("requiredFolderName");
          String fileName=(String)programElementsInput.get("fileName");
          String returnValue=null;
          

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          
          
          
          

          try {
              //System.out.println("Extracting ZIP file...");
        	  String baseFolderPath = systemDirectory + "\\Calculators";
              extractZipFile(fileTobeExtracted, baseFolderPath);
             

              returnValue = getFilePath(requiredFolderName, fileName, baseFolderPath);

              if (returnValue != null) {
                 
                  nlpResponseModel.setMessage("File not found in the specified folder." +returnValue);
                  nlpResponseModel.setStatus(CommonConstants.pass);
              } else {
                 
                  nlpResponseModel.setMessage("File not found in the specified folder.");
              }
              
              
              

          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
        	  nlpResponseModel.setMessage("File not found in the specified folder.");
             nlpResponseModel.setStatus(CommonConstants.fail);

             //Uncomment the below line and modify the message, only if you want to override the fail message defined for the program element.
             //nlpResponseModel.setMessage("Failed to add numbers");
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("fileName", returnValue);
          return nlpResponseModel;
      }

}