package org.prosolo.util.urigenerator;

import org.prosolo.common.config.CommonSettings;

/**
 *
 * @author Zoran Jeremic, May 28, 2014
 *
 */
public class AmazonS3Utility {
	
	public static String createFullPathFromRelativePath(String relativeFilePath){
		return CommonSettings.getInstance().config.fileStore.virtualHostingUrl + "/"+ relativeFilePath;
	}
	
}
