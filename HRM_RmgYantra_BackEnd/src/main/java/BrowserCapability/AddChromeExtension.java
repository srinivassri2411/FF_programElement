package BrowserCapability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class AddChromeExtension implements Nlp {
	@InputParams({ @InputParam(name = "CRX File", type = "com.amazonaws.services.s3.model.S3ObjectInputStream"),
			@InputParam(name = "Desired Capabilities", type = "org.openqa.selenium.remote.DesiredCapabilities") })
	@ReturnType(name = "DesiredCapabilities_out", type = "org.openqa.selenium.remote.DesiredCapabilities")
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

		InputStream inputStream = (InputStream) attributes.get("CRX File");
		File file = new File("extensionFile.crx");
		Exception exc = null;
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

		} catch (Exception e) {
			exc = e;
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		DesiredCapabilities desiredCapabilities = (DesiredCapabilities) attributes.get("Desired Capabilities");
		try {
			ChromeOptions options = new ChromeOptions();
			options.addExtensions(file);

			options.addArguments("--remote-allow-origins=*", "ignore-certificate-errors");

			desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
			if (exc == null) {
				nlpResponseModel.setStatus(CommonConstants.pass);
				nlpResponseModel.setMessage("Chrome Browser is Opened with Extension");
			}

		} catch (Exception e) {
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed To Open Chrome With Extension" + e + exc);
		}
		nlpResponseModel.getAttributes().put("DesiredCapabilities_out", desiredCapabilities);
		return nlpResponseModel;
	}
}