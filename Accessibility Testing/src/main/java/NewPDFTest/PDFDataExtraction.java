
package NewPDFTest;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class PDFDataExtraction implements Nlp {
    @InputParams({@InputParam(name = "PDF File Path", type = "java.lang.String")})
    @ReturnType(name = "Python File Path", type = "java.lang.String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String pdfPath = (String) programElementsInput.get("PDF File Path");
          String returnValue=null;

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              pdfPath = pdfPath.trim().replace("\\", "/");
              String systemTempDir = System.getProperty("java.io.tmpdir");
              
              // Define your own folder inside temp dir
              File myTempDir = new File(systemTempDir, "python_script_dir");
              
              // Create directory if not exists
              if (!myTempDir.exists()) {
                  boolean created = myTempDir.mkdirs();
                  if (!created) {
                      nlpResponseModel.setStatus(CommonConstants.fail);
                      nlpResponseModel.setMessage("Failed to create temp directory: " + myTempDir.getAbsolutePath());
                  }
              }

              // Define python file inside your temp dir
              File pythonFile = new File(myTempDir, "script.py");

              // Python code with placeholders
              String pythonCode = """
              		import fitz  # PyMuPDF


              		def extract_text_with_field_placeholders(pdf_path):
              		    doc = fitz.open(pdf_path)
              		    full_result = []

              		    for page_num, page in enumerate(doc):
              		        items = []

              		        for block in page.get_text("dict")["blocks"]:
              		            block_text = ""
              		            min_x = float('inf')
              		            min_y = float('inf')

              		            for line in block.get("lines", []):
              		                for span in line.get("spans", []):
              		                    block_text += span["text"] + "\\n"
              		                    min_x = min(min_x, span["bbox"][0])
              		                    min_y = min(min_y, span["bbox"][1])

              		            block_text = block_text.strip()
              		            if block_text:
              		                items.append({
              		                    "text": block_text,
              		                    "x": min_x,
              		                    "y": min_y
              		                })

              		        sorted_items = sorted(items, key=lambda i: (round(i["y"], 1), i["x"]))
              		        page_text = "\\n".join(item["text"] for item in sorted_items)
              		        full_result.append(f"\\n--- Page {page_num + 1} ---\\n{page_text}")

              		    return "\\n".join(full_result)


              		pdf_path = "%s"
              		# pdf_path = "D:/Testyantra/ACOE Solutions Team/project/CIBC/Reading Order/8248issue.pdf"
              		output = extract_text_with_field_placeholders(pdf_path)
              		print("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
              		print(output)
              		print("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
                      """.formatted(pdfPath);
              try (FileWriter writer = new FileWriter(pythonFile)) {
                  writer.write(pythonCode);
                returnValue= pythonFile.getAbsolutePath();
              } catch (IOException e) {
                  e.printStackTrace();
              }
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage(pdfPath+" updated at Python File "+returnValue);
          }
          catch(Exception e) {
        		StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to Create Python File "+exceptionAsString);

          }

          // Your program element business logic ends here ...
          nlpResponseModel.getAttributes().put("Python File Path", returnValue);
          return nlpResponseModel;
      }

  } 