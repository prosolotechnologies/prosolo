package org.prosolo.util.string;

/**
 * @author Zoran Jeremic 2013-10-06
 *
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;


public class RandomPasswordGenerator {
	private static Logger logger = Logger.getLogger(RandomPasswordGenerator.class);

	public static String generateKeyForUser(String userEmail) {
		String key = userEmail + System.currentTimeMillis();
		String salt = "3c7a507HN%2BSVmLc6FRfRtePP3Hj753lW";

		String generateHmacSHA256Signature;
		String urlEncodedSign = null;

		try {
			generateHmacSHA256Signature = generateHmacSHA256Signature(salt,	key);
			urlEncodedSign = URLEncoder.encode(generateHmacSHA256Signature,	"UTF-8");
		} catch (GeneralSecurityException e) {
			logger.error(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}

		return urlEncodedSign;
	}

	private static String generateHmacSHA256Signature(String data, String key) throws GeneralSecurityException {
		byte[] hmacData = null;

		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKey);
			hmacData = mac.doFinal(data.getBytes("UTF-8"));
			byte[] encoded = Base64.encodeBase64(hmacData);
			// new Base64Encoder().encode(hmacData, arg1, arg2, arg3)
			return new String(encoded);
		} catch (UnsupportedEncodingException e) {
			throw new GeneralSecurityException(e);
		}
	} 
}
