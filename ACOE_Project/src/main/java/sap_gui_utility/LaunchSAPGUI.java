
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

public class LaunchSAPGUI implements Nlp {
    @InputParams({@InputParam(name = "Exe Path", type = "java.lang.String")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String path = (String) programElementsInput.get("Exe Path");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  launchSAP(path);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Launched SAP GUI from this Path "+path);
              
          }
          catch(Exception e) {
        	  StringWriter writer =new StringWriter();
        	  e.printStackTrace(new PrintWriter(writer));
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to launch SAP "+writer);

          }

          return nlpResponseModel;
      }
    public static void launchSAP(String exePath) throws IOException, InterruptedException {
        String script =
            "On Error Resume Next\n" +
            "Dim WshShell\n" +
            "Set WshShell = CreateObject(\"WScript.Shell\")\n" +
            "WshShell.Run \"\"\"" + exePath + "\"\"\", 1, false\n" +
            "If Err.Number <> 0 Then\n" +
            "  WScript.Echo \"[ERROR] \" & Err.Description\n" +
            "Else\n" +
            "  WScript.Echo \"[INFO] SAP Logon launched successfully.\"\n" +
            "End If\n" +
            "WScript.Sleep 10000\n";

        File tempScript = File.createTempFile("LaunchSAP", ".vbs");
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
            System.out.println("Warning: Temporary VBS script not deleted: " + tempScript.getAbsolutePath());
        }

        if (exitCode != 0) {
            throw new RuntimeException("SAP launch script exited with error code: " + exitCode);
        }
    }

  } 