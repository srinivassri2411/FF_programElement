package socketIO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;


import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class sendChatToServer implements Nlp {
	@InputParams({@InputParam(name = "socket", type = "io.socket.client.Socket"),
		@InputParam(name = "ChatUserName", type = "java.lang.String"),
		@InputParam(name = "ChatMessage", type = "java.lang.String")})
	//@ReturnType(name = "Socket", type = "io.socket.client.Socket")
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Socket socket = (Socket) programElementsInput.get("socket");
		String ChatMessage = (String) programElementsInput.get("ChatMessage");
		String ChatUserName = (String) programElementsInput.get("ChatUserName");


		try {

			socket.on("chat message", eventParams -> {
				//System.out.println(eventParams[0]);
			});

			//			Thread inputThread = new Thread(() -> {
			//				while (true) {
			socket.emit("chat message", ChatUserName + " : " + ChatMessage);
			nlpResponseModel.setMessage("Successfully sent the chat to server "+ChatMessage);
			nlpResponseModel.setStatus(CommonConstants.pass);
			//				}
			//			});
			//			inputThread.start();

			//Thread.sleep(30000);

		}
		catch(Exception e) {
			nlpResponseModel.setMessage("Failed to sent chatmessage to user due to exception "+e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		//nlpResponseModel.getAttributes().put("Socket", socket);
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
} 
