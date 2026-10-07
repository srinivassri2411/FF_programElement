
package others;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GetRouteTableValues implements Nlp {
	@InputParams({ @InputParam(name = "Header1", type = "java.lang.String"),
			@InputParam(name = "Header2", type = "java.lang.String") })
	@ReturnType(name = "result", type = "java.util.Map")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String header1 = (String) programElementsInput.get("Header1");
		String header2 = (String) programElementsInput.get("Header2");
		Map<String, String> returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = getValues(header1, header2);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully fetched the values from the route table");
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to fetch the values from the route table " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("result", returnValue);
		return nlpResponseModel;
	}

	public static Map<String, String> getValues(String firstField, String secondField) {
		List<Map<String, String>> routeList = new ArrayList<>();
		Map<String, String> filteredValues = new LinkedHashMap<String, String>();

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
					continue;
				}

				if (inIpv4Section && line.startsWith("Active Routes:")) {
					inActiveRoutes = true;
					continue;
				}

				if (inActiveRoutes) {
					if (line.isEmpty() || line.startsWith("Persistent Routes:")) {
						break;
					}

					if (line.startsWith("Network Destination")) {
						continue;
					}

					String[] tokens = line.split("\\s+");
					if (tokens.length >= 5) {
						Map<String, String> route = new LinkedHashMap<>();
						route.put("Network Destination", tokens[0]);
						route.put("Netmask", tokens[1]);
						route.put("Gateway", tokens[2]);
						route.put("Interface", tokens[3]);
						route.put("Metric", tokens[4]);
						routeList.add(route);
					}
				}
			}

			process.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		for (Map<String, String> route : routeList) {
			String val1 = route.getOrDefault(firstField, "N/A");
			String val2 = route.getOrDefault(secondField, "N/A");
			filteredValues.put(val1, val2);
		}

		return filteredValues;
	}
}