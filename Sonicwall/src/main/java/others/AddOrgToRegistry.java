
package others;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AddOrgToRegistry implements Nlp {
	@InputParams({ @InputParam(name = "Org Name", type = "java.lang.String"),
			@InputParam(name = "Url", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String orgName = (String) programElementsInput.get("Org Name");
		String url = (String) programElementsInput.get("Url");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			setChromePolicyRegistryValue(RandomStringUtils.randomNumeric(9), orgName);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully added the org ");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to add the org " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void setChromePolicyRegistryValue(String randomId, String orgName)
			throws IOException, InterruptedException {
		System.out.println("id " + randomId);
		if (!randomId.matches("\\d{9}")) {
			throw new IllegalArgumentException("RandomID must be a 9-digit number.");
		}
		if (orgName == null || orgName.trim().isEmpty()) {
			throw new IllegalArgumentException("OrgName must not be empty.");
		}

		String json = String.format(
				"{\"pattern\":\"%s.trust-release.bnntest.com\",\"filter\":{\"SUBJECT\":{\"CN\":\"ManagedDevice-BNN-%s\"}}}",
				orgName, orgName);

		// Create PowerShell script content
		String psScript = String.format(
				"$regPath = \"HKLM:\\\\SOFTWARE\\\\Policies\\\\Google\\\\Chrome for Testing\\\\AutoSelectCertificateForUrls\"\n"
						+ "if (-not (Test-Path $regPath)) { New-Item -Path $regPath -Force | Out-Null }\n"
						+ "Set-ItemProperty -Path $regPath -Name \"%s\" -Value '%s' -Type String\n"
//                        +
//                        "Write-Host \"Registry key set for %s with ID %s\"\n"
//                        "Pause\n"
				, randomId, json, orgName, randomId);

		Path psFile = Files.createTempFile("SetChromePolicy-", ".ps1");
		Files.write(psFile, psScript.getBytes());

		System.out.println("Generated PowerShell script at: " + psFile);

		String command = String.format(
				"powershell.exe -NoProfile -ExecutionPolicy Bypass -Command \"Start-Process powershell.exe -ArgumentList '-ExecutionPolicy Bypass -File \\\"%s\\\"' -Verb RunAs\"",
				psFile.toAbsolutePath().toString());

		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();

		System.out.println("Launched PowerShell script with elevation.");
//		Files.deleteIfExists(psFile);
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Org Name", "jdsfkjds");
		model.getAttributes().put("Url", "");
		System.out.println(new AddOrgToRegistry().execute(model));
	}
}