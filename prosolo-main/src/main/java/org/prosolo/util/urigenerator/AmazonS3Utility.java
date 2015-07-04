package org.prosolo.util.urigenerator;

import org.prosolo.app.Settings;

/**
 *
 * @author Zoran Jeremic, May 28, 2014
 *
 */
public class AmazonS3Utility {
	
	private static String prefix = Settings.getInstance().config.fileManagement.fileStoreServiceUrl + 
									"/" + Settings.getInstance().config.fileManagement.fileStoreBucketName + 
									"/";

	public static String createFullPathFromRelativePath(String relativeFilePath){
		return prefix + relativeFilePath;
	}
	
	public static String getRelativeFilePathFromFullS3Path(String fullPath) {
		if (fullPath.startsWith(prefix)) {
			return fullPath.replace(prefix, "");
		}
		return fullPath;
	}
}
