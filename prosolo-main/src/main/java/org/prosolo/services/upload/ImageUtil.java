/**
 * 
 */
package org.prosolo.services.upload;

/**
 * @author "Nikola Milikic"
 *
 */
public class ImageUtil {

	public static boolean checkIfImage(String url) {
		String extension = getExtension(url);
		
		if (extension != null) {
			if (extension.equals("gif") || 
					extension.equals("jpg") || 
					extension.equals("jpeg") || 
					extension.equals("bmp") || 
					extension.equals("png")) {
				return true;
			}
		} return false;
	}
	
	public static String getExtension(String url) {
		int indexOfLastDot = url.lastIndexOf(".");
		
		if (indexOfLastDot >= 0) {
			return url.substring(indexOfLastDot + 1);
		}
		return null;
	}
}
