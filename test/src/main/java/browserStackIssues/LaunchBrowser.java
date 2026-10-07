package browserStackIssues;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;

public class LaunchBrowser {

	public static void main(String[] args) throws Exception {

		Yaml yaml = new Yaml();
		FileInputStream fis = new FileInputStream(
				"C:\\Users\\User\\Documents\\ACOE\\ProgramEle\\test\\browserstack.yml");

		Map<String, Object> caps = yaml.load(fis);

		ChromeOptions options = new ChromeOptions();
		options.merge(new ChromeOptions().merge(new org.openqa.selenium.MutableCapabilities(caps)));

		WebDriver driver = new ChromeDriver(options);

		driver.get("https://google.com");
	}
}