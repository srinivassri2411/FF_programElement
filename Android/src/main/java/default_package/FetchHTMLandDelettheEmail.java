package default_package;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;

import org.jsoup.Jsoup;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class FetchHTMLandDelettheEmail implements Nlp {

	@InputParams({ @InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Email Subject", type = "java.lang.String"),
			@InputParam(name = "Current Time", type = "java.lang.String"),
			@InputParam(name = "Unique String", type = "java.lang.String"),
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
//		String hName = (String) programElementsInput.get("HostName");
		String un = (String) programElementsInput.get("Username");
		String pass = (String) programElementsInput.get("Password");
		String eSub = (String) programElementsInput.get("Email Subject");
		String cTime = (String) programElementsInput.get("Current Time");
		String uniqueString = (String) programElementsInput.get("Unique String");
		String buffTime = (String) programElementsInput.get("Buffer time");
//		Long ctime = (Long) programElementsInput.get("Current time");
		String OneTimeOtp = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			OneTimeOtp = getOTPFromMail("imap.gmail.com", un, pass, eSub, cTime, uniqueString,
					Long.parseLong(buffTime));
			nlpResponseModel.setMessage("Fetched html successfully " + OneTimeOtp);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			e.printStackTrace();
			nlpResponseModel.setMessage("Failed to fetch html " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("OTP", OneTimeOtp);
		return nlpResponseModel;
	}

	public static String getOTPFromMail(String hname, String uname, String pass, String eSub, String cTime,
			String contentInfo, long buffSec) {
		String returnOtp = null;
		boolean emailFound = false;
		int CHECK_LATEST_N_EMAILS = 5;
		Properties properties = new Properties();
		properties.put("mail.store.protocol", "imaps");
		properties.put("mail.imaps.host", hname);
		properties.put("mail.imaps.port", "993");
		properties.put("mail.imaps.ssl.enable", "false");
		properties.put("mail.imaps.ssl.trust", "imap.gmail.com");
		properties.put("mail.imaps.ssl.checkserveridentity", "false");
		properties.put("mail.imaps.ssl.trust", "*");
		try {
			Session session = Session.getInstance(properties);
			Store store = session.getStore("imaps");
			store.connect(hname, uname, pass);
			Folder inbox = store.getFolder("INBOX");
			long startTime = System.currentTimeMillis();
			while ((System.currentTimeMillis() - startTime) < Duration.ofSeconds(buffSec).toMillis()) {
				Thread.sleep(CHECK_LATEST_N_EMAILS * 1000);
				inbox.open(Folder.READ_WRITE);
				int messageCount = inbox.getMessageCount();
				if (messageCount > 0) {
					Message[] messages = inbox.search(new SubjectTerm(eSub));
					System.out.println(messages.length);
					for (int i = messages.length - 1; i >= 0; i--) {
						Message message = messages[i];
						System.out.println(message.getSubject());
						System.out.println(message.getSentDate());
						if (message.getSubject().equalsIgnoreCase(eSub)
								&& getDifference(cTime, message.getSentDate().toString(), buffSec)
								&& getTextFromMessage(message).contains(contentInfo)) {
//	                    System.out.println("Subject: " + message.getSubject());
//	                    System.out.println("Body : " + getTextFromMessage(message));
							emailFound = true;
							returnOtp = createTempHtmlFile(getTextFromMessage(message));
							moveToTrash(inbox, message);
//                            message.setFlag(Flags.Flag.DELETED, true);
							break;
						} 
//						else if (getDifferenceTime(cTime, message.getSentDate().toString(), buffSec) < 0) {
//							break;
//						}
					}
				}
				if (emailFound) {
					break;
				}
				inbox.close(false);
			}
			if (!emailFound) {
				throw new RuntimeException("No email found with subject: " + eSub);
			}
			inbox.close(false);
			store.close();
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

	public static String createTempHtmlFile(String htmlContent) {
		try {
			htmlContent = Jsoup.parse(htmlContent).html();
			Path tempFile = Files.createTempFile("tempfile_", ".html");
			Files.write(tempFile, htmlContent.getBytes(), StandardOpenOption.WRITE);
			return tempFile.toAbsolutePath().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean getDifference(String cTime, String msgTime, long buffSec) {
		LocalDateTime bf = LocalDateTime.parse(cTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		LocalDateTime af = LocalDateTime.parse(
				LocalDateTime.parse(msgTime, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy"))
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
				DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		System.out.println(af);
		System.out.println(bf);
		long secondsDiff = Duration.between(bf, af).getSeconds();
		System.out.println("Diffff" + secondsDiff);
		return secondsDiff > 0 && secondsDiff < buffSec;
	}

	private static long getDifferenceTime(String cTime, String msgTime, long buffSec) {
		LocalDateTime bf = LocalDateTime.parse(cTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		LocalDateTime af = LocalDateTime.parse(
				LocalDateTime.parse(msgTime, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy"))
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
				DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		System.out.println(af);
		System.out.println(bf);
		long secondsDiff = Duration.between(bf, af).getSeconds();
		System.out.println("Diffff" + secondsDiff);
		return secondsDiff;
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
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel res = new NlpRequestModel();
		res.getAttributes().put("HostName", "imap.gmail.com");
		res.getAttributes().put("Username", "suryamukeshtonik@gmail.com");
		res.getAttributes().put("Password", "rltr vufp dtpn sdop");
		res.getAttributes().put("Email Subject", "Welcome to Kotak Connect Plus");
		res.getAttributes().put("Current Time", "09/02/2025 11:54:00");
		res.getAttributes().put("Unique String", "LOGIN NOW");
		res.getAttributes().put("Buffer time", "120");
		System.out.println(new FetchHTMLandDelettheEmail().execute(res));
	}
}