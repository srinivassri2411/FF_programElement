package specializedAutomation_Utility;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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


public class ExecuteAs400CommandUsingTelnet implements Nlp {
	public static   String response=null;
	public static String content=null;
    public static String breakTerms="";
    @InputParams({
    	@InputParam(name = "Commands(C1###C2---)", type = "String"),
    	@InputParam(name = "Host", type = "String"),
    	@InputParam(name = "UserID", type = "String"),
    	@InputParam(name = "Password", type = "String"),
    	@InputParam(name = "Port", type = "Integer"),
    	@InputParam(name = "TimeOut", type = "String"),
    	@InputParam(name = "Title", type = "String"),
    	@InputParam(name = "ExitTerms", type = "List")
    
    })
    @ReturnType(name = "Output", type = "String")

    
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
    	content=null;
    	breakTerms="";
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String commands = (String) programElementsInput.get("Commands(C1###C2---)");
          String server = (String) programElementsInput.get("Host");
          String userName = (String) programElementsInput.get("UserID");
          String password = (String) programElementsInput.get("Password");
          Integer port = (Integer) programElementsInput.get("Port");
          String timeOut = (String) programElementsInput.get("TimeOut");
          String title = (String) programElementsInput.get("Title");
          List[] exitTermsHolder = new List[1];
          try{
        	  exitTermsHolder[0] = (List) programElementsInput.get("ExitTerms");
          }catch (Exception e) {
          exitTermsHolder[0] = new ArrayList(); 
          }
          List exitTerms = exitTermsHolder[0];

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
//                      log.error(e.getMessage());
                  }

                  telnet.disconnect();
              } catch (Exception e) {
//                 log.error( e.getMessage());
              } finally {
                  executor.shutdown();
              }
              nlpResponseModel.setStatus(CommonConstants.pass);
System.out.println(content);
              nlpResponseModel.getAttributes().put("Output", content);
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
          }

          return nlpResponseModel;
      }

                

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
            String response=null;
            StringBuilder str = new StringBuilder();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                 response = new String(buffer, 0, bytesRead);
            //    str.append(response).append("\n");
                 str.append("\n");
                if (response.contains(title)) {
                	str.append(response+"\n");
                	
                    break;
                }
            }
          //  content=str.toString();
            List<String> breakMessages = Arrays.asList(
                    "Specified option number is not allowed",
                    "Library not found",
                    "Invalid command",
                    "not found"
            );
    breakMessages.addAll(exitTerms);
   
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response = new String(buffer, 0, bytesRead);
              //  str.append(response+"\n");
           
                if (breakMessages.stream().anyMatch(response::contains)) {
                    breakTerms=  (breakMessages.stream()
                            .filter(response::contains)
                            .collect(Collectors.toList())).toString();
                    break;
                }
            }
            content=str.toString();

            return content;
  
        }
      	public static void main(String[] args) throws NlpException {
      		
      		// NlpResponseModel nlpResponseModel = new NlpResponseModel();
      		NlpRequestModel nlpRequestModel =new NlpRequestModel();
      		nlpRequestModel.getAttributes().put("Commands(C1###C2---)", "wrklib###abcd");
      		nlpRequestModel.getAttributes().put("Host", "pub400.com");
      		nlpRequestModel.getAttributes().put("UserID", "mohank");
      		nlpRequestModel.getAttributes().put("Password", "asdf1234");
      		nlpRequestModel.getAttributes().put("Port", 23);
      		nlpRequestModel.getAttributes().put("TimeOut", "5");
      		nlpRequestModel.getAttributes().put("Title", "Work with Libraries");
      		nlpRequestModel.getAttributes().put("ExitTerms", " ");

      		ExecuteAs400CommandUsingTelnet c=new ExecuteAs400CommandUsingTelnet();
      		c.execute(nlpRequestModel);
      		
    	}
	
  } 