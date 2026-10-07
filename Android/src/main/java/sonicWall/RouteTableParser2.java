package sonicWall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RouteTableParser2 {

	public static void main(String[] args) {
		List<Map<String, String>> routeTable = new ArrayList<>();

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

				// Detect start of IPv4 Route Table
				if (line.startsWith("IPv4 Route Table")) {
					inIpv4Section = true;
					continue;
				}

				// Skip divider lines
				if (inIpv4Section && line.startsWith("==========")) {
					continue;
				}

				// Detect Active Routes section
				if (inIpv4Section && line.startsWith("Active Routes:")) {
					inActiveRoutes = true;
					continue;
				}

				if (inActiveRoutes) {
					// Stop when Persistent Routes section begins
					if (line.isEmpty() || line.startsWith("Persistent Routes:")) {
						break;
					}

					// Skip the header line
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

						routeTable.add(route);
					}
				}
			}

			process.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		// Output neatly formatted table
//		System.out.printf("%-20s %-18s %-15s %-15s %s%n", "Network Destination", "Netmask", "Gateway", "Interface",
//				"Metric");
//		System.out.println("--------------------------------------------------------------------------------");
		System.out.println(routeTable);
//        for (Map<String, String> route : routeTable) {
//            System.out.printf("%-20s %-18s %-15s %-15s %s%n",
//                    route.get("Network Destination"),
//                    route.get("Netmask"),
//                    route.get("Gateway"),
//                    route.get("Interface"),
//                    route.get("Metric"));
//        }
	}
}
