package specializedAutomation_utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;




public class DecryptGivenMessageUsingRSA implements Nlp {
	@InputParams({ @InputParam(name = "Encrypted Data", type = "java.lang.String"),
			@InputParam(name = "Private Key", type = "java.io.InputStream") })
	@ReturnType(name = "string3", type = "java.lang.String")

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
		String decryptedMessage = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			String encryptedMessage = (String) attributes.get("Encrypted Data");
			inputStream = (InputStream) attributes.get("Private Key");
			String home = System.getProperty("user.home");
			File outputFile = new File(home + File.separator + "private.key");
			outputStream = new FileOutputStream(outputFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			byte[] privateKeyBytes = Files.readAllBytes(outputFile.toPath());

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			decryptedMessage = decrypt(encryptedMessage, privateKey);
			nlpResponseModel.setMessage("Successfully Decrypted given message");
			nlpResponseModel.setStatus(CommonConstants.pass);
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Decrypt the given message");
			nlpResponseModel.setStatus(CommonConstants.fail);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		nlpResponseModel.getAttributes().put("string3", decryptedMessage);
		return nlpResponseModel;
	}

	public static String decrypt(String encryptedMessage, PrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] cipherText = Base64.getDecoder().decode(encryptedMessage);
		byte[] result = cipher.doFinal(cipherText);
		return new String(result, StandardCharsets.UTF_8);
	}
}