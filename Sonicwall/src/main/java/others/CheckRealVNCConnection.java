package others;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.*;

public class CheckRealVNCConnection implements Nlp {

    @InputParams({
        @InputParam(name = "Host", type = "java.lang.String"),
        @InputParam(name = "Port", type = "java.lang.String")
    })
    @ReturnType(name = "isConnected", type = "java.lang.Boolean")
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String ipAddress = (String) programElementsInput.get("Host");
        String port = (String) programElementsInput.get("Port");
        boolean returnValue = false;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            returnValue = isVNCProxyConnected(ipAddress, Integer.parseInt(port));
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully checked the connection");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to check the connection: " + sw);
        }

        nlpResponseModel.getAttributes().put("isConnected", returnValue);
        return nlpResponseModel;
    }

    public static boolean isVNCProxyConnected(String proxyHost, int proxyPort) throws IOException {
        String proxyIP = InetAddress.getByName(proxyHost).getHostAddress();

        Process process = Runtime.getRuntime().exec("netstat -ano");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("TCP") && line.contains("ESTABLISHED")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String remote = parts[2];
                    if (remote.endsWith(":" + proxyPort)) {
                        String remoteIP = remote.substring(0, remote.lastIndexOf(":"));
                        if (remoteIP.equals(proxyIP)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) throws NlpException {
        // Example usage
        NlpRequestModel hh = new NlpRequestModel();
        hh.getAttributes().put("Port", "56745");
        hh.getAttributes().put("Host", "127.0.0.1");

        System.out.println(new CheckRealVNCConnection().execute(hh));
    }
}
