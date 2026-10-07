	
package sonicWall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class DeleteOrgFromRegistry implements Nlp {
	@InputParams({ @InputParam(name = "Org Name", type = "java.lang.String"),
			@InputParam(name = "Url", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String orgName = (String) programElementsInput.get("Org Name");
		String url = (String) programElementsInput.get("Url");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			deleteChromePolicyRegistryValues(orgName);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully deleted the org ");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to delete the org " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static void deleteChromePolicyRegistryValues(String orgName) throws IOException, InterruptedException {
		if (orgName == null || orgName.trim().isEmpty()) {
			throw new IllegalArgumentException("OrgName must not be empty.");
		}

		// The pattern to search inside each registry value (like your JSON 'pattern')
		String pattern = String.format("%s.trust-release.bnntest.com", orgName);

		// PowerShell script to delete matching values
		String psScript = String.format(
				"$regPath = \"HKLM:\\SOFTWARE\\Policies\\Google\\Chrome for Testing\\AutoSelectCertificateForUrls\"\n"
						+ "if (Test-Path $regPath) {\n" + "    $values = Get-ItemProperty -Path $regPath\n"
						+ "    foreach ($name in $values.PSObject.Properties.Name) {\n"
						+ "        $valueData = Get-ItemPropertyValue -Path $regPath -Name $name\n"
						+ "        if ($valueData -like '*%s*') {\n"
						+ "            Remove-ItemProperty -Path $regPath -Name $name\n"
						+ "            Write-Host \"Deleted registry value: $name\"\n" + "        }\n" + "    }\n"
						+ "} else {\n" + "    Write-Host \"Registry path does not exist.\"\n" + "}\n",
				pattern);

		Path psFile = Files.createTempFile("DeleteChromePolicy-", ".ps1");
		Files.write(psFile, psScript.getBytes());

		System.out.println("Generated PowerShell script at: " + psFile);

		String command = String.format(
				"powershell.exe -NoProfile -ExecutionPolicy Bypass -Command \"Start-Process powershell.exe -ArgumentList '-ExecutionPolicy Bypass -File \\\"%s\\\"' -Verb RunAs\"",
				psFile.toAbsolutePath().toString());

		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();

		System.out.println("Launched PowerShell script with elevation to delete matching registry values.");
	}

	public static void main(String[] args) throws NlpException, IOException, InterruptedException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("Org Name", "qa-automation-yaswitha");
		model.getAttributes().put("Url", "");
		System.out.println(new DeleteOrgFromRegistry().execute(model));
//		deleteChromePolicyRegistryValues("qa-automation-yaswitha");
	}
}