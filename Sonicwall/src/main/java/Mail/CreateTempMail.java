package Mail;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
 
 
 
public class CreateTempMail implements Nlp {
    @InputParams({@InputParam(name = "Random Name", type = "java.lang.String")})
    @ReturnType(name = "mailAttributes", type = "java.util.Map")
 
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        map.clear();
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String randomName = (String) programElementsInput.get("Random Name");
     
 
          try {
        	  createMail(randomName);
        	  System.out.println(map);
              nlpResponseModel.setMessage("Mail Created as "+map.get("mail"));
              nlpResponseModel.setStatus(CommonConstants.pass);
              }
          catch(Exception e) {
           // Your program element Exception goes here ...
              nlpResponseModel.setMessage("Failed to create mail");
              nlpResponseModel.setStatus(CommonConstants.fail);
              }
 
          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("mailAttributes", map);
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
  	static Map map=new HashMap();
 
  	public static String getDomain() throws IOException {
		String apiUrl = "https://api.mail.tm/domains?page=1";
		StringBuilder response = new StringBuilder();
		URL url = new URL(apiUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
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
		net.minidev.json.JSONArray domain = JsonPath.read(document, "$..domain");
		return domain.get(0).toString();
	} 
  	public static void createMail(String randomName) throws IOException {
		//String domain="";
		String apiUrl = "https://api.mail.tm/accounts";
	//randomName="ashteafshte123";
	String password="Password@123";
	     String jsonBody = "{\n" +
                 "  \"address\": \""+randomName+"@"+getDomain()+"\",\n" +
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
		Object document = Configuration.defaultConfiguration().jsonProvider().parse(response.toString());
		net.minidev.json.JSONArray address = JsonPath.read(document, "$..address");
		net.minidev.json.JSONArray id = JsonPath.read(document, "$..id");
map.put("id", id.get(0).toString())	;
map.put("mail", address.get(0).toString());
map.put("password", password);
//System.out.println("Map is : "+map);
	
	}
  	
  	
  	
  	public static void main1(String[] args) throws NlpException {
  		
  		// NlpResponseModel nlpResponseModel = new NlpResponseModel();
  		NlpRequestModel nlpRequestModel =new NlpRequestModel();
  		nlpRequestModel.getAttributes().put("Random Name", "12nlpResponseModeldev234");
 
  		CreateTempMail c=new CreateTempMail();
  		c.execute(nlpRequestModel);
	}
}