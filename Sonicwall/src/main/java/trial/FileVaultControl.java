package trial;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileVaultControl implements Nlp {

    @InputParams({
        @InputParam(name = "type", type = "java.lang.String"),
        @InputParam(name = "enableScriptPath", type = "java.lang.String"),
        @InputParam(name = "disableScriptPath", type = "java.lang.String"),
        @InputParam(name = "username", type = "java.lang.String"),
        @InputParam(name = "password", type = "java.lang.String")
    })
    @ReturnType(name = "recoveryKey", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> attributes = nlpRequestModel.getAttributes();
        String type = (String) attributes.get("type");
        String enableScriptPath = (String) attributes.get("enableScriptPath");
        String disableScriptPath = (String) attributes.get("disableScriptPath");
        String username = (String) attributes.get("username");
        String password = (String) attributes.get("password");

        String recoveryKey = null;
        NlpResponseModel response = new NlpResponseModel();

        try {
            // Step 1: Reset log file
            File logFile = new File(System.getProperty("user.home") + "/fdesetup_output.log");
            try (FileWriter fw = new FileWriter(logFile, false)) {
                fw.write("");
            }

            // Step 2: Run the expect script (dynamic path + dynamic username/password)
            ProcessBuilder pb;
            if ("enable".equalsIgnoreCase(type)) {
                pb = new ProcessBuilder("expect", enableScriptPath, username, password);
            } else {
                pb = new ProcessBuilder("expect", disableScriptPath, username, password);
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();

            Thread.sleep(1000);

            // Step 3: If enabling, simulate GUI interaction
            if ("enable".equalsIgnoreCase(type)) {
                click();
            }

            // Step 4: Capture output from the expect script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("Process timed out.");
            }

            // Step 5: Extract recovery key from output
            Pattern pattern = Pattern.compile("[A-Z0-9]{4,5}(?:-[A-Z0-9]{4,5}){5}");
            Matcher matcher = pattern.matcher(output.toString());

            if (matcher.find()) {
                recoveryKey = matcher.group();
            } else {
                // fallback from log file
                recoveryKey = getKeyFromLog();
            }

            response.setStatus(CommonConstants.pass);
        } catch (Exception e) {
            response.setStatus(CommonConstants.fail);
            response.setMessage("FileVault control failed: " + e.getMessage());
        }

        response.getAttributes().put("recoveryKey", recoveryKey);
        return response;
    }

    // Simulates UI interaction when FileVault GUI permission is shown
    private static void click() throws AWTException, InterruptedException {
        System.setProperty("java.awt.headless", "false");

        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (screenSize.width * 0.5);
        int y = (int) (screenSize.height * 0.25);

        robot.mouseMove(x, y);
        Thread.sleep(500);

        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);

        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    // Reads recovery key from log file as fallback
    private static String getKeyFromLog() throws IOException {
        File logFile = new File(System.getProperty("user.home") + "/fdesetup_output.log");
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();

        Pattern pattern = Pattern.compile("[A-Z0-9]{4,5}(?:-[A-Z0-9]{4,5}){5}");
        Matcher matcher = pattern.matcher(content.toString());

        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }
}
