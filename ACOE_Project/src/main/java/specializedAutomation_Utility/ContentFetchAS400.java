package specializedAutomation_Utility;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.net.telnet.TelnetClient;

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

public class ContentFetchAS400 implements Nlp {
    @InputParams({@InputParam(name = "TimeOut", type = "java.lang.String"),
    	@InputParam(name = "Server", type = "java.lang.String"),
    	@InputParam(name = "Username", type = "java.lang.String"),
    	@InputParam(name = "Password", type = "java.lang.String"),
    	@InputParam(name = "Commands", type = "java.lang.String"),
    	@InputParam(name = "Port", type = "java.lang.Integer"),
    	@InputParam(name = "Title", type = "java.lang.String"),
    @InputParam(name = "ExitTerms", type = "java.util.List")})
    @ReturnType(name = "content", type = "java.lang.String")
    
    

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> attributes = nlpRequestModel.getAttributes();
        String timeOut = (String) attributes.get("TimeOut");
        String server = (String) attributes.get("Server");
        String userName = (String) attributes.get("Username");
        String password = (String) attributes.get("Password");
        String commands = (String) attributes.get("Commands");
        Integer port = (Integer) attributes.get("Port");
        String title = (String) attributes.get("Title");
        List exitTerms = (List) attributes.get("ExitTerms");


        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            TelnetClient telnet = new TelnetClient();
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                telnet.setConnectTimeout(5000);
                telnet.connect(server, port);
                InputStream inputStream = telnet.getInputStream();
                OutputStream outputStream = telnet.getOutputStream();
                PrintWriter out = new PrintWriter(outputStream, true);
                Callable<Void> readTask = () -> {
                    readMessages(inputStream,title,exitTerms);
                    return null;
                };
                Future<Void> readFuture = executor.submit(readTask);
                Runnable commandTask = () -> {
                    sendCommands(out, userName, password, commands);
                };
                executor.submit(commandTask);
                try {
                    readFuture.get(Long.parseLong(timeOut), TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    readFuture.cancel(true);
                } catch (InterruptedException | ExecutionException e) {
                    //log.error(e.getMessage());
                }

                telnet.disconnect();
            } catch (Exception e) {
               //log.error( e.getMessage());
            } finally {
                executor.shutdown();
            }
           nlpResponseModel.getAttributes().put("content",content);
          nlpResponseModel.setMessage("Content fetched after considering break terms: "+breakTerms);
            nlpResponseModel.setStatus(CommonConstants.pass);
         //   setExecutionResponse("Content fetched after considering break terms: "+breakTerms,nlpResponseModel, pass, nlpRequestModel.getPassMessage().replace("*breakTerms*",breakTerms));
        } catch (Exception e) {
        	  StringWriter sw = new StringWriter();
    			e.printStackTrace(new PrintWriter(sw));
    			String exceptionAsString = sw.toString();
    			////log.info(exceptionAsString);
    			nlpResponseModel.setStatus(CommonConstants.fail);
          	  nlpResponseModel.setMessage("Failed : "+exceptionAsString);
        }
        return nlpResponseModel;
    }
  static  String content=null;
    static String breakTerms="";
    private static void sendCommands(PrintWriter out, String userName, String password, String commands) {
        out.println(userName + "\t" + password);
        out.flush();

        String[] commandsArr = commands.split("###");
        for (String cmd : commandsArr) {
            out.println(cmd);
            out.flush();
        }
    }

    private static String readMessages(InputStream inputStream,String title,List exitTerms) throws Exception {
        byte[] buffer = new byte[300];
        int bytesRead;

        List<String> breakMessages = Arrays.asList(
                "Specified option number is not allowed",
                "Library not found",
                "Invalid command",
                "not found"
              
        );
breakMessages.addAll(exitTerms);

        StringBuilder str = new StringBuilder();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            String response = new String(buffer, 0, bytesRead);
            str.append(response).append("\n");
            if (response.contains(title)) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response = new String(buffer, 0, bytesRead);
                    str.append(response);
                    content=str.toString();
                    if (breakMessages.stream().anyMatch(response::contains)) {
                        breakTerms=  (breakMessages.stream()
                                .filter(response::contains)
                                .collect(Collectors.toList())).toString();
                        break;
                    }
                }
                break;
            }
        }
        return content;
    }


}



