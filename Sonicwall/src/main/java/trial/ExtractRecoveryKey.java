package trial;
import java.io.*;
import java.util.regex.*;

public class ExtractRecoveryKey {
    public static void main(String[] args) throws Exception {
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
//Recovery Key: T6NC-RCKE-2AM5-XOAK-7TGV-ANJ8

        if (matcher.find()) {
            String recoveryKey = matcher.group();
            System.out.println("Recovery Key: " + recoveryKey);
        } else {
            System.out.println("Recovery Key not found.");
        }
    }
}
