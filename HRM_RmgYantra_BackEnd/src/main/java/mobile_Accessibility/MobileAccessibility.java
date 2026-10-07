package mobile_Accessibility;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
public class MobileAccessibility implements Nlp {
static  Path tempDir = null;
public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          String filePath=null;
try {
              String rawUrl = "https://github.com/Swaraj-N/mobileAccessibilityPyFile/releases/download/v1.0/mobileAccessibility.py";
        filePath=executePythonFromGitUrl(rawUrl);
        System.out.println(filePath);
              nlpResponseModel.setOutputLocalFilePaths(List.of(filePath));
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("HTML Report is generated for Mobile Accessibility");
          }
catch(Exception e) {
              StringWriter sw = new StringWriter();
           e.printStackTrace(new PrintWriter(sw));
           String exceptionAsString = sw.toString();
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to generate HTML Report for Mobile Accessibility : "+exceptionAsString);
          }
finally {
              deleteDirectoryRecursively(tempDir);
          }
return nlpResponseModel;
      }    /**
       * Downloads a Python file using Apache Commons IO, executes it with the system Python,
       * captures its stdout (expected: one file path), and deletes the temp directory.
       *
       * @param rawFileUrl Raw Git URL (e.g., https://raw.githubusercontent.com/user/repo/branch/path/mobileAssibility3.py)
       * @return Trimmed stdout from Python (expected: report path)
       */
public static String executePythonFromGitUrl(String rawFileUrl) throws Exception {
if (rawFileUrl == null || rawFileUrl.isBlank()) {
throw new IllegalArgumentException("rawFileUrl must be non-empty");
          }
// 1️⃣ Create a temp directory
          Path tempDir = Files.createTempDirectory("py_exec_");
          Path scriptFile = tempDir.resolve("dummy.py");
          File script = scriptFile.toFile();
try {
              System.out.println(script);
// 2️⃣ Download the Python script directly
              FileUtils.copyURLToFile(new java.net.URL(rawFileUrl), script);
// 3️⃣ Run Python
              String pythonCmd = "python"; // change to "python3" if needed
              ProcessBuilder pb = new ProcessBuilder(pythonCmd, script.getAbsolutePath());
              pb.directory(tempDir.toFile());
              pb.environment().put("PYTHONIOENCODING", "utf-8");
              pb.redirectErrorStream(true); // merge stderr -> stdout
              Process process = pb.start();
// 4️⃣ Capture stdout
              String output;
try (BufferedReader reader = new BufferedReader(
new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                  output = reader.lines().collect(Collectors.joining("\n"));
              }
int exitCode = process.waitFor();
if (exitCode != 0) {
throw new RuntimeException("Python failed (exit=" + exitCode + "):\n" + output);
              }
// 5️⃣ Return the printed file path (last non-empty line)
              String finalOutput = output.lines()
                      .filter(line -> !line.trim().isEmpty())
                      .reduce((first, second) -> second)
                      .orElse("")
                      .trim();
return finalOutput;
          } finally {
// 6️⃣ Cleanup temp dir
//              deleteDirectoryRecursively(tempDir);
          }
      }
private static void deleteDirectoryRecursively(Path path) {
try {
if (path != null && Files.exists(path)) {
                  Files.walk(path)
                          .sorted((a, b) -> b.compareTo(a))
                          .forEach(p -> {
try {
                                  Files.deleteIfExists(p);
} catch (Exception ignored) {}
});
}
} catch (Exception ignored) {}
}
public static void main(String[] args) throws NlpException {
    NlpRequestModel NlpRequestModel = new NlpRequestModel();
new MobileAccessibility().execute(NlpRequestModel);
   }

}