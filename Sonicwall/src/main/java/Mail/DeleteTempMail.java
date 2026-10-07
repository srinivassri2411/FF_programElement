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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
 
 
 
public class DeleteTempMail implements Nlp {
    @InputParams({@InputParam(name = "Mail Attributes", type = "java.util.Map")})
  
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
       
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          Map mailDetails = (Map) programElementsInput.get("Mail Attributes");
        	map.putAll(mailDetails);
        	String message ="";
          try {
      
              nlpResponseModel.setMessage("The mail deleted Successfully");
              nlpResponseModel.setStatus(CommonConstants.pass);
              }
          catch(Exception e) {
           // Your program element Exception goes here ...
              nlpResponseModel.setMessage("Failed to delete mail");
              nlpResponseModel.setStatus(CommonConstants.fail);
              }
 
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
  	public static void deleteMail() throws IOException {
		String apiUrl = "https://api.mail.tm/accounts/"+map.get("id");
	
 
		StringBuilder response = new StringBuilder();
		URL url = new URL(apiUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("DELETE");
		  connection.setRequestProperty("accept", "application/ld+json");
	           connection.setRequestProperty("Authorization", "Bearer " + map.get("token"));
	           connection.setRequestProperty("Content-Type", "application/ld+json");
		int responseCode = connection.getResponseCode();
		
	}
  	public static void main1(String[] args) throws NlpException {
  		
  		// NlpResponseModel nlpResponseModel = new NlpResponseModel();
  		NlpRequestModel nlpRequestModel =new NlpRequestModel();
  		Map m=new HashMap();
  		m.put("password", "Password@123");
  		m.put("mail", "12nlpresponsemodeldev234@belgianairways.com");
  		m.put("id", "66a9eba37fdbec239b14ac40");
  		nlpRequestModel.getAttributes().put("Mail Attributes", m);
 
  		//GetLastMailFromInbox c=new GetLastMailFromInbox();
  		//c.execute(nlpRequestModel);
	}
}