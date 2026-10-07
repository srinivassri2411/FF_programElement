
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

public class PressEnterKeyInSAPGUI implements Nlp {
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  pressEnter();
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Successfully pressed Enter key in SAP");
          }
          catch(Exception e) {
        	  StringWriter writer=new StringWriter();
        	  e.printStackTrace(new PrintWriter(writer));
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to press Enter Key "+writer);

          }

          return nlpResponseModel;
      }
      
      public static void pressEnter() throws IOException, InterruptedException {
          File tempScript = File.createTempFile("SendEnterKey", ".vbs");

          String script = 
          	    "On Error Resume Next\n" +
          	    "Dim SapGuiAuto, application, connection, session\n" +
          	    "Set SapGuiAuto = GetObject(\"SAPGUI\")\n" +
          	    "If SapGuiAuto Is Nothing Then\n" +
          	    "  WScript.Echo \"[ERROR] SAPGUI scripting engine not found.\"\n" +
          	    "  WScript.Quit 1\n" +
          	    "End If\n" +
          	    "Set application = SapGuiAuto.GetScriptingEngine\n" +
          	    "If application Is Nothing Then\n" +
          	    "  WScript.Echo \"[ERROR] SAP application scripting engine not found.\"\n" +
          	    "  WScript.Quit 1\n" +
          	    "End If\n" +
          	    "Dim i\n" +
          	    "For i = 1 To 20\n" +
          	    "  Set connection = application.Children(0)\n" +
          	    "  If Not connection Is Nothing Then Exit For\n" +
          	    "  WScript.Sleep 500\n" +
          	    "Next\n" +
          	    "If connection Is Nothing Then\n" +
          	    "  WScript.Echo \"[ERROR] No SAP connection found.\"\n" +
          	    "  WScript.Quit 1\n" +
          	    "End If\n" +
          	    "For i = 1 To 20\n" +
          	    "  Set session = connection.Children(0)\n" +
          	    "  If Not session Is Nothing Then Exit For\n" +
          	    "  WScript.Sleep 500\n" +
          	    "Next\n" +
          	    "If session Is Nothing Then\n" +
          	    "  WScript.Echo \"[ERROR] No SAP session found.\"\n" +
          	    "  WScript.Quit 1\n" +
          	    "End If\n" +
          	    "session.findById(\"wnd[0]\").sendVKey 0\n" +
          	    "If Err.Number <> 0 Then\n" +
          	    "  WScript.Echo \"[ERROR] \" & Err.Description\n" +
          	    "  WScript.Quit 1\n" +
          	    "Else\n" +
          	    "  WScript.Echo \"[INFO] Enter key sent successfully.\"\n" +
          	    "End If\n";


          try (BufferedWriter out = new BufferedWriter(new FileWriter(tempScript))) {
              out.write(script);
          }

          ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", tempScript.getAbsolutePath());
          pb.redirectErrorStream(true);

          Process p = pb.start();

          try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
              String line;
              while ((line = reader.readLine()) != null) {
                  if (line.startsWith("[ERROR]")) {
                      System.err.println("VBScript Error: " + line);
                  } else {
                      System.out.println("Log: " + line);
                  }
              }
          }

          p.waitFor();
          boolean deleted = tempScript.delete();
          if (!deleted) {
              System.out.println("Warning: temp VBS file not deleted: " + tempScript.getAbsolutePath());
          }
      }

  } 