package nttd;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class LaunchEmulatorService implements Nlp {
	@InputParams({ @InputParam(name = "Emulator Name", type = "java.lang.String"),
			@InputParam(name = "Max Wait Time (Seconds)", type = "java.lang.Integer") })
	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String emulatorName = (String) programElementsInput.get("Emulator Name");
		Integer maxWaitSeconds = (Integer) programElementsInput.getOrDefault("Max Wait Time (Seconds)", 300);

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (!isEmulatorRunning()) {
				launchEmulator(emulatorName);
			}

			waitForBoot(maxWaitSeconds);
			if (!ensureEmulatorStable(30)) {
				throw new RuntimeException("Emulator not stable: System UI issues detected.");
			}

			nlpResponseModel.setMessage("Emulator is ready for test execution");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setMessage("Failed: " + writer);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	private static boolean isEmulatorRunning() throws IOException {
		Process process = new ProcessBuilder("adb", "devices").start();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			return br.lines()
					.anyMatch(line -> (line.startsWith("emulator-") || line.matches("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+"))
							&& line.contains("device"));
		}
	}

	private static void launchEmulator(String avdName) throws IOException {
		String os = System.getProperty("os.name").toLowerCase();
		String sdkPath = System.getenv("ANDROID_HOME") != null ? System.getenv("ANDROID_HOME")
				: System.getenv("ANDROID_SDK_ROOT");

		if (sdkPath == null) {
			throw new RuntimeException("ANDROID_HOME or ANDROID_SDK_ROOT is not set!");
		}

		String emulatorPath = sdkPath + File.separator + "emulator" + File.separator + "emulator";

		if (os.contains("win")) {
			String cmd = "cmd.exe /c start \"Emulator\" \"" + emulatorPath + "\" -avd " + avdName
					+ " -no-snapshot-load";
			Runtime.getRuntime().exec(cmd);
		} else {
			String cmd = emulatorPath + " -avd " + avdName + " -no-snapshot-load &";
			new ProcessBuilder("/bin/bash", "-c", cmd).start();
		}
		System.out.println("Emulator launched: " + avdName);
	}

	private static void waitForBoot(int maxWaitSeconds) throws IOException, InterruptedException {
		System.out.println("Waiting for emulator to boot...");
		long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWaitSeconds);

		while (System.currentTimeMillis() < endTime) {
			Process process = new ProcessBuilder("adb", "shell", "getprop", "sys.boot_completed").start();
			process.waitFor(5, TimeUnit.SECONDS);

			try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = br.readLine();
				if ("1".equals(line)) {
					System.out.println("Emulator boot completed.");
					// Extra wait for system stabilization
					Thread.sleep(5000);
					return;
				}
			} catch (Exception ignored) {
			}

			Thread.sleep(5000);
		}

		throw new RuntimeException("Emulator boot timed out after " + maxWaitSeconds + " seconds.");
	}

	private static boolean ensureEmulatorStable(int maxWaitSeconds) throws Exception {
		System.out.println("Checking emulator stability for " + maxWaitSeconds + "s...");
		long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWaitSeconds);

		while (System.currentTimeMillis() < end) {
			if (checkSystemUiNotResponding()) {
				System.out.println("Detected 'System UI isn't responding' popup -> dismissing...");
				dismissPopup();
			}
			Thread.sleep(5000);
		}
		System.out.println("Emulator is stable.");
		return true;
	}

	private static boolean checkSystemUiNotResponding() throws IOException, InterruptedException {
		String dumpPath = "/sdcard/ui.xml";

		Process dumpProcess = new ProcessBuilder("adb", "shell", "uiautomator", "dump", dumpPath).start();
		dumpProcess.waitFor(5, TimeUnit.SECONDS);

		Process pullProcess = new ProcessBuilder("adb", "pull", dumpPath, "ui.xml").start();
		pullProcess.waitFor(5, TimeUnit.SECONDS);

		File uiFile = new File("ui.xml");
		if (!uiFile.exists())
			return false;

		try (BufferedReader br = new BufferedReader(new FileReader(uiFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("isn't responding") || line.toLowerCase().contains("system ui")) {
					return true;
				}
			}
		} finally {
			uiFile.delete();
		}
		return false;
	}

	private static void dismissPopup() throws IOException, InterruptedException {
		// Navigate to "Wait" button and press Enter
		new ProcessBuilder("adb", "shell", "input", "keyevent", "22").start().waitFor();
		new ProcessBuilder("adb", "shell", "input", "keyevent", "66").start().waitFor();
	}
}
