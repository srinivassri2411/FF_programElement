package parsing_utility;

  import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

 
  


public class JSON implements Nlp {
      @InputParams({@InputParam(name = "Jar File Stream", type = "java.io.InputStream"), @InputParam(name = "JSON File Stream", type = "java.io.InputStream")})
      @ReturnType(name = "string3", type = "java.lang.String")

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
          InputStream jarStream = (InputStream) attributes.get("Jar File Stream");
          InputStream inputStream = (InputStream) attributes.get("JSON File Stream");

         try {
       	  Instant now = Instant.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss").withZone(ZoneOffset.systemDefault());
          String timestamp = formatter.format(now);
          String fileName = "JSONCopy" + timestamp + ".json";
          Path filePath = Path.of(System.getProperty("user.dir").replaceAll("\\\\", "/")+"/"+fileName);
		Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);  
		jarCheck(fileName, jarStream);
		  nlpResponseModel.setStatus(CommonConstants.pass);
	       nlpResponseModel.setMessage("JSON parsed as Excel in "+getLastModifiedFile().toString());
          }
catch(Exception e) {
	  nlpResponseModel.setStatus(CommonConstants.fail);
      nlpResponseModel.setMessage("Failed to Parse JSON to Excel");
	
}
       
          try {
			nlpResponseModel.getAttributes().put("string3", getLastModifiedFile().toString());
		} catch (IOException e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
		      nlpResponseModel.setMessage("All done but failed to generate last modified path");
		}
          return nlpResponseModel;
      }
      
      public void jarCheck(String fileCreated,InputStream jarStream) throws IOException {
    		String fileName="JSON2XL";

    		 Path jarFilePath = Path.of(System.getProperty("user.dir"), fileName+".jar");
    	   boolean boo = Files.exists(jarFilePath);
    	   System.out.println( jarFilePath.getFileName());
    		System.out.println(boo);
    		if(boo==false) {
    			 // Path tempJarFile = Files.createTempFile(fileName, ".json");
    			  Files.createFile(Path.of(System.getProperty("user.dir"), fileName+".jar"));
    		}
    		else {}
    		
    	   Files.copy(jarStream, Path.of(System.getProperty("user.dir"), fileName+".jar"), StandardCopyOption.REPLACE_EXISTING);

    		ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "java -Xmx16000m -jar "+jarFilePath+" "+fileCreated).directory(new File(System.getProperty("user.dir"))).redirectErrorStream(true);
    	   
    		Process process = processBuilder.start();

    	   try {
    	   	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())) ;
    	       String line;
    	       while ((line = reader.readLine()) != null) {
    	           System.out.println(line);
    	       }
    	 

    	   // Wait for the process to finish
    	   int exitCode = process.waitFor();
    	   System.out.println("Command executed with exit code: " + exitCode);
    	} catch (Exception e) {
    	   e.printStackTrace();
    	}
    	}
      private static Path getLastModifiedFile() throws IOException {
          // Filter only regular files and not directories
          try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(System.getProperty("user.dir")), path -> Files.isRegularFile(path))) {
              Path lastModifiedFile = null;
              long lastModifiedTime = Long.MIN_VALUE;

              // Iterate over files to find the last modified file
              for (Path file : directoryStream) {
                  long currentModifiedTime = Files.getLastModifiedTime(file).toMillis();
                  if (currentModifiedTime > lastModifiedTime) {
                      lastModifiedTime = currentModifiedTime;
                      lastModifiedFile = file;
                  }
              }

              return lastModifiedFile;
          }

  } }