

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

public class InstallExtensionBiDi {
    public static void main(String[] args) throws IOException {
        ChromeDriver driver = new ChromeDriver();
        DevTools devTools = driver.getDevTools();
        devTools.createSession();

        // Read your MV3 extension (zipped folder with manifest.json + rules.json)
        byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\User\\Documents\\ACOE\\Extensions\\newModHeader\\newModHeader.zip"));
        String encoded = Base64.getEncoder().encodeToString(bytes);

        // Send BiDi command to install extension
        Command<String> installExtension =
                new Command<>("webDriver:extension:install", Map.of("extension", encoded), String.class);

        String extensionId = driver.getDevTools().send(installExtension);
        System.out.println("Extension installed with ID: " + extensionId);

        driver.get("https://httpbin.org/headers"); // test
    }
}
