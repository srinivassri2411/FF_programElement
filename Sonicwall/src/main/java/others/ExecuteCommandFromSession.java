
package others;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import net.schmizz.sshj.connection.channel.direct.Session;

public class ExecuteCommandFromSession implements Nlp {
    @InputParams({@InputParam(name = "CMD Session", type = "net.schmizz.sshj.connection.channel.direct.Session"), 
    	@InputParam(name = "Command", type = "java.lang.String"),
    	@InputParam(name = "Exit Criteria", type = "java.lang.String"),
    	@InputParam(name = "Time in Seconds", type = "java.lang.Integer")	
    })
//    @ReturnType(name = "OutPut", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Session session = (Session) programElementsInput.get("CMD Session");
          String command = (String) programElementsInput.get("Command");
          String completionMarker = (String) programElementsInput.get("Exit Criteria");
          Integer timeoutSeconds = (Integer) programElementsInput.get("Time in Seconds");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
//        	  Session session = sSHConnection.startSession();
        	  Session.Shell shell = session.startShell();
        	  InputStream in = shell.getInputStream();
              OutputStream out = shell.getOutputStream();
              BufferedReader reader = new BufferedReader(new InputStreamReader(in));
              BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));


//              int timeoutSeconds = 15; // timeout for simple commands

              // Step 1: Send each line separately with flush
              for (String line : command.split("\n")) {
                  line = line.trim();
                  if (!line.isEmpty()) {
                      writer.write(line + "\n");
                      writer.flush();
                      System.out.println(">>> Sent: " + line);
                      Thread.sleep(300);
                  }
              }

              // Step 2: Start thread to read response
              Thread printThread = new Thread(() -> {
                  try {
                      String line;
                      long startTime = System.currentTimeMillis();

                      while ((System.currentTimeMillis() - startTime) < timeoutSeconds * 1000L
                             && (line = reader.readLine()) != null) {

                          System.out.println(line);

                          if (line.contains("Press ENTER to acknowledge")) {
                              writer.write("\n");
                              writer.flush();
                              System.out.println(">>> Sent ENTER");
                          } else if (line.contains("Do you wish to overwrite")) {
                              writer.write("y\n");
                              writer.flush();
                              System.out.println(">>> Sent 'y'");
                          } else if (!completionMarker.isEmpty() && line.contains(completionMarker)) {
                              writer.write("exit\n");
                              writer.flush();
                              System.out.println(">>> Completion marker found, exiting.");
                              break;
                          }
                      }
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              });

              printThread.start();
              writer.write("echo '>>> Simulating ENTER key press'\n");
              writer.write("\n"); // Simulate ENTER key
              writer.flush();
              System.out.println(">>> Sent ENTER");

              Thread.sleep(2000); // Wait for script to reach y/n prompt
              writer.write("echo '>>> Simulating y key press'\n");
              writer.write("y\n"); // Simulate 'y'
              writer.flush();
              System.out.println(">>> Sent 'y'");
              printThread.join(timeoutSeconds * 1000L);
//        	  shell.close();
        	  nlpResponseModel.setMessage(command+" executed successfully");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
          	 // Your program element Exception handling goes here ...
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Unable to execute command - Reason: "+e);
          }

          // Your program element business logic ends here ...
//          nlpResponseModel.getAttributes().put("OutPut", returnValue);
          return nlpResponseModel;
      }
    
    private static String readInputStream(InputStream inputStream) throws IOException {
	    StringBuilder result = new StringBuilder();
	    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            result.append(line).append("\n");
	        }
	    }
	    return result.toString();
	}

  } 