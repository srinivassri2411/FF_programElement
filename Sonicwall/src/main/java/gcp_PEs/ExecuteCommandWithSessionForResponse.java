package gcp_PEs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import net.schmizz.sshj.connection.channel.direct.Session;

public class ExecuteCommandWithSessionForResponse implements Nlp {
    @InputParams({@InputParam(name = "CMD Session", type = "net.schmizz.sshj.connection.channel.direct.Session"), 
        @InputParam(name = "Command", type = "java.lang.String"),
        @InputParam(name = "Time in Seconds", type = "java.lang.Integer")    
    })
    @ReturnType(name = "OutPut", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Session session = (Session) programElementsInput.get("CMD Session");
          String command = (String) programElementsInput.get("Command");
          Integer timeoutSeconds = (Integer) programElementsInput.get("Time in Seconds");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
//              Session session = sSHConnection.startSession();
    returnValue=runCommandViaShell(session, command, timeoutSeconds);
              nlpResponseModel.setMessage(command+" executed successfully ");
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
        	  StringWriter sw = new StringWriter();
    			e.printStackTrace(new PrintWriter(sw));
    			String exceptionAsString = sw.toString();
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Unable to execute command - Reason: "+exceptionAsString);
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("OutPut", returnValue);
          return nlpResponseModel;
      }
    
	  public static String runCommandViaShell(Session session, String command, int timeoutSeconds)
	            throws IOException, InterruptedException {

	        // Generate a unique marker every time
	        final String marker = "CMD_DONE_" + UUID.randomUUID().toString();

	        Session.Shell shell = session.startShell();

	        InputStream in = shell.getInputStream();
	        OutputStream out = shell.getOutputStream();

	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

	        StringBuilder outputBuffer = new StringBuilder();

	        Thread readerThread = new Thread(() -> {
	            try {
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    outputBuffer.append(line).append("\n");
	                    if (line.contains(marker)) {
	                        break;
	                    }
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        });
	        readerThread.start();

	        writer.write(command + "\n");
	        writer.write("echo " + marker + "\n");
	        writer.flush();

	        readerThread.join(timeoutSeconds * 1000L);
	        shell.close();

	        String fullOutput = outputBuffer.toString();
	        fullOutput = fullOutput.replaceAll("(?m)^echo .*", "");
	        fullOutput = fullOutput.replace(marker, "").trim();

	        return fullOutput;
	    }

  }