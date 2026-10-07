
package customLogic;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZipTheFolder implements Nlp {
    @InputParams({@InputParam(name = "zipFolderPath", type = "java.lang.String")})
    @ReturnType(name = "unZipedFolderPath", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String zipFolderPath = (String) programElementsInput.get("zipFolderPath");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              // Your program element business logic starts here ...
        	  returnValue=unzipAndGetFolderPath(zipFolderPath);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("unziped Folder path is "+returnValue);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Folder path "+returnValue+" is Invalid");
             throw new NlpException(e+"");
             

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("unZipedFolderPath", returnValue);
          return nlpResponseModel;
      }

    public static String unzipAndGetFolderPath(String zipFilePath) throws Exception {

        File zipFile = new File(zipFilePath);

        /*
         * Wait until the ZIP file is actually available.
         * This is useful when the ZIP was just downloaded/generated.
         */
        int maxAttempts = 30;

        for (int i = 0; i < maxAttempts; i++) {

            if (zipFile.exists() && zipFile.isFile() && zipFile.length() > 0) {
                break;
            }

            Thread.sleep(1000);
        }

        // Final validation
        if (!zipFile.exists()) {
            throw new IOException(
                    "ZIP file does not exist: " + zipFile.getAbsolutePath()
            );
        }

        if (!zipFile.isFile()) {
            throw new IOException(
                    "Path is not a file: " + zipFile.getAbsolutePath()
            );
        }

        /*
         * Example:
         *
         * C:\Users\User\AuthorIt\opi (11627).zip
         *
         * becomes:
         *
         * C:\Users\User\AuthorIt\opi (11627)
         */
        String folderName = zipFile.getName()
                .replaceFirst("(?i)\\.zip$", "");

        File extractFolder = new File(
                zipFile.getParentFile(),
                folderName
        );

        if (!extractFolder.exists()) {
            extractFolder.mkdirs();
        }

        /*
         * Unzip
         */
        try (ZipInputStream zis =
                     new ZipInputStream(new FileInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                File outputFile = new File(
                        extractFolder,
                        entry.getName()
                );

                /*
                 * Prevent Zip Slip vulnerability
                 */
                String canonicalOutputPath =
                        outputFile.getCanonicalPath();

                String canonicalExtractPath =
                        extractFolder.getCanonicalPath();

                if (!canonicalOutputPath.startsWith(
                        canonicalExtractPath + File.separator)) {

                    throw new IOException(
                            "Invalid ZIP entry: " + entry.getName()
                    );
                }

                if (entry.isDirectory()) {

                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }

                } else {

                    File parent = outputFile.getParentFile();

                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fos =
                                 new FileOutputStream(outputFile)) {

                        byte[] buffer = new byte[8192];
                        int length;

                        while ((length = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }

                zis.closeEntry();
            }
        }

        /*
         * Case 1:
         *
         * C:\Users\User\AuthorIt\opi (11627)
         *
         *
         * Case 2:
         *
         * C:\Users\User\AuthorIt\opi (11627)
         *     └── opi (11627)
         *
         * Return the nested folder when it exists.
         */
        File nestedFolder = new File(
                extractFolder,
                folderName
        );

        if (nestedFolder.exists() && nestedFolder.isDirectory()) {
            return nestedFolder.getAbsolutePath();
        }

        return extractFolder.getAbsolutePath();
    }
  } 