package Mail;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


public class GetLastMailFromInbox implements Nlp {
    @InputParams({@InputParam(name = "Mail Attributes", type = "java.util.Map")})
    @ReturnType(name = "URL and Password", type = "Map")
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
       
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Map mailDetails = (Map) programElementsInput.get("Mail Attributes");
            map.putAll(mailDetails);
            String message ="";
            Map<String, String> returnValue=null;

          try {
            message= getLastMail(getBearerToken());
      returnValue=extractPasswordAndUrl(message);
       // System.out.println(message);
              nlpResponseModel.setMessage("The password and url are: "+returnValue);
              nlpResponseModel.setStatus(CommonConstants.pass);
              }
          catch(Exception e) {
        	  StringWriter writer = new StringWriter();
  		    	e.printStackTrace(new PrintWriter(writer));
              nlpResponseModel.setMessage("Failed to get password and url "+writer ) ;
              nlpResponseModel.setStatus(CommonConstants.fail);
              throw new NlpException(writer.toString());

              }
          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("URL and Password",returnValue );
          return nlpResponseModel;
      }
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
      
      public static String getBearerToken() throws IOException {
          String apiUrl = "https://api.mail.tm/token";
          String password = map.get("password").toString();
          String mail = map.get("mail").toString();
  String jsonBody = "{\n" +
                   "  \"address\": \""+mail+"\",\n" +
                   "  \"password\": \""+password+"\"\n" +
                   "}";
          StringBuilder response = new StringBuilder();
          URL url = new URL(apiUrl);
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          
          connection.setRequestMethod("POST");
            connection.setRequestProperty("accept", "application/ld+json");
               //  connection.setRequestProperty("Authorization", "Bearer " + authorizationToken);
                 connection.setRequestProperty("Content-Type", "application/ld+json");
                 // Enable input and output streams
                 connection.setDoOutput(true);
                 
                 
                 try (OutputStream os = connection.getOutputStream()) {
                     byte[] input = jsonBody.getBytes("utf-8");
                     os.write(input, 0, input.length);
                 }
          int responseCode = connection.getResponseCode();
          if ((responseCode == HttpURLConnection.HTTP_OK )||responseCode==HttpURLConnection.HTTP_CREATED) {
              BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              String line;
              while ((line = reader.readLine()) != null) {
                  response.append(line);
              }
              reader.close();
              connection.disconnect();
          }
          //  System.out.println(response.toString());
          Object document = Configuration.defaultConfiguration().jsonProvider().parse(response.toString());
          String token= JsonPath.read(document, "$.token");
          return token;
      }
      static Map map=new HashMap();
      public static String getLastMail(String bearerToken) throws IOException {
       //String domain="";
       String apiUrl = "https://api.mail.tm/messages?page=1";
       StringBuilder response = new StringBuilder();
       URL url = new URL(apiUrl);
       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
       connection.setRequestMethod("GET");
          connection.setRequestProperty("accept", "application/ld+json");
          connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
           connection.setRequestProperty("Content-Type", "application/ld+json");
           
       int responseCode = connection.getResponseCode();
       if ((responseCode == HttpURLConnection.HTTP_OK )||responseCode==HttpURLConnection.HTTP_CREATED) {
           BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
           String line;
           while ((line = reader.readLine()) != null) {
               response.append(line);
           }
           reader.close();
           connection.disconnect();
       
       }
        // System.out.println(response.toString());
       Object document = Configuration.defaultConfiguration().jsonProvider().parse(response.toString());
       net.minidev.json.JSONArray message = JsonPath.read(document, "$..id");
String mailContent=message.get(0).toString();
//System.out.println("Message id is : "+mailContent);
url = new URL("https://api.mail.tm/messages/"+mailContent);
connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("accept", "application/ld+json");
//connection.setRequestProperty("Content-Type", "application/ld+json");
connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
responseCode = connection.getResponseCode();
   StringBuilder response1 = new StringBuilder();
if ((responseCode == HttpURLConnection.HTTP_OK )||responseCode==HttpURLConnection.HTTP_CREATED) {
   BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
   String line;
   while ((line = reader.readLine()) != null) {
       response1.append(line);
   }
   reader.close();
   connection.disconnect();
}
//System.out.println(response1);
document = Configuration.defaultConfiguration().jsonProvider().parse(response1.toString());
net.minidev.json.JSONArray content = JsonPath.read(document, "$.html");
Object text=content.get(0);
//System.out.println("Message  is : "+text);
return text.toString();
   
   }
      public static Map<String, String> extractPasswordAndUrl(String htmlContent) {
          Map<String, String> result = new HashMap<>();
          if (htmlContent == null) return result;
   
          Document doc = Jsoup.parse(htmlContent);
   
          // 1) Extract Temporary password
          String tempPassword = null;
   
          // Try: find bold element that contains "Temporary password"
          Element bold = doc.selectFirst("b:matchesOwn((?i)Temporary password)");
          if (bold != null) {
              // look for nearby text nodes after the <b> element
              Node sibling = bold.nextSibling();
              while (sibling != null) {
                  if (sibling instanceof TextNode) {
                      String txt = ((TextNode) sibling).text().trim();
                      if (!txt.isEmpty()) {
                          tempPassword = txt.split("\\s+")[0].trim(); // first token
                          break;
                      }
                  } else if (sibling instanceof Element) {
                      String txt = ((Element) sibling).text().trim();
                      if (!txt.isEmpty()) {
                          tempPassword = txt.split("\\s+")[0].trim();
                          break;
                      }
                  }
                  sibling = sibling.nextSibling();
              }
   
              // if still null, try reading parent text and removing "Temporary password" label
              if (tempPassword == null) {
                  String parentText = bold.parent() != null ? bold.parent().text() : null;
                  if (parentText != null) {
                      parentText = parentText.replaceAll("(?i)Temporary password[:\\s]*", "").trim();
                      if (!parentText.isEmpty()) {
                          // often the password is the first token
                          tempPassword = parentText.split("\\s+")[0].trim();
                      }
                  }
              }
          }
   
          // fallback: simple text search near the label if jsoup selection didn't work
          if (tempPassword == null) {
              // look for the literal "Temporary password" in full text and pick the following token
              String allText = doc.text();
              int idx = allText.toLowerCase().indexOf("temporary password");
              if (idx >= 0) {
                  String after = allText.substring(idx + "temporary password".length()).trim();
                  // remove punctuation/colon if present
                  after = after.replaceFirst("^[\\:\\-\\s]+", "");
                  if (!after.isEmpty()) {
                      tempPassword = after.split("\\s+")[0].trim();
                  }
              }
          }
   
          result.put("temporaryPassword", tempPassword);
   
          // 2) Extract "Set Password" URL
          String setUrl = null;
   
          // Prefer anchors that contain the CTA text "SET PASSWORD" (case-insensitive)
          Element cta = doc.selectFirst("a:matchesOwn((?i)SET PASSWORD)");
          if (cta == null) {
              // some emails use slightly different anchor text (like "SET PASSWORD and GET STARTED")
              cta = doc.selectFirst("a:matchesOwn((?i)SET PASSWORD.*)");
          }
          if (cta == null) {
              // fallback: find anchor with common target strings
              Elements anchors = doc.select("a[href]");
              for (Element a : anchors) {
                  String href = a.attr("href");
                  if (href != null && (href.contains("oidc/login") || href.contains("auth-release") || href.toLowerCase().contains("set-password"))) {
                      cta = a;
                      break;
                  }
              }
          }
   
          if (cta != null) {
              String href = cta.attr("href").trim();
              // If href is a tracking redirect that contains an encoded inner URL, try to extract inner https substring
              // Example pattern in your sample: .../https:%2F%2Fqa-automation-sasikala.auth-release.bnntest.com%2Foidc%2Flogin%3Forg_name=...
              int httpsIdx = href.indexOf("https");
              if (httpsIdx >= 0) {
                  String innerEncoded = href.substring(httpsIdx);
                  try {
                      // decode percent-encoding
                      String decoded = URLDecoder.decode(innerEncoded, StandardCharsets.UTF_8.name());
                      // decoded should start with "https://..."
                      setUrl = decoded;
                  } catch (Exception e) {
                      // if decode fails, use the raw substring as best-effort
                      setUrl = innerEncoded.replace("%2F", "/").replace("%3A", ":");
                  }
              } else {
                  // not a redirect, return the href as-is
                  setUrl = href;
              }
          } else {
              // final fallback: pick first anchor that looks like an auth URL
              Element firstAuth = doc.selectFirst("a[href*=\"auth-release\"], a[href*=\"oidc/login\"]");
              if (firstAuth != null) setUrl = firstAuth.attr("href");
          }
   
          result.put("setPasswordUrl", setUrl);
          
          return result;
      }
      public static void main(String[] args) throws NlpException {
       
       // NlpResponseModel nlpResponseModel = new NlpResponseModel();
       NlpRequestModel nlpRequestModel =new NlpRequestModel();
       Map m=new HashMap();
       m.put("password", "Password@123");
       m.put("mail", "mhqqizov@2200freefonts.com");
       m.put("id", "6909adf5ab46ccab120e16ad");
       nlpRequestModel.getAttributes().put("Mail Attributes", m);
       GetLastMailFromInbox c=new GetLastMailFromInbox();
System.out.println(c.execute(nlpRequestModel));   }
}
 