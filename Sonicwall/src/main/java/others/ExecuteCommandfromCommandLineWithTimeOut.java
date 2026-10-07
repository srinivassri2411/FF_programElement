
package others;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ExecuteCommandfromCommandLineWithTimeOut implements Nlp {
    @InputParams({@InputParam(name = "LaunchDirectory", type = "java.lang.String"), 
    	@InputParam(name = "Command", type = "java.lang.String"),
    	@InputParam(name = "Seconds", type = "java.lang.Integer")})
    @ReturnType(name = "Sum of Two numbers", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String LaunchDirectory = (String) programElementsInput.get("LaunchDirectory");
          String Command = (String) programElementsInput.get("Command");

          Integer Seconds = (Integer) programElementsInput.get("Seconds");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  StringBuilder buil=  command(LaunchDirectory, Command, Seconds, nlpResponseModel);
              returnValue=buil.toString();
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Execution Failed");
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Sum of Two numbers", returnValue);
          return nlpResponseModel;
      }
    

    public static StringBuilder command(String LaunchDirectory ,String Command ,int Seconds , NlpResponseModel nlpResponseModel) {
        StringBuilder returnType=new StringBuilder();

    	try {
                
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", Command);
            pb.directory(new File(LaunchDirectory));
            pb.redirectErrorStream(true);  // merge stderr with stdout
            
            Process process = pb.start();
            // Thread to read output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        returnType.append(line);
                        returnType.append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
            boolean finished = process.waitFor(Seconds, TimeUnit.SECONDS);

            if (finished) {
                System.out.println("Exited with code: " + process.exitValue());
                nlpResponseModel.setMessage("Successfully Executed Command with code: " + process.exitValue());
                nlpResponseModel.setStatus(CommonConstants.pass);

            } else {

                Runtime.getRuntime().exec("taskkill /F /T /PID " + process.pid());
                System.out.println("Timeout — killing entire process tree...");
                nlpResponseModel.setMessage("Timeout — killing entire process tree...");
                nlpResponseModel.setStatus(CommonConstants.pass);

            }

        } catch (Exception e) {
            e.printStackTrace();
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Timeout With Exception" +e);

        }
        
        return returnType;
    }

  } 