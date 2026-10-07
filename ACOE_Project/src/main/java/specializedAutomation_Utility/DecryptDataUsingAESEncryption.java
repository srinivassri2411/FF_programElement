
package specializedAutomation_Utility;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptDataUsingAESEncryption implements Nlp {
	@InputParams({ @InputParam(name = "Data to be Decrypted", type = "java.lang.String"),
			@InputParam(name = "Secret Key", type = "java.lang.String") })
	@ReturnType(name = "encrypt", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String input = (String) programElementsInput.get("Data to be Decrypted");
		String secretKey = (String) programElementsInput.get("Secret Key");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = decrypt(input, secretKey);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Given text is Decrypted");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Decrypt given text " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("encrypt", returnValue);
		return nlpResponseModel;
	}

	public String decrypt(String input, String secretKey) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
		IvParameterSpec ivParameterSpec = new IvParameterSpec("4234567890123456".getBytes());
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(input));
		return new String(decrypted);
	}
}