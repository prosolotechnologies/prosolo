package org.prosolo.util.urigenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.prosolo.app.Settings;

/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
public class MD5HashUtility {
	public static String getMD5Hex(final String inputString) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(inputString.getBytes());

	    byte[] digest = md.digest();

	    return convertByteToHex(digest);
	}
	
	private static String convertByteToHex(byte[] byteData) {
	    StringBuilder sb = new StringBuilder();
	    
	    for (int i = 0; i < byteData.length; i++) {
	        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
	
	public static String generateKeyForFilename(String fileName){
		Date now = new Date();
		String key = null;
		
		try {
			key = MD5HashUtility.getMD5Hex(String.valueOf(now.getTime()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		key=Settings.getInstance().config.fileManagement.urlPrefixFolder+key+"/"+fileName;
		return key;
	}
}
