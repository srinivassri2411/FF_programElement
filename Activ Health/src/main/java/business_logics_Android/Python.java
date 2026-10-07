package business_logics_Android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class Python implements Nlp {
	@InputParams({ @InputParam(name = "Python Code File", type = "java.lang.String"), 
		@InputParam(name = "External Libraries and Versions", type = "java.lang.String"), 
		@InputParam(name = "Log File Directory", type = "java.lang.String"),
		@InputParam(name = "Python Library Path", type = "java.lang.String") })
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
		try{
			Map<String, Object> attributes = nlpRequestModel.getAttributes();
			String pythonCodeFile = (String) attributes.get("Python Code File");
			String usedLibrariesVersion = (String) attributes.get("External Libraries and Versions");
			String logFileDirectory = (String) attributes.get("Log File Directory");
			String pythonLibraryPath = (String) attributes.get("Python Library Path");

	        StringBuilder content = new StringBuilder();
	        try (BufferedReader reader = Files.newBufferedReader(Paths.get(pythonCodeFile))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                content.append(line).append(System.lineSeparator());
	            }
	        }
	        String pyLogs = executePythonProgram(content.toString(), usedLibrariesVersion, pythonLibraryPath);
	        String logFileName = "PyLogs_" + new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss").format(new Date()) + ".txt";
	        String logFilePath = Paths.get(logFileDirectory, logFileName).toString();
	        new File(logFilePath).createNewFile();
	        try (FileWriter writer = new FileWriter(logFilePath)) {
	            writer.write(pyLogs);
	        }
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully executed python script");
		} catch (Throwable e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to execute python script");
		}
		return nlpResponseModel;
	}

    public static String executePythonProgram(String pythonCode, String usedLibraries_Version, String pythonLibraryPath) throws Exception {

        String installPyFile = pythonLibraryPath + File.separator + "install.py";
        StringBuilder callingStatement = new StringBuilder();
        if (!usedLibraries_Version.isBlank()) {
            String[] installLibrariesWithVersionArr = usedLibraries_Version.trim().split(",");
            for (String installLibrariesWithVersionStr : installLibrariesWithVersionArr) {
                if (installLibrariesWithVersionStr != null && !installLibrariesWithVersionStr.isBlank()) {
                    String[] installLibrariesWithVersionStrArr = installLibrariesWithVersionStr.trim().split("#");
                    if (installLibrariesWithVersionStrArr.length == 1)
                        callingStatement.append("install_and_import('").append(installLibrariesWithVersionStrArr[0].trim()).append("')\r\n");
                    else if (installLibrariesWithVersionStrArr.length == 2)
                        callingStatement.append("install_and_import('").append(installLibrariesWithVersionStrArr[0].trim()).append("','").append(installLibrariesWithVersionStrArr[1].trim()).append("')\r\n");
                }
            }
        }

        String installPythonCode = """
                import importlib
                import pip
                def install_and_import(package, version=None):
                    parts = package.split('.')
                    first_part = parts[0].strip()
                    print("Actual Package Name:", first_part)
                    package_name = f"{first_part}=={version}" if version else first_part
                    pip.main(['install', package_name])
                    print(f"{package} is installed successfully")
                %s
                print("========= All library installations were completed successfully =========")
                """.formatted(callingStatement);


        try (FileWriter writer = new FileWriter(installPyFile)) {
            writer.write(installPythonCode);
        }
        String testingPyFile = pythonLibraryPath + File.separator + "testing.py";
        try (FileWriter writer = new FileWriter(testingPyFile)) {
            writer.write(pythonCode);
        }
        String[] commands;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            commands = new String[]{"cmd.exe", "/c", "cd", "/D", pythonLibraryPath, "&&", "python", "install.py", "&&", "python", "testing.py"};
        } else {
            commands = new String[]{"bash", "-c", "cd " + pythonLibraryPath + " && python install.py && python testing.py"};
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder installLog = new StringBuilder();
        StringBuilder programLog = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                if (!flag) {
                    installLog.append(line).append(System.lineSeparator());
                }
                if (flag) {
                    programLog.append(line).append(System.lineSeparator());
                }
                flag = !flag && line.contains("========= All library installations were completed successfully =========");
                if (programLog.toString().contains("Traceback (most recent call last):") && (line.contains("Error:") || line.contains("Exception:") || line.trim().equals(""))) {
                    break;
                }
            }
        }
        int exitCode = process.waitFor();
        String pyLogs = programLog.toString().replace(pythonLibraryPath + File.separator, "");

        if (exitCode != 0) {
            throw new Exception("An exception occurred in the Python script: \n\n" + pyLogs);
        }
        return pyLogs;
    }
   
}
