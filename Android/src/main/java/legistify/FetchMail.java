package legistify;

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

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FetchMail implements Nlp {
	@InputParams({ @InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Email Subject", type = "java.lang.String"),
			@InputParam(name = "Current Time", type = "java.lang.String"),
			@InputParam(name = "Buffer time", type = "java.lang.String") })
	@ReturnType(name = "MailContent", type = "java.util.Map<java.lang.String,java.lang.Object>")
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
		String cTime = (String) programElementsInput.get("Current Time");
		String buffTime = (String) programElementsInput.get("Buffer time");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			log.info("PE started now " + LocalDateTime.now());
			Map<String, Object> mailContent = getMailContentFromMail(un, pass, eSub, cTime, Long.parseLong(buffTime));
			if (mailContent == null || mailContent.isEmpty()) {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("No matching email found or failed to fetch.");
			} else {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Fetched mail content successfully.");
				nlpResponseModel.getAttributes().putAll(mailContent);
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to fetch mail content: " + writer.toString());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static Map<String, Object> getMailContentFromMail(String uname, String pass, String eSub, String cTime,
			long buffSec) throws MessagingException {
		Map<String, Object> mailContentMap = null;
		boolean emailFound = false;
		int CHECK_LATEST_N_EMAILS = 5;
		Properties properties = setMailProperties();
		try (Store store = getMailStore(properties, uname, pass); Folder inbox = store.getFolder("INBOX")) {
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < Duration.ofSeconds(buffSec).toMillis()) {
				inbox.open(Folder.READ_WRITE);
				Thread.sleep(2000);
				Message[] messages = inbox.search(new SubjectTerm(eSub));
				int total = messages.length;
				int start = Math.max(0, total - CHECK_LATEST_N_EMAILS);
				for (int i = total - 1; i >= start; i--) {
					Message message = messages[i];
					log.info("Checking email received at: " + message.getReceivedDate() + " | Subject: "
							+ message.getSubject());
					if (isOtpEmailValid(cTime, message, buffSec)) {
						mailContentMap = getMailContentFromMessage(message);
						moveToTrash(inbox, message);
						emailFound = true;
						break;
					} else {
						log.info("Email skipped due to time validation.");
					}
				}
				if (emailFound)
					break;
				else
					inbox.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to fetch the message " + e.getMessage());
		}
		return mailContentMap;
	}

	private static Map<String, Object> getMailContentFromMessage(Message message) throws Exception {
		Map<String, Object> resultMap = new java.util.HashMap<>();
		List<String> attachments = new ArrayList<>();

		// Create temp folder for this email
		File parentDir = new File(System.getProperty("java.io.tmpdir"), "EmailFetch_" + System.currentTimeMillis());
		if (!parentDir.exists())
			parentDir.mkdirs();

		Object content = message.getContent();

		if (content instanceof String && message.isMimeType("text/html")) {
			String htmlPath = saveHtmlToFile((String) content, parentDir);
			resultMap.put("html", htmlPath);
		} else if (content instanceof MimeMultipart) {
			processMultipart((MimeMultipart) content, resultMap, attachments, parentDir);
		}

		if (!attachments.isEmpty()) {
			resultMap.put("attachments", attachments);
		}

		return resultMap;
	}

	private static void processMultipart(MimeMultipart mimeMultipart, Map<String, Object> resultMap,
			List<String> attachments, File parentDir) throws Exception {
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);

			if (bodyPart.isMimeType("text/html") && !resultMap.containsKey("html")) {
				String savedPath = saveHtmlToFile((String) bodyPart.getContent(), parentDir);
				resultMap.put("html", savedPath);
			} else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || bodyPart.getFileName() != null) {
				String attachmentPath = saveAttachment(bodyPart, parentDir);
				attachments.add(attachmentPath);
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				processMultipart((MimeMultipart) bodyPart.getContent(), resultMap, attachments, parentDir);
			}
		}
	}

	private static String saveHtmlToFile(String html, File parentDir) throws IOException {
		Document doc = Jsoup.parse(html, "UTF-8");
		File htmlFile = new File(parentDir, "email.html");
		FileUtils.writeStringToFile(htmlFile, doc.html(), "UTF-8");
		log.info("HTML content saved to: " + htmlFile.getAbsolutePath());
		return htmlFile.getAbsolutePath();
	}

	private static String saveAttachment(BodyPart bodyPart, File parentDir) throws Exception {
		String fileName = bodyPart.getFileName();
		if (fileName == null) {
			fileName = "attachment_" + System.currentTimeMillis();
		}
		File file = new File(parentDir, fileName);
		FileUtils.copyInputStreamToFile(bodyPart.getInputStream(), file);
		log.info("Attachment saved to: " + file.getAbsolutePath());
		return file.getAbsolutePath();
	}

	private static boolean isOtpEmailValid(String currentTime, Message message, long bufferTime)
			throws MessagingException {
		String msgTime = message.getReceivedDate().toString();
		boolean valid = getDifference(currentTime, msgTime, bufferTime);
		if (!valid) {
			log.info("Email received time " + msgTime + " is outside buffer range compared to click time "
					+ currentTime);
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

	private static void moveToTrash(Folder inbox, Message message) throws MessagingException {
		Folder trash = inbox.getStore().getFolder("[Gmail]/Trash");
		if (!trash.exists()) {
			trash = inbox.getStore().getFolder("[Gmail]/Bin");
			if (!trash.exists()) {
				throw new RuntimeException("Trash folder not found! Email not deleted.");
			}
		}
		inbox.copyMessages(new Message[] { message }, trash);
		inbox.expunge();
		log.info("Moved message to trash.");
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

	private static Store getMailStore(Properties properties, String username, String password)
			throws MessagingException {
		Session session = Session.getInstance(properties);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", username, password);
		return store;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel nlpreq = new NlpRequestModel();
		nlpreq.getAttributes().put("Username", "srinivas.m.2411@gmail.com");
		nlpreq.getAttributes().put("Password", "zsii rrry wejr dpem");
		nlpreq.getAttributes().put("Email Subject", "Here Is Your Generated Contract View");
		nlpreq.getAttributes().put("Current Time", "29/09/2025 17:21:40");
		nlpreq.getAttributes().put("Buffer time", "120");
		NlpResponseModel resp = new FetchMail().execute(nlpreq);
		System.out.println(resp);
		System.out.println("Status: " + resp.getStatus());
		System.out.println("Message: " + resp.getMessage());
		System.out.println("Attributes: " + resp.getAttributes());
	}
}
