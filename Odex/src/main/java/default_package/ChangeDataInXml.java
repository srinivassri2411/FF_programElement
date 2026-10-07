
package default_package;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ChangeDataInXml implements Nlp {
	@InputParams({ @InputParam(name = "File Path", type = "java.lang.String"),
			@InputParam(name = "List of Xpath", type = "java.lang.String"),
			@InputParam(name = "List of Changes", type = "java.lang.String") })
	@ReturnType(name = "File Path", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String filePath = (String) programElementsInput.get("File Path");
		String listofxpath = (String) programElementsInput.get("List of Xpath");
		String listofchanges = (String) programElementsInput.get("List of Changes");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = replaceDataInXML(filePath, listofxpath, listofchanges);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Changes are made in given");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to do changes in given");
			nlpResponseModel.setStatus(CommonConstants.fail);

		}

		nlpResponseModel.getAttributes().put("File Path", returnValue);
		return nlpResponseModel;
	}

	public static String replaceDataInXML(String filePath, String listOfXpath, String listOfChanges) throws Exception {
		File inputFile = new File(filePath);
		org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
		doc.getDocumentElement().normalize();

		XPath xPath = XPathFactory.newInstance().newXPath();
		String[] xpath = listOfXpath.split(">>");
		String[] values = listOfChanges.split(">>");

		if (xpath.length != values.length) {
			throw new IllegalArgumentException("Size mismatch between XPaths and changes");
		}

		for (int i = 0; i < xpath.length; i++) {
			Node node = (Node) xPath.evaluate(xpath[i], doc, XPathConstants.NODE);
			if (node == null) {
				System.out.println("Warning: No match for XPath: " + xpath[i]);
				continue;
			}
			node.setTextContent(values[i]);
		}
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		File tempFile = File.createTempFile("FF_Hapag Master_", ".txt");
		transformer.transform(new DOMSource(doc), new StreamResult(tempFile));
		return tempFile.getAbsolutePath();
	}
}