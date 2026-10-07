package specializedAutomation_Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class JSONToExcel implements Nlp {

    @InputParams({
            @InputParam(name = "Jar File", type = "java.lang.String"),
            @InputParam(name = "JSON File Stream", type = "java.io.InputStream")
    })
    @ReturnType(name = "string3", type = "java.lang.String")

    @Override
    public List<String> getTestParameters() throws NlpException {
        return new ArrayList<>();
    }

    @Override
    public StringBuilder getTestCode() throws NlpException {
        return new StringBuilder();
    }

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        NlpResponseModel response = new NlpResponseModel();
        Map<String, Object> attributes = nlpRequestModel.getAttributes();

        String jarPath = (String) attributes.get("Jar File");
        InputStream inputStream = (InputStream) attributes.get("JSON File Stream");

        try {
           
            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMHHmmss")
                    .withZone(ZoneOffset.systemDefault());

            String fileName = "JSONCopy" + formatter.format(now) + ".json";

            Path jsonFilePath = Path.of(System.getProperty("user.dir"), fileName);

            Files.copy(inputStream, jsonFilePath, StandardCopyOption.REPLACE_EXISTING);

            try (InputStream jarStream = new FileInputStream(new File(jarPath))) {
                jarCheck(fileName, jarStream);
            }

            Path outputFile = getLastModifiedFile();

            response.setStatus(CommonConstants.pass);
            response.setMessage("JSON parsed to Excel successfully: " + outputFile);

            response.getAttributes().put("string3", outputFile.toString());

        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));

            response.setStatus(CommonConstants.fail);
            response.setMessage("Failed to Parse JSON to Excel\n" + writer.toString());
        }

        return response;
    }

    public void jarCheck(String fileCreated, InputStream jarStream) throws Exception {

        String jarFileName = "JSON2XL_" + System.currentTimeMillis() + ".jar";
        Path jarFilePath = Path.of(System.getProperty("user.home"), jarFileName);

        // Copy jar safely
        Files.copy(jarStream, jarFilePath, StandardCopyOption.REPLACE_EXISTING);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c",
                "java -Xmx16000m -jar \"" + jarFilePath.toString() + "\" " + fileCreated
        );

        processBuilder.directory(new File(System.getProperty("user.dir")));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("Process exited with code: " + exitCode);

        try {
            Files.deleteIfExists(jarFilePath);
        } catch (Exception e) {
            System.out.println("Jar deletion failed (probably still in use): " + e.getMessage());
        }
    }

    private static Path getLastModifiedFile() throws Exception {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(System.getProperty("user.dir")),
                path -> Files.isRegularFile(path))) {

            Path lastFile = null;
            long lastModified = Long.MIN_VALUE;

            for (Path file : stream) {
                long time = Files.getLastModifiedTime(file).toMillis();

                if (time > lastModified) {
                    lastModified = time;
                    lastFile = file;
                }
            }

            return lastFile;
        }
    }

    public static void main(String[] args) throws Exception {

        NlpRequestModel model = new NlpRequestModel();

        model.getAttributes().put("Jar File", "C:\\Users\\User\\JSON2XL.jar");

        model.getAttributes().put("JSON File Stream",
                new FileInputStream("C:\\Users\\User\\Downloads\\Large_Data_mobikwik.json"));

        JSONToExcel obj = new JSONToExcel();
        System.out.println(obj.execute(model));

        Path testPath = Path.of(System.getProperty("user.home"), "ddssd.ewe");
        System.out.println("Test Path: " + testPath);
    }
}