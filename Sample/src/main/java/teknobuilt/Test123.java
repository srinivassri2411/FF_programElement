package teknobuilt;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import javax.mail.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Test123 {
	public static String hostName = "smtp.gmail.com";
	public static String username = "fireflinkactivhealth@gmail.com";
	public static String password = "bepkrxxiraaeqppo";
	public static String emailsub = "User Account Verification";
	public static int flag = 1;
	public static int OneTimeOtp;
	public static String strOtp;
	public static long currentTime;

	public static void main(String[] args) {
		currentTime = System.currentTimeMillis();
		System.out.println(currentTime);
//		System.out.println(OutlookMailReader("Hello", "There", currentTime, hostName, username, password, emailsub));
	}

	public static Integer OutlookMailReader(String locatorName, String locatorValue, long currentTime, String hname,
			String uname, String pass, String eSub) {
		Message emailMessage;
		Properties sysProps = System.getProperties();
		sysProps.setProperty("mail.store.protocol", "imaps");
		sysProps.setProperty("mail.imaps.host", "imap.gmail.com");
		sysProps.setProperty("mail.imaps.port", "993");
		sysProps.setProperty("mail.imaps.ssl.enable", "true");
		sysProps.setProperty("mail.imaps.auth", "true");
		try {
			Session session = Session.getInstance(sysProps, null);
			Store store = session.getStore("imaps");
			store.connect(hname, uname, pass);
			Flags seen = new Flags(Flags.Flag.SEEN);
			for (int k = 1; k <= 3; k++) {
				Thread.sleep(5000);
				Folder emailBox = store.getFolder("Inbox");
				emailBox.open(Folder.READ_WRITE);
				Message[] messages = emailBox.getMessages();
				for (int i = messages.length - 1; i >= messages.length - 2; i--) {
					Thread.sleep(2000);
					Message message = messages[i];
					emailMessage = emailBox.getMessage(i);

					long mailTime = message.getReceivedDate().getTime();
					if (currentTime < mailTime) {
						if (message.getSubject().contains(eSub)) {
							String content = message.getContent().toString();
							FileWriter fw = new FileWriter(new File("./test.html"));
							fw.append(content);
							fw.flush();
							fw.close();
							flag++;
							String strOtp = extractOTP(content);
							OneTimeOtp = Integer.parseInt(strOtp);
							emailMessage.setFlag(Flags.Flag.SEEN, true);
							emailBox.close(true);
							break;
						}

					}
				}
				if (flag > 1)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to fetch OTP" + e);
		}
		if (flag == 1) {
			System.out.println("OTP NOT Found");
		}
		return OneTimeOtp;
	}

	public static String extractOTP(String emailBody) {
		// Parse HTML
		Document doc = Jsoup.parse(emailBody);
		// Find the span with class "fstnm"
		Element otpElement = doc.selectFirst("span.fstnm");

		// Check if OTP element is found and extract the text
		if (otpElement != null) {
			strOtp = otpElement.text().trim();
		}
		return strOtp;
	}

}