
package sonicWall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

public class CleanBanyanFiles implements Nlp {

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			String os = System.getProperty("os.name");
			System.out.println(os);
			if (os.toLowerCase().contains("windows")) {
				cleanWin();
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Cleanup completed successfully");
			} else if (os.toLowerCase().contains("mac")) {
				cleanMac();
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Cleanup completed successfully");
			} else {
				throw new RuntimeException("Unsupported Os Details " + os);
			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to complete cleanup" + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void cleanWin() {
		try {
			Path psScriptPath = Files.createTempFile("cleanup-banyan-", ".ps1");

			String psScript = """
					Write-Output "Stopping processes..."
					Stop-Process -Name "banyan" -Force -ErrorAction SilentlyContinue
					Stop-Process -Name "banyanwgs" -Force -ErrorAction SilentlyContinue

					Write-Output "Deleting Banyan folder..."
					Remove-Item -Recurse -Force "C:\\Program Files\\Banyan" -ErrorAction SilentlyContinue

					Write-Output "Deleting folders..."
					Remove-Item -Recurse -Force "$env:APPDATA\\banyanapp" -ErrorAction SilentlyContinue
					Remove-Item -Recurse -Force "$env:ALLUSERSPROFILE\\Banyan" -ErrorAction SilentlyContinue

					Write-Output "Deleting SSH configs..."
					Remove-Item -Recurse -Force "$env:USERPROFILE\\.ssh\\bnn.config" -ErrorAction SilentlyContinue
					Remove-Item -Recurse -Force "$env:USERPROFILE\\.ssh\\bnn-dev.config" -ErrorAction SilentlyContinue

					Write-Output "Deleting saved credentials..."
					cmdkey /delete:Banyan Safe Storage/Banyan

					Write-Output "Cleanup script completed successfully."
					Start-Sleep -Seconds 2
					Remove-Item -Path $MyInvocation.MyCommand.Path -Force
					""";

			Files.writeString(psScriptPath, psScript);
			System.out.println("Created PowerShell script at: " + psScriptPath);

			String psCommand = String.format(
					"Start-Process powershell.exe -Verb RunAs -ArgumentList '-ExecutionPolicy Bypass -NoProfile -File \"%s\"'",
					psScriptPath.toAbsolutePath().toString());

			ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-Command", psCommand);
			builder.inheritIO();
			Process process = builder.start();
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				throw new RuntimeException("Cleanup script failed with exit code: " + exitCode);
			}

			System.out.println("Cleanup completed successfully");

		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Error executing cleanup", e);
		}
	}

	public static void cleanMac() {
		String[] commands = { "echo \"Unloading System Services\"",
				"sudo launchctl unload -w \"/Library/LaunchDaemons/com.banyan.banyanapp-admin.plist\"",
				"sudo launchctl unload -w \"/Library/LaunchDaemons/com.banyan.banyanwgs.plist\"",
				"sudo rm -f /Library/LaunchDaemons/com.banyan.banyanapp-admin.plist",
				"sudo rm -f /Library/LaunchDaemons/com.banyan.banyanwgs.plist", "echo \"Deleting app folders\"",
				"rm -rf \"$HOME/Library/Application Support/banyanapp\"", "rm -f \"$HOME/.ssh/bnn.config\"",
				"rm -f \"$HOME/.ssh/bnn-dev.config\"", "sudo rm -rf /etc/banyanapp", "echo \"Deleting application\"",
				"sudo rm -rf /Applications/Banyan.app", "echo \"Deleting stored password\"",
				"security delete-generic-password -l \"Banyan Safe Storage\"",
				"echo \"Uninstall Complete. Open Keychain and ensure user login certificates are removed\"" };

		for (String command : commands) {
			executeCommand(new String[] { "bash", "-c", command });
		}
	}

	private static void executeCommand(String[] command) {
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			Process process = builder.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new RuntimeException("Command failed: " + String.join(" ", command));
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Error running command: " + String.join(" ", command));
		}
	}

}