
package sonicWall;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;

public class DeleteChromeProfile implements Nlp {

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

//		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			removeProfile();
			Thread.sleep(1500);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Removed all the profiles");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to remove all the profiles: " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void removeProfile() throws Exception {
		String profileToKeep = "Default";
		File tempDir = Files.createTempDirectory("chrome_cleanup").toFile();
		File ps1 = new File(tempDir, "DeleteChromeProfiles.ps1");
		String script = """
				param(
				    [string]$KeepProfile
				)

				# Chrome For Testing User Data Path
				$root = "$env:LOCALAPPDATA\\Google\\Chrome for Testing\\User Data"

				Write-Host "Chrome User Data: $root"
				Write-Host "Keeping ONLY: $KeepProfile"

				# Delete all profile folders except $KeepProfile
				Get-ChildItem $root -Directory | ForEach-Object {
				    if ($_.Name -ne $KeepProfile) {
				        Write-Host "Deleting: $($_.Name)"
				        Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
				    }
				}

				# Fix Local State
				$localState = Join-Path $root "Local State"
				if (Test-Path $localState) {
				    $json = Get-Content $localState -Raw | ConvertFrom-Json

				    # Ensure JSON structure and Remove all info_cache except the one we keep
				    if ($json.profile -and $json.profile.info_cache) {
				        $keys = @($json.profile.info_cache.PSObject.Properties.Name)
				        foreach ($key in $keys) {
				            if ($key -ne $KeepProfile) {
				                Write-Host "Removing info_cache entry: $key"
				                $json.profile.info_cache.PSObject.Properties.Remove($key)
				            }
				        }

				        # Update last_used profile
				        if ($json.profile.last_used) {
				            $json.profile.last_used = $KeepProfile
				        }
				    }

				    # Save the Local State file back
				    $json | ConvertTo-Json -Depth 20 | Set-Content $localState -Encoding UTF8
				}

				Write-Host "Profile cleanup finished."
				""";
		try (FileWriter fw = new FileWriter(ps1)) {
			fw.write(script);
		}

		System.out.println("Created PS1 at: " + ps1.getAbsolutePath());

		String command = "powershell.exe -Command " + "\"Start-Process powershell -Verb RunAs "
				+ "-ArgumentList '-ExecutionPolicy Bypass -File \"" + ps1.getAbsolutePath() + "\" -KeepProfile \""
				+ profileToKeep + "\"'\"";

		System.out.println("Running elevated script...");
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
	}

}