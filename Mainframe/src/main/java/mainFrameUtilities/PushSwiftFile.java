package mainFrameUtilities;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.tn5250j.Session5250;
import org.tn5250j.framework.common.SessionManager;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class PushSwiftFile implements Nlp {
    @InputParams({@InputParam(name = "Text File Path", type = "java.lang.String"),
    	@InputParam(name = "Host", type = "java.lang.String"),
    	@InputParam(name = "UserName", type = "java.lang.String"),
    	@InputParam(name = "Password", type = "java.lang.String")
    
    })
//    @ReturnType(name = "session", type = "org.tn5250j.Session5250")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String filePath = (String) programElementsInput.get("Text File Path");
          String host = (String) programElementsInput.get("Host");
          String userName = (String) programElementsInput.get("UserName");
          String password = (String) programElementsInput.get("Password");
//          String port = (String) programElementsInput.get("Session Port");
    
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
          
             uploadTransactions(host, userName, password, filePath);

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Swift transaction Pushed successfully from "+filePath);

          }
          catch(Exception e) {
        	  
        	     e.printStackTrace();
                 StringWriter sw = new StringWriter();
                 e.printStackTrace(new PrintWriter(sw));

                 nlpResponseModel.setStatus(CommonConstants.fail);
                 nlpResponseModel.setMessage("Failed to push swift transaction("+filePath+" ): " + sw);
                       }

//          nlpResponseModel.getAttributes().put("session", returnValue);
          return nlpResponseModel;
      }
    public static void uploadTransactions(String host,String userName,String password,String filePath) throws Exception {

        String boundary = UUID.randomUUID().toString();
        String LINE_FEED = "\r\n";

        URL url = new URL("http://localhost:9090/add-transactions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream outputStream = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

        // ---------- file part ----------
        File uploadFile = new File(filePath);
        String fileName = uploadFile.getName();

        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain").append(LINE_FEED);
        writer.append(LINE_FEED).flush();

        Files.copy(uploadFile.toPath(), outputStream);
        outputStream.flush();
        writer.append(LINE_FEED).flush();

        // ---------- ip field ----------
        addFormField(writer, boundary, "ip", host);

        // ---------- user field ----------
        addFormField(writer, boundary, "user", userName);

        // ---------- password field ----------
        addFormField(writer, boundary, "password", password);

        // ---------- finish ----------
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException("API call failed. Expected 201 but got: " + responseCode);
        }

        System.out.println("API call successful. Response code: " + responseCode);
    }

    private static void addFormField(PrintWriter writer, String boundary, String name, String value) {
        String LINE_FEED = "\r\n";
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
  } 