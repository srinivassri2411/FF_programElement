package sonicWall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UpdatedRouteTableParser {

    public static void main(String[] args) {
        String firstField = "Network Destination";
        String secondField = "Metric";

        List<Map<String, String>> routeList = new ArrayList<>();
        Map<String,String> filteredValues=new LinkedHashMap<String, String>();

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

                if (inIpv4Section && line.startsWith("===========================================================================")) {
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

        System.out.println(filteredValues);
    }
}