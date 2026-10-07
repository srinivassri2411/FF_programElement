package integrationLogics;

import java.time.Duration;
import java.time.Instant;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import java.util.Map;

public class NavigateToURLRetry implements Nlp {

    @InputParams({ @InputParam(name = "URL", type = "java.lang.String") })
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String url = (String) programElementsInput.get("URL");

        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        WebDriver driver = null;

        try {
            driver = nlpRequestModel.getWebDriver();

            // Core logic — verify block / network errors
            verifyWebsiteBlockStatus(driver, url);

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Website verification completed successfully for: " + url);

        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Error during website verification: " + e.getMessage());
        }

        return nlpResponseModel;
    }

    // Verify if site is blocked or unreachable
    public static WebDriver verifyWebsiteBlockStatus(WebDriver driver, String url) {
        try {
            System.out.println("Navigating to: " + url);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(35));

            try {
                driver.navigate().to(url);
            } catch (Exception e) {
                System.out.println("Initial navigation timeout or error, continuing...");
            }

            if (hasNetworkError(driver)) {
                System.out.println("Detected browser network error — possible unreachable or unsafe site.");
                return driver;
            }

            Instant end = Instant.now().plus(Duration.ofSeconds(5));
            boolean blockedDetected = false;

            while (Instant.now().isBefore(end)) {
                if (isWebsiteBlocked(driver)) {
                    blockedDetected = true;
                    break;
                }

                if (hasNetworkError(driver)) {
                    System.out.println("Detected network or security error while verifying.");
                    return driver;
                }

                Thread.sleep(1000);
            }

            if (blockedDetected) {
                System.out.println("PASS — 'Website Blocked' message detected.");
            } else {
                System.out.println("No 'Website Blocked' message detected. Refreshing once to confirm...");
                try {
                    ((JavascriptExecutor) driver).executeScript("window.stop();");
                    Thread.sleep(500);
                    driver.navigate().refresh();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.out.println("Refresh failed: " + e.getMessage());
                }

                if (hasNetworkError(driver)) {
                    System.out.println("FAIL — site unreachable (network/unsafe error after refresh).");
                } else if (isWebsiteBlocked(driver)) {
                    System.out.println("PASS after refresh — 'Website Blocked' appeared.");
                } else {
                    System.out.println("FAIL — site is accessible, not blocked.");
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR during verification: " + e.getMessage());
        }

        return driver;
    }

    private static boolean isWebsiteBlocked(WebDriver driver) {
        try {
            return driver
                    .findElements(By.xpath("//*[contains(translate(normalize-space(text()),"
                            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'website blocked')]"))
                    .size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasNetworkError(WebDriver driver) {
        try {
            String source = driver.getPageSource().toLowerCase();
            return source.contains("this site can’t be reached") || source.contains("this site can't be reached")
                    || source.contains("err_name_not_resolved") || source.contains("dns_probe_finished")
                    || source.contains("err_connection_timed_out") || source.contains("err_connection_refused")
                    || source.contains("your connection is not private") || source.contains("proceed to")
                    || source.contains("net::err_cert_authority_invalid");
        } catch (Exception e) {
            return false;
        }
    }
}