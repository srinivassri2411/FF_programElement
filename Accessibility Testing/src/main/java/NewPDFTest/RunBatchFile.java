package NewPDFTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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




public class RunBatchFile implements Nlp {
	@InputParams({ @InputParam(name = "Batch_FilePath", type = "java.lang.String") })
	@ReturnType(name = "response", type = "java.lang.String")

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
		Map<String, Object> attributes = nlpRequestModel.getAttributes();
		String batch_FilePath = (String) attributes.get("Batch_FilePath");

	
		try {
			String response = runBatchFile(batch_FilePath);
			nlpResponseModel.getAttributes().put("response", response);
			nlpResponseModel.setMessage("Batch file execution is successful.");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to execute batch file." + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

	public static String runBatchFile(String batchFilePath) throws IOException, InterruptedException {
       
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", batchFilePath);

    
        Process process = processBuilder.start();

     
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

           
            while ((line = errorReader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Process exited with code: " + exitCode);
        }

        return response.toString();
    }
}