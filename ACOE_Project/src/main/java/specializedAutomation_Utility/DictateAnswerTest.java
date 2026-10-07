package specializedAutomation_Utility;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DictateAnswerTest {

    public static void main(String[] args) throws Exception {

        String question = "Tell me about a challenging technical problem you solved while working with Automation. How did you debug and optimize it?";

        String answer = "";

        if (question.equalsIgnoreCase(
                "Tell me about a challenging technical problem you solved while working with Automation. How did you debug and optimize it?")) {

            answer = "One challenge I faced was automating a dynamic web application where elements loaded asynchronously, causing intermittent test failures. I debugged it using Playwright traces and logs, identified synchronization issues, replaced fixed waits with Playwright's auto-waiting and explicit assertions, and improved the locators. This made the tests much more stable and reduced flaky failures.";
        }

        System.out.println("Answer:");
        System.out.println(answer);

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe",
                "/c",
                "powershell -Command \"Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" 
                + answer + "')\""
        );

        Process process = builder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        process.waitFor();

        System.out.println("Speech completed");
    }
}