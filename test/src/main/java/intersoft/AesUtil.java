package intersoft;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {
	public static final int GCM_TAG_LENGTH = 16;

	public byte[] IV = null;

	private Cipher decCipher;

	private SecretKey secretKey;

	private static final String AES = "AES";

	private static final String CIPHER_TRANS = "AES/GCM/NoPadding";

	public AesUtil(String workingKey) throws Exception {
		this.secretKey = new SecretKeySpec(workingKey.getBytes(), 0, (workingKey.getBytes()).length, "AES");
		this.decCipher = Cipher.getInstance("AES/GCM/NoPadding");
	}

	public String decrypt(String pEncryptedText) throws Exception {
		String encodedIV = pEncryptedText.substring(0, 24);
		this.IV = hexToByte(encodedIV);
		pEncryptedText = pEncryptedText.substring(24, pEncryptedText.length());
		this.decCipher.init(2, this.secretKey, new GCMParameterSpec(128, this.IV));
		byte[] decodedBytes = hexToByte(pEncryptedText);
		byte[] decryptedData = null;
		try {
			decryptedData = this.decCipher.doFinal(decodedBytes);
		} catch (Exception e) {
			throw new RuntimeException("Invalid or expired token.");
		}
		return new String(decryptedData);	
	}

	public static byte[] hexToByte(String hexString) {
		int len = hexString.length();
		byte[] ba = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
			ba[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		return ba;
	}
}
