package WindowsNLPS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class PerformAction implements Nlp {
	static Path tempDir = null;

	@InputParams({ @InputParam(name = "Title", type = "java.lang.String"),
			@InputParam(name = "NameType", type = "java.lang.String"),
			@InputParam(name = "Name", type = "java.lang.String"),
			@InputParam(name = "Type", type = "java.lang.String"),
			@InputParam(name = "Input", type = "java.lang.String") })
//	@ReturnType(name = "text", type = "java.lang.String")

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		System.out.println("dfg");
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String output = null;
		String title = (String) programElementsInput.get("Title");
		String nameT = (String) programElementsInput.get("NameType");
		String name = (String) programElementsInput.get("Name");
		String type = (String) programElementsInput.get("Type");
		String input = (String) programElementsInput.get("Input");

		try {

			// String audioFilePath = "C:\\Users\\User\\Downloads\\Call recording
			// 18002086633_251217_102052.wav";

			// String pythonSafePath = audioFilePath
			// .replace("\\", "\\\\")
			// .replace("\"", "\\\"");
			String pythonTemplate = null;
			if (input.equals("")) {
				pythonTemplate = """
						# -- coding: utf-8 --
						from pywinauto import Application, Desktop
						import time
						import pyperclip
						from pywinauto.keyboard import send_keys
						window_title = "%s"
						namevalue = "%s"
						typeValue = "%s"
						nameTYPE ="%s"
						dlg = Desktop(backend="uia").window(title=window_title)
						#dlg = Application(backend="uia").connect(process=11952)
						#dlg.wait("visible", timeout=20)
						dlg.set_focus()
						dlg.child_window(nameTYPE=namevalue, control_type=typeValue).click_input()
						#send_keys("^v")
						""".formatted(title, name, type, nameT);
			} else {
				pythonTemplate = """
						# -- coding: utf-8 --
						from pywinauto import Application, Desktop
						import time
						import pyperclip
						from pywinauto.keyboard import send_keys
						window_title = "%s"
						namevalue = "%s"
						typeValue = "%s"
						inputValue= "%s"
						nameTYPE ="%s"
						dlg = Desktop(backend="uia").window(title=window_title)
						#dlg = Application(backend="uia").connect(process=11952)
						#dlg.wait("visible", timeout=20)
						dlg.set_focus()
						dlg.child_window(nameTYPE=namevalue, control_type=typeValue).click_input()
						pyperclip.copy(inputValue)
						send_keys("^v")
						""".formatted(title, name, type, input, nameT);
			}

			// pythonTemplate = """
			// # -- coding: utf-8 --
			// from pywinauto import Application, Desktop
			// import time
			// import pyperclip
			// from pywinauto.keyboard import send_keys
			// window_title = "%s"
			// namevalue = "%s"
			// typeValue = "%s"
			// dlg = Desktop(backend="uia").window(title=window_title)
			// #dlg = Application(backend="uia").connect(process=11952)
			// #dlg.wait("visible", timeout=20)
			// dlg.set_focus()
			// dlg.child_window(title="namevalue", control_type="typeValue").click_input()
			// """.formatted(title, name, type);

			// String pythonCode = String.format(pythonTemplate, pythonSafePath);

			System.out.println("before");

			output = executePythonScript(pythonTemplate);
			System.out.println(output);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully performed the action");

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to perform action " + sw);
		}
//		nlpResponseModel.getAttributes().put("text", output);

		return nlpResponseModel;
	}

	public static String executePythonScript(String pythonCode) throws IOException, InterruptedException {

		tempDir = Files.createTempDirectory("py_exec_");
		Path scriptFile = tempDir.resolve("audio_transcribe.py");

		Files.write(scriptFile, pythonCode.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);

		ProcessBuilder pb = new ProcessBuilder("python", scriptFile.toString());
		pb.directory(tempDir.toFile());
		pb.environment().put("PYTHONIOENCODING", "utf-8");
		pb.redirectErrorStream(true);

		Process process = pb.start();

		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
		}

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Python execution failed:\n" + output);
		}

		return output.toString().trim();
	}

	private static String toPythonRawString(String path) {
		if (path == null)
			return "";
		// Escape only quotes, backslashes stay as-is for raw strings
		return path.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	public static void main(String[] args) throws NlpException {

	}
}