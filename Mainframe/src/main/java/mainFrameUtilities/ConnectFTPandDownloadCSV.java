
package mainFrameUtilities;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ConnectFTPandDownloadCSV implements Nlp {
	@InputParams({ @InputParam(name = "UserName", type = "java.lang.String"),
			@InputParam(name = "Password", type = "java.lang.String"),
			@InputParam(name = "FileName", type = "java.lang.String"),
			@InputParam(name = "Host", type = "java.lang.String") })
	@ReturnType(name = "FilePath", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String tempDir = System.getProperty("java.io.tmpdir");
		String userName = (String) programElementsInput.get("UserName");
		String password = (String) programElementsInput.get("Password");
		String fileName = (String) programElementsInput.get("FileName");
		String host = (String) programElementsInput.get("Host");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			FTPClient ftp = new FTPClient();

			ftp.connect(host);

			ftp.login(userName, password);

			ftp.changeWorkingDirectory("/tmp");
			FileOutputStream fos = new FileOutputStream(tempDir + fileName + ".csv");
			ftp.retrieveFile(fileName + ".csv", fos);
			fos.close();
			ftp.logout();
			ftp.disconnect();
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("File Downloaded Successfully at " + tempDir + fileName + ".csv");
		} catch (Exception e) {
			StringWriter exception = new StringWriter();
			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Unable to download the file" + "\n" + exception);

		}

		// Your program element business logic ends here ...
		returnValue = tempDir + fileName + ".csv";
		nlpResponseModel.getAttributes().put("FilePath", returnValue);
		return nlpResponseModel;
	}

	public static void main(String[] args) throws NlpException {
		NlpRequestModel req = new NlpRequestModel();
		req.getAttributes().put("Host", "pub400.com");
		req.getAttributes().put("FileName", "transactions");
		req.getAttributes().put("Password", "asdf123");
		req.getAttributes().put("UserName", "FIREFLINK");
		System.out.println(new ConnectFTPandDownloadCSV().execute(req));
	}
}