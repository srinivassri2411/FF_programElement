package specializedAutomation_Utility;
import java.util.ArrayList;
import java.util.HashMap;
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

import io.socket.client.IO;
import io.socket.client.Socket;


public class connectToServer implements Nlp {
	Socket socket;
	@InputParams({@InputParam(name = "ServerURL", type = "java.lang.String"), 
		@InputParam(name = "CustomUsername", type = "java.lang.String"),
		@InputParam(name = "CustomPassword", type = "java.lang.String"),
		@InputParam(name = "ChatUserName", type = "java.lang.String")})
	@ReturnType(name = "socket", type = "io.socket.client.Socket")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String ServerURL = (String) programElementsInput.get("ServerURL");
		String CustomUsername = (String) programElementsInput.get("CustomUsername");
		String CustomPassword = (String) programElementsInput.get("CustomPassword");
		String ChatUserName = (String) programElementsInput.get("ChatUserName");


		try {

			Map<String, List<String>> extraHeaders = new HashMap<>();
			List<String> customUsername = new ArrayList<>();
			customUsername.add(CustomUsername);
			extraHeaders.put("custom-username", customUsername);
			List<String> customPassword = new ArrayList<>();
			customPassword.add(CustomPassword);
			extraHeaders.put("custom-password", customPassword);	
			IO.Options options = new IO.Options();
			options.extraHeaders = extraHeaders;		
			socket = IO.socket(ServerURL, options);
			socket.on(Socket.EVENT_CONNECT, params -> {
				socket.emit("chat message", ChatUserName+" Connected to server");
				System.out.println("connected");

			});
			socket.connect();
			nlpResponseModel.setMessage("Succesfully connected to Server with user "+ChatUserName+" to "+ServerURL);
			nlpResponseModel.setStatus(CommonConstants.pass);
		}
		catch(Exception e) {
			nlpResponseModel.setMessage("Failed to connect to user due to exception "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("socket", socket);
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


	//	public static void main(String[] args) throws NlpException {
	//		NlpRequestModel nlpRequestModel = new NlpRequestModel();
	//		Map<String, Object> attributes = nlpRequestModel.getAttributes();
	//		attributes.put("ServerURL", "http://localhost:3000");
	//		attributes.put("CustomUsername", "wizard");
	//		attributes.put("CustomPassword", "1234");
	//		attributes.put("ChatUserName", "chandan");
	//		connectToServer cs=new connectToServer();
	//		cs.execute(nlpRequestModel);
	//		
	//	}
} 
