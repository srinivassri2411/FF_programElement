package generic;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;

import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FetchFirstEmail {
	public static void main(String[] args) {
		String host = "imap.gmail.com";
		String username = "diqueryfireflink@gmail.com"; // Replace with your email
		String password = "vrhr gpld blvt reko"; // Replace with your app password
		String targetSubject = "Log in to Zomato"; // Replace with the subject you're searching for

		Properties properties = new Properties();
		properties.put("mail.store.protocol", "imaps");
		properties.put("mail.imaps.host", host);
		properties.put("mail.imaps.port", "993");
		properties.put("mail.imaps.ssl.enable", "true");

		try {
			Session session = Session.getInstance(properties);
			Store store = session.getStore("imaps");
			store.connect(host, username, password);

			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);

			// Fetch all messages
			Message[] messages = inbox.getMessages();

			boolean emailFound = false;
			for (int i = messages.length - 1; i >= messages.length - 2; i--) {
				Thread.sleep(2000);
				Message message = messages[i];
				if (message.getSubject().equalsIgnoreCase(targetSubject)) {
					// Found the email with the target subject
					System.out.println("Subject: " + message.getSubject());
					System.out.println("OTP: " + fetchTheOTp(getTextFromMessage(message)));
					emailFound = true;
					break;
				}
			}

			if (!emailFound) {
				System.out.println("No email found with subject: " + targetSubject);
			}

			// Close resources
			inbox.close(false);
			store.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Helper method to extract the email body
	private static String getTextFromMessage(Message message) throws Exception {
		if (message.isMimeType("text/plain")) {
			return message.getContent().toString();
		} else if (message.isMimeType("text/html")) {
			return message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			return getTextFromMimeMultipart(mimeMultipart);
		}
		return "Unsupported content type";
	}

	// Helper method to handle multipart messages
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

	private static String fetchTheOTp(String msg) {
		Document html = Jsoup.parse(msg);
		return html.getElementsByClass("otp_text").get(0).text();
	}
}