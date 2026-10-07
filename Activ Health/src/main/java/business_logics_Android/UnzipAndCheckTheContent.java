package business_logics_Android;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class UnzipAndCheckTheContent implements Nlp {

    @InputParams({
        @InputParam(name = "Zip File Path", type = "java.lang.String"),
    })
    @ReturnType(name = "Extracted Folder Path", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> inputParams = nlpRequestModel.getAttributes();
        NlpResponseModel responseModel = new NlpResponseModel();

  System.out.println("Before try");
        try {
            String zipFilePath = (String) inputParams.get("Zip File Path");

            if (zipFilePath == null) {
            	responseModel.setStatus(CommonConstants.fail);
                responseModel.setMessage("Zip file path or destination folder cannot be null.");
                return responseModel;

            }

            String extractedPath = extractZipFile(zipFilePath);

            responseModel.setStatus(CommonConstants.pass);
            responseModel.setMessage("ZIP file extracted successfully.");
            responseModel.getAttributes().put("Extracted Folder Path", extractedPath);
System.out.println(extractedPath);
        } catch (IllegalArgumentException e) {
        	System.out.println(e.getMessage());

            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage(e.getMessage());
        } catch (IOException e) {
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage("Error extracting ZIP file: " + e.getMessage());
        } catch (Exception e) {
            responseModel.setStatus(CommonConstants.fail);
            responseModel.setMessage("Unexpected error: " + e.getMessage());
        }

        return responseModel;
    }

    private String extractZipFile(String zipFilePath) throws IOException {
       Path extractPath = Paths.get(zipFilePath.replace(".zip", ""));
            if (Files.exists(extractPath)) {
                deleteDirectory(extractPath);
            }
            
            // Create the folder to extract to
            Files.createDirectories(extractPath);
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outputFile = new File(extractPath.toString(), entry.getName());

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
        return zipFilePath.replace(".zip", "");
    }


   public static void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

}


