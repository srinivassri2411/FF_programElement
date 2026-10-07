package testProjsdfsdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

import org.openqa.selenium.bidi.BiDi;
import org.openqa.selenium.chrome.ChromeDriver;

public class InstallExtensionBiDi {
	public static void main(String[] args) throws IOException {
		ChromeDriver driver = new ChromeDriver();

		// Start BiDi session
		BiDi bidi = new BiDi(driver);

		// Read your MV3 extension as .zip
		byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\User\\Documents\\ACOE\\Extensions\\newModHeader.zip"));
		String encoded = Base64.getEncoder().encodeToString(bytes);

		// Install extension using BiDi
		String extensionId = bidi.send("webDriver:extension:install", Map.of("extension", encoded), String.class);

		System.out.println("Extension installed with ID: " + extensionId);

		driver.get("https://httpbin.org/headers"); // verify
	}
}
