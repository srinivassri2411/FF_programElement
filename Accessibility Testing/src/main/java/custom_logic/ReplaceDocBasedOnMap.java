
package custom_logic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;


import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class ReplaceDocBasedOnMap implements Nlp {
	
	@InputParams({
		@InputParam(name = "Doc File", type = "String"),
		@InputParam(name = "Is Mapping Required(true, false)", type = "String"),
		@InputParam(name = "Mapping Data(if true)", type = "Map")
	})
	@ReturnType(name = "Output", type = "String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String textFile = (String) programElementsInput.get("Doc File");
		String isMappingReq = (String) programElementsInput.get("Is Mapping Required(true, false)");
		Map<String,String> mappingData=new LinkedHashMap<String, String>();
		if(StringUtils.equalsIgnoreCase(isMappingReq,"true")) {
		 mappingData = (Map<String,String>) programElementsInput.get("Mapping Data(if true)");
		}
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {

			textFile=textFile.replace("\\", "/");
			String returnValue = replaceCaseInsensitive(textFile, mappingData);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Replace Text Based On Map");
			nlpResponseModel.getAttributes().put("Output", returnValue);
		}
		catch(Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Replace Text Based On Map, Reason for Failure ==> \n"+sw.toString());
		}

		return nlpResponseModel;
	}


	 public static String replaceCaseInsensitive(String textFile, Map<String, String> mapper) throws IOException {
	        String text=readDocx(textFile);
		 	String[] words = text.split(" ");
	        StringBuilder result = new StringBuilder();

	        for (String word : words) {
	            String lowerCaseWord = word.toLowerCase();
	            boolean replaced = false;

	            for (Map.Entry<String, String> entry : mapper.entrySet()) {
	                if (lowerCaseWord.equals(entry.getKey().toLowerCase())) {
	                    result.append(entry.getValue()).append(" ");
	                    replaced = true;
	                    break;
	                }
	            }

	            if (!replaced) {
	                result.append(word).append(" ");
	            }
	        }
	        
	        try (XWPFDocument doc = new XWPFDocument()) {
	            XWPFParagraph paragraph = doc.createParagraph();
	            paragraph.createRun().setText(result.toString());
	            textFile=textFile.substring(0, textFile.lastIndexOf("."));
	            try (FileOutputStream out = new FileOutputStream(textFile+"/Replaced_Text.docx")) {
	                doc.write(out);
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return textFile+"/Replaced_Text.docx";
	    }
	 
	 public static String readDocx(String filePath) throws IOException {
	        FileInputStream fis = new FileInputStream(filePath);
	        XWPFDocument document = new XWPFDocument(fis);
	        StringBuilder fullText = new StringBuilder();
	        List<XWPFParagraph> paragraphs = document.getParagraphs();

	        for (XWPFParagraph para : paragraphs) {
	            fullText.append(para.getText()).append("\n");
	        }

	        fis.close();
	        return fullText.toString();
	    }

}
