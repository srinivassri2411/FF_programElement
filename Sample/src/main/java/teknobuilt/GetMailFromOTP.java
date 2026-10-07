package teknobuilt;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;

public class GetMailFromOTP {
	public static void main(String[] args) {
		// Gmail IMAP settings
		String host = "imap.gmail.com";
		String username = "srinivas.m.2411@gmail.com"; // Replace with your Gmail address
		String appPassword = "kazh mzta aces hidm"; // Replace with your App Password

		// Setting properties
		Properties properties = new Properties();
		properties.put("mail.imap.host", host);
		properties.put("mail.imap.port", "993");
		properties.put("mail.imap.ssl.enable", "true");

		// Get the session
		Session session = Session.getDefaultInstance(properties);

		try {
			// Connect to the store
			Store store = session.getStore("imap");
			store.connect(host, username, appPassword);

			// Open the inbox folder
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);

			// Fetch unread messages
			Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			System.out.println("Number of unread messages: " + messages.length);

			for (int i = messages.length - 1; i >= messages.length - 2; i--) {
				Message message = messages[i];
				System.out.println("Subject: " + message.getSubject());
				System.out.println("Date: " + message.getReceivedDate());
//				if (message.getContent() instanceof String) {
//					System.out.println("Body: " + message.getContent());
//				} else if (message.getContent() instanceof Multipart) {
//					Multipart multipart = (Multipart) message.getContent();
//					for (int i = 0; i < multipart.getCount(); i++) {
//						MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
//						if (part.isMimeType("text/plain")) {
//							System.out.println("Body: " + part.getContent());
//						}
//					}
//				}
			}

			// Close resources
			inbox.close(false);
			store.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}