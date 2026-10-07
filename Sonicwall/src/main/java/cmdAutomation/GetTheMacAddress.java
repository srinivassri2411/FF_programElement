
package cmdAutomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import oshi.SystemInfo;
import oshi.software.os.NetworkParams;

public class GetTheMacAddress implements Nlp {
	@ReturnType(name = "Mac Address", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String gateway = getDefaultGatewayIP();
			returnValue = getMacAddressOf(gateway);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched the Mac Address");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch the Mac Address");
		}
		nlpResponseModel.getAttributes().put("Mac Address", returnValue);
		return nlpResponseModel;
	}

	public static String getDefaultGatewayIP() {
		SystemInfo si = new SystemInfo();
		NetworkParams params = si.getOperatingSystem().getNetworkParams();
		return params.getIpv4DefaultGateway();
	}

	public static String getMacAddressOf(String ipAddress) throws IOException, InterruptedException {
		Process ping = Runtime.getRuntime().exec("ping -n 1 " + ipAddress);
		ping.waitFor();
		Process arp = Runtime.getRuntime().exec("arp -a");
		BufferedReader reader = new BufferedReader(new InputStreamReader(arp.getInputStream()));

		String line;
		Pattern pattern = Pattern.compile(ipAddress.replace(".", "\\.") + "\\s+([\\w-]+)");
		while ((line = reader.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		throw new RuntimeException("MAC not found in ARP table");
	}
}