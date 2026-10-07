package Windows;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetValueFromXML implements Nlp {
    @InputParams({ @InputParam(name = "XML", type = "java.lang.String"),
            @InputParam(name = "XML Path", type = "java.lang.String") })
    @ReturnType(name = "Data", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String xML = (String) programElementsInput.get("XML");
        String xMLPath = (String) programElementsInput.get("XML Path");
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            returnValue = getXMLData(xML, xMLPath);
            if (returnValue != null) {
                nlpResponseModel.setStatus(CommonConstants.pass);
                nlpResponseModel
                        .setMessage("Fetched the value from the given path " + xMLPath + " value is " + returnValue);
            } else {
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setMessage("Failed to fetch the from the given path " + xMLPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            nlpResponseModel.setMessage("Failed to fetch the from the given path " + e);
            nlpResponseModel.setStatus(CommonConstants.fail);
        }
        nlpResponseModel.getAttributes().put("Data", returnValue);
        return nlpResponseModel;
    }

    public static String getXMLData(String filePath, String xpathExpression)
            throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(filePath)));
        document.getDocumentElement().normalize();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new UniversalNamespaceResolver());
        XPathExpression expr = xpath.compile(xpathExpression);
        String result = expr.evaluate(document);
        if (result != null && !result.trim().isEmpty()) {
            return result.trim();
        } else {
            return null;
        }
    }

    public static class UniversalNamespaceResolver implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            switch (prefix) {
            case "md":
                return "urn:oasis:names:tc:SAML:2.0:metadata";
            case "ds":
                return "http://www.w3.org/2000/09/xmldsig#";
            case "xml":
                return javax.xml.XMLConstants.XML_NS_URI;
            default:
                return javax.xml.XMLConstants.NULL_NS_URI;
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException();
        }
    }

    
}