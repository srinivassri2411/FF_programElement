package Mac;
import java.io.IOException;

public class LaunchTerminalAndRun {
    public static void main(String[] args) {
        // The command to be executed in Terminal
        String command = "java -jar /Users/sonicwall/Downloads/click.jar \"/Users/sonicwall/Downloads/lock.png\" \"/Users/sonicwall/Downloads/hand.png\"";

        // Build AppleScript to run the command in Terminal
        String[] openTerminalCommand = {
            "osascript", "-e",
            "tell application \"Terminal\" to do script \"" + command.replace("\"", "\\\"") + "\""
        };

        try {
            Runtime.getRuntime().exec(openTerminalCommand);
            System.out.println("Command sent to Terminal.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

