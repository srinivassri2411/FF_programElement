
package sonicWall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class VerifyRDPConnection implements Nlp {
	@InputParams({ @InputParam(name = "IP Address", type = "java.lang.String") })
	@ReturnType(name = "connection", type = "java.lang.Boolean")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String iPAddress = (String) programElementsInput.get("IP Address");
		Boolean returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = isRdpConnectedToIp(iPAddress);
			if (returnValue) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("RDP connection is avaliable at " + iPAddress);
			} else {
				nlpResponseModel.setStatus(CommonConstants.fail);
				nlpResponseModel.setMessage("RDP connection is not avaliable at " + iPAddress);
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("RDP connection is not avaliable at " + iPAddress + " Exception is " + writer);
		}
		nlpResponseModel.getAttributes().put("connection", returnValue);
		return nlpResponseModel;
	}

	public static boolean isRdpConnectedToIp(String targetIp) throws IOException, InterruptedException {
		Path tempPs1 = Files.createTempFile("check-rdp-", ".ps1");

		String psScript = """
				netstat -ano | findstr ESTABLISHED | ForEach-Object {
				    $parts = ($_ -split "\\s+") | Where-Object { $_ -ne "" }
				    $localAddr  = $parts[1]
				    $remoteAddr = $parts[2]
				    $procId     = $parts[-1]
				    $procName   = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName

				    if ($procName -match "mstsc") {
				        Write-Output "RDP Active and Connected To: $remoteAddr"
				    }
				}
				""";
		Files.writeString(tempPs1, psScript);

		String command = "powershell.exe -ExecutionPolicy Bypass -File \"" + tempPs1.toAbsolutePath() + "\"";

		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
		builder.redirectErrorStream(true);
		Process process = builder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		boolean matchFound = false;

		while ((line = reader.readLine()) != null) {
			System.out.println("[PowerShell Output] " + line);
			if (line.contains(targetIp)) {
				matchFound = true;
			}
		}

		process.waitFor();
		Files.deleteIfExists(tempPs1);

		return matchFound;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel model = new NlpRequestModel();
		model.getAttributes().put("IP Address", "49.249.29.6:3389");
		System.out.println(new VerifyRDPConnection().execute(model));
	}

}