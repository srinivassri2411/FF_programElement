
package encryption;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDataUsingAESEncryption implements Nlp {
	@InputParams({ @InputParam(name = "Data to be encrypted", type = "java.lang.String") })
	@ReturnType(name = "encrypt", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String input = (String) programElementsInput.get("Data to be encrypted");
		String returnValue = null;

		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			returnValue = encrypt(input);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Given text is Encrypted");
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed to Encrypt given text " + e);
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		nlpResponseModel.getAttributes().put("encrypt", returnValue);
		return nlpResponseModel;
	}

	public String encrypt(String input) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(new String("K9#mP" + "2$vL8@qX7!n").getBytes(), "AES");
		IvParameterSpec ivParameterSpec = new IvParameterSpec("4234567890123456".getBytes());
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(input.getBytes());
		return Base64.getEncoder().encodeToString(encrypted);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(new EncryptDataUsingAESEncryption().encrypt("Password@123"));
	}
}