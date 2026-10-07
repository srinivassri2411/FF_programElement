
package blogic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class EnableAutoPDFDownloads implements Nlp {
	@InputParams({ @InputParam(name = "Browser Options", type = "java.lang.Object"),
			@InputParam(name = "Desired Capability", type = "org.openqa.selenium.remote.DesiredCapabilities") })
	@ReturnType(name = "Desired Capability", type = "org.openqa.selenium.remote.DesiredCapabilities")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		Object browserOptions = (Object) programElementsInput.get("Browser Options");
		DesiredCapabilities desiredCapability = (DesiredCapabilities) programElementsInput.get("Desired Capability");
		DesiredCapabilities returnValue = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = createInstance(desiredCapability, browserOptions);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully enabled auto pdf download");
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to enable " + e);
		}
		nlpResponseModel.getAttributes().put("Desired Capability", returnValue);
		return nlpResponseModel;
	}

	public static DesiredCapabilities createInstance(DesiredCapabilities cap, Object browserOptions) {
		String browserName = cap.getBrowserName();
		String userHome = System.getProperty("user.home");
		String downloadDir = userHome + File.separator + "Downloads";
		switch (browserName.toLowerCase()) {
		case "chrome":
			ChromeOptions chromeOptions = (ChromeOptions) browserOptions;
			Map<String, Object> chromePrefs = new HashMap<>();
			chromePrefs.put("printing.print_preview_sticky_settings.appState",
					"{\"recentDestinations\":[" + "{\"id\":\"Save as PDF\",\"origin\":\"local\",\"account\": \"\"}],"
							+ "\"selectedDestinationId\":\"Save as PDF\"," + "\"version\":2}");
			chromePrefs.put("savefile.default_directory", downloadDir);
			chromePrefs.put("download.default_directory", downloadDir);
			chromePrefs.put("download.prompt_for_download", false);
			chromePrefs.put("download.directory_upgrade", true);
			chromePrefs.put("plugins.always_open_pdf_externally", true);

			chromePrefs.put("credentials_enable_service", false);
			chromePrefs.put("profile.password_manager_enabled", false);
			chromePrefs.put("profile.password_manager_leak_detection", false);
			chromePrefs.put("autofill.profile_enabled", false);

			chromeOptions.setExperimentalOption("prefs", chromePrefs);
			chromeOptions.addArguments("--kiosk-printing");
			chromeOptions.addArguments("--remote-allow-origins=*");
			cap.merge(chromeOptions);
			return cap;
		case "microsoftedge":
			EdgeOptions edgeOptions = (EdgeOptions) browserOptions;
			Map<String, Object> edgePrefs = new HashMap<>();
			edgePrefs.put("printing.print_preview_sticky_settings.appState",
					"{\"recentDestinations\":[" + "{\"id\":\"Save as PDF\",\"origin\":\"local\",\"account\": \"\"}],"
							+ "\"selectedDestinationId\":\"Save as PDF\"," + "\"version\":2}");
			edgePrefs.put("savefile.default_directory", downloadDir);
			edgePrefs.put("download.default_directory", downloadDir);
			edgePrefs.put("download.prompt_for_download", false);
			edgePrefs.put("download.directory_upgrade", true);
			edgePrefs.put("plugins.always_open_pdf_externally", true);

			edgePrefs.put("credentials_enable_service", false);
			edgePrefs.put("profile.password_manager_enabled", false);
			edgePrefs.put("profile.password_manager_leak_detection", false);
			edgePrefs.put("autofill.profile_enabled", false);

			edgeOptions.setExperimentalOption("prefs", edgePrefs);
			edgeOptions.addArguments("--kiosk-printing");
			edgeOptions.addArguments("--remote-allow-origins=*");
			cap.merge(edgeOptions);
			return cap;

		case "firefox":
			String pdfOutputPath = downloadDir + File.separator + "Djubo.pdf";
			FirefoxProfile profile = new FirefoxProfile();
			String userAgent = "";
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/140.0";
			} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
				userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14.0; rv:128.0) Gecko/20100101 Firefox/140.0";
			} else if (System.getProperty("os.name").toLowerCase().contains("nux")) {
				userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/140.0";
			}
			profile.setPreference("general.useragent.override", userAgent);
			profile.setPreference("print_printer", "Microsoft Print to PDF");
			profile.setPreference("print.always_print_silent", true);
			profile.setPreference("print.show_print_progress", true);
			profile.setPreference("print.save_as_pdf.links.enabled", true);
			profile.setPreference("print.printer_Microsoft_Print_to_PDF.print_to_file", true);
			profile.setPreference("print.printer_Microsoft_Print_to_PDF.print_to_filename", pdfOutputPath);

			profile.setPreference("signon.rememberSignons", false); // Disable save password prompt
			profile.setPreference("signon.autofillForms", false); // Disable autofill in forms
			profile.setPreference("signon.generation.enabled", false); // Disable password generation
			profile.setPreference("signon.management.page.enabled", false); // Disable about:logins page
			profile.setPreference("extensions.formautofill.addresses.enabled", false); // Disable autofill addresses
			profile.setPreference("extensions.formautofill.creditCards.enabled", false); // Disable autofill credit
																							// cards

			FirefoxOptions firefoxOptions = (FirefoxOptions) browserOptions;
			firefoxOptions.setProfile(profile);
			cap.merge(firefoxOptions);
			return cap;
		default:
			throw new RuntimeException("Unsupported browser: " + browserName);
		}
	}
}