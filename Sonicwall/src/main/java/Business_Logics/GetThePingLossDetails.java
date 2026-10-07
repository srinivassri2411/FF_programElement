
package Business_Logics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetThePingLossDetails implements Nlp {
    @InputParams({ @InputParam(name = "HostName", type = "java.lang.String") })
    @ReturnType(name = "passPercentage", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String hostName = (String) programElementsInput.get("HostName");
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            returnValue = getPacketLoss(hostName);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Successfully fetched the lost percentage");
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            nlpResponseModel.setMessage("Failed to fetch the lost percentage " + writer);
            nlpResponseModel.setStatus(CommonConstants.fail);

        }

        // Your program element business logic ends here ...
        nlpResponseModel.getAttributes().put("passPercentage", returnValue);
        return nlpResponseModel;
    }

    public static String getPacketLoss(String host) throws Exception {
        int lossPercentage = -1;
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder builder = os.contains("win") ? new ProcessBuilder("cmd.exe", "/c", "ping -n 4 " + host)
                : new ProcessBuilder("sh", "-c", "ping -c 4 " + host);

        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Windows: look for "Lost = X (Y% loss)"
            if (line.contains("Lost =")) {
                Pattern patternWin = Pattern.compile("\\((\\d+)%\\s*loss\\)");
                Matcher matcher = patternWin.matcher(line);
                if (matcher.find()) {
                    lossPercentage = Integer.parseInt(matcher.group(1));
                    break;
                }
            }

            // Linux/macOS: look for "X% packet loss"
            Pattern patternUnix = Pattern.compile("(\\d+)% packet loss");
            Matcher matcherUnix = patternUnix.matcher(line);
            if (matcherUnix.find()) {
                lossPercentage = Integer.parseInt(matcherUnix.group(1));
                break;
            }

            // Host not found
            if (line.toLowerCase().contains("could not find host") || line.toLowerCase().contains("unknown host")) {
                lossPercentage = -1;
                break;
            }
        }

        process.waitFor();

        return lossPercentage + "";
    }

    public static void main(String[] args) throws NlpException {
        NlpRequestModel nlp = new NlpRequestModel();
        nlp.getAttributes().put("HostName", "powerhouse.com");
        System.out.println(new GetThePingLossDetails().execute(nlp));
    }
}