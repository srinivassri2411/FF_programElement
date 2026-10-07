
package sonicWall;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class ConnectToRDP implements Nlp {
	@InputParams({ @InputParam(name = "Host Name", type = "java.lang.String"),
			@InputParam(name = "User Name", type = "java.lang.String"),
			@InputParam(name = "RDGateway", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String hostName = (String) programElementsInput.get("Host Name");
		String userName = (String) programElementsInput.get("User Name");
		String rDGateway = (String) programElementsInput.get("RDGateway");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (connectRDP(hostName, userName, rDGateway)) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Connection started");
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Failed to start connection");
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to start connection " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	private static boolean connectRDP(String hostname, String username, String rdGateway) throws Exception {
		Path rdpFile = Files.createTempFile("rdp-connection", ".rdp");
		List<String> rdpConfig = Arrays.asList("full address:s:" + hostname, "username:s:" + username,
				"prompt for credentials:i:1", // Force prompt for password
				"authentication level:i:2", // Always connect, no warning
				"enablecredsspsupport:i:1", // Enable NLA
				"gatewayhostname:s:" + rdGateway, "gatewayusagemethod:i:1", // Always use gateway
				"gatewaycredentialssource:i:0", // Ask for creds
				"gatewayprofileusagemethod:i:1", // Same creds for gateway
				"gatewaybrokeringtype:i:0", "screen mode id:i:1", "desktopwidth:i:1280", "desktopheight:i:720",
				"redirectclipboard:i:1", "redirectprinters:i:1", "redirectcomports:i:0", "redirectsmartcards:i:1");
		Files.write(rdpFile, rdpConfig);
		try {
			new ProcessBuilder("mstsc", rdpFile.toString()).inheritIO().start();
			return true;
		} catch (IOException e) {
			throw new Exception("Failed to start mstsc process", e);
		}
	}
}