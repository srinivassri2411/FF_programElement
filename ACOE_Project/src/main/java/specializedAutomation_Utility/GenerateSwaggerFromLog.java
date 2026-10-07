
package specializedAutomation_Utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GenerateSwaggerFromLog implements Nlp {
    @InputParams({@InputParam(name = "Log File Path", type = "java.lang.nlp.filepath")})
    

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          byte[] logFilePath = (byte[]) programElementsInput.get("Log File Path");
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	
        		String path=generateSwaggerFromLog(logFilePath);
nlpResponseModel.setOutputLocalFilePaths(List.of(path));
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Swagger successfully generated with the Log File Provided");
          }
          catch(Exception e) {
        	  
        	  StringWriter sw = new StringWriter();
  			e.printStackTrace(new PrintWriter(sw));
  			String exceptionAsString = sw.toString();
  			//log.info(exceptionAsString);
  			nlpResponseModel.setStatus(CommonConstants.fail);
        	  nlpResponseModel.setMessage("Failed to generate swagger : "+exceptionAsString);
          }

          return nlpResponseModel;
      }
    public static String generateSwaggerFromLog(byte[] logBytes) throws Exception {

        Path tempDir = Files.createTempDirectory("swagger_gen_");
        Path tempLog = tempDir.resolve("input.log");
        Path tempPy = Files.createTempFile("log_to_swagger_", ".py");

        Files.write(tempLog, logBytes);

        String pythonCode = """
import re
import json
import sys
import os
from urllib.parse import urlparse, parse_qs

LOG_FILE = sys.argv[1]
OUTPUT_DIR = os.path.dirname(LOG_FILE)
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "swagger.json")

PATTERNS = {
    "log_prefix": re.compile(r"\\[([a-f0-9]{6,})\\]\\s+\\[([a-f0-9]{6,})\\]"),
    "any_url": re.compile(r"(https?://[^\\s\\"'>]+)"),
    "http_status": re.compile(r"\\bStatus\\s*=?\\s*(\\d{3})\\b"),
    "controller_payload": re.compile(r"(\\w+)\\s+Controller\\s+with\\s+data:\\s*(\\{[\\s\\S]*?\\})"),
    "controller_response": re.compile(r"(\\w+)\\s+Controller\\s+Response:\\s*(\\{[\\s\\S]*?\\})"),
    "mapped_url": re.compile(r"Mapped URL path \\[(.*?)\\] onto method \\[(.*?)\\]"),
    "validation_error": re.compile(r"Validation failed.*controller\\.(\\w+)\\.(\\w+)")
}

def extract_path_and_query(full_url):
    parsed = urlparse(full_url)
    return parsed.path if parsed.path else "/", parsed.query

def get_swagger_template():
    return {
        "openapi": "3.0.0",
        "info": {
            "title": "Generic Log API Extraction",
            "version": "1.0.0"
        },
        "paths": {},
        "components": {"schemas": {}}
    }

swagger = get_swagger_template()
paths = swagger["paths"]
trace_to_path = {}

with open(LOG_FILE, "r", encoding="utf-8", errors="ignore") as f:
    lines = f.readlines()

for line in lines:
    line = line.strip()
    if not line:
        continue

    trace_id = None
    m = PATTERNS["log_prefix"].search(line)
    if m:
        trace_id = m.group(1)

    m = PATTERNS["controller_payload"].search(line)
    if m:
        name, js = m.group(1), m.group(2)
        path = f"/api/{name.lower()}"
        if trace_id:
            trace_to_path[trace_id] = path
        try:
            payload = json.loads(js)
            paths.setdefault(path, {})
            paths[path].setdefault("post", {
                "summary": f"{name} Controller API",
                "requestBody": {
                    "content": {
                        "application/json": {
                            "schema": {"type": "object"},
                            "example": payload
                        }
                    }
                },
                "responses": {"200": {"description": "Success"}}
            })
        except:
            pass
        continue

    m = PATTERNS["controller_response"].search(line)
    if m and trace_id:
        js = m.group(2)
        path = trace_to_path.get(trace_id)
        if path and "post" in paths.get(path, {}):
            try:
                resp = json.loads(js)
                code = str(resp.get("statusCodeValue", "200"))
                paths[path]["post"]["responses"].setdefault(code, {
                    "description": resp.get("statusCode", "Response")
                })
            except:
                pass
        continue

    m = PATTERNS["mapped_url"].search(line)
    if m:
        p, sig = m.group(1), m.group(2).lower()
        paths.setdefault(p, {})
        method = "get"
        if "post" in sig: method = "post"
        elif "put" in sig: method = "put"
        elif "delete" in sig: method = "delete"
        paths[p].setdefault(method, {
            "summary": "Detected Spring Mapping",
            "responses": {"200": {"description": "OK"}}
        })
        continue

    m = PATTERNS["validation_error"].search(line)
    if m:
        c, mth = m.group(1), m.group(2)
        p = f"/api/{c.lower()}/{mth}"
        paths.setdefault(p, {})
        paths[p].setdefault("post", {
            "summary": "Validation Error Endpoint",
            "responses": {"400": {"description": "Validation Failed"}}
        })
        continue

    m = PATTERNS["any_url"].search(line)
    if m:
        url = m.group(1).rstrip(".,\\"');")
        p, q = extract_path_and_query(url)
        paths.setdefault(p, {})
        paths[p].setdefault("get", {"parameters": [], "responses": {}})
        if q:
            for k, v in parse_qs(q).items():
                if not any(x["name"] == k for x in paths[p]["get"]["parameters"]):
                    paths[p]["get"]["parameters"].append({
                        "name": k,
                        "in": "query",
                        "schema": {"type": "string", "example": v[0] if v else ""}
                    })
        sm = PATTERNS["http_status"].search(line)
        if sm:
            paths[p]["get"]["responses"][sm.group(1)] = {"description": "Observed HTTP status"}

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(swagger, f, indent=2)

print(os.path.abspath(OUTPUT_FILE))
""";

        Files.writeString(tempPy, pythonCode);

        String pythonCmd = detectPython();

        ProcessBuilder pb = new ProcessBuilder(
                pythonCmd,
                tempPy.toAbsolutePath().toString(),
                tempLog.toAbsolutePath().toString()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        String jsonPath = null;

        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    jsonPath = line.trim();
                }
            }
        }

        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        Files.deleteIfExists(tempPy);
        Files.deleteIfExists(tempLog);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Python execution timed out");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("Python execution failed");
        }

        if (jsonPath == null || !Files.exists(Paths.get(jsonPath))) {
            throw new RuntimeException("Swagger JSON not generated");
        }

        return jsonPath;
    }

    private static String detectPython() {
        if (isAvailable("python")) return "python";
        if (isAvailable("python3")) return "python3";
        throw new RuntimeException("Python not available in PATH");
    }

    private static boolean isAvailable(String cmd) {
        try {
            Process p = new ProcessBuilder(cmd, "--version").start();
            return p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
  } 