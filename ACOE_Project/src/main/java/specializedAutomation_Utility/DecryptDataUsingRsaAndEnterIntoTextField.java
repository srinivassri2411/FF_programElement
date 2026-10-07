package specializedAutomation_Utility;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;

import org.openqa.selenium.WebElement;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class DecryptDataUsingRsaAndEnterIntoTextField implements Nlp {
	@InputParams({ @InputParam(name = "Element", type = "org.openqa.selenium.WebElement"),
		@InputParam(name = "Encrypted Data", type = "java.lang.String"),
		@InputParam(name = "Private Key Path", type = "java.lang.String") })
		
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {
			WebElement element = (WebElement) programElementsInput.get("Element");
			String encryptedData = (String) programElementsInput.get("Encrypted Data");
			String privateKeyPath = (String) programElementsInput.get("Private Key Path");
			
			// Decrypt data
			byte[] privateKeyBytes = Files.readAllBytes(new File(privateKeyPath).toPath());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			String decryptedData = decryptUsingRsaType(encryptedData, privateKey);

			// Enter Decrypted Data
			element.sendKeys(decryptedData);

			// Set Pass message and status
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Entered Decrypted data into textfield.");

		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			// Set Fail message and status
			nlpResponseModel.setMessage("Failed to Decrypt and enter data.");
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}
	
	public static String decryptUsingRsaType(String encryptedMessage, PrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] cipherText = Base64.getDecoder().decode(encryptedMessage);
		byte[] result = cipher.doFinal(cipherText);
		return new String(result, StandardCharsets.UTF_8);
	}
	
}