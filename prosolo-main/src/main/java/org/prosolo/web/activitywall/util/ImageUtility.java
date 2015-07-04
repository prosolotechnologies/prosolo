/**
 * 
 */
package org.prosolo.web.activitywall.util;

import org.prosolo.web.activitywall.data.FileType;

/**
 * @author "Nikola Milikic"
 *
 */
public class ImageUtility {

	public static boolean showImage(FileType fileType) {
		if (fileType != null) {
			switch (fileType) {
				case JPG:
				case JPEG:
				case PNG:
				case GIF:
					return true;
				default:
					return false;
			}
		}
		return false;
	}
}
