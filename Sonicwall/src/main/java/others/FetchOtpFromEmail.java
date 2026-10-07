package others;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.SubjectTerm;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import org.w3c.dom.Node;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FetchOtpFromEmail implements Nlp {
	public static String data = "";

	@InputParams({ @InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Email Subject", type = "java.lang.String"),
			// @InputParam(name = "Current Time", type = "java.lang.String"),
			@InputParam(name = "Buffer time", type = "java.lang.String") })
	@ReturnType(name = "code", type = "java.lang.String")
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

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String un = (String) programElementsInput.get("Username");
		String pass = (String) programElementsInput.get("Password");
		String eSub = (String) programElementsInput.get("Email Subject");
		// String cTime = (String) programElementsInput.get("Current Time");
		String buffTime = (String) programElementsInput.get("Buffer time");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		// String cTime = getCurrentTime();
		String mainCode = "";

		try {
//			WebDriver driver = new ChromeDriver();
//	        driver.manage().window().maximize();
//	        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
//
//	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
//	        driver.get("https://betafinx.choiceindia.com/");
//	    	driver.findElement(By.xpath("//input[@name='username']")).sendKeys("8779552825");
//	    	driver.findElement(By.xpath("//button[text()='Submit']")).click();
//	    	// ... (rest of your login logic)
//	    	driver.findElement(By.xpath("//input[@name='password']")).sendKeys("Ajay@1234");
//	        driver.findElement(By.xpath("//button[text()=' Continue ']")).click();
//	        String fetchVerificationMail = fetchVerificationMail("device@choicetechlab.com", "hleh yrtq zayc wfku", "Choice FinX-Login OTP", "", "120"); 
//	        String otp = extractOtp(fetchVerificationMail); 
//	        System.out.println(otp); 
//	        driver.findElement(By.xpath("(//div//input[@name='answer1'])[2]")).sendKeys(otp);
			mainCode = fetchVerificationMail(un, pass, eSub, "", buffTime);
			nlpResponseModel.setMessage("OTP is  " + mainCode);
			nlpResponseModel.setStatus(CommonConstants.pass);

		} catch (Exception e) {

			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to fetch mail content: " + writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}

		nlpResponseModel.getAttributes().put("code", mainCode);

		return nlpResponseModel;
	}

	public static String extractOtp(String mailText) {
		System.out.println(mailText);
		if (mailText == null || mailText.isEmpty()) {
			return null;
		}

		// This regex finds exactly 6 consecutive digits
		Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
		Matcher matcher = pattern.matcher(mailText);

		if (matcher.find()) {
			return matcher.group();
		}

		return null;
	}

	public static String fetchVerificationMail(String username, String password, String emailSubject,
			String currentTime, String bufferTimeSeconds) {

		try {
			if (currentTime == null || currentTime.isEmpty()) {
				currentTime = getCurrentTime();
//				currentTime = "10/06/2026 12:46:00";
			}

			Map<String, Object> mailContent = getMailContentFromMail(username, password, emailSubject, currentTime,
					Long.parseLong(bufferTimeSeconds));

			if (mailContent == null || mailContent.isEmpty()) {
				throw new RuntimeException("No matching email found.");
			}

			return mailContent.get("html").toString(); // same behavior as before

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch mail content: " + e.getMessage(), e);
		}
	}

	public static Map<String, Object> getMailContentFromMail(String uname, String pass, String eSub, String cTime,
			long buffSec) throws MessagingException {
		Map<String, Object> mailContentMap = null;
		boolean emailFound = false;
		final int CHECK_LATEST_N_EMAILS = 5;
		Properties properties = setMailProperties();

		try (Store store = getMailStore(properties, uname, pass); Folder inbox = store.getFolder("INBOX")) {

// Open once and keep open for the duration of the search loop
			if (!inbox.isOpen()) {
				inbox.open(Folder.READ_WRITE);
			}

			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < Duration.ofSeconds(buffSec).toMillis()) {
				try {
// small sleep to wait for incoming mail (keep inside try so interruption doesn't close store)
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}

// Force a refresh of the folder state (this is cheap)
				try {
					inbox.getMessageCount();
				} catch (MessagingException me) {
// if folder got closed unexpectedly, try to re-open
					if (!inbox.isOpen()) {
						inbox.open(Folder.READ_WRITE);
					} else {
						throw me;
					}
				}

				Message[] messages = inbox.search(new SubjectTerm(eSub));
				int total = messages.length;
				int start = Math.max(0, total - CHECK_LATEST_N_EMAILS);

				for (int i = total - 1; i >= start; i--) {
					Message message = messages[i];
//log.info("Checking email received at: " + message.getReceivedDate() + " | Subject: " + message.getSubject());
					if (isOtpEmailValid(cTime, message, buffSec)) {
						mailContentMap = getMailContentFromMessage(message);

						System.out.println("moving to trash");
						moveToTrash(inbox, message);
						emailFound = true;
						break;
					} else {
//log.info("Email skipped due to time validation.");
					}
				}

				if (emailFound)
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to fetch the message " + e.getMessage(), e);
		}

		return mailContentMap;
	}

	private static void moveToTrash(Folder inbox, Message message) throws MessagingException {
		Folder trash = inbox.getStore().getFolder("[Gmail]/Trash");
		System.out.println(message);
		if (!trash.exists()) {
			trash = inbox.getStore().getFolder("[Gmail]/Bin");
			if (!trash.exists()) {
				throw new RuntimeException("Trash folder not found! Email not deleted.");
			}
		}
		inbox.copyMessages(new Message[] { message }, trash);
		inbox.expunge();
		// log.info("Moved message to trash.");
	}

	private static boolean isOtpEmailValid(String currentTime, Message message, long bufferTime)
			throws MessagingException {
		String msgTime = message.getReceivedDate().toString();
		boolean valid = getDifference(currentTime, msgTime, bufferTime);
		if (!valid) {
			// log.info("Email received time " + msgTime + " is outside buffer range
			// compared to click time "
			// + currentTime);
		}
		return valid;
	}

	private static boolean getDifference(String currentTime, String msgTime, long bufferSec) {
		try {
			LocalDateTime clickTime = LocalDateTime.parse(currentTime,
					DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			Date receivedDate = sdf.parse(msgTime);
			LocalDateTime mailTime = LocalDateTime.ofInstant(receivedDate.toInstant(), ZoneId.systemDefault());

			long secondsDiff = Duration.between(clickTime, mailTime).getSeconds();

			// Return true if mail time is >= click time and within bufferSec
			return secondsDiff >= 0 && secondsDiff <= bufferSec;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static Map<String, Object> getMailContentFromMessage(Message message) throws Exception {
		Map<String, Object> resultMap = new java.util.HashMap<>();
		List<String> attachments = new ArrayList<>();

		// Create temp folder for this email
		File parentDir = new File(System.getProperty("java.io.tmpdir"), "EmailFetch_" + System.currentTimeMillis());
		if (!parentDir.exists())
			parentDir.mkdirs();

		Object content = message.getContent();

		if (content instanceof String) {
			String body = content.toString();

			if (message.isMimeType("text/html")) {
				Document doc = Jsoup.parse(body, "UTF-8");
				resultMap.put("mail", doc.html());
				data = doc.text();
			} else if (message.isMimeType("text/plain")) {
				resultMap.put("mail", body);
				data = body;
			}
			System.out.println("body==>" + body);
			// Save content for debugging/backward compatibility
			String savedPath = saveHtmlToFile(body, parentDir);
			resultMap.put("html", savedPath);
		}

		else if (content instanceof MimeMultipart) {
			processMultipart((MimeMultipart) content, resultMap, attachments, parentDir);
		}

		if (!attachments.isEmpty()) {
			resultMap.put("attachments", attachments);
		}

		return resultMap;
	}

	private static String saveHtmlToFile(String html, File parentDir) throws IOException {
		Document doc = Jsoup.parse(html, "UTF-8");
		File htmlFile = new File(parentDir, "email.html");
		FileUtils.writeStringToFile(htmlFile, doc.html(), "UTF-8");
		// log.info("HTML content saved to: " + htmlFile.getAbsolutePath());
		// Return path to saved file (backwards compatibility)
		return htmlFile.getAbsolutePath();
	}

	public static String extractWithJsoupSelector(String html, String cssSelector, int index) {
		if (html == null || html.isEmpty())
			return null;

		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		System.out.println(doc.text().toString());
		Elements els = doc.select(cssSelector);

		return els.size() > index ? els.get(index).text().trim() : null;
	}

	public static String extractByXPathFromHtml(String html, String xpathExpr) throws Exception {
		if (html == null || html.isEmpty() || xpathExpr == null || xpathExpr.isEmpty()) {
			return null;
		}

		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

		W3CDom w3cDom = new W3CDom();
		org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

		// Evaluate XPath
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile(xpathExpr);

		Node node = (Node) expr.evaluate(w3cDoc, XPathConstants.NODE);
		if (node == null) {
			return null;
		}

		return node.getTextContent() != null ? node.getTextContent().trim() : null;
	}

	private static void processMultipart(MimeMultipart mimeMultipart, Map<String, Object> resultMap,
			List<String> attachments, File parentDir) throws Exception {
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);

			if (bodyPart.isMimeType("text/html") && !resultMap.containsKey("html")) {
				Object partContent = bodyPart.getContent();
				String htmlString = (partContent != null) ? partContent.toString() : "";
				// Parse and store HTML into "mail" variable
				Document doc = Jsoup.parse(htmlString, "UTF-8");
				String mailContent = doc.html();
				resultMap.put("mail", mailContent);
				data = doc.text();
				System.out.println("Data=>" + data);
				// Save file and keep path as well
				String savedPath = saveHtmlToFile(htmlString, parentDir);
				resultMap.put("html", savedPath);
			} else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || bodyPart.getFileName() != null) {
				String attachmentPath = saveAttachment(bodyPart, parentDir);
				attachments.add(attachmentPath);
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				processMultipart((MimeMultipart) bodyPart.getContent(), resultMap, attachments, parentDir);
			}
		}
	}

	private static Properties setMailProperties() {
		Properties properties = new Properties();
		properties.put("mail.store.protocol", "imaps");
		properties.put("mail.imaps.host", "imap.gmail.com");
		properties.put("mail.imaps.port", "993");
		properties.put("mail.imaps.ssl.enable", "false");
		properties.put("mail.imaps.ssl.trust", "imap.gmail.com");
		properties.put("mail.imaps.ssl.checkserveridentity", "false");
		properties.put("mail.imaps.ssl.trust", "*");
		return properties;
	}

	private static String saveAttachment(BodyPart bodyPart, File parentDir) throws Exception {
		String fileName = bodyPart.getFileName();
		if (fileName == null) {
			fileName = "attachment_" + System.currentTimeMillis();
		}
		File file = new File(parentDir, fileName);
		FileUtils.copyInputStreamToFile(bodyPart.getInputStream(), file);
		// log.info("Attachment saved to: " + file.getAbsolutePath());
		return file.getAbsolutePath();
	}

	private static Store getMailStore(Properties properties, String username, String password)
			throws MessagingException {
		Session session = Session.getInstance(properties);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", username, password);
		return store;
	}

	

	public static String getCurrentTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return LocalDateTime.now().minusSeconds(120).format(formatter);
	}

}
