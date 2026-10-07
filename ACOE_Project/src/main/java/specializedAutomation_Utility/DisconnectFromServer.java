package specializedAutomation_Utility;

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


public class DisconnectFromServer implements Nlp {
	@InputParams({@InputParam(name = "socket", type = "io.socket.client.Socket"),
		@InputParam(name = "ChatUserName", type = "java.lang.String")})
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Socket socket = (Socket) programElementsInput.get("socket");
		String ChatUserName = (String) programElementsInput.get("ChatUserName");


		try {
			socket.on(Socket.EVENT_DISCONNECT, params -> {
				socket.emit("chat message", ChatUserName+" Disconnected to server");
			});
			socket.disconnect();
			nlpResponseModel.setMessage("Successfully disconnected from the server");
			nlpResponseModel.setStatus(CommonConstants.pass);
		}

		catch(Exception e) {
			nlpResponseModel.setMessage("Failed to Disconnect from server due to exception "+e);
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

}
