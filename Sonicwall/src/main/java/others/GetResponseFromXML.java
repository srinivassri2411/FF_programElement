package others;

import java.io.StringReader;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetResponseFromXML implements Nlp {
    @InputParams({ @InputParam(name = "XML", type = "java.lang.String") })
    @ReturnType(name = "Prepared Certificate", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String XML = (String) programElementsInput.get("XML");
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(XML)));
            NodeList certNodes = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
            if (certNodes.getLength() == 0) {
                throw new RuntimeException("No certificate found.");
            }
            String certRaw = certNodes.item(0).getTextContent().replaceAll("\\s+", "");
            StringBuilder pem = new StringBuilder();
            pem.append("-----BEGIN CERTIFICATE-----\\n");
            for (int i = 0; i < certRaw.length(); i += 64) {
                int end = Math.min(i + 64, certRaw.length());
                pem.append(certRaw, i, end).append("\\n");
            }
            pem.append("-----END CERTIFICATE-----");
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Certification Creation Passed");
            returnValue = pem.toString();
        } catch (Exception e) {
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Certification Creation Failed" + e);
            e.printStackTrace();
        }
        nlpResponseModel.getAttributes().put("Prepared Certificate", returnValue);
        return nlpResponseModel;
    }

    public static void main(String[] args) throws NlpException {
        NlpRequestModel req = new NlpRequestModel();
        req.getAttributes().put("XML",
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <md:EntityDescriptor entityID="https://qa-ashwini-idp-okta-saml.portal-release.bnntest.com/v2/callback" xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata">
                            <md:IDPSSODescriptor WantAuthnRequestsSigned="false" protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
                                <md:KeyDescriptor use="signing">
                                    <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                                        <ds:X509Data>
                                            <ds:X509Certificate>MIIDqjCCApKgAwIBAgIGAZepBx/lMA0GCSqGSIb3DQEBCwUAMIGVMQswCQYDVQQGEwJVUzETMBEG
                        A1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5jaXNjbzENMAsGA1UECgwET2t0YTEU
                        MBIGA1UECwwLU1NPUHJvdmlkZXIxFjAUBgNVBAMMDXRyaWFsLTQwNzIwNDQxHDAaBgkqhkiG9w0B
                        CQEWDWluZm9Ab2t0YS5jb20wHhcNMjUwNjI1MjEzNzI1WhcNMzUwNjI1MjEzODI1WjCBlTELMAkG
                        A1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFjAUBgNVBAcMDVNhbiBGcmFuY2lzY28xDTAL
                        BgNVBAoMBE9rdGExFDASBgNVBAsMC1NTT1Byb3ZpZGVyMRYwFAYDVQQDDA10cmlhbC00MDcyMDQ0
                        MRwwGgYJKoZIhvcNAQkBFg1pbmZvQG9rdGEuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB
                        CgKCAQEAt1jqbFhOZzm+CxyT/AvQpt10NDd1izYGPjadwaB0NhMX+j1+kiTnJ7fmHaFdknQwIUMp
                        E7SZjoC5Qlg6eNDZbENR3or4t4qzASX6+/HUE1G95w4CbrfnX+wBRgcjLMtLbj5R8BIVS4Z42jcb
                        E6QxK7A4ojfEsXd7TZ1yqyXIWBJE450ROqvDuHgXqUpkLNIg61uGdX6eK+YSY48gERoQGXM0fqC3
                        CMkbiEtKqG0suA32YuxM5yEhcGyOGx0eKjfhUnJvcKNLNaYhInquErAsUZHdKTH6ry2ea+pJMhIV
                        e40gvZGn0Q6vNzmlmKmPlbopwLDil0abd7cbJ1JMGexXZQIDAQABMA0GCSqGSIb3DQEBCwUAA4IB
                        AQAEcKqTsodhAbSLK5qibw35vMeWyJG/9XuGoeEV9f4VvFpsA+Gc+JtLqLvun+hbMIUp2z/meCti
                        p+cCljLHez82mgtbl1yR8s8kV/8+LMPWLYwVaNJU6vS5/OP8ElBI+3BWQz5yPTo+xFei5HqGAAKt
                        8+TUx4j9agtFbLn6KmzWr4HqSg3BZYA1tKVjpk+ajXaWvGKLQWPgcUKTGhKSz+765bPr9B189QRy
                        B5/kzK1m730HYqN5c/e+Qn3HTwzNh3uZUNDk3t6RAZc5QPWaO7LFDqrWQ7DvDxhvAX5XzCK+ID3a
                        2gnsaWRHKd6IILGaUXRt2VwrHmg0lwhDTRIYgbTC</ds:X509Certificate>
                                        </ds:X509Data>
                                    </ds:KeyInfo>
                                </md:KeyDescriptor>
                                <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>
                                <md:SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="https://trial-4072044.okta.com/app/trial-4072044_qaashwiniidpoktasamltrustprovidersaml20app_1/exksobmqcfLTBXtnp697/sso/saml"/>
                                <md:SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" Location="https://trial-4072044.okta.com/app/trial-4072044_qaashwiniidpoktasamltrustprovidersaml20app_1/exksobmqcfLTBXtnp697/sso/saml"/>
                            </md:IDPSSODescriptor>
                        </md:EntityDescriptor>
                                        """);
        System.out.println(new GetResponseFromXML().execute(req));
    }
}