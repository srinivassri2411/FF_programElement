package paitentxpress;

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

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
public class FetchOTPAndDeleteEmails implements Nlp {
	@InputParams({ @InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Email Subject", type = "java.lang.String"),
			@InputParam(name = "Current Time", type = "java.lang.String"),
			@InputParam(name = "Buffer time", type = "java.lang.String") })
	@ReturnType(name = "OTP", type = "java.lang.String")
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

//	public static Integer flag = 1;
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String un = (String) programElementsInput.get("Username");
		String pass = (String) programElementsInput.get("Password");
		String eSub = (String) programElementsInput.get("Email Subject");
		String cTime = (String) programElementsInput.get("Current Time");
		String buffTime = (String) programElementsInput.get("Buffer time");
		String OneTimeOtp = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
//			cTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
			// log.info("PE started now " + LocalDateTime.now());
			OneTimeOtp = getOTPFromMail(un, pass, eSub, cTime, Long.parseLong(buffTime));
			nlpResponseModel.setMessage("Fetched html successfully " + OneTimeOtp);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed to fetch html " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("OTP", OneTimeOtp);
		return nlpResponseModel;
	}

	public static String getOTPFromMail(String uname, String pass, String eSub, String cTime, long buffSec)
			throws MessagingException {
		String returnOtp = null;
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
						returnOtp = getOTPFromHTML(getTextFromMessage(message));
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
		return returnOtp;
	}

	private static String getTextFromMessage(Message message) throws Exception {
		if (message.isMimeType("text/plain")) {
			return message.getContent().toString();
		} else if (message.isMimeType("text/html")) {
			return message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			return getTextFromMimeMultipart(mimeMultipart);
		} else {
			throw new RuntimeException("Unsupported content type");
		}
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
		StringBuilder result = new StringBuilder();
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result.append(bodyPart.getContent());
			} else if (bodyPart.isMimeType("text/html")) {
				result.append(bodyPart.getContent());
			}
		}
		return result.toString();
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

	public static String getOTPFromHTML(String html) {
		Document doc = Jsoup.parse(html, "UTF-8");
		Element paragraph = doc.selectFirst("p");
		if (paragraph != null) {
			String text = paragraph.text();
			Pattern pattern = Pattern.compile("\\b\\d{4}\\b");
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				return matcher.group();
			}
		}
		throw new RuntimeException("OTP not found");
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
		nlpreq.getAttributes().put("Email Subject", "Apurva Development Authentication Pin");
		nlpreq.getAttributes().put("Current Time", "15/07/2025 17:53:00");
		nlpreq.getAttributes().put("Buffer time", "120");
		System.out.println(new FetchOTPAndDeleteEmails().execute(nlpreq));
	}
}