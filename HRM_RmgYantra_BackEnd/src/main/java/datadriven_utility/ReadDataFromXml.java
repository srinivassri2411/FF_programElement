package datadriven_utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class ReadDataFromXml implements Nlp {
	@InputParams({@InputParam(name = "XmlFile", type = "com.amazonaws.services.s3.model.S3ObjectInputStream"), @InputParam(name = "Key", type = "java.lang.String")})
	@ReturnType(name = "Value", type = "java.lang.String")

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
	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		InputStream xmlFile=(InputStream)attributes.get("XmlFile");
		String KeytoFetch = (String) attributes.get("Key");
		String Value="";
		File file = new File("Sample.xml");
		Exception exc = null;
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = xmlFile.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

		} catch (Exception e) {
			exc = e;
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("entry");
			

			for (int temp = 0; temp < nodeList.getLength(); temp++) {
				Node node = nodeList.item(temp);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element entryElement = (Element) node;
					String key = entryElement.getElementsByTagName("key").item(0).getTextContent();
					 Value = entryElement.getElementsByTagName("value").item(0).getTextContent();

					if (key.equals(KeytoFetch)) {
						nlpResponseModel.setStatus(CommonConstants.pass);
						nlpResponseModel.setMessage("Succesfully fetched the Key:" + key +", Value:"+ Value+" from Xml file");
						break;
					}


				}
			}
		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("failed to fetch the value from the key");
		}

		nlpResponseModel.getAttributes().put("Value", Value);
		return nlpResponseModel;
	}
} 
