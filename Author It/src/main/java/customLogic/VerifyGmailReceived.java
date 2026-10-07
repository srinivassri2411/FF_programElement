package customLogic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;

public class VerifyGmailReceived implements Nlp {

    @InputParams({ @InputParam(name = "Email", type = "java.lang.String"),
            @InputParam(name = "App Password", type = "java.lang.String"),
            @InputParam(name = "Expected Mail Time(dd/MM/yyyy HH:mm:ss)", type = "java.lang.String"),
            @InputParam(name = "BufferTime", type = "java.lang.Integer"),
            @InputParam(name = "Subject of Mail", type = "java.lang.String") })
    @ReturnType(name = "IsMailReceived", type = "java.lang.Boolean")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String email = (String) programElementsInput.get("Email");
        String appPwd = (String) programElementsInput.get("App Password");
        String expectedMailTime = (String) programElementsInput.get("Expected Mail Time(dd/MM/yyyy HH:mm:ss)");
        Integer bufferTime = (Integer) programElementsInput.get("BufferTime");
        String subjectofMail = (String) programElementsInput.get("Subject of Mail");

        boolean isMailReceived = false;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            isMailReceived = isMailReceived(email, appPwd, expectedMailTime, bufferTime, subjectofMail);

            if (!isMailReceived) {
                throw new RuntimeException("No mail found with subject: " + subjectofMail);
            }

            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Mail found with subject: " + subjectofMail);
        } catch (Exception e) {
            isMailReceived = false;
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("No mail found with subject: " + subjectofMail + " - " + e.getMessage());
        }
        nlpResponseModel.getAttributes().put("IsMailReceived", isMailReceived);
        return nlpResponseModel;
    }

    public static boolean isMailReceived(String email, String appPwd, String formattedDateTime, int bufferTime,
            String subject) throws MessagingException, ParseException, InterruptedException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Properties props = System.getProperties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", email, appPwd);

        Date startDate = dateFormat.parse(formattedDateTime);

        boolean found = false;
        try {
            for (int i = 0; i < bufferTime; i += 10) {
                ArrayList<Message> dateList = new ArrayList<Message>();
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                ReceivedDateTerm rdt = new ReceivedDateTerm(ComparisonTerm.GE, startDate);

                Message[] msgs = inbox.search(rdt);
                for (Message msg : msgs) {
                    String msgSubject = msg.getSubject();
                    if (msgSubject != null
                            && msgSubject.toLowerCase().contains(subject.toLowerCase())) {
                        dateList.add(msg);
                    }
                }

                inbox.close(false);

                Message result = findGreaterDate(dateList, startDate);
                if (result != null) {
                    found = true;
                    break;
                }

                Thread.sleep(10000);
            }
        } finally {
            if (store.isConnected()) {
                store.close();
            }
        }

        return found;
    }

    private static Message findGreaterDate(ArrayList<Message> msgList, Date startDate) throws MessagingException {
        for (Message msg : msgList) {
            if (msg.getReceivedDate().after(startDate)) {
                return msg;
            }
        }
        return null;
    }
//    public static void main(String[] args) {
//        Map<String, Object> inputAttributes = new java.util.HashMap<String, Object>();
//        inputAttributes.put("Email", "saiteja.s@fireflink.com");
//        inputAttributes.put("App Password", "ucwr hboz cewj ydov");
//        inputAttributes.put("Expected Mail Time(dd/MM/yyyy HH:mm:ss)", getCurrentDateTime("dd/MM/yyyy HH:mm:ss"));
//        inputAttributes.put("BufferTime", 30);
//        inputAttributes.put("Subject of Mail", "gembox");
// 
//        NlpRequestModel nlpRequestModel = new NlpRequestModel();
//        nlpRequestModel.setAttributes(inputAttributes);
// 
//        VerifyGmailReceived programElement = new VerifyGmailReceived();
//        try {
//            NlpResponseModel nlpResponseModel = programElement.execute(nlpRequestModel);
//            System.out.println("Status: " + nlpResponseModel.getStatus());
//            System.out.println("Message: " + nlpResponseModel.getMessage());
//            System.out.println("IsMailReceived: " + nlpResponseModel.getAttributes().get("IsMailReceived"));
//        } catch (NlpException e) {
//            e.printStackTrace();
//        }
//    }
//    public static String getCurrentDateTime(String pattern) {
//        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//        return sdf.format(new Date());
//    }
}