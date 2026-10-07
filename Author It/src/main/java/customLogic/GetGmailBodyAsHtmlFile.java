package customLogic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;

public class GetGmailBodyAsHtmlFile implements Nlp {

    static File mailHtmlFile;

    @InputParams({ @InputParam(name = "Email", type = "java.lang.String"),
            @InputParam(name = "App Password", type = "java.lang.String"),
            @InputParam(name = "Expected Mail Time(dd/MM/yyyy HH:mm:ss)", type = "java.lang.String"),
            @InputParam(name = "BufferTime", type = "java.lang.Integer"),
            @InputParam(name = "Subject of Mail", type = "java.lang.String") })
    @ReturnType(name = "HtmlFilePath", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String email = (String) programElementsInput.get("Email");
        String appPwd = (String) programElementsInput.get("App Password");
        String expectedMailTime = (String) programElementsInput.get("Expected Mail Time(dd/MM/yyyy HH:mm:ss)");
        Integer bufferTime = (Integer) programElementsInput.get("BufferTime");
        String subjectofMail = (String) programElementsInput.get("Subject of Mail");
        String returnValue = null;
        
        
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            File htmlFile = getMailBody(email, appPwd, expectedMailTime, bufferTime, subjectofMail);
            if (htmlFile != null) {
                returnValue = htmlFile.getAbsolutePath();
            } else {
                throw new RuntimeException();
            }
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("HTML file path of mail is at " + returnValue);
        } catch (Exception e) {            
            

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("No mail found with subject :" + subjectofMail+" "+stringWriter.toString());
            
        }
        nlpResponseModel.getAttributes().put("HtmlFilePath", returnValue);
        return nlpResponseModel;
    }
    public static File getMailBody(String email, String appPwd, String formattedDateTime, int bufferTime,
            String subject) throws MessagingException, ParseException, IOException, InterruptedException {
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

        for (int i = 0; i < bufferTime; i += 10) {
            System.out.println(i);
            ArrayList<Message> dateList = new ArrayList<Message>();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            ReceivedDateTerm rdt = new ReceivedDateTerm(ComparisonTerm.GE, startDate);
            SubjectTerm subTerm = new SubjectTerm(subject);
            SearchTerm finalTerms = new AndTerm(new SearchTerm[] { rdt, subTerm });
            Message[] msgs = inbox.search(finalTerms);
            for (Message msg : msgs) {
                dateList.add(msg);
                
            }
            Message result = findGreaterDate(dateList, startDate);
            if (result != null) {
                Object content = result.getContent();
                if (content instanceof String) {
                    mailHtmlFile = generateHtmlFile(content.toString());
                } else if (content instanceof MimeMultipart) {
                    MimeMultipart multipart = (MimeMultipart) content;

                    for (int j = 0; j < multipart.getCount(); j++) {
                        BodyPart bodyPart = multipart.getBodyPart(j);

                        if (bodyPart.isMimeType("text/html")) {
                            String htmlContent = (String) bodyPart.getContent();
                            mailHtmlFile = generateHtmlFile(htmlContent);
                        }
                    }
                }
                break;
            }
            Thread.sleep(10000);
        }
        return mailHtmlFile;
    }
    public static Message findGreaterDate(ArrayList<Message> msgList, Date startDate) throws MessagingException {
        for (Message msg : msgList) {
            if (msg.getReceivedDate().after(startDate)) {
                return msg;
            }
        }
        return null;
    }
    public static File generateHtmlFile(String htmlContent) {
        
        String tempDir = System.getProperty("java.io.tmpdir");

        String fileName = "generated_html_" + System.currentTimeMillis() + ".html";
        File htmlFile = new File(tempDir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile))) {
            
            writer.write(htmlContent);
            return htmlFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String fetchTheOTp(String msg) {
        Document html = Jsoup.parse(msg);
        return html.getElementsByClass("otp_text").get(0).text();
    }

}