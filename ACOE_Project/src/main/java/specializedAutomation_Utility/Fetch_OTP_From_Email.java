package specializedAutomation_Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Fetch_OTP_From_Email implements Nlp {

	@InputParams({ @InputParam(name = "HostName", type = "java.lang.String"),
			@InputParam(name = "Username", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "Email Subject", type = "java.lang.String") })
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
		String hName = (String) programElementsInput.get("HostName");
		String un = (String) programElementsInput.get("Username");
		String pass = (String) programElementsInput.get("Password");
		String eSub = (String) programElementsInput.get("Email Subject");
		String OneTimeOtp = null;
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			OneTimeOtp = OutlookMailReader(hName, un, pass, eSub, nlpResponseModel);
			nlpResponseModel.setMessage("Fetched Otp successfully " + OneTimeOtp);
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to fetch Otp" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);

		}
		nlpResponseModel.getAttributes().put("OTP", OneTimeOtp);
		return nlpResponseModel;
	}

	public static String OutlookMailReader(String hname, String uname, String pass, String eSub,
			NlpResponseModel nlpResponseModel) {
		String returnOtp = null;
		Properties sysProps = System.getProperties();
		sysProps.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getInstance(sysProps, null);
			Store store = session.getStore();
			store.connect(hname, uname, pass);
			Flags seen = new Flags(Flags.Flag.SEEN);
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			Message[] messages = inbox.getMessages();
			boolean emailFound = false;
			for (int i = messages.length - 1; i >= messages.length - 2; i--) {
				Thread.sleep(2000);
				Message message = messages[i];
				System.out.println(message.getSubject());
				if (message.getSubject().equalsIgnoreCase(eSub)) {
					System.out.println("Subject: " + message.getSubject());
					System.out.println("Body : " + getTextFromMessage(message));
					emailFound = true;
					returnOtp = fetchTheOTp(getTextFromMessage(message));
					break;
				}
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
		}
		return "Unsupported content type";
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

	private static String fetchTheOTp(String msg) {
		Document html = Jsoup.parse(msg);
		return html.getElementsByClass("otp_text").get(0).text();
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel res = new NlpRequestModel();
		res.getAttributes().put("HostName", "smtp.gmail.com");
		res.getAttributes().put("Username", "diqueryfireflink@gmail.com");
		res.getAttributes().put("Password", "vrhrgpldblvtreko");
		res.getAttributes().put("Email Subject", "Test Mail");
//		res.getAttributes().put("Current time", System.currentTimeMillis());
		System.out.println(new Fetch_OTP_From_Email().execute(res));
	}
}
