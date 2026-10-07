package Mac;
import java.io.IOException;
import java.util.Arrays;
import java.io.*;

public class RunCommandWithSudo {
    public static void main(String[] args) {
        try {
            // Construct command
            String[] cmd = {
                "/bin/bash",
                "-",
                "sudo java -jar /Users/sonicwall/Downloads/click.jar /Users/sonicwall/Downloads/lock.png"
            };

            Process process = Runtime.getRuntime().exec(cmd);

            // Forward output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            while ((line = errReader.readLine()) != null) {
                System.err.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
