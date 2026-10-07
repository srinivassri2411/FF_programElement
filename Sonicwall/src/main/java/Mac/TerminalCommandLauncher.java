package Mac;
import java.io.IOException;

public class TerminalCommandLauncher {
    public static void main(String[] args) {
        try {
            // Define the full command to run in Terminal
            String shellCommand = "java -jar /Users/sonicwall/Downloads/click.jar " +
                    "'/Users/sonicwall/Downloads/lock.png' " +
                    "'/Users/sonicwall/Downloads/hand.png'; exit";

            // Wrap the command in an AppleScript for Terminal
            String appleScript =
                "tell application \"Terminal\"\n" +
                "    activate\n" +
                "    do script \"" + shellCommand + "\"\n" +
                "end tell";

            // Execute the AppleScript using osascript
            String[] command = { "osascript", "-e", appleScript };
            Process process = new ProcessBuilder(command).start();
            process.waitFor();

            System.out.println("Terminal command executed successfully.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
