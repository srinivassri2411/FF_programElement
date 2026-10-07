package customLogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class GetDocxContentAsString implements Nlp {

    @InputParams({ @InputParam(name = "Docx File Path", type = "java.lang.String") })
    @ReturnType(name = "DocxContent", type = "java.lang.String")

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String docxFilePath = (String) programElementsInput.get("Docx File Path");
        String returnValue = null;

        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        try {
            returnValue = getDocxContent(docxFilePath);
            if (returnValue == null || returnValue.trim().isEmpty()) {
                throw new RuntimeException("No content found in docx file");
            }
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.setMessage("Docx content fetched successfully");
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);

            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage("Failed to fetch content from docx: " + docxFilePath + " " + stringWriter.toString());
        }
        nlpResponseModel.getAttributes().put("DocxContent", returnValue);
        return nlpResponseModel;
    }

    public static String getDocxContent(String docxFilePath) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(docxFilePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<IBodyElement> elements = document.getBodyElements();
            for (IBodyElement element : elements) {
                if (element instanceof XWPFParagraph) {
                    String text = ((XWPFParagraph) element).getText();
                    if (text != null && !text.trim().isEmpty()) {
                        sb.append(text).append("\n");
                    }
                } else if (element instanceof XWPFTable) {
                    appendTable((XWPFTable) element, sb);
                }
            }
        }
        return sb.toString().trim();
    }

    private static void appendTable(XWPFTable table, StringBuilder sb) {
        List<XWPFTableRow> rows = table.getRows();
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            StringBuilder rowText = new StringBuilder();
            for (int c = 0; c < cells.size(); c++) {
                rowText.append(cells.get(c).getText().trim());
                if (c < cells.size() - 1) {
                    rowText.append(" | ");
                }
            }
            sb.append(rowText).append("\n");
        }
    }

//    public static void main(String[] args) {
//        Map<String, Object> inputAttributes = new java.util.HashMap<String, Object>();
//        inputAttributes.put("Docx File Path", "C:\\Users\\User\\AuthorIt\\Copy of Copy of Test Case — Gembox Word Publishing — All test cases.docx"); // change to your .docx file path
//
//        NlpRequestModel nlpRequestModel = new NlpRequestModel();
//        nlpRequestModel.setAttributes(inputAttributes);
//
//        GetDocxContentAsString programElement = new GetDocxContentAsString();
//        try {
//            NlpResponseModel nlpResponseModel = programElement.execute(nlpRequestModel);
//            System.out.println("Status: " + nlpResponseModel.getStatus());
//            System.out.println("Message: " + nlpResponseModel.getMessage());
//            System.out.println("DocxContent:\n" + nlpResponseModel.getAttributes().get("DocxContent"));
//        } catch (NlpException e) {
//            e.printStackTrace();
//        }
//    }
}