
package others;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetIPv4Ips implements Nlp {
	@ReturnType(name = "ipValues", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getIps();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Fetched the IPv4 ip address");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to fetch the IPv4 ip address =>" + e);
		}
		nlpResponseModel.getAttributes().put("ipValues", returnValue);
		return nlpResponseModel;
	}

	private Map<String, Object> getIps() {
		Map<String, Object> ipv4RouteMap = new LinkedHashMap<>();

		try {
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "route print");
			builder.redirectErrorStream(true);
			Process process = builder.start();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
			String line;
			boolean inIpv4Section = false;
			boolean inActiveRoutes = false;

			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.startsWith("IPv4 Route Table")) {
					inIpv4Section = true;
					continue;
				}

				if (inIpv4Section && line
						.startsWith("===========================================================================")) {
					continue; // Skip table dividers
				}

				if (inIpv4Section && line.startsWith("Active Routes:")) {
					inActiveRoutes = true;
					continue;
				}

				if (inActiveRoutes) {
					if (line.isEmpty() || line.startsWith("Persistent Routes:")) {
						break; // End of Active Routes section
					}

					// Avoid header row
					if (line.startsWith("Network Destination")) {
						continue;
					}

					// Tokenize the line
					String[] tokens = line.split("\\s+");
					if (tokens.length >= 5) {
						String destination = tokens[0];
						String iface = tokens[3];
						ipv4RouteMap.put(destination, iface);
					}
				}
			}

			process.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return ipv4RouteMap;
	}
}