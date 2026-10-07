package telus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.HasExtensions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class OpenBrowserExtensionLoaded implements Nlp {
	@InputParams({ @InputParam(name = "Capability", type = "org.openqa.selenium.remote.DesiredCapabilities"),
			@InputParam(name = "WebHub Url", type = "java.lang.String"),
			@InputParam(name = "File Path", type = "java.lang.String") })

	@Override
	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();
		return params;
	}

	@Override
	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();
		return sb;
	}

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
//Srinivas M
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		try {
			String file = (String) attributes.get("File Path");
			String url = (String) attributes.get("WebHub Url");
			DesiredCapabilities cap = (DesiredCapabilities) attributes.get("Capability");
			String browserName = cap.getBrowserName();
			WebDriver driver = extension(cap, url, file);
			if (driver != null) {
				nlpResponseModel.setWebDriver(driver);
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Successfully Opened " + browserName);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("Not Supported " + browserName + " Browser is selected");
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Open Browser " + e);
		}

		return nlpResponseModel;
	}

	public WebDriver extension(DesiredCapabilities cap, String webHubUrl, String extensionPath) throws IOException {
		if (extensionPath.contains(".zip")) {
			extensionPath = unzipToTempFolder(extensionPath);
		}
		WebDriver driver = null;
		String browserName = cap.getBrowserName().toLowerCase();
		switch (browserName) {
		case "chrome":
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--remote-allow-origins=*");
			options.addArguments("--load-extension=" + extensionPath);
			options.addArguments("--disable-features=DisableLoadExtensionCommandLineSwitch");
//			options.addArguments("--disable-extensions-except=" + extensionPath);
			cap.merge(options);
			driver = new RemoteWebDriver(new URL(webHubUrl), cap);
			break;
		case "firefox":
			RemoteWebDriver driver1 = new RemoteWebDriver(new URL(webHubUrl), cap);
			driver1.setFileDetector(new LocalFileDetector());
			driver = new Augmenter().augment(driver1);
			((HasExtensions) driver).installExtension(Paths.get(extensionPath));
			break;
		case "microsoftedge":
			EdgeOptions egOptions = new EdgeOptions();
			egOptions.addArguments("--remote-allow-origins=*");
			egOptions.addArguments("--load-extension=" + extensionPath);
			egOptions.addArguments("--disable-extensions-except=" + extensionPath);
			cap.merge(egOptions);
			System.out.println(cap);
			driver = new RemoteWebDriver(new URL(webHubUrl), cap);
			break;
		case "safari":
			driver = new RemoteWebDriver(new URL(webHubUrl), cap);
			break;
		default:
			System.err.println("invalid browser");
			break;
		}
		return driver;
	}

	public static String unzipToTempFolder(String zipFilePath) throws IOException {
		File tempDir = Files.createTempDirectory("FF_unzippedFiles_").toFile();
		try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				File outFile = new File(tempDir, entry.getName());

				if (entry.isDirectory()) {
					outFile.mkdirs();
				} else {
					File parent = outFile.getParentFile();
					if (!parent.exists())
						parent.mkdirs();

					try (InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(outFile)) {
						in.transferTo(out);
					}
				}
			}
		}
		return tempDir.getAbsolutePath() + File.separator;
	}

	public static void main(String[] args) throws NlpException {
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setBrowserName("Chrome");
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("Capability", cap);
		nlpreq.getAttributes().put("WebHub Url", "http://localhost:4444/wd/hub");
		nlpreq.getAttributes().put("File Path", "C:\\Users\\User\\Documents\\ACOE\\Extensions\\newModHeader.zip");
		System.out.println(new OpenBrowserExtensionLoaded().execute(nlpreq));
//		ChromeOptions op = new ChromeOptions();
//		op.addExtensions(new File("C:\\Users\\User\\Documents\\ACOE\\Extensions\\newModHeader"));
//		new ChromeDriver(op);
	}
}