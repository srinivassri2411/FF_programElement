package latest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

import io.appium.java_client.android.AndroidDriver;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class StartTrigger implements Nlp {
	private static AndroidDriver driver = null;

	@InputParams({ @InputParam(name = "Whatsapp Group", type = "java.lang.String"),
			@InputParam(name = "From_MailId", type = "java.lang.String"),
			@InputParam(name = "To_MailId", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String group = (String) programElementsInput.get("Whatsapp Group");
		String fromMailId = (String) programElementsInput.get("From_MailId");
		String toMailId = (String) programElementsInput.get("To_MailId");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {
			Map<String, String> exeIdentifiers = (Map<String, String>) nlpRequestModel.getExecutionIdentifiers();
			String executionId = exeIdentifiers.get("executionId");
			// String executionId = "EXEC1234";
			String whatsappMessage = "Suite execution is started with execution Id " + executionId;
			sendMessage(whatsappMessage, group);
			sendMail(fromMailId, toMailId, executionId);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Notification sent on Whatsapp");
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to send notification " + writer.toString());
		}
		if (driver != null) {
			driver.quit();
			driver = null;
		}
		return nlpResponseModel;
	}

	private static void sendMail(String from, String to, String executionId)
			throws AddressException, MessagingException {
		String relayHost = "10.15.0.35";
		Properties props = new Properties();
		props.put("mail.smtp.host", relayHost);
		props.put("mail.smtp.port", "25");
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.from", from);

		Session session = Session.getInstance(props);

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject("Suite Execution Started for " + executionId);

		String mailBody = "<html><body>" + "<div>Dear Fireflink User</div></b>"
				+ "<div><b>Suite execution is started with Execution ID:</b> " + executionId + "</div>"
				+ "<p>Looking forward to serving your complete Testing Automation needs.</p>" + "<div>FireFlink</div>"
				+ "<div>Extreme Automation</div>" + "</body></html>";

		// Create multipart message
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setContent(mailBody, "text/html; charset=utf-8");

		MimeMultipart multipart = new MimeMultipart();
		multipart.addBodyPart(textPart);

		message.setContent(multipart);
		// Send the message
		Transport.send(message);
	}

	private static void sendMessage(String message, String groupName) throws Exception {
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability("appium:automationName", "uiAutomator2");
		cap.setCapability("appium:platformName", "android");

		driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), cap);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

		clearRecents();

		driver.executeScript("mobile: shell",
				Map.of("command", "am", "args", List.of("start", "-n", "com.whatsapp/.Main")));
		driver.findElement(By.xpath("//*[contains(@text,'Search')]")).click();
		driver.findElement(By.id("com.whatsapp:id/search_input")).sendKeys(groupName);
		Thread.sleep(1500);
		if (!driver.findElement(By.xpath("//android.widget.TextView[@text='" + groupName + "']")).isDisplayed()) {
			throw new NotFoundException(groupName + " not found");
		}
		driver.findElement(By.xpath("//android.widget.TextView[@text='" + groupName + "']")).click();
		driver.findElement(By.id("com.whatsapp:id/entry")).sendKeys(message);
		driver.findElement(By.id("com.whatsapp:id/send")).click();
		driver.executeScript("mobile: shell", Map.of("command", "am", "args", List.of("force-stop", "com.whatsapp")));

		clearRecents();
		driver.quit();
	}

	private static void clearRecents() throws Exception {
		openRecentsPage();
		try {
			driver.findElement(By.xpath("//android.widget.Button[contains(@text,'Close')]")).click();
		} catch (Exception ignored) {
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		int maxSwipes = 10;
		for (int i = 0; i < maxSwipes; i++) {
			if (!driver.findElements(By.xpath("//android.widget.TextView[contains(@text,'recent')]")).isEmpty()) {
				System.out.println("Recents cleared");
				driver.executeScript("mobile: shell", Map.of("command", "input", "args", List.of("keyevent", "3")));
				break;
			}
			swipeUp();
			Thread.sleep(200);
			openRecentsPage();
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

	}

	private static void openRecentsPage() throws Exception {
		driver.executeScript("mobile: shell", Map.of("command", "input", "args", List.of("keyevent", "3")));
		Thread.sleep(500);
		driver.executeScript("mobile: shell", Map.of("command", "input", "args", List.of("keyevent", "187")));
		Thread.sleep(1000);
	}

	private static void swipeUp() {

		Dimension size = driver.manage().window().getSize();

		int startX = size.width / 2;
		int startY = (int) (size.height * 0.7);
		int endY = (int) (size.height * 0.2);

		PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
		Sequence swipe = new Sequence(finger, 1);

		swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));

		swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));

		swipe.addAction(finger.createPointerMove(Duration.ofMillis(100), PointerInput.Origin.viewport(), startX, endY));

		swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

		driver.perform(Collections.singletonList(swipe));
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Whatsapp Group", "Acoe_test");
		model.getAttributes().put("Mob_Hub Url", "http://localhost:4723/wd/hub");
		System.out.println(new StartTrigger().execute(model));
	}
}