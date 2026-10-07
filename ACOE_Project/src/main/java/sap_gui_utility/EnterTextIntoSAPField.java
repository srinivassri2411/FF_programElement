
package sap_gui_utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class EnterTextIntoSAPField implements Nlp {
    @InputParams({@InputParam(name = "Input Element", type = "java.lang.String"),@InputParam(name = "Input Text", type = "java.lang.String")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String inputElement = (String) programElementsInput.get("Input Element");
          String inputText = (String) programElementsInput.get("Input Text");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  enterText(inputElement, inputText);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Successfully entered Text into provided field");
          }
          catch(Exception e) {
        	  StringWriter writer =new StringWriter();
        	  e.printStackTrace(new PrintWriter(writer));
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to enter text into provided field "+writer);

          }

          return nlpResponseModel;
      }
    public static void enterText(String elementId, String text) throws IOException, InterruptedException {
        File tempScript = File.createTempFile("EnterText", ".vbs");

        String script =
            "On Error Resume Next\n" +
            "Dim SapGuiAuto, application, connection, session\n" +
            "Set SapGuiAuto = GetObject(\"SAPGUI\")\n" +
            "Set application = SapGuiAuto.GetScriptingEngine\n" +
            "Set connection = application.Children(0)\n" +
            "Set session = connection.Children(0)\n" +
            "session.findById(\"" + elementId + "\").Text = \"\"\n" +  
            "session.findById(\"" + elementId + "\").Text = \"" + text + "\"\n" +
            "If Err.Number <> 0 Then\n" +
            "  WScript.Echo \"[ERROR] \" & Err.Description\n" +
            "Else\n" +
            "  WScript.Echo \"[INFO] Text entered successfully into: " + elementId + "\"\n" +
            "End If\n";

        try (BufferedWriter out = new BufferedWriter(new FileWriter(tempScript))) {
            out.write(script);
        }

        ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", tempScript.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[ERROR]")) {
                    System.err.println("VBScript Error: " + line);
                } else {
                    System.out.println("Log: " + line);
                }
            }
        }

        int exitCode = process.waitFor();

        boolean deleted = tempScript.delete();
        if (!deleted) {
            System.out.println("Warning: temp VBS file not deleted: " + tempScript.getAbsolutePath());
        }

        if (exitCode != 0) {
            throw new RuntimeException("VBScript exited with code " + exitCode);
        }
    }
  } 