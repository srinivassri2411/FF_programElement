package Windows;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.windows.WindowsDriver;

public class ClickOnElementRetry implements Nlp {
	static String result = "Intial";
	public static Set<String> execptions = null;
	static StringBuilder logs = new StringBuilder();

	@InputParams({ @InputParam(name = "Locator Type", type = "java.lang.String"),
			@InputParam(name = "Locator Value", type = "java.lang.String"),
			@InputParam(name = "maxAttempt", type = "java.lang.String"),
			@InputParam(name = "explicitSeconds", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String locatorType = (String) programElementsInput.get("Locator Type");
		String locatorValue = (String) programElementsInput.get("Locator Value");
		String maxAttempt = (String) programElementsInput.get("maxAttempt");
		String explicitSeconds = (String) programElementsInput.get("explicitSeconds");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			execptions = new LinkedHashSet<String>();
			logs = new StringBuilder();
			safeClick(nlpRequestModel.getWebDriver(), locatorType, locatorValue, Integer.parseInt(maxAttempt),
					Integer.parseInt(explicitSeconds));
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage(
					"Clicked on element with retry " + result + " on " + locatorType + " = " + locatorValue);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			//nlpResponseModel.setMessage(
			//		"Failed to click on element with retry " + result + e + "\n\n" + "Main exception " + execptions);
			// nlpResponseModel.setMessage("Failed to click on element with retry " +
			// writer);
			
			
			 nlpResponseModel.setMessage(
			            "❌ Failed to click on element with retry \n" +
			            "Action: Click Element\n" +
			            "Locator: " + locatorType + " = " + locatorValue + "\n\n" +
			            "Execution Log:\n" + logs.toString() + "\n" +
			            "Final Error: " + e.getMessage());

			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void safeClick(WebDriver driver, String locatorType, String locatorValue, int maxAttempt,
			int explicitSeconds) {
		By locator = getBy(locatorType, locatorValue);
		int attempts = 0;

		while (attempts < maxAttempt) {
			logs.append("\n---------------------- Attempt ").append(attempts + 1).append(" ----------------------\n");

			try {
				Thread.sleep(1500);
//				driver.getPageSource();
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(explicitSeconds));
				
				
				 WebElement element = wait
						.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(locator)));
				  logs.append("✔ Element located and clickable\n");
				Thread.sleep(250);

				try {
					if (driver instanceof WindowsDriver) {
		                logs.append("➡ Trying Windows Mouse Click\n");

						String elementXml = element.getAttribute("BoundingRectangle");
						int left = Integer.parseInt(elementXml.split("Left:")[1].split(" ")[0].trim());
						int top = Integer.parseInt(elementXml.split("Top:")[1].split(" ")[0].trim());
						int width = Integer.parseInt(elementXml.split("Width:")[1].split(" ")[0].trim());
						int height = Integer.parseInt(elementXml.split("Height:")[1].trim());

						int x = left + width / 2;
						int y = top + height / 2;

						PointerInput mouse = new PointerInput(PointerInput.Kind.PEN, "mouse");

						// Build a click sequence: move → press → release
						Sequence click = new Sequence(mouse, 1)
								.addAction(mouse.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
								.addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
								.addAction(new Pause(mouse, Duration.ofMillis(200)))
								.addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

						((WindowsDriver) driver).perform(Arrays.asList(click));
						result = " Using Mouse NLP";
		                logs.append("✔ Success using Mouse Click\n");

						return;
					} else {
		                logs.append("➡ Trying Normal Click\n");
						element.click();
						result = " Using Normal NLP(Web)";
		                logs.append("✔ Success using Normal Click\n");

						return;
					}
				} catch (Exception ignored) {
		            logs.append("⚠ Normal Click Failed: ").append(cleanMsg(ignored)).append("\n");

					execptions.add(ignored.getMessage());
					try {
						if (driver instanceof WindowsDriver) {
							element.sendKeys(Keys.ENTER);
							result = " Using Action NLP ";
							return;
						} else {
							element.click();
							result = " Using Normal NLP(Web)";
		                    logs.append("✔ Success on Using Normal NLP(Web)\n");

							return;
						}

					} catch (Exception ignored2) {
						execptions.add(ignored2.getMessage());
						if (driver instanceof WindowsDriver) {
							element.click();
							result = " Using Normal NLP";
							return;
						} else {
							((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
							result = " Using JavascriptExecutor NLP";
							return;
						}
					}
				}

			}
			
			catch (StaleElementReferenceException e) {
				 execptions.add(e.getMessage());
				 logs.append("⚠ Stale Element - retrying\n");
				 attempts++;
			}catch (TimeoutException e) {
				 execptions.add(e.getMessage());
				 logs.append("⚠ Element not found\n ")
		            .append(explicitSeconds).append(" sec\n");
				 attempts++;

			}
			catch (NoSuchElementException e) {
				 execptions.add(e.getMessage());
			        logs.append("⚠ Element not found\n");

				 attempts++;
			}catch (ElementClickInterceptedException e) {
				 execptions.add(e.getMessage());
				 logs.append("⚠ Click intercepted (overlay issue)\n");
				 attempts++;
			}
			
			
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Thread interrupted", e);
			}
			
			
		}
		logs.append("\n❌ All attempts failed\n");
		throw new RuntimeException("Failed to click element after retries: " + locator.toString());
	}

	private static By getBy(String type, String value) {
		switch (type.toLowerCase()) {
		case "id":
			return By.id(value);
		case "name":
			return By.name(value);
		case "xpath":
			return By.xpath(value);
		case "classname":
			return By.className(value);
		case "css":
			return By.cssSelector(value);
		default:
			throw new IllegalArgumentException("Unsupported locator type: " + type);
		}
	}

	public static void main(String[] args) throws NlpException, InterruptedException {
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\User\\AppData\\Roaming\\fire-flink-client\\localnode\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(40));
		driver.manage().window().maximize();
		driver.get("http://49.249.29.6:8084/");
		driver.findElement(By.id("username")).sendKeys("rmgyantra");
		driver.findElement(By.id("inputPassword")).sendKeys("rmgy@9999");
		NlpRequestModel model = new NlpRequestModel();
		model.setWebDriver(driver);
		model.getAttributes().put("Locator Type", "xpath");
		model.getAttributes().put("Locator Value",
				"//*[@id=\"root\"]/div/section/div/div/div[2]/div/div/div/form/button[1]");
		model.getAttributes().put("maxAttempt", "3");
		model.getAttributes().put("explicitSeconds", "20");
		System.out.println(new ClickOnElementRetry().execute(model));
		System.out.println("clicked");
		Thread.sleep(4000);
		driver.findElement(By.xpath("//a[text()='Projects']")).click();

	}
	private static String cleanMsg(Exception e) {
	    String msg = e.getMessage();
	    if (msg == null) return "No message";

	    // Keep only first line to avoid huge Selenium stack traces
	    return msg.split("\n")[0];
	}
}