package Business_Logics;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ToFetchCertificateDetails implements Nlp {
    @InputParams({@InputParam(name = "URL", type = "java.lang.String"),
    	@InputParam(name = "UserName", type = "java.lang.String"),
    	@InputParam(name = "Password", type = "java.lang.String")})
    @ReturnType(name = "Sum of Two numbers", type = "java.util.Map")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String URL = (String) programElementsInput.get("URL");
          String UserName = (String) programElementsInput.get("UserName");

          String Password = (String) programElementsInput.get("Password");

          Map<String, Map<String, String>> certDetails=null;
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  
        	  Authenticator.setDefault(new Authenticator() {
                  @Override
                  protected PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(UserName, Password.toCharArray());
                  }
              });
        	  
        	  
              certDetails = getCertificateDetails(URL);

              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Successfully fetched all the Certificate Details");
          }
          catch(Exception e) {
        	
  			StringWriter sw = new StringWriter();
  			e.printStackTrace(new PrintWriter(sw));
  			String exceptionAsString = sw.toString();
  			// Log.info(exceptionAsString);
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed fetch the Details "+exceptionAsString);
          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Sum of Two numbers", certDetails);
          return nlpResponseModel;
      }
   
    public static Map<String,String> extractCertInfo(X509Certificate cert) throws Exception {
        Map<String,String> info = new LinkedHashMap<>();

        // Basic fields
        info.put("SubjectDN", cert.getSubjectX500Principal().getName());
        info.put("IssuerDN", cert.getIssuerX500Principal().getName());
        info.put("SerialNumber", cert.getSerialNumber().toString());
        info.put("NotBefore", cert.getNotBefore().toString());
        info.put("NotAfter", cert.getNotAfter().toString());
        info.put("SignatureAlgorithm", cert.getSigAlgName());
        info.put("Version", Integer.toString(cert.getVersion()));

        // Parse SubjectDN into RDNs
        X500Principal subj = cert.getSubjectX500Principal();
        LdapName subjName = new LdapName(subj.getName(X500Principal.RFC2253));
        for (Rdn rdn : subjName.getRdns()) {
            String type = rdn.getType();
            String value = rdn.getValue().toString();
            // e.g., “CN”, “O”, “OU”, “C”, “DNQUALIFIER”, etc
            info.put("Subject_" + type, value);
        }

        // Similarly for IssuerDN
        X500Principal issuer = cert.getIssuerX500Principal();
        LdapName issuerName = new LdapName(issuer.getName(X500Principal.RFC2253));
        for (Rdn rdn : issuerName.getRdns()) {
            String type = rdn.getType();
            String value = rdn.getValue().toString();
            info.put("Issuer_" + type, value);
        }

        return info;
    }
    public static Map<String, Map<String, String>> getCertificateDetails(String urlString) throws Exception {
        URL url = new URL(urlString);
        String hostname = url.getHost();
        int port = url.getPort() == -1 ? 443 : url.getPort();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        }}, new java.security.SecureRandom());

        SSLSocketFactory factory = sslContext.getSocketFactory();

        try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
            socket.startHandshake();
            SSLSession session = socket.getSession();

            java.security.cert.Certificate[] peerCerts = session.getPeerCertificates();
            if (peerCerts == null || peerCerts.length == 0) {
                throw new Exception("No peer certificates found");
            }

            X509Certificate serverCert = (X509Certificate) peerCerts[0];
            X509Certificate issuerCert;

            if (peerCerts.length > 1) {
                issuerCert = (X509Certificate) peerCerts[1];
            } else {
                // Only one certificate was sent; use the same cert as issuer (or set issuerCert = serverCert)
                issuerCert = serverCert;
            }

             System.out.println(serverCert);
            Map<String, Map<String, String>> allCertInfo = new LinkedHashMap<>();
            allCertInfo.put("serverCert", extractCertInfo(serverCert));
            allCertInfo.put("issuerCert", extractCertInfo(issuerCert));
            return allCertInfo;
        }
    }

    // Main method that returns a Map of maps with server and issuer cert info
    public static Map<String, Map<String, String>> getCertificateDetailsSS(String urlString) throws Exception {
        URL url = new URL(urlString);
        String hostname = url.getHost();
        int port = url.getPort() == -1 ? 443 : url.getPort();

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
            socket.startHandshake();
            SSLSession session = socket.getSession();

            X509Certificate serverCert = (X509Certificate) session.getPeerCertificates()[0];

            X509Certificate issuerCert;
            if (serverCert.getIssuerX500Principal().getName().equals(serverCert.getSubjectX500Principal().getName())) {
                issuerCert = serverCert;
            } else {
                issuerCert = (X509Certificate) session.getPeerCertificates()[1];
            }

            Map<String, Map<String, String>> allCertInfo = new LinkedHashMap<>();
            allCertInfo.put("serverCert", extractCertInfo(serverCert));
            allCertInfo.put("issuerCert", extractCertInfo(issuerCert));

            return allCertInfo;
        }
    }
    
    public static void main(String[] args) throws Exception {
   	 Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("sonicwalluser1@skybeyo.testinator.com", "FireFlink@1234".toCharArray());
            }
        });
   	
   	
   	
   	
   	//   String testUrl = "https://testsample.sampledomain04213.qa-automation-testorg1-at.gcp.bnntest.com";
       String testUrl = "https://subdomain.random-test6635.qa-automation-testorg1-at.gcp.bnntest.com/";

       Map<String, Map<String, String>> certDetails = getCertificateDetails(testUrl);

       certDetails.forEach((key, certMap) -> {
           System.out.println("=== " + key + " ===");
           certMap.forEach((field, value) -> System.out.println(field + ": " + value));
           System.out.println();
       });
   }

  } 