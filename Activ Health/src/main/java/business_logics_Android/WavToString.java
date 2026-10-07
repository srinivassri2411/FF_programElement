package business_logics_Android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class WavToString implements Nlp {

    @InputParams({
    	@InputParam(name = "Wav Audio File Path", type = "String"), 
    	})
    @ReturnType(name = "Output", type = "String")

      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String wavAudioFilePath = (String) programElementsInput.get("Wav Audio File Path");

          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
        	  wavAudioFilePath=wavAudioFilePath.replace("\\", "/");
        	  String returnValue = transcribeAudioToText(wavAudioFilePath);
              nlpResponseModel.setStatus(CommonConstants.pass);
              nlpResponseModel.setMessage("Successfully converted audio to text "+returnValue);
           System.out.println("Successfully converted audio to text "+returnValue);


              
              nlpResponseModel.getAttributes().put("Output", returnValue);
          }
          catch(Exception e) {
        	 StringWriter sw=new StringWriter();
        	 PrintWriter pw=new PrintWriter(sw);
        	 e.printStackTrace(pw);
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to Convert as text from the Audio ==> \n"+sw.toString());
          }

          return nlpResponseModel;
      }
    
    public static String transcribeAudioToText(String audioFilePath) throws Exception {
        ensurePythonInstalled();

        // Install required Python libraries
        installPythonLibraries();

        // Python code to process and transcribe audio in chunks
        String pythonCode = 
                "import speech_recognition as sr\n" +
                "from pydub import AudioSegment\n\n" +

                "# Function to split audio into chunks\n" +
                "def split_audio(audio_file, chunk_length_ms):\n" +
                "    audio = AudioSegment.from_wav(audio_file)\n" +
                "    return [audio[i:i + chunk_length_ms] for i in range(0, len(audio), chunk_length_ms)]\n\n" +

                "# Function to convert audio chunks to text\n" +
                "def audio_chunks_to_text(audio_file):\n" +
                "    recognizer = sr.Recognizer()\n" +
                "    chunks = split_audio(audio_file, 30000)  # 30 seconds chunks\n" +
                "    full_text = \"\"\n\n" +

                "    for i, chunk in enumerate(chunks):\n" +
                "        chunk.export(\"chunk.wav\", format=\"wav\")  # Export chunk to a temporary file\n" +
                "        with sr.AudioFile(\"chunk.wav\") as source:\n" +
                "            audio = recognizer.record(source)\n" +
                "        try:\n" +
                "            text = recognizer.recognize_google(audio)\n" +
                "            full_text += text + \" \"\n" +
                "        except sr.UnknownValueError:\n" +
                "            print(f\"Chunk {i} could not be understood\")\n" +
                "        except sr.RequestError as e:\n" +
                "            print(f\"Could not request results for chunk {i}; {e}\")\n\n" +

                "    print(\"Full Transcribed Text: \", full_text)\n\n" +

                "audio_chunks_to_text('" + audioFilePath.replace("\\", "\\\\") + "')";

        // Create a temporary Python file
        File tempPythonFile = File.createTempFile("audio_to_text", ".py");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempPythonFile))) {
            writer.write(pythonCode);
        }

        ProcessBuilder processBuilder = new ProcessBuilder("python", tempPythonFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Capture both standard and error output of the Python script
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("Error Output:\n" + errorOutput.toString());  // Print the error output
            throw new RuntimeException("Python script failed with exit code " + exitCode);
        }

        // Delete the temporary Python file
        tempPythonFile.delete();

        return output.toString().trim();
    }

    private static void ensurePythonInstalled() throws Exception {
        try {
            ProcessBuilder checkPython = new ProcessBuilder("python", "--version");
            Process process = checkPython.start();
            process.waitFor();

            if (process.exitValue() != 0) {
//             log.info("Python is not installed!");
            }
        } catch (IOException | InterruptedException e) {
            installPython();
        }
    }

    private static void installPython() throws Exception {
        System.out.println("Python is not installed. Downloading and installing...");
        String pythonInstallerUrl = "https://www.python.org/ftp/python/3.10.9/python-3.10.9-amd64.exe";
        String installerPath = "python_installer.exe";

        downloadFile(pythonInstallerUrl, installerPath);

        new ProcessBuilder(installerPath, "/quiet", "InstallAllUsers=1", "PrependPath=1").start().waitFor();

        new File(installerPath).delete();

        System.out.println("Python installation completed.");
    }

    private static void installPythonLibraries() throws Exception {
        System.out.println("Installing required Python libraries...");
        String[] libraries = {"speechrecognition", "pydub"};

        for (String library : libraries) {
            ProcessBuilder pipInstall = new ProcessBuilder("python", "-m", "pip", "install", library);
            Process process = pipInstall.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to install Python library: " + library);
            }
        }
        System.out.println("Python libraries installed.");
    }

    private static void downloadFile(String url, String destination) throws IOException {
        try (InputStream in = new java.net.URL(url).openStream()) {
            Files.copy(in, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }
    }



    
    public static void maain(String[] args) throws NlpException {
		NlpRequestModel nlpRequestModel=new NlpRequestModel();
		Map<String, Object> map = nlpRequestModel.getAttributes();
		//map.put("AccessToken", "EB5BRUAV5EB4FBR4Z3NQSDFIJWLBRP3E");
	//	map.put("FilePath", "C:\\Users\\User\\Downloads\\20250127_160539.mp3");
		map.put("Wav Audio File Path", "C:\\Users\\User\\Downloads\\20250128_104839.wav");
		nlpRequestModel.setAttributes(map);
		new WavToString().execute(nlpRequestModel);
	}
}
